package app.kotori.japanese.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.ScreenHelpDialog

@Composable
fun ProgressScreen(onBack: () -> Unit, onStartReview: () -> Unit = {}) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Progress",
            description = "Track how much you have learned across all content types.\n\n" +
                "• Progress bars show how many items you have marked as known vs the total available.\n" +
                "• Mark items as known from any detail screen (kanji, verb, grammar, etc.) using the star icon.\n" +
                "• SRS Due — items scheduled for spaced-repetition review. Tap 'Start Review' to study them.\n\n" +
                "Spaced repetition (SRS) schedules items so you review them just before you forget, making study more efficient.",
            onDismiss = { showHelp = false },
        )
    }

    // Known counts — reactive
    val knownKanji  by container.knownRepository.getKnownCount("kanji").collectAsStateWithLifecycle(0)
    val knownVerb   by container.knownRepository.getKnownCount("verb").collectAsStateWithLifecycle(0)
    val knownAdj    by container.knownRepository.getKnownCount("adjective").collectAsStateWithLifecycle(0)
    val knownNoun   by container.knownRepository.getKnownCount("noun").collectAsStateWithLifecycle(0)
    val knownGrammar by container.knownRepository.getKnownCount("grammar").collectAsStateWithLifecycle(0)
    val knownPhrase by container.knownRepository.getKnownCount("phrase").collectAsStateWithLifecycle(0)
    val dueCount    by container.knownRepository.getDueCount().collectAsStateWithLifecycle(0)

    // Totals — loaded once from repos
    val totalKanji   by produceState(0) { value = container.kanjiRepository.getAllKanji().size }
    val totalVerb    by produceState(0) { value = container.verbRepository.getAllVerbs().size }
    val totalAdj     by produceState(0) { value = container.adjectiveRepository.getAllAdjectives().size }
    val totalNoun    by produceState(0) { value = container.nounRepository.getAllNouns().size }
    val totalGrammar by produceState(0) { value = container.grammarRepository.getAllGrammar().size }
    val totalPhrase  by produceState(0) { value = container.phraseRepository.getAllPhrases().size }

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = "Progress",
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
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // SRS due card
            if (dueCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                "Review due",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                "$dueCount item${if (dueCount != 1) "s" else ""} ready",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            )
                        }
                        Button(onClick = onStartReview) { Text("Review") }
                    }
                }
            }

            // Overall known summary
            val totalKnown = knownKanji + knownVerb + knownAdj + knownNoun + knownGrammar + knownPhrase
            val totalItems = totalKanji + totalVerb + totalAdj + totalNoun + totalGrammar + totalPhrase

            SectionCard(title = "Overall") {
                OverallSummary(known = totalKnown, total = totalItems)
            }

            // Per-category breakdown
            SectionCard(title = "By Category") {
                val categories = listOf(
                    Triple("漢字  Kanji",      knownKanji,   totalKanji),
                    Triple("動詞  Verbs",      knownVerb,    totalVerb),
                    Triple("形容詞  Adjectives", knownAdj,   totalAdj),
                    Triple("名詞  Nouns",      knownNoun,    totalNoun),
                    Triple("文法  Grammar",    knownGrammar, totalGrammar),
                    Triple("フレーズ  Phrases", knownPhrase,  totalPhrase),
                )
                categories.forEachIndexed { i, (label, known, total) ->
                    if (i > 0) HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                    CategoryRow(label = label, known = known, total = total)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun OverallSummary(known: Int, total: Int) {
    val progress = if (total > 0) known.toFloat() / total else 0f
    val pct = (progress * 100).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "$known known",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "of $total total ($pct%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        )
    }
}

@Composable
private fun CategoryRow(label: String, known: Int, total: Int) {
    val progress = if (total > 0) known.toFloat() / total else 0f
    val pct = (progress * 100).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "$known / $total  ($pct%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (total > 0) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }
    }
}
