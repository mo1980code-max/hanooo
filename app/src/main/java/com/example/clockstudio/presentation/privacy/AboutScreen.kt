package com.example.clockstudio.presentation.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockstudio.R
import com.example.clockstudio.presentation.components.ClockLogo
import com.example.clockstudio.presentation.components.ScreenTopBar

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = { ScreenTopBar(stringResource(R.string.about_title), onBack) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ClockLogo(logoSize = 112.dp, contentDescription = stringResource(R.string.splash_title))
            Text(stringResource(R.string.splash_title), style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBDBDBD))
            Text(stringResource(R.string.privacy_account_note), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF00BFA5))
        }
    }
}
