package net.iblankdigital.ads.lib

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdk.SdkInitializationListener
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import net.iblankdigital.ads.AdsConfigs
import net.iblankdigital.ads.AdsEvent
import net.iblankdigital.ads.AdsLogger
import androidx.core.net.toUri
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError

internal class AdsLovin(context: Context) : BaseAd(context) {
    override val tag = "AdsLovin"
    override val config = AdsConfigs.lovin

    private var interstitialAd: MaxInterstitialAd? = null
    private val handler = Handler(Looper.getMainLooper())

    private var onDone: ((Boolean, String) -> Unit)? = null

    override fun init() {
        // Create the initialization configuration
        val conf = AppLovinSdkInitializationConfiguration
            .builder(AdsConfigs.lovinSDK)
            .setMediationProvider(AppLovinMediationProvider.MAX)
            .build()

        // Configure the SDK settings if needed before or after SDK initialization.
        val settings = AppLovinSdk.getInstance(context).settings
        settings.userIdentifier = AdsConfigs.uuid
        settings.termsAndPrivacyPolicyFlowSettings.isEnabled = true
        settings.termsAndPrivacyPolicyFlowSettings.privacyPolicyUri = AdsConfigs.privacyPolicy.toUri()
        settings.termsAndPrivacyPolicyFlowSettings.termsOfServiceUri = AdsConfigs.termsOfUse.toUri()

        // Initialize the SDK with the configuration
        AppLovinSdk.getInstance(context).initialize(conf) { sdkConfig: AppLovinSdkConfiguration? ->
            log("initialize adId=${config.adId} adFullId=${config.adFullId}")
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
        interstitialAd = MaxInterstitialAd(config.adFullId)
        interstitialAd!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(maxAd: MaxAd) {
                log("onAdLoaded")
                isLoaded = true
                isLoading = false
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                log("onAdFailedToLoad: ${error.message}")
                reload()
                logEvent(AdsEvent.FailedLoad)
                isLoaded = false
                isLoading = false
            }

            override fun onAdDisplayed(maxAd: MaxAd) {
                logEvent(AdsEvent.Showed)
                logEvent(AdsEvent.Impression)
            }

            override fun onAdHidden(ad: MaxAd) {
                interstitialAd = null
                logEvent(AdsEvent.Dismissed)
                onDone?.invoke(true, AdsEvent.Dismissed.toString())
                reload()
                successShow()
            }

            override fun onAdClicked(ad: MaxAd) {
                logEvent(AdsEvent.Clicked)
            }


            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                interstitialAd = null
                reload()
                logEvent(AdsEvent.FailedShow)
                onDone?.invoke(false, AdsEvent.FailedShow.toString())
            }
        })
        interstitialAd!!.loadAd()
    }

    override fun canShow(): Boolean {
        return interstitialAd != null && isLoaded && !inDelayBetweenAdsShow()
    }

    override fun show(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)): Boolean {
        try {
            this.onDone = onDone
            if (activity.isFinishing || activity.isDestroyed) {
                log("activity not valid to show ad")
                onDone.invoke(false, "$tag activity not valid")
                return false
            }
            if (!config.active) {
                log("not active")
                onDone.invoke(false, "$tag not active")
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
            interstitialAd?.showAd(activity)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDone.invoke(false, AdsEvent.FailedShow.toString())
        return false
    }
}
