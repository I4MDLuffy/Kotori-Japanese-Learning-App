package app.kotori.japanese.kanji.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.model.KanjiEntry
import kotlinx.coroutines.launch
import app.kotori.japanese.kanji.list.mvi.KanjiListAction
import app.kotori.japanese.kanji.list.mvi.KanjiListViewModel
import app.kotori.japanese.ui.components.JlptBadge
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.ScreenHelpDialog
import app.kotori.japanese.util.kanaToRomaji

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KanjiListScreen(onKanjiClick: (id: String, allIds: String) -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: KanjiListViewModel = viewModel(
        factory = viewModelFactory { initializer { KanjiListViewModel(container.kanjiRepository) } }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Kanji",
            description = "Browse all kanji in the app.\n\n" +
                "• Search by kanji character, English meaning, or reading (hiragana/romaji).\n" +
                "• Filter by JLPT level using the chips below the search bar.\n" +
                "• Tap a kanji to see its full detail: stroke count, radicals, on/kun readings, and example words.\n" +
                "• Tap the bookmark icon to save a kanji to your Saved list.\n" +
                "• Tap the star icon to mark a kanji as known for progress tracking.",
            onDismiss = { showHelp = false },
        )
    }

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = "Kanji",
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
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { vm.dispatchAction(KanjiListAction.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Kanji, meaning, reading…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { vm.dispatchAction(KanjiListAction.Search("")) }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            if (state.availableJlptLevels.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.availableJlptLevels.forEach { level ->
                        FilterChip(
                            selected = state.selectedJlptLevel == level,
                            onClick = {
                                val next = if (state.selectedJlptLevel == level) null else level
                                vm.dispatchAction(KanjiListAction.FilterByJlpt(next))
                            },
                            label = { Text(level) },
                        )
                    }
                }
            }

            Text(
                text = "${state.displayedEntries.size} kanji",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            HorizontalDivider()

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.displayedEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No kanji found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.displayedEntries, key = { it.id }) { entry ->
                        KanjiListItem(entry = entry, onClick = {
                                onKanjiClick(entry.id, state.displayedEntries.joinToString("|") { it.id })
                            })
                    }
                }
            }
        }
    }
}

@Composable
private fun KanjiListItem(entry: KanjiEntry, onClick: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val isKnown by container.knownRepository.isItemKnownFlow("kanji", entry.id)
        .collectAsStateWithLifecycle(initialValue = false)
    val isSaved by container.savedRepository.isItemSavedFlow("kanji", entry.id)
        .collectAsStateWithLifecycle(initialValue = false)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = entry.kanji,
            fontSize = 40.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(56.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.meaning,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            if (entry.onYomi.isNotEmpty()) {
                Text(
                    text = "音: ${entry.onYomi.joinToString("、")}  ${entry.onYomi.joinToString(" / ") { kanaToRomaji(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.kunYomi.isNotEmpty()) {
                Text(
                    text = "訓: ${entry.kunYomi.joinToString("、")}  ${entry.kunYomi.joinToString(" / ") { kanaToRomaji(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Known (star) toggle
        IconButton(onClick = { scope.launch { container.knownRepository.toggle("kanji", entry.id) } }) {
            Icon(
                imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                tint = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Bookmark toggle
        IconButton(onClick = { scope.launch { container.savedRepository.toggle("kanji", entry.id, entry.kanji, entry.hiragana, entry.meaning) } }) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (isSaved) "Remove bookmark" else "Bookmark",
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        if (entry.jlptLevel.isNotBlank()) {
            JlptBadge(level = entry.jlptLevel)
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}
