package com.tini.tuner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    
    private var hasAudioPermission by mutableStateOf(false)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        hasAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        
        setContent {
            TiniTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1A1A1A)
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
                        PermissionRequestScreen(
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

@Composable
fun TiniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF4CAF50),
            secondary = Color(0xFF2196F3),
            background = Color(0xFF1A1A1A),
            surface = Color(0xFF2A2A2A)
        ),
        content = content
    )
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mikrofon İzni Gerekli",
            fontSize = 24.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tını, gitarınızı akort etmek için mikrofonunuzu kullanarak ses frekansını algılamalıdır. " +
                    "Lütfen mikrofon iznini verin.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("İzin Ver", fontSize = 18.sp)
        }
    }
}
