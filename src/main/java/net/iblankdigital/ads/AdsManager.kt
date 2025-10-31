package net.iblankdigital.ads

import android.app.Activity
import android.content.Context
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

    fun inDelayBetweenAdsShow(): Boolean {
        val admobInDelay = admob?.inDelayBetweenAdsShow() ?: false
        val lovinInDelay = lovin?.inDelayBetweenAdsShow() ?: false
        val unityInDelay = unity?.inDelayBetweenAdsShow() ?: false
        return admobInDelay || lovinInDelay || unityInDelay
    }

    fun canShowAdmob(): Boolean {
        return admob?.canShow() ?: false
    }

    fun showAdmob(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)): Boolean {
        return admob?.show(activity, onDone) ?: false
    }

    fun canShowLovin(): Boolean {
        return lovin?.canShow() ?: false
    }

    fun showLovin(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)): Boolean {
        return lovin?.show(activity, onDone) ?: false
    }

    fun canShowUnity(): Boolean {
        return unity?.canShow() ?: false
    }

    fun showUnity(activity: Activity, onDone: ((success: Boolean, message: String) -> Unit)): Boolean {
        return unity?.show(activity, onDone) ?: false
    }


    val timeToShow: Long
        get() {
            admob?.timeToShow?.let {
                if (it > 0) {
                    return it
                }
            }
            lovin?.timeToShow?.let {
                if (it > 0) {
                    return it
                }
            }
            unity?.timeToShow?.let {
                if (it > 0) {
                    return it
                }
            }
            return 0
        }
}

