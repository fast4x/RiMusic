package it.fast4x.rimusic.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.models.VideoOrSongInfo
import it.fast4x.rimusic.R
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.Loader
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.ui.components.themed.TitleMiniSection

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun ShowVideoOrSongInfo(
    videoId: String,
) {

    if (videoId.isBlank()) return

    val thumbnailRoundness by rememberPreference(thumbnailRoundnessKey, ThumbnailRoundness.Heavy)

    val windowInsets = WindowInsets.systemBars

    var info by remember {
        mutableStateOf<VideoOrSongInfo?>(null)
    }

    LaunchedEffect(Unit, videoId) {
        info = EnvironmentExt.getVideOrSongInfo(videoId).getOrNull()
        //println("ShowVideoOrSongInfo: ${info?.description}")
    }

    //if (info == null) return


        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .padding(
                    windowInsets
                        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                )
                .fillMaxSize()
        ) {
            item(contentType = "InfoTitlePage") {
                Title(
                    title = stringResource(R.string.information),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp, bottom = 8.dp),
                    icon = R.drawable.chevron_down,
                    onClick = {},
                    enableClick = true
                )
            }
            if (info != null) {
                item(contentType = "InfoTitle") {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    ) {
                        TitleMiniSection(
                            title = stringResource(R.string.title)
                        )
                        BasicText(
                            text = "" + info?.title,
                            style = typography().xs.color(colorPalette().text)
                                .align(TextAlign.Start),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                }
                item(contentType = "InfoAuthor") {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    ) {
                        TitleMiniSection(
                            title = stringResource(R.string.artists)
                        )
                        BasicText(
                            text = "" + info?.author,
                            style = typography().xs.color(colorPalette().text)
                                .align(TextAlign.Start),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                }
                item(contentType = "InfoDescription") {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                    ) {
                        TitleMiniSection(
                            title = stringResource(R.string.description)
                        )
                        BasicText(
                            text = info?.description ?: "",
                            style = typography().xs.color(colorPalette().text)
                                .align(TextAlign.Start),
                            modifier = Modifier
                                .padding(all = 16.dp)
                        )
                    }
                }
                item(contentType = "InfoNumbers") {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                    ) {
                        TitleMiniSection(
                            title = stringResource(R.string.numbers)
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Column {
                                BasicText(
                                    text = stringResource(R.string.subscribers),
                                    style = typography().xs.color(colorPalette().text)
                                        .align(TextAlign.Start),
                                    modifier = Modifier
                                )
                                BasicText(
                                    text = info?.subscribers ?: "",
                                    style = typography().xs.color(colorPalette().text)
                                        .align(TextAlign.Start),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Column {
                                BasicText(
                                    text = stringResource(R.string.views),
                                    style = typography().xs.color(colorPalette().text)
                                        .align(TextAlign.Start),
                                    modifier = Modifier
                                )
                                BasicText(
                                    text = "" + info?.viewCount?.toInt()
                                        ?.let { numberFormatter(it) },
                                    style = typography().xs.color(colorPalette().text)
                                        .align(TextAlign.Start),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Column {
                                BasicText(
                                    text = stringResource(R.string.likes),
                                    style = typography().xs.color(colorPalette().text)
                                        .align(TextAlign.Start),
                                    modifier = Modifier
                                )
                                BasicText(
                                    text = "" + info?.like?.toInt()?.let { numberFormatter(it) },
                                    style = typography().xs.color(colorPalette().text)
                                        .align(TextAlign.Start),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Column {
                                BasicText(
                                    text = stringResource(R.string.dislikes),
                                    style = typography().xs.color(colorPalette().text)
                                        .align(TextAlign.Start),
                                    modifier = Modifier
                                )
                                BasicText(
                                    text = "" + info?.dislike?.toInt()?.let { numberFormatter(it) },
                                    style = typography().xs.color(colorPalette().text)
                                        .align(TextAlign.Start),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                        }

                    }
                }

            } else {
                item(contentType = "InfoLoader") {
                    Loader()
                }
            }
        }


}