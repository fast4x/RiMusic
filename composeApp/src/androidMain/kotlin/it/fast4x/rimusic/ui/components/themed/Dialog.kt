package it.fast4x.rimusic.ui.components.themed

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.request.ImageRequest
import it.fast4x.compose.persist.persist
import it.fast4x.environment.Environment
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.models.bodies.SearchBody
import it.fast4x.environment.requests.searchPage
import it.fast4x.environment.utils.from
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.Database.Companion.update
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.enums.ValidationType
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.ui.styling.favoritesIcon
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.utils.blurDarkenFactorKey
import it.fast4x.rimusic.utils.blurStrengthKey
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.center
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.utils.VinylSizeKey
import it.fast4x.rimusic.utils.colorPaletteModeKey
import it.fast4x.rimusic.utils.drawCircle
import it.fast4x.rimusic.utils.expandedplayerKey
import it.fast4x.rimusic.utils.fadingedgeKey
import it.fast4x.rimusic.utils.getDeviceVolume
import it.fast4x.rimusic.utils.isLandscape
import it.fast4x.rimusic.utils.isValidIP
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.playbackDeviceVolumeKey
import it.fast4x.rimusic.utils.playbackDurationKey
import it.fast4x.rimusic.utils.playbackPitchKey
import it.fast4x.rimusic.utils.playbackSpeedKey
import it.fast4x.rimusic.utils.playbackVolumeKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.resize
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.setDeviceVolume
import it.fast4x.rimusic.utils.setGlobalVolume
import it.fast4x.rimusic.utils.showCoverThumbnailAnimationKey
import it.fast4x.rimusic.utils.thumbnailFadeKey
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import it.fast4x.rimusic.utils.thumbnailSpacingKey
import kotlinx.coroutines.delay
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.isBassBoostEnabled
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongAlbumMap
import it.fast4x.rimusic.models.SongArtistMap
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.screens.settings.isYouTubeSyncEnabled
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.px
import it.fast4x.rimusic.utils.asMediaItem
import it.fast4x.rimusic.utils.asSong
import it.fast4x.rimusic.utils.bassboostLevelKey
import it.fast4x.rimusic.utils.getLikeState
import it.fast4x.rimusic.utils.isExplicit
import it.fast4x.rimusic.utils.lyricsSizeKey
import it.fast4x.rimusic.utils.lyricsSizeLKey
import it.fast4x.rimusic.utils.removeYTSongFromPlaylist
import it.fast4x.rimusic.utils.thumbnail
import it.fast4x.rimusic.utils.thumbnailFadeExKey
import it.fast4x.rimusic.utils.thumbnailSpacingLKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Composable
fun TextFieldDialog(
    hintText: String,
    onDismiss: () -> Unit,
    onDone: (String) -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = stringResource(R.string.cancel),
    doneText: String = stringResource(R.string.done),
    initialTextInput: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    onCancel: () -> Unit = onDismiss,
    isTextInputValid: (String) -> Boolean = { it.isNotEmpty() }
) {
    val focusRequester = remember {
        FocusRequester()
    }

    var textFieldValue by rememberSaveable(initialTextInput, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = initialTextInput,
                selection = TextRange(initialTextInput.length)
            )
        )
    }

    DefaultDialog(
        onDismiss = onDismiss,
        modifier = modifier

    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            textStyle = typography().xs.semiBold.center,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(imeAction = if (singleLine) ImeAction.Done else ImeAction.None),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isTextInputValid(textFieldValue.text)) {
                        onDismiss()
                        onDone(textFieldValue.text)
                    }
                }
            ),
            cursorBrush = SolidColor(colorPalette().text),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = textFieldValue.text.isEmpty(),
                        enter = fadeIn(tween(100)),
                        exit = fadeOut(tween(100)),
                    ) {
                        BasicText(
                            text = hintText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = typography().xs.semiBold.secondary,
                        )
                    }

                    innerTextField()
                }
            },
            modifier = Modifier
                .padding(all = 16.dp)
                .weight(weight = 1f, fill = false)
                .focusRequester(focusRequester)

        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            DialogTextButton(
                text = cancelText,
                onClick = onCancel
            )

            DialogTextButton(
                primary = true,
                text = doneText,
                onClick = {
                    if (isTextInputValid(textFieldValue.text)) {
                        onDismiss()
                        onDone(textFieldValue.text)
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}

@Composable
fun ConfirmationDialog(
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = stringResource(R.string.cancel),
    confirmText: String = stringResource(R.string.confirm),
    onCancel: () -> Unit = onDismiss,
    cancelBackgroundPrimary: Boolean = false,
    confirmBackgroundPrimary: Boolean = true
) {
    DefaultDialog(
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        BasicText(
            text = text,
            style = typography().xs.medium.center,
            modifier = Modifier
                .padding(all = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            DialogTextButton(
                text = cancelText,
                primary = cancelBackgroundPrimary,
                onClick = onCancel
            )

            DialogTextButton(
                text = confirmText,
                primary = confirmBackgroundPrimary,
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
inline fun DefaultDialog(
    noinline onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    crossinline content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            horizontalAlignment = horizontalAlignment,
            modifier = modifier
                .padding(all = 10.dp)
                .background(
                    color = colorPalette().background1,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            content = content
        )
    }
}

@Composable
fun <T> ValueSelectorDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    title: String,
    titleSecondary: String? = null,
    selectedValue: T,
    values: List<T>,
    onValueSelected: (T) -> Unit,
    valueText: @Composable (T) -> String = { it.toString() }
) {
    val colorPalette = colorPalette()
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .padding(all = 10.dp)
                .background(color = colorPalette.background1, shape = RoundedCornerShape(8.dp))
                .padding(vertical = 16.dp)
        ) {
            BasicText(
                text = title,
                style = typography().s.semiBold,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )
            if (titleSecondary != null) {
                BasicText(
                    text = titleSecondary,
                    style = typography().xxs.semiBold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                values.forEach { value ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    onDismiss()
                                    onValueSelected(value)
                                }
                            )
                            .padding(vertical = 12.dp, horizontal = 24.dp)
                            .fillMaxWidth()
                    ) {
                        if (selectedValue == value) {
                            Canvas(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        color = colorPalette.accent,
                                        shape = CircleShape
                                    )
                            ) {
                                drawCircle(
                                    color = colorPalette.onAccent,
                                    radius = 4.dp.toPx(),
                                    center = size.center,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        blurRadius = 4.dp.toPx(),
                                        offset = Offset(x = 0f, y = 1.dp.toPx())
                                    )
                                )
                            }
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(
                                        width = 1.dp,
                                        color = colorPalette.textDisabled,
                                        shape = CircleShape
                                    )
                            )
                        }

                        BasicText(
                            text = valueText(value),
                            style = typography().xs.medium
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 24.dp)
            ) {
                DialogTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
inline fun SelectorDialog(
    noinline onDismiss: () -> Unit,
    title: String,
    values: List<Info>?,
    crossinline onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showItemsIcon: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .padding(all = 10.dp)
                .background(color = colorPalette().background1, shape = RoundedCornerShape(8.dp))
                .padding(vertical = 16.dp)
        ) {
            BasicText(
                text = title,
                style = typography().s.semiBold,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {

                values?.distinct()?.forEach { value ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    onDismiss()
                                    onValueSelected(value.id)
                                }
                            )
                            .padding(vertical = 12.dp, horizontal = 24.dp)
                            .fillMaxWidth()
                    ) {
                        if (showItemsIcon)
                            IconButton(
                                onClick = {},
                                icon = R.drawable.playlist,
                                color = colorPalette().text,
                                modifier = Modifier
                                    .size(18.dp)
                            )

                        BasicText(
                            text = value.name ?: "Not selectable",
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            style = typography().xs.medium
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 24.dp)
            ) {
                DialogTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
inline fun SelectorArtistsDialog(
    noinline onDismiss: () -> Unit,
    title: String,
    values: List<Info>?,
    crossinline onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    //showItemsIcon: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val thumbnailRoundness by rememberPreference(thumbnailRoundnessKey, ThumbnailRoundness.Heavy)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = modifier
                .requiredSize(if (isLandscape) (0.85 * screenHeight) else (0.85 * screenWidth))
                .clip(thumbnailRoundness.shape())
                .background(color = colorPalette().background1)
        ) {
            if (values != null) {
                val pagerState = rememberPagerState(pageCount = { values.size })
                val colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.Dark)

                Box {
                    HorizontalPager(state = pagerState) { idArtist ->
                        val browseId = values[idArtist].id
                        var artist by persist<Artist?>("artist/$browseId/artist")
                        LaunchedEffect(browseId) {
                            Database.artist(values[idArtist].id).collect{artist = it}
                        }
                        LaunchedEffect(Unit) {
                            if (artist?.thumbnailUrl == null) {
                                withContext(Dispatchers.IO) {
                                    EnvironmentExt.getArtistPage(browseId = browseId)
                                        .onSuccess { currentArtistPage ->
                                            artist?.copy(
                                                thumbnailUrl = currentArtistPage.artist.thumbnail?.url
                                            )?.let(::update)
                                            Database.artist(values[idArtist].id).collect{artist = it}
                                        }
                                }
                            }
                        }

                        Box {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(artist?.thumbnailUrl?.resize(1200, 1200))
                                    .build(),
                                contentDescription = "",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .requiredSize(if (isLandscape) (0.85 * screenHeight) else (0.85 * screenWidth))
                                    .clickable(
                                        onClick = {
                                            onDismiss()
                                            onValueSelected(browseId)
                                        }
                                    )
                                    .align(Alignment.Center)
                            )
                            if (artist?.isYoutubeArtist == true) {
                                Image(
                                    painter = painterResource(R.drawable.ytmusic),
                                    colorFilter = ColorFilter.tint(
                                        Color.Red.copy(0.75f).compositeOver(Color.White)
                                    ),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(all = 5.dp)
                                        .offset(10.dp,10.dp),
                                    contentDescription = "Background Image",
                                    contentScale = ContentScale.Fit
                                )
                            }
                            values[idArtist].name?.let { it1 ->
                                BasicText(
                                    text = cleanPrefix(it1),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    style = typography().xs.medium,
                                    modifier = Modifier
                                        .padding(bottom = 20.dp)
                                        .align(Alignment.BottomCenter)
                                )
                                BasicText(
                                    text = cleanPrefix(it1),
                                    style = typography().xs.medium.merge(TextStyle(
                                        drawStyle = Stroke(width = 1.0f, join = StrokeJoin.Round),
                                        color = if (colorPaletteMode == ColorPaletteMode.Light || (colorPaletteMode == ColorPaletteMode.System && (!isSystemInDarkTheme()))) Color.White.copy(0.5f)
                                        else Color.Black
                                    )),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .padding(bottom = 20.dp)
                                        .align(Alignment.BottomCenter)
                                )
                            }
                        }

                    }
                    Row(
                        Modifier
                            .height(20.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(values.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) colorPalette().text else colorPalette().text.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(10.dp)

                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
inline fun InputNumericDialog(
    noinline onDismiss: () -> Unit,
    title: String,
    value: String,
    valueMin: String,
    valueMax: String,
    placeholder: String,
    crossinline setValue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val txtFieldError = remember { mutableStateOf("") }
    val txtField = remember { mutableStateOf(value) }
    val value_cannot_empty = stringResource(R.string.value_cannot_be_empty)
    val value_must_be_greater = stringResource(R.string.value_must_be_greater_than)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .padding(all = 10.dp)
                .background(color = colorPalette().background1, shape = RoundedCornerShape(8.dp))
                .padding(vertical = 16.dp)
                .requiredHeight(190.dp)
        ) {
            BasicText(
                text = title,
                style = typography().s.semiBold,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                TextField(
                    modifier = Modifier
                        //.padding(horizontal = 30.dp)
                        .fillMaxWidth(0.7f),
                    /*
                    .border(
                        BorderStroke(
                            width = 1.dp,
                            color = if (txtFieldError.value.isEmpty()) colorPalette.textDisabled else colorPalette.red
                        ),

                        shape = thumbnailShape
                    ),
                     */
                    colors = TextFieldDefaults.textFieldColors(
                        placeholderColor = colorPalette().textDisabled,
                        cursorColor = colorPalette().text,
                        textColor = colorPalette().text,
                        backgroundColor = if (txtFieldError.value.isEmpty()) colorPalette().background1 else colorPalette().red,
                        focusedIndicatorColor = colorPalette().accent,
                        unfocusedIndicatorColor = colorPalette().textDisabled
                    ),
                    leadingIcon = {
/*
                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.background0),
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clickable(
                                    indication = rememberRipple(bounded = false),
                                    interactionSource = remember { MutableInteractionSource() },
                                    enabled = true,
                                    onClick = { onDismiss() }
                                )
                        )

 */


                    },
                    placeholder = { Text(text = placeholder) },
                    value = txtField.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        txtField.value = it.take(10)
                    })
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                BasicText(
                    text = if (txtFieldError.value.isNotEmpty()) txtFieldError.value else "---",
                    style = typography().xs.medium,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )
            }


            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                DialogTextButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        if (txtField.value.isEmpty()) {
                            txtFieldError.value = value_cannot_empty
                            return@DialogTextButton
                        }
                        if (txtField.value.isNotEmpty() && txtField.value.toInt() < valueMin.toInt()) {
                            txtFieldError.value = value_must_be_greater + valueMin
                            return@DialogTextButton
                        }
                        setValue(txtField.value)
                    }
                )

                DialogTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier
                )
            }

        }
    }

}

@Composable
inline fun InputTextDialog(
    modifier: Modifier = Modifier,
    noinline onDismiss: () -> Unit,
    title: String,
    value: String,
    setValueRequireNotNull: Boolean = true,
    placeholder: String,
    crossinline setValue: (String) -> Unit,
    validationType: ValidationType = ValidationType.None,
    prefix: String = "",
) {
    val txtFieldError = remember { mutableStateOf("") }
    val txtField = remember { mutableStateOf(cleanPrefix(value)) }
    val value_cannot_empty = stringResource(R.string.value_cannot_be_empty)
    //val value_must_be_greater = stringResource(R.string.value_must_be_greater_than)
    val value_must_be_ip_address = stringResource(R.string.value_must_be_ip_address)
    var checkedState = remember{
        mutableStateOf(value.startsWith(prefix))
    }


    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .padding(all = 10.dp)
                .background(color = colorPalette().background1, shape = RoundedCornerShape(8.dp))
                .padding(vertical = 16.dp)
                .defaultMinSize(Dp.Unspecified, 190.dp)
        ) {
            BasicText(
                text = title,
                style = typography().s.semiBold,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                TextField(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    maxLines = 20,
                    colors = TextFieldDefaults.textFieldColors(
                        placeholderColor = colorPalette().textDisabled,
                        cursorColor = colorPalette().text,
                        textColor = colorPalette().text,
                        backgroundColor = if (txtFieldError.value.isEmpty()) colorPalette().background1 else colorPalette().red,
                        focusedIndicatorColor = colorPalette().accent,
                        unfocusedIndicatorColor = colorPalette().textDisabled
                    ),
                    leadingIcon = {
                        /*
                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.background0),
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clickable(
                                    enabled = true,
                                    onClick = { onDismiss() }
                                )
                        )
                         */

                    },
                    placeholder = { Text(text = placeholder) },
                    value = txtField.value,
                    keyboardOptions = KeyboardOptions(keyboardType = if (validationType == ValidationType.Ip) KeyboardType.Number else KeyboardType.Text),
                    onValueChange = {
                        txtField.value = it
                    })
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (prefix != "") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                        Checkbox(
                            checked = checkedState.value,
                            onCheckedChange = {
                                checkedState.value = it
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorPalette().accent,
                                uncheckedColor = colorPalette().text
                            ),
                            modifier = Modifier
                                .scale(0.7f)
                        )
                        BasicText(
                            text = stringResource(R.string.set_custom_value),
                            style = typography().xs.medium,
                            maxLines = 2,
                            modifier = Modifier
                        )

                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                DialogTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier
                )

                DialogTextButton(
                    text = stringResource(R.string.confirm),
                    onClick = {
                        if (txtField.value.isEmpty() && setValueRequireNotNull) {
                            txtFieldError.value = value_cannot_empty
                            return@DialogTextButton
                        }
                        if (txtField.value.isNotEmpty() && validationType == ValidationType.Ip) {
                            if (!isValidIP(txtField.value)) {
                                txtFieldError.value = value_must_be_ip_address
                                return@DialogTextButton
                            }
                        }
                        println("mediaItem ${checkedState.value} prefix ${prefix} value ${txtField.value}")
                        if (checkedState.value && prefix.isNotEmpty())
                            setValue(prefix + cleanPrefix(txtField.value))
                        else
                            setValue(txtField.value)

                        onDismiss()

                    },
                    primary = true
                )
            }

        }
    }

}

@Composable
inline fun StringListDialog(
    title: String,
    addTitle: String,
    addPlaceholder: String,
    removeTitle: String,
    conflictTitle: String,
    list: List<String>,
    crossinline add: (String) -> Unit,
    crossinline remove: (String) -> Unit,
    noinline onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStringAddDialog by remember {
        mutableStateOf(false)
    }
    var showStringRemoveDialog by remember {
        mutableStateOf(false)
    }
    var removingItem by remember { mutableStateOf("") }
    var errorDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .padding(all = 10.dp)
                .background(color = colorPalette().background1, shape = RoundedCornerShape(8.dp))
                .padding(vertical = 16.dp)
                .defaultMinSize(Dp.Unspecified, 190.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicText(
                    text = title,
                    style = typography().s.semiBold,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )
                DialogTextButton(
                    text = addTitle,
                    primary = true,
                    onClick = {
                        showStringAddDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                list.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp),
                    ) {
                        BasicText(
                            text = item,
                            style = typography().s.semiBold,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        Icon(
                            painter = painterResource(R.drawable.trash),
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.clickable {
                                removingItem = item
                                showStringRemoveDialog = true
                            }
                        )
                    }
                }
            }

        }

    }

    if (showStringAddDialog) {
        InputTextDialog(
            onDismiss = { showStringAddDialog = false },
            placeholder = addPlaceholder,
            setValue = {
                if (it !in list) {
                    add(it)
                } else {
                    errorDialog = true
                }
            },
            title = addTitle,
            value = ""
        )
    }

    if (showStringRemoveDialog) {
        ConfirmationDialog(
            text = removeTitle,
            onDismiss = { showStringRemoveDialog = false },
            onConfirm = {
                remove(removingItem)
            }
        )
    }

    if (errorDialog) {
        DefaultDialog(
            onDismiss = {errorDialog = false},
            modifier = modifier
        ) {
            BasicText(
                text = conflictTitle,
                style = typography().xs.medium.center,
                modifier = Modifier
                    .padding(all = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                DialogTextButton(
                    text = stringResource(R.string.confirm),
                    primary = true,
                    onClick = {
                        errorDialog = false
                    }
                )
            }
        }
    }

}

@Composable
fun NewVersionDialog (
    updatedProductName: String,
    updatedVersionName: String,
    updatedVersionCode: Int,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    DefaultDialog(
        onDismiss = { onDismiss() },
        content = {
            BasicText(
                text = stringResource(R.string.update_available),
                style = typography().s.bold.copy(color = colorPalette().text),
            )
            Spacer(modifier = Modifier.height(10.dp))
            BasicText(
                text = String.format(stringResource(R.string.app_update_dialog_new),updatedVersionName),
                style = typography().xs.semiBold.copy(color = colorPalette().text),
            )
            Spacer(modifier = Modifier.height(10.dp))
            BasicText(
                text = stringResource(R.string.actions_you_can_do),
                style = typography().xs.semiBold.copy(color = colorPalette().textSecondary),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth()
            ) {
                BasicText(
                    text = stringResource(R.string.open_the_github_releases_web_page_and_download_latest_version),
                    style = typography().xxs.semiBold.copy(color = colorPalette().textSecondary),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Image(
                    painter = painterResource(R.drawable.globe),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette().shimmer),
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            onDismiss()
                            uriHandler.openUri("https://github.com/fast4x/RiMusic/releases/latest")
                        }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth()
            ) {
                BasicText(
                    text = stringResource(R.string.download_latest_version_from_github_you_will_find_the_file_in_the_notification_area_and_you_can_install_by_clicking_on_it),
                    style = typography().xxs.semiBold.copy(color = colorPalette().textSecondary),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Image(
                    painter = painterResource(R.drawable.downloaded),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette().shimmer),
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            onDismiss()
                            uriHandler.openUri("https://github.com/fast4x/RiMusic/releases/download/$updatedVersionName/rimusic-full-release.apk")
                        }
                )
            }
//            Row(
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .padding(bottom = 20.dp)
//                    .fillMaxWidth()
//            ) {
//                BasicText(
//                    text = stringResource(R.string.f_droid_users_can_wait_for_the_update_info),
//                    style = typography().xxs.semiBold.copy(color = colorPalette().textSecondary),
//                    maxLines = 4,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
        }

    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurParamsDialog(
    onDismiss: () -> Unit,
    scaleValue: (Float) -> Unit,
    darkenFactorValue: (Float) -> Unit
) {
    val defaultStrength = 25f
    //val defaultStrength2 = 30f
    val defaultDarkenFactor = 0.2f
    var blurStrength  by rememberPreference(blurStrengthKey, defaultStrength)
    //var blurStrength2  by rememberPreference(blurStrength2Key, defaultStrength2)
    var blurDarkenFactor  by rememberPreference(blurDarkenFactorKey, defaultDarkenFactor)

    /*
    var isShowingLyrics by rememberSaveable {
        mutableStateOf(false)
    }
    var showlyricsthumbnail by rememberPreference(showlyricsthumbnailKey, false)

     */

  //if (!isShowingLyrics || (isShowingLyrics && showlyricsthumbnail))
    DefaultDialog(
        onDismiss = {
            scaleValue(blurStrength)
            darkenFactorValue(blurDarkenFactor)
            onDismiss()
        }
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    blurStrength = defaultStrength
                },
                icon = R.drawable.droplet,
                color = colorPalette().favoritesIcon,
                modifier = Modifier
                    .size(24.dp)
            )

            SliderControl(
                state = blurStrength,
                onSlide = { blurStrength = it },
                onSlideComplete = {},
                toDisplay = { "%.0f".format(it) },
                range = 0f..100f,
                steps = 99
            )

            /*
            CustomSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                value = blurStrength,
                onValueChange = {
                    blurStrength = it
                },
                valueRange = 0f..50f,
                gap = 1,
                //showIndicator = true,
                thumb = { thumbValue ->
                    CustomSliderDefaults.Thumb(
                        thumbValue = "%.0f".format(blurStrength),
                        color = Color.Transparent,
                        size = 40.dp,
                        modifier = Modifier.background(
                            brush = Brush.linearGradient(listOf(colorPalette.background1, colorPalette.favoritesIcon)),
                            shape = CircleShape
                        )
                    )
                },
                track = { sliderPositions ->
                    Box(
                        modifier = Modifier
                            .track()
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .background(Color.White)
                            .padding(1.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .progress(sliderPositions = sliderPositions)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            colorPalette.favoritesIcon,
                                            Color.Red
                                        )
                                    )
                                )
                        )
                    }
                }
            )
            */
        }
    }
}
    @androidx.annotation.OptIn(UnstableApi::class)
    @Composable
    fun ThumbnailOffsetDialog(
        onDismiss: () -> Unit,
        spacingValue: (Float) -> Unit,
        spacingValueL: (Float) -> Unit,
        fadeValue: (Float) -> Unit,
        fadeValueEx: (Float) -> Unit,
        imageCoverSizeValue: (Float) -> Unit
    ) {
        val defaultFade = 5f
        val defaultSpacing = 0f
        val defaultImageCoverSize = 50f
        var thumbnailSpacing by rememberPreference(thumbnailSpacingKey, defaultSpacing)
        var thumbnailSpacingL by rememberPreference(thumbnailSpacingLKey, defaultSpacing)
        var thumbnailFade by rememberPreference(thumbnailFadeKey, defaultFade)
        var thumbnailFadeEx by rememberPreference(thumbnailFadeExKey, defaultFade)
        var fadingedge by rememberPreference(fadingedgeKey, false)
        var imageCoverSize by rememberPreference(VinylSizeKey, defaultImageCoverSize)
        val showCoverThumbnailAnimation by rememberPreference(showCoverThumbnailAnimationKey, false)
        val expandedplayer by rememberPreference(expandedplayerKey, false)
        DefaultDialog(
            onDismiss = {
                spacingValue(thumbnailSpacing)
                spacingValueL(thumbnailSpacingL)
                fadeValue(thumbnailFade)
                fadeValueEx(thumbnailFadeEx)
                imageCoverSizeValue(imageCoverSize)
                onDismiss()
            }
        ) {
            if (showCoverThumbnailAnimation) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            imageCoverSize = defaultImageCoverSize
                        },
                        icon = R.drawable.album,
                        color = colorPalette().favoritesIcon,
                        modifier = Modifier
                            .size(24.dp)
                    )

                    SliderControl(
                        state = imageCoverSize,
                        onSlide = { imageCoverSize = it },
                        onSlideComplete = {
                            imageCoverSizeValue(imageCoverSize)
                        },
                        toDisplay = { "%.0f".format(it) },
                        steps = 10,
                        range = 50f..100f
                    )
                }
            }

            if(fadingedge && !isLandscape) {
                if (expandedplayer) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                thumbnailFadeEx = defaultFade
                            },
                            icon = R.drawable.droplet,
                            color = colorPalette().favoritesIcon,
                            modifier = Modifier
                                .size(24.dp)
                        )

                        SliderControl(
                            state = thumbnailFadeEx,
                            onSlide = { thumbnailFadeEx = it },
                            onSlideComplete = {},
                            toDisplay = { "%.0f".format(it) },
                            steps = 9,
                            range = 0f..10f
                        )

                        /*
                CustomSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    value = thumbnailOffset,
                    onValueChange = {
                        thumbnailOffset = it
                    },
                    valueRange = 0f..50f,
                    gap = 1,
                    //showIndicator = true,
                    thumb = { thumbValue ->
                        CustomSliderDefaults.Thumb(
                            thumbValue = "%.0f".format(thumbnailOffset),
                            color = Color.Transparent,
                            size = 40.dp,
                            modifier = Modifier.background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        colorPalette.background1,
                                        colorPalette.favoritesIcon
                                    )
                                ),
                                shape = CircleShape
                            )
                        )
                    },
                    track = { sliderPositions ->
                        Box(
                            modifier = Modifier
                                .track()
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .background(Color.White)
                                .padding(1.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .progress(sliderPositions = sliderPositions)
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(
                                                colorPalette.favoritesIcon,
                                                Color.Red
                                            )
                                        )
                                    )
                            )
                        }
                    }
                )
                */
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                thumbnailFade = defaultFade
                            },
                            icon = R.drawable.droplet,
                            color = colorPalette().favoritesIcon,
                            modifier = Modifier
                                .size(24.dp)
                        )

                        SliderControl(
                            state = thumbnailFade,
                            onSlide = { thumbnailFade = it },
                            onSlideComplete = {},
                            toDisplay = { "%.0f".format(it) },
                            steps = 9,
                            range = 0f..10f
                        )
                    }
                }
            }
            if (expandedplayer && !isLandscape) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            thumbnailSpacing = defaultSpacing
                        },
                        icon = R.drawable.burger,
                        color = colorPalette().favoritesIcon,
                        modifier = Modifier
                            .size(24.dp)
                    )

                    SliderControl(
                        state = thumbnailSpacing,
                        onSlide = { thumbnailSpacing = it },
                        onSlideComplete = {},
                        toDisplay = { "%.0f".format(it) },
                        range = -50f..50f
                    )

                    /*
                CustomSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    value = thumbnailSpacing,
                    onValueChange = {
                        thumbnailSpacing = it
                    },
                    valueRange = -50f..50f,
                    gap = 1,
                    //showIndicator = true,
                    thumb = { thumbValue ->
                        CustomSliderDefaults.Thumb(
                            thumbValue = "%.0f".format(thumbnailSpacing),
                            color = Color.Transparent,
                            size = 40.dp,
                            modifier = Modifier.background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        colorPalette.background1,
                                        colorPalette.favoritesIcon
                                    )
                                ),
                                shape = CircleShape
                            )
                        )
                    },
                    track = { sliderPositions ->
                        Box(
                            modifier = Modifier
                                .track()
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .background(Color.White)
                                .padding(1.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .progress(sliderPositions = sliderPositions)
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(
                                                colorPalette.favoritesIcon,
                                                Color.Red
                                            )
                                        )
                                    )
                            )
                        }
                    }
                )
                 */
                }
            }
            if (isLandscape) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            thumbnailSpacingL = defaultSpacing
                        },
                        icon = R.drawable.burger,
                        color = colorPalette().favoritesIcon,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(90f)
                    )

                    SliderControl(
                        state = thumbnailSpacingL,
                        onSlide = { thumbnailSpacingL = it },
                        onSlideComplete = {},
                        toDisplay = { "%.0f".format(it) },
                        range = -50f..50f
                    )
                }
            }
        }
    }



@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun AppearancePresetDialog(
    onDismiss: () -> Unit,
    onClick0: () -> Unit,
    onClick1: () -> Unit,
    onClick2: () -> Unit,
    onClick3: () -> Unit,
    onClick4: () -> Unit,
    onClick5: () -> Unit
) {
    val images = listOf(R.drawable.preset0,R.drawable.preset1,R.drawable.preset2,R.drawable.preset3,R.drawable.preset4,R.drawable.preset5)
    val pagerStateAppearance = rememberPagerState(pageCount = { images.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = colorPalette().background1)
        ){
            Box(
                modifier = Modifier
            ){
                HorizontalPager(
                    state = pagerStateAppearance,
                    pageSize = PageSize.Fill,
                    modifier = Modifier
                        .fillMaxSize()
                ) { index ->
                    Image(
                        painter = painterResource(images[index]),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                        colorFilter = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(bottom = 30.dp)
                        .padding(end = 15.dp)
                        .background(colorPalette().accent, CircleShape)
                        .align(Alignment.BottomEnd),
                ) {
                    IconButton(
                        icon = R.drawable.checkmark,
                        color = colorPalette().background0,
                        indication = ripple(false),
                        onClick = if (pagerStateAppearance.settledPage == 0) onClick0
                        else if (pagerStateAppearance.settledPage == 1) onClick1
                        else if (pagerStateAppearance.settledPage == 2) onClick2
                        else if (pagerStateAppearance.settledPage == 3) onClick3
                        else if (pagerStateAppearance.settledPage == 4) onClick4
                        else onClick5,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(30.dp)
                    )
                }
                Row(
                    Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(images.size) { iteration ->
                        val lineWeight = animateFloatAsState(
                            targetValue = if (pagerStateAppearance.currentPage == iteration) {1.5f} else {
                                if (iteration < pagerStateAppearance.currentPage) {0.5f} else {1f}
                            }, label = "weight", animationSpec = tween(300, easing = EaseInOut)
                        )
                        val color = if (pagerStateAppearance.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                                .weight(lineWeight.value)
                                .size(5.dp)
                        )
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LyricsSizeDialog(
    onDismiss: () -> Unit,
    sizeValue: (Float) -> Unit,
    sizeValueL: (Float) -> Unit,
) {
    var lyricsSize by rememberPreference(lyricsSizeKey, 20f)
    var lyricsSizeL by rememberPreference(lyricsSizeLKey, 20f)
    DefaultDialog(
        onDismiss = {
            sizeValue(lyricsSize)
            sizeValueL(lyricsSizeL)
            onDismiss()
        }
    ) {

        if (!isLandscape) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        lyricsSize = 20f
                    },
                    icon = R.drawable.text,
                    color = colorPalette().favoritesIcon,
                    modifier = Modifier
                        .size(24.dp)
                )

                SliderControl(
                    state = lyricsSize,
                    onSlide = { lyricsSize = it },
                    onSlideComplete = {},
                    toDisplay = { "%.0f".format(it) },
                    steps = 82,
                    range = 18f..100f
                )
            }
        }
        if (isLandscape) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        lyricsSizeL = 20f
                    },
                    icon = R.drawable.text,
                    color = colorPalette().favoritesIcon,
                    modifier = Modifier
                        .size(24.dp)
                )

                SliderControl(
                    state = lyricsSizeL,
                    onSlide = { lyricsSizeL = it },
                    onSlideComplete = {},
                    toDisplay = { "%.0f".format(it) },
                    range = 18f..100f
                )
            }
        }
    }
}

@Composable
fun InProgressDialog(
    total : Int,
    done : Int,
    text : String,
    onDismiss: (() -> Unit)? = null,
) {
    DefaultDialog(
        onDismiss = {if (onDismiss != null) {onDismiss()}},
        modifier = Modifier
            .fillMaxWidth(if (isLandscape) 0.3f else 0.8f)
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = typography().l.bold.fontSize,
                fontWeight = typography().l.bold.fontWeight,
                color = colorPalette().text
            ),
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier
            .height(10.dp)
        )
        BasicText(
            text = "$done / $total",
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontStyle = typography().xs.semiBold.fontStyle,
                color = colorPalette().text
            ),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun SongMatchingDialog(
    songToRematch : Song,
    playlistId : Long,
    position : Int,
    playlist : Playlist?,
    onDismiss: (() -> Unit)
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.5f else 0.9f)
                .fillMaxHeight(if (isLandscape) 0.9f else 0.7f)
                .background(color = colorPalette().background1,shape = RoundedCornerShape(8.dp))
        ) {
            fun filteredText(text : String): String{
                val filteredText = text
                    .lowercase()
                    .replace("(", " ")
                    .replace(")", " ")
                    .replace("-", " ")
                    .replace("lyrics", "")
                    .replace("vevo", "")
                    .replace(" hd", "")
                    .replace("official video", "")
                    .filter {it.isLetterOrDigit() || it.isWhitespace() || it == '\'' || it == ',' }
                    .replace(Regex("\\s+"), " ")
                return filteredText
            }
            var songsList by remember { mutableStateOf<List<Environment.SongItem?>>(emptyList()) }
            var searchText by remember {mutableStateOf(filteredText("${cleanPrefix(songToRematch.title)} ${songToRematch.artistsText}"))}
            var startSearch by remember { mutableStateOf(false) }

            LaunchedEffect(Unit,startSearch) {
                runBlocking(Dispatchers.IO) {
                    val searchQuery = Environment.searchPage(
                        body = SearchBody(
                            query = searchText,
                            params = Environment.SearchFilter.Song.value
                        ),
                        fromMusicShelfRendererContent = Environment.SongItem.Companion::from
                    )

                    songsList = searchQuery?.getOrNull()?.items ?: emptyList()
                    startSearch = false
                }
            }
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colorPalette().text, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 5.dp)
                    .padding(vertical = 10.dp)
            ) {
                Box {
                    AsyncImage(
                        model = songToRematch.asMediaItem.mediaMetadata.artworkUri.thumbnail(
                            Dimensions.thumbnails.song.px / 2
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .size(40.dp)
                    )
                    if (songToRematch.likedAt != null) {
                        HeaderIconButton(
                            onClick = {},
                            icon = getLikeState(songToRematch.asMediaItem.mediaId),
                            color = colorPalette().favoritesIcon,
                            iconSize = 12.dp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .absoluteOffset((-8).dp, 0.dp)
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    ) {
                        if (songToRematch.asMediaItem.isExplicit) {
                            IconButton(
                                icon = R.drawable.explicit,
                                color = colorPalette().text,
                                enabled = true,
                                onClick = {},
                                modifier = Modifier
                                    .size(18.dp)
                            )
                            Spacer(
                                modifier = Modifier
                                    .width(5.dp)
                            )
                        }
                        BasicText(
                            text = cleanPrefix(songToRematch.title),
                            style = typography().xs.semiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        BasicText(
                            text = songToRematch.artistsText ?: "",
                            style = typography().s.semiBold.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee(iterations = Int.MAX_VALUE)
                        )
                        BasicText(
                            text = songToRematch.durationText ?: "",
                            style = typography().xs.secondary.medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(end = 5.dp)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { it ->
                        searchText = it
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
                        startSearch = true
                    },
                    modifier = Modifier
                        .background(shape = RoundedCornerShape(4.dp),color = Color.White)
                        .padding(all = 4.dp)
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                        .weight(0.1f)
                )
            }
            if (songsList.isNotEmpty()) {
                LazyColumn {
                    itemsIndexed(songsList) { _, song ->
                        val artistsNames = song?.authors?.filter { it.endpoint != null }?.map { it.name }
                        val artistsIds = song?.authors?.filter { it.endpoint != null }?.map { it.endpoint?.browseId }
                        val artistNameString = song?.asMediaItem?.mediaMetadata?.artist?.toString() ?: ""
                        if (song != null) {
                            Row(horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .padding(vertical = 10.dp)
                                    .clickable(onClick = {
                                        Database.asyncTransaction {
                                            if (isYouTubeSyncEnabled() && playlist?.isYoutubePlaylist == true && playlist.isEditable){
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    if (removeYTSongFromPlaylist(songToRematch.id, playlist.browseId ?: "", playlistId))
                                                        deleteSongFromPlaylist(songToRematch.id, playlistId)
                                                }
                                            } else {
                                                deleteSongFromPlaylist(songToRematch.id, playlistId)
                                            }

                                            if (songExist(song.asSong.id) == 0) {
                                                Database.insert(song.asMediaItem)
                                            }

                                            insert(
                                                SongPlaylistMap(
                                                    songId = song.asMediaItem.mediaId,
                                                    playlistId = playlistId,
                                                    position = position
                                                ).default()
                                            )
                                            insert(
                                                Album(id = song.album?.endpoint?.browseId ?: "", title = song.asMediaItem.mediaMetadata.albumTitle?.toString()),
                                                SongAlbumMap(songId = song.asMediaItem.mediaId, albumId = song.album?.endpoint?.browseId ?: "", position = null)
                                            )
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val album = Database.album(song.album?.endpoint?.browseId ?: "").firstOrNull()
                                                album?.copy(thumbnailUrl = song.thumbnail?.url)?.let { update(it) }

                                                if (isYouTubeSyncEnabled() && playlist?.isYoutubePlaylist == true && playlist.isEditable){
                                                    EnvironmentExt.addToPlaylist(playlist.browseId ?: "", song.asMediaItem.mediaId)
                                                }
                                            }
                                            if ((artistsNames != null) && (artistsIds != null)) {
                                                artistsNames.let { artistNames ->
                                                    artistsIds.let { artistIds ->
                                                        if (artistNames.size == artistIds.size) {
                                                            insert(
                                                                artistNames.mapIndexed { index, artistName ->
                                                                    Artist(id = (artistIds[index]) ?: "", name = artistName)
                                                                },
                                                                artistIds.map { artistId ->
                                                                    SongArtistMap(
                                                                        songId = song.asMediaItem.mediaId,
                                                                        artistId = (artistId) ?: ""
                                                                    )
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Database.updateSongArtist(song.asMediaItem.mediaId, artistNameString)
                                        }
                                        onDismiss()
                                    }
                                    )
                            ) {
                                Box {
                                    AsyncImage(
                                        model = song.asMediaItem.mediaMetadata.artworkUri.thumbnail(
                                            Dimensions.thumbnails.song.px / 2
                                        ),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .padding(end = 5.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .size(30.dp)
                                    )
                                    if (song.asSong.likedAt != null) {
                                        HeaderIconButton(
                                            onClick = {},
                                            icon = getLikeState(song.asMediaItem.mediaId),
                                            color = colorPalette().favoritesIcon,
                                            iconSize = 9.dp,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .absoluteOffset((-6.75).dp, 0.dp)
                                        )
                                    }
                                }
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .basicMarquee(iterations = Int.MAX_VALUE)
                                    ) {
                                        if (song.asMediaItem.isExplicit) {
                                            IconButton(
                                                icon = R.drawable.explicit,
                                                color = colorPalette().text,
                                                enabled = true,
                                                onClick = {},
                                                modifier = Modifier
                                                    .size(18.dp)
                                            )
                                            Spacer(
                                                modifier = Modifier
                                                    .width(5.dp)
                                            )
                                        }
                                        BasicText(
                                            text = cleanPrefix(song.title ?: ""),
                                            style = typography().xs.semiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        BasicText(
                                            text = song.asSong.artistsText ?: "",
                                            style = typography().xs.semiBold.secondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier
                                                .weight(1f)
                                                .basicMarquee(iterations = Int.MAX_VALUE)
                                        )
                                        BasicText(
                                            text = song.durationText ?: "",
                                            style = typography().xxs.secondary.medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .padding(end = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

  /*if (isShowingLyrics && !showlyricsthumbnail)
      DefaultDialog(
          onDismiss = {
              scaleValue(blurStrength2)
              darkenFactorValue(blurDarkenFactor)
              onDismiss()
          }
      ) {

          Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                  .fillMaxWidth()
          ) {
              IconButton(
                  onClick = {
                      blurStrength2 = defaultStrength2
                  },
                  icon = R.drawable.droplet,
                  color = colorPalette.favoritesIcon,
                  modifier = Modifier
                      .size(24.dp)
              )

              CustomSlider(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 5.dp),
                  value = blurStrength2,
                  onValueChange = {
                      blurStrength2 = it
                  },
                  valueRange = 0f..50f,
                  gap = 1,
                  showIndicator = true,
                  thumb = { thumbValue ->
                      CustomSliderDefaults.Thumb(
                          thumbValue = "%.0f".format(blurStrength2),
                          color = Color.Transparent,
                          size = 40.dp,
                          modifier = Modifier.background(
                              brush = Brush.linearGradient(
                                  listOf(
                                      colorPalette.background1,
                                      colorPalette.favoritesIcon
                                  )
                              ),
                              shape = CircleShape
                          )
                      )
                  },
                  track = { sliderPositions ->
                      Box(
                          modifier = Modifier
                              .track()
                              .border(
                                  width = 1.dp,
                                  color = Color.LightGray.copy(alpha = 0.4f),
                                  shape = CircleShape
                              )
                              .background(Color.White)
                              .padding(1.dp),
                          contentAlignment = Alignment.CenterStart
                      ) {
                          Box(
                              modifier = Modifier
                                  .progress(sliderPositions = sliderPositions)
                                  .background(
                                      brush = Brush.linearGradient(
                                          listOf(
                                              colorPalette.favoritesIcon,
                                              Color.Red
                                          )
                                      )
                                  )
                          )
                      }
                  }
              )
          }
      }*/




        /*
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 4.dp)
        ) {
            IconButton(
                onClick = {
                    blurDarkenFactor = defaultDarkenFactor
                },
                icon = R.drawable.moon,
                color = colorPalette.favoritesIcon,
                modifier = Modifier
                    .size(20.dp)
            )

            CustomSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                value = blurDarkenFactor,
                onValueChange = {
                    blurDarkenFactor = it
                },
                valueRange = 0f..1f,
                gap = 1,
                showIndicator = true,
                thumb = { thumbValue ->
                    CustomSliderDefaults.Thumb(
                        thumbValue = "%.2f".format(blurDarkenFactor),
                        color = Color.Transparent,
                        size = 40.dp,
                        modifier = Modifier.background(
                            brush = Brush.linearGradient(listOf(colorPalette.background1, colorPalette.favoritesIcon)),
                            shape = CircleShape
                        )
                    )
                },
                track = { sliderPositions ->
                    Box(
                        modifier = Modifier
                            .track()
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .background(Color.White)
                            .padding(1.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .progress(sliderPositions = sliderPositions)
                                .background(
                                    brush = Brush.linearGradient(listOf(colorPalette.favoritesIcon, Color.Red))
                                )
                        )
                    }
                }
            )
        }
         */

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PlaybackParamsDialog(
    onDismiss: () -> Unit,
    speedValue: (Float) -> Unit,
    pitchValue: (Float) -> Unit,
    durationValue: (Float) -> Unit,
    scaleValue: (Float) -> Unit,
) {
    val binder = LocalPlayerServiceBinder.current
    val context = LocalContext.current
    val defaultSpeed = 1f
    val defaultPitch = 1f
    //val defaultVolume = 0.5f //binder?.player?.volume ?: 1f
    //val defaultDeviceVolume = getDeviceVolume(context)
    val defaultDuration = 0f
    val defaultStrength = 25f
    val defaultBassboost = 0.5f
    var playbackSpeed  by rememberPreference(playbackSpeedKey,   defaultSpeed)
    var playbackPitch  by rememberPreference(playbackPitchKey,   defaultPitch)
    var playbackVolume  by rememberPreference(playbackVolumeKey, 0.5f)
    var playbackDeviceVolume  by rememberPreference(playbackDeviceVolumeKey, getDeviceVolume(context))
    var playbackDuration by rememberPreference(playbackDurationKey, defaultDuration)
    var blurStrength  by rememberPreference(blurStrengthKey, defaultStrength)
    var bassBoost  by rememberPreference(bassboostLevelKey, defaultBassboost)

    DefaultDialog(
        onDismiss = {
            speedValue(playbackSpeed)
            pitchValue(playbackPitch)
            durationValue(playbackDuration)
            scaleValue(blurStrength)
            onDismiss()
        }
    ) {
        TitleSection(stringResource(R.string.controls_header_customize))

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleMiniSection(stringResource(R.string.controls_title_blur_effect))
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    blurStrength = defaultStrength
                },
                icon = R.drawable.droplet,
                color = colorPalette().favoritesIcon,
                modifier = Modifier
                    .size(20.dp)
            )
            SliderControl(
                state = blurStrength,
                onSlide = { blurStrength = it },
                onSlideComplete = {},
                toDisplay = { "%.0f".format(it) },
                range = 0f..50f
            )
            /*
            CustomSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                value = blurStrength,
                onValueChange = {
                    blurStrength = it
                },
                valueRange = 0f..50f,
                gap = 1,
                //showIndicator = true,
                thumb = { thumbValue ->
                    CustomSliderDefaults.Thumb(
                        thumbValue = "%.0f".format(blurStrength),
                        color = Color.Transparent,
                        size = 40.dp,
                        modifier = Modifier.background(
                            brush = Brush.linearGradient(listOf(colorPalette.background1, colorPalette.favoritesIcon)),
                            shape = CircleShape
                        )
                    )
                },
                track = { sliderPositions ->
                    Box(
                        modifier = Modifier
                            .track()
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .background(Color.White)
                            .padding(1.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .progress(sliderPositions = sliderPositions)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            colorPalette.favoritesIcon,
                                            Color.Red
                                        )
                                    )
                                )
                        )
                    }
                }
            )
            */
        }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleMiniSection(stringResource(R.string.controls_title_medley_duration))
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    playbackDuration = defaultDuration
                },
                icon = R.drawable.playbackduration,
                color = colorPalette().favoritesIcon,
                modifier = Modifier
                    .size(20.dp)
            )

            SliderControl(
                state = playbackDuration,
                onSlide = { playbackDuration = it },
                onSlideComplete = {},
                toDisplay = { "%.0f".format(playbackDuration) },
                range = 0f..60f
            )

            /*
            CustomSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    //.padding(top = 13.dp)
                    .padding(horizontal = 5.dp),
                value = playbackDuration,
                onValueChange = {
                    playbackDuration = it
                },
                valueRange = 1f..60f,
                gap = 1,
                //showIndicator = true,
                thumb = { thumbValue ->
                    CustomSliderDefaults.Thumb(
                        thumbValue = "%.0f".format(playbackDuration),
                        color = Color.Transparent,
                        size = 40.dp,
                        modifier = Modifier.background(
                            brush = Brush.linearGradient(
                                listOf(
                                    colorPalette.background1,
                                    colorPalette.favoritesIcon
                                )
                            ),
                            shape = CircleShape
                        )
                    )
                },
                track = { sliderPositions ->
                    Box(
                        modifier = Modifier
                            .track()
                            .border(
                                width = 1.dp,
                                color = Color.LightGray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .background(Color.White)
                            .padding(1.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .progress(sliderPositions = sliderPositions)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            colorPalette.favoritesIcon,
                                            Color.Red
                                        )
                                    )
                                )
                        )
                    }
                }
            )
            */
        }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleMiniSection(stringResource(R.string.controls_title_playback_speed))
        }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        playbackSpeed = defaultSpeed
                        binder?.player?.playbackParameters =
                            PlaybackParameters(playbackSpeed, playbackPitch)
                    },
                    icon = R.drawable.slow_motion,
                    color = colorPalette().favoritesIcon,
                    modifier = Modifier
                        .size(20.dp)
                )

                SliderControl(
                    state = playbackSpeed,
                    onSlide = {
                        playbackSpeed = it
                        binder?.player?.playbackParameters =
                            PlaybackParameters(playbackSpeed, playbackPitch)
                    },
                    onSlideComplete = {},
                    toDisplay = { "%.1fx".format(playbackSpeed) },
                    range = 0.1f..5f
                )

                /*
                CustomSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.padding(top = 13.dp)
                        .padding(horizontal = 5.dp),
                    value = playbackSpeed,
                    onValueChange = {
                        playbackSpeed = it
                        binder?.player?.playbackParameters =
                            PlaybackParameters(playbackSpeed, playbackPitch)
                    },
                    valueRange = 0.1f..5f,
                    gap = 1,
                    //showIndicator = true,
                    thumb = { thumbValue ->
                        CustomSliderDefaults.Thumb(
                            thumbValue = "%.1fx".format(playbackSpeed),
                            color = Color.Transparent,
                            size = 40.dp,
                            modifier = Modifier.background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        colorPalette.background1,
                                        colorPalette.favoritesIcon
                                    )
                                ),
                                shape = CircleShape
                            )
                        )
                    },
                    track = { sliderPositions ->
                        Box(
                            modifier = Modifier
                                .track()
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .background(Color.White)
                                .padding(1.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .progress(sliderPositions = sliderPositions)
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(
                                                colorPalette.favoritesIcon,
                                                Color.Red
                                            )
                                        )
                                    )
                            )
                        }
                    }
                )
                 */
            }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleMiniSection(stringResource(R.string.controls_title_playback_pitch))
        }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        playbackPitch = defaultPitch
                        binder?.player?.playbackParameters =
                            PlaybackParameters(playbackSpeed, playbackPitch)
                    },
                    icon = R.drawable.equalizer,
                    color = colorPalette().favoritesIcon,
                    modifier = Modifier
                        .size(20.dp)
                )

                SliderControl(
                    state = playbackPitch,
                    onSlide = {
                        playbackPitch = it
                        binder?.player?.playbackParameters =
                            PlaybackParameters(playbackSpeed, playbackPitch)
                    },
                    onSlideComplete = {},
                    toDisplay = { "%.1fx".format(playbackPitch) },
                    range = 0.1f..5f
                )

                /*
                CustomSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.padding(top = 13.dp)
                        .padding(horizontal = 5.dp),
                    value = playbackPitch,
                    onValueChange = {
                        playbackPitch = it
                        binder?.player?.playbackParameters =
                            PlaybackParameters(playbackSpeed, playbackPitch)
                    },
                    valueRange = 0.1f..5f,
                    gap = 1,
                    //showIndicator = true,
                    thumb = { thumbValue ->
                        CustomSliderDefaults.Thumb(
                            thumbValue = "%.1fx".format(playbackPitch),
                            color = Color.Transparent,
                            size = 40.dp,
                            modifier = Modifier.background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        colorPalette.background1,
                                        colorPalette.favoritesIcon
                                    )
                                ),
                                shape = CircleShape
                            )
                        )
                    },
                    track = { sliderPositions ->
                        Box(
                            modifier = Modifier
                                .track()
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .background(Color.White)
                                .padding(1.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .progress(sliderPositions = sliderPositions)
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(
                                                colorPalette.favoritesIcon,
                                                Color.Red
                                            )
                                        )
                                    )
                            )
                        }
                    }
                )
                 */
            }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleMiniSection(stringResource(R.string.controls_title_playback_volume))
        }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        playbackVolume = 0.5f
                        binder?.player?.volume = playbackVolume
                        binder?.player?.setGlobalVolume(playbackVolume)
                    },
                    icon = R.drawable.volume_up,
                    color = colorPalette().favoritesIcon,
                    modifier = Modifier
                        .size(20.dp)
                )

                SliderControl(
                    state = playbackVolume,
                    onSlide = {
                        playbackVolume = it
                        binder?.player?.volume = playbackVolume
                        binder?.player?.setGlobalVolume(playbackVolume)
                    },
                    onSlideComplete = {},
                    toDisplay = { "%.1f".format(playbackVolume) },
                    range = 0.0f..1.0f
                )

                /*
                CustomSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.padding(top = 13.dp)
                        .padding(horizontal = 5.dp),
                    value = playbackVolume,
                    onValueChange = {
                        playbackVolume = it
                        binder?.player?.volume = playbackVolume
                    },
                    valueRange = 0.0f..1.0f,
                    gap = 1,
                    //showIndicator = true,
                    thumb = { thumbValue ->
                        CustomSliderDefaults.Thumb(
                            thumbValue = "%.1f".format(playbackVolume),
                            color = Color.Transparent,
                            size = 40.dp,
                            modifier = Modifier.background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        colorPalette.background1,
                                        colorPalette.favoritesIcon
                                    )
                                ),
                                shape = CircleShape
                            )
                        )
                    },
                    track = { sliderPositions ->
                        Box(
                            modifier = Modifier
                                .track()
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .background(Color.White)
                                .padding(1.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .progress(sliderPositions = sliderPositions)
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(
                                                colorPalette.favoritesIcon,
                                                Color.Red
                                            )
                                        )
                                    )
                            )
                        }
                    }
                )
                 */
            }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleMiniSection(stringResource(R.string.controls_title_device_volume))
        }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        playbackDeviceVolume = getDeviceVolume(context)
                        setDeviceVolume(context, playbackDeviceVolume)
                    },
                    icon = R.drawable.master_volume,
                    color = colorPalette().favoritesIcon,
                    modifier = Modifier
                        .size(20.dp)
                )

                SliderControl(
                    state = playbackDeviceVolume,
                    onSlide = {
                        playbackDeviceVolume = it
                        setDeviceVolume(context, playbackDeviceVolume)
                    },
                    onSlideComplete = {},
                    toDisplay = { "%.1f".format(playbackDeviceVolume) },
                    range = 0.0f..1.0f
                )

            }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TitleMiniSection(stringResource(R.string.settings_bass_boost_level))
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    bassBoost = defaultBassboost
                },
                icon = R.drawable.musical_notes,
                color = colorPalette().favoritesIcon,
                modifier = Modifier
                    .size(20.dp)
            )

            SliderControl(
                isEnabled = isBassBoostEnabled(),
                state = bassBoost,
                onSlide = {
                    bassBoost = it
                },
                onSlideComplete = {},
                toDisplay = { "%.1f".format(bassBoost) },
                range = 0.0f..1.0f
            )

        }

    }
}

@Composable
fun <T> ValueSelectorDialogBody(
    onDismiss: () -> Unit,
    title: String,
    selectedValue: T?,
    values: List<T>,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    valueText: @Composable (T) -> String = { it.toString() }
) = Column(modifier = modifier) {
    val colorPalette = colorPalette()

    BasicText(
        text = title,
        style = typography().s.semiBold,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)
    )

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        values.forEach { value ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .clickable(
                        onClick = {
                            onDismiss()
                            onValueSelected(value)
                        }
                    )
                    .padding(vertical = 12.dp, horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                if (selectedValue == value) Canvas(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            color = colorPalette.accent,
                            shape = CircleShape
                        )
                ) {
                    drawCircle(
                        color = colorPalette.onAccent,
                        radius = 4.dp.toPx(),
                        center = size.center,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            blurRadius = 4.dp.toPx(),
                            offset = Offset(x = 0f, y = 1.dp.toPx())
                        )
                    )
                } else Spacer(
                    modifier = Modifier
                        .size(18.dp)
                        .border(
                            width = 1.dp,
                            color = colorPalette.textDisabled,
                            shape = CircleShape
                        )
                )

                BasicText(
                    text = valueText(value),
                    style = typography().xs.medium
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .align(Alignment.End)
            .padding(end = 24.dp)
    ) {
        DialogTextButton(
            text = stringResource(R.string.cancel),
            onClick = onDismiss
        )
    }
}