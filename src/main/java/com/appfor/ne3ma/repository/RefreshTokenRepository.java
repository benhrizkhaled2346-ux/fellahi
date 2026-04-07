package com.appfor.ne3ma.repository;
import com.appfor.ne3ma.model.RefreshToken;
import com.appfor.ne3ma.security.UserPrincipal;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(com.appfor.ne3ma.model.User user);

    void deleteByUser(com.appfor.ne3ma.model.User user);
}