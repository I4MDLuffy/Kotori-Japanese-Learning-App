package app.kotori.japanese.master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.ScreenHelpDialog

private data class MasterSection(
    val emoji: String,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

@Composable
fun MasterScreen(
    onBack: () -> Unit,
    onLevelClick: (String) -> Unit,
    onReadingComprehension: () -> Unit,
    onDialogues: () -> Unit,
) {
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Master",
            description = "Native-level Japanese study at N2–N1.\n\n" +
                "• JLPT N1 — The highest level. Classical forms, advanced conjunctions, rare kanji, and idiomatic expressions used in native media.\n" +
                "• JLPT N2 — Broad vocabulary, complex grammar, and nuanced usage found in newspapers and formal writing.\n" +
                "• Reading Comprehension — Long-form passages at N2–N1 difficulty. Practice sustained reading under JLPT conditions.\n" +
                "• Dialogues — Advanced conversations covering reservations, interests, and complex social situations.",
            onDismiss = { showHelp = false },
        )
    }

    val sections = listOf(
        MasterSection(
            "⛩", "JLPT N1 — Advanced",
            "The highest JLPT level. Classical forms, advanced conjunctions, rare kanji, and idiomatic expressions used in native media.",
            { onLevelClick("N1") },
        ),
        MasterSection(
            "🎋", "JLPT N2 — Upper-Intermediate",
            "Broad vocabulary, complex grammar, and nuanced usage found in newspapers and formal writing.",
            { onLevelClick("N2") },
        ),
        MasterSection(
            "📰", "Reading Comprehension",
            "Long-form passages at N2–N1 difficulty. Practice sustained reading and precise comprehension under JLPT conditions.",
            { onReadingComprehension() },
        ),
        MasterSection(
            "🗣", "Dialogues",
            "Advanced conversations — making reservations, discussing interests, handling complex social situations.",
            { onDialogues() },
        ),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Master  ⛩",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(
                        Icons.Outlined.HelpOutline,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            },
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Native-level mastery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(sections.size) { i ->
                val section = sections[i]
                MasterSectionCard(section)
            }
        }
    }
}

@Composable
private fun MasterSectionCard(section: MasterSection) {
    Card(
        onClick = section.onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = section.emoji,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
