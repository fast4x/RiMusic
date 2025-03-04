package it.fast4x.rimusic.utils

/**
 * Created by Rohan Jahagirdar on 07-02-2018.
 */


import it.fast4x.environment.utils.ProxyPreferences
import it.fast4x.environment.utils.getProxy
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration

class OkHttpRequest(client: OkHttpClient) {
    //internal var client = OkHttpClient()
    internal var client = okHttpClient()

    init {
        this.client = client
    }

    private fun okHttpClient() : OkHttpClient {
        ProxyPreferences.preference?.let{
            return OkHttpClient.Builder()
                .proxy(
                    getProxy(it)
                )
                .connectTimeout(Duration.ofSeconds(16))
                .readTimeout(Duration.ofSeconds(8))
                .build()
        }
        return OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(16))
            .readTimeout(Duration.ofSeconds(8))
            .build()
    }


    fun POST(url: String, parameters: HashMap<String, String>, callback: Callback): Call {
        val builder = FormBody.Builder()
        val it = parameters.entries.iterator()
        while (it.hasNext()) {
            val pair = it.next() as Map.Entry<*, *>
            builder.add(pair.key.toString(), pair.value.toString())
        }

        val formBody = builder.build()
        val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()


        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun GET(url: String, callback: Callback): Call {
        val request = Request.Builder()
                .url(url)
                .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    companion object {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    }
}