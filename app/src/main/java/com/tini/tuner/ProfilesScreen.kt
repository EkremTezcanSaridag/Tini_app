package com.tini.tuner

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfilesScreen(
    onBackClick: () -> Unit,
    repository: ProfileRepository,
    currentUiState: TunerUiState,
    currentTheme: ThemeMode,
    onSaveCurrent: () -> Unit
) {
    val profiles by repository.profilesFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val colors = AppColors
    var showSaveDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("Profilim") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TiniSubScreenTopBar(title = "Profiller", onBackClick = onBackClick, modifier = Modifier.weight(1f))
            TextButton(onClick = { showSaveDialog = true }) {
                Text("Kaydet", color = colors.AccentBlue, fontFamily = InterFont)
            }
        }

        if (profiles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Henüz profil yok", color = colors.TextSecondary, fontFamily = InterFont)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(profiles) { profile ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.Surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(profile.name, fontWeight = FontWeight.SemiBold, color = colors.TextPrimary, fontFamily = InterFont)
                                Text(
                                    "${profile.instrumentName} · ${profile.tuningName} · A4 ${profile.a4Reference.toInt()} Hz",
                                    fontSize = 13.sp,
                                    color = colors.TextSecondary,
                                    fontFamily = InterFont
                                )
                                Text(
                                    SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(Date(profile.createdAtMs)),
                                    fontSize = 12.sp,
                                    color = colors.TextSecondary,
                                    fontFamily = InterFont
                                )
                            }
                            IconButton(onClick = {
                                val file = repository.exportToJsonFile(profile)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Profili paylaş"))
                            }) {
                                Icon(Icons.Default.Share, "Paylaş", tint = colors.AccentBlue)
                            }
                            IconButton(onClick = {
                                scope.launch { repository.deleteProfile(profile.id) }
                            }) {
                                Icon(Icons.Default.Delete, "Sil", tint = colors.AccentRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Profil Kaydet", fontFamily = InterFont) },
            text = {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profil adı") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.saveProfile(
                            repository.createFromState(profileName, currentUiState, currentTheme)
                        )
                    }
                    showSaveDialog = false
                    onSaveCurrent()
                }) {
                    Text("Kaydet", fontFamily = InterFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("İptal", fontFamily = InterFont)
                }
            }
        )
    }
}
