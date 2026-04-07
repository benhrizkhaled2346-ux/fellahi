package com.appfor.ne3ma.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    @JoinColumn(name = "AI_conv_id")
    private AI_Conversations AI_conv;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;

    @Column
    private  String content;

    @Column
    @CreationTimestamp
    private LocalDateTime timestamp;

}
