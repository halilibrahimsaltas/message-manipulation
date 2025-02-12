package com.message.message_manipulation.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

@Service
public class LinkConversionService {

    private static final String BASE_API_URL = "https://sh.gelirortaklari.com/shortlink";
    private static final String AFFILIATE_ID = "38040";  // Your aff_id
    private static final String ADGROUP_ID = "38040";    // Your adgroup ID
    private static final String LOCALE = "tr";          // Turkish locale

    private static final Logger log = LoggerFactory.getLogger(LinkConversionService.class);

    

    public String generateTrackingLink(String originalUrl) {
        try {
            int offerId = getOfferIdFromWebsite(originalUrl);
            if (offerId == -1) {
                log.warn("URL için uygun offer_id bulunamadı: {}", originalUrl);
                return "Bu URL için uygun offer_id bulunamadı!";
            }

            // Encode the original URL properly
            String encodedUrl = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);

            // Construct API request URL
            String apiRequestUrl = buildApiRequestUrl(encodedUrl, offerId);
            log.info("🛰️ API isteği gönderiliyor: {}", apiRequestUrl);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiRequestUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            // Log the full API response
            log.info("📡 API yanıtı alındı: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Extract `shortlink` safely
                if (responseBody.containsKey("shortlink")) {
                    String shortLink = responseBody.get("shortlink").toString();
                    log.info("✅ Başarıyla oluşturulan kısa link: {}", shortLink);
                    return shortLink;
                } else {
                    log.error("🚨 API yanıtında 'shortlink' bulunamadı!");
                }
                return "API yanıtı geçersiz!";
            }

            log.error("❌ API yanıtı başarısız: {}", response.getStatusCode());
            return "Tracking Link oluşturulamadı!";

        } catch (RestClientException e) {
            log.error("🚨 API isteği sırasında hata: ", e);
            return "API isteği başarısız oldu!";
        } catch (Exception e) {
            log.error("🔥 Beklenmeyen hata: ", e);
            return "İşlem sırasında hata oluştu!";
        }
    }

    /**
     * Build the API request URL with correctly encoded parameters.
     */
    private String buildApiRequestUrl(String encodedUrl, int offerId) {
        return BASE_API_URL +
                "?aff_id=" + AFFILIATE_ID +
                "&adgroup=" + ADGROUP_ID +
                "&url=" + encodedUrl +
                "&offer_id=" + offerId +
                "&locale=" + LOCALE;
    }

    /**
     * Determine offer_id based on the detected website name.
     */
    private int getOfferIdFromWebsite(String url) {
        Map<String, Integer> offerIdMap = new HashMap<>();
        offerIdMap.put("amazon", 6718);
        offerIdMap.put("boyner", 6568);
        offerIdMap.put("getir", 6906);
        offerIdMap.put("decathlon", 6786);
        offerIdMap.put("karaca", 6716);
        offerIdMap.put("mediamarkt", 6816);
        offerIdMap.put("n11", 6717);
        offerIdMap.put("trendyol", 6719);
        offerIdMap.put("hepsiburada", 6720);
        offerIdMap.put("ciceksepeti", 6721);
        offerIdMap.put("gittigidiyor", 6722);
        offerIdMap.put("supplementler", 5528); 
        offerIdMap.put("amzn", 6718);// Example: Supplementler.com

        for (Map.Entry<String, Integer> entry : offerIdMap.entrySet()) {
            if (url.toLowerCase().contains(entry.getKey())) {
                log.info("🔍 {} için offer_id bulundu: {}", entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }
        log.warn("⚠️ URL için eşleşen offer_id bulunamadı: {}", url);
        return -1;
    }
}
