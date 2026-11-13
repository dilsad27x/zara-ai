# ZARA AI — Native Android Foundation & Premium Interface

ZARA AI is a native Android intelligence assistant built with modern Android development standards, Jetpack Compose, Material 3, and reactive clean architecture.

---

## ⚡ Technology Stack

- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose (BOM 2024.06.00)
- **Design System**: Material Design 3 (Part 02 Cyber/Midnight palette: `#080810`, `#10101C`, `#171727`, `#8B5CF6`, `#6366F1`, `#3B82F6`)
- **Central Focus**: Multi-layered Animated AI Orb (`ZaraAiOrb`) supporting 8 real operational states
- **Floating Overlay**: Draggable Mini Orb, Mini Chat, and Expanded Control Panel (`FloatingAssistant`)
- **Dependency Injection**: Dagger Hilt 2.51.1
- **Persistence**: Room Database 2.6.1 (with KSP)
- **Preferences**: Jetpack DataStore Preferences
- **Security**: Android Keystore + AES-256 GCM Encryption + AndroidX Biometric
- **Navigation**: Navigation Compose 2.8.0
- **Asynchrony**: Kotlin Coroutines & Flow
- **Build System**: Gradle 8.9 + Kotlin DSL + Version Catalog (`libs.versions.toml`)

---

## 📱 Part 02 Screen Architecture

| Screen | File Location | Key Responsibilities |
| :--- | :--- | :--- |
| **A. Splash Screen** | `features/splash/SplashScreen.kt` | Animated ZARA logo, purple/blue ambient lighting, real subsystem readiness verification, automatic navigation |
| **B. Main AI Home Screen** | `features/home/HomeScreen.kt` | User greeting, **Central Animated AI Orb (`ZaraAiOrb`)**, prompt input, active task cards, quick actions, connection status |
| **C. Conversation Screen** | `features/chat/ChatScreen.kt` | Markdown rendering, syntax highlighted code blocks with copy action, real-time token streaming with Stop Generation, session switcher |
| **D. Voice Assistant Screen** | `features/voice/VoiceScreen.kt` | Full-screen voice assistant, large reactive animated orb, live user and assistant transcription, Mute, Interrupt, End controls |
| **E. Device Control Dashboard** | `features/devicecontrol/DeviceControlScreen.kt` | Accessibility service status, notification access, screen capture status, hardware telemetry, volume slider, app launcher |
| **F. Social Agent Dashboard** | `features/social/SocialScreen.kt` | Telegram, WhatsApp, Instagram, Messenger channels, pending message drafts, approval requests, auto-reply, **Emergency Stop Killswitch** |
| **G. Memory Dashboard** | `features/memory/MemoryScreen.kt` | Short-term context tokens, long-term facts, saved preferences, dynamic search query, custom memory creation, clear context |
| **H. Active Tasks Screen** | `features/tasks/TasksScreen.kt` | Autonomous agent task monitoring, progress tracking, subtasks, Pause, Resume, Cancel, and Delete controls |
| **I. Permission Center** | `features/permissions/PermissionCenterScreen.kt` | Runtime and special access permission management, clear explanations, direct intent shortcuts to Android system settings |
| **J. Settings Screen** | `features/settings/SettingsScreen.kt` | 13 comprehensive categories: Appearance, Themes, Language, Voice models, AI Provider, Keystore Master Key, Privacy, Notifications, Floating Orb |
| **Floating Assistant** | `features/floating/FloatingAssistant.kt` | Draggable Mini Orb, Mini Chat, and Expanded Control Panel with window controls (drag, minimize, expand, collapse, close) |

---

## 🎨 Visual Design System

- **Primary Background**: `#080810`
- **Secondary Background**: `#10101C`
- **Surface**: `#171727`
- **Surface Variant**: `#1F1F35`
- **Primary Accent**: `#8B5CF6` (Vibrant Purple)
- **Secondary Accent**: `#6366F1` (Radiant Indigo)
- **Electric Blue**: `#3B82F6`
- **Text Primary**: `#FFFFFF`
- **Text Secondary**: `#A1A1B5`
- **Success**: `#22C55E`
- **Error**: `#EF4444`

---

## 🚀 Building & Testing the Project

### Via Automated GitHub Actions CI
Push this repository to GitHub; the automated workflow at `.github/workflows/build.yml` compiles `app-debug.apk` and executes unit tests on Ubuntu with JDK 17 without requiring Android Studio on your phone.

### Local Command-Line Build
```bash
# Navigate to project
cd /data/user/0/com.termux/files/home/projects/zara-ai

# Build the Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest
```
