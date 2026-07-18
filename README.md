# 📅 Remainder

**Remainder** is a minimalist Android application that transforms your phone's live wallpaper into a visual representation of your year.

Instead of displaying dates or numbers, Remainder uses **a grid of dots**, where each dot represents a single day of the year. As each day passes, its corresponding dot changes color, giving you a subtle but powerful reminder of how much of the year has already gone by.

The goal is simple:

> **Make time visible.**

---

## ✨ Features

- 🎨 **Live Wallpaper**
  - Turns your home screen into a dynamic year tracker.
  - Updates automatically every day.

- ⚫ **365-Day Dot Grid**
  - Each dot represents one day.
  - Completed days are highlighted.
  - Remaining days stay dim, making progress instantly visible.

- 📅 **Leap Year Support**
  - Automatically adjusts to 366 days when required.

- 🌙 **Minimal Design**
  - Clean and distraction-free.
  - Designed to blend naturally with your wallpaper.

- ⚡ **Lightweight**
  - Minimal battery consumption.
  - Runs efficiently as a live wallpaper.

- 🎯 **Daily Motivation**
  - A constant reminder that every day matters.
  - Encourages consistency and mindful use of time.

---

## 📱 Preview

**Year View** — all twelve months at once. Each month shows its **name** and a
small grid of dots beneath it, one dot per day:

```
Jan          Feb          Mar
● ● ● ● ● ● ●  ○ ○ ○ ○ ○ ○ ○  ○ ○ ○ ○ ○ ○ ○
● ● ● ● ● ● ●  ○ ○ ○ ○ ○ ○ ○  ○ ○ ○ ○ ○ ○ ○
● ● ● ◉ ○ ○ ○  ○ ○ ○ ○ ○ ○ ○  ○ ○ ○ ○ ○ ○ ○
...
                166d left · 55%
```

- **●** = Day completed (bright)
- **◉** = Today (accent color)
- **○** = Day remaining (dim)

The current month's name is highlighted, and a footer shows the days left and
percent of the year completed. The top of the screen is left clear for the
lock-screen clock.

### Fitting it to your screen

Lock-screen clocks and home-screen icons sit in different places on every OEM,
so the app lets you dial the grid in:

- **Size** and **Vertical position** sliders scale and move the grid so it
  clears your clock / notifications / icons.
- A **Lock / Home** toggle overlays a mock of the system UI (clock or icons +
  dock) on the live preview, so you can see exactly what you're avoiding while
  you adjust.
- A full **HSV colour picker** sets the accent used for "today" and the footer.

> ℹ️ The mock lock/home overlay is a *representative approximation*, not a pixel
> replica of your specific device — Android doesn't expose the exact clock
> geometry to wallpapers. Use it to get close, then confirm on the real lock
> screen after applying.

---

## 💡 Why Remainder?

Most calendars tell you **what day it is.**

Remainder shows you **how much of your year has already passed.**

Watching the dots slowly fill throughout the year provides a unique perspective on time, helping you stay focused on your goals and appreciate each day.

---

## 🚀 Future Features

- Multiple dot themes
- Custom color palettes
- AMOLED mode

---

## 🛠️ Built With

- Android (Kotlin)
- Live Wallpaper Service
- Canvas API
- Material Design 3

---

## 📦 Project Structure

```
Remainder/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/remainder/
│       │   ├── YearRenderer.kt          # draws month names + day-dot grid (shared)
│       │   ├── YearWallpaperService.kt  # the live wallpaper, redraws at midnight
│       │   ├── YearPreviewView.kt        # in-app preview + mock lock/home overlay
│       │   ├── ColorPickerView.kt         # HSV accent colour picker
│       │   ├── MainActivity.kt           # preview + size/position/colour controls
│       │   └── WallpaperConfig.kt         # persisted look (accent, scale, position)
│       └── res/                          # layout, colors, theme, launcher icon
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew                              # Gradle wrapper (8.7)
```

The wallpaper and the in-app preview render through the **same** `YearRenderer`,
so what you see in the app is exactly what lands on your lock screen.

---

## 🧰 Requirements

- **JDK 17** (Temurin/OpenJDK)
- **Android SDK** — Platform 34, Build-Tools 34.0.0
- **Gradle 8.7** (bundled via the `./gradlew` wrapper — no manual install needed)
- Android device or emulator running **Android 7.0 (API 24)** or newer

Point Gradle at your SDK either by setting `ANDROID_HOME`, or by creating a
`local.properties` file in the project root:

```properties
sdk.dir=/path/to/Android/Sdk
```

---

## 🔨 Build

The easiest path is to open the project in **Android Studio** (Hedgehog or
newer) and let it sync — then Run ▶. From the command line:

```bash
# Build a debug APK
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Clean build artifacts
./gradlew clean
```

On Windows use `gradlew.bat` instead of `./gradlew`.

### Release build (signed)

A release APK must be **signed** or Android will refuse to install it. Signing
credentials are read from a `keystore.properties` file in the project root
(kept out of version control):

```properties
storeFile=remainder-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=remainder
keyPassword=YOUR_KEY_PASSWORD
```

Create the keystore once with `keytool` (ships with the JDK):

```bash
keytool -genkeypair -v \
  -keystore remainder-release.keystore \
  -alias remainder \
  -keyalg RSA -keysize 2048 -validity 10000
# then fill remainder-release.keystore + the passwords into keystore.properties
```

Build the signed release APK:

```bash
./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

> ⚠️ Keep `remainder-release.keystore` and `keystore.properties` safe and
> **never commit them** (both are already in `.gitignore`). You must reuse the
> same keystore to ship updates. If Gradle finds no `keystore.properties`, the
> release task still runs but produces an **unsigned** APK.

Verify the signature:

```bash
$ANDROID_HOME/build-tools/34.0.0/apksigner verify --verbose \
  app/build/outputs/apk/release/app-release.apk
```

---

## ▶️ Run / Install

**Debug build:**

```bash
# Install onto a connected device or running emulator
./gradlew installDebug

# …or install the built APK manually
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Release build** (signed — see [Release build](#release-build-signed) above):

```bash
# Build + install the signed release in one step
./gradlew installRelease

# …or install the built APK manually
adb install -r app/build/outputs/apk/release/app-release.apk
```

> If you previously installed the **debug** build, uninstall it first
> (`adb uninstall com.remainder`) — debug and release are signed with different
> keys, so Android won't upgrade one over the other.

Then:

1. Open **Remainder** from the app drawer to see the live preview and pick an
   accent color.
2. Tap **Set as live wallpaper** — the system live-wallpaper preview opens.
3. Confirm to apply it to your home/lock screen.

The wallpaper advances the "today" dot automatically at local midnight, and
re-reads your chosen accent whenever it becomes visible again.

---

## ✅ Test

```bash
# Compile + lint checks (fast sanity pass)
./gradlew lint

# Unit tests (JVM)
./gradlew test

# Instrumented tests (needs a connected device/emulator)
./gradlew connectedAndroidTest
```

Because `YearRenderer` is pure drawing logic driven by a `Calendar`, you can
verify the visual layout for any date without a device — render it to a
`Bitmap`-backed `Canvas` and inspect the result:

```kotlin
val bmp = Bitmap.createBitmap(1080, 2340, Bitmap.Config.ARGB_8888)
val canvas = Canvas(bmp)
val date = Calendar.getInstance().apply { set(2026, Calendar.JULY, 18) }
YearRenderer().draw(canvas, bmp.width, bmp.height, WallpaperConfig(), date)
// assert / save bmp to eyeball the year view
```

---

## 📖 Philosophy

> We often think we have plenty of time.

Seeing each day disappear one dot at a time reminds us that time is our most valuable resource. Remainder isn't about creating pressure—it's about encouraging intentional living, one day at a time.

---

## 📄 License

This project is licensed under the MIT License.

---

**Every dot is a day. Every day counts.**
