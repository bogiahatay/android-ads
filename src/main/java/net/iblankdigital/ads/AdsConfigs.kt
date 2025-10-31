package net.iblankdigital.ads

private const val DELAY_RETRY = 15000L
private const val DELAY_SHOW = 120000L

data class AdsConfig(
    var active: Boolean = true,
    var adId: String = "",
    var adFullId: String = "",
    var delayShow: Long = DELAY_SHOW,
    var delayRetry: Long = DELAY_RETRY
)

object AdsConfigs {
    var debug = true
    var logger = ""
    var uuid = ""
    var privacyPolicy = ""
    var termsOfUse = ""

    var admob = AdsConfig(
        active = false,
        adId = "",
        adFullId = "",
        delayShow = DELAY_SHOW,
        delayRetry = DELAY_RETRY
    )

    var lovinSDK = ""
    var lovin = AdsConfig(
        active = false,
        adId = "",
        delayShow = DELAY_SHOW,
        delayRetry = DELAY_RETRY
    )

    var unity = AdsConfig(
        active = false,
        adId = "",
        adFullId = "",
        delayShow = DELAY_SHOW,
        delayRetry = DELAY_RETRY
    )
}

