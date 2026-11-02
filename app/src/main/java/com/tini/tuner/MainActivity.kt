package com.tini.tuner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.foundation.isSystemInDarkTheme

enum class Screen {
    Tuner,
    Metronome,
    History
}

class MainActivity : ComponentActivity() {
    
    private var hasAudioPermission by mutableStateOf(false)
    private var viewModel: TunerViewModel? = null
    private lateinit var historyRepository: TuningHistoryRepository
    private lateinit var themeRepository: ThemeRepository
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            // Permission denied - stop any ongoing listening
            viewModel?.stopListening()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        historyRepository = TuningHistoryRepository(applicationContext)
        themeRepository = ThemeRepository(applicationContext)
        
        hasAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        
        setContent {
            val themeMode by themeRepository.themeModeFlow.collectAsState(initial = ThemeMode.LIGHT)
            val isSystemInDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDark
            }
            
            TiniTheme(isDark = isDark) {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Tuner) }
                
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = AppColors.Background
                ) {
                    when (currentScreen) {
                        Screen.Tuner -> {
                            if (hasAudioPermission) {
                                val vm: TunerViewModel = viewModel()
                                viewModel = vm
                                
                                // Set history repository in ViewModel
                                LaunchedEffect(Unit) {
                                    vm.setHistoryRepository(historyRepository)
                                }
                                
                                val uiState by vm.uiState.collectAsState()
                                
                                TunerScreen(
                                    uiState = uiState,
                                    onStartListening = { vm.startListening() },
                                    onStopListening = { vm.stopListening() },
                                    onStringSelected = { vm.selectString(it) },
                                    onAutoModeToggle = { vm.setAutoMode(it) },
                                    onTuningChanged = { vm.setTuning(it) },
                                    onA4ReferenceChanged = { vm.setA4Reference(it) },
                                    onMetronomeClick = { currentScreen = Screen.Metronome },
                                    onHistoryClick = { currentScreen = Screen.History },
                                    themeRepository = themeRepository
                                )
                            } else {
                                ProfessionalPermissionScreen(
                                    onRequestPermission = {
                                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                )
                            }
                        }
                        Screen.Metronome -> {
                            MetronomeScreen(
                                onBackClick = { currentScreen = Screen.Tuner }
                            )
                        }
                        Screen.History -> {
                            HistoryScreen(
                                onBackClick = { currentScreen = Screen.Tuner },
                                repository = historyRepository
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        // Stop audio capture when app goes to background
        viewModel?.stopListening()
    }
    
    override fun onStart() {
        super.onStart()
        // Resume if we had permission and were listening before
        if (hasAudioPermission) {
            viewModel?.startListening()
        }
    }
}

val InterFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_light, FontWeight.Light)
)

interface AppColorsScheme {
    val Background: Color
    val Surface: Color
    val SurfaceSecondary: Color
    val TextPrimary: Color
    val TextSecondary: Color
    val AccentBlue: Color
    val AccentGreen: Color
    val AccentOrange: Color
    val AccentRed: Color
    val Separator: Color
}

object AppColorsLight : AppColorsScheme {
    override val Background = Color(0xFFFAFAFA)
    override val Surface = Color(0xFFFFFFFF)
    override val SurfaceSecondary = Color(0xFFF5F5F7)
    override val TextPrimary = Color(0xFF1D1D1F)
    override val TextSecondary = Color(0xFF86868B)
    override val AccentBlue = Color(0xFF007AFF)
    override val AccentGreen = Color(0xFF34C759)
    override val AccentOrange = Color(0xFFFF9500)
    override val AccentRed = Color(0xFFFF3B30)
    override val Separator = Color(0xFFE5E5E5)
}

object AppColorsDark : AppColorsScheme {
    override val Background = Color(0xFF000000)
    override val Surface = Color(0xFF1C1C1E)
    override val SurfaceSecondary = Color(0xFF2C2C2E)
    override val TextPrimary = Color(0xFFFFFFFF)
    override val TextSecondary = Color(0xFF98989D)
    override val AccentBlue = Color(0xFF007AFF)
    override val AccentGreen = Color(0xFF34C759)
    override val AccentOrange = Color(0xFFFF9500)
    override val AccentRed = Color(0xFFFF3B30)
    override val Separator = Color(0xFF38383A)
}

private val LocalIsDark = compositionLocalOf { false }

val AppColors: AppColorsScheme
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDark.current) AppColorsDark else AppColorsLight

@Composable
fun TiniTheme(isDark: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (isDark) AppColorsDark else AppColorsLight
    
    CompositionLocalProvider(LocalIsDark provides isDark) {
        MaterialTheme(
            colorScheme = if (isDark) {
                darkColorScheme(
                    primary = colors.AccentBlue,
                    secondary = colors.AccentGreen,
                    background = colors.Background,
                    surface = colors.Surface,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = colors.TextPrimary,
                    onSurface = colors.TextPrimary
                )
            } else {
                lightColorScheme(
                    primary = colors.AccentBlue,
                    secondary = colors.AccentGreen,
                    background = colors.Background,
                    surface = colors.Surface,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = colors.TextPrimary,
                    onSurface = colors.TextPrimary
                )
            },
            content = content
        )
    }
}

@Composable
fun ProfessionalPermissionScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Minimal microphone icon
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AppColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title - clean and simple
            Text(
                text = "Mikrofon İzni Gerekli",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
                fontFamily = InterFont
            )
            
            // Description
            Text(
                text = "Tını, gitarınızın sesini dinleyerek size mükemmel akort sağlar.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                fontFamily = InterFont,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Clean iOS-style button
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "İzin Ver",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFont
                )
            }
        }
    }
}
