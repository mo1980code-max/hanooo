package com.example.clockstudio.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.unit.sp
import com.example.clockstudio.R
import com.example.clockstudio.data.preferences.UserPreferencesRepository
import com.example.clockstudio.presentation.components.ClockLogo
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    preferencesRepository: UserPreferencesRepository,
    onInitialized: () -> Unit,
) {
    LaunchedEffect(preferencesRepository) {
        // Waiting for DataStore's first read is the initialization gate; no artificial splash delay.
        val settings = preferencesRepository.settingsFlow.first()
        preferencesRepository.configurationFlow.first()
        preferencesRepository.favoritesFlow.first()
        val requestedTag = settings.languageTag.ifBlank { "en" }
        val activeTag = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore(',')
        if (!activeTag.equals(requestedTag, ignoreCase = true)) {
            // AppCompat recreates this Activity after applying the persisted per-app locale.
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(requestedTag))
        } else {
            onInitialized()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101417)),
    ) {
        ClockLogo(
            modifier = Modifier.align(Alignment.Center),
            logoSize = 172.dp,
            contentDescription = stringResource(R.string.clock_preview_description),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.splash_title),
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                color = Color(0xFFBDBDBD),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(22.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFF00BFA5),
                strokeWidth = 2.dp,
            )
        }
    }
}
