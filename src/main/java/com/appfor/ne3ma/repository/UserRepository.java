package com.appfor.ne3ma.repository;

import com.appfor.ne3ma.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("SELECT u.email FROM User u WHERE u.phone = :phone")
    Optional<String> findByPhone(@Param("phone") String phone);
    boolean existsByEmail(String email);
}
