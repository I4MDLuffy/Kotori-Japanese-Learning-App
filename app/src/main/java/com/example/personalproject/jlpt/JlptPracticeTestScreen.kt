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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

// ── Question types ─────────────────────────────────────────────────────────────

private enum class QuestionType(val label: String) {
    MEANING("Meaning"),          // Japanese word → English meaning
    KANJI_READING("Reading"),    // Kanji form → correct hiragana reading
    ORTHOGRAPHY("Orthography"),  // Hiragana reading → correct kanji/kana form
    CONTEXTUAL("Context"),       // Fill-in-the-blank from an example sentence
    GRAMMAR("Grammar"),          // Grammar description → identify pattern
    REARRANGE("Sentence Order"), // Choose correct fragment for ★ position
}

// ── Data models ────────────────────────────────────────────────────────────────

private data class TestQuestion(
    val type: QuestionType,
    val prompt: String,
    val questionLabel: String,
    val correctAnswer: String,
    val wrongAnswers: List<String>,
    val wordTitle: String = "",    // Japanese title for weak/review tracking
    val wordReading: String = "",
    val wordMeaning: String = "",
    val itemId: String = "",
    val itemType: String = "vocab",
)

private data class TypeResult(val type: QuestionType, val correct: Int, val total: Int)

private data class WrongAnswerEntry(val question: TestQuestion, val userAnswer: String)

// ── Static sentence-rearrangement questions ───────────────────────────────────
// Each question shows a base sentence with ＿＿ ★ ＿＿ blanks and 4 numbered
// fragments; the user identifies which fragment belongs in the ★ position.

private val rearrangeByLevel: Map<String, List<TestQuestion>> = mapOf(
    "N5" to listOf(
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "これは ＿＿ ★ ＿＿ 本です。\n①きれいな ②私の ③古い ④友達の",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "①きれいな",
            wrongAnswers = listOf("②私の", "③古い", "④友達の"),
        ),
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "毎朝 ＿＿ ★ ＿＿ を飲みます。\n①おいしい ②コーヒー ③熱い ④一杯",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "③熱い",
            wrongAnswers = listOf("①おいしい", "②コーヒー", "④一杯"),
        ),
    ),
    "N4" to listOf(
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "もっと ＿＿ ★ ＿＿ なりたいです。\n①日本語が ②上手に ③話せる ④ように",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "②上手に",
            wrongAnswers = listOf("①日本語が", "③話せる", "④ように"),
        ),
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "この映画は ＿＿ ★ ＿＿ 感動的です。\n①何度も ②見た ③ことが ④ある",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "②見た",
            wrongAnswers = listOf("①何度も", "③ことが", "④ある"),
        ),
    ),
    "N3" to listOf(
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "彼女は ＿＿ ★ ＿＿ 悩んでいるそうです。\n①仕事の ②ことで ③ずっと ④最近",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "①仕事の",
            wrongAnswers = listOf("②ことで", "③ずっと", "④最近"),
        ),
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "この問題は ＿＿ ★ ＿＿ 解けません。\n①簡単 ②では ③なく ④とても",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "②では",
            wrongAnswers = listOf("①簡単", "③なく", "④とても"),
        ),
    ),
    "N2" to listOf(
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "計画を ＿＿ ★ ＿＿ いかない。\n①変更 ②する ③わけには ④しない",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "③わけには",
            wrongAnswers = listOf("①変更", "②する", "④しない"),
        ),
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "環境問題は ＿＿ ★ ＿＿ ならず世界全体の課題だ。\n①日本 ②のみ ③だけで ④にとどまら",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "②のみ",
            wrongAnswers = listOf("①日本", "③だけで", "④にとどまら"),
        ),
    ),
    "N1" to listOf(
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "この条約は ＿＿ ★ ＿＿ ものだ。\n①廃止 ②余儀なく ③された ④とはいえ",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "①廃止",
            wrongAnswers = listOf("②余儀なく", "③された", "④とはいえ"),
        ),
        TestQuestion(
            type = QuestionType.REARRANGE,
            prompt = "経済発展は ＿＿ ★ ＿＿ 急速に変化している。\n①歴史的背景 ②を踏まえ ③著しく ④ながらも",
            questionLabel = "Which fragment goes in the ★ position?",
            correctAnswer = "②を踏まえ",
            wrongAnswers = listOf("①歴史的背景", "③著しく", "④ながらも"),
        ),
    ),
)

// ── Helper ─────────────────────────────────────────────────────────────────────

private fun shortGrammarTitle(title: String): String =
    title.split("—").firstOrNull()?.trim()?.take(30) ?: title.take(30)

// ── Entry composable ──────────────────────────────────────────────────────────

@Composable
fun JlptPracticeTestScreen(level: String, isMock: Boolean = false, onBack: () -> Unit) {
    val container = LocalAppContainer.current

    val questions by produceState<List<TestQuestion>?>(null) {
        val vocab  = container.vocabularyRepository.getAllWords().filter { it.jlptLevel == level }
        val nouns  = container.nounRepository.getAllNouns().filter { it.jlptLevel == level }
        val grammar = container.grammarRepository.getAllGrammar().filter { it.jlptLevel == level }

        if (vocab.isEmpty() && nouns.isEmpty()) {
            value = emptyList()
            return@produceState
        }

        val targetCount = if (isMock) 35 else 20
        val generated  = mutableListOf<TestQuestion>()

        // ── Word pool shared by meaning / reading / orthography / contextual ──
        data class WordInfo(
            val jp: String, val hiragana: String, val meaning: String,
            val group: String, val id: String,
        )

        val wordPool: List<WordInfo> = (
            vocab.map { WordInfo(it.japanese, it.hiragana, it.english, it.category, it.id) } +
            nouns.map { WordInfo(it.kanji.ifBlank { it.hiragana }, it.hiragana, it.meaning, it.theme, it.id) }
        ).filter { it.jp.isNotBlank() && it.meaning.isNotBlank() }.shuffled()

        // ── MEANING ──────────────────────────────────────────────────────────
        val meaningCount = (targetCount * 0.25).toInt().coerceAtLeast(4)
        for (word in wordPool.take(meaningCount)) {
            val sameGroup = wordPool.filter { it.group == word.group && it.meaning != word.meaning }
            val anyOther  = wordPool.filter { it.meaning != word.meaning }
            val distractors = (sameGroup.map { it.meaning } + anyOther.map { it.meaning })
                .distinct().take(3)
            if (distractors.size == 3) {
                generated += TestQuestion(
                    type = QuestionType.MEANING,
                    prompt = word.jp,
                    questionLabel = "What does this word mean?",
                    correctAnswer = word.meaning,
                    wrongAnswers = distractors,
                    wordTitle = word.jp,
                    wordReading = word.hiragana,
                    wordMeaning = word.meaning,
                    itemId = word.id,
                    itemType = "vocab",
                )
            }
        }

        // ── KANJI_READING ─────────────────────────────────────────────────────
        val readingPool = (
            vocab.filter { it.japanese != it.hiragana && it.hiragana.isNotBlank() }
                .map { Triple(it.japanese, it.hiragana, it.id) } +
            nouns.filter { it.kanji.isNotBlank() && it.kanji != it.hiragana && it.hiragana.isNotBlank() }
                .map { Triple(it.kanji, it.hiragana, it.id) }
        ).filter { it.first.isNotBlank() }.shuffled()

        val allReadings  = readingPool.map { it.second }.distinct()
        val readingCount = (targetCount * 0.20).toInt().coerceAtLeast(3)
        for ((jp, correct, id) in readingPool.take(readingCount)) {
            val distractors = allReadings.filter { it != correct }.shuffled().take(3)
            if (distractors.size == 3) {
                generated += TestQuestion(
                    type = QuestionType.KANJI_READING,
                    prompt = jp,
                    questionLabel = "How do you read this word?",
                    correctAnswer = correct,
                    wrongAnswers = distractors,
                    wordTitle = jp,
                    wordReading = correct,
                    wordMeaning = wordPool.firstOrNull { it.jp == jp }?.meaning ?: "",
                    itemId = id,
                    itemType = "vocab",
                )
            }
        }

        // ── ORTHOGRAPHY ───────────────────────────────────────────────────────
        val orthPool = (
            vocab.filter { it.japanese != it.hiragana && it.hiragana.isNotBlank() }
                .map { Triple(it.hiragana, it.japanese, it.id) } +
            nouns.filter { it.kanji.isNotBlank() && it.kanji != it.hiragana && it.hiragana.isNotBlank() }
                .map { Triple(it.hiragana, it.kanji, it.id) }
        ).filter { it.first.isNotBlank() && it.second.isNotBlank() }.shuffled()

        val allForms  = orthPool.map { it.second }.distinct()
        val orthCount = (targetCount * 0.15).toInt().coerceAtLeast(2)
        for ((reading, correct, id) in orthPool.take(orthCount)) {
            val distractors = allForms.filter { it != correct }.shuffled().take(3)
            if (distractors.size == 3) {
                generated += TestQuestion(
                    type = QuestionType.ORTHOGRAPHY,
                    prompt = reading,
                    questionLabel = "Which is the correct kanji spelling?",
                    correctAnswer = correct,
                    wrongAnswers = distractors,
                    wordTitle = correct,
                    wordReading = reading,
                    wordMeaning = wordPool.firstOrNull { it.hiragana == reading }?.meaning ?: "",
                    itemId = id,
                    itemType = "vocab",
                )
            }
        }

        // ── CONTEXTUAL ────────────────────────────────────────────────────────
        val contextPool = vocab
            .filter { it.exampleJapanese.isNotBlank() && it.japanese.isNotBlank() }
            .filter {
                it.exampleJapanese.contains(it.japanese) ||
                (it.hiragana.isNotBlank() && it.exampleJapanese.contains(it.hiragana))
            }.shuffled()

        val allJp        = wordPool.map { it.jp }.distinct()
        val contextCount = (targetCount * 0.15).toInt().coerceAtLeast(2)
        for (word in contextPool.take(contextCount)) {
            val sentence = when {
                word.exampleJapanese.contains(word.japanese) ->
                    word.exampleJapanese.replace(word.japanese, "＿＿＿")
                word.hiragana.isNotBlank() && word.exampleJapanese.contains(word.hiragana) ->
                    word.exampleJapanese.replace(word.hiragana, "＿＿＿")
                else -> continue
            }
            if (!sentence.contains("＿＿＿")) continue
            val correct     = word.japanese.ifBlank { word.hiragana }
            val distractors = allJp.filter { it != correct }.shuffled().take(3)
            if (distractors.size == 3) {
                generated += TestQuestion(
                    type = QuestionType.CONTEXTUAL,
                    prompt = sentence,
                    questionLabel = "Choose the word that completes the sentence:",
                    correctAnswer = correct,
                    wrongAnswers = distractors,
                    wordTitle = correct,
                    wordReading = word.hiragana,
                    wordMeaning = word.english,
                    itemId = word.id,
                    itemType = "vocab",
                )
            }
        }

        // ── GRAMMAR ───────────────────────────────────────────────────────────
        val allGrammarShort = grammar.map { shortGrammarTitle(it.title) }.distinct()
        val grammarCount    = (targetCount * 0.15).toInt().coerceAtLeast(2)
        for (g in grammar.shuffled().take(grammarCount)) {
            val clue        = g.content.split(". ").firstOrNull()?.trim()?.take(130) ?: continue
            val correct     = shortGrammarTitle(g.title)
            val distractors = allGrammarShort.filter { it != correct }.shuffled().take(3)
            if (distractors.size == 3 && clue.isNotBlank()) {
                generated += TestQuestion(
                    type = QuestionType.GRAMMAR,
                    prompt = clue,
                    questionLabel = "Which grammar pattern matches this usage?",
                    correctAnswer = correct,
                    wrongAnswers = distractors,
                    wordTitle = g.title,
                    wordReading = "",
                    wordMeaning = clue,
                    itemId = g.id,
                    itemType = "grammar",
                )
            }
        }

        // ── REARRANGE (static) ────────────────────────────────────────────────
        val rearrangeQuestions = rearrangeByLevel[level] ?: emptyList()
        generated.addAll(rearrangeQuestions)

        value = generated.shuffled().take(targetCount)
    }

    when {
        questions == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Preparing test…", style = MaterialTheme.typography.bodyMedium)
            }
        }
        questions!!.isEmpty() -> Column(Modifier.fillMaxSize()) {
            KotobaTopBar(title = "$level ${if (isMock) "Mock Test" else "Practice Test"}", onBack = onBack)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No content available for $level yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }
        else -> TestSession(level = level, isMock = isMock, questions = questions!!, onBack = onBack)
    }
}

// ── Test session ───────────────────────────────────────────────────────────────

@Composable
private fun TestSession(
    level: String,
    isMock: Boolean,
    questions: List<TestQuestion>,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current

    var currentIndex   by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isFinished     by remember { mutableStateOf(false) }

    // (type, wasCorrect) log for breakdown table
    val answeredLog     = remember { mutableStateListOf<Pair<QuestionType, Boolean>>() }
    // Indices the user bookmarked with the flag icon
    val bookmarkedIndices = remember { mutableStateListOf<Int>() }
    // Full wrong-answer log for the review section
    val wrongAnswerLog  = remember { mutableStateListOf<WrongAnswerEntry>() }

    // Section-level timer
    val totalSeconds = remember {
        if (isMock) when (level) { "N5" -> 25*60; "N4" -> 30*60; "N3" -> 30*60; else -> 35*60 }
        else questions.size * 75
    }
    var timeLeft by remember { mutableIntStateOf(totalSeconds) }

    LaunchedEffect(isFinished) {
        if (!isFinished) {
            while (timeLeft > 0) { delay(1000L); timeLeft-- }
            isFinished = true
        }
    }

    // Save weak/bookmarked items once the test finishes
    LaunchedEffect(isFinished) {
        if (!isFinished) return@LaunchedEffect
        wrongAnswerLog.forEach { entry ->
            val q = entry.question
            if (q.itemId.isNotBlank() && !container.savedRepository.isSaved("jlpt_weak", q.itemId)) {
                container.savedRepository.toggle(
                    type = "jlpt_weak", itemId = q.itemId,
                    title = q.wordTitle, reading = q.wordReading, meaning = q.wordMeaning,
                )
            }
        }
        bookmarkedIndices.forEach { idx ->
            val q = questions[idx]
            if (q.itemId.isNotBlank() && !container.savedRepository.isSaved("jlpt_review", q.itemId)) {
                container.savedRepository.toggle(
                    type = "jlpt_review", itemId = q.itemId,
                    title = q.wordTitle, reading = q.wordReading, meaning = q.wordMeaning,
                )
            }
        }
    }

    if (isFinished) {
        val typeResults = QuestionType.entries.map { type ->
            val entries = answeredLog.filter { it.first == type }
            TypeResult(type, entries.count { it.second }, entries.size)
        }
        TestResults(
            level = level, isMock = isMock,
            typeResults = typeResults,
            wrongAnswers = wrongAnswerLog.toList(),
            onBack = onBack,
        )
        return
    }

    val question  = questions[currentIndex]
    val options   = remember(currentIndex) {
        (listOf(question.correctAnswer) + question.wrongAnswers).shuffled()
    }
    val isBookmarked = currentIndex in bookmarkedIndices

    val isLargePrompt = question.type !in setOf(QuestionType.CONTEXTUAL, QuestionType.REARRANGE, QuestionType.GRAMMAR)
    val typeBadgeColor = when (question.type) {
        QuestionType.MEANING       -> MaterialTheme.colorScheme.primary
        QuestionType.KANJI_READING -> MaterialTheme.colorScheme.secondary
        QuestionType.ORTHOGRAPHY   -> MaterialTheme.colorScheme.tertiary
        QuestionType.CONTEXTUAL    -> MaterialTheme.colorScheme.error
        QuestionType.GRAMMAR       -> Color(0xFF7B1FA2)
        QuestionType.REARRANGE     -> Color(0xFF00796B)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "$level ${if (isMock) "Mock Test" else "Practice Test"}",
            onBack = onBack,
            actions = {
                // Bookmark icon
                IconButton(onClick = {
                    if (isBookmarked) bookmarkedIndices.remove(currentIndex)
                    else bookmarkedIndices.add(currentIndex)
                }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark for review",
                    )
                }
                // Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Icon(
                        Icons.Filled.Timer, contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (timeLeft < 120) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = "${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (timeLeft < 120) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Progress row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Q${currentIndex + 1} / ${questions.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(typeBadgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = question.type.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeBadgeColor,
                    )
                }
            }

            LinearProgressIndicator(
                progress = { currentIndex.toFloat() / questions.size },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
            )

            // Prompt card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(if (isLargePrompt) 28.dp else 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = question.prompt,
                    fontSize = if (isLargePrompt) 46.sp else 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isLargePrompt) 56.sp else 26.sp,
                )
            }

            Text(
                text = question.questionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            // Answer options
            options.forEach { option ->
                val isSelected = selectedAnswer == option
                val isCorrect  = option == question.correctAnswer
                val bgColor = when {
                    selectedAnswer == null -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
                val borderColor = when {
                    selectedAnswer == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    isCorrect -> Color(0xFF2E7D32)
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = selectedAnswer == null) {
                            selectedAnswer = option
                            val wasCorrect = (option == question.correctAnswer)
                            answeredLog.add(Pair(question.type, wasCorrect))
                            if (!wasCorrect) {
                                wrongAnswerLog.add(WrongAnswerEntry(question, option))
                            }
                        }
                        .padding(16.dp),
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (selectedAnswer != null) {
                Button(
                    onClick = {
                        if (currentIndex < questions.size - 1) {
                            currentIndex++; selectedAnswer = null
                        } else {
                            isFinished = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = if (currentIndex < questions.size - 1) "Next Question" else "See Results",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Results screen ─────────────────────────────────────────────────────────────

@Composable
private fun TestResults(
    level: String,
    isMock: Boolean,
    typeResults: List<TypeResult>,
    wrongAnswers: List<WrongAnswerEntry>,
    onBack: () -> Unit,
) {
    val answered     = typeResults.filter { it.total > 0 }
    val totalCorrect = answered.sumOf { it.correct }
    val totalAnswered= answered.sumOf { it.total }
    val pct          = if (totalAnswered > 0) (totalCorrect.toFloat() / totalAnswered * 100).toInt() else 0
    val passed       = pct >= 60
    val emoji        = when { pct >= 90 -> "🏆"; pct >= 70 -> "🎉"; pct >= 60 -> "👍"; else -> "📚" }

    var showWrongReview by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "$level Results", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(emoji, fontSize = 64.sp, textAlign = TextAlign.Center)
            Text(
                text = if (passed) "Well done!" else "Keep practising!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "$totalCorrect / $totalAnswered correct ($pct%)",
                style = MaterialTheme.typography.titleLarge,
                color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )

            // ── Per-type breakdown ─────────────────────────────────────────────
            if (answered.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Results by question type",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        answered.forEach { result ->
                            val typePct  = (result.correct.toFloat() / result.total * 100).toInt()
                            val barColor = if (typePct >= 60) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.error
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = result.type.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "${result.correct}/${result.total}  ($typePct%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = barColor,
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { result.correct.toFloat() / result.total },
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                                    color = barColor,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                )
                            }
                        }
                    }
                }
            }

            // ── Weak area advice ───────────────────────────────────────────────
            val weakest = answered.minByOrNull { it.correct.toFloat() / it.total }
            if (weakest != null && (weakest.correct.toFloat() / weakest.total) < 0.70f) {
                val advice = when (weakest.type) {
                    QuestionType.MEANING ->
                        "Review $level word lists and reinforce Japanese–English meaning associations with flashcards."
                    QuestionType.KANJI_READING ->
                        "Practice on'yomi and kun'yomi for $level kanji in full word context, not in isolation."
                    QuestionType.ORTHOGRAPHY ->
                        "Study stroke order and radicals to distinguish visually similar kanji. Write by hand."
                    QuestionType.CONTEXTUAL ->
                        "Study words through their example sentences. Focus on how words behave in context."
                    QuestionType.GRAMMAR ->
                        "Review the $level grammar section. Pay attention to how each pattern is used differently."
                    QuestionType.REARRANGE ->
                        "Practice reading full Japanese sentences. Focus on particle order and verb placement."
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Weak area: ${weakest.type.label}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = advice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            // ── Weak points + bookmarks saved notice ───────────────────────────
            if (wrongAnswers.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "${wrongAnswers.size} missed item${if (wrongAnswers.size != 1) "s" else ""} saved to Weak Points in your Saved section.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            // ── Detailed wrong-answer review ───────────────────────────────────
            if (wrongAnswers.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showWrongReview = !showWrongReview }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Review wrong answers (${wrongAnswers.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Icon(
                                imageVector = if (showWrongReview) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (showWrongReview) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            wrongAnswers.forEachIndexed { idx, entry ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    // Question type badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = entry.question.type.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    // Prompt (truncated if very long)
                                    Text(
                                        text = entry.question.prompt.take(100) +
                                            if (entry.question.prompt.length > 100) "…" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // User's wrong answer
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                        ) {
                                            Text(
                                                text = "✗  ${entry.userAnswer}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                        // Correct answer
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF2E7D32).copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                        ) {
                                            Text(
                                                text = "✓  ${entry.question.correctAnswer}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }
                                    // Meaning hint if available
                                    if (entry.question.wordMeaning.isNotBlank() &&
                                        entry.question.type != QuestionType.MEANING
                                    ) {
                                        Text(
                                            text = entry.question.wordMeaning.take(80),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                                if (idx < wrongAnswers.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Pass/fail summary ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (passed) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        text = if (passed) "You passed the $level ${if (isMock) "mock test" else "practice test"}!"
                               else "Passing score is 60%. Review $level content and try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Back to $level Hub", fontWeight = FontWeight.Bold)
            }
        }
    }
}
