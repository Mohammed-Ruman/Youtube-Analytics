package com.pacescape.userms.youtube.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "youtube_access_tokens")
@Getter
public class YoutubeToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer tokenId;

    @Column(name = "userid",nullable = false)
    private UUID userId;
    @Column(name = "accesstoken",nullable = false)
    private String accessToken;
    @Column(name = "refreshtoken",nullable = false)
    private String refreshToken;
    @Column(name="isauthorized",nullable = false)
    private Boolean isAuthorized;
    @Column(name = "accesstokenrefresh_count",nullable = false)
    private Integer tokenRefreshCount;
}
