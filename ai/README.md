# AI & Autonomous Agent Architecture - ZARA AI

Defines the autonomous reasoning engine, device bridge, and tool integration registry for ZARA AI.

## Modules

- **`agent/`**: The core autonomous agent engine (`ZaraAgentEngine`), multi-step planner, state transitions (`IDLE`, `PLANNING`, `EXECUTING`, `EVALUATING`, `COMPLETED`), and Room task persistence.
- **`android/`**: Android hardware bridge (`AndroidSystemBridge`) connecting ZARA AI to device sensors, battery, memory, volume, and clipboard.
- **`integrations/`**: Dynamic AI tool registry (`IntegrationRegistry`) enabling ZARA to invoke system utilities and custom extensions.

Implementation located under [app/src/main/java/com/zara/ai/ai/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/ai/).
