package net.iblankdigital.ads


data class AdsConfig(
    var active: Boolean = true,
    var adId: String = "",
    var adFullId: String = "",
    var delayShow: Long = 0L,
    var delayRetry: Long = 10000L
)

object AdsConfigs {
    var debug = true
    var logger = ""

    var uuid = ""
    var privacyPolicy = ""
    var termsOfUse = ""

    var admob = AdsConfig(
        active = true,
        adId = "",
        adFullId = "",
        delayShow = 120000L,
        delayRetry = 10000L
    )

    var lovin = AdsConfig(
        active = false,
        adId = "",
        delayShow = 120000L,
        delayRetry = 10000L
    )

    var unity = AdsConfig(
        active = true,
        adId = "",
        adFullId = "",
        delayShow = 120000L,
        delayRetry = 10000L
    )
}

