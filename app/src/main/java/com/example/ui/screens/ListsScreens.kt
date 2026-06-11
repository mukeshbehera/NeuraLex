package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WordObject
import com.example.ui.components.NeuraLexCard
import com.example.ui.theme.LightPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryTextLight

@Composable
fun BookmarksScreen(
    favorites: List<WordObject>,
    onWordSelected: (WordObject) -> Unit,
    onToggleFavorite: (WordObject) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Text(
            text = "Bookmarks",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "No Bookmarks",
                        tint = PrimaryPurple.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your bookmarks checklist is empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the heart icon on any word card or meaning page to preserve terms offline for future recap.",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryTextLight,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favorites, key = { it.word }) { word ->
                    NeuraLexCard(
                        modifier = Modifier.clickable { onWordSelected(word) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = word.word,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = word.type,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PrimaryPurple,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = word.pronunciation,
                                        style = MaterialTheme.typography.labelMedium.copy(color = SecondaryTextLight)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = word.meaning,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(onClick = { onToggleFavorite(word) }) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Remove favorite",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    historyTerms: List<String>,
    onTermSelected: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteSingle: (String) -> Unit,
    onDeleteMultiple: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedTerms = remember { mutableStateListOf<String>() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var termToDelete by remember { mutableStateOf<String?>(null) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                termToDelete = null
            },
            title = {
                Text(text = "Delete History")
            },
            text = {
                Text(text = "Delete selected history items?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        if (isSelectionMode) {
                            onDeleteMultiple(selectedTerms.toList())
                            isSelectionMode = false
                            selectedTerms.clear()
                        } else {
                            termToDelete?.let {
                                onDeleteSingle(it)
                            }
                            termToDelete = null
                        }
                    }
                ) {
                    Text(text = "Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        termToDelete = null
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            isSelectionMode = false
                            selectedTerms.clear()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Selection",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${selectedTerms.size} Selected",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (selectedTerms.size == historyTerms.size) "Deselect All" else "Select All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable {
                                if (selectedTerms.size == historyTerms.size) {
                                    selectedTerms.clear()
                                } else {
                                    selectedTerms.clear()
                                    selectedTerms.addAll(historyTerms)
                                }
                            }
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (selectedTerms.isNotEmpty()) {
                                showDeleteConfirmation = true
                            }
                        },
                        enabled = selectedTerms.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Selected",
                            tint = if (selectedTerms.isNotEmpty()) Color.Red else Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Search History",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                if (historyTerms.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable(onClick = onClearHistory)
                            .padding(4.dp)
                    )
                }
            }
        }

        if (historyTerms.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Empty History",
                        tint = PrimaryPurple.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your search history is blank",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Any NeuraLex term you lookup will be stored locally here to allow rapid re-learning.",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryTextLight,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 24.dp)
            ) {
                items(historyTerms, key = { it }) { term ->
                    val isSelected = selectedTerms.contains(term)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedTerms.clear()
                                        selectedTerms.add(term)
                                    }
                                },
                                onClick = {
                                    if (isSelectionMode) {
                                        if (isSelected) {
                                            selectedTerms.remove(term)
                                            if (selectedTerms.isEmpty()) {
                                                isSelectionMode = false
                                            }
                                        } else {
                                            selectedTerms.add(term)
                                        }
                                    } else {
                                        onTermSelected(term)
                                    }
                                }
                            )
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked == true) {
                                            if (!selectedTerms.contains(term)) {
                                                selectedTerms.add(term)
                                            }
                                        } else {
                                            selectedTerms.remove(term)
                                            if (selectedTerms.isEmpty()) {
                                                isSelectionMode = false
                                            }
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryPurple,
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = SecondaryTextLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = term,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }

                        if (isSelectionMode) {
                            // No other actions needed
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        termToDelete = term
                                        showDeleteConfirmation = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Item",
                                        tint = SecondaryTextLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = SecondaryTextLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }
    }
}
