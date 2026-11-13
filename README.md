# ZARA AI - Native Android Foundation

ZARA AI is a native Android intelligence foundation built with modern Android development standards, reactive clean architecture, and modular layer separation.

---

## ⚡ Technology Stack

- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose (BOM 2024.06.00)
- **Design System**: Material Design 3 (Dynamic Color, Amoled Dark, Cyber Neon palettes)
- **Dependency Injection**: Dagger Hilt 2.51.1
- **Persistence**: Room Database 2.6.1 (with KSP)
- **Preferences**: Jetpack DataStore Preferences
- **Security**: Android Keystore + AES-256 GCM Encryption + AndroidX Biometric
- **Navigation**: Navigation Compose 2.8.0
- **Asynchrony**: Kotlin Coroutines & Flow
- **Build System**: Gradle 8.9 + Kotlin DSL + Gradle Version Catalog (`libs.versions.toml`)

---

## 📂 Architecture & Folder Structure

```
zara-ai/
├── app/                      # Application module (Manifest, Build configs, App entry)
│   ├── src/main/java/com/zara/ai/
│   │   ├── ZaraApplication.kt # @HiltAndroidApp, global exception boundary, logger
│   │   ├── MainActivity.kt    # @AndroidEntryPoint, Edge-to-Edge, ZaraTheme
│   │   ├── core/              # Foundational domain-agnostic components
│   │   │   ├── common/        # Dispatchers, ZaraResult, ZaraError, ErrorHandler, ZaraLogger
│   │   │   ├── database/      # ZaraDatabase, Entities, DAOs, Type Converters, Hilt Module
│   │   │   ├── network/       # NetworkConnectivityObserver, NetworkClient, Api Models
│   │   │   ├── security/      # Android Keystore, AES-256 GCM, BiometricAuthManager
│   │   │   ├── designsystem/  # ZaraTheme, Color, Typography, Shape, ThemeManager, Reusable UI
│   │   │   └── permissions/   # PermissionManager, Runtime Rationale Dialogs
│   │   ├── features/          # Feature domains and composable screens
│   │   │   ├── navigation/    # ZaraDestination, ZaraNavHost, ZaraBottomBar
│   │   │   ├── home/          # HomeScreen, HomeViewModel (Device vitals, quick tiles)
│   │   │   ├── chat/          # ChatScreen, ChatViewModel (Room persistence, streaming)
│   │   │   ├── voice/         # VoiceScreen, VoiceViewModel (AI Glowing Orb, speech loop)
│   │   │   ├── memory/        # MemoryScreen, MemoryViewModel (Neural knowledge graph)
│   │   │   ├── dashboard/     # DashboardScreen, DashboardViewModel (Vitals & Live Log Viewer)
│   │   │   ├── settings/      # SettingsScreen, SettingsViewModel (DataStore theme & security)
│   │   │   ├── devicecontrol/ # DeviceControlScreen, DeviceControlViewModel (Hardware bridge)
│   │   │   ├── social/        # SocialScreen, SocialViewModel (Multi-channel integrations)
│   │   │   └── websiteforge/  # WebsiteForgeScreen, WebsiteForgeViewModel (Autonomous site builder)
│   │   ├── ai/                # AI reasoning & system bridge
│   │   │   ├── agent/         # ZaraAgentEngine, AgentStep, state machine, task execution
│   │   │   ├── android/       # AndroidSystemBridge, DeviceMetrics, hardware telemetry
│   │   │   └── integrations/  # IntegrationRegistry, ToolDefinition
│   │   └── testing/           # TestDataFactory, FakeZaraRepository, ZaraTestDispatcherRule
│   └── src/test/java/com/zara/ai/ # Unit tests (ZaraResultTest, ZaraLoggerTest)
├── core/                      # Root Core layer specifications & documentation
├── features/                  # Root Features layer specifications & documentation
├── ai/                        # Root AI layer specifications & documentation
├── testing/                   # Root Testing specifications & documentation
├── gradle/
│   ├── libs.versions.toml     # Gradle Version Catalog
│   └── wrapper/               # Gradle wrapper binaries & properties
├── build.gradle.kts           # Root Gradle build script
├── settings.gradle.kts        # Root Gradle settings
├── gradle.properties          # JVM & AndroidX optimization flags
└── gradlew                    # Executable Gradle wrapper
```

---

## 🎨 Theme & Design System

The application features a modern cyber-intelligence visual aesthetic:
- **Neon Cyan Accent (`#00E5FF`)**: Represents active cognitive intelligence.
- **Radiant Purple (`#7C4DFF`)**: Represents deep reasoning and neural memories.
- **Emerald Accent (`#00E676`)**: Operational readiness and healthy telemetry.
- **Glassmorphic Surfaces (`ZaraGlassmorphicSurface`)**: Translucent backdrops with gradient borders.
- **Dynamic Theming (`ThemeManager`)**: DataStore-backed preference engine supporting `LIGHT`, `DARK`, `SYSTEM`, and dynamic wallpaper colors (Android 12+).

---

## 🔒 Security Architecture

- **Hardware Keystore**: Keys are generated and stored in the secure hardware-backed Android Keystore enclave using `KeyGenParameterSpec` with AES-256 in GCM mode with no padding.
- **Data Protection**: EncryptedSharedPreferences with AES-256 SIV for key names and AES-256 GCM for values prevents unauthorized exposure of stored AI credentials and tokens.
- **Biometric Authentication**: `BiometricAuthManager` supports strong biometric verification and device credentials prior to executing high-privilege autonomous actions or accessing sensitive keys.

---

## 🛠️ Build Variants

- **`debug`**:
  - `applicationId`: `com.zara.ai.debug`
  - `versionNameSuffix`: `-debug`
  - `LOGGING_ENABLED`: `true`
  - `IS_DEV_MODE`: `true`
  - `BASE_URL`: `https://api.dev.zara.ai/v1/`
- **`release`**:
  - `applicationId`: `com.zara.ai`
  - ProGuard / R8 code shrinking and resource optimization enabled.
  - `LOGGING_ENABLED`: `false`
  - `IS_DEV_MODE`: `false`
  - `BASE_URL`: `https://api.zara.ai/v1/`

---

## 🚀 Building & Deploying the Project

### In Android Studio
1. Open Android Studio and select **File → Open**.
2. Select the directory:
   `/data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai`
3. Allow Gradle sync to download dependencies and build the model.
4. Select target device and press **Run (Shift + F10)**.

### Via Command-Line
```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest

# Install on connected device via ADB
./gradlew installDebug
```
