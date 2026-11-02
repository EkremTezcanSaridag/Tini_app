<div align="center">

<img src="docs/icon.png" width="120" alt="Tını Icon"/>

# Tını

**Gitarınız için minimal ve hassas akort uygulaması**

[![Android CI](https://github.com/EkremTezcanSaridag/Tini_app/actions/workflows/build.yml/badge.svg)](https://github.com/EkremTezcanSaridag/Tini_app/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-blue.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-7.0%2B%20(API%2024)-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📱 Ekran Görüntüleri

<table>
  <tr>
    <td><img src="docs/screenshots/in_tune.png" width="220" alt="Akortlu"/></td>
    <td><img src="docs/screenshots/too_low.png" width="220" alt="Pest"/></td>
    <td><img src="docs/screenshots/too_high.png" width="220" alt="Tiz"/></td>
    <td><img src="docs/screenshots/permission.png" width="220" alt="İzin Ekranı"/></td>
  </tr>
  <tr>
    <td align="center"><b>Akortlu (0 cent)</b></td>
    <td align="center"><b>Pest (-25 cent)</b></td>
    <td align="center"><b>Tiz (+30 cent)</b></td>
    <td align="center"><b>Mikrofon İzni</b></td>
  </tr>
</table>

---

## ✨ Özellikler

- **🎯 YIN Perde Algılama:** Yüksek hassasiyetli perde algılama algoritması ile profesyonel sonuçlar
- **🎸 Çoklu Akort Desteği:** 5 farklı akort türü
  - **Standart:** E A D G B E
  - **Drop D:** D A D G B E
  - **DADGAD:** D A D G A D
  - **Open G:** D G D G B D
  - **Open D:** D A D F# A D
- **✅ Hassas Akort:** ±5 cent toleransla tam akort göstergesi
- **🎚️ A4 Referans Ayarı:** 430-450 Hz arası özelleştirilebilir (varsayılan: 440 Hz)
- **🎭 Akıllı Modlar:**
  - **Otomatik:** Çaldığınız teli otomatik algılar
  - **Manuel:** İstediğiniz teli seçip akort edin
- **📳 Haptik Geri Bildirim:** Akort tamamlandığında titreşim
- **🎨 Minimal Arayüz:** Apple tarzı temiz ve modern tasarım
- **📊 Gerçek Zamanlı Gösterge:** 
  - Büyük not gösterimi (örn: E2, A2, D3)
  - Frekans (Hz) gösterimi
  - -50 ile +50 cent arası yatay cetvel
  - Renkli durum göstergesi (yeşil: akortlu, turuncu: pest, kırmızı: tiz)

---

## 🎵 "Cent" Nedir?

**Cent**, müzikte kullanılan logaritmik bir frekans ölçü birimidir. İki nota arasındaki mesafe 100 cent'tir.

- **0 cent:** Tam akortlu
- **Negatif değer (örn: -25 cent):** Pest (teli gerdirmeniz gerekiyor)
- **Pozitif değer (örn: +30 cent):** Tiz (teli gevşetmeniz gerekiyor)

Tını, **±5 cent** toleransla akordunuzu kontrol eder. Bu hassasiyet profesyonel müzisyenler için yeterlidir.

---

## 📥 Kurulum

### Yöntem 1: GitHub Actions (Önerilen)

1. [Actions sayfasına](https://github.com/EkremTezcanSaridag/Tini_app/actions) gidin
2. Son başarılı çalıştırmaya tıklayın
3. **Artifacts** bölümünden `Tini-apk` dosyasını indirin
4. ZIP'i açın ve `app-debug.apk` dosyasını telefonunuza yükleyin

### Yöntem 2: GitHub Releases

1. [Releases sayfasına](https://github.com/EkremTezcanSaridag/Tini_app/releases) gidin
2. En son sürümü (v2.1) indirin
3. APK dosyasını telefonunuza yükleyin

### ⚠️ Önemli Not: Eski Sürümlerden Güncelleme

**v2.1 öncesi kurulu bir sürümünüz varsa, uygulamayı önce kaldırmalısınız.** Nedeni: v2.1 ile birlikte imza anahtarı değiştirilmiştir (kararlı imzalar için). Eski sürümü kaldırmadan yeni sürümü yükleyemezsiniz.

```bash
# Eski sürümü kaldırın (Ayarlar > Uygulamalar > Tını > Kaldır)
# Ardından yeni APK'yı yükleyin
```

v2.1 ve sonraki sürümler aynı imza anahtarını kullanacağı için sorunsuz güncellenecektir.

---

## 🛠️ Kaynaktan Derleme

### Gereksinimler

- Android Studio Hedgehog | 2023.1.1 veya üzeri
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Derleme Adımları

```bash
# Depoyu klonlayın
git clone https://github.com/EkremTezcanSaridag/Tini_app.git
cd Tini_app

# Debug APK oluşturun
./gradlew assembleDebug

# APK konumu: app/build/outputs/apk/debug/app-debug.apk
```

### Testler

```bash
# Tüm testleri çalıştırın (25 birim testi)
./gradlew test

# Test raporu: app/build/reports/tests/testDebugUnitTest/index.html
```

### Ekran Görüntüleri (Roborazzi)

```bash
# Ekran görüntülerini oluşturun (yerel olarak çalıştırın)
./gradlew recordRoborazziDebug

# Çıktılar: app/build/outputs/roborazzi/
```

**Not:** Ekran görüntüsü testleri CI'da `@Ignore` ile devre dışı bırakılmıştır (Roborazzi NATIVE grafik modu ekran gerektirir).

---

## 📂 Proje Yapısı

```
Tini_app/
├── app/
│   ├── src/main/
│   │   ├── java/com/tini/tuner/
│   │   │   ├── MainActivity.kt          # Ana Activity (izinler, yaşam döngüsü)
│   │   │   ├── TunerViewModel.kt        # Durum yönetimi ve iş mantığı
│   │   │   ├── TunerScreen.kt           # UI bileşenleri (Compose)
│   │   │   ├── AudioCapture.kt          # Mikrofon ses kaydı
│   │   │   ├── YinPitchDetector.kt      # YIN perde algılama algoritması
│   │   │   ├── TuningUtils.kt           # Akort yardımcı fonksiyonları
│   │   │   └── Models.kt                # Veri modelleri
│   │   └── res/
│   │       ├── drawable/                # Vektör grafikler (ikon katmanları)
│   │       ├── font/                    # Inter font ailesi
│   │       └── values/                  # Tema, renkler
│   ├── src/test/
│   │   └── java/com/tini/tuner/
│   │       ├── TunerScreenshotTest.kt   # Roborazzi UI testleri
│   │       ├── TuningUtilsTest.kt       # Akort mantık testleri
│   │       └── YinPitchDetectorTest.kt  # YIN algoritma testleri
│   ├── tini-debug.keystore              # Debug imza anahtarı (kararlı imzalar)
│   └── build.gradle.kts                 # Modül yapılandırması
├── docs/
│   ├── icon.png                         # Uygulama ikonu (README için)
│   └── screenshots/                     # Gerçek ekran görüntüleri
├── .github/workflows/
│   └── build.yml                        # CI/CD (test + APK artifact)
└── README.md                            # Bu dosya
```

---

## 🛠️ Teknoloji Yığını

### **Android & Kotlin**
- **Kotlin** 1.9.20
- **Android Gradle Plugin** 8.2.0
- **minSdk:** 24 (Android 7.0 Nougat)
- **targetSdk:** 34 (Android 14)

### **UI & Tasarım**
- **Jetpack Compose** (Material 3)
- **Inter Font Family** (Google Fonts)
- Minimal beyaz tema (Apple tarzı)
- Duyarlı düzen (BoxWithConstraints)

### **Ses İşleme**
- **AudioRecord** (düşük gecikmeli ses yakalama)
- **YIN Algoritması** (gelişmiş perde algılama)
- Üstel Hareketli Ortalama (EMA) yumuşatma

### **Yaşam Döngüsü & Durum**
- **ViewModel** (durum yönetimi)
- **StateFlow** (reaktif UI)
- **Lifecycle-aware** bileşenler (onStop/onStart'ta ses durdurma/başlatma)

### **Test & Kalite**
- **JUnit 4** (birim testleri)
- **Robolectric** (Android bileşen testleri)
- **Roborazzi** (Compose ekran görüntüsü testleri)
- **GitHub Actions CI** (otomatik derleme ve test)

### **İmzalama & Dağıtım**
- Özel debug keystore (kararlı imzalar)
- APK Signature Scheme v2
- GitHub Actions artifacts

---

## 🚀 Yol Haritası

### v2.1 ✅ (Mevcut Sürüm)
- [x] YIN perde algılama
- [x] 5 akort türü desteği
- [x] Otomatik/Manuel modlar
- [x] A4 referans ayarı (430-450 Hz)
- [x] Haptik geri bildirim
- [x] Minimal beyaz tema
- [x] Kararlı imza anahtarı
- [x] Edge-to-edge insets
- [x] Yaşam döngüsü ses yönetimi

### v2.2 🔜 (Planlanan)
- [ ] Daha fazla akort türü (Half Step Down, Drop C, vs.)
- [ ] Metronom özelliği
- [ ] Akort geçmişi kaydetme
- [ ] Koyu tema desteği
- [ ] Widget desteği
- [ ] Akor kütüphanesi

### v3.0 💭 (Gelecek)
- [ ] Çok kanallı ses analizi (her telin ayrı ayrı akort edilmesi)
- [ ] Spektrum görselleştirme
- [ ] Ses profili kaydetme ve paylaşma
- [ ] Diğer enstrümanlar için destek (bas, ukulele, vs.)

---

## 🐛 Hata Bildirimi

Bir hata buldunuz mu? [Issue açın](https://github.com/EkremTezcanSaridag/Tini_app/issues/new) veya pull request gönderin!

---

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

---

## 🙏 Teşekkürler

- **YIN Algoritması:** Alain de Cheveigné ve Hideki Kawahara tarafından geliştirilmiştir
- **Inter Font:** Rasmus Andersson tarafından tasarlanmıştır
- **İkon Tasarımı:** Minimal dalga + aksan noktası

---

<div align="center">

## 🌍 English Summary

**Tını** is a minimal and precise guitar tuner app for Android built with Jetpack Compose and Kotlin.

**Features:**
- YIN pitch detection algorithm for professional accuracy
- 5 tuning presets (Standard, Drop D, DADGAD, Open G, Open D)
- ±5 cent in-tune tolerance
- Auto/Manual string selection modes
- A4 reference adjustment (430-450 Hz)
- Haptic feedback on in-tune
- Clean Apple-style minimal white UI

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), AudioRecord, YIN Algorithm, ViewModel, StateFlow, Roborazzi, GitHub Actions CI

**Download:** [GitHub Releases](https://github.com/EkremTezcanSaridag/Tini_app/releases) or [Actions Artifacts](https://github.com/EkremTezcanSaridag/Tini_app/actions)

---

**Geliştirici:** [Ekrem Tezcan Sarıdağ](https://github.com/EkremTezcanSaridag)  
**Versiyon:** 2.1  
**Son Güncelleme:** 2 Kasım 2026

</div>
