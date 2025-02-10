package com.message.message_manipulation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.message.message_manipulation.model.Message;
import com.message.message_manipulation.service.MessageService;

@RestController
@RequestMapping("/api/messages")    
public class MessageController {

    private final MessageService messageService;


    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // Tüm mesajları JSON olarak döndürür
    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    // Tek mesaj detayını döndürmek isterseniz
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        Message message = messageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }



    // İhtiyaç varsa yeni mesaj ekleme, silme gibi endpoint'ler de eklenebilir
}
