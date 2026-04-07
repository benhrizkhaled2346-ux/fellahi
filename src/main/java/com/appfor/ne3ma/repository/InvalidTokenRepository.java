package com.appfor.ne3ma.repository;

import com.appfor.ne3ma.model.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken,String> {
}
