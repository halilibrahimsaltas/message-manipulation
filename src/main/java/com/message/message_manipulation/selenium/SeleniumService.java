package com.message.message_manipulation.selenium;

import java.util.List;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.message.message_manipulation.service.MessageService;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;

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
            driver.get("https://web.whatsapp.com"); // QR kod ile giriş yapılacak
            isInitialized = true;
            log.info("Selenium başarıyla başlatıldı ve WhatsApp Web açıldı.");
        } catch (Exception e) {
            log.error("Selenium başlatma hatası: ", e);
        }
    }

    /**
     * 10 saniyede bir yalnızca "Kanallar" sekmesindeki mesajları almak için çağrılan metot.
     */
    @Scheduled(fixedDelay = 10000)
    public void startListening() {
        try {
            if (!isInitialized || driver == null) {
                log.warn("Driver başlatılmamış, yeniden başlatılıyor...");
                init();
                return;
            }
            // Sadece kanalları işleme
            fetchAllChannels();

        } catch (Exception e) {
            log.error("Dinleme hatası: ", e);
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
     * "Kanallar" sekmesini açmak için butona tıklar.
     */
    private void openChannelsTab() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // "Kanallar" sekmesini temsil eden butonu bul
            // DOM'u inceleyip buton[aria-label='Kanallar'] vb. olarak güncelleyin
            WebElement channelsTabBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[aria-label='Kanallar']")
            ));

            channelsTabBtn.click();

            // Kanal listesi container görünene kadar bekle
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div[aria-label='Kanal Listesi']"))
            );

            log.info("Kanallar sekmesi açıldı.");
        } catch (Exception e) {
            log.error("Kanallar sekmesine geçilemedi: ", e);
        }
    }

    /**
     * "Kanallar" sekmesindeki tüm kanalları bulur, her bir kanala tıklar ve son mesajını çeker.
     */
    private void fetchAllChannels() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Önce kanallar sekmesini aç
            openChannelsTab();

            // Kanal listesi item'larını bul
            List<WebElement> channelElements = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("div[aria-label='Kanal Listesi'] div[role='listitem']"))
            );

            log.info("Toplam {} kanal bulundu.", channelElements.size());

            for (WebElement channel : channelElements) {
                try {
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.cssSelector("div[data-testid='popup-overlay']")));

                    WebElement clickableChannel = wait.until(ExpectedConditions.elementToBeClickable(channel));

                    // Görünür alana kaydır
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block: 'center'});", clickableChannel);
                    Thread.sleep(1000);

                    // Normal tıklama dene, başarısızsa JS click
                    try {
                        clickableChannel.click();
                    } catch (Exception e) {
                        log.warn("Normal tıklama başarısız, JS tıklama deneniyor.");
                        ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].click();", clickableChannel);
                    }

                    // Kanal içeriğinin yüklendiğini bekleyebilirsiniz
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div[role='application']")));
                    Thread.sleep(1500);

                    // Kanal adını al
                    String channelName = getChannelName(channel);
                    log.info("İşlenen kanal: {}", channelName);

                    // Kanal içeriğinden son mesajı çek
                    fetchMessagesFromChannel(channelName);

                } catch (Exception e) {
                    log.error("Kanal işleme hatası: ", e);
                }
            }

        } catch (Exception e) {
            log.error("fetchAllChannels hatası: ", e);
        }
    }

    /**
     * Kanal öğesinden kanal adını almak (span[title] vb.)
     */
    private String getChannelName(WebElement channelElement) {
        try {
            WebElement titleElement = channelElement.findElement(By.cssSelector("span[title]"));
            String channelName = titleElement.getAttribute("title");
            return normalizeString(channelName);
        } catch (Exception e) {
            log.error("Kanal adı alma hatası: ", e);
            return "Bilinmeyen Kanal";
        }
    }

    /**
     * Türkçe karakterleri normalleştirme (isteğe bağlı)
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
     * Kanalın içindeki son mesajı DOM üzerinden alır.
     */
    private void fetchMessagesFromChannel(String channelName) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Mesaj konteynerini bekle
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[role='application']")));

            // Tüm mesaj öğelerini bul
            List<WebElement> messages = wait.until(ExpectedConditions
                    .presenceOfAllElementsLocatedBy(By.cssSelector("div[role='row']")));

            // Son mesaj
            if (!messages.isEmpty()) {
                WebElement lastMessage = messages.get(messages.size() - 1);
                try {
                    String msgType = determineMessageType(lastMessage);
                    String msgContent = "";
                    String sender = getSenderFromMessage(lastMessage);

                    switch (msgType) {
                        case "text":
                            WebElement textElement = lastMessage.findElement(
                                    By.cssSelector("span.selectable-text"));
                            msgContent = normalizeString(textElement.getText());
                            break;
                        case "image":
                            msgContent = "[Resim]";
                            break;
                        case "sticker":
                            msgContent = "[Sticker]";
                            break;
                        case "document":
                            msgContent = "[Doküman]";
                            break;
                        case "voice":
                            msgContent = "[Sesli Mesaj]";
                            break;
                        default:
                            msgContent = "[Diğer Medya]";
                    }

                    if (!msgContent.isEmpty()) {
                        // Gönderen bulamadıysak kanal adını kullanabilirsiniz
                        if (sender == null || sender.isEmpty()) {
                            sender = channelName;
                        }
                        sender = normalizeString(sender);

                        // Veritabanına kaydet
                        messageService.saveMessage(msgContent, sender);
                    }

                } catch (Exception e) {
                    log.error("Son mesaj işleme hatası: ", e);
                }
            }

            log.info("Kanal: {}, son mesaj alındı", normalizeString(channelName));

        } catch (Exception e) {
            log.error("fetchMessagesFromChannel hatası: ", e);
        }
    }

    /**
     * Son mesajın tipini belirleme (text/image vs.)
     */
    private String determineMessageType(WebElement messageElement) {
        try {
            // Metin
            if (!messageElement.findElements(By.cssSelector("span.selectable-text")).isEmpty()) {
                return "text";
            }
            // Resim
            if (!messageElement.findElements(By.cssSelector("img[data-testid='image-thumb']")).isEmpty()) {
                return "image";
            }
            // Sticker
            if (!messageElement.findElements(By.cssSelector("img[data-testid='sticker']")).isEmpty()) {
                return "sticker";
            }
            // Doküman
            if (!messageElement.findElements(By.cssSelector("div[data-testid='document-thumb']")).isEmpty()) {
                return "document";
            }
            // Sesli mesaj
            if (!messageElement.findElements(By.cssSelector("div[data-testid='audio-player']")).isEmpty()) {
                return "voice";
            }
            return "unknown";
        } catch (Exception e) {
            log.error("Mesaj tipi belirleme hatası: ", e);
            return "unknown";
        }
    }

    /**
     * Mesaj balonundan göndereni almak. 
     * Kanallarda farklı olabilir, test ederek DOM'u güncellemelisiniz.
     */
    private String getSenderFromMessage(WebElement messageElement) {
        try {
            WebElement senderElement = messageElement.findElement(
                    By.cssSelector("div[data-pre-plain-text]"));
            String senderInfo = senderElement.getAttribute("data-pre-plain-text");

            // "[15, 2/11/2025] Amazon Indirimleri - OZEL FIRSATLAR:" formatından sadece ismi al
            if (senderInfo != null && senderInfo.contains("]")) {
                // "]" karakterinden sonraki kısmı al ve ":" karakterini kaldır
                String[] parts = senderInfo.split("]");
                if (parts.length > 1) {
                    return parts[1].replace(":", "").trim();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Gönderen bilgisi alma hatası: ", e);
            return null;
        }
    }

    /**
     * Uygulama sonlandığında driver'ı kapatmak için
     */
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
