package com.message.message_manipulation.selenium;


import java.util.List;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.message.message_manipulation.service.MessageService;
import io.github.bonigarcia.wdm.WebDriverManager;


@Service
public class SeleniumService {

    private WebDriver driver;
    private final MessageService messageService;
    private static final Logger log = LoggerFactory.getLogger(SeleniumService.class);
    private boolean isInitialized = false;

    public SeleniumService(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostConstruct
    public void init() {
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.get("https://web.whatsapp.com");
            isInitialized = true;
            log.info("Selenium başarıyla başlatıldı");
        } catch (Exception e) {
            log.error("Selenium başlatma hatası: ", e);
        }
    }

    /**
     * Sol paneldeki tüm sohbetleri bulur, her birini tıklar ve mesajları çeker.
     */
    public void fetchAllChats() {
        try {
            // Sayfanın yüklenmesini bekle
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // Sohbet listesini bul
            List<WebElement> chatElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div[aria-label='Sohbet listesi'] div[role='listitem']")));
            
            for (WebElement chat : chatElements) {
                try {
                    // Elementin görünür ve tıklanabilir olmasını bekle
                    wait.until(ExpectedConditions.elementToBeClickable(chat));
                    
                    // JavaScript ile tıklama
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chat);
                    
                    Thread.sleep(2000);
                    
                    // Mesajları çek
                    fetchMessagesFromChat(getSenderFromMessage(chat));
                    
                } catch (Exception e) {
                    log.error("Sohbet işleme hatası: ", e);
                }
            }
        } catch (Exception e) {
            log.error("fetchAllChats hatası: ", e);
        }
    }


    /**
     * Verilen bir sohbet adına tıkladıktan sonra mesajları DOM üzerinden alır.
     */
    public void fetchMessagesFromChat(String chatName) {
        try {
            List<WebElement> messages = driver.findElements(By.cssSelector("div._22Msk"));
            
            for (WebElement msg : messages) {
                try {
                    String text = msg.getText();
                    // Göndereni ve mesaj içeriğini ayır
                    String sender = getSenderFromMessage(msg);
                    messageService.saveMessage(text, sender != null ? sender : chatName);
                } catch (Exception e) {
                    log.error("Mesaj işleme hatası: ", e);
                }
            }
        } catch (Exception e) {
            log.error("fetchMessagesFromChat hata: ", e);
        }
    }

    private String getSenderFromMessage(WebElement messageElement) {
        try {
            // WhatsApp Web'de gönderen kişinin adının bulunduğu elementi bul
            WebElement senderElement = messageElement.findElement(By.cssSelector("span.selectable-text"));
            return senderElement.getText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Yeni mesajları tespit etmek için periyodik döngü: 10 saniyede bir tüm sohbetleri kontrol et.
     */
    @Scheduled(fixedDelay = 10000)
    public void startListening() {
        try {
            if (!isInitialized || driver == null) {
                log.warn("Driver başlatılmamış, yeniden başlatılıyor...");
                init();
                return;
            }
            fetchAllChats();
        } catch (Exception e) {
            log.error("Dinleme hatası: ", e);
            // Oturum hatası durumunda yeniden başlat
            isInitialized = false;
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception quitEx) {
                    log.error("Driver kapatma hatası: ", quitEx);
                }
                driver = null;
            }
        }
    }

    /**
     * Uygulama sonlandığında driver'ı kapatalım.
     */
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
