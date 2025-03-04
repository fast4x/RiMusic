package it.fast4x.rimusic.ui.screens.player

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.valentinilk.shimmer.shimmer
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.NextBody
import it.fast4x.environment.requests.lyrics
import it.fast4x.kugou.KuGou
import it.fast4x.lrclib.LrcLib
import it.fast4x.lrclib.models.Track
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.Languages
import it.fast4x.rimusic.enums.LyricsAlignment
import it.fast4x.rimusic.enums.LyricsBackground
import it.fast4x.rimusic.enums.LyricsColor
import it.fast4x.rimusic.enums.LyricsFontSize
import it.fast4x.rimusic.enums.LyricsHighlight
import it.fast4x.rimusic.enums.LyricsOutline
import it.fast4x.rimusic.enums.PlayerBackgroundColors
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.Romanization
import it.fast4x.rimusic.models.Lyrics
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.components.themed.IconButton
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.Menu
import it.fast4x.rimusic.ui.components.themed.MenuEntry
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.TextPlaceholder
import it.fast4x.rimusic.ui.components.themed.TitleSection
import it.fast4x.rimusic.ui.styling.DefaultDarkColorPalette
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.PureBlackColorPalette
import it.fast4x.rimusic.ui.styling.onOverlayShimmer
import it.fast4x.rimusic.utils.SynchronizedLyrics
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.expandedplayerKey
import it.fast4x.rimusic.utils.getHttpClient
import it.fast4x.rimusic.utils.isShowingSynchronizedLyricsKey
import it.fast4x.rimusic.utils.languageDestination
import it.fast4x.rimusic.utils.languageDestinationName
import it.fast4x.rimusic.utils.lyricsAlignmentKey
import it.fast4x.rimusic.utils.lyricsBackgroundKey
import it.fast4x.rimusic.utils.lyricsColorKey
import it.fast4x.rimusic.utils.lyricsFontSizeKey
import it.fast4x.rimusic.utils.lyricsHighlightKey
import it.fast4x.rimusic.utils.lyricsOutlineKey
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.otherLanguageAppKey
import it.fast4x.rimusic.utils.playerBackgroundColorsKey
import it.fast4x.rimusic.utils.playerEnableLyricsPopupMessageKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.romanizationKey
import it.fast4x.rimusic.utils.showBackgroundLyricsKey
import it.fast4x.rimusic.utils.showSecondLineKey
import it.fast4x.rimusic.utils.showlyricsthumbnailKey
import it.fast4x.rimusic.utils.textCopyToClipboard
import it.fast4x.rimusic.utils.verticalFadingEdge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.thumbnailShape
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.LyricsSizeDialog
import it.fast4x.rimusic.utils.colorPaletteNameKey
import it.fast4x.rimusic.utils.conditional
import it.fast4x.rimusic.utils.effectRotationKey
import it.fast4x.rimusic.utils.jumpPreviousKey
import it.fast4x.rimusic.utils.landscapeControlsKey
import it.fast4x.rimusic.utils.lyricsSizeAnimateKey
import it.fast4x.rimusic.utils.lyricsSizeKey
import it.fast4x.rimusic.utils.lyricsSizeLKey
import it.fast4x.rimusic.utils.playNext
import it.fast4x.rimusic.utils.playPrevious
import timber.log.Timber
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@UnstableApi
@Composable
fun Lyrics(
    mediaId: String,
    isDisplayed: Boolean,
    onDismiss: () -> Unit,
    size: Dp,
    mediaMetadataProvider: () -> MediaMetadata,
    durationProvider: () -> Long,
    ensureSongInserted: () -> Unit,
    modifier: Modifier = Modifier,
    clickLyricsText: Boolean,
    trailingContent: (@Composable () -> Unit)? = null,
    isLandscape: Boolean,
) {
    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val menuState = LocalMenuState.current
        val currentView = LocalView.current
        val binder = LocalPlayerServiceBinder.current

        var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)
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
            PlayerBackgroundColors.BlurredCoverColor
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
        var textTranslated by remember {
            mutableStateOf("")
        }

        var isError by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }
        var isErrorSync by remember(mediaId, isShowingSynchronizedLyrics) {
            mutableStateOf(false)
        }

        var showLanguagesList by remember {
            mutableStateOf(false)
        }

        var translateEnabled by remember {
            mutableStateOf(false)
        }

        var romanization by rememberPreference(romanizationKey, Romanization.Off)
        var showSecondLine by rememberPreference(showSecondLineKey, false)

        var otherLanguageApp by rememberPreference(otherLanguageAppKey, Languages.English)
        var lyricsBackground by rememberPreference(lyricsBackgroundKey, LyricsBackground.Black)

        if (showLanguagesList) {
            translateEnabled = false
            menuState.display {
                Menu {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TitleSection(title = stringResource(R.string.languages))
                    }

                    MenuEntry(
                        icon = R.drawable.translate,
                        text = stringResource(R.string.do_not_translate),
                        secondaryText = "",
                        onClick = {
                            menuState.hide()
                            showLanguagesList = false
                            translateEnabled = false

                        }
                    )
                    MenuEntry(
                        icon = R.drawable.translate,
                        text = stringResource(R.string._default),
                        secondaryText = languageDestinationName(otherLanguageApp),
                        onClick = {
                            menuState.hide()
                            showLanguagesList = false
                            translateEnabled = true

                        }
                    )

                    Languages.entries.forEach {
                        if (it != Languages.System)
                            MenuEntry(
                                icon = R.drawable.translate,
                                text = languageDestinationName(it),
                                secondaryText = "",
                                onClick = {
                                    menuState.hide()
                                    otherLanguageApp = it
                                    showLanguagesList = false
                                    translateEnabled = true

                                }
                            )
                    }
                }
            }
        }

        var languageDestination = languageDestination(otherLanguageApp)

        val translator = Translator(getHttpClient())

        var copyToClipboard by remember {
            mutableStateOf(false)
        }

        if (copyToClipboard) text?.let {
            textCopyToClipboard(it, context)
            copyToClipboard = false
        }

        var copyTranslatedToClipboard by remember {
            mutableStateOf(false)
        }

        if (copyTranslatedToClipboard) textTranslated.let {
            textCopyToClipboard(it, context)
            copyTranslatedToClipboard = false
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
        var lyricsAlignment by rememberPreference(lyricsAlignmentKey, LyricsAlignment.Center)
        var lyricsSizeAnimate by rememberPreference(lyricsSizeAnimateKey, false)
        val mediaMetadata = mediaMetadataProvider()
        var artistName by rememberSaveable { mutableStateOf(mediaMetadata.artist?.toString().orEmpty())}
        var title by rememberSaveable { mutableStateOf(cleanPrefix(mediaMetadata.title?.toString().orEmpty()))}
        var lyricsSize by rememberPreference(lyricsSizeKey, 20f)
        var lyricsSizeL by rememberPreference(lyricsSizeLKey, 20f)
        var customSize = if (isLandscape) lyricsSizeL else lyricsSize
        var showLyricsSizeDialog by rememberSaveable {
            mutableStateOf(false)
        }
        val lightTheme = colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))
        val effectRotationEnabled by rememberPreference(effectRotationKey, true)
        var landscapeControls by rememberPreference(landscapeControlsKey, true)
        var jumpPrevious by rememberPreference(jumpPreviousKey,"3")
        var isRotated by rememberSaveable { mutableStateOf(false) }
        val rotationAngle by animateFloatAsState(
            targetValue = if (isRotated) 360F else 0f,
            animationSpec = tween(durationMillis = 200), label = ""
        )
        val colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)

        if (showLyricsSizeDialog) {
            LyricsSizeDialog(
                onDismiss = { showLyricsSizeDialog = false},
                sizeValue = { lyricsSize = it },
                sizeValueL = { lyricsSizeL = it}
            )
        }

        LaunchedEffect(mediaMetadata.title, mediaMetadata.artist) {
            artistName = mediaMetadata.artist?.toString().orEmpty()
            title = cleanPrefix(mediaMetadata.title?.toString().orEmpty())
        }

        fun translateLyricsWithRomanization(output: MutableState<String>, textToTranslate: String, isSync: Boolean, destinationLanguage: Language = Language.AUTO) = @Composable{
            LaunchedEffect(showSecondLine, romanization, textToTranslate, destinationLanguage){
                var destLanguage = destinationLanguage
                val result = withContext(Dispatchers.IO) {
                    try {

                        /** used to find the source language of the text and detect CHINESE_TRADITIONAL*/
                        val helperTranslation = translator.translate(
                            textToTranslate,
                            Language.CHINESE_TRADITIONAL,
                            Language.AUTO
                        )
                        if(destinationLanguage == Language.AUTO){
                            destLanguage = if(helperTranslation.translatedText == textToTranslate){
                                Language.CHINESE_TRADITIONAL
                            } else {
                                helperTranslation.sourceLanguage
                            }
                        }
                        val mainTranslation = translator.translate(
                            textToTranslate,
                            destLanguage,
                            Language.AUTO
                        )
                        val outputText = if (textToTranslate == "") {
                            ""
                       }
                        else if (!showSecondLine || (mainTranslation.sourceText == mainTranslation.translatedText)){
                            if (romanization == Romanization.Off) {
                                if (translateEnabled) mainTranslation.translatedText else textToTranslate
                            }
                            else if (romanization == Romanization.Original) if (helperTranslation.sourceText == helperTranslation.translatedText) helperTranslation.sourcePronunciation else mainTranslation.sourcePronunciation ?: mainTranslation.sourceText
                            else if (romanization == Romanization.Translated) mainTranslation.translatedPronunciation ?: mainTranslation.translatedText
                            else if (helperTranslation.sourceText == helperTranslation.translatedText) helperTranslation.sourcePronunciation else mainTranslation.sourcePronunciation ?: mainTranslation.sourceText
                        } else {
                            if (romanization == Romanization.Off) {
                                textToTranslate + "\\n[${mainTranslation.translatedText}]"
                            } else if (romanization == Romanization.Original) {
                                if (helperTranslation.sourceText == helperTranslation.translatedText){
                                    helperTranslation.sourcePronunciation
                                } else {mainTranslation.sourcePronunciation ?: mainTranslation.sourceText} + "\\n[${mainTranslation.translatedText}]"
                            } else if (romanization == Romanization.Translated) {
                                textToTranslate + "\\n[${mainTranslation.translatedPronunciation ?: mainTranslation.translatedText}]"
                            } else
                                if (helperTranslation.sourceText == helperTranslation.translatedText){
                                    helperTranslation.sourcePronunciation
                                } else {mainTranslation.sourcePronunciation ?: mainTranslation.sourceText} + "\\n[${mainTranslation.translatedPronunciation ?: mainTranslation.translatedText}]"
                        }
                        outputText?.replace("\\r","\r")?.replace("\\n","\n")

                    } catch (e: Exception) {
                        if(isSync){
                            Timber.e("Lyrics sync translation ${e.stackTraceToString()}")
                        } else {
                            Timber.e("Lyrics not sync translation ${e.stackTraceToString()}")
                        }
                    }
                }
                val translatedText =
                    if (result.toString() == "kotlin.Unit") "" else result.toString()
                showPlaceholder = false
                output.value = translatedText

                textTranslated = translatedText
            }
        }


        LaunchedEffect(mediaId, isShowingSynchronizedLyrics, checkLyrics) {
            withContext(Dispatchers.IO) {

                Database.lyrics(mediaId).collect { currentLyrics ->
                    if (isShowingSynchronizedLyrics && currentLyrics?.synced == null) {
                        lyrics = null
                        var duration = withContext(Dispatchers.Main) {
                            durationProvider()
                        }

                        while (duration == C.TIME_UNSET) {
                            delay(100)
                            duration = withContext(Dispatchers.Main) {
                                durationProvider()
                            }
                        }

                        kotlin.runCatching {
                            LrcLib.lyrics(
                                artist = artistName ?: "",
                                title = title ?: "",
                                duration = duration.milliseconds,
                                album = mediaMetadata.albumTitle?.toString()
                            )?.onSuccess {
                                if ((it?.text?.isNotEmpty() == true || it?.sentences?.isNotEmpty() == true)
                                    && playerEnableLyricsPopupMessage
                                )
                                    coroutineScope.launch {
                                        SmartMessage(
                                            context.resources.getString(R.string.info_lyrics_found_on_s)
                                                .format("LrcLib.net"),
                                            type = PopupType.Success, context = context
                                        )
                                    }
                                else
                                    if (playerEnableLyricsPopupMessage)
                                        coroutineScope.launch {

                                            SmartMessage(
                                                context.resources.getString(R.string.info_lyrics_not_found_on_s).format("LrcLib.net"),
                                                type = PopupType.Error,
                                                durationLong = true,
                                                context = context
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
                                        SmartMessage(
                                            context.resources.getString(R.string.info_lyrics_not_found_on_s_try_on_s)
                                                .format("LrcLib.net", "KuGou.com"),
                                            type = PopupType.Error,
                                            durationLong = true, context = context
                                        )
                                    }

                                checkedLyricsLrc = true

                                kotlin.runCatching {
                                    KuGou.lyrics(
                                        artist = mediaMetadata.artist?.toString() ?: "",
                                        title = cleanPrefix(mediaMetadata.title?.toString() ?: ""),
                                        duration = duration / 1000
                                    )?.onSuccess {
                                        if ((it?.value?.isNotEmpty() == true || it?.sentences?.isNotEmpty() == true)
                                            && playerEnableLyricsPopupMessage
                                        )
                                            coroutineScope.launch {
                                                SmartMessage(
                                                    context.resources.getString(R.string.info_lyrics_found_on_s)
                                                        .format("KuGou.com"),
                                                    type = PopupType.Success, context = context
                                                )
                                            }
                                        else
                                            if (playerEnableLyricsPopupMessage)
                                                coroutineScope.launch {
                                                    SmartMessage(
                                                        context.resources.getString(R.string.info_lyrics_not_found_on_s)
                                                            .format("KuGou.com"),
                                                        type = PopupType.Error,
                                                        durationLong = true, context = context
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
                                                SmartMessage(
                                                    context.resources.getString(R.string.info_lyrics_not_found_on_s)
                                                        .format("KuGou.com"),
                                                    type = PopupType.Error,
                                                    durationLong = true, context = context
                                                )
                                            }

                                        isError = true
                                    }
                                }.onFailure {
                                    Timber.e("Lyrics Kugou get error ${it.stackTraceToString()}")
                                }
                            }
                        }.onFailure {
                            Timber.e("Lyrics get error ${it.stackTraceToString()}")
                        }

                    } else if (!isShowingSynchronizedLyrics && currentLyrics?.fixed == null) {
                        isError = false
                        lyrics = null
                        kotlin.runCatching {
                            Environment.lyrics(NextBody(videoId = mediaId))
                                ?.onSuccess { fixedLyrics ->
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
                        }.onFailure {
                            Timber.e("Lyrics Innertube get error ${it.stackTraceToString()}")
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
                    Database.asyncTransaction {
                        ensureSongInserted()
                        upsert(
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

        @Composable
        fun SelectLyricFromTrack(
            tracks: List<Track>,
            mediaId: String,
            lyrics: Lyrics?
        ) {
            menuState.display {
                Menu {
                    MenuEntry(
                        icon = R.drawable.chevron_back,
                        text = stringResource(R.string.cancel),
                        onClick = { menuState.hide() }
                    )
                    Row{
                        TextField(
                            value = title,
                            onValueChange = {
                                title = it
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.textFieldColors(textColor = colorPalette().text, unfocusedIndicatorColor = colorPalette().text),
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .weight(1f)
                        )
                        TextField(
                            value = artistName,
                            onValueChange = {
                                artistName = it
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.textFieldColors(textColor = colorPalette().text, unfocusedIndicatorColor = colorPalette().text),
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .weight(1f)
                        )
                        IconButton(
                            icon = R.drawable.search,
                            color = Color.Black,
                            onClick = {
                                isPicking = false
                                menuState.hide()
                                isPicking = true
                            },
                            modifier = Modifier
                                .background(shape = RoundedCornerShape(4.dp), color = Color.White)
                                .padding(all = 4.dp)
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                                .weight(0.2f)
                        )
                    }
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
                                Database.asyncTransaction {
                                    upsert(
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
            isPicking = false
        }


        if (isPicking && isShowingSynchronizedLyrics) {
            var loading by remember { mutableStateOf(true) }
            val tracks = remember { mutableStateListOf<Track>() }
            var error by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                kotlin.runCatching {
                    LrcLib.lyrics(
                        artist = artistName,
                        title = title
                    )?.onSuccess {
                        if (it.isNotEmpty() && playerEnableLyricsPopupMessage)
                            coroutineScope.launch {
                                SmartMessage(
                                    context.resources.getString(R.string.info_lyrics_tracks_found_on_s)
                                        .format("LrcLib.net"),
                                    type = PopupType.Success, context = context
                                )
                            }
                        else
                            if (playerEnableLyricsPopupMessage)
                                coroutineScope.launch {
                                    SmartMessage(
                                        context.resources.getString(R.string.info_lyrics_tracks_not_found_on_s)
                                            .format("LrcLib.net"),
                                        type = PopupType.Error,
                                        durationLong = true, context = context
                                    )
                                }
                        if (it.isEmpty()){
                            menuState.display {
                                Menu {
                                    MenuEntry(
                                        icon = R.drawable.chevron_back,
                                        text = stringResource(R.string.cancel),
                                        onClick = { menuState.hide() }
                                    )
                                    Row {
                                        TextField(
                                            value = title,
                                            onValueChange = { it ->
                                                title = it
                                            },
                                            singleLine = true,
                                            colors = TextFieldDefaults.textFieldColors(
                                                textColor = colorPalette().text,
                                                unfocusedIndicatorColor = colorPalette().text
                                            ),
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp)
                                                .weight(1f)
                                        )
                                        TextField(
                                            value = artistName,
                                            onValueChange = { it ->
                                                artistName = it
                                            },
                                            singleLine = true,
                                            colors = TextFieldDefaults.textFieldColors(
                                                textColor = colorPalette().text,
                                                unfocusedIndicatorColor = colorPalette().text
                                            ),
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp)
                                                .weight(1f)
                                        )
                                        IconButton(
                                            icon = R.drawable.search,
                                            color = Color.Black,
                                            onClick = {
                                                isPicking = false
                                                menuState.hide()
                                                isPicking = true
                                            },
                                            modifier = Modifier
                                                .background(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.White
                                                )
                                                .padding(all = 4.dp)
                                                .size(24.dp)
                                                .align(Alignment.CenterVertically)
                                                .weight(0.2f)
                                        )
                                    }
                                }
                            }
                            isPicking = false
                        }

                        tracks.clear()
                        tracks.addAll(it)
                        loading = false
                        error = false
                    }?.onFailure {
                        if (playerEnableLyricsPopupMessage)
                            coroutineScope.launch {
                                SmartMessage(
                                    context.resources.getString(R.string.an_error_has_occurred_while_fetching_the_lyrics)
                                        .format("LrcLib.net"),
                                    type = PopupType.Error,
                                    durationLong = true, context = context
                                )
                            }

                        loading = false
                        error = true
                    } ?: run { loading = false }
                }.onFailure {
                    Timber.e("Lyrics get error 1 ${it.stackTraceToString()}")
                }
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
                SelectLyricFromTrack(tracks = tracks,mediaId = mediaId,lyrics = lyrics)
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
                .clip(thumbnailShape())

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
                    style = typography().xs.center.medium.color(PureBlackColorPalette.text),
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
                            (-thumbnailSize.div(if (!showlyricsthumbnail && !isLandscape) if (trailingContent == null) 2 else 1
                                                else if (trailingContent == null) 3 else 2))
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
                        modifierBG.background(colorPalette().accent)

                    LazyColumn(
                        state = lazyListState,
                        userScrollEnabled = true,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = modifierBG
                            .background(
                                if (isDisplayed && !showlyricsthumbnail) if (lyricsBackground == LyricsBackground.Black) Color.Black.copy(0.6f)
                                else if (lyricsBackground == LyricsBackground.White) Color.White.copy(0.4f)
                                else Color.Transparent else Color.Transparent
                            )
                    ) {

                        item(key = "header", contentType = 0) {
                            Spacer(modifier = Modifier.height(thumbnailSize))
                        }
                        itemsIndexed(
                            items = synchronizedLyrics.sentences
                        ) { index, sentence ->
                            var translatedText by remember { mutableStateOf("") }
                            val trimmedSentence = sentence.second.trim()
                            if (showSecondLine || translateEnabled || romanization != Romanization.Off) {
                                val mutState = remember { mutableStateOf("") }
                                translateLyricsWithRomanization(mutState, trimmedSentence, true, languageDestination)()
                                translatedText = mutState.value
                            } else {
                                    translatedText = trimmedSentence
                            }

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
                                listOf(colorPalette().background2, colorPalette().accent)
                            val Themegradient2 = listOf(
                                colorPalette().background2.copy(0.5f),
                                colorPalette().accent.copy(0.5f)
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
                            val animateSizeText by animateFloatAsState(
                                targetValue = if (index == synchronizedLyrics.index) 1.05f else 0.85f,
                                animationSpec = tween(500, easing = LinearOutSlowInEasing),
                                label = ""
                            )
                            val animateOpacity by animateFloatAsState(
                                targetValue = if (index == synchronizedLyrics.index) 1f else 0.6f,
                                animationSpec = tween(500, easing = LinearOutSlowInEasing),
                                label = ""
                            )
                            //Rainbow Shimmer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                ////Lyrics Outline Synced
                                if (!showlyricsthumbnail) {
                                    if (lyricsOutline == LyricsOutline.None) {

                                    } else if ((lyricsOutline == LyricsOutline.White) || (lyricsOutline == LyricsOutline.Black) || (lyricsOutline == LyricsOutline.Thememode))
                                        BasicText(
                                            text = translatedText,
                                            style = TextStyle(
                                                fontWeight = FontWeight.Medium,
                                                color = if (lyricsOutline == LyricsOutline.White) Color.White
                                                else if (lyricsOutline == LyricsOutline.Black) Color.Black
                                                else if (lyricsOutline == LyricsOutline.Thememode)
                                                    if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                    else Color.Black
                                                else Color.Transparent,
                                                fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                                else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                                else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                                else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                                else customSize.sp,
                                                textAlign = lyricsAlignment.selected,
                                                drawStyle = Stroke(
                                                    width = if (fontSize == LyricsFontSize.Large)
                                                        if (lyricsOutline == LyricsOutline.White) 6.0f
                                                        else if (lyricsOutline == LyricsOutline.Black) 10.0f
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) 6.0f
                                                            else 10.0f
                                                        else 0f
                                                    else if (fontSize == LyricsFontSize.Heavy)
                                                        if (lyricsOutline == LyricsOutline.White) 3f
                                                        else if (lyricsOutline == LyricsOutline.Black) 7f
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) 3f
                                                            else 7f
                                                        else 0f
                                                    else if (fontSize == LyricsFontSize.Medium)
                                                        if (lyricsOutline == LyricsOutline.White) 2f
                                                        else if (lyricsOutline == LyricsOutline.Black) 6f
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) 2f
                                                            else 6f
                                                        else 0f
                                                    else if (fontSize == LyricsFontSize.Light)
                                                        if (lyricsOutline == LyricsOutline.White) 1.3f
                                                        else if (lyricsOutline == LyricsOutline.Black) 5.3f
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) 1.3f
                                                            else 5.3f
                                                        else 0f
                                                    else
                                                        if (lyricsOutline == LyricsOutline.White) (customSize/5.6f)
                                                        else if (lyricsOutline == LyricsOutline.Black) (customSize/3.4f)
                                                        else if (lyricsOutline == LyricsOutline.Thememode)
                                                            if (colorPaletteMode == ColorPaletteMode.Light) (customSize/5.6f)
                                                            else (customSize/3.4f)
                                                        else 0f,
                                                    join = StrokeJoin.Round
                                                )
                                            ),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp, horizontal = 32.dp)
                                                .conditional(lyricsSizeAnimate) { padding(vertical = 4.dp) }
                                                .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                                .conditional(lyricsSizeAnimate) {
                                                    graphicsLayer {
                                                        transformOrigin =
                                                            if (lyricsAlignment == LyricsAlignment.Center) TransformOrigin(
                                                                0.5f,
                                                                0.5f
                                                            )
                                                            else if (lyricsAlignment == LyricsAlignment.Left) TransformOrigin(
                                                                0f,
                                                                0.5f
                                                            )
                                                            else TransformOrigin(1f, 0.5f)
                                                        scaleY = animateSizeText
                                                        scaleX = animateSizeText
                                                    }
                                                }
                                                .graphicsLayer{
                                                    alpha = animateOpacity
                                                }
                                        )
                                    else if (lyricsOutline == LyricsOutline.Rainbow)
                                        BasicText(
                                            text = translatedText,
                                            style = TextStyle(
                                                textAlign = lyricsAlignment.selected,
                                                brush = if (lightTheme) brushrainbow else brushrainbowdark,
                                                fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                                else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                                else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                                else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                                else customSize.sp,
                                                fontWeight = FontWeight.Medium,
                                                drawStyle = Stroke(
                                                    width = if (fontSize == LyricsFontSize.Large) if (index == synchronizedLyrics.index) 10.0f else 6f
                                                    else if (fontSize == LyricsFontSize.Heavy) if (index == synchronizedLyrics.index) 7f else 5f
                                                    else if (fontSize == LyricsFontSize.Medium) if (index == synchronizedLyrics.index) 6f else 4f
                                                    else if (fontSize == LyricsFontSize.Light) if (index == synchronizedLyrics.index) 5.3f else 3.3f
                                                    else if (index == synchronizedLyrics.index) (customSize/3.4f) else (customSize/5.6f),
                                                    join = StrokeJoin.Round
                                                )
                                            ),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp, horizontal = 32.dp)
                                                .conditional(lyricsSizeAnimate) { padding(vertical = 4.dp) }
                                                .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                                .conditional(lyricsSizeAnimate) {
                                                    graphicsLayer {
                                                        transformOrigin =
                                                            if (lyricsAlignment == LyricsAlignment.Center) TransformOrigin(
                                                                0.5f,
                                                                0.5f
                                                            )
                                                            else if (lyricsAlignment == LyricsAlignment.Left) TransformOrigin(
                                                                0f,
                                                                0.5f
                                                            )
                                                            else TransformOrigin(1f, 0.5f)
                                                        scaleY = animateSizeText
                                                        scaleX = animateSizeText
                                                    }
                                                }
                                                .graphicsLayer{
                                                    alpha = animateOpacity
                                                }
                                        )
                                    else //For Glow Outline//
                                        BasicText(
                                            text = translatedText,
                                            style = TextStyle(
                                                fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                                else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                                else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                                else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                                else customSize.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = lyricsAlignment.selected,
                                                color = if ((lyricsColor == LyricsColor.Thememode || lyricsColor == LyricsColor.White || lyricsColor == LyricsColor.Black) || lyricsColor == LyricsColor.Accent)
                                                    Color.White.copy(0.3f) else Color.Transparent,
                                                shadow = Shadow(
                                                    color = if (index == synchronizedLyrics.index)
                                                        if (lyricsColor == LyricsColor.Thememode) Color.White.copy(
                                                            0.3f
                                                        ).compositeOver(colorPalette().text)
                                                        else if (lyricsColor == LyricsColor.White) Color.White.copy(
                                                            0.3f
                                                        ).compositeOver(Color.White)
                                                        else if (lyricsColor == LyricsColor.Black) Color.White.copy(
                                                            0.3f
                                                        ).compositeOver(Color.Black)
                                                        else if (lyricsColor == LyricsColor.Accent) Color.White.copy(
                                                            0.3f
                                                        ).compositeOver(colorPalette().accent)
                                                        else Color.Transparent
                                                    else Color.Transparent,
                                                    offset = Offset(0f, 0f), blurRadius = 25f
                                                ),
                                            ),
                                            modifier = Modifier
                                                .padding(vertical = 4.dp, horizontal = 32.dp)
                                                .conditional(lyricsSizeAnimate) { padding(vertical = 4.dp) }
                                                .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                                .conditional(lyricsSizeAnimate) {
                                                    graphicsLayer {
                                                        transformOrigin =
                                                            if (lyricsAlignment == LyricsAlignment.Center) TransformOrigin(
                                                                0.5f,
                                                                0.5f
                                                            )
                                                            else if (lyricsAlignment == LyricsAlignment.Left) TransformOrigin(
                                                                0f,
                                                                0.5f
                                                            )
                                                            else TransformOrigin(1f, 0.5f)
                                                        scaleY = animateSizeText
                                                        scaleX = animateSizeText
                                                    }
                                                }

                                        )
                                }
                                if (showlyricsthumbnail) {
                                    BasicText(
                                        text = translatedText,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Medium,
                                            color = if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled,
                                            fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                                       else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                                       else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                                       else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                                       else customSize.sp,
                                            textAlign = lyricsAlignment.selected,
                                        ),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 32.dp)
                                            .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = if (clickLyricsText) ripple(true) else null,
                                                onClick = {
                                                    if (clickLyricsText)
                                                        binder?.player?.seekTo(sentence.first)
                                                    else onDismiss()
                                                }
                                            )
                                    )
                                }
                                else if ((lyricsColor == LyricsColor.White) || (lyricsColor == LyricsColor.Black) || (lyricsColor == LyricsColor.Accent) || (lyricsColor == LyricsColor.Thememode)) {
                                    BasicText(
                                        text = translatedText,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Medium,
                                            color = if (lyricsColor == LyricsColor.White) Color.White
                                            else if (lyricsColor == LyricsColor.Black) Color.Black
                                            else if (lyricsColor == LyricsColor.Thememode) colorPalette().text
                                            else colorPalette().accent,
                                            fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                            else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                            else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                            else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                            else customSize.sp,
                                            textAlign = lyricsAlignment.selected,
                                        ),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 32.dp)
                                            .conditional(lyricsSizeAnimate) { padding(vertical = 4.dp) }
                                            .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                            .conditional(lyricsSizeAnimate) {
                                                graphicsLayer {
                                                    transformOrigin =
                                                        if (lyricsAlignment == LyricsAlignment.Center) TransformOrigin(
                                                            0.5f,
                                                            0.5f
                                                        )
                                                        else if (lyricsAlignment == LyricsAlignment.Left) TransformOrigin(
                                                            0f,
                                                            0.5f
                                                        )
                                                        else TransformOrigin(1f, 0.5f)
                                                    scaleY = animateSizeText
                                                    scaleX = animateSizeText
                                                }
                                            }
                                            .graphicsLayer{
                                                alpha = animateOpacity
                                            }
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = if (clickLyricsText) ripple(true) else null,
                                                onClick = {
                                                    if (clickLyricsText)
                                                        binder?.player?.seekTo(sentence.first)
                                                    else onDismiss()
                                                }
                                            )
                                            .background(
                                                if (index == synchronizedLyrics.index) if (lyricsHighlight == LyricsHighlight.White) Color.White.copy(
                                                    0.5f
                                                ) else if (lyricsHighlight == LyricsHighlight.Black) Color.Black.copy(
                                                    0.5f
                                                ) else Color.Transparent else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .conditional(lyricsHighlight != LyricsHighlight.None) { fillMaxWidth() }
                                    )
                                }
                                else
                                    BasicText(
                                        text = translatedText,
                                        style = TextStyle(
                                            brush = if (lightTheme) brushrainbow else brushrainbowdark,
                                            fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                                       else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                                       else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                                       else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                                       else customSize.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = lyricsAlignment.selected
                                        ),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 32.dp)
                                            .conditional(lyricsSizeAnimate){padding(vertical = 4.dp)}
                                            .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                            .conditional(lyricsSizeAnimate){
                                                graphicsLayer {
                                                    transformOrigin = if (lyricsAlignment == LyricsAlignment.Center) TransformOrigin(0.5f,0.5f)
                                                    else if (lyricsAlignment == LyricsAlignment.Left) TransformOrigin(0f,0.5f)
                                                    else TransformOrigin(1f,0.5f)
                                                    scaleY = if (index == synchronizedLyrics.index) 1.1f else 0.9f
                                                    scaleX = if (index == synchronizedLyrics.index) 1.1f else 0.9f
                                                }
                                            }
                                            .graphicsLayer{
                                                alpha = animateOpacity
                                            }
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = if (clickLyricsText) ripple(true) else null,
                                                onClick = {
                                                    if (clickLyricsText)
                                                        binder?.player?.seekTo(sentence.first)
                                                    else onDismiss()
                                                }
                                            )
                                    )
                                /*else
                                  BasicText(
                                     text = translatedText,
                                     style = TextStyle(
                                         brush = brushtheme
                                     ).merge(when (fontSize) {
                                         LyricsFontSize.Light ->
                                             typography().m.center.medium.color(
                                                 if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                 else colorPalette().text.copy(0.6f)
                                             )
                                         LyricsFontSize.Medium ->
                                             typography().l.center.medium.color(
                                                 if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                 else colorPalette().text.copy(0.6f)
                                             )
                                         LyricsFontSize.Heavy ->
                                             typography().xl.center.medium.color(
                                                 if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                 else colorPalette().text.copy(0.6f)
                                             )
                                         LyricsFontSize.Large ->
                                             typography().xlxl.center.medium.color(
                                                 if (index == synchronizedLyrics.index) PureBlackColorPalette.text
                                                 else colorPalette().text.copy(0.6f)
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

                            }
                        }
                        item(key = "footer", contentType = 0) {
                            Spacer(modifier = Modifier.height(thumbnailSize))
                        }
                    }
                } else {
                    var translatedText by remember { mutableStateOf("") }
                    if (showSecondLine || translateEnabled || romanization != Romanization.Off) {
                        val mutState = remember { mutableStateOf("") }
                        translateLyricsWithRomanization(mutState, text, false, languageDestination)()
                        translatedText = mutState.value
                    } else {
                        translatedText = text
                    }

                    Column(
                        modifier = Modifier
                            .verticalFadingEdge()
                            .background(
                                if (isDisplayed && !showlyricsthumbnail) if (lyricsBackground == LyricsBackground.Black) Color.Black.copy(
                                    0.4f
                                ) else if (lyricsBackground == LyricsBackground.White) Color.White.copy(
                                    0.4f
                                ) else Color.Transparent else Color.Transparent
                            ),
                    ) {
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

                            if (!showlyricsthumbnail) {
                                if ((lyricsOutline == LyricsOutline.None) || (lyricsOutline == LyricsOutline.Glow)) {

                                } else if (lyricsOutline == LyricsOutline.Thememode || (lyricsOutline == LyricsOutline.White) || (lyricsOutline == LyricsOutline.Black))
                                    BasicText(
                                        text = translatedText,
                                        style = TextStyle(
                                            textAlign = lyricsAlignment.selected,
                                            fontWeight = FontWeight.Medium,
                                            color = if (lyricsOutline == LyricsOutline.White) Color.White
                                            else if (lyricsOutline == LyricsOutline.Black) Color.Black
                                            else if (lyricsOutline == LyricsOutline.Thememode)
                                                if (colorPaletteMode == ColorPaletteMode.Light) Color.White
                                                else Color.Black
                                            else Color.Transparent,
                                            fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                            else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                            else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                            else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                            else customSize.sp,
                                            drawStyle = Stroke(
                                                width = if (fontSize == LyricsFontSize.Large)
                                                    if (lyricsOutline == LyricsOutline.White) 6.0f
                                                    else if (lyricsOutline == LyricsOutline.Black) 10.0f
                                                    else if (lyricsOutline == LyricsOutline.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) 6.0f
                                                        else 10.0f
                                                    else 0f
                                                else if (fontSize == LyricsFontSize.Heavy)
                                                    if (lyricsOutline == LyricsOutline.White) 3f
                                                    else if (lyricsOutline == LyricsOutline.Black) 7f
                                                    else if (lyricsOutline == LyricsOutline.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) 3f
                                                        else 7f
                                                    else 0f
                                                else if (fontSize == LyricsFontSize.Medium)
                                                    if (lyricsOutline == LyricsOutline.White) 2f
                                                    else if (lyricsOutline == LyricsOutline.Black) 6f
                                                    else if (lyricsOutline == LyricsOutline.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) 2f
                                                        else 6f
                                                    else 0f
                                                else if (fontSize == LyricsFontSize.Light)
                                                    if (lyricsOutline == LyricsOutline.White) 1.3f
                                                    else if (lyricsOutline == LyricsOutline.Black) 5.3f
                                                    else if (lyricsOutline == LyricsOutline.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) 1.3f
                                                        else 5.3f
                                                    else 0f
                                                else
                                                    if (lyricsOutline == LyricsOutline.White) (customSize/5.6f)
                                                    else if (lyricsOutline == LyricsOutline.Black) (customSize/3.4f)
                                                    else if (lyricsOutline == LyricsOutline.Thememode)
                                                        if (colorPaletteMode == ColorPaletteMode.Light) (customSize/5.6f)
                                                        else (customSize/3.4f)
                                                    else 0f,
                                                join = StrokeJoin.Round
                                            )
                                        ),
                                        modifier = Modifier
                                            .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                    )
                                else
                                    BasicText(
                                        text = translatedText,
                                        style = TextStyle(
                                            textAlign = lyricsAlignment.selected,
                                            brush = if (lightTheme) brushrainbow else brushrainbowdark,
                                            fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                            else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                            else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                            else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                            else customSize.sp,
                                            fontWeight = FontWeight.Medium,
                                            drawStyle = Stroke(
                                                width = if (fontSize == LyricsFontSize.Large) 10f
                                                else if (fontSize == LyricsFontSize.Heavy) 7f
                                                else if (fontSize == LyricsFontSize.Medium) 6f
                                                else if (fontSize == LyricsFontSize.Light) 5.3f
                                                else (customSize/3.4f),
                                                join = StrokeJoin.Round
                                            )
                                        ),
                                        modifier = Modifier
                                            .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                    )
                            }

                            if (showlyricsthumbnail) {
                                BasicText(
                                    text = translatedText,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Medium,
                                        color = PureBlackColorPalette.text,
                                        fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                        else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                        else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                        else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                        else customSize.sp,
                                        textAlign = lyricsAlignment.selected,
                                    ),
                                    modifier = Modifier
                                        .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                )
                            }
                            else if ((lyricsColor == LyricsColor.Thememode) || (lyricsColor == LyricsColor.White) || (lyricsColor == LyricsColor.Black) || (lyricsColor == LyricsColor.Accent))
                                BasicText(
                                    text = translatedText,
                                    style = TextStyle (
                                        fontWeight = FontWeight.Medium,
                                        color = if (lyricsColor == LyricsColor.White) Color.White
                                                else if (lyricsColor == LyricsColor.Black) Color.Black
                                                else if (lyricsColor == LyricsColor.Thememode) colorPalette().text
                                                else colorPalette().accent,
                                        fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                                   else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                                   else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                                   else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                                   else customSize.sp,
                                        textAlign = lyricsAlignment.selected,
                                    ),
                                    modifier = Modifier
                                        .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                )
                            else
                                BasicText(
                                    text = translatedText,
                                    style = TextStyle(
                                        brush = if (lightTheme) brushrainbow else brushrainbowdark,
                                        fontSize = if (fontSize == LyricsFontSize.Light) typography().m.fontSize
                                                   else if (fontSize == LyricsFontSize.Medium) typography().l.fontSize
                                                   else if (fontSize == LyricsFontSize.Heavy) typography().xl.fontSize
                                                   else if (fontSize == LyricsFontSize.Large) typography().xlxl.fontSize
                                                   else customSize.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = lyricsAlignment.selected
                                    ),
                                    modifier = Modifier
                                        .align(if (lyricsAlignment == LyricsAlignment.Left) Alignment.CenterStart else if (lyricsAlignment == LyricsAlignment.Right) Alignment.CenterEnd else Alignment.Center)
                                )
                            //Lyrics Outline Non Synced
                        }
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
                            color = colorPalette().onOverlayShimmer,
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
                if (isLandscape && !showlyricsthumbnail)
                    IconButton(
                        icon = R.drawable.chevron_back,
                        color = colorPalette().accent,
                        enabled = true,
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .align(Alignment.BottomStart)
                            .size(30.dp)
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
            if (!showlyricsthumbnail && isDisplayed && isLandscape && landscapeControls) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent,if (lightTheme) Color.White.copy(0.5f) else Color.Black.copy(0.5f)),
                                startY = 0f,
                                endY = POSITIVE_INFINITY
                            ),
                        )
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                ){
                    Image(
                        painter = painterResource(R.drawable.play_skip_back),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette().text),
                        modifier = Modifier
                            .clickable(
                                indication = ripple(bounded = false),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    if (jumpPrevious == "") jumpPrevious = "0"
                                    if(binder?.player?.hasPreviousMediaItem() == false || (jumpPrevious != "0" && (binder?.player?.currentPosition ?: 0) > jumpPrevious.toInt() * 1000)
                                    ){
                                        binder?.player?.seekTo(0)
                                    }
                                    else binder?.player?.playPrevious()
                                    if (effectRotationEnabled) isRotated = !isRotated
                                }
                            )
                            .rotate(rotationAngle)
                            .padding(horizontal = 15.dp)
                            .size(30.dp)

                    )
                    Box {
                        Box(modifier = Modifier
                            .align(Alignment.Center)
                            .size(45.dp)
                            .background(colorPalette().accent, RoundedCornerShape(15.dp))
                        ){}
                        Image(
                            painter = painterResource(if (binder?.player?.isPlaying == true) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(if (colorPaletteName == ColorPaletteName.PureBlack) Color.Black else colorPalette().text),
                            modifier = Modifier
                                .clickable(
                                    indication = ripple(bounded = false),
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        if (binder?.player?.isPlaying == true) {
                                            binder.callPause({})
                                        } else {
                                            binder?.player?.play()
                                        }
                                    },
                                )
                                .align(Alignment.Center)
                                .rotate(rotationAngle)
                                .padding(horizontal = 15.dp)
                                .size(36.dp)

                        )
                    }
                    Image(
                        painter = painterResource(R.drawable.play_skip_forward),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette().text),
                        modifier = Modifier
                            .clickable(
                                indication = ripple(bounded = false),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    binder?.player?.playNext()
                                    if (effectRotationEnabled) isRotated = !isRotated
                                }
                            )
                            .rotate(rotationAngle)
                            .padding(horizontal = 15.dp)
                            .size(30.dp)

                    )
                }

            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.2f)
            ) {


                if (showlyricsthumbnail)
                    IconButton(
                        icon = R.drawable.translate,
                        color = if (translateEnabled == true) colorPalette().text else colorPalette().textDisabled,
                        enabled = true,
                        onClick = {
                            translateEnabled = !translateEnabled
                            if (translateEnabled) showLanguagesList = true
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
                                        if (isLandscape && !showlyricsthumbnail){
                                            MenuEntry(
                                                icon = if (landscapeControls) R.drawable.checkmark else R.drawable.play,
                                                text = stringResource(R.string.toggle_controls_landscape),
                                                enabled = true,
                                                onClick = {
                                                    menuState.hide()
                                                    landscapeControls = !landscapeControls
                                                }
                                            )
                                        }
                                        MenuEntry(
                                            icon = R.drawable.text,
                                            enabled = true,
                                            text = stringResource(R.string.lyricsalignment),
                                            onClick = {
                                                menuState.display {
                                                    Menu {
                                                        MenuEntry(
                                                            icon = R.drawable.arrow_left,
                                                            text = stringResource(R.string.direction_left),
                                                            secondaryText = "",
                                                            onClick = {
                                                                menuState.hide()
                                                                lyricsAlignment = LyricsAlignment.Left
                                                            }
                                                        )
                                                        MenuEntry(
                                                            icon = R.drawable.arrow_down,
                                                            text = stringResource(R.string.center),
                                                            secondaryText = "",
                                                            onClick = {
                                                                menuState.hide()
                                                                lyricsAlignment = LyricsAlignment.Center
                                                            }
                                                        )
                                                        MenuEntry(
                                                            icon = R.drawable.arrow_right,
                                                            text = stringResource(R.string.direction_right),
                                                            secondaryText = "",
                                                            onClick = {
                                                                menuState.hide()
                                                                lyricsAlignment = LyricsAlignment.Right
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        )

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
                                                            MenuEntry(
                                                                icon = R.drawable.text,
                                                                text = stringResource(R.string.custom),
                                                                secondaryText = stringResource(R.string.lyricsSizeSecondary),
                                                                onClick = {
                                                                    menuState.hide()
                                                                    fontSize = LyricsFontSize.Custom
                                                                },
                                                                onLongClick = {showLyricsSizeDialog = !showLyricsSizeDialog},
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
                                                            if (isShowingSynchronizedLyrics) {
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
                                                }
                                            )

                                        //if (!showlyricsthumbnail)
                                            MenuEntry(
                                                icon = R.drawable.translate,
                                                text = stringResource(R.string.translate_to, otherLanguageApp),
                                                enabled = true,
                                                onClick = {
                                                    menuState.hide()
                                                    translateEnabled = true
                                                }
                                            )
                                        MenuEntry(
                                            icon = R.drawable.translate,
                                            text = stringResource(R.string.translate_to_other_language),
                                            enabled = true,
                                            onClick = {
                                                menuState.hide()
                                                showLanguagesList = true
                                            }
                                        )

                                        MenuEntry(
                                            icon = if (romanization == Romanization.Original || romanization == Romanization.Translated || romanization == Romanization.Both) R.drawable.checkmark else R.drawable.text,
                                            enabled = true,
                                            text = stringResource(R.string.toggle_romanization),
                                            onClick = {
                                                menuState.display {
                                                    Menu {
                                                        MenuEntry(
                                                            icon = if (romanization == Romanization.Off) R.drawable.checkmark else R.drawable.text,
                                                            text = stringResource(R.string.turn_off),
                                                            secondaryText = "",
                                                            onClick = {
                                                                menuState.hide()
                                                                romanization =
                                                                    Romanization.Off
                                                            }
                                                        )
                                                        MenuEntry(
                                                            icon = if (romanization == Romanization.Original || (romanization == Romanization.Both && !showSecondLine)) R.drawable.checkmark else R.drawable.text,
                                                            text = stringResource(R.string.original_lyrics),
                                                            secondaryText = "",
                                                            onClick = {
                                                                menuState.hide()
                                                                romanization =
                                                                    Romanization.Original
                                                            }
                                                        )
                                                        MenuEntry(
                                                            icon = if (romanization == Romanization.Translated) R.drawable.checkmark else R.drawable.text,
                                                            text = stringResource(R.string.translated_lyrics),
                                                            secondaryText = "",
                                                            onClick = {
                                                                menuState.hide()
                                                                romanization =
                                                                    Romanization.Translated
                                                            }
                                                        )
                                                        if (showSecondLine) {
                                                            MenuEntry(
                                                                icon = if (romanization == Romanization.Both) R.drawable.checkmark else R.drawable.text,
                                                                text = stringResource(R.string.both),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    romanization =
                                                                        Romanization.Both
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                        MenuEntry(
                                            icon = if (showSecondLine) R.drawable.checkmark else R.drawable.close,
                                            text = stringResource(R.string.showsecondline),
                                            enabled = true,
                                            onClick = {
                                                menuState.hide()
                                                showSecondLine = !showSecondLine
                                            }
                                        )

                                        if (!showlyricsthumbnail && isShowingSynchronizedLyrics) {
                                            MenuEntry(
                                                icon = if (lyricsSizeAnimate) R.drawable.checkmark else R.drawable.close,
                                                text = stringResource(R.string.lyricsanimate),
                                                enabled = true,
                                                onClick = {
                                                    menuState.hide()
                                                    lyricsSizeAnimate = !lyricsSizeAnimate
                                                }
                                            )
                                        }

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

                                        if (!showlyricsthumbnail)
                                            MenuEntry(
                                                icon = R.drawable.droplet,
                                                enabled = true,
                                                text = stringResource(R.string.lyricsbackground),
                                                onClick = {
                                                    menuState.display {
                                                        Menu {
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.none),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsBackground =
                                                                        LyricsBackground.None
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.white),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsBackground =
                                                                        LyricsBackground.White
                                                                }
                                                            )
                                                            MenuEntry(
                                                                icon = R.drawable.droplet,
                                                                text = stringResource(R.string.black),
                                                                secondaryText = "",
                                                                onClick = {
                                                                    menuState.hide()
                                                                    lyricsBackground =
                                                                        LyricsBackground.Black
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
                                            icon = R.drawable.copy,
                                            text = stringResource(R.string.copy_translated_lyrics),
                                            onClick = {
                                                menuState.hide()
                                                copyTranslatedToClipboard = true
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
                                                                "${cleanPrefix(mediaMetadata.title.toString())} ${mediaMetadata.artist} lyrics"
                                                            )
                                                        }
                                                    )
                                                } catch (e: ActivityNotFoundException) {
                                                    SmartMessage(
                                                        context.resources.getString(R.string.info_not_find_app_browse_internet),
                                                        type = PopupType.Warning, context = context
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
                                                Database.asyncTransaction {
                                                    upsert(
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


/*@Composable
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
                        Database.asyncTransaction {
                            upsert(
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
}*/