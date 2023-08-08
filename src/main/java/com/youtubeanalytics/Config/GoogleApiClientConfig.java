package com.pacescape.userms.youtube.Config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.pacescape.exception.PaceScapeException;
import com.pacescape.exception.PaceScapeRuntimeException;
import com.pacescape.userms.youtube.entity.YoutubeToken;
import com.pacescape.userms.youtube.service.YoutubeService;
import com.pacescape.userms.youtube.utility.YoutubeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

@Configuration
public class GoogleApiClientConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${youtube.redirectUri}")
    private String redirectUri;

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    @Autowired
    private YoutubeService youtubeService;

    @Bean
    public AuthorizationCodeFlow googleAuthorizationCodeFlow(HttpTransport httpTransport) throws IOException {
        Details details = new Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setRedirectUris(Collections.singletonList(redirectUri));

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(details);

        GoogleAuthorizationCodeFlow.Builder builder =
                new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton("https://www.googleapis.com/auth/youtubepartner"));

        builder.setAccessType("offline");
        builder.setApprovalPrompt("force");
        return builder.build();
    }

    @Bean
    public HttpTransport httpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    public void refreshAccessToken(UUID userId) throws PaceScapeException, JsonProcessingException {

        YoutubeToken youtubeToken = youtubeService.getYoutubeToken(userId).orElseThrow(() ->
                new PaceScapeRuntimeException("Invalid User", HttpStatus.NOT_FOUND));

        UriComponentsBuilder refreshTokenUriBuilder = UriComponentsBuilder.fromHttpUrl(YoutubeConstants.ACCESSTOKEN_REFRESH_URL)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", youtubeToken.getRefreshToken());

        try {
            ResponseEntity<Object> refreshTokenApiResponse = new RestTemplate().exchange(
                    refreshTokenUriBuilder.toUriString(),
                    HttpMethod.POST,
                    new HttpEntity<>(null),
                    Object.class
            );
            youtubeService.updateYoutubeToken(youtubeToken, refreshTokenApiResponse.getBody());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                youtubeService.removeAuthorization(youtubeToken);
                throw new PaceScapeException("Bad Request : check refresh Token or Authorize the user once");

            }
        }


    }

}

