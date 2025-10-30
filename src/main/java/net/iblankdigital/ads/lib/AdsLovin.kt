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

class AdsLovin(context: Context) : BaseAd(context) {
    override val tag = "AdsLovin"
    override val config = AdsConfigs.lovin

    private var interstitialAd: MaxInterstitialAd? = null
    private val handler = Handler(Looper.getMainLooper())

    private var onDone: ((Boolean, String) -> Unit)? = null

    override fun init() {
        if (config.active && config.adId.isNotEmpty() && config.adFullId.isNotEmpty()) {
            log("initialize adId=${config.adId} adFullId=${config.adFullId}")
            initLovin()
        } else {
            handler.postDelayed(Runnable { this.init() }, 5000)
        }
    }

    private fun initLovin() {
        // Create the initialization configuration
        val conf = AppLovinSdkInitializationConfiguration
            .builder("N3WUfruFWgaTyTXFOhkBN331CW6ZUbAmpG7g9qAqv5CZrIH_3MLxI_QQKNEikoy6OUrup5QlWWWSFFqP1uoxXU")
            .setMediationProvider(AppLovinMediationProvider.MAX)
            .build()

        // Configure the SDK settings if needed before or after SDK initialization.
        val settings = AppLovinSdk.getInstance(context).settings
        settings.userIdentifier = AdsConfigs.uuid
        settings.termsAndPrivacyPolicyFlowSettings.isEnabled = true
        settings.termsAndPrivacyPolicyFlowSettings.privacyPolicyUri = AdsConfigs.privacyPolicy.toUri()
        settings.termsAndPrivacyPolicyFlowSettings.termsOfServiceUri = AdsConfigs.termsOfUse.toUri()

        // Initialize the SDK with the configuration
        AppLovinSdk.getInstance(context).initialize(conf) { sdkConfig: AppLovinSdkConfiguration? -> load() }
    }

    override fun load() {
        if (isLoading) {
            log("isLoading..")
            return
        }
        isLoaded = false
        isLoading = true

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

    override fun show(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)?) {
        try {
            this.onDone = onDone
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
            interstitialAd?.showAd(activity)
        } catch (e: Exception) {
            e.printStackTrace()
            onDone?.invoke(false, AdsEvent.FailedShow.toString())
        }
    }
}
