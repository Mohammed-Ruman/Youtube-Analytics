package com.pacescape.userms.youtube.repository;

import com.pacescape.userms.youtube.entity.YoutubeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;


public interface YoutubeTokenRepo extends JpaRepository<YoutubeToken,Integer> {

    @Query("select o from YoutubeToken o where o.userId=:id and o.isAuthorized=TRUE")
    Optional<YoutubeToken> findYoutubeTokenByUserId(UUID id);
}
