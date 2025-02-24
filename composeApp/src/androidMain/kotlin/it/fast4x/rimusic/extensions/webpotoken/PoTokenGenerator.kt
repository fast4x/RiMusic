package com.dd3boh.outertune.utils.potoken

import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import it.fast4x.innertube.Innertube
import it.fast4x.rimusic.context
import it.fast4x.rimusic.isDebugModeEnabled
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class PoTokenGenerator {
    private val TAG = PoTokenGenerator::class.simpleName
    private val webViewSupported by lazy { runCatching { CookieManager.getInstance() }.isSuccess }
    private var webViewBadImpl = false // whether the system has a bad WebView implementation

    private object WebPoTokenGenLock
    private var webPoTokenSessionIdentifier: String? = null
    private var webPoTokenStreamingPot: String? = null
    private var webPoTokenGenerator: PoTokenWebView? = null

    fun getWebClientPoToken(videoId: String): PoTokenResult? {
        if (!webViewSupported || webViewBadImpl) {
            return null
        }

        try {
            return getWebClientPoToken(videoId, false)
        } catch (e: Exception) {
            when (e) {
                is BadWebViewException -> {
                    if (TAG != null) {
                        Timber.tag(TAG).e(e, "Could not obtain poToken because WebView is broken")
                    }
                    webViewBadImpl = true
                    return null
                }
                else -> throw e // includes PoTokenException
            }
        }
    }

    /**
     * @param forceRecreate whether to force the recreation of [webPoTokenGenerator], to be used in
     * case the current [webPoTokenGenerator] threw an error last time
     * [PoTokenGenerator.generatePoToken] was called
     */
    private fun getWebClientPoToken(videoId: String, forceRecreate: Boolean): PoTokenResult {
        // just a helper class since Kotlin does not have builtin support for 4-tuples
        data class Quadruple<T1, T2, T3, T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4)

        val (poTokenGenerator, sessionIdentifier, streamingPot, hasBeenRecreated) =
            synchronized(WebPoTokenGenLock) {
                val shouldRecreate = webPoTokenGenerator == null || forceRecreate || webPoTokenGenerator!!.isExpired()

                if (shouldRecreate) {
                    webPoTokenSessionIdentifier = if (Innertube.cookie != null) {
                        // signed in sessions use dataSyncId as identifier
                        Innertube.dataSyncId
                    } else {
                        // signed out sessions use visitorData as identifier
                        Innertube.visitorData
                    }

                    if (webPoTokenSessionIdentifier == null) {
                        throw PoTokenException("Session identifier is null")
                    }

                    runBlocking {
                        // close the current webPoTokenGenerator on the main thread
                        webPoTokenGenerator?.let { Handler(Looper.getMainLooper()).post { it.close() } }

                        // create a new webPoTokenGenerator
                        webPoTokenGenerator = PoTokenWebView
                            .newPoTokenGenerator(context())

                        // The streaming poToken needs to be generated exactly once before generating
                        // any other (player) tokens.
                        webPoTokenStreamingPot = webPoTokenGenerator!!.generatePoToken(webPoTokenSessionIdentifier!!)
                    }
                }

                return@synchronized Quadruple(
                    webPoTokenGenerator!!,
                    webPoTokenSessionIdentifier!!,
                    webPoTokenStreamingPot!!,
                    shouldRecreate
                )
            }

        val playerPot = try {
            // Not using synchronized here, since poTokenGenerator would be able to generate
            // multiple poTokens in parallel if needed. The only important thing is for exactly one
            // visitorData/streaming poToken to be generated before anything else.
            runBlocking {
                poTokenGenerator.generatePoToken(videoId)
            }
        } catch (throwable: Throwable) {
            if (hasBeenRecreated) {
                // the poTokenGenerator has just been recreated (and possibly this is already the
                // second time we try), so there is likely nothing we can do
                throw throwable
            } else {
                // retry, this time recreating the [webPoTokenGenerator] from scratch;
                // this might happen for example if NewPipe goes in the background and the WebView
                // content is lost
                if (TAG != null) {
                    Timber.tag(TAG).e(throwable, "Failed to obtain poToken, retrying")
                }
                return getWebClientPoToken(videoId = videoId, forceRecreate = true)
            }
        }

        if (isDebugModeEnabled()) {
            if (TAG != null) {
                Timber.tag(TAG).d(
                    "poToken for $videoId: playerPot=$playerPot, " +
                            "streamingPot=$streamingPot, sessionIdentifier=$sessionIdentifier"
                )
            }
        }

        return PoTokenResult(playerPot, streamingPot)
    }
}