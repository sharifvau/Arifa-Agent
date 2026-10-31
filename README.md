# ARIFA AI — Multilingual Personal AI Assistant for Android

> **"A Pearl of Light in the Night"**  
> An advanced, voice-first personal AI companion for Android, inspired by futuristic systems like JARVIS.

---

## 🌟 Executive Summary

**ARIFA AI** (`com.arifa.assistant`) is a production-quality Android application built with **Kotlin**, **Jetpack Compose**, and **Material 3**, powered by Google's **Gemini Live Multimodal API** and **Gemini REST API**.

Arifa communicates naturally in **three languages**:
1. **বাংলা (Bangla)**: Fluent, everyday conversational Bengali with authentic Bangladeshi expressions.
2. **Banglish**: Bengali phonetically written using the Latin/English alphabet (*e.g., "kemon acho", "ki khobor", "amar jonno ekta website banao"*).
3. **English**: Clear, modern, articulate English.

---

## 💎 Key Capabilities & Highlights

| Feature | Description |
| :--- | :--- |
| **Full-Duplex Voice AI** | WebSocket integration with Gemini Live API (`models/gemini-2.0-flash-exp`) streaming 16kHz PCM audio up and 24kHz PCM audio down. |
| **Instant Barge-In** | Interrupt Arifa mid-speech; AudioPlayer flushes buffers instantly and restarts listening. |
| **EchoGuard & Hardware FX** | Acoustic Echo Canceler (AEC), Noise Suppressor (NS), Automatic Gain Control (AGC) & software feedback attenuation. |
| **Ten Selectable Personalities** | Normal, Friendly, Professional, Funny, Companion, GF Mode, Teacher, Developer, Hungry, and Nautanki — immediate switching. |
| **Ethical Safeguards** | Companion and GF Mode strictly declare their artificial nature, never claim a biological body, and respect boundaries. |
| **Phone Control** | Open installed apps, search apps, dial contacts, prepare SMS (`ACTION_SENDTO`), open WhatsApp chats, and launch system settings. |
| **Website Forge 3D** | AI-powered 3D website builder generating semantic HTML5, modern CSS3, and embedded **Three.js 3D canvas** scenes. |
| **GPU-Powered 3D Orb** | 7 selectable visual themes (Pearl Classic, Neon Cyber, Solar Flare, Quantum Lotus, Celestial Aurora, Dark Matter, Bio-Pulse) with touch-drag and accelerometer physics. |
| **Dual Build Flavors** | **Play Build** (100% Google Play policy compliant) and **Direct Build** (with AccessibilityService and Social Media Visual Automation). |

---

## 🏗️ Architecture & Flavor Separation

```
ArifaAI/
├── app/
│   ├── src/main/       # Common Code: Voice engine, Chat, 3D Orb, Website Forge, UI
│   ├── src/play/       # Play Store Flavor: Safe stubs, zero prohibited automation
│   ├── src/direct/     # Direct Sideload Flavor: AccessibilityService, Social Agent, Licensing
│   └── src/test/       # Pure logic unit tests (Language, Personalities, VAD, Forge, JSON)
```

### Build Flavor Matrix

| Capability | Google Play Flavor (`play`) | Direct Sideload Flavor (`direct`) |
| :--- | :---: | :---: |
| Application ID | `com.arifa.assistant.play` | `com.arifa.assistant.direct` |
| Voice AI & Chat | ✅ Included | ✅ Included |
| Website Forge 3D | ✅ Included | ✅ Included |
| Safe Phone Actions | ✅ Included | ✅ Included |
| Accessibility Service | ❌ Completely Excluded at Compile Time | ✅ Included (`ArifaAccessibilityService`) |
| Social Visual Agent | ❌ Completely Excluded at Compile Time | ✅ Included (`SocialMediaVisualAgent`) |
| Licensing Code Paths | ❌ Unrestricted Play Experience | ✅ Included (`AccessKeyManager`) |

---

## 🛠️ Build Commands

Ensure you have Android SDK 36 and Java 17 configured.

### Build APKs
```bash
# Debug builds
./gradlew assemblePlayDebug
./gradlew assembleDirectDebug

# Release APKs
./gradlew assemblePlayRelease
./gradlew assembleDirectRelease
```

### Build Google Play Bundle (.aab)
```bash
./gradlew bundlePlayRelease
```

### Run Unit Tests
```bash
./gradlew test
```

---

## 📱 Installation Instructions

### Method 1: ADB Direct Install
```bash
# Install Play flavor debug build
adb install app/build/outputs/apk/play/debug/app-play-debug.apk

# Install Direct flavor debug build
adb install app/build/outputs/apk/direct/debug/app-direct-debug.apk
```

### Method 2: Android Studio
1. Open Android Studio → **File → Open** → Select `/data/user/0/com.termux/files/home/projects/ArifaAI`.
2. Sync Project with Gradle Files.
3. Select Active Build Variant in **Build Variants** panel (`playDebug` or `directDebug`).
4. Click **Run** on your physical device or emulator.

---

## 🧪 Real-Device Testing Checklist

| Test Item | Verification Procedure | Expected Outcome |
| :--- | :--- | :--- |
| **1. Gemini API Key** | Navigate to Settings → enter valid Gemini API key. | Saved in local DataStore/Prefs; verified by connection indicator. |
| **2. Live Voice Streaming** | Tap Center Talk Orb in navigation or Home. Speak into mic in Bangla (*"কেমন আছো আরিফা?"*). | Waveform moves; Arifa replies with low latency in warm Bangla audio. |
| **3. Instant Barge-In** | While Arifa is speaking, say *"Arifa, stop"* or tap the Interrupt button. | Audio playback flushes immediately; returns to `LISTENING`. |
| **4. Echo Guard Test** | Run voice session on loudspeaker without headphones. | Assistant audio from speaker does not trigger feedback loop into mic. |
| **5. Personality Shift** | Switch personality from Friendly to Hungry, ask *"Ki khobor?"*. | Responds with authentic food cravings (*"Kacchi Biryani", "Fuchka"*). |
| **6. Banglish Detection** | Type or speak: *"Amar jonno ekta restaurant er website banao"*. | Auto-detects Banglish; triggers Website Forge. |
| **7. Website Forge 3D** | Click Forge Website Now. | Progress steps animate; renders responsive website with interactive Three.js 3D canvas in WebView. |
| **8. Phone Dial & App** | Speak *"Open YouTube"* or *"Call Mom"*. | Dispatches intent; launches YouTube or opens dialer. |
| **9. Direct Accessibility** | (*Direct Flavor only*) Enable Arifa Accessibility Service in Android Settings. | Inspects UI nodes; automates post preparation; aborts if security/login screen is detected. |

---

## 📜 Google Play Release Documentation

### Policy Compliance
1. **No Accessibility Automation in Play Build**: The `play` source set contains zero references to `android.accessibilityservice.AccessibilityService` or `performAction()`.
2. **Safe Messaging**: No `SEND_SMS` permission is requested. Messages are prepared safely via `Intent.ACTION_SENDTO` with `smsto:` URI so the user maintains full control and confirmation in their default SMS app.
3. **Data Safety**: Audio bytes and Gemini API keys are processed in real-time memory and local private storage (`context.filesDir`). No audio files are logged or transmitted to unauthorized third parties.

---

## 📋 Hardware Verification & Notes

- **Acoustic Echo Canceler (AEC)**: Relies on device hardware HAL support (`AcousticEchoCanceler.isAvailable()`). When hardware AEC is absent on budget hardware, Arifa's software `EchoGuard` filters feedback automatically.
- **Three.js WebGL in WebView**: Requires GPU hardware acceleration (`android:hardwareAccelerated="true"` in AndroidManifest).
- **Telephony Calling**: Direct call execution (`Intent.ACTION_CALL`) requires an active physical SIM card with cellular carrier connection; otherwise falls back to `Intent.ACTION_DIAL`.

---

## 🏆 Project Specifications Met

- Package: `com.arifa.assistant`
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Min SDK: 26 (Android 8.0)
- Target SDK: 36 (Android 16)
- Networking & JSON: OkHttp & `org.json` ONLY
- Dev Parts: 01 through 15 completely fulfilled with zero TODO shortcuts!
