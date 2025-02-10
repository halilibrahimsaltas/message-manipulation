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
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // QR kod taraması için yeterli süre bekle
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[aria-label='Sohbet listesi']")));
            
            // Yükleme ekranının kaybolmasını bekle
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div[data-testid='popup-overlay']")));
            
            // Sohbet listesini bul
            List<WebElement> chatElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div[aria-label='Sohbet listesi'] div[role='listitem']")));
            
            log.info("Toplam {} sohbet bulundu", chatElements.size());
            
            for (WebElement chat : chatElements) {
                try {
                    // Yükleme ekranının kaybolmasını bekle
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector("div[data-testid='popup-overlay']")));
                    
                    // Elementin görünür ve tıklanabilir olmasını bekle
                    WebElement clickableChat = wait.until(ExpectedConditions.elementToBeClickable(chat));
                    
                    // Görünür olması için kaydır
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", clickableChat);
                    Thread.sleep(1000); // Kaydırma animasyonunun tamamlanmasını bekle
                    
                    // Tüm overlay elementlerinin kaybolmasını bekle
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector(".overlay, .loading, div[data-testid='popup-overlay']")));
                    
                    // Normal tıklama dene, olmazsa JavaScript ile tıkla
                    try {
                        wait.until(ExpectedConditions.elementToBeClickable(clickableChat));
                        clickableChat.click();
                    } catch (Exception e) {
                        log.warn("Normal tıklama başarısız, JavaScript ile deneniyor");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clickableChat);
                    }
                    
                    // Mesajların yüklenmesini bekle
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div[role='application']")));
                    
                    // Yükleme ekranının kaybolmasını bekle
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector("div[data-testid='popup-overlay']")));
                    
                    Thread.sleep(1500);
                    
                    // Mesajları çek
                    fetchMessagesFromChat(getSenderFromMessage(chat));
                    
                } catch (Exception e) {
                    log.error("Sohbet işleme hatası: ", e);
                    continue;
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
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // Mesaj konteynerinin yüklenmesini bekle
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[role='application']")));
            
            // Tüm mesaj elementlerini bul (metin, resim, sticker vb.)
            List<WebElement> messages = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div[role='row']")));
            
            log.info("{} sohbetinde {} mesaj bulundu", chatName, messages.size());
            
            for (WebElement msg : messages) {
                try {
                    String messageType = determineMessageType(msg);
                    String messageContent = "";
                    String sender = getSenderFromMessage(msg);
                    
                    switch (messageType) {
                        case "text":
                            // Metin mesajı
                            WebElement textElement = msg.findElement(By.cssSelector("span.selectable-text"));
                            messageContent = textElement.getText();
                            break;
                            
                        case "image":
                            // Resim mesajı
                            messageContent = "[Resim]";
                            try {
                                WebElement imageCaption = msg.findElement(By.cssSelector("span.selectable-text"));
                                String caption = imageCaption.getText();
                                if (!caption.isEmpty()) {
                                    messageContent += " - Açıklama: " + caption;
                                }
                            } catch (Exception e) {
                                // Resim açıklaması yok, sadece [Resim] olarak kaydet
                            }
                            break;
                            
                        case "sticker":
                            messageContent = "[Sticker]";
                            break;
                            
                        case "document":
                            messageContent = "[Döküman]";
                            try {
                                WebElement docName = msg.findElement(By.cssSelector("span[title]"));
                                messageContent += " - " + docName.getAttribute("title");
                            } catch (Exception e) {
                                // Döküman adı alınamadı
                            }
                            break;
                            
                        case "voice":
                            messageContent = "[Sesli Mesaj]";
                            break;
                            
                        default:
                            messageContent = "[Diğer Medya]";
                    }
                    
                    if (!messageContent.isEmpty()) {
                        messageService.saveMessage(messageContent, sender != null ? sender : chatName);
                    }
                    
                } catch (Exception e) {
                    log.error("Mesaj işleme hatası: ", e);
                }
            }
        } catch (Exception e) {
            log.error("fetchMessagesFromChat hatası: ", e);
        }
    }

    private String determineMessageType(WebElement messageElement) {
        try {
            // Metin mesajı kontrolü
            if (messageElement.findElements(By.cssSelector("span.selectable-text")).size() > 0) {
                return "text";
            }
            
            // Resim kontrolü
            if (messageElement.findElements(By.cssSelector("img[data-testid='image-thumb']")).size() > 0) {
                return "image";
            }
            
            // Sticker kontrolü
            if (messageElement.findElements(By.cssSelector("img[data-testid='sticker']")).size() > 0) {
                return "sticker";
            }
            
            // Döküman kontrolü
            if (messageElement.findElements(By.cssSelector("div[data-testid='document-thumb']")).size() > 0) {
                return "document";
            }
            
            // Sesli mesaj kontrolü
            if (messageElement.findElements(By.cssSelector("div[data-testid='audio-player']")).size() > 0) {
                return "voice";
            }
            
            return "unknown";
        } catch (Exception e) {
            log.error("Mesaj tipi belirleme hatası: ", e);
            return "unknown";
        }
    }

    private String getSenderFromMessage(WebElement messageElement) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement senderElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("span.selectable-text")));
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
