package com.youtubeanalytics.Controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.youtubeanalytics.Config.GoogleApiClientConfig;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class OAuthController {

    @Autowired
    private AuthorizationCodeFlow authorizationCodeFlow;
    @Autowired
    private GoogleApiClientConfig config;

    @Autowired
    private LocalServerReceiver localServerReceiver;

    @Autowired
    private HttpServletResponse response;

    String accessToken;

    @GetMapping("/authorize")
    public void authorize() throws IOException, GeneralSecurityException {
        // Redirect the application to the authorization URL
        String authorizationUrl = config.googleAuthorizationCodeFlow(config.httpTransport()).newAuthorizationUrl()
                .setRedirectUri("http://localhost:9090/oauth2callback")
                .build();
        response.sendRedirect(authorizationUrl);
    }


    @GetMapping("/oauth2callback")
    public String getAccessToken(@RequestParam("code") String authorizationCode) throws IOException, GeneralSecurityException {
        TokenResponse tokenResponse = config.googleAuthorizationCodeFlow(config.httpTransport())
                .newTokenRequest(authorizationCode)
                .setRedirectUri("http://localhost:9090/oauth2callback")
                .execute();

        accessToken = tokenResponse.getAccessToken();
        // Perform further actions with the access token, e.g., call the YouTube API
        response.sendRedirect("http://localhost:9090/welcome");
        return accessToken;
    }


    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome: Account Linked ! : You can start fetching reports now " ;
    }


    @GetMapping("/getMetrics")
    public ResponseEntity<Object> getMetrics(
            @RequestParam(value = "dimensions", defaultValue = "", required = false) String dimensions,
            @RequestParam(value = "endDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate,
            @RequestParam(value = "filters", defaultValue = "", required = false) String filters,
            @RequestParam(value = "maxResults", defaultValue = "0", required = false) Integer maxResults,
            @RequestParam(value = "metrics", defaultValue = "", required = false) String metrics,
            @RequestParam(value = "sort", defaultValue = "", required = false) String sort,
            @RequestParam(value = "startDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate

    ) throws Exception {

        String baseUrl = "https://youtubeanalytics.googleapis.com/v2/reports";

         if (accessToken == null) {
            this.authorize();
            return new ResponseEntity<Object>(HttpStatus.FORBIDDEN);
        }

        String token = accessToken;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);

        if (!dimensions.isEmpty()) builder.queryParam("dimensions", dimensions);
        if (!endDate.isEmpty()) builder.queryParam("endDate", endDate);
        if (!filters.isEmpty()) builder.queryParam("filters", filters);
        if (maxResults != 0) builder.queryParam("maxResults", maxResults);
        if (!metrics.isEmpty()) builder.queryParam("metrics", metrics);
        if (!sort.isEmpty()) builder.queryParam("sort", sort);
        if (!startDate.isEmpty()) builder.queryParam("startDate", startDate);

        //builder.queryParam("ids","channel==MINE");
        builder.queryParam("access_token", token);
        //System.out.println(builder.toUriString());
        String finalUrl = builder.toUriString() + "&ids=channel==MINE";


        RestTemplate template = new RestTemplate();

        return template.exchange(
                finalUrl,
                HttpMethod.GET,
                new HttpEntity<>(null),
                Object.class
        );
    }
}




