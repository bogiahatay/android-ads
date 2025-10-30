package net.iblankdigital.ads.lib

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import net.iblankdigital.ads.AdsConfigs
import net.iblankdigital.ads.AdsEvent
import net.iblankdigital.ads.AdsLogger

class AdsAdmob(context: Context) : BaseAd(context) {
    override val tag = "AdsAdmob"
    override val config = AdsConfigs.admob

    private var interstitialAd: InterstitialAd? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun init() {
        if (config.active && config.adId.isNotEmpty() && config.adFullId.isNotEmpty()) {
            log("initialize adId=${config.adId} adFullId=${config.adFullId}")
            initAdmob()
        } else {
            handler.postDelayed(Runnable { this.init() }, 5000)
        }
    }

    private fun initAdmob() {
        MobileAds.initialize(context) { initializationStatus: InitializationStatus? -> load() }
    }

    override fun load() {
        if (isLoading) {
            log("isLoading..")
            return
        }
        isLoaded = false
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, config.adFullId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                log("onAdLoaded")
                interstitialAd = ad
                interstitialAd?.setImmersiveMode(true)

                isLoaded = true
                isLoading = false
            }

            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                log("onAdFailedToLoad: ${error.message}")
                reload()
                logEvent(AdsEvent.FailedLoad)

                isLoaded = false
                isLoading = false
            }
        })
    }

    override fun show(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)?) {
        try {
            if (activity.isFinishing || activity.isDestroyed) {
                log("activity not valid to show ad")
                onDone?.invoke(false, "$tag activity not valid")
                return
            }

            if (!config.active) {
                log("not active")
                onDone?.invoke(false, "$tag not active")
                return
            }
            if (interstitialAd == null || !isLoaded) {
                log("not ready, retry loading...")
                reload()
                onDone?.invoke(false, "$tag not ready, retry loading...")
                return
            }
            if (inDelayBetweenAdsShow()) {
                log("inDelayBetweenAdsShow ")
                onDone?.invoke(false, "$tag in delay interval between ads")
                return
            }
            interstitialAd?.apply {
                fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        interstitialAd = null
                        logEvent(AdsEvent.Dismissed)
                        onDone?.invoke(true, AdsEvent.Dismissed.toString())
                        successShow()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        interstitialAd = null
                        reload()
                        logEvent(AdsEvent.FailedShow)
                        onDone?.invoke(false, AdsEvent.FailedShow.toString())
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        logEvent(AdsEvent.Clicked)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        logEvent(AdsEvent.Impression)
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        logEvent(AdsEvent.Showed)
                    }
                }
                show(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onDone?.invoke(false, AdsEvent.FailedShow.toString())
        }
    }
}
