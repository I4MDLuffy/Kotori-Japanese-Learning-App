package app.kotori.japanese.jlpt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.ui.components.KotobaTopBar

// ── Data ───────────────────────────────────────────────────────────────────────

private data class DiagQuestion(
    val level: String,
    val prompt: String,
    val correctAnswer: String,
    val wrongAnswers: List<String>,
)

private data class DiagResult(val level: String, val correct: Int, val total: Int)

private val levelOrder = listOf("N5", "N4", "N3", "N2", "N1")

private val levelEmoji = mapOf(
    "N5" to "🌱", "N4" to "🌿", "N3" to "🌳", "N2" to "🎋", "N1" to "⛩",
)

// ── Entry point ────────────────────────────────────────────────────────────────

@Composable
fun JlptDiagnosticTestScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current

    val questions by produceState<List<DiagQuestion>?>(null) {
        val allVocab = container.vocabularyRepository.getAllWords()
        val allNouns = container.nounRepository.getAllNouns()

        data class WordInfo(val jp: String, val meaning: String, val level: String, val group: String)

        val wordsByLevel: Map<String, List<WordInfo>> = levelOrder.associateWith { lvl ->
            (allVocab.filter { it.jlptLevel == lvl }
                .map { WordInfo(it.japanese, it.english, lvl, it.category) } +
            allNouns.filter { it.jlptLevel == lvl }
                .map { WordInfo(it.kanji.ifBlank { it.hiragana }, it.meaning, lvl, it.theme) })
                .filter { it.jp.isNotBlank() && it.meaning.isNotBlank() }
                .distinctBy { it.jp }
        }

        val generated = mutableListOf<DiagQuestion>()
        val questionsPerLevel = 4

        for (lvl in levelOrder) {
            val pool = wordsByLevel[lvl]?.shuffled() ?: continue
            val allMeanings = wordsByLevel.values.flatten().map { it.meaning }.distinct()

            for (word in pool.take(questionsPerLevel)) {
                val distractors = allMeanings
                    .filter { it != word.meaning }
                    .shuffled()
                    .take(3)
                if (distractors.size == 3) {
                    generated += DiagQuestion(
                        level = lvl,
                        prompt = word.jp,
                        correctAnswer = word.meaning,
                        wrongAnswers = distractors,
                    )
                }
            }
        }

        value = generated.shuffled()
    }

    when {
        questions == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator()
                    Text("Preparing your diagnostic test…", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        questions!!.isEmpty() -> {
            Column(modifier = Modifier.fillMaxSize()) {
                KotobaTopBar(title = "Diagnostic Test", onBack = onBack)
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Not enough content loaded to run a diagnostic test.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
        }
        else -> DiagnosticSession(questions = questions!!, onBack = onBack)
    }
}

// ── Session ────────────────────────────────────────────────────────────────────

@Composable
private fun DiagnosticSession(questions: List<DiagQuestion>, onBack: () -> Unit) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateListOf<String?>() }
    var isFinished by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var confirmed by remember { mutableStateOf(false) }

    // Initialise answer slots
    if (answers.size < questions.size) {
        repeat(questions.size - answers.size) { answers.add(null) }
    }

    if (isFinished) {
        val results = levelOrder.map { lvl ->
            val levelQuestions = questions.withIndex().filter { it.value.level == lvl }
            val correct = levelQuestions.count { (idx, q) ->
                answers.getOrNull(idx) == q.correctAnswer
            }
            DiagResult(level = lvl, correct = correct, total = levelQuestions.size)
        }
        DiagnosticResults(results = results, onBack = onBack)
        return
    }

    val question = questions[currentIndex]
    val allOptions = remember(currentIndex) {
        (listOf(question.correctAnswer) + question.wrongAnswers).shuffled()
    }
    val progress = (currentIndex + 1).toFloat() / questions.size.toFloat()

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Diagnostic Test",
            onBack = onBack,
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
                Text(
                    text = "What does this word mean?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }

            // Prompt card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = question.prompt,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Answer options
            allOptions.forEach { option ->
                val isSelected = selectedAnswer == option
                val isCorrect = option == question.correctAnswer
                val bgColor = when {
                    !confirmed -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val borderColor = when {
                    !confirmed && isSelected -> MaterialTheme.colorScheme.primary
                    confirmed && isCorrect -> Color(0xFF2E7D32)
                    confirmed && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .border(
                            width = if (borderColor != Color.Transparent) 2.dp else 0.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clickable(enabled = !confirmed) {
                            selectedAnswer = option
                        }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                confirmed && isCorrect -> Color(0xFF1B5E20)
                                confirmed && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                        )
                        if (confirmed && isCorrect) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Confirm / Next button
            Button(
                onClick = {
                    if (!confirmed) {
                        if (selectedAnswer != null) {
                            answers[currentIndex] = selectedAnswer
                            confirmed = true
                        }
                    } else {
                        if (currentIndex + 1 < questions.size) {
                            currentIndex++
                            selectedAnswer = null
                            confirmed = false
                        } else {
                            isFinished = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null || confirmed,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (confirmed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = when {
                        !confirmed -> "Confirm"
                        currentIndex + 1 < questions.size -> "Next Question"
                        else -> "See Results"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ── Results ────────────────────────────────────────────────────────────────────

@Composable
private fun DiagnosticResults(results: List<DiagResult>, onBack: () -> Unit) {
    // Recommend the highest level where ≥ 50% correct (at least 2/4)
    val recommendedLevel = results
        .filter { it.total > 0 && it.correct.toFloat() / it.total.toFloat() >= 0.5f }
        .maxByOrNull { levelOrder.indexOf(it.level) }
        ?.level ?: "N5"

    val totalCorrect = results.sumOf { it.correct }
    val totalQuestions = results.sumOf { it.total }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Your Results", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Recommendation banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Filled.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        text = "Recommended Level",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = levelEmoji[recommendedLevel] ?: "",
                            fontSize = 32.sp,
                        )
                        Text(
                            text = recommendedLevel,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = levelDescription(recommendedLevel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Text(
                        text = "Overall: $totalCorrect / $totalQuestions correct",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    )
                }
            }

            // Per-level breakdown
            Text(
                text = "Score by Level",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            results.forEach { result ->
                if (result.total == 0) return@forEach
                val pct = result.correct.toFloat() / result.total.toFloat()
                val passed = pct >= 0.5f
                val isRecommended = result.level == recommendedLevel

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRecommended)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    border = if (isRecommended) androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    ) else null,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(text = levelEmoji[result.level] ?: "", fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = result.level,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    if (isRecommended) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = "Recommended",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${result.correct} / ${result.total} correct",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                            // Score circle
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (passed) Color(0xFF2E7D32).copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "${(pct * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (passed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (passed) Color(0xFF43A047) else MaterialTheme.colorScheme.error,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        )
                    }
                }
            }

            // Advice card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "What to do next",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = nextStepsAdvice(recommendedLevel),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 20.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Done", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun levelDescription(level: String): String = when (level) {
    "N5" -> "Beginner — basic hiragana, katakana, and survival vocabulary"
    "N4" -> "Elementary — common grammar patterns and everyday conversation"
    "N3" -> "Intermediate — broad contexts, abstract topics, and newspaper headlines"
    "N2" -> "Upper-Intermediate — formal writing, nuanced expression, complex grammar"
    "N1" -> "Advanced — near-native comprehension, classical forms, idiomatic Japanese"
    else -> ""
}

private fun nextStepsAdvice(level: String): String = when (level) {
    "N5" -> "Focus on mastering hiragana and katakana, then build core vocabulary. " +
        "Use the Beginner section and JLPT N5 Study Hub to get started."
    "N4" -> "You have a solid beginner base. Study て-form verb conjugations and N4 grammar patterns. " +
        "Browse the JLPT N4 hub and practice with short conversations."
    "N3" -> "Good intermediate footing! Expand your kanji knowledge and work on noun modification patterns. " +
        "Try N3 practice tests and reading comprehension exercises."
    "N2" -> "Strong command of everyday Japanese. Study abstract grammar, formal expressions, and newspaper vocabulary. " +
        "Challenge yourself with N2 mock tests and the Reading Comprehension section."
    "N1" -> "Near-native level! Focus on classical forms, domain-specific vocabulary, and idiomatic expressions. " +
        "Use N1 mock tests and advanced dialogues to refine your comprehension."
    else -> "Start with the Beginner section to build a strong foundation."
}
