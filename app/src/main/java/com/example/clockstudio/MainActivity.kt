package com.example.clockstudio

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.clockstudio.presentation.ClockStudioNavHost
import com.example.clockstudio.presentation.theme.ClockStudioTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClockStudioTheme {
                ClockStudioNavHost()
            }
        }
    }
}
