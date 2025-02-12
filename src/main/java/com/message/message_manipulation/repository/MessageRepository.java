package com.message.message_manipulation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.message.message_manipulation.model.Message;


@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySender(String sender);
    List<Message> findByContentContainingIgnoreCase(String content);
    boolean existsByContentAndReceivedAtAfter(
        String content, 
        LocalDateTime receivedAt
    );
    List<Message> findTop3BySenderOrderByReceivedAtDesc(String sender);
    @Query("SELECT DISTINCT m.content FROM Message m ORDER BY m.receivedAt DESC")
    List<String> findDistinctContentByOrderByReceivedAtDesc();
    @Query("SELECT EXISTS (SELECT 1 FROM Message m WHERE m.content LIKE CONCAT('%', ?1, '%') AND m.receivedAt > ?2)")
    boolean existsByProductNameAndReceivedAtAfter(String productName, LocalDateTime receivedAt);
}