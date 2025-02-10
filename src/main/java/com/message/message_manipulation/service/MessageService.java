package com.message.message_manipulation.service;

import org.springframework.stereotype.Service;
import com.message.message_manipulation.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import com.message.message_manipulation.model.Message;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {


    private final MessageRepository messageRepository;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    

}
