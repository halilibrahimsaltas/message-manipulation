package com.message.message_manipulation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import jakarta.persistence.UniqueConstraint;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"content", "sender"})
})

public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String sender;

    @Column(columnDefinition = "TEXT")
    private String convertedText;
    
    @Column(name = "receivedAt", nullable = false)
    private LocalDateTime receivedAt;


}
