package net.iblankdigital.ads

private const val DELAY_RETRY = 15000L
private const val DELAY_SHOW = 120000L

data class AdsConfig(
    var adId: String = "",
    var adFullId: String = "",
    var delayShow: Long = DELAY_SHOW,
    var delayRetry: Long = DELAY_RETRY,
    var immersiveMode: Boolean = false
) {
//    override fun toString(): String {
//        return "AdsConfig(adId='$adId', adFullId='$adFullId', delayShow=$delayShow, delayRetry=$delayRetry, immersiveMode=$immersiveMode)"
//    }
}

object AdsConfigs {
    var debug = false
    var logger = ""
    var uuid = ""
    var privacyPolicy = ""
    var termsOfUse = ""

    var admob = AdsConfig(
        adId = "",
        adFullId = "",
        delayShow = DELAY_SHOW,
        delayRetry = DELAY_RETRY
    )

    var lovinSDK = ""
    var lovin = AdsConfig(
        adId = "",
        delayShow = DELAY_SHOW,
        delayRetry = DELAY_RETRY
    )

    var unity = AdsConfig(
        adId = "",
        adFullId = "",
        delayShow = DELAY_SHOW,
        delayRetry = DELAY_RETRY
    )
}

