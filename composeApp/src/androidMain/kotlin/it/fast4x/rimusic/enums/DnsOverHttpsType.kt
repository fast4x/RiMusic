package it.fast4x.rimusic.enums

enum class DnsOverHttpsType {
    None,
    Google,
    CloudFlare,
    OpenDns,
    AdGuard,
    Custom;

    val type: String?
    get() = when (this) {
        None -> null
        Google -> "google"
        CloudFlare -> "cloudflare"
        OpenDns -> "opendns"
        AdGuard -> "adguard"
        Custom -> "custom"

    }

    val textName: String
        get() = when(this) {
            None -> "None"
            Google -> "Google Public Dns"
            CloudFlare -> "Cloudflare Public Dns"
            OpenDns -> "OpenDns Public Dns"
            AdGuard -> "AdGuard Public Dns"
            Custom -> "Custom Dns"
        }
}