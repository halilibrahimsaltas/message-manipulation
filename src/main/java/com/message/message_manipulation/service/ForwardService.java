package com.message.message_manipulation.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Telegram'a mesaj göndermek için kullanılan servis
 */

@Service    // Spring Boot uygulamasında servis olarak tanımlanır
public class ForwardService {

    private static final Logger log = LoggerFactory.getLogger(ForwardService.class);
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot"; 
    private final RestTemplate restTemplate;
    private final SettingsService settingsService;

    public ForwardService(RestTemplate restTemplate, SettingsService settingsService) {
        this.restTemplate = restTemplate;
        this.settingsService = settingsService;
    }

    /**
     * Telegram'a mesaj gönderir.
     */
    public void sendToTelegram(String chatId, String messageText) {
        try {
            String token = settingsService.getValue("telegram.bot.token");
            if (token == null || token.isEmpty()) {
                log.error("Telegram bot token tanımlanmamış!");
                return;
            }

            String formattedChatId = chatId.startsWith("-") ? chatId : "-" + chatId;
            String url = TELEGRAM_API_URL + token + "/sendMessage";
            
            Map<String, String> body = new HashMap<>();
            body.put("chat_id", formattedChatId);
            body.put("text", messageText);
            body.put("parse_mode", "HTML"); // HTML formatını destekle
            
            restTemplate.postForObject(url, body, String.class);
            log.info("Telegram mesajı gönderildi: {}", messageText);
            
        } catch (Exception e) {
            log.error("Telegram mesaj gönderme hatası: ", e);
        }
    }

    public void sendToAllChats(String messageText) {
        String chatIdsStr = settingsService.getValue("telegram.bot.chatIds");
        if (chatIdsStr == null || chatIdsStr.isEmpty()) {
            log.error("Telegram chat ID'leri tanımlanmamış!");
            return;
        }

        String[] chatIds = chatIdsStr.split(",");
        for (String chatId : chatIds) {
            sendToTelegram(chatId.trim(), messageText);
        }
    }

    /**
     * WhatsApp mesajını Telegram formatına dönüştürür
     */
    public void forwardMessage(String sender, String content) {
        try {
            // Sadece mesaj içeriğini gönder
            sendToAllChats(content);
        } catch (Exception e) {
            log.error("Mesaj yönlendirme hatası: ", e);
        }
    }
}