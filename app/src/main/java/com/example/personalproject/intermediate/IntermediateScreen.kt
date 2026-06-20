package app.kotori.japanese.intermediate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.ScreenHelpDialog

private data class LevelCard(
    val level: String,
    val emoji: String,
    val name: String,
    val description: String,
    val vocabCount: Int,
    val kanjiCount: Int,
    val grammarCount: Int,
    val onClick: () -> Unit,
)

@Composable
fun IntermediateScreen(
    onBack: () -> Unit,
    onLevelClick: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Intermediate",
            description = "Study Japanese at N4–N3 level.\n\n" +
                "• N4 — Elementary: て-form grammar, common verb conjugations, everyday vocabulary and kanji for daily life.\n" +
                "• N3 — Intermediate: broader contexts, abstract nouns, noun modification, short-form speech, and complex sentence patterns.\n\n" +
                "Tap a level to open its Study Hub where you can browse vocabulary, kanji, grammar, and take practice tests.",
            onDismiss = { showHelp = false },
        )
    }

    val n4Vocab by produceState(0) {
        value = container.vocabularyRepository.getAllWords().count { it.jlptLevel == "N4" }
    }
    val n4Kanji by produceState(0) {
        value = container.kanjiRepository.getAllKanji().count { it.jlptLevel == "N4" }
    }
    val n4Grammar by produceState(0) {
        value = container.grammarRepository.getAllGrammar().count { it.jlptLevel == "N4" }
    }

    val n3Vocab by produceState(0) {
        value = container.vocabularyRepository.getAllWords().count { it.jlptLevel == "N3" }
    }
    val n3Kanji by produceState(0) {
        value = container.kanjiRepository.getAllKanji().count { it.jlptLevel == "N3" }
    }
    val n3Grammar by produceState(0) {
        value = container.grammarRepository.getAllGrammar().count { it.jlptLevel == "N3" }
    }

    val cards = listOf(
        LevelCard(
            level = "N4", emoji = "🌿", name = "Elementary",
            description = "Everyday vocabulary, て-form grammar, extended verb conjugations, and common kanji used in daily life.",
            vocabCount = n4Vocab, kanjiCount = n4Kanji, grammarCount = n4Grammar,
            onClick = { onLevelClick("N4") },
        ),
        LevelCard(
            level = "N3", emoji = "🌳", name = "Intermediate",
            description = "Broader contexts, abstract nouns, noun modification, short form speech, and complex sentence patterns.",
            vocabCount = n3Vocab, kanjiCount = n3Kanji, grammarCount = n3Grammar,
            onClick = { onLevelClick("N3") },
        ),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Intermediate  🌿",
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = "N4–N3 Level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(cards) { card ->
                IntermediateLevelCard(card)
            }
        }
    }
}

@Composable
private fun IntermediateLevelCard(card: LevelCard) {
    Card(
        onClick = card.onClick,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(text = card.emoji, fontSize = 28.sp)
                Column {
                    Text(
                        text = card.level,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
            Text(
                text = card.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
            if (card.vocabCount > 0 || card.kanjiCount > 0 || card.grammarCount > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (card.vocabCount > 0) StatChip("${card.vocabCount} Vocab")
                    if (card.kanjiCount > 0) StatChip("${card.kanjiCount} Kanji")
                    if (card.grammarCount > 0) StatChip("${card.grammarCount} Grammar")
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
