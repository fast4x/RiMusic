package it.fast4x.rimusic.service

import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi

@UnstableApi
class PlayableFormatNotFoundException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class UnplayableException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class StreamExpiredException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class LoginRequiredException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class VideoIdMismatchException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class PlayableFormatNonSupported : PlaybackException(null, null, ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED)
@UnstableApi
class NoInternetException : PlaybackException(null, null, ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
@UnstableApi
class TimeoutException : PlaybackException(null, null, ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT)
@UnstableApi
class UnknownException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class FakeException : PlaybackException(null, null, ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
@UnstableApi
class AccessDeniedToPlayableFormatException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)

