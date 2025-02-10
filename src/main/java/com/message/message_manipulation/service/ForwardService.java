package com.message.message_manipulation.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


/**
 * Telegram'a mesaj göndermek için kullanılan servis
 */

@Service    // Spring Boot uygulamasında servis olarak tanımlanır
public class ForwardService {

    private static final Logger log = LoggerFactory.getLogger(ForwardService.class);
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot"; 
    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chatId}")
    private String chatId;

    public ForwardService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Telegram'a mesaj gönderir.
     */
    public void sendToTelegram(String messageText) {
        try {
            if (botToken == null || botToken.isEmpty()) {
                log.error("Telegram bot token tanimlanmamis!");
                return;
            }

            // Chat ID'nin başında '-' varsa kaldır ve tekrar ekle
            String formattedChatId = chatId.startsWith("-") ? chatId : "-" + chatId;
            
            String url = TELEGRAM_API_URL + botToken + "/sendMessage";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("chat_id", formattedChatId);
            requestBody.put("text", messageText);
            requestBody.put("parse_mode", "HTML");

            restTemplate.postForObject(url, requestBody, String.class);
            log.info("Telegram mesaji gonderildi: {}", messageText);
            
        } catch (RestClientException e) {
            log.error("Telegram API hatasi: ", e);
        } catch (IllegalArgumentException e) {
            log.error("Gecersiz parametre hatasi: ", e);
        }
    }

    /**
     * WhatsApp mesajını Telegram formatına dönüştürür
     */
    public void forwardMessage(String sender, String content) {
        try {
            // Mesaj formatı: 
            // <b>Gönderen:</b> Ahmet
            // <i>Mesaj:</i> Merhaba dünya
            String formattedMessage = String.format(
                "<b>Gonderen:</b> %s\n<i>Mesaj:</i> %s",
                sender,
                content
            );
            
            sendToTelegram(formattedMessage);
            
        } catch (Exception e) {
            log.error("Mesaj yonlendirme hatasi: ", e);
        }
    }
}