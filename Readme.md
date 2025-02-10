# WhatsApp Web Mesaj İzleme ve İletme Sistemi

Bu proje, WhatsApp Web üzerinden gelen mesajları Selenium kullanarak otomatik olarak izler, PostgreSQL veritabanına kaydeder ve Telegram'a iletir.

## 🚀 Özellikler

### WhatsApp Web Entegrasyonu

- Selenium WebDriver ile otomatik tarayıcı kontrolü
- WhatsApp Web üzerinden mesajların gerçek zamanlı takibi
- QR kod tarama sonrası otomatik mesaj izleme

### Veritabanı İşlemleri

- PostgreSQL veritabanı entegrasyonu
- Mesaj içeriği, gönderen ve zaman bilgilerinin saklanması
- JPA/Hibernate ile ORM desteği

### Telegram Entegrasyonu

- Telegram Bot API kullanarak mesaj iletimi
- HTML formatında özelleştirilmiş mesaj yapısı
- Otomatik kanal/grup mesaj gönderimi

## 🛠️ Teknolojiler

- Java 17
- Spring Boot
- Selenium WebDriver
- PostgreSQL
- JPA/Hibernate
- Telegram Bot API
- Maven

## ⚙️ Kurulum

1. Projeyi klonlayın:  
   bash
   git clone https://github.com/kullaniciadi/proje-adi.git

2. Maven ile proje derleyin:
   bash
   mvn clean install

3. Uygulamayı başlatın:
   bash
   java -jar target/whatsapp-web-1.0-SNAPSHOT.jar

## 📝 Kullanım

1. Uygulama başlatıldığında Chrome tarayıcısı otomatik açılacaktır
2. WhatsApp Web QR kodunu telefonunuzdan tarayın
3. Tarama işlemi tamamlandıktan sonra sistem otomatik olarak mesajları izlemeye başlayacaktır
4. İzlenen mesajlar veritabanına kaydedilip Telegram'a iletilecektir

## 🔍 API Endpoints

- `GET /api/messages`: Tüm mesajları listeler
- `GET /api/messages/{id}`: ID'ye göre mesaj detayını getirir

## ⚠️ Önemli Notlar

- WhatsApp Web'in açık kalması ve QR kodunun taranmış olması gerekir
- Telegram bot token ve chat ID değerlerinin doğru yapılandırılması önemlidir
- Sistem 10 saniyede bir otomatik olarak yeni mesajları kontrol eder

## 🤝 Katkıda Bulunma

1. Bu depoyu fork edin
2. Yeni bir branch oluşturun (`git checkout -b feature/yeniOzellik`)
3. Değişikliklerinizi commit edin (`git commit -am 'Yeni özellik eklendi'`)
4. Branch'inizi push edin (`git push origin feature/yeniOzellik`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakınız.
