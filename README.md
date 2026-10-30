# Tını - Gitar Akort Cihazı

**Tını**, Android telefonunuz için geliştirilmiş profesyonel bir gitar akort uygulamasıdır. Mikrofonunuzu kullanarak gitarınızın her telinin frekansını algılar ve size hassas akort yardımı sağlar.

## Özellikler

### 🎸 Çok Yönlü Akort Desteği
- **Standart Akort**: E2, A2, D3, G3, B3, E4
- **Drop D**: D2, A2, D3, G3, B3, E4
- **Yarım Ton Pes**: Eb standard
- **Open G**: D2, G2, D3, G3, B3, D3
- **DADGAD**: D2, A2, D3, G3, A3, D3

### 🎯 Hassas Algılama
- **YIN Algoritması**: Düşük frekanslı teller için optimize edilmiş profesyonel pitch algılama
- **Gerçek Zamanlı Analiz**: Anlık frekans gösterimi (Hz cinsinden)
- **Cent Göstergesi**: ±50 cent hassasiyetle akort durumu
- **Gürültü Filtresi**: Sessizlik ve düşük seviye gürültü otomatik olarak filtrelenir
- **Yumuşatma**: Kararlı okumalar için sinyal yumuşatma

### 🎼 Akıllı Özellikler
- **Otomatik Tel Algılama**: Çaldığınız sese en yakın teli otomatik seçer
- **Manuel Tel Seçimi**: İstediğiniz teli elle seçebilirsiniz
- **Ayarlanabilir A4 Referansı**: 430-450 Hz arası ayarlanabilir (varsayılan 440 Hz)
- **Görsel Geri Bildirim**: 
  - 🔵 Mavi: "Teli sık ↑" (frekans düşük)
  - 🟡 Sarı: "Teli gevşet ↓" (frekans yüksek)
  - 🟢 Yeşil: "Akortlu ✓" (±5 cent içinde)

### 🌙 Modern Arayüz
- Karanlık tema ile göz yormayan tasarım
- Büyük, kolay okunabilir ibre göstergesi
- Sezgisel tel seçimi
- Tam Türkçe arayüz

## Kurulum

### Android Telefona APK Yükleme

1. **APK Dosyasını İndirin**
   - `Tini.apk` dosyasını telefonunuza aktarın (USB, e-posta, Bluetooth vb. ile)

2. **Bilinmeyen Kaynaklardan Kuruluma İzin Verin**
   - Ayarlar → Güvenlik → Bilinmeyen Kaynaklar (veya Android 8+: Uygulamalar ve bildirimler → Özel uygulama erişimi → Bilinmeyen uygulamaları yükle)
   - Dosya yöneticiniz veya tarayıcınız için izni etkinleştirin

3. **APK'yi Kurun**
   - Dosya yöneticinizde `Tini.apk` dosyasına dokunun
   - "Yükle" butonuna basın
   - Kurulum tamamlandığında "Aç" butonuna basın

4. **Mikrofon İznini Verin**
   - İlk açılışta uygulama mikrofon izni isteyecektir
   - "İzin Ver" butonuna basarak mikrofonun kullanılmasına izin verin

### Uygulamayı Kullanma

1. **Akort Türünü Seçin**
   - Ekranın üstündeki "Standart" butonuna basarak istediğiniz akort türünü seçin

2. **A4 Referansını Ayarlayın** (İsteğe bağlı)
   - "A4: 440 Hz" butonuna basarak referans frekansı değiştirebilirsiniz
   - 430-450 Hz arası ayarlanabilir

3. **Dinlemeye Başlayın**
   - "Dinlemeye Başla" butonuna basın
   - Gitarınızın bir telini çalın
   - Otomatik modda uygulama en yakın teli seçecektir
   - Manuel mod için tele dokunarak seçebilirsiniz

4. **Akort Yapın**
   - İbre ve yazı talimatlarını takip edin:
     - **Mavi "Teli sık ↑"**: Teli sıkılaştırın (akort anahtarını saat yönünde çevirin)
     - **Sarı "Teli gevşet ↓"**: Teli gevşetin (akort anahtarını saat yönünün tersine çevirin)
     - **Yeşil "Akortlu ✓"**: Tel akortlu! (±5 cent içinde)

5. **Diğer Teller**
   - Her teli tek tek akort edin
   - Tüm telleri akortladıktan sonra tekrar kontrol edin (teller birbirini etkileyebilir)

## Teknik Özellikler

- **Minimum Android Sürümü**: Android 7.0 (API 24)
- **Hedef Android Sürümü**: Android 14 (API 34)
- **Örnekleme Hızı**: 44,100 Hz
- **Pitch Algılama**: YIN algoritması
- **Frekans Aralığı**: 50-1000 Hz
- **Hassasiyet**: ±1 cent (testlerle doğrulanmıştır)

## Geliştirme

### Proje Yapısı

```
/workspace/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tini/tuner/
│   │   │   │   ├── MainActivity.kt          # Ana aktivite ve izin yönetimi
│   │   │   │   ├── TunerScreen.kt           # Jetpack Compose UI
│   │   │   │   ├── TunerViewModel.kt        # Durum yönetimi
│   │   │   │   ├── PitchDetector.kt         # YIN pitch algılama algoritması
│   │   │   │   ├── AudioCapture.kt          # Mikrofon ses yakalama
│   │   │   │   └── TuningModel.kt           # Akort modelleri ve yardımcılar
│   │   │   ├── res/                         # Android kaynakları
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── java/com/tini/tuner/
│   │           ├── PitchDetectorTest.kt     # Pitch algılama testleri
│   │           └── TuningUtilsTest.kt       # Akort yardımcı testleri
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### Testler

Proje, pitch algılama algoritmasının doğruluğunu garanti eden kapsamlı birim testleri içerir:

```bash
./gradlew test
```

**Test Kapsamı**:
- Tüm standart akort frekansları (E2, A2, D3, G3, B3, E4)
- Drop D frekansı
- A4 referans (440 Hz)
- Sessizlik algılama
- Düşük amplitüd gürültü filtreleme
- Hafif pest/tiz notalar
- Cent hesaplama
- Otomatik tel algılama
- A4 referans ayarlama

**Test Sonucu**: ✅ 21/21 test başarıyla geçti

Her hedef frekans sentetik sinüs dalgalarıyla test edilmiş ve ±1 cent hassasiyetle doğrulanmıştır.

### APK Oluşturma

```bash
./gradlew assembleDebug
```

APK dosyası şurada oluşturulur: `app/build/outputs/apk/debug/app-debug.apk`

## Lisans

Bu proje Cursor Cloud Agent tarafından oluşturulmuştur.

## İletişim & Destek

Sorunlar veya öneriler için GitHub Issues kullanın.

---

**Not**: Bu uygulama gerçek bir Android cihazda mikrofonla test edilmek üzere tasarlanmıştır. Emülatörler mikrofon desteği sınırlı olabilir.
