package com.message.message_manipulation.selenium;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
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

import com.message.message_manipulation.dto.ProductInfo;
import com.message.message_manipulation.service.MessageService;
import com.message.message_manipulation.service.LinkConversionService;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.WindowType;  

@Service
public class SeleniumService {

    private WebDriver driver;
    private final MessageService messageService;
    private final LinkConversionService linkConversionService;
    private static final Logger log = LoggerFactory.getLogger(SeleniumService.class);
    private boolean isInitialized = false;

    private static final Pattern LINK_PATTERN = 
        Pattern.compile("(https?://[^\\s]+)");

    public SeleniumService(MessageService messageService, LinkConversionService linkConversionService) {
        this.messageService = messageService;
        this.linkConversionService = linkConversionService;
    }

    private List<String> extractLinks(String text) {
        List<String> links = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(text);
        while (matcher.find()) {
            links.add(matcher.group(1));
        }
        return links;
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
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[role='application']")));

            List<WebElement> messages = wait.until(ExpectedConditions
                    .presenceOfAllElementsLocatedBy(By.cssSelector("div[role='row']")));

            int startIndex = Math.max(0, messages.size() - 3);
            List<WebElement> lastThreeMessages = messages.subList(startIndex, messages.size());

            for (WebElement message : lastThreeMessages) {
                try {
                    String msgType = determineMessageType(message);
                    if (msgType.equals("text")) {
                        WebElement textElement = message.findElement(By.cssSelector("span.selectable-text"));
                        String msgContent = normalizeString(textElement.getText());
                        String sender = getSenderFromMessage(message);
                        
                        // Link kontrolü ve işleme
                        processIncomingMessage(msgContent, sender != null ? sender : channelName);
                    }
                } catch (Exception e) {
                    log.error("Mesaj işleme hatası: ", e);
                }
            }
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
     * Siteden ürün adı, fiyat, resim linki gibi bilgileri alır.
     * Domain veya elementin varlığına göre if-else / try-catch
     */
    private ProductInfo scrapeProductInfo(String url) {
        ProductInfo info = new ProductInfo();
        info.setPageUrl(url);
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            if (url.contains("amazon.com.tr")) {
                // Amazon için selektörler
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productTitle")));
                
                info.setName(driver.findElement(By.cssSelector("#productTitle")).getText().trim());
                info.setPrice(driver.findElements(By.cssSelector("span.a-price-whole"))
                        .stream().findFirst()
                        .map(WebElement::getText)
                        .orElse("Fiyat bulunamadı"));
                info.setImageUrl(driver.findElements(By.cssSelector("#landingImage, #imgBlkFront"))
                        .stream().findFirst()
                        .map(e -> e.getAttribute("src"))
                        .orElse(""));
                        
            } else if (url.contains("trendyol.com")) {
                // Trendyol için selektörler
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".pr-new-br")));
                
                info.setName(driver.findElement(By.cssSelector(".pr-new-br")).getText().trim());
                info.setPrice(driver.findElements(By.cssSelector(".prc-dsc, .prc-slg"))
                        .stream().findFirst()
                        .map(WebElement::getText)
                        .orElse("Fiyat bulunamadı"));
                info.setImageUrl(driver.findElements(By.cssSelector(".base-product-image img"))
                        .stream().findFirst()
                        .map(e -> e.getAttribute("src"))
                        .orElse(""));
                        
            } else if (url.contains("hepsiburada.com")) {
                // Hepsiburada için selektörler
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("product-name")));
                
                info.setName(driver.findElement(By.cssSelector("#product-name")).getText().trim());
                info.setPrice(driver.findElements(By.cssSelector("[data-price], .product-price"))
                        .stream().findFirst()
                        .map(WebElement::getText)
                        .orElse("Fiyat bulunamadı"));
                info.setImageUrl(driver.findElements(By.cssSelector("#image-0"))
                        .stream().findFirst()
                        .map(e -> e.getAttribute("src"))
                        .orElse(""));
            }
            
            return info;
        } catch (Exception e) {
            log.error("Ürün bilgisi çekme hatası: {} - URL: {}", e.getMessage(), url);
            return info;
        }
    }

    /**
     * Örnek: Selenium ile WhatsApp'tan gelen mesaj
     */
    public void processIncomingMessage(String whatsappText, String sender) {
        try {
            List<String> links = extractLinks(whatsappText);
            if (links.isEmpty()) {
                log.info("Bu mesajda link yok: {}", whatsappText);
                return;
            }

            for (String link : links) {
                try {
                    String originalWindow = driver.getWindowHandle();
                    
                    // Yeni sekme aç
                    driver.switchTo().newWindow(WindowType.TAB);
                    driver.get(link);
                    
                    // Sayfa yüklenme beklemesi
                    Thread.sleep(3000);
                    
                    // Ürün bilgilerini çek
                    ProductInfo info = scrapeProductInfo(link);
                    
                    // Ürün adı kontrolü yap
                    if (info.getName() != null && !info.getName().isEmpty()) {
                        // Ürün daha önce paylaşılmış mı kontrol et
                        if (!messageService.isProductExists(info.getName())) {
                            String templateMessage = buildMessageTemplate(info);
                            messageService.saveMessage(templateMessage, sender);
                            log.info("Yeni ürün paylaşıldı: {}", info.getName());
                        } else {
                            log.info("Bu ürün zaten paylaşılmış: {}", info.getName());
                        }
                    }
                    
                    // Sekmeyi kapat ve ana sekmeye dön
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    log.error("Link işleme hatası: {} - Link: {}", e.getMessage(), link);
                }
            }
        } catch (Exception e) {
            log.error("Mesaj işleme hatası: {}", e.getMessage());
        }
    }

    /**
     * Ürün bilgisine dayalı mesaj şablonu
     */
    private String buildMessageTemplate(ProductInfo info) {
        // Önce link dönüşümünü yap
        String convertedLink = linkConversionService.generateTrackingLink(info.getPageUrl());
        
        return String.format("""
            %s
            
            💰₺ %s
            
            
            🔗 %s
            
            #işbirliği """,
            info.getName(),
            info.getPrice(),
            convertedLink  // Dönüştürülmüş linki kullan
        );
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
