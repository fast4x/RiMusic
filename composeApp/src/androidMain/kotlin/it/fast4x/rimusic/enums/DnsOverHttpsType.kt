package it.fast4x.rimusic.enums

enum class DnsOverHttpsType {
    None,
    Google,
    CloudFlare,
    OpenDns;

    val type: String?
    get() = when (this) {
        None -> null
        Google -> "google"
        CloudFlare -> "cloudflare"
        OpenDns -> "opendns"
    }

    val textName: String
        get() = when(this) {
            None -> "None"
            Google -> "Google Public Dns"
            CloudFlare -> "Cloudflare Public Dns"
            OpenDns -> "OpenDns Public Dns"
        }
}