package com.zara.ai.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraTextPrimary

private sealed interface ContentSegment {
    data class TextSegment(val text: String) : ContentSegment
    data class CodeBlockSegment(val code: String, val language: String?) : ContentSegment
}

/**
 * Lightweight, high-performance Markdown & code block parser for ZARA conversation responses.
 */
@Composable
fun ZaraMarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = ZaraTextPrimary
) {
    val segments = parseContent(content)

    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is ContentSegment.TextSegment -> {
                    if (segment.text.isNotBlank()) {
                        FormattedInlineMarkdown(
                            text = segment.text.trim(),
                            textColor = textColor
                        )
                    }
                }
                is ContentSegment.CodeBlockSegment -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    ZaraCodeBlock(
                        code = segment.code,
                        language = segment.language
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun parseContent(raw: String): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    val fenceRegex = Regex("```([a-zA-Z0-9_-]*)\\n?([\\s\\S]*?)```")

    var lastIndex = 0
    val matches = fenceRegex.findAll(raw)

    for (match in matches) {
        if (match.range.first > lastIndex) {
            val precedingText = raw.substring(lastIndex, match.range.first)
            if (precedingText.isNotEmpty()) {
                segments.add(ContentSegment.TextSegment(precedingText))
            }
        }

        val lang = match.groupValues[1].ifBlank { null }
        val code = match.groupValues[2].trimEnd()
        segments.add(ContentSegment.CodeBlockSegment(code, lang))

        lastIndex = match.range.last + 1
    }

    if (lastIndex < raw.length) {
        val remainingText = raw.substring(lastIndex)
        if (remainingText.isNotEmpty()) {
            segments.add(ContentSegment.TextSegment(remainingText))
        }
    }

    return segments
}

@Composable
private fun FormattedInlineMarkdown(
    text: String,
    textColor: Color
) {
    val lines = text.split("\n")

    Column {
        lines.forEach { line ->
            val annotated = buildAnnotatedString {
                var current = line

                // Check for Header
                val isHeader = current.startsWith("# ") || current.startsWith("## ") || current.startsWith("### ")
                val cleanLine = current.removePrefix("### ").removePrefix("## ").removePrefix("# ")

                // Format inline code `code`
                val parts = cleanLine.split("`")
                parts.forEachIndexed { i, part ->
                    if (i % 2 == 1) { // Inside backticks
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = ZaraElectricBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(part)
                        }
                    } else {
                        // Handle bold **text**
                        val boldParts = part.split("**")
                        boldParts.forEachIndexed { j, bPart ->
                            if (j % 2 == 1) {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                                    append(bPart)
                                }
                            } else {
                                append(bPart)
                            }
                        }
                    }
                }
            }

            Text(
                text = annotated,
                style = if (line.startsWith("#")) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                fontWeight = if (line.startsWith("#")) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
