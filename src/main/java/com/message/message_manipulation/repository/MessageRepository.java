package com.message.message_manipulation.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.message.message_manipulation.model.Message;


@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Gerekirse custom sorgular
}