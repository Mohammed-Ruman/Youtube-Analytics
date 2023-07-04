package com.youtubeanalytics.Controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController

public class OAuthController {

    @Autowired
    private AuthorizationCodeFlow authorizationCodeFlow;

    @Autowired
    private LocalServerReceiver localServerReceiver;

    String accessToken;

    @GetMapping("/authorize")
    public String authorize() throws GeneralSecurityException, IOException {
        Credential credential = new AuthorizationCodeInstalledApp(
                authorizationCodeFlow, localServerReceiver).authorize("user");

         accessToken = credential.getAccessToken();
        // Perform further actions with the access token, e.g., call the YouTube API
        System.out.println(accessToken);
        System.out.println(credential.toString());
        System.out.println(credential);

        return "Access token: " ;
    }

    @GetMapping("/oauth2callback")
    public String getAccessToken(){
        return accessToken;
    }
}




