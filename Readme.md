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
- Dinamik bot token ve chat ID yapılandırması
- Çoklu kanal/grup desteği

### Web Arayüzü

- React tabanlı modern kullanıcı arayüzü
- Gerçek zamanlı mesaj listesi
- Gelişmiş arama özellikleri
- Sistem ayarları yönetim paneli

## 🛠️ Teknolojiler

### Backend

- Java 17
- Spring Boot
- Selenium WebDriver
- PostgreSQL
- JPA/Hibernate
- Telegram Bot API
- Maven

### Frontend

- React 19
- Vite
- React Router v6
- Modern CSS
- Responsive Tasarım

## ⚙️ Kurulum

### Backend

1. Projeyi klonlayın:  
   bash
   git clone https://github.com/kullaniciadi/proje-adi.git

2. Maven ile proje derleyin:
   bash
   mvn clean install

3. Uygulamayı başlatın:
   bash
   java -jar target/whatsapp-web-1.0-SNAPSHOT.jar

### Frontend

1. Frontend klasörüne gidin:
   bash
   cd frontend

2. Bağımlılıkları yükleyin:
   bash
   npm install

3. Geliştirme sunucusunu başlatın:
   bash
   npm run dev

## 📝 Kullanım

1. `http://localhost:5173` adresinden web arayüzüne erişin
2. WhatsApp Web QR kodunu telefonunuzdan tarayın
3. Sistem otomatik olarak mesajları izlemeye başlayacak
4. Ayarlar sayfasından Telegram bot token ve chat ID'leri yapılandırın

## 🔍 API Endpoints

### Mesaj İşlemleri

- `GET /api/messages`: Tüm mesajları listeler
- `GET /api/messages/{id}`: ID'ye göre mesaj detayı
- `GET /api/messages/search`: Mesaj içeriğinde arama yapar

### Ayar İşlemleri

- `GET /api/settings`: Tüm ayarları listeler
- `GET /api/settings/{key}`: Belirli bir ayarı getirir
- `POST /api/settings/{key}`: Ayar değerini günceller

## ⚠️ Önemli Notlar

- WhatsApp Web'in açık kalması gerekir
- Telegram ayarlarının doğru yapılandırılması önemlidir
- Sistem 10 saniyede bir yeni mesajları kontrol eder
- Link dönüştürme özelliği için referans parametresi ayarlanmalıdır

## 🤝 Katkıda Bulunma

1. Bu depoyu fork edin
2. Yeni bir branch oluşturun (`git checkout -b feature/yeniOzellik`)
3. Değişikliklerinizi commit edin (`git commit -am 'Yeni özellik eklendi'`)
4. Branch'inizi push edin (`git push origin feature/yeniOzellik`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakınız.
