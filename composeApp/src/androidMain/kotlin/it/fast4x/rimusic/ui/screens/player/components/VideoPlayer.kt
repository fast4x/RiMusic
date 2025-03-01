package it.fast4x.rimusic.ui.screens.player.components

/*
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(videoId: String) {
    var betterVideoUrl by remember { mutableStateOf("") }
    LaunchedEffect(videoId) {
        betterVideoUrl = getBetterVideoUrl(videoId).toString()
        println("MediaPlayerView call getBetterVideoUrl $videoId url: $betterVideoUrl")
    }
    println("MediaPlayerView Video $videoId url: $betterVideoUrl")

    val playerListener = remember {
        object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                println("MediaPlayerView Player error: $error")
            }
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                /*
                super.onVideoSizeChanged(videoSize)
                Log.w("MediaPlayerView", "Video size changed: ${videoSize.width} / ${videoSize.height}")
                if (videoSize.width != 0 && videoSize.height != 0) {
                    val h = (videoSize.width.toFloat()/videoSize.height)*screenSize.hPX
                    Log.w("MediaPlayerView", "Calculated width: $h")
                    widthPx = h.roundToInt()
                }
                 */
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    //keepScreenOn = true
                } else {
                    //keepScreenOn = false
                }
            }
        }
    }

    val exoPlayer = ExoPlayer.Builder(LocalContext.current).build().apply {
        addListener(playerListener)
    }

    val mediaItemUri = "$betterVideoUrl${videoId}".toUri()

    val mediaSource = remember(mediaItemUri) {
        MediaItem.fromUri(mediaItemUri)
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(mediaSource) {
        exoPlayer.setMediaItem(mediaSource)
        exoPlayer.prepare()
        exoPlayer.play()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
    }

    val modifier = Modifier
        .fillMaxHeight()
        .wrapContentWidth(unbounded = true, align = Alignment.CenterHorizontally)
        .zIndex(3f)

    Box(modifier = modifier.graphicsLayer { clip = true }) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).also {
                    exoPlayer.setVideoTextureView(it)
                    exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                //.width(with(density) { widthPx.toDp() })
                .align(Alignment.Center)
        )
    }

    /*
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .padding(8.dp)
                .zIndex(3f),
            factory = {
                PlayerView(it).apply {
                    player =
                }
            }
            }
        )
        */
}


@OptIn(UnstableApi::class)
fun getBetterVideoUrl(videoId: String): String? {
    val body = runBlocking(Dispatchers.IO) {
        Innertube.player(
            PlayerBody(videoId = videoId),
            pipedSession = getPipedSession().toApiSession()
        )
    }?.getOrElse { throwable ->
        when (throwable) {
            is ConnectException, is UnknownHostException -> throw NoInternetException()
            is SocketTimeoutException -> throw TimeoutException()
            else -> throw UnknownException()
        }

    }


    val format = body?.streamingData?.adaptiveFormats
        ?.filter { it.isVideo }
        ?.maxByOrNull { it.bitrate?.times(
            (if (it.mimeType.startsWith("video/mp4")) 100 else 1)
        ) ?: -1 }

    val videoUrl = format?.url

    println("getBetterVideoUrl id: $videoId url: $videoUrl")
    return videoUrl


}
*/