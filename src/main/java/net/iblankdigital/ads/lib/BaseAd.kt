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

internal abstract class BaseAd(val context: Context) {

    abstract val tag: String
    abstract val config: AdsConfig
    private val handler = Handler(Looper.getMainLooper())

    protected var isLoaded = false
    protected var isLoading = false

    abstract fun init()
    abstract fun load()
    abstract fun show(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)): Boolean
    abstract fun canShow(): Boolean

    protected fun reload() {
        log("reload after ${config.delayRetry}")
        handler.postDelayed({
            load()
        }, config.delayRetry)
    }


    private var lastTimeShow = 0L
    val timeToShow: Long
        get() {
            return config.delayShow - (System.currentTimeMillis() - lastTimeShow)
        }

    fun inDelayBetweenAdsShow(): Boolean {
        val now = System.currentTimeMillis()
        return now - lastTimeShow < config.delayShow
    }

    fun successShow() {
        lastTimeShow = System.currentTimeMillis()
        if (AdsConfigs.debug) {
            Handler(Looper.getMainLooper()).postDelayed({
                log("Có thể show QC")
            }, config.delayShow)
        }
    }

    protected fun log(msg: String) {
        AdsLogger.log("[${tag}] $msg")
    }

    protected fun logEvent(event: AdsEvent) {
        log(event.toString())
        AdsLogger.logEvent(tag, event.toString())
    }

}
