package it.fast4x.rimusic.ui.screens.artist

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.navigation.NavController
import com.valentinilk.shimmer.shimmer
import it.fast4x.compose.persist.persist
import it.fast4x.environment.EnvironmentExt
import it.fast4x.environment.requests.ArtistPage
import it.fast4x.environment.requests.ArtistSection
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerServiceBinder
import it.fast4x.rimusic.R
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.ThumbnailRoundness
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.ui.components.themed.Header
import it.fast4x.rimusic.ui.components.themed.HeaderIconButton
import it.fast4x.rimusic.ui.components.themed.HeaderPlaceholder
import it.fast4x.rimusic.ui.components.themed.SecondaryTextButton
import it.fast4x.rimusic.ui.components.themed.adaptiveThumbnailContent
import it.fast4x.rimusic.utils.disableScrollingTextKey
import it.fast4x.rimusic.utils.parentalControlEnabledKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.thumbnailRoundnessKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.ui.components.Skeleton
import kotlinx.coroutines.flow.firstOrNull

@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun ArtistScreen(
    navController: NavController,
    browseId: String,
    miniPlayer: @Composable () -> Unit = {},
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    //var tabIndex by rememberPreference(artistScreenTabIndexKey, defaultValue = 0)

    val binder = LocalPlayerServiceBinder.current

    var tabIndex by rememberSaveable {
        mutableStateOf(0)
    }

    //PersistMapCleanup(tagPrefix = "artist/$browseId/")

    var artist by persist<Artist?>("artist/$browseId/artist")

    var artistPage by persist<ArtistPage?>("artist/$browseId/artistPage")

    var downloadState by remember {
        mutableStateOf(Download.STATE_STOPPED)
    }
    val context = LocalContext.current

    val thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Heavy
    )
    var changeShape by remember {
        mutableStateOf(false)
    }
    val hapticFeedback = LocalHapticFeedback.current
    val parentalControlEnabled by rememberPreference(parentalControlEnabledKey, false)

    val disableScrollingText by rememberPreference(disableScrollingTextKey, false)

    var artistInDatabase by remember { mutableStateOf<Artist?>(null) }

    Database.asyncTransaction {
        CoroutineScope(Dispatchers.IO).launch {
            artistInDatabase = artist(browseId).firstOrNull()
        }
    }

    LaunchedEffect(Unit) {

        //artistPage = YtMusic.getArtistPage(browseId)

        Database
            .artist(browseId)
            .combine(snapshotFlow { tabIndex }.map { it != 4 }) { artist, mustFetch -> artist to mustFetch }
            .distinctUntilChanged()
            .collect { (currentArtist, mustFetch) ->
                artist = currentArtist

                if (artistPage == null && (currentArtist?.timestamp == null || mustFetch)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        EnvironmentExt.getArtistPage(browseId = browseId)
                            .onSuccess { currentArtistPage ->
                                artistPage = currentArtistPage

                                Database.upsert(
                                    Artist(
                                        id = browseId,
                                        name = currentArtistPage.artist.info?.name,
                                        thumbnailUrl = currentArtistPage.artist.thumbnail?.url,
                                        timestamp = System.currentTimeMillis(),
                                        bookmarkedAt = currentArtist?.bookmarkedAt,
                                        isYoutubeArtist = artistInDatabase?.isYoutubeArtist == true
                                    )
                                )
                            }
                    }
                }
            }
    }

    val listMediaItems = remember { mutableListOf<MediaItem>() }

    var artistItemsSection by remember { mutableStateOf<ArtistSection?>(null) }

            val thumbnailContent =
                adaptiveThumbnailContent(
                    artist?.timestamp == null,
                    artist?.thumbnailUrl,
                    //CircleShape
                    onClick = { changeShape = !changeShape },
                    shape = if (changeShape) CircleShape else thumbnailRoundness.shape(),
                )

            val headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit =
                { textButton ->
                    if (artist?.timestamp == null) {
                        HeaderPlaceholder(
                            modifier = Modifier
                                .shimmer()
                        )
                    } else {
                        Header(title = cleanPrefix(artist?.name ?: "Unknown"), actionsContent = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(top = 50.dp)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    textButton?.invoke()

                                    Spacer(
                                        modifier = Modifier
                                            .weight(0.2f)
                                    )

                                    SecondaryTextButton(
                                        text = if (artist?.bookmarkedAt == null) stringResource(R.string.follow) else stringResource(
                                            R.string.following
                                        ),
                                        onClick = {
                                            val bookmarkedAt =
                                                if (artist?.bookmarkedAt == null) System.currentTimeMillis() else null

                                            Database.asyncTransaction {
                                                artist?.copy( bookmarkedAt = bookmarkedAt )
                                                      ?.let( ::update )
                                            }
                                        },
                                        alternative = artist?.bookmarkedAt == null
                                    )

                                    HeaderIconButton(
                                        icon = R.drawable.share_social,
                                        color = colorPalette().text,
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "https://music.youtube.com/channel/$browseId"
                                                )
                                            }

                                            context.startActivity(
                                                Intent.createChooser(
                                                    sendIntent,
                                                    null
                                                )
                                            )
                                        }
                                    )
                                }
                            },
                            disableScrollingText = disableScrollingText)
                    }
                }

    Skeleton(
        navController,
        tabIndex,
        onTabChanged = { tabIndex = it },
        miniPlayer,
        navBarContent = { Item ->
            Item(0, stringResource(R.string.overview), R.drawable.artist)
            Item(1, stringResource(R.string.library), R.drawable.library)
        }
    ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> {
                            ArtistOverview(
                                navController = navController,
                                browseId = browseId,
                                artistPage = artistPage,
                                onItemsPageClick = {
                                    artistItemsSection = it
                                    tabIndex = 2

                                },
                                disableScrollingText = disableScrollingText
                            )
                        }


                        1 -> {
                            ArtistLocalSongs(
                                navController = navController,
                                browseId = browseId,
                                headerContent = headerContent,
                                thumbnailContent = thumbnailContent,
                                onSearchClick = {
                                    //searchRoute("")
                                    navController.navigate(NavRoutes.search.name)
                                },
                                onSettingsClick = {
                                    //settingsRoute()
                                    navController.navigate(NavRoutes.settings.name)
                                }
                            )
                        }
                    }
                }
            }

}
