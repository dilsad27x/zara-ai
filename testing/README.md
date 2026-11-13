# Testing Infrastructure - ZARA AI

Provides fakes, test factories, and coroutine test rules for unit and integration testing.

## Components

- **`TestDataFactory.kt`**: Factory for producing test entities (`ConversationEntity`, `MessageEntity`, `MemoryEntity`, `AgentTaskEntity`).
- **`FakeZaraRepository.kt`**: In-memory fake repository simulating Room database operations without SQLite dependencies.
- **`ZaraTestDispatcherRule.kt`**: Coroutine test rule managing `StandardTestDispatcher` and `TestScope` for predictable async testing.

Implementation located under [app/src/main/java/com/zara/ai/testing/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/main/java/com/zara/ai/testing/) and [app/src/test/java/com/zara/ai/](file:///data/user/0/com.termux/files/home/.gemini/antigravity-cli/scratch/zara-ai/app/src/test/java/com/zara/ai/).
