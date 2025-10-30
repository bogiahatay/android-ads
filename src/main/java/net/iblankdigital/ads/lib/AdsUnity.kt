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

class AdsUnity(context: Context) : BaseAd(context) {
    override val tag = "AdsUnity"
    override val config = AdsConfigs.unity

    //    private var interstitialAd: MaxInterstitialAd? = null
    private val handler = Handler(Looper.getMainLooper())

    private var onDone: ((Boolean, String) -> Unit)? = null

    override fun init() {
        if (config.active && config.adId.isNotEmpty() && config.adFullId.isNotEmpty()) {
            log("initialize adId=${config.adId} adFullId=${config.adFullId}")
            initUnity()
        } else {
            handler.postDelayed(Runnable { this.init() }, 5000)
        }
    }

    private fun initUnity() {
        UnityAds.initialize(context, config.adId, BuildConfig.DEBUG, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                load()
            }

            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                log("onInitializationFailed $message")
            }
        })
    }

    override fun load() {
        if (isLoading) {
            log("isLoading..")
            return
        }
        isLoaded = false
        isLoading = true

        UnityAds.load(config.adFullId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                log("onAdLoaded")
                isLoaded = true
                isLoading = false
            }

            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                log("onAdFailedToLoad: ${message}")
                reload()
                logEvent(AdsEvent.FailedLoad)
                isLoaded = false
                isLoading = false
            }
        })
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
            if (!isLoaded) {
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
            val options = UnityAdsShowOptions()
            UnityAds.show(activity, config.adFullId, options, object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    reload()
                    logEvent(AdsEvent.FailedShow)
                    onDone?.invoke(false, AdsEvent.FailedShow.toString())
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
                    onDone?.invoke(true, AdsEvent.Dismissed.toString())
                    successShow()
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            onDone?.invoke(false, AdsEvent.FailedShow.toString())
        }
    }
}
