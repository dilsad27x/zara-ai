# Features Layer - ZARA AI (Part 02)

Contains user-facing feature modules and screen implementations, powered by Navigation Compose, Hilt ViewModels, Room Database, and the Part 02 Premium Design System.

## Screens & Feature Domains

1. **`splash/` (Splash Screen)**:
   - Animated ZARA geometric monogram with ambient purple and electric blue lighting.
   - Real subsystem verification (Database, DataStore, Lifecycle, Keystore).
   - Seamless transition to Home on ready.

2. **`home/` (Main AI Home Screen)**:
   - Contextual user greeting based on time of day.
   - **Central Animated AI Orb (`ZaraAiOrb`)** as the visual focus.
   - Live network connection status pill & AI provider status.
   - Text input bar with send and voice activation controls.
   - Active task indicator cards with real-time progress bars.
   - Quick action shortcuts matrix and recent conversation session history.

3. **`chat/` (Conversation Screen)**:
   - User message bubbles (vibrant accent gradient, timestamp, copy).
   - ZARA response bubbles (glassmorphic container, Markdown rendering, code blocks).
   - Real-time token streaming with Stop Generation control.
   - Full code block renderer with language badge, monospace font, and one-tap copy.
   - Session history switcher and new session creator.
   - Auto-scroll to latest message and scroll-to-bottom FAB.
   - Offline notification banner.

4. **`voice/` (Voice Assistant Screen)**:
   - Full-screen voice assistant featuring large centerpiece animated AI orb.
   - State cycle: `LISTENING`, `PROCESSING`, `SPEAKING`, `IDLE`.
   - Live transcription card displaying user speech and ZARA response.
   - Controls: Mute toggle, Push-to-Talk trigger, Interrupt assistant, End call.
   - Microphone permission validation.

5. **`devicecontrol/` (Device Control Dashboard)**:
   - System privileges: Accessibility Service, Notification Interceptor, Overlay, Screen Capture.
   - Real hardware telemetry (Model, OS, battery, RAM).
   - Media volume stream slider connected to Android `AudioManager`.
   - Application launch controls for installed apps.
   - Execution history from `AgentTaskDao`.

6. **`social/` (Social Agent Dashboard)**:
   - Multi-platform integration cards (Telegram, WhatsApp, Instagram, Messenger).
   - Connected account status and polling webhook indicators.
   - Pending message drafts with approve / discard controls.
   - Autonomous approval requests with risk ratings.
   - Auto-reply toggle per channel.
   - **Emergency Stop Master Killswitch** to halt all automated outbound activity.

7. **`memory/` (Memory Dashboard)**:
   - Memory architecture vitals (Short-term tokens, Long-term facts, User preferences).
   - Category filtering (`FACT`, `PREFERENCE`, `RULE`, `SYSTEM_CAPABILITY`).
   - Dynamic search query filter over neural memory entries.
   - Autonomous memory extraction setting toggle.
   - Clean short-term conversation context action.
   - Custom memory creation modal sheet.

8. **`tasks/` (Active Tasks Screen)**:
   - Autonomous agent task monitoring with real task states (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`).
   - Real progress bars and subtask results.
   - Controls: Pause, Resume, Cancel, and Delete task.
   - Autonomous task creator sheet to launch new goals.

9. **`permissions/` (Permission Center)**:
   - Centralized management for all runtime and special permissions.
   - Detailed rationale explanations for each privilege.
   - Direct intent launcher to specific Android system settings pages.
   - Auto-refresh lifecycle observer upon returning to the app.

10. **`settings/` (Settings Screen)**:
    - Complete settings engine across all 13 categories:
      1. Appearance (Theme mode, AMOLED pure black, Dynamic Color)
      2. Language selection
      3. Voice synthesis models
      4. AI provider & Hardware Keystore master API key
      5. Privacy & biometric settings lock
      6. Floating assistant toggles
      7. Notification preferences
      8. Permission Center shortcut
      9. About ZARA AI & build telemetry

11. **`floating/` (Floating Assistant)**:
    - Draggable floating window assistant.
    - Mini Orb (compact, draggable, status glow).
    - Mini Chat (compact overlay conversation window).
    - Expanded Control Panel (voice trigger, active task status, pause/stop).
    - Window controls: Drag, Minimize, Expand, Collapse, Close.
