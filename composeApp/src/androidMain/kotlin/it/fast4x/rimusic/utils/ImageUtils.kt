package it.fast4x.rimusic.utils

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers


fun cacheImage(context: Context, url: String, key: String) {

    val listener = object : ImageRequest.Listener {
        override fun onError(request: ImageRequest, result: ErrorResult) {
            super.onError(request, result)
        }

        override fun onSuccess(request: ImageRequest, result: SuccessResult) {
            super.onSuccess(request, result)
        }
    }
    val imageRequest = ImageRequest.Builder(context)
        .data(url.thumbnail(256))
        .allowHardware(false)
        .listener(listener)
        .dispatcher(Dispatchers.IO)
        .memoryCacheKey(url.thumbnail(256))
        .diskCacheKey(url.thumbnail(256))
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .build()

    ImageLoader(context).enqueue(imageRequest)

}