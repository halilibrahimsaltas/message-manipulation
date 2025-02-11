package com.message.message_manipulation.service;

import org.springframework.stereotype.Service;
import com.message.message_manipulation.repository.MessageRepository;
import com.message.message_manipulation.model.Message;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ForwardService forwardService;
    private final LinkConversionService linkConversionService;

    public MessageService(MessageRepository messageRepository, ForwardService forwardService, LinkConversionService linkConversionService) {
        this.messageRepository = messageRepository;
        this.forwardService = forwardService;
        this.linkConversionService = linkConversionService;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
    
    public List<Message> getMessagesBySender(String sender) {
        return messageRepository.findBySender(sender);
    }

    public void saveMessage(String content, String sender) {
        Message message = new Message();
        message.setContent(content);
        message.setConvertedText(linkConversionService.convertLinks(content));
        message.setSender(sender);
        message.setReceivedAt(LocalDateTime.now(ZoneId.of("Europe/Istanbul")));
        
        messageRepository.save(message);
        
        // Sadece dönüştürülmüş mesaj içeriğini ilet
        forwardService.sendToAllChats(message.getConvertedText());
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public Message fetchMessagesFromChat(String sender) {
        List<Message> existingMessages = messageRepository.findBySender(sender);
        Message lastMessage = existingMessages.isEmpty() ? null : existingMessages.get(existingMessages.size() - 1);
        
        for (Message msg : existingMessages) {
            String text = msg.getContent();
            
            if (lastMessage == null || !text.equals(lastMessage.getContent())) {
                saveMessage(text, sender);
            }
        }
        return lastMessage;
    }
}


