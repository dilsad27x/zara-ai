# Core Layer - ZARA AI

The Core layer contains domain-agnostic and platform-level infrastructure for ZARA AI.

## Modules & Packages

- **`common/`**: Coroutine dispatchers (`ZaraDispatchers`), Result wrappers (`ZaraResult`), centralized errors (`ZaraError`), error bus (`ErrorHandler`), structured logging (`ZaraLogger`), and application lifecycle observer (`AppLifecycleManager`).
  - Source: [app/src/main/java/com/zara/ai/core/common/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/core/common/)
- **`database/`**: Room Database (`ZaraDatabase`), entities (`ConversationEntity`, `MessageEntity`, `MemoryEntity`, `AgentTaskEntity`), DAOs, type converters, and Hilt injection module.
  - Source: [app/src/main/java/com/zara/ai/core/database/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/core/database/)
- **`network/`**: Flow-based network connectivity monitoring (`NetworkConnectivityObserver`), API models, and safe request client (`ZaraNetworkClient`).
  - Source: [app/src/main/java/com/zara/ai/core/network/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/core/network/)
- **`security/`**: Hardware-backed Android Keystore integration, AES-256 GCM encryption/decryption, encrypted settings (`SecurityManager`), and biometric authentication (`BiometricAuthManager`).
  - Source: [app/src/main/java/com/zara/ai/core/security/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/core/security/)
- **`designsystem/`**: Jetpack Compose Material 3 theme (`ZaraTheme`), Cyber/Neon palettes, typography, shapes, DataStore theme management (`ThemeManager`), and reusable UI components (`ZaraButton`, `ZaraCard`, `ZaraTextField`, `ZaraAppBar`, `ZaraStatusBadge`, `ZaraLoadingIndicator`, `ZaraEmptyState`, `ZaraGlassmorphicSurface`, `ZaraModalSheet`).
  - Source: [app/src/main/java/com/zara/ai/core/designsystem/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/core/designsystem/)
- **`permissions/`**: Android runtime permission management, status checking, and rationale dialogs.
  - Source: [app/src/main/java/com/zara/ai/core/permissions/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/core/permissions/)
