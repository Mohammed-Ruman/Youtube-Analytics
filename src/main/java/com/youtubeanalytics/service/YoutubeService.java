package com.pacescape.userms.youtube.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.pacescape.exception.PaceScapeException;
import com.pacescape.userms.repository.UserRepository;
import com.pacescape.userms.youtube.entity.YoutubeToken;
import com.pacescape.userms.youtube.repository.YoutubeTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class YoutubeService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private YoutubeTokenRepo youtubeTokenRepo;

    public Map<String, Object> youtubeAnalyticApiJsonFormatter(Object json) {


        Map<String, Object> dataMap = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(mapper.writeValueAsString(json));

            // Extract columnHeaders names
            List<String> columnHeadersNames = new ArrayList<>();

            if (root.has("columnHeaders")) {
                JsonNode columnHeadersNode = root.get("columnHeaders");
                for (JsonNode columnHeaderNode : columnHeadersNode) {
                    columnHeadersNames.add(columnHeaderNode.get("name").asText());
                }
            }

            // Extract rows
            List<List<Integer>> rows = new ArrayList<>();
            if (root.has("rows")) {
                JsonNode rowsNode = root.get("rows");
                for (JsonNode rowNode : rowsNode) {
                    List<Integer> rowData = mapper.convertValue(rowNode, List.class);
                    rows.add(rowData);
                }
            }

            // Create the Map with columnHeaders.names as keys and rows as values
            dataMap = new LinkedHashMap<>();
            for (int i = 0; i < columnHeadersNames.size(); i++) {
                String columnName = columnHeadersNames.get(i);
                if (!rows.isEmpty() && i < rows.get(0).size()) {
                    //Integer value = rows.get(0).get(i);
                    dataMap.put(columnName, rows.get(0).get(i));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return dataMap;
    }


    public Map<String, Object> youtubeDataApiJsonFormatter(Object obj) {
        Map<String, Object> dataMap = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(mapper.writeValueAsString(obj));

            if (root.has("items")) {
                JsonNode itemRoot = root.get("items");
                if (itemRoot.get(0).has("snippet")) {
                    JsonNode snippetRoot = itemRoot.get(0).get("snippet");
                    dataMap = new LinkedHashMap<>();
                    dataMap.put("channelTitle", snippetRoot.get("title"));
                    dataMap.put("channelDescription", snippetRoot.get("description"));
                    dataMap.put("channelCreatedON", snippetRoot.get("publishedAt").toString().substring(1, 11));
                    dataMap.put("customURL", "https://www.youtube.com/" + snippetRoot.get("customUrl").asText());
                }
                if (itemRoot.get(0).has("id")) {
                    dataMap.put("channelId", itemRoot.get(0).get("id"));
                }
                if (itemRoot.get(0).has("statistics")) {
                    JsonNode staticticsRoot = itemRoot.get(0).get("statistics");
                    dataMap.put("viewCount", staticticsRoot.get("viewCount"));
                    dataMap.put("subscriberCount", staticticsRoot.get("subscriberCount"));
                    dataMap.put("videoCount", staticticsRoot.get("videoCount"));

                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // System.out.println(dataMap.toString());
        return dataMap;

    }

    public String getDate() {
        Date date = new Date();

        // Convert the Date to LocalDate (Java 8 or later)
        LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        // Create a DateTimeFormatter instance with the desired date format
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return localDate.format(dtf);
    }

    public Boolean isValidUser(UUID userId) throws PaceScapeException {
        return userRepository.findByID(userId).map((user) -> {
            user.getId().equals(userId);
            return true;
        }).orElseThrow(() ->
                new PaceScapeException("Invalid user"));
    }

    public void addYoutubeToken(TokenResponse tokenResponse, UUID userId) {
        YoutubeToken youtubeToken = YoutubeToken.builder().userId(userId).accessToken(tokenResponse.getAccessToken()).refreshToken(tokenResponse.getRefreshToken()).isAuthorized(true)
                .tokenRefreshCount(0).build();
        youtubeTokenRepo.save(youtubeToken);
    }

    public Optional<YoutubeToken> getYoutubeToken(UUID userId) throws PaceScapeException {

        return youtubeTokenRepo.findYoutubeTokenByUserId(userId);
    }

    public Boolean isChannelExist(Object responseBody) {


        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(mapper.writeValueAsString(responseBody));

            if (root.has("pageInfo")) {

                JsonNode pageInfoNode = root.get("pageInfo");
                if (pageInfoNode.has("totalResults")) {

                    if (pageInfoNode.get("totalResults").intValue() == 0) {

                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    public void updateYoutubeToken(YoutubeToken youtubeToken,Object json) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(mapper.writeValueAsString(json));

        if(root.has("access_token")){
            youtubeToken.setAccessToken(root.get("access_token").asText());
            youtubeToken.setTokenRefreshCount(youtubeToken.getTokenRefreshCount()+1);
        }
        youtubeTokenRepo.save(youtubeToken);

    }

    public void removeAuthorization(YoutubeToken youtubeToken){
        youtubeToken.setIsAuthorized(false);
        youtubeTokenRepo.save(youtubeToken);
    }

    public Boolean isAccountLinked(UUID userId) throws PaceScapeException {
        if(isValidUser(userId)){
            return getYoutubeToken(userId).isPresent() && getYoutubeToken(userId).get().getIsAuthorized();
        }
        return false;
    }

}