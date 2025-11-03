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

internal class AdsAdmob(context: Context) : BaseAd(context) {
    override val tag = "AdsAdmob"
    override val config = AdsConfigs.admob
    private var interstitialAd: InterstitialAd? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun init() {
        MobileAds.initialize(context) { initializationStatus: InitializationStatus? ->
            log("initialize $config")
            load()
        }
    }

    override fun load() {
        if (config.adFullId.isEmpty()) {
            handler.postDelayed(Runnable { this.load() }, 5000)
            return
        }
        if (isLoading) {
            log("isLoading..")
            return
        }
        isLoaded = false
        isLoading = true
        log("load: ${config.adFullId}")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, config.adFullId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                log("onAdLoaded: immersiveMode=${config.immersiveMode}")
                interstitialAd = ad
                if (config.immersiveMode) {
                    interstitialAd?.setImmersiveMode(true)
                }

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

    override fun canShow(): Boolean {
        return interstitialAd != null && isLoaded && !inDelayBetweenAdsShow()
    }

    override fun show(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)): Boolean {
        try {
            if (activity.isFinishing || activity.isDestroyed) {
                log("activity not valid to show ad")
                onDone.invoke(false, "$tag activity not valid")
                return false
            }
            if (config.adFullId.isEmpty()) {
                log("not active adFullId isEmpty")
                onDone.invoke(false, "not active adFullId isEmpty")
                return false
            }
            if (interstitialAd == null || !isLoaded) {
                log("not ready, retry loading...")
                reload()
                onDone.invoke(false, "$tag not ready, retry loading...")
                return false
            }
            if (inDelayBetweenAdsShow()) {
                log("inDelayBetweenAdsShow ")
                onDone.invoke(false, "$tag in delay interval between ads")
                return false
            }
            interstitialAd?.apply {
                fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        interstitialAd = null
                        logEvent(AdsEvent.Dismissed)
                        onDone.invoke(true, AdsEvent.Dismissed.toString())
                        reload()
                        successShow()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        interstitialAd = null
                        reload()
                        logEvent(AdsEvent.FailedShow)
                        onDone.invoke(false, AdsEvent.FailedShow.toString())
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
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDone.invoke(false, AdsEvent.FailedShow.toString())
        return false
    }

}
