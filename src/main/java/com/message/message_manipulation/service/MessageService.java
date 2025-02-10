package com.message.message_manipulation.service;

import org.springframework.stereotype.Service;
import com.message.message_manipulation.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import com.message.message_manipulation.model.Message;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {


    private final MessageRepository messageRepository;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public List<Message> getMessagesBySender(String sender) {
        return messageRepository.findBySender(sender);
    }


    public Message saveMessage(String content, String sender) {

        Message message = new Message();
        message.setContent(content);
        message.setConvertedText(convertLinks(content));
        message.setSender(sender);
        message.setReceivedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }


    private String convertLinks(String text) {
        // URL pattern'i
        String urlPattern = "https?://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(text);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group();
            String modifiedUrl = url.replaceAll("ref=\\w+", "ref=MY_REF");
            matcher.appendReplacement(result, Matcher.quoteReplacement(modifiedUrl));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    



}

