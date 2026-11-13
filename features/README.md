# Features Layer - ZARA AI

Contains user-facing feature modules and screen implementations, powered by Navigation Compose, Hilt ViewModels, and Room Database.

## Features Directory

- **`home/`**: Main overview, device vitals overview, quick launch tiles, recent conversation history, and quick prompts.
- **`chat/`**: Conversational AI UI with real-time streaming simulation, Room message persistence, voice trigger, and modern chat bubbles.
- **`voice/`**: Voice assistant mode featuring an AI glowing orb, state transitions (Listening, Processing, Speaking), and permission handling.
- **`memory/`**: Neural knowledge base inspector, categorizing knowledge into Facts, Preferences, Rules, and System Capabilities with custom addition and deletion.
- **`dashboard/`**: System diagnostic cockpit, showing runtime vitals, autonomous agent execution trigger, and structured live log viewer.
- **`settings/`**: Customization center with DataStore theme switches, Dynamic Color toggles, Android Keystore API key encryption, and build variant metadata.
- **`devicecontrol/`**: Android hardware controls, system volume adjustment, hardware diagnostics, and clipboard bridge.
- **`social/`**: Multi-channel social sync and bot status controller.
- **`websiteforge/`**: Autonomous web application generator and live preview renderer.

All features are implemented under [app/src/main/java/com/zara/ai/features/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/features/).
