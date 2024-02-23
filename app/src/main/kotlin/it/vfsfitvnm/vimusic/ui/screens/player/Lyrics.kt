package it.vfsfitvnm.vimusic.ui.screens.player

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.valentinilk.shimmer.shimmer
import io.ktor.client.HttpClient
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.bodies.NextBody
import it.vfsfitvnm.innertube.requests.lyrics
import it.vfsfitvnm.kugou.KuGou
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.ArtistSortBy
import it.vfsfitvnm.vimusic.enums.Languages
import it.vfsfitvnm.vimusic.models.Lyrics
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.ui.components.LocalMenuState
import it.vfsfitvnm.vimusic.ui.components.themed.IconButton
import it.vfsfitvnm.vimusic.ui.components.themed.InputTextDialog
import it.vfsfitvnm.vimusic.ui.components.themed.Menu
import it.vfsfitvnm.vimusic.ui.components.themed.MenuEntry
import it.vfsfitvnm.vimusic.ui.components.themed.SortMenu
import it.vfsfitvnm.vimusic.ui.components.themed.TextFieldDialog
import it.vfsfitvnm.vimusic.ui.components.themed.TextPlaceholder
import it.vfsfitvnm.vimusic.ui.styling.DefaultDarkColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.PureBlackColorPalette
import it.vfsfitvnm.vimusic.ui.styling.onOverlayShimmer
import it.vfsfitvnm.vimusic.utils.SynchronizedLyrics
import it.vfsfitvnm.vimusic.utils.bold
import it.vfsfitvnm.vimusic.utils.center
import it.vfsfitvnm.vimusic.utils.color
import it.vfsfitvnm.vimusic.utils.getHttpClient
import it.vfsfitvnm.vimusic.utils.isShowingSynchronizedLyricsKey
import it.vfsfitvnm.vimusic.utils.languageAppKey
import it.vfsfitvnm.vimusic.utils.languageDestination
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.toast
import it.vfsfitvnm.vimusic.utils.verticalFadingEdge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.Locale


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
    enableClick: Boolean = false
) {
    AnimatedVisibility(
        visible = isDisplayed,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val (colorPalette, typography) = LocalAppearance.current
        val context = LocalContext.current
        val menuState = LocalMenuState.current
        val currentView = LocalView.current
        val binder = LocalPlayerServiceBinder.current

        var isShowingSynchronizedLyrics by rememberPreference(isShowingSynchronizedLyricsKey, false)

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

        val languageDestination = languageDestination()
        /*
        val languageApp  by rememberPreference(languageAppKey, Languages.English)

        val languageDestination = when (languageApp){
            Languages.Arabic -> Language.ARABIC
            Languages.Bashkir -> Language.BASQUE
            Languages.Catalan -> Language.CATALAN
            Languages.ChineseSimplified -> Language.CHINESE_SIMPLIFIED
            Languages.ChineseTraditional -> Language.CHINESE_TRADITIONAL
            Languages.Czech -> Language.CZECH
            Languages.Dutch -> Language.DUTCH
            Languages.English -> Language.ENGLISH
            Languages.Esperanto -> Language.ESPERANTO
            Languages.Finnish -> Language.FINNISH
            Languages.French -> Language.FRENCH
            Languages.German -> Language.GERMAN
            Languages.Greek -> Language.GREEK
            Languages.Hebrew -> Language.HEBREW_HE
            Languages.Hindi -> Language.HINDI
            Languages.Hungarian -> Language.HUNGARIAN
            Languages.Indonesian -> Language.INDONESIAN
            Languages.Japanese -> Language.JAPANESE
            Languages.Korean -> Language.KOREAN
            Languages.Italian -> Language.ITALIAN
            Languages.Odia -> Language.ODIA
            Languages.Persian -> Language.PERSIAN
            Languages.Polish -> Language.POLISH
            Languages.PortugueseBrazilian -> Language.PORTUGUESE
            Languages.Portuguese -> Language.PORTUGUESE
            Languages.Romanian -> Language.ROMANIAN
            Languages.Russian -> Language.RUSSIAN
            Languages.Sinhala -> Language.SINHALA
            Languages.Spanish -> Language.SPANISH
            Languages.Turkish -> Language.TURKISH
            Languages.Ukrainian -> Language.UKRAINIAN
            Languages.Vietnamese -> Language.VIETNAMESE
            else -> Language.ENGLISH
        }
         */
        //val systemLocale = LocaleListCompat.getDefault().get(0).toString()
        //val systemLangCode = AppCompatDelegate.getApplicationLocales().get(0).toString()
        /*
        val systemLocale = Locale.getDefault().getLanguage()

        val languageDestination = when (systemLocale) {
            "ru" -> Language.RUSSIAN
            "it" -> Language.ITALIAN
            "cs" -> Language.CZECH
            "de" -> Language.GERMAN
            "es" -> Language.SPANISH
            "fr" -> Language.FRENCH
            "ro" -> Language.ROMANIAN
            "tr" -> Language.TURKISH
            "pl" -> Language.POLISH
            else -> {
                Language.ENGLISH
            }
        }
         */

        var translateEnabled by remember {
            mutableStateOf(false)
        }

        val translator = Translator(getHttpClient())


        LaunchedEffect(mediaId, isShowingSynchronizedLyrics) {
            withContext(Dispatchers.IO) {

                Database.lyrics(mediaId).collect {
                    if (isShowingSynchronizedLyrics && it?.synced == null) {
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

                        KuGou.lyrics(
                            artist = mediaMetadata.artist?.toString() ?: "",
                            title = mediaMetadata.title?.toString() ?: "",
                            duration = duration / 1000
                        )?.onSuccess { syncedLyrics ->
                            Database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = it?.fixed,
                                    synced = syncedLyrics?.value ?: ""
                                )

                            )
                        }?.onFailure {
                            isError = true
                        }
                    } else if (!isShowingSynchronizedLyrics && it?.fixed == null) {
                        Innertube.lyrics(NextBody(videoId = mediaId))?.onSuccess { fixedLyrics ->
                            Database.upsert(
                                Lyrics(
                                    songId = mediaId,
                                    fixed = fixedLyrics ?: "",
                                    synced = it?.synced
                                )
                            )
                        }?.onFailure {
                            isError = true
                        }
                    } else {
                        lyrics = it
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
            /*
            TextFieldDialog(
                hintText = stringResource(R.string.enter_the_lyrics),
                initialTextInput = text ?: "",
                singleLine = false,
                maxLines = 10,
                isTextInputValid = { true },
                onDismiss = { isEditing = false },
                onDone = {
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
             */
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
                .background(Color.Black.copy(0.8f))

        ) {
            AnimatedVisibility(
                visible = isError && text == null,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = stringResource(R.string.an_error_has_occurred_while_fetching_the_lyrics),
                    style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = text?.let(String::isEmpty) ?: false,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                BasicText(
                    text = "${
                        if (isShowingSynchronizedLyrics) stringResource(id = R.string.synchronized_lyrics) else stringResource(
                            id = R.string.unsynchronized_lyrics
                        )
                    } " +
                            " ${stringResource(R.string.are_not_available_for_this_song)}",
                    //text = stringResource(R.string.are_not_available_for_this_song)
                    style = typography.xs.center.medium.color(PureBlackColorPalette.text),
                    modifier = Modifier
                        .background(Color.Black.copy(0.4f))
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                )
            }

            if (text?.isNotEmpty() == true) {
                if (isShowingSynchronizedLyrics) {
                    val density = LocalDensity.current
                    val player = LocalPlayerServiceBinder.current?.player
                        ?: return@AnimatedVisibility

                    val synchronizedLyrics = remember(text) {
                        SynchronizedLyrics(KuGou.Lyrics(text).sentences) {
                            player.currentPosition + 50
                        }
                    }


                    val lazyListState = rememberLazyListState(
                        synchronizedLyrics.index,
                        with(density) { size.roundToPx() } / 6)

                    LaunchedEffect(synchronizedLyrics) {
                        val center = with(density) { size.roundToPx() } / 6

                        while (isActive) {
                            delay(50)
                            if (synchronizedLyrics.update()) {
                                lazyListState.animateScrollToItem(
                                    synchronizedLyrics.index,
                                    center
                                )
                            }
                        }
                    }

                    LazyColumn(
                        state = lazyListState,
                        userScrollEnabled = true,
                        contentPadding = PaddingValues(vertical = size / 2),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .verticalFadingEdge()
                    ) {
                        itemsIndexed(items = synchronizedLyrics.sentences) { index, sentence ->
                            var translatedText by remember { mutableStateOf("") }
                            if (translateEnabled == true) {
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
                            BasicText(
                                text = translatedText,
                                style = typography.m.center.medium.color(if (index == synchronizedLyrics.index) PureBlackColorPalette.text else PureBlackColorPalette.textDisabled),
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 32.dp)
                                    .clickable {
                                        //Log.d("mediaItem","${sentence.first}")
                                        if (enableClick)
                                            binder?.player?.seekTo(sentence.first)
                                    }
                            )
                        }
                    }
                } else {
                    var translatedText by remember { mutableStateOf("") }
                    if (translateEnabled == true) {
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

                    BasicText(
                        text = translatedText,
                        style = typography.m.center.medium.color(PureBlackColorPalette.text),
                        modifier = Modifier
                            .verticalFadingEdge()
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth()
                            .padding(vertical = size / 4, horizontal = 32.dp)
                    )
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

            IconButton(
                icon = R.drawable.minmax,
                color = DefaultDarkColorPalette.text,
                enabled = true,
                onClick = onMaximize,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .align(Alignment.BottomStart)
                    .size(24.dp)
            )


            IconButton(
                icon = R.drawable.translate,
                color = if (translateEnabled == true) colorPalette.text else colorPalette.textDisabled,
                enabled = true,
                onClick = {
                    translateEnabled = !translateEnabled
                    if (!translateEnabled) showPlaceholder = false else showPlaceholder = true
                },
                modifier = Modifier
                    .padding(horizontal = 50.dp)
                    .padding(bottom = 10.dp)
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
            )

            /*
            BasicText(
                text = "Language",
                style = typography.l.bold.copy(color = colorPalette.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clickable {
                        menuState.display{
                            Menu {
                                enumValues<Languages>().toList().forEach {
                                    MenuEntry(
                                        icon = R.drawable.translate,
                                        onClick = {
                                            languageDestination = languageDestination(it)
                                            menuState.hide()
                                        },
                                        text = when (it) {
                                            Languages.System -> stringResource(R.string.system_language)
                                            Languages.Arabic -> stringResource(R.string.arabic)
                                            Languages.Bashkir -> stringResource(R.string.bashkir)
                                            Languages.Catalan -> stringResource(R.string.catalan)
                                            Languages.ChineseSimplified -> stringResource(R.string.chinese_simplified)
                                            Languages.ChineseTraditional -> stringResource(R.string.chinese_traditional)
                                            Languages.Czech -> stringResource(R.string.czech)
                                            Languages.Dutch -> stringResource(R.string.lang_dutch)
                                            Languages.English -> stringResource(R.string.english)
                                            Languages.Esperanto -> stringResource(R.string.esperanto)
                                            Languages.Finnish -> stringResource(R.string.lang_finnish)
                                            Languages.French -> stringResource(R.string.french)
                                            Languages.German -> stringResource(R.string.german)
                                            Languages.Greek -> stringResource(R.string.greek)
                                            Languages.Hebrew -> stringResource(R.string.lang_hebrew)
                                            Languages.Hindi -> stringResource(R.string.lang_hindi)
                                            Languages.Hungarian -> stringResource(R.string.hungarian)
                                            Languages.Indonesian -> stringResource(R.string.indonesian)
                                            Languages.Japanese -> stringResource(R.string.lang_japanese)
                                            Languages.Korean -> stringResource(R.string.korean)
                                            Languages.Italian -> stringResource(R.string.italian)
                                            Languages.Odia -> stringResource(R.string.odia)
                                            Languages.Persian -> stringResource(R.string.persian)
                                            Languages.Polish -> stringResource(R.string.polish)
                                            Languages.PortugueseBrazilian -> stringResource(R.string.portuguese_brazilian)
                                            Languages.Portuguese -> stringResource(R.string.portuguese)
                                            Languages.Romanian -> stringResource(R.string.romanian)
                                            //Languages.RomanianEmo -> stringResource(R.string.romanian_emoticons_rom_n)
                                            Languages.Russian -> stringResource(R.string.russian)
                                            Languages.Sinhala -> stringResource(R.string.lang_sinhala)
                                            Languages.Spanish -> stringResource(R.string.spanish)
                                            Languages.Turkish -> stringResource(R.string.turkish)
                                            Languages.Ukrainian -> stringResource(R.string.lang_ukrainian)
                                            Languages.Vietnamese -> "Vietnamese"
                                        }
                                    )
                                }

                            }
                        }
                    }
            )
             */


            Image(
                painter = painterResource(R.drawable.ellipsis_vertical),
                contentDescription = null,
                colorFilter = ColorFilter.tint(DefaultDarkColorPalette.text),
                modifier = Modifier
                    .padding(all = 4.dp)
                    .clickable(
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            menuState.display {
                                Menu {
                                    MenuEntry(
                                        icon = R.drawable.time,
                                        text = stringResource(R.string.show) + " ${
                                            if (isShowingSynchronizedLyrics) stringResource(
                                                R.string.unsynchronized_lyrics
                                            ) else stringResource(R.string.synchronized_lyrics)
                                        }",
                                        secondaryText = if (isShowingSynchronizedLyrics) null else stringResource(
                                            R.string.provided_by
                                        ) + " kugou.com",
                                        onClick = {
                                            menuState.hide()
                                            isShowingSynchronizedLyrics =
                                                !isShowingSynchronizedLyrics
                                        }
                                    )

                                    MenuEntry(
                                        icon = R.drawable.pencil,
                                        text = stringResource(R.string.edit_lyrics),
                                        onClick = {
                                            menuState.hide()
                                            isEditing = true
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
                                                context.toast("Couldn't find an application to browse the Internet")
                                            }
                                        }
                                    )

                                    MenuEntry(
                                        icon = R.drawable.download,
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


