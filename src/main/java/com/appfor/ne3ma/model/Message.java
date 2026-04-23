package com.appfor.ne3ma.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "Messages")
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AI_conv_id", foreignKey = @ForeignKey(name = "fk_messages_ai_conversation"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private AI_Conversations AI_conv;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    @CreationTimestamp
    private LocalDateTime timestamp;

}
