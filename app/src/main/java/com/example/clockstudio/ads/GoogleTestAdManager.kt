package com.example.clockstudio.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/** Google Mobile Ads adapter intentionally wired to Google's official test units only. */
object GoogleTestAdManager : AdManager {
    private val initializationStarted = AtomicBoolean(false)
    private val interstitialLoading = AtomicBoolean(false)

    @Volatile private var interstitial: InterstitialAd? = null

    override fun createBanner(context: Context, size: AdSize): AdView {
        ensureInitialized(context)
        return AdView(context).apply {
            adUnitId = AdsConfiguration.TEST_BANNER_AD_UNIT_ID
            setAdSize(size)
            loadAd(AdRequest.Builder().build())
        }
    }

    override fun preloadInterstitial(context: Context) {
        if (!AdsConfiguration.ADS_ENABLED || interstitial != null || !interstitialLoading.compareAndSet(false, true)) return
        ensureInitialized(context)
        InterstitialAd.load(
            context.applicationContext,
            AdsConfiguration.TEST_INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    interstitialLoading.set(false)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    interstitialLoading.set(false)
                }
            },
        )
    }

    override fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        if (!AdsConfiguration.ADS_ENABLED) {
            onDismissed()
            return
        }
        val readyAd = interstitial
        if (readyAd == null) {
            preloadInterstitial(activity)
            onDismissed()
            return
        }
        interstitial = null
        readyAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadInterstitial(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preloadInterstitial(activity)
                onDismissed()
            }
        }
        readyAd.show(activity)
    }

    private fun ensureInitialized(context: Context) {
        if (initializationStarted.compareAndSet(false, true)) {
            MobileAds.initialize(context.applicationContext)
        }
    }
}
