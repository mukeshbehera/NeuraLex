# 🪐 **NeuraLex**

> AI-powered modern dictionary with Gen Z translations, contextual explanations, and custom speech engines.

---

## 📱 Interactive & Immersive Screens 

Since **NeuraLex** is styled in a deep Cosmic Dark Theme & Light Glassmorphism, here is how the primary application layouts are structured on your device:

### 🏠 1. The Welcome Screen
A premium, animated gateway designed to welcome new users with dynamic particle grids and cosmic typography.
```text
┌──────────────────────────────────────────────┐
│  (★) [AutoAwesome]               10:30 AM    │
│                                              │
│                   🪐                         │
│               NeuraLex                       │
│                                              │
│        "Built different dictionary           │
│         for the new internet age"            │
│                                              │
│          ┌────────────────────────┐          │
│          │    [ GET STARTED -> ]  │          │
│          └────────────────────────┘          │
│                                              │
│      Aesthetically Crafted with Compose      │
└──────────────────────────────────────────────┘
```
* **Core Characteristics**: Embedded dynamic `Canvas` with orbiting starry paths, visual state fade-ins, and glassmorphic quick action buttons.

### 🔍 2. The Hunt/Home Dashboard
The central command center for hunting down definitions, looking up words, and triggering voice queries.
```text
┌──────────────────────────────────────────────┐
│ [Menu]              NeuraLex          [Settings]│
├──────────────────────────────────────────────┤
│  ┌────────────────────────────────────────┐  │
│  │ 🔍 Search word...                  (🎙️) │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  🔥 QUICK ACTIONS                            │
│  ┌────────────┐ ┌────────────┐ ┌───────────┐ │
│  │ 🎲 Random  │ │ 📅 W.O.T.D  │ │ ⚡ Slang  │ │
│  └────────────┘ └────────────┘ └───────────┘ │
│                                              │
│  🕒 RECENT SEARCHES                           │
│  • Resilient     • Serendipity    • Euphoria │
└──────────────────────────────────────────────┘
```
* **Core Characteristics**: High-fidelity search field with continuous Speech-to-Text (`SpeechRecognizer`), word of the day cards, and list tracking.

### 📖 3. The Definition Detail Screen
A pristine definition viewer powered by Material Design 3 cards showing multi-layered dictionary metadata.
```text
┌──────────────────────────────────────────────┐
│ [<- Back]     Word Details       [♥ Favorite]│
├──────────────────────────────────────────────┤
│  ⚡ Resilient                                │
│  /rɪˈzɪl.jənt/  [adjective]           (🗣️ TTS)│
│                                              │
│  📖 MEANING                                  │
│  "Able to recoil or spring back into shape;  │
│   recovering quickly from hard situations"   │
│                                              │
│  ✨ GEN Z TRANSLATION                         │
│  ┌────────────────────────────────────────┐  │
│  │ "Tbh she's built different, literally  │  │
│  │  unmatched bounce back energy fr ⚡"  │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```
* **Core Characteristics**: Complete Text-To-Speech audio pronunciation engine, quick bookmark modifiers, tag-styled synonym list layout, and one-tap copy/share functionality.

---

## ⚡ Key Best Features

### 1. 🤖 Instant LLM Definitions & Gen Z Slang Translations
* Deep analytical breakdown of every lookup query.
* Translates traditional, dense textbook definitions into dynamic Gen Z colloquialisms with relevant humor and modern internet culture references.

### 2. 🎙️ Continuous Voice Search Activation
* Powered by Google's native speech recognition pipelines for fast, hands-free query creation.
* Complete with a voice recognition overlay dialog prompting search confirmation before lookup navigation.

### 3. 💾 Robust Local Storage & Cache
* Integration of offline fallbacks ensuring continuous operability.
* Word bookmarks, custom wordlists, history management, and favorite toggles persisting seamlessly through Room integration.

---

## 🧪 Testing the Best Features

NeuraLex includes comprehensive verification testing suites running locally on the JVM via Robolectric and screenshot visual coverage using Roborazzi.

To run and verify the best parts of the application:

### 1. Run Unit & Core App Tests
Execute the local suite from your launcher terminal to verify state mutations, quick word validation mechanisms, and database interactions:
```bash
gradle :app:testDebugUnitTest
```

### 2. Capture and Verify Visual Designs (Roborazzi)
Our custom screenshot assertion library captures visual state screens to verify color layout density and edge-to-edge component rendering:
* **To assert against current visual layouts**:
  ```bash
  gradle :app:verifyRoborazziDebug
  ```
* **To capture new baseline screenshots when tweaking color schemes**:
  ```bash
  gradle :app:recordRoborazziDebug
  ```

---

## 📦 GitHub Actions CI/CD Pipeline

Never compile manually. NeuraLex includes an automated GitHub workflow that compiles, signs, and yields build packages automatically.

### 📥 Downloading Your APK
1. Open the **Actions** tab in your GitHub repository.
2. Select the recent run marked **Android APK Build**.
3. Scroll down to the **Artifacts** module footer.
4. Click and download the **NeuraLex-apk** zip package.

---

## 💻 Local Setup & Development Guide

Follow these steps to set up NeuraLex on your local development machine:

### 📋 Prerequisites
Before you begin, ensure you have the following installed on your PC:
* **Java Development Kit (JDK)**: **JDK 17** is recommended.
* **Android Studio**: Android Studio (Ladybug or newer) is highly recommended for coding, interactive testing, and emulator support.
* **Gradle**: Handled automatically via standard Gradle wrapper inside Android Studio.

### 🛠️ Step 1: Clone and Open
1. **Clone the Repository**:
   ```bash
   git clone <your-repository-url>
   cd <your-project-directory>
   ```
2. **Open in Android Studio**:
   * Open Android Studio.
   * Click **File > Open...** and navigate to your cloned repository folder.
   * Android Studio will automatically resolve the dependencies and sync the project using Gradle.

### 🚀 Step 2: Running the Application
You can run the application on your physical device or a virtual device (emulator).

#### Run on a Device / Emulator from Android Studio
1. Select your target device (physical device connected via USB/Wi-Fi or a virtual emulator) from the device selector drop-down.
2. Click the green **Run** button (`Shift + F10`) to compile, install, and launch NeuraLex on your device.

#### Run on a Device from Command Line
Ensure your device/emulator is connected and recognized (verify via standard Android SDK utilities). Run:
```bash
./gradlew installDebug
```

---

## 📦 How to Build the APK Locally

If you wish to build the debugging or production-ready APK on your local machine, we have built-in Gradle configurations:

### ⚙️ 1. Build a Debug APK
Best for rapid local installation and manual testing. Run:
```bash
./gradlew assembleDebug
```
* **Output Path**: Your compiled APK will be located inside:
  `app/build/outputs/apk/debug/app-debug.apk`

### 🔒 2. Build a Release APK (Signed or Unsigned)
For production builds, compile via:
```bash
./gradlew assembleRelease
```
* **Output Path**: Your compiled Release APK or App Bundle will be located inside:
  `app/build/outputs/apk/release/`
