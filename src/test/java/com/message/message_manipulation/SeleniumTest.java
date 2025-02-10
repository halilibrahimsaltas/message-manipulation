package com.message.message_manipulation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;

public class SeleniumTest {
    
    @Test
    void testBasitWebSitesi() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        
        try {
            // Wikipedia'ya git (genelde CAPTCHA olmaz)
            driver.get("https://www.wikipedia.org");
            
            // Başlığı kontrol et
            String title = driver.getTitle();
            System.out.println("Sayfa başlığı: " + title);
            
            // Basit bir doğrulama
            assertTrue(title.contains("Wikipedia"));
            
            // 2 saniye bekle (opsiyonel - sayfayı görebilmek için)
            Thread.sleep(2000);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    @Test
    void testLocalHtml() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        
        try {
            // Test klasöründeki HTML dosyasını aç
            String htmlPath = new File("src/test/resources/test.html").getAbsolutePath();
            driver.get("file:///" + htmlPath);
            
            System.out.println("Sayfa başlığı: " + driver.getTitle());
            Thread.sleep(2000);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
} 