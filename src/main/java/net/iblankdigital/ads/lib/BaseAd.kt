package net.iblankdigital.ads.lib

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import net.iblankdigital.ads.AdsConfig
import net.iblankdigital.ads.AdsConfigs
import net.iblankdigital.ads.AdsEvent
import net.iblankdigital.ads.AdsLogger

abstract class BaseAd(val context: Context) {

    abstract val tag: String
    abstract val config: AdsConfig
    private val handler = Handler(Looper.getMainLooper())

    protected var isLoaded = false
    protected var isLoading = false

    abstract fun init()
    abstract fun load()
    abstract fun show(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)? = null)

    protected fun reload() {
        handler.postDelayed({
            load()
        }, config.delayRetry)
    }


    private var lastTimeShow = 0L

    protected fun inDelayBetweenAdsShow(): Boolean {
        val now = System.currentTimeMillis()
        return now - lastTimeShow < config.delayShow
    }

    protected fun successShow() {
        lastTimeShow = System.currentTimeMillis()
    }

    protected fun log(msg: String) {
        if (AdsConfigs.debug) {
            Log.e("iBlankDigital-ADS", "[${tag}] $msg")
        }
    }

    protected fun logEvent(event: AdsEvent) {
        log(event.toString())
        AdsLogger.log(tag, event.toString())
    }

}
