package com.message.message_manipulation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.message.message_manipulation.repository.MessageRepository;
import com.message.message_manipulation.model.Message;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
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
        try {
            // Son 1 dakika içinde aynı mesaj var mı kontrol et
            LocalDateTime oneMinuteAgo = LocalDateTime.now(ZoneId.of("Europe/Istanbul")).minusMinutes(1);
            boolean exists = messageRepository.existsByContentAndReceivedAtAfter(content, oneMinuteAgo);

            if (!exists) {
                Message message = new Message();
                message.setContent(content);
                message.setConvertedText(linkConversionService.convertLinks(content));
                message.setSender(sender);
                message.setReceivedAt(LocalDateTime.now(ZoneId.of("Europe/Istanbul")));
                
                // Mesajı kaydet
                Message savedMessage = messageRepository.save(message);
                
                // Her mesajı Telegram'a gönder
                forwardService.forwardMessage(sender, savedMessage.getConvertedText());
                
                log.debug("Yeni mesaj kaydedildi ve iletildi: {}", content);
            } else {
                log.debug("Tekrar eden mesaj atlandı: {}", content);
            }
        } catch (Exception e) {
            log.warn("Mesaj kaydetme sırasında hata: {}", e.getMessage());
        }
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public Message fetchMessagesFromChat(String sender) {
        try {
            // Tüm mesajlardan son 3 mesajın içeriğini al
            List<String> existingContents = messageRepository.findDistinctContentByOrderByReceivedAtDesc();
            
            List<Message> existingMessages = messageRepository.findBySender(sender);
            
            // Son 3 mesajı kontrol et ve içeriği daha önce kaydedilmemişse kaydet
            for (int i = Math.max(0, existingMessages.size() - 3); i < existingMessages.size(); i++) {
                Message msg = existingMessages.get(i);
                if (!existingContents.contains(msg.getContent())) {
                    saveMessage(msg.getContent(), sender);
                    log.debug("Yeni mesaj içeriği bulundu ve kaydedildi: {}", msg.getContent());
                } else {
                    log.debug("Mesaj içeriği zaten mevcut, atlanıyor: {}", msg.getContent());
                }
            }

            return existingMessages.isEmpty() ? null : existingMessages.get(existingMessages.size() - 1);
        } catch (Exception e) {
            log.warn("Mesaj çekme sırasında hata: {}", e.getMessage());
            return null;
        }
    }
}


