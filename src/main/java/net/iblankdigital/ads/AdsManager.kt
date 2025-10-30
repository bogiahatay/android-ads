package com.yourname.adslib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import kotlinx.coroutines.*
import net.iblankdigital.ads.AdsConfigs
import net.iblankdigital.ads.lib.AdsAdmob
import net.iblankdigital.ads.lib.AdsLovin
import net.iblankdigital.ads.lib.AdsUnity

class AdsManager {

    private var admob: AdsAdmob? = null
    private var lovin: AdsLovin? = null
    private var unity: AdsUnity? = null

    fun init(context: Context) {
        initAds(context)
    }

    private fun initAds(context: Context) {
        admob ?: run {
            admob = AdsAdmob(context).apply { init() }
        }
        lovin ?: run {
            lovin = AdsLovin(context).apply { init() }
        }
        unity ?: run {
            unity = AdsUnity(context).apply { init() }
        }
    }
}

