package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.state.AppUiState
import com.example.ui.theme.LightPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryTextLight
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: AppUiState,
    onSetTheme: (String) -> Unit,
    onUpdateAi: (String, String, String) -> Unit,
    onTestConnection: () -> Unit,
    onClearConnectionTestResult: () -> Unit,
    onImportFavorites: (String) -> Pair<Boolean, String>,
    onExportFavorites: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var baseUrl by remember { mutableStateOf(uiState.baseUrl) }
    var apiKey by remember { mutableStateOf(uiState.apiKey) }
    var modelName by remember { mutableStateOf(uiState.modelName) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Clear connection result toast on disposal or change
    LaunchedEffect(uiState.connectionTestResult) {
        uiState.connectionTestResult?.let { result ->
            val message = result.second
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onClearConnectionTestResult()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Simple distinct settings header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Appearance Section (Palette selector row style)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Segments button bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val themes = listOf(
                                ThemeOption("Light", "light"),
                                ThemeOption("Dark", "dark"),
                                ThemeOption("System Default", "system")
                            )

                            themes.forEach { option ->
                                val isActive = uiState.themeMode == option.key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isActive) LightPurple else Color.Transparent)
                                        .clickable { onSetTheme(option.key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isActive) PrimaryPurple else SecondaryTextLight,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. AI Integration Configuration Settings
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Integration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
                Text(
                    text = "Configure your preferred AI provider to get word meanings and details.",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SecondaryTextLight,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp, start = 28.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Base URL
                        Column {
                            Text(
                                text = "Base URL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = baseUrl,
                                onValueChange = {
                                    baseUrl = it
                                    onUpdateAi(baseUrl, apiKey, modelName)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // API Key
                        Column {
                            Text(
                                text = "API Key",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = {
                                    apiKey = it
                                    onUpdateAi(baseUrl, apiKey, modelName)
                                },
                                singleLine = true,
                                placeholder = { Text(text = "••••••••••••••••••••••••", color = SecondaryTextLight) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = icon, contentDescription = "Toggle password mask", tint = SecondaryTextLight)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Model Name
                        Column {
                            Text(
                                text = "Model Name",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = modelName,
                                onValueChange = {
                                    modelName = it
                                    onUpdateAi(baseUrl, apiKey, modelName)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Connection test button with custom loader animation
                        Button(
                            onClick = onTestConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = LightPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isTestingConnection
                        ) {
                            if (uiState.isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Test Connection",
                                    color = PrimaryPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Import & Export Configuration Fields
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import & Export",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
                Text(
                    text = "Backup or restore your favorite words.",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SecondaryTextLight,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp, start = 28.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        // Export Favorites row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val exportedJson = onExportFavorites()
                                    if (exportedJson == "[]" || exportedJson.isEmpty()) {
                                        Toast.makeText(context, "Export failed: No favorite words added yet.", Toast.LENGTH_LONG).show()
                                    } else {
                                        try {
                                            // Copy to system clipboard for easy external sharing
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Neuralex Favorites Backup", exportedJson)
                                            clipboard.setPrimaryClip(clip)

                                            // Save to local file in filesDir for app backup resilience
                                            context.openFileOutput("favorites_backup.json", android.content.Context.MODE_PRIVATE).use {
                                                it.write(exportedJson.toByteArray())
                                            }

                                            Toast.makeText(
                                                context,
                                                "Successfully exported!\nData copied to Clipboard & saved to internal backup.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Export Favorites",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = "Save your favorite words to a file",
                                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryTextLight)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Export backups",
                                tint = SecondaryTextLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                        // Import Favorites row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        var localFileContent = ""
                                        // 1. Try reading the local backup file
                                        try {
                                            val backupFile = java.io.File(context.filesDir, "favorites_backup.json")
                                            if (backupFile.exists() && backupFile.isFile) {
                                                localFileContent = backupFile.readText()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                        // 2. Try reading from system clipboard
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clipData = clipboard.primaryClip
                                        val clipboardText = if (clipData != null && clipData.itemCount > 0) {
                                            clipData.getItemAt(0).text?.toString() ?: ""
                                        } else ""

                                        val trimmedClipboard = clipboardText.trim()
                                        val isClipboardJson = trimmedClipboard.startsWith("[") || trimmedClipboard.startsWith("{")

                                        val source: String
                                        val jsonToImport: String

                                        if (isClipboardJson) {
                                            source = "Clipboard backup"
                                            jsonToImport = clipboardText
                                        } else if (localFileContent.isNotEmpty()) {
                                            source = "favorites_backup.json file"
                                            jsonToImport = localFileContent
                                        } else {
                                            source = ""
                                            jsonToImport = ""
                                        }

                                        if (jsonToImport.isEmpty()) {
                                            Toast.makeText(
                                                context,
                                                "Import failed: No valid JSON backup found on Clipboard or local storage.\n\nPlease copy a favorites backup text to clipboard first or export beforehand.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            val result = onImportFavorites(jsonToImport)
                                            if (result.first) {
                                                Toast.makeText(
                                                    context,
                                                    "Successfully imported from $source!\n${result.second}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Error processing $source:\n${result.second}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "System import error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Import Favorites",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = "Restore favorite words from a file",
                                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryTextLight)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Import backups",
                                tint = SecondaryTextLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ThemeOption(
    val label: String,
    val key: String
)
