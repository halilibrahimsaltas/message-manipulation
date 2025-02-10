package com.message.message_manipulation.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;


/**
 * Telegram'a mesaj göndermek için kullanılan servis
 */

@Service    // Spring Boot uygulamasında servis olarak tanımlanır
public class ForwardService {

    private static final Logger log = LoggerFactory.getLogger(ForwardService.class);
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot"; 
    private final RestTemplate restTemplate;

    // Test için sabit değerler
    private final String botToken = "7811675034:AAGgOp6VnrGgEbcqClbhtNu9LqV3WorNIfc";
    private final String chatId = "-1002405663941";

    public ForwardService() {
        this.restTemplate = new RestTemplate();
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
            
        } catch (Exception e) {
            log.error("Telegram mesaj gonderme hatasi: ", e);
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