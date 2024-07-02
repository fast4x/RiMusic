package it.fast4x.rimusic.ui.screens.player

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.valentinilk.shimmer.shimmer
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.bodies.NextBody
import it.fast4x.innertube.requests.lyrics
import it.fast4x.kugou.KuGou
import it.fast4x.lrclib.LrcLib
import it.fast4x.lrclib.models.Track
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.LyricsColor
import it.fast4x.rimusic.enums.LyricsFontSize
import it.fast4x.rimusic.enums.LyricsOutline
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.models.Lyrics
import it.fast4x.rimusic.query
import it.fast4x.rimusic.transaction
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.Menu
import it.fast4x.rimusic.ui.components.themed.MenuEntry
import it.fast4x.rimusic.ui.components.themed.SmartToast
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.styling.DefaultDarkColorPalette
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.PureBlackColorPalette
import it.fast4x.rimusic.ui.styling.onOverlayShimmer
import it.fast4x.rimusic.utils.SynchronizedLyrics
import it.fast4x.rimusic.utils.TextCopyToClipboard
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.expandedplayerKey
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.isShowingSynchronizedLyricsKey
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.lyricsColorKey
import it.fast4x.rimusic.utils.lyricsFontSizeKey
import it.fast4x.rimusic.utils.lyricsOutlineKey
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerEnableLyricsPopupMessageKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.showBackgroundLyricsKey
import it.fast4x.rimusic.utils.showlyricsthumbnailKey
import it.fast4x.rimusic.utils.showthumbnailKey
import it.fast4x.rimusic.utils.verticalFadingEdge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import it.fast4x.rimusic.utils.lyricsHighlightKey
import it.fast4x.rimusic.enums.LyricsHighlight



@UnstableApi
@Composable
fun Lyrics(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    onMaximize: () -> Unit,
    size: Dp,
    mediaMetadataProvider: () -> MediaMetadata,
    durationProvider: () -> Long,
    ensureSongInserted: () -> Unit,
    modifier: Modifier = Modifier,
    enableClick: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    isLandscape: Boolean,
) {
    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val coroutineScope = rememberCoroutineScope()
        val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
        val context = LocalContext.current
        val menuState = LocalMenuState.current
        val currentView = LocalView.current
        val binder = LocalPlayerServiceBinder.current
        val player = binder?.player

        var showthumbnail by rememberPreference(showthumbnailKey, true)
        var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, true)
        var isShowingSynchronizedLyrics by rememberPreference(isShowingSynchronizedLyricsKey, false)
        var invalidLrc by remember(mediaId, isShowingSynchronizedLyrics) { mutableStateOf(false) }
        var isPicking by remember(mediaId, isShowingSynchronizedLyrics) { mutableStateOf(false) }
        var lyricsColor by rememberPreference(
            lyricsColorKey,
            LyricsColor.Thememode
        )
        var lyricsOutline by rememberPreference(
            lyricsOutlineKey,
            LyricsOutline.None
        )
        val playerBackgroundColors by rememberPreference(
            playerBackgroundColorsKey,
            PlayerBackgroundColors.ThemeColor
        )
        var lyricsFontSize by rememberPreference(lyricsFontSizeKey, LyricsFontSize.Medium)

        val thumbnailSize = Dimensions.thumbnails.player.song
        val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)

        var isEditing by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }

        var showPlaceholder by remember {
            mutableStateOf(false)
        }

        var lyrics by remember {
            mutableStateOf<Lyrics?>(null)
        }

        val text = if (isShowingSynchronizedLyrics) lyrics?.synced else lyrics?.fixed

        var isError by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }
        var isErrorSync by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }

        val languageDestination = languageDestination()

        var translateEnabled by remember {
            mutableStateOf(false)
        }

        val translator = Translator(getHttpClient())

        var copyToClipboard by remember {
            mutableStateOf(false)
        }

        if (copyToClipboard) text?.let {
            TextCopyToClipboard(it)
        }

        var fontSize by rememberPreference(lyricsFontSizeKey, LyricsFontSize.Medium)
        val showBackgroundLyrics by rememberPreference(showBackgroundLyricsKey, false)
        val playerEnableLyricsPopupMessage by rememberPreference(
            playerEnableLyricsPopupMessageKey,
            true
        )
        var expandedplayer by rememberPreference(expandedplayerKey, false)

        var checkedLyricsLrc by remember {
            mutableStateOf(false)
        }
        var checkedLyricsKugou by remember {
            mutableStateOf(false)
        }
        var checkedLyricsInnertube by remember {
            mutableStateOf(false)
        }
        var checkLyrics by remember {
            mutableStateOf(false)
        }
        var lyricsHighlight by rememberPreference(lyricsHighlightKey, LyricsHighlight.None)

        LaunchedEffect(mediaId, isShowingSynchronizedLyrics, checkLyrics) {
            withContext(Dispatchers.IO) {

                Database.lyrics(mediaId).collect { currentLyrics ->
                    if (isShowingSynchronizedLyrics && currentLyrics?.synced == null) {
                        lyrics = null
                        val mediaMetadata = mediaMetadataProvider()
                        var duration = withContext(Dispatchers.Main) {
                            durationProvider()
                        }

                        while (duration == C.TIME_UNSET) {
                            delay(100)
                            duration = withContext(Dispatchers.Main) {
                                durationProvider()
                            }
                        }

                        LrcLib.lyrics(
                            artist = mediaMetadata.artist?.toString() ?: "",
                            title = mediaMetadata.title?.toString() ?: "",
                            duration = duration.milliseconds,
                            album = mediaMetadata.albumTitle?.toString()
                        )?.onSuccess {
                            if ((it?.text?.isNotEmpty() == true || it?.sentences?.isNotEmpty() == true)
                                && playerEnableLyricsPopupMessage
                            )
                                coroutineScope.launch {
                                    SmartToast(
                                        context.getString(R.string.info_lyrics_found_on_s)
                                            .format("LrcLib.net"),
                                        type = PopupType.Success
                                    )
                                }
                            else
                                if (playerEnableLyricsPopupMessage)
                                    coroutineScope.launch {
                                        SmartToast(
                                            context.getString(R.string.info_lyrics_not_found_on_s)
                                                .format("LrcLib.net"),
                                            type = PopupType.Error,
                                            durationLong = true
                                        )
                                    }

                            isError = false
                            Database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = currentLyrics?.fixed,
                                    synced = it?.text.orEmpty()
                                )
                            )
                            checkedLyricsLrc = true
                        }?.onFailure {
                            if (playerEnableLyricsPopupMessage)
                                coroutineScope.launch {
                                    SmartToast(
                                        context.getString(R.string.info_lyrics_not_found_on_s_try_on_s)
                                            .format("LrcLib.net", "KuGou.com"),
                                        type = PopupType.Error,
                                        durationLong = true
                                    )
                                }

                            checkedLyricsLrc = true

                            KuGou.lyrics(
                                artist = mediaMetadata.artist?.toString() ?: "",
                                title = mediaMetadata.title?.toString() ?: "",
                                duration = duration / 1000
                            )?.onSuccess {
                                if ((it?.value?.isNotEmpty() == true || it?.sentences?.isNotEmpty() == true)
                                    && playerEnableLyricsPopupMessage
                                )
                                    coroutineScope.launch {
                                        SmartToast(
                                            context.getString(R.string.info_lyrics_found_on_s)
                                                .format("KuGou.com"),
                                            type = PopupType.Success
                                        )
                                    }
                                else
                                    if (playerEnableLyricsPopupMessage)
                                        coroutineScope.launch {
                                            SmartToast(
                                                context.getString(R.string.info_lyrics_not_found_on_s)
                                                    .format("KuGou.com"),
                                                type = PopupType.Error,
                                                durationLong = true
                                            )
                                        }

                                isError = false
                                Database.upsert(
                                    Lyrics(
                                        songId = mediaId,
                                        fixed = currentLyrics?.fixed,
                                        synced = it?.value.orEmpty()
                                    )
                                )
                                checkedLyricsKugou = true
                            }?.onFailure {
                                if (playerEnableLyricsPopupMessage)
                                    coroutineScope.launch {
                                        SmartToast(
                                            context.getString(R.string.info_lyrics_not_found_on_s)
                                                .format("KuGou.com"),
                                            type = PopupType.Error,
                                            durationLong = true
                                        )
                                    }

                                isError = true
                            }
                        }

                    } else if (!isShowingSynchronizedLyrics && currentLyrics?.fixed == null) {
                        isError = false
                        lyrics = null
                        Innertube.lyrics(NextBody(videoId = mediaId))?.onSuccess { fixedLyrics ->
                            Database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = fixedLyrics ?: "",
                                    synced = currentLyrics?.synced
                                )
                            )
                        }?.onFailure {
                            isError = true
                        }
                        checkedLyricsInnertube = true
                    } else {
                        lyrics = currentLyrics
                    }
                }

            }

        }


        if (isEditing) {
            InputTextDialog(
                onDismiss = { isEditing = false },
                setValueRequireNotNull = false,
                title = stringResource(R.string.enter_the_lyrics),
                value = text ?: "",
                placeholder = stringResource(R.string.enter_the_lyrics),
                setValue = {
                    query {
                        ensureSongInserted()
                        Database.upsert(
                            Lyrics(
                                songId = mediaId,
                                fixed = if (isShowingSynchronizedLyrics) lyrics?.fixed else it,
                                synced = if (isShowingSynchronizedLyrics) it else lyrics?.synced,
                            )
                        )
                    }

                }
            )
        }


        if (isPicking && isShowingSynchronizedLyrics) {
            var loading by remember { mutableStateOf(true) }
            val tracks = remember { mutableStateListOf<Track>() }
            var error by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val mediaMetadata = mediaMetadataProvider()
                LrcLib.lyrics(
                    artist = mediaMetadata.artist?.toString().orEmpty(),
                    title = mediaMetadata.title?.toString().orEmpty()
                )?.onSuccess {
                    if (it.isNotEmpty() && playerEnableLyricsPopupMessage)
                        coroutineScope.launch {
                            SmartToast(
                                context.getString(R.string.info_lyrics_tracks_found_on_s)
                                    .format("LrcLib.net"),
                                type = PopupType.Success
                            )
                        }
                    else
                        if (playerEnableLyricsPopupMessage)
                            coroutineScope.launch {
                                SmartToast(
                                    context.getString(R.string.info_lyrics_tracks_not_found_on_s)
                                        .format("LrcLib.net"),
                                    type = PopupType.Error,
                                    durationLong = true
                                )
                            }

                    tracks.clear()
                    tracks.addAll(it)
                    loading = false
                    error = false
                }?.onFailure {
                    if (playerEnableLyricsPopupMessage)
                        coroutineScope.launch {
                            SmartToast(
                                context.getString(R.string.an_error_has_occurred_while_fetching_the_lyrics)
                                    .format("LrcLib.net"),
                                type = PopupType.Error,
                                durationLong = true
                            )
                        }

                    loading = false
                    error = true
                } ?: run { loading = false }
            }

            if (loading)
                DefaultDialog(
                    onDismiss = {
                        isPicking = false
                    }
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

            if (tracks.isNotEmpty()) {
                SelectLyricFromTrack(tracks = tracks, mediaId = mediaId, lyrics = lyrics)
                isPicking = false
            }
        }




        if (isShowingSynchronizedLyrics) {
            DisposableEffect(Unit) {
                currentView.keepScreenOn = true
                onDispose {
                    currentView.keepScreenOn = false
                }
            }
        }

        /*
        AsyncImage(
            model = player?.currentMediaItem?.mediaMetadata?.artworkUri.thumbnail(
                Dimensions.thumbnails.song.px
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(all = 10.dp)
                .clip(thumbnailShape)
                .background(colorPalette.overlay)
                .fillMaxSize(0.9f)
                .aspectRatio(1f)
        )
         */

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() }
                    )
                }
                .fillMaxSize()
                .background(if (!showlyricsthumbnail) Color.Transparent else Color.Black.copy(0.8f))
                .clip(thumbnailShape)

        ) {
            AnimatedVisibility(
                visible = (isError && text == null) || (invalidLrc && isShowingSynchronizedLyrics),
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = stringResource(R.string.an_error_has_occurred_while_fetching_the_lyrics),
                    style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(
                            if (!showlyricsthumbnail) Color.Transparent else Color.Black.copy(
                                0.4f
                            )
                        )
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            if (text?.isEmpty() == true && !checkedLyricsLrc && !checkedLyricsKugou && !checkedLyricsInnertube)
                checkLyrics = !checkLyrics

            if (lyrics?.fixed?.isEmpty() == true && lyrics?.synced?.isNotEmpty() == true)
                isShowingSynchronizedLyrics = true
            if (lyrics?.fixed?.isEmpty() == false && lyrics?.synced?.isNotEmpty() == false)
                isShowingSynchronizedLyrics = false

            /*
            AnimatedVisibility(
                visible = text?.let(String::isEmpty) ?: false,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "${
                        if (isShowingSynchronizedLyrics) stringResource(R.string.synchronized_lyrics) else stringResource(
                            R.string.unsynchronized_lyrics
                        )
                    } " +
                            "${stringResource(R.string.are_not_available_for_this_song)}",
                    style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(
                            if (!showlyricsthumbnail) Color.Transparent else Color.Black.copy(
                                0.4f
                            )
                        )
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
                BasicText(
                    text = "${stringResource(R.string.click_to_switch_to)} ${
                        if (isShowingSynchronizedLyrics) stringResource(R.string.unsynchronized_lyrics) else stringResource(
                            R.string.synchronized_lyrics
                        )
                    }",
                    style = typography.xs.center.bold.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(
                            if (!showlyricsthumbnail) Color.Transparent else Color.Black.copy(
                                0.4f
                            )
                        )
                        .padding(all = 8.dp)
                        .padding(top = 30.dp)
                        .fillMaxWidth()
                        .clickable {
                            isShowingSynchronizedLyrics = !isShowingSynchronizedLyrics
                        }
                )
            }
             */


            if (text?.isNotEmpty() == true) {
                if (isShowingSynchronizedLyrics) {
                    val density = LocalDensity.current
                    val player = LocalPlayerServiceBinder.current?.player
                        ?: return@AnimatedVisibility

                    val synchronizedLyrics = remember(text) {
                        val sentences = LrcLib.Lyrics(text).sentences

                        run {
                            invalidLrc = false
                            SynchronizedLyrics(sentences) {
                                player.currentPosition + 50L //- (lyrics?.startTime ?: 0L)
                            }
                        }
                    }

                    val lazyListState = rememberLazyListState()

                    LaunchedEffect(synchronizedLyrics, density) {
                        //val centerOffset = with(density) { (-thumbnailSize / 3).roundToPx() }
                        val centerOffset = with(density) {
                            (-thumbnailSize.div(if (expandedplayer && !showlyricsthumbnail && !isLandscape) if (trailingContent == null) 2 else 1 else if (trailingContent == null) 3 else 2))
                                .roundToPx()
                        }

                        lazyListState.animateScrollToItem(
                            index = synchronizedLyrics.index + 1,
                            scrollOffset = centerOffset
                        )

                        while (isActive) {
                            delay(50)
                            if (!synchronizedLyrics.update()) continue

                            lazyListState.animateScrollToItem(
                                index = synchronizedLyrics.index + 1,
                                scrollOffset = centerOffset
                            )
                        }
                    }

                    var modifierBG = Modifier.verticalFadingEdge()
                    if (showBackgroundLyrics && showlyricsthumbnail) modifierBG =
                        modifierBG.background(colorPalette.accent)

                    LazyColumn(
                        state = lazyListState,
                        userScrollEnabled = true,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = modifierBG
                    ) {

                        item(key = "header", contentType = 0) {
                            Spacer(modifier = Modifier.height(thumbnailSize))
                        }
                        itemsIndexed(
                            items = synchronizedLyrics.sentences
                        ) { index, sentence ->
                            var translatedText by remember { mutableStateOf("") }
                            if (translateEnabled) {
                                LaunchedEffect(Unit) {
                                    val result = withContext(Dispatchers.IO) {
                                        try {
                                            translator.translate(
                                                sentence.second,
                                                languageDestination,
                                                Language.AUTO
                                            ).translatedText
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    translatedText =
                                        if (result.toString() == "kotlin.Unit") "" else result.toString()
                                    showPlaceholder = false
                                }
                            } else translatedText = sentence.second

                            //Rainbow Shimmer
                            val infiniteTransition = rememberInfiniteTransition()

                            val offset by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        durationMillis = 10000,
                                        easing = LinearEasing
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ), label = ""
                            )

                            val RainbowColors = listOf(
                                Color.Red,
                                Color.Magenta,
                                Color.Blue,
                                Color.Cyan,
                                Color.Green,
                                Color.Yellow,
                                Color.Red
                            )
                            val RainbowColorsdark = listOf(
                                Color.Black.copy(0.35f).compositeOver(Color.Red),
                                Color.Black.copy(0.35f).compositeOver(Color.Magenta),
                                Color.Black.copy(0.35f).compositeOver(Color.Blue),
                                Color.Black.copy(0.35f).compositeOver(Color.Cyan),
                                Color.Black.copy(0.35f).compositeOver(Color.Green),
                                Color.Black.copy(0.35f).compositeOver(Color.Yellow),
                                Color.Black.copy(0.35f).compositeOver(Color.Red)
                            )
                            val RainbowColors2 = listOf(
                                Color.Red.copy(0.3f),
                                Color.Magenta.copy(0.3f),
                                Color.Blue.copy(0.3f),
                                Color.Cyan.copy(0.3f),
                                Color.Green.copy(0.3f),
                                Color.Yellow.copy(0.3f),
                                Color.Red.copy(0.3f)
                            )
                            val Themegradient =
                                listOf(colorPalette.background2, colorPalette.accent)
                            val Themegradient2 = listOf(
                                colorPalette.background2.copy(0.5f),
                                colorPalette.accent.copy(0.5f)
                            )
                            val oldlyrics =
                                listOf(PureBlackColorPalette.text, PureBlackColorPalette.text)
                            val oldlyrics2 = listOf(
                                PureBlackColorPalette.textDisabled,
                                PureBlackColorPalette.textDisabled
                            )

                            val brushrainbow = remember(offset) {
                                object : ShaderBrush() {
                                    override fun createShader(size: Size): Shader {
                                        val widthOffset = size.width * offset
                                        val heightOffset = size.height * offset
                                        return LinearGradientShader(
                                            colors = if (index == synchronizedLyrics.index)
                                                if (showlyricsthumbnail) oldlyrics else RainbowColors
                                            else if (showlyricsthumbnail) oldlyrics2 else RainbowColors2,
                                            from = Offset(widthOffset, heightOffset),
                                            to = Offset(
                                                widthOffset + size.width,
                                                heightOffset + size.height
                                            ),
                                            tileMode = TileMode.Mirror
                                        )
                                    }
                                }
                            }
                            val brushrainbowdark = remember(offset) {
                                object : ShaderBrush() {
                                    override fun createShader(size: Size): Shader {
                                        val widthOffset = size.width * offset
                                        val heightOffset = size.height * offset
                                        return LinearGradientShader(
                                            colors = if (index == synchronizedLyrics.index) RainbowColorsdark else RainbowColors2,
                                            from = Offset(widthOffset, heightOffset),
                                            to = Offset(
                                                widthOffset + size.width,
                                                heightOffset + size.height
                                            ),
                                            tileMode = TileMode.Mirror
                                        )
                                    }
                                }
                            }
                            val brushtheme = remember(offset) {
                                object : ShaderBrush() {
                                    override fun createShader(size: Size): Shader {
                                        val widthOffset = size.width * offset
                                        val heightOffset = size.height * offset
                                        return LinearGradientShader(
                                            colors = if (index == synchronizedLyrics.index)
                                                if (showlyricsthumbnail) oldlyrics else Themegradient
                                            else if (showlyricsthumbnail) oldlyrics2 else Themegradient2,
                                            from = Offset(widthOffset, heightOffset),
                                            to = Offset(
                                                widthOffset + size.width,
                                                heightOffset + size.height
                                            ),
                                            tileMode = TileMode.Mirror
                                        )
                                    }
                                }
                            }
                            //Rainbow Shimmer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (showlyricsthumbnail)
                                    BasicText(
                                        text = translatedText,
                                        style = when (fontSize) {
                                            LyricsFontSize.Light ->
                                                typography.m.center.medium.color(
                                                    if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled
                                                )

                                            LyricsFontSize.Medium ->
                                                typography.l.center.medium.color(
                                                    if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled
                                                )

                                            LyricsFontSize.Heavy ->
                                                typography.xl.center.medium.color(
                                                    if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled
                                                )

                                            LyricsFontSize.Large ->
                                                typography.xlxl.center.medium.color(
                                                    if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled
                                                )
                                        },
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 32.dp)
                                            .clickable {
                                                if (enableClick)
                                                    binder?.player?.seekTo(sentence.first)
                                            }
                                    )
                                else if ((lyricsColor == LyricsColor.White) || (lyricsColor == LyricsColor.Black) || (lyricsColor == LyricsColor.Accent) || (lyricsColor == LyricsColor.Thememode))
                                    BasicText(
                                        text = translatedText,
                                        style = when (fontSize) {
                                            LyricsFontSize.Light ->
                                                typography.m.center.medium.color(
                                                    if (index == synchronizedLyrics.index)
                                                        if (lyricsColor == LyricsColor.White) Color.White
                                                        else if (lyricsColor == LyricsColor.Black) Color.Black
                                                        else if (lyricsColor == LyricsColor.Thememode) colorPalette.text
                                                        else colorPalette.accent
                                                    else if (lyricsColor == LyricsColor.White) Color.White.copy(0.6f)
                                                    else if (lyricsColor == LyricsColor.Black) Color.Black.copy(0.6f)
                                                    else if (lyricsColor == LyricsColor.Thememode) colorPalette.text.copy(0.6f)
                                                    else colorPalette.accent.copy(0.6f)
                                                )

                                            LyricsFontSize.Medium ->
                                                typography.l.center.medium.color(
                                                    if (index == synchronizedLyrics.index)
                                                        if (lyricsColor == LyricsColor.White) Color.White
                                                        else if (lyricsColor == LyricsColor.Black) Color.Black
                                                        else if (lyricsColor == LyricsColor.Thememode) colorPalette.text
                                                        else colorPalette.accent
                                                    else if (lyricsColor == LyricsColor.White) Color.White.copy(0.6f)
                                                    else if (lyricsColor == LyricsColor.Black) Color.Black.copy(0.6f)
                                                    else if (lyricsColor == LyricsColor.Thememode) colorPalette.text.copy(0.6f)
                                                    else colorPalette.accent.copy(0.6f)
                                                )

                                            LyricsFontSize.Heavy ->
                                                typography.xl.center.medium.color(
                                                    if (index == synchronizedLyrics.index)
                                                        if (lyricsColor == LyricsColor.White) Color.White
                                                        else if (lyricsColor == LyricsColor.Black) Color.Black
                                                        else if (lyricsColor == LyricsColor.Thememode) colorPalette.text
                                                        else colorPalette.accent
                                                    else if (lyricsColor == LyricsColor.White) Color.White.copy(0.6f)
                                                    else if (lyricsColor == LyricsColor.Black) Color.Black.copy(0.6f)
                                                    else if (lyricsColor == LyricsColor.Thememode) colorPalette.text.copy(0.6f)
                                                    else colorPalette.accent.copy(0.6f)
                                                )

                                            LyricsFontSize.Large ->
                                                typography.xlxl.center.medium.color(
                                                    if (index == synchronizedLyrics.index)
                                                        if (lyricsColor == LyricsColor.White) Color.White
                                                        else if (lyricsColor == LyricsColor.Black) Color.Black
                                                        else if (lyricsColor == LyricsColor.Thememode) colorPalette.text
                                                        else colorPalette.accent
                                                    else if (lyricsColor == LyricsColor.White) Color.White.copy(0.6f)
                                                    else if (lyricsColor == LyricsColor.Black) Color.Black.copy(0.6f)
                                                    else if (lyricsColor == LyricsColor.Thememode) colorPalette.text.copy(0.6f)
                                                    else colorPalette.accent.copy(0.6f)
                                                )
                                        },
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 32.dp)
                                            .clickable {
                                                if (enableClick)
                                                    binder?.player?.seekTo(sentence.first)
                                            }
                                            .background(if (index == synchronizedLyrics.index) if (lyricsHighlight == LyricsHighlight.White) Color.White.copy(0.5f) else if (lyricsHighlight == LyricsHighlight.Black) Color.Black.copy(0.5f) else Color.Transparent else Color.Transparent)
                                    )
                                else
                                    BasicText(
                                        text = translatedText,
                                        style = TextStyle(
                                            brush = if (colorPaletteMode == ColorPaletteMode.Light) brushrainbow else brushrainbowdark
                                        ).merge(
                                            when (fontSize) {
                                                LyricsFontSize.Light ->
                                                    typography.m.center.medium.color(
                                                        if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                        else colorPalette.text.copy(0.6f)
                                                    )

                                                LyricsFontSize.Medium ->
                                                    typography.l.center.medium.color(
                                                        if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                        else colorPalette.text.copy(0.6f)
                                                    )

                                                LyricsFontSize.Heavy ->
                                                    typography.xl.center.medium.color(
                                                        if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                        else colorPalette.text.copy(0.6f)
                                                    )

                                                LyricsFontSize.Large ->
                                                    typography.xlxl.center.medium.color(
                                                        if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                        else colorPalette.text.copy(0.6f)
                                                    )
                                            },
                                        ),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 32.dp)
                                            .clickable {
                                                if (enableClick)
                                                    binder?.player?.seekTo(sentence.first)
                                            }
                                    )
                                /*else
                                  BasicText(
                                     text = translatedText,
                                     style = TextStyle(
                                         brush = brushtheme
                                     ).merge(when (fontSize) {
                                         LyricsFontSize.Light ->
                                             typography.m.center.medium.color(
                                                 if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                 else colorPalette.text.copy(0.6f)
                                             )
                                         LyricsFontSize.Medium ->
                                             typography.l.center.medium.color(
                                                 if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                 else colorPalette.text.copy(0.6f)
                                             )
                                         LyricsFontSize.Heavy ->
                                             typography.xl.center.medium.color(
                                                 if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                 else colorPalette.text.copy(0.6f)
                                             )
                                         LyricsFontSize.Large ->
                                             typography.xlxl.center.medium.color(
                                                 if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                 else colorPalette.text.copy(0.6f)
                                             )
                                     },
                                     ),
                                     modifier = Modifier
                                         .padding(vertical = 4.dp, horizontal = 32.dp)
                                         .clickable {
                                             if (enableClick)
                                                 binder?.player?.seekTo(sentence.first)
                                         }
                                 )*/
                                ////Lyrics Outline Synced
                                if (!showlyricsthumbnail)
                                    if (lyricsOutline == LyricsOutline.None) {

                                    } else if ((lyricsOutline == LyricsOutline.White) || (lyricsOutline == LyricsOutline.Black) || (lyricsOutline == LyricsOutline.Thememode))
                                        BasicText(
                                            text = translatedText,
                                            style = TextStyle(
                                                drawStyle = Stroke(
                                                    width = if (fontSize == LyricsFontSize.Large)
                                                        if (lyricsOutline == LyricsOutline.White) 3.0f
                                                        else if (lyricsOutline == LyricsOutline.Black) 5.0f
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) 3.0f
                                                            else 5.0f
                                                        else 0f
                                                    else if (fontSize == LyricsFontSize.Heavy)
                                                        if (lyricsOutline == LyricsOutline.White) 1.5f
                                                        else if (lyricsOutline == LyricsOutline.Black) 3.5f
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) 1.5f
                                                            else 3.5f
                                                        else 0f
                                                    else if (fontSize == LyricsFontSize.Medium)
                                                        if (lyricsOutline == LyricsOutline.White) 0.95f
                                                        else if (lyricsOutline == LyricsOutline.Black) 2.95f
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) 0.95f
                                                            else 2.95f
                                                        else 0f
                                                    else
                                                        if (lyricsOutline == LyricsOutline.White) 0.65f
                                                        else if (lyricsOutline == LyricsOutline.Black) 2.65f
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) 0.65f
                                                            else 2.65f
                                                        else 0f,
                                                    join = StrokeJoin.Round
                                                ),
                                            ).merge(
                                                when (fontSize) {
                                                    LyricsFontSize.Light ->
                                                        typography.m.center.medium.color(
                                                            if (index == synchronizedLyrics.index)
                                                                if (lyricsOutline == LyricsOutline.White) Color.White
                                                                else if (lyricsOutline == LyricsOutline.Black) Color.Black
                                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                                    if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                                    else Color.Black
                                                                else Color.Transparent
                                                            else if (lyricsOutline == LyricsOutline.White) Color.White.copy(0.6f)
                                                            else if (lyricsOutline == LyricsOutline.Black) Color.Black.copy(0.6f)
                                                            else if (lyricsOutline == LyricsOutline.Thememode)
                                                                if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(0.6f)
                                                                else Color.Black.copy(0.6f)
                                                            else Color.Transparent
                                                        )

                                                    LyricsFontSize.Medium ->
                                                        typography.l.center.medium.color(
                                                            if (index == synchronizedLyrics.index)
                                                                if (lyricsOutline == LyricsOutline.White) Color.White
                                                                else if (lyricsOutline == LyricsOutline.Black) Color.Black
                                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                                    if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                                    else Color.Black
                                                                else Color.Transparent
                                                            else if (lyricsOutline == LyricsOutline.White) Color.White.copy(0.6f)
                                                            else if (lyricsOutline == LyricsOutline.Black) Color.Black.copy(0.6f)
                                                            else if (lyricsOutline == LyricsOutline.Thememode)
                                                                if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(0.6f)
                                                                else Color.Black.copy(0.6f)
                                                            else Color.Transparent
                                                        )

                                                    LyricsFontSize.Heavy ->
                                                        typography.xl.center.medium.color(
                                                            if (index == synchronizedLyrics.index)
                                                                if (lyricsOutline == LyricsOutline.White) Color.White
                                                                else if (lyricsOutline == LyricsOutline.Black) Color.Black
                                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                                    if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                                    else Color.Black
                                                                else Color.Transparent
                                                            else if (lyricsOutline == LyricsOutline.White) Color.White.copy(0.6f)
                                                            else if (lyricsOutline == LyricsOutline.Black) Color.Black.copy(0.6f)
                                                            else if (lyricsOutline == LyricsOutline.Thememode)
                                                                if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(0.6f)
                                                                else Color.Black.copy(0.6f)
                                                            else Color.Transparent
                                                        )

                                                    LyricsFontSize.Large ->
                                                        typography.xlxl.center.medium.color(
                                                            if (index == synchronizedLyrics.index)
                                                                if (lyricsOutline == LyricsOutline.White) Color.White
                                                                else if (lyricsOutline == LyricsOutline.Black) Color.Black
                                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                                    if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                                    else Color.Black
                                                                else Color.Transparent
                                                            else if (lyricsOutline == LyricsOutline.White) Color.White.copy(0.6f)
                                                            else if (lyricsOutline == LyricsOutline.Black) Color.Black.copy(0.6f)
                                                            else if (lyricsOutline == LyricsOutline.Thememode)
                                                                if (colorPaletteMode == ColorPaletteMode.Light) Color.White.copy(0.6f)
                                                                else Color.Black.copy(0.6f)
                                                            else Color.Transparent
                                                        )
                                                }
                                            ),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp, horizontal = 32.dp)
                                                .clickable {
                                                    if (enableClick)
                                                        binder?.player?.seekTo(sentence.first)
                                                }
                                        )
                                    else if (lyricsOutline == LyricsOutline.Rainbow)
                                        BasicText(
                                            text = translatedText,
                                            style = TextStyle(
                                                brush = brushrainbowdark,
                                                drawStyle = Stroke(
                                                    width = if (fontSize == LyricsFontSize.Large) if (index == synchronizedLyrics.index) 5.0f else 4f
                                                    else if (fontSize == LyricsFontSize.Heavy) if (index == synchronizedLyrics.index) 3.5f else 2.5f
                                                    else if (fontSize == LyricsFontSize.Medium) if (index == synchronizedLyrics.index) 2.95f else 1.95f
                                                    else if (index == synchronizedLyrics.index) 2.65f else 1.65f,
                                                    join = StrokeJoin.Round
                                                ),
                                            ).merge(
                                                when (fontSize) {
                                                    LyricsFontSize.Light ->
                                                        typography.m.center.medium.color(
                                                            if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                            else colorPalette.text.copy(0.6f)
                                                        )

                                                    LyricsFontSize.Medium ->
                                                        typography.l.center.medium.color(
                                                            if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                            else colorPalette.text.copy(0.6f)
                                                        )

                                                    LyricsFontSize.Heavy ->
                                                        typography.xl.center.medium.color(
                                                            if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                            else colorPalette.text.copy(0.6f)
                                                        )

                                                    LyricsFontSize.Large ->
                                                        typography.xlxl.center.medium.color(
                                                            if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                            else colorPalette.text.copy(0.6f)
                                                        )
                                                }
                                            ),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp, horizontal = 32.dp)
                                                .clickable {
                                                    if (enableClick)
                                                        binder?.player?.seekTo(sentence.first)
                                                }
                                        )
                                    else //For Glow Outline//
                                        BasicText(
                                            text = translatedText,
                                            style = TextStyle(
                                                shadow = Shadow(
                                                    color = if (index == synchronizedLyrics.index)
                                                        if (lyricsColor == LyricsColor.Thememode) Color.White.copy(0.3f).compositeOver(colorPalette.text)
                                                        else if (lyricsColor == LyricsColor.White) Color.White.copy(0.3f).compositeOver(Color.White)
                                                        else if (lyricsColor == LyricsColor.Black) Color.White.copy(0.3f).compositeOver(Color.Black)
                                                        else if (lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f).compositeOver(colorPalette.accent)
                                                        else Color.Transparent
                                                    else Color.Transparent,
                                                    offset = Offset(0f, 0f), blurRadius = 25f
                                                ),
                                            ).merge(
                                                when (fontSize) {
                                                    LyricsFontSize.Light ->
                                                        typography.m.center.medium.color(
                                                            if (index == synchronizedLyrics.index)
                                                                if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f) else Color.Transparent
                                                            else Color.Transparent
                                                        )

                                                    LyricsFontSize.Medium ->
                                                        typography.l.center.medium.color(
                                                            if (index == synchronizedLyrics.index)
                                                                if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f) else Color.Transparent
                                                            else Color.Transparent
                                                        )

                                                    LyricsFontSize.Heavy ->
                                                        typography.xl.center.medium.color(
                                                            if (index == synchronizedLyrics.index)
                                                                if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f) else Color.Transparent
                                                            else Color.Transparent
                                                        )

                                                    LyricsFontSize.Large ->
                                                        typography.xlxl.center.medium.color(
                                                            if (index == synchronizedLyrics.index)
                                                                if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f) else Color.Transparent
                                                            else Color.Transparent
                                                        )
                                                }
                                            ),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp, horizontal = 32.dp)
                                                .clickable {
                                                    if (enableClick)
                                                        binder?.player?.seekTo(sentence.first)
                                                }
                                        )
                            }
                        }
                        item(key = "footer", contentType = 0) {
                            Spacer(modifier = Modifier.height(thumbnailSize))
                        }
                    }
                } else {
                    var translatedText by remember { mutableStateOf("") }
                    if (translateEnabled) {
                        LaunchedEffect(Unit) {
                            val result = withContext(Dispatchers.IO) {
                                try {
                                    translator.translate(
                                        text,
                                        languageDestination,
                                        Language.AUTO
                                    ).translatedText
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            translatedText =
                                if (result.toString() == "kotlin.Unit") "" else result.toString()
                            showPlaceholder = false
                        }
                    } else translatedText = text

                    Box(
                        modifier = Modifier
                            .verticalFadingEdge()
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth()
                            .padding(vertical = size / 4, horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        //Rainbow Shimmer
                        val infiniteTransition = rememberInfiniteTransition()

                        val offset by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 10000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = ""
                        )

                        val RainbowColors = listOf(
                            Color.Red,
                            Color.Magenta,
                            Color.Blue,
                            Color.Cyan,
                            Color.Green,
                            Color.Yellow,
                            Color.Red
                        )
                        val RainbowColorsdark = listOf(
                            Color.Black.copy(0.35f).compositeOver(Color.Red),
                            Color.Black.copy(0.35f).compositeOver(Color.Magenta),
                            Color.Black.copy(0.35f).compositeOver(Color.Blue),
                            Color.Black.copy(0.35f).compositeOver(Color.Cyan),
                            Color.Black.copy(0.35f).compositeOver(Color.Green),
                            Color.Black.copy(0.35f).compositeOver(Color.Yellow),
                            Color.Black.copy(0.35f).compositeOver(Color.Red)
                        )

                        val brushrainbow = remember(offset) {
                            object : ShaderBrush() {
                                override fun createShader(size: Size): Shader {
                                    val widthOffset = size.width * offset
                                    val heightOffset = size.height * offset
                                    return LinearGradientShader(
                                        colors = RainbowColors,
                                        from = Offset(widthOffset, heightOffset),
                                        to = Offset(
                                            widthOffset + size.width,
                                            heightOffset + size.height
                                        ),
                                        tileMode = TileMode.Mirror
                                    )
                                }
                            }
                        }
                        val brushrainbowdark = remember(offset) {
                            object : ShaderBrush() {
                                override fun createShader(size: Size): Shader {
                                    val widthOffset = size.width * offset
                                    val heightOffset = size.height * offset
                                    return LinearGradientShader(
                                        colors = RainbowColorsdark,
                                        from = Offset(widthOffset, heightOffset),
                                        to = Offset(
                                            widthOffset + size.width,
                                            heightOffset + size.height
                                        ),
                                        tileMode = TileMode.Mirror
                                    )
                                }
                            }
                        }

                        if (showlyricsthumbnail)
                            BasicText(
                                text = translatedText,
                                style = when (fontSize) {
                                    LyricsFontSize.Light ->
                                        typography.m.center.medium.color(PureBlackColorPalette.text)

                                    LyricsFontSize.Medium ->
                                        typography.l.center.medium.color(PureBlackColorPalette.text)

                                    LyricsFontSize.Heavy ->
                                        typography.xl.center.medium.color(PureBlackColorPalette.text)

                                    LyricsFontSize.Large ->
                                        typography.xlxl.center.medium.color(PureBlackColorPalette.text)
                                }
                            )
                        else if ((lyricsColor == LyricsColor.Thememode) || (lyricsColor == LyricsColor.White) || (lyricsColor == LyricsColor.Black) || (lyricsColor == LyricsColor.Accent))
                            BasicText(
                                text = translatedText,
                                style = when (fontSize) {
                                    LyricsFontSize.Light ->
                                        typography.m.center.medium.color(
                                            if (lyricsColor == LyricsColor.White) Color.White
                                            else if (lyricsColor == LyricsColor.Black) Color.Black
                                            else if (lyricsColor == LyricsColor.Thememode) colorPalette.text
                                            else if (lyricsColor == LyricsColor.Accent) colorPalette.accent
                                            else Color.Transparent
                                        )

                                    LyricsFontSize.Medium ->
                                        typography.l.center.medium.color(
                                            if (lyricsColor == LyricsColor.White) Color.White
                                            else if (lyricsColor == LyricsColor.Black) Color.Black
                                            else if (lyricsColor == LyricsColor.Thememode) colorPalette.text
                                            else if (lyricsColor == LyricsColor.Accent) colorPalette.accent
                                            else Color.Transparent
                                        )

                                    LyricsFontSize.Heavy ->
                                        typography.xl.center.medium.color(
                                            if (lyricsColor == LyricsColor.White) Color.White
                                            else if (lyricsColor == LyricsColor.Black) Color.Black
                                            else if (lyricsColor == LyricsColor.Thememode) colorPalette.text
                                            else if (lyricsColor == LyricsColor.Accent) colorPalette.accent
                                            else Color.Transparent
                                        )

                                    LyricsFontSize.Large ->
                                        typography.xlxl.center.medium.color(
                                            if (lyricsColor == LyricsColor.White) Color.White
                                            else if (lyricsColor == LyricsColor.Black) Color.Black
                                            else if (lyricsColor == LyricsColor.Thememode) colorPalette.text
                                            else if (lyricsColor == LyricsColor.Accent) colorPalette.accent
                                            else Color.Transparent
                                        )
                                }
                            )
                        else
                            BasicText(
                                text = translatedText,
                                style = TextStyle(
                                    brush = if (colorPaletteMode == ColorPaletteMode.Light) brushrainbow else brushrainbowdark
                                ).merge(
                                    when (fontSize) {
                                        LyricsFontSize.Light ->
                                            typography.m.center.medium.color(PureBlackColorPalette.text)

                                        LyricsFontSize.Medium ->
                                            typography.l.center.medium.color(PureBlackColorPalette.text)

                                        LyricsFontSize.Heavy ->
                                            typography.xl.center.medium.color(PureBlackColorPalette.text)

                                        LyricsFontSize.Large ->
                                            typography.xlxl.center.medium.color(
                                                PureBlackColorPalette.text
                                            )
                                    }
                                )
                            )
                        //Lyrics Outline Non Synced
                        if (!showlyricsthumbnail)
                            if (lyricsOutline == LyricsOutline.None) {

                            } else if (lyricsOutline == LyricsOutline.Thememode || (lyricsOutline == LyricsOutline.White) || (lyricsOutline == LyricsOutline.Black))
                                BasicText(
                                    text = translatedText,
                                    style = TextStyle(
                                        drawStyle = Stroke(
                                            width = if (fontSize == LyricsFontSize.Large)
                                                if (lyricsOutline == LyricsOutline.White) 3.0f
                                                else if (lyricsOutline == LyricsOutline.Black) 5.0f
                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                    if (colorPaletteMode == ColorPaletteMode.Light) 3.0f
                                                    else 5.0f
                                                else 0f
                                            else if (fontSize == LyricsFontSize.Heavy)
                                                if (lyricsOutline == LyricsOutline.White) 1.5f
                                                else if (lyricsOutline == LyricsOutline.Black) 3.5f
                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                    if (colorPaletteMode == ColorPaletteMode.Light) 1.5f
                                                    else 3.5f
                                                else 0f
                                            else if (fontSize == LyricsFontSize.Medium)
                                                if (lyricsOutline == LyricsOutline.White) 0.95f
                                                else if (lyricsOutline == LyricsOutline.Black) 2.95f
                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                    if (colorPaletteMode == ColorPaletteMode.Light) 0.95f
                                                    else 2.95f
                                                else 0f
                                            else
                                                if (lyricsOutline == LyricsOutline.White) 0.65f
                                                else if (lyricsOutline == LyricsOutline.Black) 2.65f
                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                    if (colorPaletteMode == ColorPaletteMode.Light) 0.65f
                                                    else 2.65f
                                                else 0f,
                                            join = StrokeJoin.Round
                                        ),
                                    ).merge(
                                        when (fontSize) {
                                            LyricsFontSize.Light ->
                                                typography.m.center.medium.color(
                                                    if (lyricsOutline == LyricsOutline.White) Color.White
                                                    else if (lyricsOutline == LyricsOutline.Black) Color.Black
                                                    else if (lyricsOutline == LyricsOutline.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                        else Color.Black
                                                    else Color.Transparent
                                                )

                                            LyricsFontSize.Medium ->
                                                typography.l.center.medium.color(
                                                    if (lyricsColor == LyricsColor.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                        else Color.Black
                                                    else Color.Black.copy(0.6f)
                                                )

                                            LyricsFontSize.Heavy ->
                                                typography.xl.center.medium.color(
                                                    if (lyricsColor == LyricsColor.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                        else Color.Black
                                                    else Color.Black.copy(0.6f)
                                                )

                                            LyricsFontSize.Large ->
                                                typography.xlxl.center.medium.color(
                                                    if (lyricsColor == LyricsColor.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                        else Color.Black
                                                    else Color.Black.copy(0.6f)
                                                )
                                        }
                                    )
                                )
                            else if (lyricsOutline == LyricsOutline.Rainbow)
                                BasicText(
                                    text = translatedText,
                                    style = TextStyle(
                                        brush = brushrainbowdark,
                                        drawStyle = Stroke(
                                            width = if (fontSize == LyricsFontSize.Large) 5.0f
                                            else if (fontSize == LyricsFontSize.Heavy) 3.5f
                                            else if (fontSize == LyricsFontSize.Medium) 2.95f
                                            else 2.65f,
                                            join = StrokeJoin.Round
                                        ),
                                    ).merge(
                                        when (fontSize) {
                                            LyricsFontSize.Light ->
                                                typography.m.center.medium.color(
                                                    if (lyricsColor == LyricsColor.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                        else Color.Black
                                                    else Color.Black.copy(0.6f)
                                                )

                                            LyricsFontSize.Medium ->
                                                typography.l.center.medium.color(
                                                    if (lyricsColor == LyricsColor.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                        else Color.Black
                                                    else Color.Black.copy(0.6f)
                                                )

                                            LyricsFontSize.Heavy ->
                                                typography.xl.center.medium.color(
                                                    if (lyricsColor == LyricsColor.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                        else Color.Black
                                                    else Color.Black.copy(0.6f)
                                                )

                                            LyricsFontSize.Large ->
                                                typography.xlxl.center.medium.color(
                                                    if (lyricsColor == LyricsColor.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                        else Color.Black
                                                    else Color.Black.copy(0.6f)
                                                )
                                        }
                                    )
                                )
                            else
                                BasicText(
                                    text = translatedText,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = if (lyricsColor == LyricsColor.Thememode) Color.White.copy(0.3f).compositeOver(colorPalette.text)
                                            else if (lyricsColor == LyricsColor.White) Color.White.copy(0.3f).compositeOver(Color.White)
                                            else if (lyricsColor == LyricsColor.Black) Color.White.copy(0.3f).compositeOver(Color.Black)
                                            else if (lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f).compositeOver(colorPalette.accent)
                                            else Color.Transparent,
                                            offset = Offset(0f, 0f), blurRadius = 25f
                                        ),
                                    ).merge(
                                        when (fontSize) {
                                            LyricsFontSize.Light ->
                                                typography.m.center.medium.color(
                                                    if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f) else Color.Transparent
                                                )

                                            LyricsFontSize.Medium ->
                                                typography.l.center.medium.color(
                                                    if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f) else Color.Transparent
                                                )

                                            LyricsFontSize.Heavy ->
                                                typography.xl.center.medium.color(
                                                    if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f) else Color.Transparent
                                                )

                                            LyricsFontSize.Large ->
                                                typography.xlxl.center.medium.color(
                                                    if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent) Color.White.copy(0.3f) else Color.Transparent
                                                )
                                        }
                                    )
                                )



                    }
                }
            }

            if ((text == null && !isError) || showPlaceholder) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .shimmer()
                ) {
                    repeat(4) {
                        TextPlaceholder(
                            color = colorPalette.onOverlayShimmer,
                            modifier = Modifier
                                .alpha(1f - it * 0.1f)
                        )
                    }
                }
            }

            /**********/
            if (trailingContent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.4f)
                ) {
                    trailingContent()
                }
            }
            /*********/


            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(if (trailingContent == null) 0.30f else 0.22f)
            ) {
                if (showlyricsthumbnail)
                    IconButton(
                        icon = R.drawable.minmax,
                        color = DefaultDarkColorPalette.text,
                        enabled = true,
                        onClick = onMaximize,
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .align(Alignment.BottomStart)
                            .size(20.dp)
                    )
                if (showlyricsthumbnail)
                    IconButton(
                        icon = R.drawable.text,
                        color = DefaultDarkColorPalette.text,
                        enabled = true,
                        onClick = {
                            menuState.display {
                                Menu {
                                    MenuEntry(
                                        icon = R.drawable.text,
                                        text = stringResource(R.string.light),
                                        secondaryText = "",
                                        onClick = {
                                            menuState.hide()
                                            fontSize = LyricsFontSize.Light
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.text,
                                        text = stringResource(R.string.medium),
                                        secondaryText = "",
                                        onClick = {
                                            menuState.hide()
                                            fontSize = LyricsFontSize.Medium
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.text,
                                        text = stringResource(R.string.heavy),
                                        secondaryText = "",
                                        onClick = {
                                            menuState.hide()
                                            fontSize = LyricsFontSize.Heavy
                                        }
                                    )
                                    MenuEntry(
                                        icon = R.drawable.text,
                                        text = stringResource(R.string.large),
                                        secondaryText = "",
                                        onClick = {
                                            menuState.hide()
                                            fontSize = LyricsFontSize.Large
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                    )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.2f)
            ) {

                if (showlyricsthumbnail)
                    IconButton(
                        icon = R.drawable.translate,
                        color = if (translateEnabled == true) colorPalette.text else colorPalette.textDisabled,
                        enabled = true,
                        onClick = {
                            translateEnabled = !translateEnabled
                            showPlaceholder = if (!translateEnabled) false else true
                        },
                        modifier = Modifier
                            //.padding(horizontal = 8.dp)
                            .padding(bottom = 10.dp)
                            .align(Alignment.BottomStart)
                            .size(24.dp)
                    )
                Image(
                    painter = painterResource(R.drawable.ellipsis_vertical),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(DefaultDarkColorPalette.text),
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .clickable(
                            indication = ripple(bounded = false),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                menuState.display {
                                    Menu {
                                        if (!showlyricsthumbnail)
                                            MenuEntry(
                                                icon = R.drawable.text,
                                                enabled = true,
                                                text = stringResource(R.string.lyrics_size),
                                                onClick = {
                                                    menuState.display {
                                                        Menu {
                                                            MenuEntry(
                                                                icon = R.drawable.text,
                                                                text = stringResource(R.string.light),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    fontSize = LyricsFontSize.Light
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.text,
                                                                text = stringResource(R.string.medium),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    fontSize = LyricsFontSize.Medium
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.text,
                                                                text = stringResource(R.string.heavy),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    fontSize = LyricsFontSize.Heavy
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.text,
                                                                text = stringResource(R.string.large),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    fontSize = LyricsFontSize.Large
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                        if (!showlyricsthumbnail)
                                            MenuEntry(
                                                icon = R.drawable.droplet,
                                                enabled = true,
                                                text = stringResource(R.string.lyricscolor),
                                                onClick = {
                                                    menuState.display {
                                                        Menu {
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.theme),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsColor =
                                                                        LyricsColor.Thememode
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.white),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsColor =
                                                                        LyricsColor.White
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.black),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsColor =
                                                                        LyricsColor.Black
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.accent),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsColor = LyricsColor.Accent
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.fluidrainbow),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsColor =
                                                                        LyricsColor.FluidRainbow
                                                                }
                                                            )
                                                            /*MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.fluidtheme),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsColor = LyricsColor.FluidTheme
                                                                }
                                                            )*/
                                                        }
                                                    }
                                                }
                                            )
                                        if (!showlyricsthumbnail)
                                            MenuEntry(
                                                icon = R.drawable.horizontal_bold_line,
                                                enabled = true,
                                                text = stringResource(R.string.lyricsoutline),
                                                onClick = {
                                                    menuState.display {
                                                        Menu {
                                                            MenuEntry(
                                                                icon = R.drawable.close,
                                                                text = stringResource(R.string.none),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsOutline =
                                                                        LyricsOutline.None
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.horizontal_bold_line,
                                                                text = stringResource(R.string.theme),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsOutline =
                                                                        LyricsOutline.Thememode
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.horizontal_bold_line,
                                                                text = stringResource(R.string.white),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsOutline =
                                                                        LyricsOutline.White
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.horizontal_bold_line,
                                                                text = stringResource(R.string.black),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsOutline =
                                                                        LyricsOutline.Black
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.fluidrainbow),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsOutline =
                                                                        LyricsOutline.Rainbow
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.glow),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsOutline =
                                                                        LyricsOutline.Glow
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                        if (!showlyricsthumbnail)
                                            MenuEntry(
                                                icon = R.drawable.translate,
                                                text = stringResource(R.string.translate),
                                                enabled = true,
                                                onClick = {
                                                    translateEnabled = !translateEnabled
                                                    showPlaceholder =
                                                        if (!translateEnabled) false else true
                                                }
                                            )

                                        if (!showlyricsthumbnail)
                                            MenuEntry(
                                                icon = R.drawable.horizontal_bold_line_rounded,
                                                enabled = true,
                                                text = stringResource(R.string.highlight),
                                                onClick = {
                                                    menuState.display {
                                                        Menu {
                                                            MenuEntry(
                                                                icon = R.drawable.horizontal_straight_line,
                                                                text = stringResource(R.string.none),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsHighlight =
                                                                        LyricsHighlight.None
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.horizontal_straight_line,
                                                                text = stringResource(R.string.white),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsHighlight =
                                                                        LyricsHighlight.White
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.horizontal_straight_line,
                                                                text = stringResource(R.string.black),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsHighlight =
                                                                        LyricsHighlight.Black
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            )

                                        MenuEntry(
                                            icon = R.drawable.time,
                                            text = stringResource(R.string.show) + " ${
                                                if (isShowingSynchronizedLyrics) stringResource(
                                                    R.string.unsynchronized_lyrics
                                                ) else stringResource(R.string.synchronized_lyrics)
                                            }",
                                            secondaryText = if (isShowingSynchronizedLyrics) null else stringResource(
                                                R.string.provided_by
                                            ) + " kugou.com and LrcLib.net",
                                            onClick = {
                                                menuState.hide()
                                                isShowingSynchronizedLyrics =
                                                    !isShowingSynchronizedLyrics
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.title_edit,
                                            text = stringResource(R.string.edit_lyrics),
                                            onClick = {
                                                menuState.hide()
                                                isEditing = true
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.copy,
                                            text = stringResource(R.string.copy_lyrics),
                                            onClick = {
                                                menuState.hide()
                                                copyToClipboard = true
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.search,
                                            text = stringResource(R.string.search_lyrics_online),
                                            onClick = {
                                                menuState.hide()
                                                val mediaMetadata = mediaMetadataProvider()

                                                try {
                                                    context.startActivity(
                                                        Intent(Intent.ACTION_WEB_SEARCH).apply {
                                                            putExtra(
                                                                SearchManager.QUERY,
                                                                "${mediaMetadata.title} ${mediaMetadata.artist} lyrics"
                                                            )
                                                        }
                                                    )
                                                } catch (e: ActivityNotFoundException) {
                                                    SmartToast(
                                                        context.getString(R.string.info_not_find_app_browse_internet),
                                                        type = PopupType.Warning
                                                    )
                                                }
                                            }
                                        )

                                        MenuEntry(
                                            icon = R.drawable.sync,
                                            text = stringResource(R.string.fetch_lyrics_again),
                                            enabled = lyrics != null,
                                            onClick = {
                                                menuState.hide()
                                                query {
                                                    Database.upsert(
                                                        Lyrics(
                                                            songId = mediaId,
                                                            fixed = if (isShowingSynchronizedLyrics) lyrics?.fixed else null,
                                                            synced = if (isShowingSynchronizedLyrics) null else lyrics?.synced,
                                                        )
                                                    )
                                                }
                                            }
                                        )

                                        if (isShowingSynchronizedLyrics) {
                                            MenuEntry(
                                                icon = R.drawable.sync,
                                                text = stringResource(R.string.pick_from) + " LrcLib.net",
                                                onClick = {
                                                    menuState.hide()
                                                    isPicking = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        .padding(all = 8.dp)
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}


@Composable
fun SelectLyricFromTrack(
    tracks: List<Track>,
    mediaId: String,
    lyrics: Lyrics?
) {
    val menuState = LocalMenuState.current

    menuState.display {
        Menu {
            MenuEntry(
                icon = R.drawable.chevron_back,
                text = stringResource(R.string.cancel),
                onClick = { menuState.hide() }
            )
            tracks.forEach {
                MenuEntry(
                    icon = R.drawable.text,
                    text = "${it.artistName} - ${it.trackName}",
                    secondaryText = "(${stringResource(R.string.sort_duration)} ${
                        it.duration.seconds.toComponents { minutes, seconds, _ ->
                            "$minutes:${seconds.toString().padStart(2, '0')}"
                        }
                    } ${stringResource(R.string.id)} ${it.id}) ",
                    onClick = {
                        menuState.hide()
                        transaction {
                            Database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = lyrics?.fixed,
                                    synced = it.syncedLyrics.orEmpty()
                                )
                            )
                        }
                    }
                )
            }
            MenuEntry(
                icon = R.drawable.chevron_back,
                text = stringResource(R.string.cancel),
                onClick = { menuState.hide() }
            )
        }
    }
}