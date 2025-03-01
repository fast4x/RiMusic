package it.fast4x.environment.utils

import java.net.InetSocketAddress
import java.net.Proxy

object ProxyPreferences {
    var preference: ProxyPreferenceItem? = null
}

data class ProxyPreferenceItem(
    var proxyHost: String,
    var proxyPort: Int,
    var proxyMode: Proxy.Type
)

fun getProxy(proxyPreference: ProxyPreferenceItem): Proxy {
    return if(proxyPreference.proxyMode == Proxy.Type.DIRECT) {
        Proxy.NO_PROXY
    } else {
        Proxy(
            proxyPreference.proxyMode,
            InetSocketAddress(proxyPreference.proxyHost, proxyPreference.proxyPort)
        )
    }
}
