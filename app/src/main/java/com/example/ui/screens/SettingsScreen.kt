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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonText = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: ""
                
                val result = onImportFavorites(jsonText)
                if (result.first) {
                    Toast.makeText(context, "Successfully imported", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Import failed. Invalid file", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed. Invalid file", Toast.LENGTH_LONG).show()
            }
        }
    }

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
                                    if (uiState.favorites.isEmpty() || exportedJson == "{}" || exportedJson.isEmpty()) {
                                        Toast.makeText(context, "Export failed. Please try again.", Toast.LENGTH_LONG).show()
                                    } else {
                                        try {
                                            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                                            val fileName = "NeuraLex_${sdf.format(Date())}.json"
                                            
                                            val success = exportJsonToDownloads(context, exportedJson, fileName)
                                            if (success) {
                                                Toast.makeText(context, "Successfully exported to Downloads", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Export failed. Please try again.", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Export failed. Please try again.", Toast.LENGTH_LONG).show()
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
                                        filePickerLauncher.launch("application/json")
                                    } catch (e: Exception) {
                                        try {
                                            filePickerLauncher.launch("*/*")
                                        } catch (ex: Exception) {
                                            Toast.makeText(context, "Import failed. Invalid file", Toast.LENGTH_LONG).show()
                                        }
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

private fun exportJsonToDownloads(context: android.content.Context, jsonContent: String, fileName: String): Boolean {
    return try {
        // Validation: JSON is properly formatted
        try {
            org.json.JSONObject(jsonContent)
        } catch (e: Exception) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonContent.toByteArray())
                }
                true
            } else {
                false
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir == null) return false
            if (!downloadsDir.exists()) {
                if (!downloadsDir.mkdirs()) return false
            }
            val file = File(downloadsDir, fileName)
            file.outputStream().use { outputStream ->
                outputStream.write(jsonContent.toByteArray())
            }
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

data class ThemeOption(
    val label: String,
    val key: String
)
