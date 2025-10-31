package net.iblankdigital.ads

import android.util.Log

internal enum class AdsEvent {
    Clicked, FailedShow, Showed, Impression, Dismissed, FailedLoad
}

internal object AdsLogger {
    var arrLog: ArrayList<String> = arrayListOf()

    fun logEvent(tag: String, event: String) {
        arrLog.add("$tag|$event")
    }

    fun log(msg: String) {
        if (AdsConfigs.debug) {
            Log.e("iBlank ADS Module", msg)
        }
    }
}
