package com.appfor.ne3ma.repository;

import com.appfor.ne3ma.model.AI_Conversations;
import com.appfor.ne3ma.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIConversationRepository extends JpaRepository<AI_Conversations, Long> {
    Optional<AI_Conversations> findByIdAndUser(Long id, User user);
}
