package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// 1. Unified Gradient Background (Reusable background token for onboarding)
@Composable
fun GradientBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            ),
        content = content
    )
}

// 2. Custom Pill Button (Overloaded to retain backward compatibility with old layout calls and support a premium variant specification)
enum class ButtonVariant {
    Primary,
    Secondary,
    Outlined,
    Text,
    White
}

@Composable
fun NeuraLexButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val containerColor = backgroundColor ?: when (variant) {
        ButtonVariant.Primary -> PrimaryPurple
        ButtonVariant.Secondary -> LightPurple
        ButtonVariant.Outlined -> Color.Transparent
        ButtonVariant.Text -> Color.Transparent
        ButtonVariant.White -> Color.White
    }

    val contentColor = textColor ?: when (variant) {
        ButtonVariant.Primary -> Color.White
        ButtonVariant.Secondary -> PrimaryPurple
        ButtonVariant.Outlined -> PrimaryPurple
        ButtonVariant.Text -> PrimaryPurple
        ButtonVariant.White -> PrimaryPurple
    }

    val borderStroke = if (variant == ButtonVariant.Outlined) {
        androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple)
    } else null

    Surface(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .then(
                if (variant == ButtonVariant.Primary || variant == ButtonVariant.White || backgroundColor == Color.White) {
                    Modifier.shadow(4.dp, RoundedCornerShape(16.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = borderStroke,
        enabled = enabled && !isLoading,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        fontSize = 16.sp
                    )
                )

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// 3. Custom Card Container (Supports 22dp card radius, custom border, shadow, elevation, and dynamic tap triggers)
@Composable
fun NeuraLexCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(22.dp))
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    } else {
        modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(22.dp))
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        content = content
    )
}

// 4. Word display pill (For synonyms or labels tags)
@Composable
fun SynonymTag(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LightPurple)
            .border(1.dp, BorderColorLight, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = PrimaryPurple
            )
        )
    }
}

// 5. Custom Reusable Input Text Field
@Composable
fun NeuraLexInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isSystemInDarkTheme()) SecondaryTextDark else SecondaryTextLight
                    )
                )
            },
            leadingIcon = {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = {
                if (trailingIcon != null) {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = if (isSystemInDarkTheme()) SecondaryTextDark else SecondaryTextLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(16.dp),
            isError = isError,
            singleLine = singleLine,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isSystemInDarkTheme()) PrimaryTextDark else PrimaryTextLight,
                unfocusedTextColor = if (isSystemInDarkTheme()) PrimaryTextDark else PrimaryTextLight,
                focusedContainerColor = if (isSystemInDarkTheme()) CardBgDark else CardBgLight,
                unfocusedContainerColor = if (isSystemInDarkTheme()) CardBgDark else CardBgLight,
                disabledContainerColor = if (isSystemInDarkTheme()) CardBgDark else CardBgLight,
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = if (isSystemInDarkTheme()) BorderColorDark else BorderColorLight,
                errorBorderColor = ErrorRed,
                cursorColor = PrimaryPurple
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = ErrorRed,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

// 6. Reusable Premium Icon Wrapper (Ensuring proper accessibility, circular/rounded bubble, and touch sizes)
@Composable
fun NeuraLexIconWrapper(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = PrimaryPurple,
    backgroundColor: Color = if (isSystemInDarkTheme()) BorderColorDark else LightPurple,
    onClick: (() -> Unit)? = null,
    iconSize: Dp = 20.dp
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(clickableModifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

// 7. Reusable Section Header Component
@Composable
fun NeuraLexSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    tint: Color = PrimaryPurple
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) PrimaryTextDark else PrimaryTextLight
                )
            )
        }
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = PrimaryPurple,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

// 8. Reusable Dynamic Edge-to-Edge Responsive Container
@Composable
fun NeuraLexScreenContainer(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    hasGradientBg: Boolean = false,
    useScroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (navigationIcon != null) {
                            navigationIcon()
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (hasGradientBg) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                    if (actions != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            actions()
                        }
                    }
                }
            }
        },
        bottomBar = bottomBar ?: {},
        floatingActionButton = floatingActionButton ?: {},
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        val backgroundModifier = if (hasGradientBg) {
            Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
        } else {
            Modifier.background(MaterialTheme.colorScheme.background)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
            ) {
                if (useScroll) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                content = content
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        content = content
                    )
                }
            }
        }
    }
}

// 9. Custom Bottom Tab Navigation Bar
@Composable
fun NeuraLexBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        TabItem("Home", "home", Icons.Default.Home),
        TabItem("Bookmarks", "bookmarks", Icons.Default.Bookmarks),
        TabItem("History", "history", Icons.Default.History),
        TabItem("Settings", "settings", Icons.Default.Settings)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isActive = currentTab == item.id
                val activeTabColor = PrimaryPurple
                val inactiveTabColor = SecondaryTextLight

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(item.id) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isActive) activeTabColor else inactiveTabColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isActive) activeTabColor else inactiveTabColor,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

data class TabItem(
    val label: String,
    val id: String,
    val icon: ImageVector
)
