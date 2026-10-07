package com.example.clockstudio.presentation.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockstudio.R
import com.example.clockstudio.ads.AdsConfiguration
import com.example.clockstudio.presentation.components.ScreenTopBar

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = { ScreenTopBar(stringResource(R.string.privacy_title), onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(R.string.privacy_intro), color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.privacy_local_data), color = Color(0xFFBDBDBD), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(if (AdsConfiguration.ADS_ENABLED) R.string.privacy_ads_enabled else R.string.privacy_ads_disabled),
                color = Color(0xFFBDBDBD),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(stringResource(R.string.privacy_account_note), color = Color(0xFF00BFA5), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
