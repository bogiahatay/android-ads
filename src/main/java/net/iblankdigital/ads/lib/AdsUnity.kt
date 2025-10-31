package net.iblankdigital.ads.lib

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import net.iblankdigital.ads.AdsEvent
import net.iblankdigital.ads.AdsLogger
import androidx.core.net.toUri
import com.unity3d.ads.BuildConfig
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import net.iblankdigital.ads.AdsConfigs

internal class AdsUnity(context: Context) : BaseAd(context) {
    override val tag = "AdsUnity"
    override val config = AdsConfigs.unity

    //    private var interstitialAd: MaxInterstitialAd? = null
    private val handler = Handler(Looper.getMainLooper())
    private var onDone: ((Boolean, String) -> Unit)? = null

    override fun init() {
        UnityAds.initialize(context, config.adId, BuildConfig.DEBUG, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                log("initialize adId=${config.adId} adFullId=${config.adFullId}")
                load()
            }

            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                log("onInitializationFailed $message")
            }
        })
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
        UnityAds.load(config.adFullId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                log("onAdLoaded")
                isLoaded = true
                isLoading = false
            }

            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                log("onAdFailedToLoad: $message")
                reload()
                logEvent(AdsEvent.FailedLoad)
                isLoaded = false
                isLoading = false
            }
        })
    }

    override fun canShow(): Boolean {
        return isLoaded && !inDelayBetweenAdsShow()
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
            if (!isLoaded) {
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
            val options = UnityAdsShowOptions()
            UnityAds.show(activity, config.adFullId, options, object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    reload()
                    logEvent(AdsEvent.FailedShow)
                    onDone.invoke(false, AdsEvent.FailedShow.toString())
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    logEvent(AdsEvent.Showed)
                    logEvent(AdsEvent.Impression)
                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    logEvent(AdsEvent.Clicked)
                }

                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    logEvent(AdsEvent.Dismissed)
                    onDone.invoke(true, AdsEvent.Dismissed.toString())
                    reload()
                    successShow()
                }
            })
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDone.invoke(false, AdsEvent.FailedShow.toString())
        return false
    }
}
