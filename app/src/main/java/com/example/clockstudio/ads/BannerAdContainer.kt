package com.example.clockstudio.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdContainer(modifier: Modifier = Modifier) {
    if (!AdsConfiguration.ADS_ENABLED) return
    val context = LocalContext.current
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        val widthDp = with(density) { maxWidth.roundToPx().toFloat().div(density).toInt() }.coerceAtLeast(1)
        val adSize = remember(context, widthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }
        val adHeight = adSize.getHeightInDp(context).dp
        var view by remember { mutableStateOf<AdView?>(null) }
        AndroidView(
            factory = { currentContext ->
                GoogleTestAdManager.createBanner(currentContext, adSize).also { view = it }
            },
            modifier = Modifier.fillMaxWidth().height(adHeight),
            update = { adView -> if (view !== adView) view = adView },
        )
        DisposableEffect(view) {
            onDispose { view?.destroy() }
        }
    }
}
