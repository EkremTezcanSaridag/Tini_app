package com.tini.tuner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    repository: TuningHistoryRepository
) {
    val history by repository.historyFlow.collectAsState(initial = emptyList())
    var showClearDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(24.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = AppColors.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Akort Geçmişi",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    fontFamily = InterFont
                )
            }
            
            if (history.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Temizle",
                        tint = AppColors.AccentRed
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz akort geçmişi yok",
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    fontFamily = InterFont
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { entry ->
                    HistoryEntryCard(entry)
                }
            }
        }
    }
    
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Geçmişi Temizle", fontFamily = InterFont) },
            text = { Text("Tüm akort geçmişi silinecek. Emin misiniz?", fontFamily = InterFont) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.clearHistory()
                        }
                        showClearDialog = false
                    }
                ) {
                    Text("Sil", color = AppColors.AccentRed, fontFamily = InterFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("İptal", fontFamily = InterFont)
                }
            }
        )
    }
}

@Composable
fun HistoryEntryCard(entry: TuningHistoryEntry) {
    val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale("tr"))
    val date = Date(entry.timestamp)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entry.tuningType} - ${entry.stringName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    fontFamily = InterFont
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(date),
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary,
                    fontFamily = InterFont
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (entry.cents >= 0) "+%.1f¢".format(entry.cents) else "%.1f¢".format(entry.cents),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        entry.wasInTune -> AppColors.AccentGreen
                        entry.cents < 0 -> AppColors.AccentOrange
                        else -> AppColors.AccentRed
                    },
                    fontFamily = InterFont
                )
                Text(
                    text = if (entry.wasInTune) "Akortlu" else if (entry.cents < 0) "Pest" else "Tiz",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    fontFamily = InterFont
                )
            }
        }
    }
}
