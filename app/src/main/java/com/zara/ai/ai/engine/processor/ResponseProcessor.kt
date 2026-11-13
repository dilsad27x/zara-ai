package com.zara.ai.ai.engine.processor

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseProcessor @Inject constructor() {

    private val secretPattern = Regex("""(?i)(AIzaSy[a-zA-Z0-9_-]{33}|key=[a-zA-Z0-9_-]{20,})""")

    /**
     * Sanitizes output text, normalizes whitespace and redacts potential API tokens.
     */
    fun process(rawText: String): String {
        if (rawText.isBlank()) return ""

        // Redact any leaked API keys or credentials
        val redacted = secretPattern.replace(rawText, "[REDACTED_SECRET]")

        // Normalize trailing linebreaks
        return redacted.trimEnd()
    }

    /**
     * Extracts all fenced code blocks from markdown text.
     */
    fun extractCodeBlocks(markdown: String): List<Pair<String, String>> {
        val pattern = Regex("```([a-zA-Z0-9_-]*)\n([\\s\\S]*?)```")
        return pattern.findAll(markdown).map { match ->
            val lang = match.groupValues[1].ifBlank { "text" }
            val code = match.groupValues[2].trimEnd()
            Pair(lang, code)
        }.toList()
    }
}
