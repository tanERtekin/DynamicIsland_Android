# 🏝️ Dynamic Island for Android (Samsung Galaxy Fold 7)
## Kapsamlı Özellik, Mimari ve Geliştirme Spesifikasyonu

Bu doküman, macOS üzerinde geliştirilen **Dynamic Island** uygulamasındaki tüm özellikleri, mimari çözümleri ve Android (özellikle **Samsung Galaxy Fold 7** gibi katlanabilir cihazlar) platformuna uyarlama yol haritasını içerir. Yeni Android projenizde rehber olarak kullanabilirsiniz.

---

## 📱 1. Vizyon & Katlanabilir Ekran (Fold 7) Yaklaşımı

Galaxy Fold 7, çift ekranlı (Dış Kapak Ekranı ve Katlanabilir İç Tablet Ekranı) yapısıyla Dynamic Island konsepti için dünyadaki en ideal cihazlardan biridir.

### Çift Ekran Uyarlaması:
1. **Dış Kapak Ekranı (Katlı Durum):**
   * Ön kamera deliğinin etrafında ince, kompakt ve akıcı bir hap (Pill).
   * Bildirimler, müzik kontrolü, şarj durumu, zamanlayıcı ve iğneli notları gösterir.
2. **İç Tablet Ekranı (Açık Durum):**
   * Geniş alanda sadece bir ada değil; aynı zamanda **"Çoklu Görev Rafı" (Shelf)** ve **"Yüzen Üretkenlik Çubuğu"** haline gelebilir.
   * Bölünmüş ekranda (Split Screen) çalışırken uygulamalar arası metin ve görsel taşımak için sürükle-bırak istasyonu olarak görev yapar.
3. **Flex Mod (Yarı Katlı Masaüstü Modu):**
   * Üst ekranda medya veya toplantı oynarken, ada alt ekranda dokunmatik kontrol paneline dönüşür.

---

## 🛠️ 2. Teknoloji Yığını (Tech Stack)

* **Programlama Dili:** Kotlin
* **Kullanıcı Arayüzü (UI):** Jetpack Compose (Modern bildirimsel UI, SwiftUI benzeri State ve Animation mimarisi)
* **Pencere & Overlay Yönetimi:** Android `WindowManager` (`TYPE_APPLICATION_OVERLAY`)
* **Katlanabilir Ekran Takibi:** AndroidX `WindowManager` (`FoldingFeature`, `WindowInfoTracker`)
* **Arka Plan Yönetimi:** Android `LifecycleService` (Foreground Service)
* **Animasyonlar:** Jetpack Compose `Animatable`, `spring(dampingRatio, stiffness)`
* **Veri Kalıcılığı:** Jetpack DataStore / Room Database

---

## 🚀 3. Modüller ve Özellik Envanteri

Aşağıdaki özelliklerin tümü macOS uygulamasında test edilmiş ve doğrulanmış mantığa dayanır:

### A. Çentik Hapı & Çift Kanat Mimarisi (Collapsed Island)
* **Sol Kanat (Left Wing):**
  * Öncelik Sıralaması: İğneli Not (`📌`) > İğneli Takvim Geri Sayımı (`🏖️ 12g`) > Aktif Zamanlayıcı/Kronometre > Saat / Tarih / Hava Durumu.
* **Sağ Kanat (Right Wing):**
  * Akıllı Mod: Müzik çalıyorsa mini albüm kapağı + canlı ekolayzer çubukları; çalmıyorsa batarya yüzdesi veya ağ hızı.
* **Etkileşim:**
  * Tek Dokunuş: Adayı tam panele genişletir (`expand`).
  * İki Dokunuş / Kaydırma: Hızlı işlem (Örn: Hızlı not alma veya sesi kısma).
  * Dokunmatik Geri Bildirim: `HapticFeedbackConstants.CLOCK_TICK`.

### B. Canlı HUD Animasyonları (Head-Up Display)
Sistem olaylarında ada otomatik genişleyip şık bir hap animasyonu gösterir:
1. **Ses HUD:** Ses tuşuna basıldığında ses yüzdesi + dinamik hoparlör simgesi + akıcı dolum kapsülü.
2. **Parlaklık HUD:** Parlaklık değiştiğinde güneş simgesi + yüzde barı.
3. **Şarj & Pil HUD:** Şarj kablosu takıldığında veya kablosuz şarjda yeşil şimşek + yüzde animasyonu.
4. **İğneleme HUD:** Not veya geri sayım iğnelendiğinde çentikte sarı `📌 İğnelendi` animasyonu.

### C. Müzik & Medya Kontrolü (Now Playing)
* Spotify, YouTube, Apple Music gibi tüm medya oynatıcıları otomatik dinler.
* Albüm kapağı, parça adı, sanatçı, ilerleme çubuğu (Seek bar), Oynat/Duraklat/İleri/Geri.
* 4 çubuklu canlı ritmik ekolayzer animasyonu (`EqualizerBars`).
* `MediaSessionManager` ve `MediaController` ile tam senkronizasyon.

### D. Pano (Clipboard) Yöneticisi
* Arka planda kopyalanan son 20 metni depolar.
* Arama çubuğu ile pano geçmişinde filtreleme.
* Tek tıkla eski bir metni tekrar panoya kopyalama.
* Hassas verileri (şifreler vb.) tek tuşla temizleme.

### E. Kalan Günler & Google Takvim Entegrasyonu
* **Google Takvim Senkronizasyonu:** `CalendarContract.Events` üzerinden Google Takvim'deki önümüzdeki 365 günlük doğum günleri, tatiller, sınavlar vb. otomatik taranır.
* **Akıllı Emoji Tahmini:** Etkinlik adına göre otomatik simge:
  * Doğum günü → 🎂, Tatil → 🏖️, Uçuş → ✈️, Sınav → 📝, Konser → 🎟️, Toplantı → 💼, Spor/Maç → 🏆 vb.
* **Geri Sayım:** `14 gün`, `Yarın`, `Bugün!` veya 24 saatten azsa canlı `14s 35dk`.
* **Çentiğe İğneleme (Pin to Notch):** Seçilen etkinliği çentiğin kanadına sabitleme (`🏖️ Tatil: 12g`).

### F. Hızlı Notlar & To-Do (Scratchpad)
* Çentikten anında açılan hızlı not defteri.
* Her notun başında onay kutusu (Checkbox) ve çentiğe iğneleme (`📌`) butonu.
* İğnelenen not çentiğin sol kanadında kalır; üzerine tıklandığında doğrudan tamamlanır.

### G. Sürükle-Bırak Rafı (Shelf / Drop Zone)
* Katlanabilir ekranın en güçlü özelliği!
* Ekrandaki herhangi bir resmi, metni veya dosyayı çentiğin üzerine sürükleyip bırakarak geçici olarak "park etme".
* Sonra bölünmüş ekrandaki diğer uygulamaya (Örn: WhatsApp, Mail, Notlar) oradan sürükleyip yapıştırma.

### H. Üretkenlik Araçları (Tools)
* **Su Takibi (Water Tracker):** Günlük hedef (Örn: 8 bardak), tek tıkla içme/geri alma, gece yarısı otomatik sıfırlama.
* **Sayaç (Tally Counter):** Genel amaçlı zikir/sayım aracı.
* **Zamanlayıcı & Kronometre:** Çentikte saniye saniye geri sayım gösterimi.
* **Yüzen Prompter (Teleprompter):** Kamera kaydı veya sunum yaparken çentiğin hemen altında akan metin penceresi.
* **Hava Durumu:** Anlık sıcaklık ve durum ikonu.

---

## 📂 4. Android Proje Mimarisi (Klasör Yapısı)

```text
app/src/main/java/com/example/foldisland/
│
├── core/
│   ├── overlay/
│   │   ├── IslandOverlayService.kt       # WindowManager ile ada penceresini yöneten Foreground Service
│   │   ├── DisplayCutoutHelper.kt       # Fold 7 kamera koordinatlarını (Cutout) hesaplayan yardımcı
│   │   └── FoldStateDetector.kt         # Dış ekran vs İç ekran durumunu dinleyen sınıf
│   ├── model/
│   │   ├── IslandState.kt               # Collapsed, Expanded, HUD modları
│   │   └── Preferences.kt               # Kullanıcı ayarları (DataStore)
│   └── theme/
│       ├── Color.kt                     # Siyah cam (Glassmorphism), neon vurgu renkleri
│       └── Motion.kt                    # Yaylı animasyon sabitleri (Spring Specs)
│
├── managers/
│   ├── MediaManager.kt                  # NotificationListenerService + MediaSession
│   ├── HUDManager.kt                    # Ses, parlaklık, şarj olaylarını yöneten EventBus
│   ├── CalendarEventsManager.kt         # Google Takvim ve Kalan Günler mantığı
│   ├── NotesStore.kt                    # İğneli notlar ve To-Do veritabanı
│   ├── ClipboardStore.kt                # Pano geçmişi takipçisi
│   └── TimerCenter.kt                   # Kronometre ve zamanlayıcı servisleri
│
└── ui/
    ├── island/
    │   ├── IslandRootView.kt            # Pencerenin ana Compose kapsayıcısı
    │   ├── CollapsedIslandView.kt       # Siyah hap (Sol kanat, Kamera boşluğu, Sağ kanat)
    │   ├── ExpandedIslandView.kt        # Genişletilmiş panel ve sekme navigasyonu
    │   └── HUDView.kt                   # Ses/Parlaklık/Şarj hap animasyonu
    │
    ├── tabs/
    │   ├── HomeTabView.kt               # Medya kontrolü, hızlı araçlar, iğneli özet
    │   ├── CalendarTabView.kt           # Takvim etkinlikleri ve Kalan Günler listesi
    │   ├── NotesTabView.kt              # Notlar ve yapılacaklar listesi
    │   ├── ShelfTabView.kt              # Sürükle-bırak park rafı (Galaxy Fold için)
    │   └── ToolsTabView.kt              # Su, sayaç, zamanlayıcı
    │
    └── settings/
        └── SettingsActivity.kt          # İzinler, hizalama kalibrasyonu ve renk seçimi
```

---

## 🔑 5. Android Manifest & Kritik İzinler

Uygulamanın çalışması için gereken temel izinler:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Diğer uygulamaların üstüne çizim izni -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    
    <!-- Ön plan servisi (Android 14+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    
    <!-- Müzik ve bildirimleri dinleme -->
    <uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />
    
    <!-- Google Takvim okuma -->
    <uses-permission android:name="android.permission.READ_CALENDAR" />
    
    <!-- Titreşim & Dokunsal Geri Bildirim -->
    <uses-permission android:name="android.permission.VIBRATE" />
</manifest>
```

---

## 💡 6. Geliştirme Adımları (Checklist)

1. [ ] **Adım 1 - Overlay Penceresi:** `WindowManager` ile ekranda her şeyin üstünde duran temel bir Compose görünümü oluşturun.
2. [ ] **Adım 2 - Cutout Hizalama:** `WindowInsets.displayCutout` kullanarak hapı Galaxy Fold 7 kamerasının tam merkezine yerleştirin.
3. [ ] **Adım 3 - Fold Listener:** `WindowInfoTracker` ekleyip telefon açıldığında ada genişliğini ve konumunu iç ekrana göre yeniden ölçeklendirin.
4. [ ] **Adım 4 - Genişleme Animasyonu:** Tıklama ile `animateContentSize(spring())` kullanarak yumuşak bir genişleme sağlayın.
5. [ ] **Adım 5 - Medya ve Takvim:** Spotify/YouTube kontrollerini ve Google Takvim Kalan Günler entegrasyonunu bağlayın.
6. [ ] **Adım 6 - Galaxy Fold Sürükle-Bırak:** Samsung çoklu pencere modunda çalışacak `Shelf` rafını ekleyin.
