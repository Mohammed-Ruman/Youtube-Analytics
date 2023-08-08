package com.pacescape.userms.youtube.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.pacescape.exception.PaceScapeException;
import com.pacescape.exception.PaceScapeRuntimeException;
import com.pacescape.userms.utility.ApplicationConstants;
import com.pacescape.userms.youtube.Config.GoogleApiClientConfig;
import com.pacescape.userms.youtube.entity.YoutubeToken;
import com.pacescape.userms.youtube.repository.YoutubeTokenRepo;
import com.pacescape.userms.youtube.service.YoutubeService;
import com.pacescape.userms.youtube.utility.SwaggerConstant;
import com.pacescape.userms.youtube.utility.YoutubeConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class OAuthController {


    private static final Logger LOGGER = LogManager.getLogger(OAuthController.class);

    @Autowired
    private AuthorizationCodeFlow authorizationCodeFlow;
    @Autowired
    private YoutubeService youtubeService;
    @Autowired
    private GoogleApiClientConfig config;

    @Autowired
    private HttpServletResponse response;

    @Value("${youtube.welcomeredirectUri}")
    private String welcomeredirectUri;

    @Value("${youtube.redirectUri}")
    private  String redirectUri;


    private UUID Id;


    @Operation(summary = "Get Authorization Code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authorization Code fetched Successfully", content = {
                    @Content(mediaType = "application/json", examples = {@ExampleObject(value = SwaggerConstant.GETAUTHORIZATIONCODESUCCESS)})
            }),
            @ApiResponse(responseCode = "400", description = "Unable to fetch : Authorization failed", content = {
                    @Content(mediaType = "application/json", examples = {@ExampleObject(value = SwaggerConstant.GETAUTHORIZATIONCODEFAILURE)})
            }),
            @ApiResponse(responseCode = "404", description = "Method not found")
    })
    @GetMapping("/public/authorize")
    public void getAuthorizationCode(@RequestParam(value = "userId", required = true) UUID userId) throws IOException, GeneralSecurityException, PaceScapeException {
        // Redirect the application to the authorization URL

        LOGGER.info(ApplicationConstants.CLASSNAME + this.getClass().getSimpleName() + ", "
                + ApplicationConstants.METHODNAME + "getAuthorizationCode");

        //Validation of UserId
        if (youtubeService.isValidUser(userId)) {
            this.Id = userId;
        }

        //Construction of Authorization URL
        String authorizationUrl = config.googleAuthorizationCodeFlow(config.httpTransport()).newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();
        //Redirecting the browser to Authorization URL
        response.sendRedirect(authorizationUrl);
    }

    @Operation(summary = "Get Access Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token fetched Successfully", content = {
                    @Content(mediaType = "application/json", examples = {@ExampleObject(value = SwaggerConstant.GETAUTHENTICATIONCODESUCCESS)})
            }),
            @ApiResponse(responseCode = "400", description = "Unable to fetch : Check Authorization Code", content = {
                    @Content(mediaType = "application/json", examples = {@ExampleObject(value = SwaggerConstant.GETAUTHENTICATIONCODEFAILURE)})
            }),
            @ApiResponse(responseCode = "404", description = "Method not found")
    })
    @GetMapping("/public/oauth2callback")
    public String getAccessToken(@RequestParam(value = "code", required = false) String authorizationCode
    ) throws IOException, GeneralSecurityException, PaceScapeException {

        LOGGER.info(ApplicationConstants.CLASSNAME + this.getClass().getSimpleName() + ", "
                + ApplicationConstants.METHODNAME + "getAccessToken");

        //Validation if user has provided the required permission
        if (authorizationCode == null) {
            throw new PaceScapeException("Give the permission to access your account");
        }

        //Getting the TokenResponse using authorization code
        TokenResponse tokenResponse = config.googleAuthorizationCodeFlow(config.httpTransport())
                .newTokenRequest(authorizationCode)
                .setRedirectUri(redirectUri)
                .execute();

        //Storing Access Token and Refresh Token in our DB
        youtubeService.addYoutubeToken(tokenResponse, Id);

        //Redirecting to Welcome Screen
        response.sendRedirect(welcomeredirectUri);

        return "Success";
    }


    @GetMapping("/public/welcome")
    public String welcome() {
        LOGGER.info(ApplicationConstants.CLASSNAME + this.getClass().getSimpleName() + ", "
                + ApplicationConstants.METHODNAME + "welcome");

        return "Success : Account Linked! Kindly Close this Pop-up Window  ";
    }



    @Operation(summary = "Get All YouTube Metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Youtube metrics fetched Successfully", content = {
                    @Content(mediaType = "application/json", examples = {@ExampleObject(value = SwaggerConstant.GETAUTHENTICATIONCODESUCCESS)})
            }),
            @ApiResponse(responseCode = "400", description = "Failed to fetch", content = {
                    @Content(mediaType = "application/json", examples = {@ExampleObject(value = SwaggerConstant.GETAUTHENTICATIONCODEFAILURE)})
            }),
            @ApiResponse(responseCode = "404", description = "Method not found")
    })
    @GetMapping("/private/youtube")
    public ResponseEntity<Object> getAllMetrics(@RequestParam(value = "userId") UUID userId) throws Exception {

        LOGGER.info(ApplicationConstants.CLASSNAME + this.getClass().getSimpleName() + ", "
                + ApplicationConstants.METHODNAME + "getAllMetrics");

        //Validation of UserId
        youtubeService.isValidUser(userId);

        //Fetching youtube Token for the given UserId
        YoutubeToken youtubeToken = youtubeService.getYoutubeToken(userId).orElseThrow(() ->
                new PaceScapeException("YouTube Account not linked!!"));


        String accessToken = youtubeToken.getAccessToken();

        // Get channel details from Youtube Data API
        UriComponentsBuilder dataApiBuilder = UriComponentsBuilder.fromHttpUrl(YoutubeConstants.DATAAPI_BASE_URL)
                .queryParam("part", YoutubeConstants.DATAAPI_PARAMETER_PART)
                .queryParam("access_token", accessToken)
                .queryParam("mine", true);

        try {

            ResponseEntity<Object> dataApiResponse = new RestTemplate().exchange(
                    dataApiBuilder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    Object.class
            );


            if (!youtubeService.isChannelExist(dataApiResponse.getBody())) {
                throw new PaceScapeException("This user doesn't have a youtube channel");
            }


            Map<String, Object> channelDetails = youtubeService.youtubeDataApiJsonFormatter(dataApiResponse.getBody());

            // Get metrics data from Youtube Analytic API
            UriComponentsBuilder analyticApiBuilder = UriComponentsBuilder.fromHttpUrl(YoutubeConstants.ANALYTICAPI_BASE_URL)
                    .queryParam("metrics", YoutubeConstants.ANALYTICAPI_PARAMETER_METRICS)
                    .queryParam("access_token", accessToken)
                    .queryParam("endDate", youtubeService.getDate())
                    .queryParam("startDate", channelDetails.get("channelCreatedON"));


            ResponseEntity<Object> analyticApiResponse = new RestTemplate().exchange(
                    analyticApiBuilder.toUriString() + "&ids=channel==MINE",
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    Object.class
            );

            if (dataApiResponse.getStatusCode().is4xxClientError() || dataApiResponse.getStatusCode().is5xxServerError()) {
                throw new PaceScapeException("Invalid AnalyticAPI URL or Youtube Server Error");
            }
            Map<String, Object> metricsData = youtubeService.youtubeAnalyticApiJsonFormatter(analyticApiResponse.getBody());

            Map<String, Object> resultMap = new LinkedHashMap<>();

            resultMap.putAll(channelDetails);
            resultMap.putAll(metricsData);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(resultMap);
            Map<String, Object> jsonObject = objectMapper.readValue(json, Map.class);

            return ResponseEntity.ok(jsonObject);

        } catch (HttpClientErrorException e) {

            if (e.getStatusCode().is5xxServerError()) {
                throw new PaceScapeException("Invalid DataApi URL or Youtube Server Error");
            } else if (e.getStatusCode().is4xxClientError()) {

                if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new PaceScapeRuntimeException("Invalid Data API Url", HttpStatus.BAD_REQUEST);
                } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    config.refreshAccessToken(userId);
                    return getAllMetrics(userId);
                }
            }
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(summary = "YouTube Account Link Status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "YouTube Account Linked", content = {
                    @Content(mediaType = "application/json", examples = {@ExampleObject(value = SwaggerConstant.GETYOUTUBEACCOUNTLINKSTATUS_LINKED)})
            }),
            @ApiResponse(responseCode = "400", description = "YouTube Account Not Linked", content = {
                    @Content(mediaType = "application/json", examples = {@ExampleObject(value = SwaggerConstant.GETYOUTUBEACCOUNTLINKSTATUS_NOTLINKED)})
            }),
            @ApiResponse(responseCode = "404", description = "Method not found")
    })
    @GetMapping("/private/youtube/linkstatus")
    public ResponseEntity<Boolean> isYouTubeAccountLinked(@RequestParam(value="userId") UUID userId) throws PaceScapeException {
        return new ResponseEntity<>(youtubeService.isAccountLinked(userId),HttpStatus.OK);
    }


}




