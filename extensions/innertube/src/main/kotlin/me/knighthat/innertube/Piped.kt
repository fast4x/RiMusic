package me.knighthat.innertube

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.brotli
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import it.fast4x.innertube.utils.ProxyPreferences
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.net.InetSocketAddress
import java.net.Proxy

object Piped {

    val client = HttpClient(OkHttp) {
        BrowserUserAgent()

        expectSuccess = true

        install(ContentNegotiation) {
            @OptIn(ExperimentalSerializationApi::class)
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            brotli()
        }

        ProxyPreferences.preference?.let {
            engine {
                proxy = Proxy(
                    it.proxyMode,
                    InetSocketAddress(
                        it.proxyHost,
                        it.proxyPort
                    )
                )
            }
        }

        defaultRequest {
            url(scheme = "https", host ="pipedapi.tokhmi.xyz") {
                headers.append( HttpHeaders.Accept, "*/*" )
            }
        }
    }

}