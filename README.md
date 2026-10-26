# 🏝️ Fold Island - Dynamic Island for Samsung Galaxy Fold 7

Samsung Galaxy Fold 7 ve katlanabilir Android cihazlar için geliştirilmiş, çentik hapı (Pill), canlı HUD animasyonları, medya kontrolleri, Google Takvim geri sayımları, iğneli notlar ve bölünmüş ekran sürükle-bırak rafı (Shelf) içeren gelişmiş Dynamic Island çözümü.

---

## 📱 Özellikler

1. **Katlanabilir Ekran Uyarlaması:**
   - **Kapak Ekranı:** Kompakt hap, çift kanat (Sol: İğneli Not / Geri Sayım; Sağ: 4 çubuklu canlı ekolayzer veya batarya).
   - **İç Ekran & Flex Mod:** Genişletilebilir çoklu görev ve bölünmüş ekran rafı (Shelf).
2. **Canlı HUD Bildirimleri:**
   - Ses seviyesi, Ekran parlaklığı, Şarj bağlantısı (yeşil parıltı) ve İğneleme bildirimleri.
3. **Medya & Müzik Kontrolü:**
   - Spotify, Apple Music ve YouTube için oynat/duraklat/atla ve dinamik ritimli ekolayzer.
4. **Google Takvim & Kalan Günler:**
   - Akıllı emoji eşleştirmesi (Tatil 🏖️, Doğum günü 🎂, Uçuş ✈️, Sınav 📝) ve çentiğe iğneleme.
5. **Hızlı Notlar & To-Do:**
   - Çentikten doğrudan not alma, tamamlama ve çentiğin sol kanadına sabitleme.
6. **Üretkenlik Araçları:**
   - Su takipçisi (8 bardak hedefi), Zikir/sayaç aracı, Kronometre ve Zamanlayıcı.

---

## 📂 Proje Mimarisi

* `core/overlay/`: `IslandOverlayService` (`WindowManager` + `ComposeView`), `DisplayCutoutHelper`, `FoldStateDetector`
* `core/model/`: `IslandState`, `Preferences` (Jetpack DataStore)
* `core/theme/`: Siyah OLED, cam efektleri (`GlassSurface`), iOS tarzı yaylı animasyonlar (`IslandMotion`)
* `managers/`: `MediaManager`, `HUDManager`, `CalendarEventsManager`, `NotesStore`, `ClipboardStore`, `TimerCenter`
* `ui/island/`: `IslandRootView`, `CollapsedIslandView`, `ExpandedIslandView`, `HUDView`
* `ui/tabs/`: `HomeTabView`, `CalendarTabView`, `NotesTabView`, `ShelfTabView`, `ToolsTabView`
* `ui/settings/`: `SettingsActivity` (İzin kontrol paneli, servis başlatma ve interaktif HUD test simülatörü)

---

## 🚀 Çalıştırma & Kurulum

### Android Studio ile Açma:
1. Android Studio'yu açın ve `DynamicIsland_Android` klasörünü proje olarak seçin (`Open Project`).
2. Gradle senkronizasyonunun tamamlanmasını bekleyin.
3. Samsung Galaxy Fold cihazınızı USB veya Kablosuz Hata Ayıklama (Wireless Debugging) ile bağlayın.
4. **Run (`Shift + F10`)** tuşuna basarak uygulamayı yükleyin.

### İlk İzinler:
Uygulama ilk açıldığında `SettingsActivity` ekranı sizi karşılar:
- **Diğer Uygulamaların Üstünde Gösterim:** Çentiğin her yerde görünmesi için açın.
- **Medya & Bildirim Erişimi:** Müzik kontrolleri ve ekolayzer için açın.
- **Takvim İzni:** Geri sayım etkinliklerinin listelenmesi için onaylayın.
- **Servisi Başlat** butonuna basarak Dynamic Island'ı aktif hale getirin!
