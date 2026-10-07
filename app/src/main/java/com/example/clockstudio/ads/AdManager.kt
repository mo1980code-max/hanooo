package com.example.clockstudio.ads

import android.app.Activity
import android.content.Context
import com.example.clockstudio.BuildConfig
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/** App-facing seam for an optional banner provider; UI never depends on SDK loading details. */
interface AdManager {
    fun createBanner(context: Context, size: AdSize): AdView
    fun preloadInterstitial(context: Context)
    /** Call only at an intentional break point; the UI currently never invokes this method. */
    fun showInterstitial(activity: Activity, onDismissed: () -> Unit)
}

object AdsConfiguration {
    val ADS_ENABLED: Boolean = BuildConfig.ADS_ENABLED
    const val TEST_BANNER_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_APPLICATION_ID: String = "ca-app-pub-3940256099942544~3347511713"
}
