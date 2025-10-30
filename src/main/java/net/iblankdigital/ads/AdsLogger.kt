package net.iblankdigital.ads


enum class AdsEvent {
    Clicked, FailedShow, Showed, Impression, Dismissed, FailedLoad
}

object AdsLogger {

    var arrLog: ArrayList<String> = arrayListOf()

    fun log(tag: String, event: String) {
        arrLog.add("$tag|$event")
    }
}