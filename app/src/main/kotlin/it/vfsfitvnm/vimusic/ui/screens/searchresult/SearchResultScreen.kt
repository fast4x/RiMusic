package it.vfsfitvnm.vimusic.ui.screens.searchresult

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.themed.Scaffold
import it.vfsfitvnm.vimusic.ui.screens.PlaylistScreen
import it.vfsfitvnm.vimusic.ui.screens.albumRoute
import it.vfsfitvnm.vimusic.ui.screens.artistRoute
import it.vfsfitvnm.vimusic.ui.screens.globalRoutes
import it.vfsfitvnm.vimusic.ui.screens.playlistRoute
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.ui.views.AlbumItem
import it.vfsfitvnm.vimusic.ui.views.AlbumItemShimmer
import it.vfsfitvnm.vimusic.ui.views.ArtistItem
import it.vfsfitvnm.vimusic.ui.views.ArtistItemShimmer
import it.vfsfitvnm.vimusic.ui.views.PlaylistItem
import it.vfsfitvnm.vimusic.ui.views.PlaylistItemShimmer
import it.vfsfitvnm.vimusic.ui.views.SmallSongItem
import it.vfsfitvnm.vimusic.ui.views.SmallSongItemShimmer
import it.vfsfitvnm.vimusic.ui.views.VideoItem
import it.vfsfitvnm.vimusic.ui.views.VideoItemShimmer
import it.vfsfitvnm.vimusic.utils.asMediaItem
import it.vfsfitvnm.vimusic.utils.forcePlay
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.searchResultScreenTabIndexKey
import it.vfsfitvnm.youtubemusic.YouTube

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun SearchResultScreen(query: String, onSearchAgain: () -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val (tabIndex, onTabIndexChanges) = rememberPreference(searchResultScreenTabIndexKey, 0)

    RouteHandler(listenToGlobalEmitter = true) {
        globalRoutes()

        playlistRoute { browseId ->
            PlaylistScreen(
                browseId = browseId ?: "browseId cannot be null"
            )
        }

        host {
            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabIndexChanges,
                tabColumnContent = { Item ->
                    Item(0, "Songs", R.drawable.musical_notes)
                    Item(1, "Albums", R.drawable.disc)
                    Item(2, "Artists", R.drawable.person)
                    Item(3, "Videos", R.drawable.film)
                    Item(4, "Playlists", R.drawable.playlist)
                    Item(5, "Featured", R.drawable.playlist)
                }
            ) { tabIndex ->
                val searchFilter = when (tabIndex) {
                    0 -> YouTube.Item.Song.Filter
                    1 -> YouTube.Item.Album.Filter
                    2 -> YouTube.Item.Artist.Filter
                    3 -> YouTube.Item.Video.Filter
                    4 -> YouTube.Item.CommunityPlaylist.Filter
                    5 -> YouTube.Item.FeaturedPlaylist.Filter
                    else -> error("unreachable")
                }.value

                saveableStateHolder.SaveableStateProvider(tabIndex) {
                    when (tabIndex) {
                        0 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val thumbnailSizeDp = Dimensions.thumbnails.song
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemSearchResult<YouTube.Item.Song>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain,
                                itemContent = { song ->
                                    SmallSongItem(
                                        song = song,
                                        thumbnailSizePx = thumbnailSizePx,
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(song.asMediaItem)
                                            binder?.setupRadio(song.info.endpoint)
                                        }
                                    )
                                },
                                itemShimmer = {
                                    SmallSongItemShimmer(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        1 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemSearchResult<YouTube.Item.Album>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain,
                                itemContent = { album ->
                                    AlbumItem(
                                        album = album,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { albumRoute(album.info.endpoint?.browseId) }
                                            )
                                    )

                                },
                                itemShimmer = {
                                    AlbumItemShimmer(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }

                        2 -> {
                            val thumbnailSizeDp = 64.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemSearchResult<YouTube.Item.Artist>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain,
                                itemContent = { artist ->
                                    ArtistItem(
                                        artist = artist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { artistRoute(artist.info.endpoint?.browseId) }
                                            )
                                    )
                                },
                                itemShimmer = {
                                    ArtistItemShimmer(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                        3 -> {
                            val binder = LocalPlayerServiceBinder.current
                            val thumbnailHeightDp = 72.dp
                            val thumbnailWidthDp = 128.dp

                            ItemSearchResult<YouTube.Item.Video>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain,
                                itemContent = { video ->
                                    VideoItem(
                                        video = video,
                                        thumbnailWidthDp = thumbnailWidthDp,
                                        thumbnailHeightDp = thumbnailHeightDp,
                                        onClick = {
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(video.asMediaItem)
                                            binder?.setupRadio(video.info.endpoint)
                                        }
                                    )
                                },
                                itemShimmer = {
                                    VideoItemShimmer(
                                        thumbnailHeightDp = thumbnailHeightDp,
                                        thumbnailWidthDp = thumbnailWidthDp
                                    )
                                }
                            )
                        }

                        4, 5 -> {
                            val thumbnailSizeDp = 108.dp
                            val thumbnailSizePx = thumbnailSizeDp.px

                            ItemSearchResult<YouTube.Item.Playlist>(
                                query = query,
                                filter = searchFilter,
                                onSearchAgain = onSearchAgain,
                                itemContent = { playlist ->
                                    PlaylistItem(
                                        playlist = playlist,
                                        thumbnailSizePx = thumbnailSizePx,
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .clickable(
                                                indication = rememberRipple(bounded = true),
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { playlistRoute(playlist.info.endpoint?.browseId) }
                                            )
                                    )
                                },
                                itemShimmer = {
                                    PlaylistItemShimmer(thumbnailSizeDp = thumbnailSizeDp)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
