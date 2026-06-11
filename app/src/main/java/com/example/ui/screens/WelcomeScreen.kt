package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ButtonVariant
import com.example.ui.components.GradientBox
import com.example.ui.components.NeuraLexButton
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.TransparentGlass
import com.example.ui.theme.TransparentGlassBorder

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Core entry animation values for premium intro feel
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateIn = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutQuad),
        label = "entryAlpha"
    )

    val slideUp by animateDpAsState(
        targetValue = if (animateIn) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "entrySlide"
    )

    // 2. Parallax and floating infinite animations to breathe life into background symbols and main card
    val infiniteTransition = rememberInfiniteTransition(label = "WelcomeFloat")
    
    val logoFloatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoFloatOffset"
    )

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    val floatOffsetBook by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffsetBook"
    )

    val floatOffsetSparkle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffsetSparkle"
    )

    val floatOffsetStar by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffsetStar"
    )

    GradientBox(modifier = modifier) {
        // 3. Ambient Custom Bezier Wave Canvas at the bottom
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha }
        ) {
            val width = size.width
            val height = size.height

            // Layered translucent abstract wave 1
            val path1 = Path().apply {
                moveTo(0f, height * 0.72f)
                cubicTo(
                    width * 0.35f, height * 0.62f,
                    width * 0.65f, height * 0.82f,
                    width, height * 0.68f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = path1,
                color = Color.White.copy(alpha = 0.08f)
            )

            // Layered translucent abstract wave 2 (intersecting for a beautiful visual blend)
            val path2 = Path().apply {
                moveTo(0f, height * 0.78f)
                cubicTo(
                    width * 0.28f, height * 0.85f,
                    width * 0.72f, height * 0.70f,
                    width, height * 0.80f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = path2,
                color = Color.White.copy(alpha = 0.12f)
            )
        }

        // 4. Floating decorative symbols with subtle drift animations
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 50.dp)
                .size(46.dp)
                .rotate(18f)
                .offset(y = floatOffsetBook.dp)
        )

        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 36.dp, top = 140.dp)
                .size(32.dp)
                .offset(y = floatOffsetSparkle.dp)
        )

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.14f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 260.dp, end = 44.dp)
                .size(24.dp)
                .offset(y = floatOffsetStar.dp)
        )

        // 5. Main Content Stack
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = slideUp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo & Brand Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Glassmorphism Logo Card (Drop Shadow, Frosted glass gradient, custom border)
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .offset(y = logoFloatOffset.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                        }
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(32.dp),
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.25f),
                            spotColor = Color.Black.copy(alpha = 0.35f)
                        )
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.40f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_neuralex_logo_vector),
                        contentDescription = "NeuraLex Logo",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Beautiful, High-Impact Header Typography
                Text(
                    text = "NeuraLex",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tagline text with generous line spacing
                Text(
                    text = "Words mean more when you understand them.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 280.dp)
                )
            }

            // Bottom Call-to-Action Controls Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                // High contrast white pill buttons
                NeuraLexButton(
                    text = "Get Started",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    variant = ButtonVariant.White,
                    onClick = onGetStarted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary "Explore Words" text button with premium hover/click feedback
                Text(
                    text = "Explore Words",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onGetStarted)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Modern onboarding pagination indicator dots (First active, matching layout spec)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == 0) 24.dp else 8.dp, 8.dp) // Stylized active dot expand pill
                                .clip(CircleShape)
                                .background(
                                    if (index == 0) Color.White else Color.White.copy(alpha = 0.35f)
                                )
                        )
                    }
                }
            }
        }
    }
}
