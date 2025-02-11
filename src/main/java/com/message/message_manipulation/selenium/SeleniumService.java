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
            log.info("Selenium basariyla baslatildi");
        } catch (Exception e) {
            log.error("Selenium baslatma hatasi: ", e);
        }
    }

    /**
     * Sol paneldeki tum sohbetleri bulur, her birini tiklar ve mesajlari ceker.
     */
    public void fetchAllChats() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // QR kod taramasi icin yeterli sure bekle
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[aria-label='Sohbet listesi']")));
            
            // Yukleme ekraninin kaybolmasini bekle
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div[data-testid='popup-overlay']")));
            
            // Sohbet listesini bul
            List<WebElement> chatElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div[aria-label='Kanal Listesi'] div[role='listitem']")));
            
            log.info("Toplam {} sohbet bulundu", chatElements.size());
            
            for (WebElement chat : chatElements) {
                try {
                    // Yukleme ekraninin kaybolmasini bekle
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector("div[data-testid='popup-overlay']")));
                    
                    // Elementin gorunur ve tiklanabilir olmasini bekle
                    WebElement clickableChat = wait.until(ExpectedConditions.elementToBeClickable(chat));
                    
                    // Gorunur olmasi icin kaydir
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", clickableChat);
                    Thread.sleep(1000); // Kaydirma animasyonunun tamamlanmasini bekle
                    
                    // Tum overlay elementlerinin kaybolmasini bekle
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector(".overlay, .loading, div[data-testid='popup-overlay']")));
                    
                    // Normal tiklama dene, olmazsa JavaScript ile tikla
                    try {
                        wait.until(ExpectedConditions.elementToBeClickable(clickableChat));
                        clickableChat.click();
                    } catch (Exception e) {
                        log.warn("Normal tiklama basarisiz, JavaScript ile deneniyor");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clickableChat);
                    }
                    
                    // Mesajlarin yuklenmesini bekle
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div[role='application']")));
                    
                    // Yukleme ekraninin kaybolmasini bekle
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector("div[data-testid='popup-overlay']")));
                    
                    Thread.sleep(1500);
                    
                    // Sohbet adini al
                    String chatName = getChatName(chat);
                    log.info("Islenen sohbet: {}", chatName);
                    
                    // Mesajlari cek - artik dogru chatName ile
                    fetchMessagesFromChat(chatName);
                    
                } catch (Exception e) {
                    log.error("Sohbet isleme hatasi: ", e);
                    continue;
                }
            }
        } catch (Exception e) {
            log.error("fetchAllChats hatasi: ", e);
        }
    }

    /**
     * Sohbet elementinden sohbet adini alir
     */
    private String getChatName(WebElement chatElement) {
        try {
            WebElement titleElement = chatElement.findElement(By.cssSelector("span[title]"));
            String chatName = titleElement.getAttribute("title");
            chatName = normalizeString(chatName);
            return chatName != null && !chatName.isEmpty() ? chatName : "Bilinmeyen Sohbet";
        } catch (Exception e) {
            log.error("Sohbet adi alma hatasi: ", e);
            return "Bilinmeyen Sohbet";
        }
    }

    /**
     * String icindeki Turkce karakterleri duzeltir
     */
    private String normalizeString(String input) {
        if (input == null) return null;
        
        return input.replace('ı', 'i')
                   .replace('İ', 'I')
                   .replace('ğ', 'g')
                   .replace('Ğ', 'G')
                   .replace('ü', 'u')
                   .replace('Ü', 'U')
                   .replace('ş', 's')
                   .replace('Ş', 'S')
                   .replace('ö', 'o')
                   .replace('Ö', 'O')
                   .replace('ç', 'c')
                   .replace('Ç', 'C');
    }

    /**
     * Verilen bir sohbet adina tikladiktan sonra mesajlari DOM uzerinden alir.
     */
    public void fetchMessagesFromChat(String chatName) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // Mesaj konteynerinin yuklenmesini bekle
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[role='application']")));
            
            // Tum mesaj elementlerini bul (metin, resim, sticker vb.)
            List<WebElement> messages = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("div[role='row']")));
            
            // Sadece son mesaji al
            if (!messages.isEmpty()) {
                WebElement lastMessage = messages.get(messages.size() - 1);
                try {
                    String messageType = determineMessageType(lastMessage);
                    String messageContent = "";
                    String sender = getSenderFromMessage(lastMessage);
                    
                    switch (messageType) {
                        case "text":
                            WebElement textElement = lastMessage.findElement(By.cssSelector("span.selectable-text"));
                            messageContent = normalizeString(textElement.getText());
                            break;
                            
                        case "image":
                            messageContent = "[Resim]";
                            try {
                                WebElement imageCaption = lastMessage.findElement(By.cssSelector("span.selectable-text"));
                                String caption = imageCaption.getText();
                                if (!caption.isEmpty()) {
                                    messageContent += " - Aciklama: " + caption;
                                }
                            } catch (Exception e) {
                                // Resim aciklamasi yok, sadece [Resim] olarak kaydet
                            }
                            break;
                            
                        case "sticker":
                            messageContent = "[Sticker]";
                            break;
                            
                        case "document":
                            messageContent = "[Dokuman]";
                            try {
                                WebElement docName = lastMessage.findElement(By.cssSelector("span[title]"));
                                messageContent += " - " + docName.getAttribute("title");
                            } catch (Exception e) {
                                // Dokuman adi alinamadi
                            }
                            break;
                            
                        case "voice":
                            messageContent = "[Sesli Mesaj]";
                            break;
                            
                        default:
                            messageContent = "[Diger Medya]";
                    }
                    
                    if (!messageContent.isEmpty()) {
                        sender = sender != null ? normalizeString(sender) : normalizeString(chatName);
                        messageService.saveMessage(messageContent, sender);
                    }
                    
                } catch (Exception e) {
                    log.error("Son mesaj isleme hatasi: ", e);
                }
            }
            
            log.info("{} sohbetinden son mesaj alindi", normalizeString(chatName));
            
        } catch (Exception e) {
            log.error("fetchMessagesFromChat hatasi: ", e);
        }
    }

    private String determineMessageType(WebElement messageElement) {
        try {
            // Metin mesaji kontrolu
            if (messageElement.findElements(By.cssSelector("span.selectable-text")).size() > 0) {
                return "text";
            }
            
            // Resim kontrolu
            if (messageElement.findElements(By.cssSelector("img[data-testid='image-thumb']")).size() > 0) {
                return "image";
            }
            
            // Sticker kontrolu
            if (messageElement.findElements(By.cssSelector("img[data-testid='sticker']")).size() > 0) {
                return "sticker";
            }
            
            // Dokuman kontrolu
            if (messageElement.findElements(By.cssSelector("div[data-testid='document-thumb']")).size() > 0) {
                return "document";
            }
            
            // Sesli mesaj kontrolu
            if (messageElement.findElements(By.cssSelector("div[data-testid='audio-player']")).size() > 0) {
                return "voice";
            }
            
            return "unknown";
        } catch (Exception e) {
            log.error("Mesaj tipi belirleme hatasi: ", e);
            return "unknown";
        }
    }

    /**
     * Mesajdan gondereni alir
     */
    private String getSenderFromMessage(WebElement messageElement) {
        try {
            // Mesajin icindeki gonderen adini bul
            WebElement senderElement = messageElement.findElement(By.cssSelector("div[data-pre-plain-text]"));
            String senderInfo = senderElement.getAttribute("data-pre-plain-text");
            
            // "[HH:mm] Gonderen:" formatindan gonderen adini ayikla
            if (senderInfo != null && senderInfo.contains(":")) {
                String[] parts = senderInfo.split(":");
                if (parts.length > 1) {
                    return parts[1].trim().replace("]", "").trim();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Yeni mesajlari tespit etmek icin periyodik dongu: 10 saniyede bir tum sohbetleri kontrol et.
     */
    @Scheduled(fixedDelay = 10000)
    public void startListening() {
        try {
            if (!isInitialized || driver == null) {
                log.warn("Driver baslatilmamis, yeniden baslatiliyor...");
                init();
                return;
            }
            fetchAllChats();
        } catch (Exception e) {
            log.error("Dinleme hatasi: ", e);
            // Oturum hatasi durumunda yeniden baslat
            isInitialized = false;
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception quitEx) {
                    log.error("Driver kapatma hatasi: ", quitEx);
                }
                driver = null;
            }
        }
    }

    /**
     * Uygulama sonlandiginda driver'i kapatalim.
     */
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
