package com.appfor.ne3ma.repository;

import com.appfor.ne3ma.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
