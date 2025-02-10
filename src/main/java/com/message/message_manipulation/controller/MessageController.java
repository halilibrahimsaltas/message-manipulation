package com.message.message_manipulation.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.message.message_manipulation.model.Message;
import com.message.message_manipulation.repository.MessageRepository;





@RestController
@RequestMapping("/api/messages")    
public class MessageController {


    private final MessageRepository messageRepository;



    public MessageController(MessageRepository messageRepository ) {
        this.messageRepository = messageRepository; 
    }



    // Tüm mesajları JSON olarak döndürür
    @GetMapping
    public List<Message> getAllMessages() {
            // Gerekirse sayfalama veya sıralama eklenebilir
        return messageRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }


    // Tek mesaj detayını döndürmek isterseniz
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return messageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Message> searchMessages(@RequestParam String q) {
        // Örnek: content içinde arama
        return messageRepository.findByContentContainingIgnoreCase(q);
    }



    // İhtiyaç varsa yeni mesaj ekleme, silme gibi endpoint'ler de eklenebilir
}
