package com.tini.tuner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    
    private var hasAudioPermission by mutableStateOf(false)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        hasAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        
        setContent {
            TiniTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.Background
                ) {
                    if (hasAudioPermission) {
                        val viewModel: TunerViewModel = viewModel()
                        val uiState by viewModel.uiState.collectAsState()
                        
                        TunerScreen(
                            uiState = uiState,
                            onStartListening = { viewModel.startListening() },
                            onStopListening = { viewModel.stopListening() },
                            onStringSelected = { viewModel.selectString(it) },
                            onAutoModeToggle = { viewModel.setAutoMode(it) },
                            onTuningChanged = { viewModel.setTuning(it) },
                            onA4ReferenceChanged = { viewModel.setA4Reference(it) }
                        )
                    } else {
                        ProfessionalPermissionScreen(
                            onRequestPermission = {
                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    }
                }
            }
        }
    }
}

val ManropeFont = FontFamily(Font(R.font.manrope_regular))

object AppColors {
    val Background = Color(0xFF0F0F14)
    val Surface = Color(0xFF1A1A2E)
    val SurfaceElevated = Color(0xFF252538)
    val PrimaryIndigo = Color(0xFF4F46E5)
    val PrimaryAmber = Color(0xFFF59E0B)
    val AccentGreen = Color(0xFF10B981)
    val AccentRed = Color(0xFFEF4444)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA0A0B8)
}

@Composable
fun TiniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AppColors.PrimaryIndigo,
            secondary = AppColors.PrimaryAmber,
            background = AppColors.Background,
            surface = AppColors.Surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = AppColors.TextPrimary,
            onSurface = AppColors.TextPrimary
        ),
        content = content
    )
}

@Composable
fun ProfessionalPermissionScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.Background, AppColors.Surface)
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(96.dp),
                shape = RoundedCornerShape(24.dp),
                color = AppColors.PrimaryIndigo.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = AppColors.PrimaryIndigo
                    )
                }
            }
            
            // Title
            Text(
                text = "Mikrofon İzni Gerekli",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
                fontFamily = ManropeFont
            )
            
            // Description
            Text(
                text = "Tını, gitarınızın sesini dinleyerek size mükemmel akort sağlar. " +
                        "Mikrofon erişimine izin vererek başlayalım.",
                fontSize = 16.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                fontFamily = ManropeFont,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Permission button
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryIndigo
                )
            ) {
                Text(
                    "Mikrofon İznini Ver",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ManropeFont
                )
            }
        }
    }
}
