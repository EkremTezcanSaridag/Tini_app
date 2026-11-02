package com.tini.tuner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot tests for the Tını tuner UI using Roborazzi + Robolectric.
 * These generate REAL renders of the Compose UI, not mockups.
 * 
 * These tests run locally only (NATIVE graphics mode requires display).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w393dp-h851dp-xxhdpi")
class TunerScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tunerScreen_idle_noAudio() {
        composeTestRule.setContent {
            TiniTheme {
                TunerScreen(
                    uiState = TunerUiState(
                        isListening = true,
                        selectedStringIndex = 0,
                        currentTuning = TuningType.STANDARD,
                        autoMode = false,
                        detectedFrequency = null,
                        tuningState = null,
                        a4Reference = 440f
                    ),
                    onStartListening = {},
                    onStopListening = {},
                    onStringSelected = {},
                    onAutoModeToggle = {},
                    onTuningChanged = {},
                    onA4ReferenceChanged = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun tunerScreen_inTune_E2() {
        composeTestRule.setContent {
            TiniTheme {
                TunerScreen(
                    uiState = TunerUiState(
                        isListening = true,
                        selectedStringIndex = 0,
                        currentTuning = TuningType.STANDARD,
                        autoMode = false,
                        detectedFrequency = 82.41f,
                        tuningState = TuningState(
                            detectedFrequency = 82.41f,
                            targetFrequency = 82.41f,
                            note = "E2",
                            cents = 0f,
                            direction = TuningDirection.IN_TUNE,
                            isInTune = true
                        ),
                        a4Reference = 440f
                    ),
                    onStartListening = {},
                    onStopListening = {},
                    onStringSelected = {},
                    onAutoModeToggle = {},
                    onTuningChanged = {},
                    onA4ReferenceChanged = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun tunerScreen_tooLow_E2_minus25cents() {
        composeTestRule.setContent {
            TiniTheme {
                TunerScreen(
                    uiState = TunerUiState(
                        isListening = true,
                        selectedStringIndex = 0,
                        currentTuning = TuningType.STANDARD,
                        autoMode = false,
                        detectedFrequency = 80.5f,
                        tuningState = TuningState(
                            detectedFrequency = 80.5f,
                            targetFrequency = 82.41f,
                            note = "E2",
                            cents = -25f,
                            direction = TuningDirection.TOO_LOW,
                            isInTune = false
                        ),
                        a4Reference = 440f
                    ),
                    onStartListening = {},
                    onStopListening = {},
                    onStringSelected = {},
                    onAutoModeToggle = {},
                    onTuningChanged = {},
                    onA4ReferenceChanged = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun tunerScreen_tooHigh_A2_plus30cents() {
        composeTestRule.setContent {
            TiniTheme {
                TunerScreen(
                    uiState = TunerUiState(
                        isListening = true,
                        selectedStringIndex = 1,
                        currentTuning = TuningType.STANDARD,
                        autoMode = false,
                        detectedFrequency = 115.2f,
                        tuningState = TuningState(
                            detectedFrequency = 115.2f,
                            targetFrequency = 110.0f,
                            note = "A2",
                            cents = 30f,
                            direction = TuningDirection.TOO_HIGH,
                            isInTune = false
                        ),
                        a4Reference = 440f
                    ),
                    onStartListening = {},
                    onStopListening = {},
                    onStringSelected = {},
                    onAutoModeToggle = {},
                    onTuningChanged = {},
                    onA4ReferenceChanged = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun tunerScreen_allStrings_Standard() {
        composeTestRule.setContent {
            TiniTheme {
                TunerScreen(
                    uiState = TunerUiState(
                        isListening = true,
                        selectedStringIndex = 3,
                        currentTuning = TuningType.STANDARD,
                        autoMode = true,
                        detectedFrequency = 196.0f,
                        tuningState = TuningState(
                            detectedFrequency = 196.0f,
                            targetFrequency = 196.0f,
                            note = "G3",
                            cents = -5f,
                            direction = TuningDirection.TOO_LOW,
                            isInTune = false
                        ),
                        a4Reference = 440f
                    ),
                    onStartListening = {},
                    onStopListening = {},
                    onStringSelected = {},
                    onAutoModeToggle = {},
                    onTuningChanged = {},
                    onA4ReferenceChanged = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun tunerScreen_DADGAD_tuning() {
        composeTestRule.setContent {
            TiniTheme {
                TunerScreen(
                    uiState = TunerUiState(
                        isListening = true,
                        selectedStringIndex = 0,
                        currentTuning = TuningType.DADGAD,
                        autoMode = false,
                        detectedFrequency = 73.42f,
                        tuningState = TuningState(
                            detectedFrequency = 73.42f,
                            targetFrequency = 73.42f,
                            note = "D2",
                            cents = 2f,
                            direction = TuningDirection.IN_TUNE,
                            isInTune = true
                        ),
                        a4Reference = 440f
                    ),
                    onStartListening = {},
                    onStopListening = {},
                    onStringSelected = {},
                    onAutoModeToggle = {},
                    onTuningChanged = {},
                    onA4ReferenceChanged = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }
    
    @Test
    fun permissionScreen() {
        composeTestRule.setContent {
            TiniTheme {
                ProfessionalPermissionScreen(
                    onRequestPermission = {}
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }
    
    @Test
    fun appIcon() {
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(108.dp)
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }
}

@Composable
private fun TiniTheme(content: @Composable () -> Unit) {
    androidx.compose.material3.MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = AppColors.AccentBlue,
            secondary = AppColors.AccentGreen,
            background = AppColors.Background,
            surface = AppColors.Surface,
            onPrimary = androidx.compose.ui.graphics.Color.White,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            onBackground = AppColors.TextPrimary,
            onSurface = AppColors.TextPrimary
        ),
        content = content
    )
}
