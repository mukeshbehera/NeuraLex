package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WordObject
import com.example.ui.components.SynonymTag
import com.example.ui.theme.BorderColorDark
import com.example.ui.theme.BorderColorLight
import com.example.ui.theme.LightPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryTextLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    wordObj: WordObject?,
    onBack: () -> Unit,
    onSearchWord: (String) -> Unit,
    onToggleFavorite: (WordObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Dynamic fallback to static mock WordObject if none is currently selected in app state
    val resolvedWord = wordObj ?: WordObject(
        word = "Resilient",
        pronunciation = "/rɪˈzɪl.jənt/",
        type = "adjective",
        meaning = "Able to recoil or spring back into shape after bending, stretching, or being compressed. Possessing the capacity to recover quickly from difficult conditions; tough.",
        exampleSentence = "She is incredibly resilient, bouncing back stronger from every single obstacle in her path.",
        genZVersion = "tbh she's built different, literally unmatched bounce back energy fr ⚡",
        synonymsListString = "Tough, Strong, Hardy, Adaptable, Flexible, Buoyant",
        isFavorite = false,
        isWordOfTheDay = true
    )

    val isFav = resolvedWord.isFavorite

    val isLoading = resolvedWord.pronunciation == "/.../" && resolvedWord.type == "..."
    val isError = resolvedWord.meaning.startsWith("Error:") || 
                  resolvedWord.meaning.startsWith("Failed to parse") || 
                  resolvedWord.meaning.startsWith("AI returned") || 
                  resolvedWord.meaning.startsWith("Connection failed:")

    val synonyms = remember(resolvedWord.synonymsListString) {
        resolvedWord.synonyms
    }

    val infiniteTransition = rememberInfiniteTransition(label = "DetailPulsing")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // App Bar with Navigation, Save Indicator, and Overflow Options Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        onToggleFavorite(resolvedWord)
                        val message = if (isFav) "Removed '${resolvedWord.word}' from favorites" else "Saved '${resolvedWord.word}' to favorites"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite word",
                            tint = if (isFav) Color.Red else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Options & preferences panel", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Scrollable Content Layout
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrimaryPurple,
                        trackColor = LightPurple
                    )
                }
                // Word Display Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = resolvedWord.word,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = resolvedWord.pronunciation,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SecondaryTextLight,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LightPurple)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = resolvedWord.type,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }

                    // audio play button
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Playing pronunciation: ${resolvedWord.word}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightPurple)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play Pronunciation",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (isError) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .shadow(2.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline or Connection Error",
                                tint = Color.Red,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Search Failed",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = resolvedWord.meaning,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { onSearchWord(resolvedWord.word) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry Search", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    val sectionModifier = if (isLoading) {
                        Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = alphaAnim }
                    } else {
                        Modifier.fillMaxWidth()
                    }

                    Column(modifier = sectionModifier) {
                        // Meaning Block Section with soft card background and purple accent lines
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, if (isDark) BorderColorDark else BorderColorLight)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LightPurple),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Meaning",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = resolvedWord.meaning,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Example Sentence section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, if (isDark) BorderColorDark else BorderColorLight)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LightPurple),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FormatQuote,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Example Sentence",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                val exampleSentenceAnnotated = remember(resolvedWord.word, resolvedWord.exampleSentence) {
                                    buildAnnotatedString {
                                        val baseText = resolvedWord.exampleSentence
                                        val queryTerm = resolvedWord.word
                                        val indexStart = baseText.indexOf(queryTerm, ignoreCase = true)
                                        if (indexStart != -1) {
                                            val indexEnd = indexStart + queryTerm.length
                                            append(baseText.substring(0, indexStart))
                                            withStyle(style = SpanStyle(color = PrimaryPurple, fontWeight = FontWeight.Bold)) {
                                                append(baseText.substring(indexStart, indexEnd))
                                            }
                                            append(baseText.substring(indexEnd))
                                        } else {
                                            append(baseText)
                                        }
                                    }
                                }

                                Text(
                                    text = exampleSentenceAnnotated,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Gen Z Style card with beautiful gradient elements and a purple splash theme
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(18.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = if (isDark) {
                                            listOf(Color(0xFF281E46), Color(0xFF1D1535))
                                        } else {
                                            listOf(Color(0xFFF1EDFD), Color(0xFFE5DEFC))
                                        }
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = PrimaryPurple.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryPurple),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Gen Z Version",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PrimaryPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = resolvedWord.genZVersion,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = if (isDark) Color(0xFFE4DDFF) else PrimaryPurple,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        if (synonyms.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(26.dp))

                            // Synonyms pills List section
                            Text(
                                text = "Synonyms",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                synonyms.forEach { synonym ->
                                    SynonymTag(text = synonym, onClick = { onSearchWord(synonym) })
                                }
                            }
                        }
                    }
                }

                // Generous bottom padding spacer to allow content to scroll safely above the floating bar
                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        // Floating Action Bar docked beautifully above content with glass borders & soft shadows
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.dp,
                    color = (if (isDark) BorderColorDark else BorderColorLight).copy(alpha = 0.8f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailActionItem(
                        icon = Icons.Outlined.ContentCopy,
                        label = "Copy",
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText(
                                "Word Definition",
                                "${resolvedWord.word} [${resolvedWord.pronunciation}] (${resolvedWord.type})\n\nMeaning: ${resolvedWord.meaning}\n\nExample: ${resolvedWord.exampleSentence}"
                            )
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied '${resolvedWord.word}' definition to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DetailActionItem(
                        icon = Icons.Outlined.Share,
                        label = "Share",
                        onClick = {
                            try {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(
                                        android.content.Intent.EXTRA_TEXT,
                                        "${resolvedWord.word} [${resolvedWord.pronunciation}] (${resolvedWord.type})\n\nMeaning: ${resolvedWord.meaning}\n\nExample: ${resolvedWord.exampleSentence}\n\nGen Z version:\n${resolvedWord.genZVersion}"
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "Share '${resolvedWord.word}'")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error sharing word: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    DetailActionItem(
                        icon = if (isFav) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        label = "Save",
                        iconColor = if (isFav) Color.Red else PrimaryPurple,
                        onClick = {
                            onToggleFavorite(resolvedWord)
                            val message = if (isFav) "Removed '${resolvedWord.word}' from favorites" else "Saved '${resolvedWord.word}' to favorites"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconColor: Color = PrimaryPurple
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (iconColor == Color.Red) Color.Red else SecondaryTextLight,
                fontSize = 11.sp
            )
        )
    }
}
