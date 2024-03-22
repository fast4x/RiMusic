package it.vfsfitvnm.vimusic.ui.components.themed

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.LocalPlayerServiceBinder
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.enums.PlaylistSortBy
import it.vfsfitvnm.vimusic.enums.SortOrder
import it.vfsfitvnm.vimusic.models.Album
import it.vfsfitvnm.vimusic.models.Info
import it.vfsfitvnm.vimusic.models.Playlist
import it.vfsfitvnm.vimusic.models.PlaylistPreview
import it.vfsfitvnm.vimusic.models.Song
import it.vfsfitvnm.vimusic.models.SongPlaylistMap
import it.vfsfitvnm.vimusic.query
import it.vfsfitvnm.vimusic.service.isLocal
import it.vfsfitvnm.vimusic.transaction
import it.vfsfitvnm.vimusic.ui.items.AlbumItem
import it.vfsfitvnm.vimusic.ui.items.SongItem
import it.vfsfitvnm.vimusic.ui.screens.home.PINNED_PREFIX
import it.vfsfitvnm.vimusic.ui.styling.Dimensions
import it.vfsfitvnm.vimusic.ui.styling.LocalAppearance
import it.vfsfitvnm.vimusic.ui.styling.px
import it.vfsfitvnm.vimusic.utils.downloadedStateMedia
import it.vfsfitvnm.vimusic.utils.formatAsDuration
import it.vfsfitvnm.vimusic.utils.getDownloadState
import it.vfsfitvnm.vimusic.utils.manageDownload
import it.vfsfitvnm.vimusic.utils.medium
import it.vfsfitvnm.vimusic.utils.playlistSortByKey
import it.vfsfitvnm.vimusic.utils.playlistSortOrderKey
import it.vfsfitvnm.vimusic.utils.positionAndDurationState
import it.vfsfitvnm.vimusic.utils.rememberPreference
import it.vfsfitvnm.vimusic.utils.semiBold
import it.vfsfitvnm.vimusic.utils.thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@ExperimentalTextApi
@SuppressLint("SuspiciousIndentation")
@UnstableApi
@ExperimentalAnimationApi
@Composable
fun AlbumsItemMenu(
    onDismiss: () -> Unit,
    onSelect: (() -> Unit)? = null,
    onUncheck: (() -> Unit)? = null,
    onChangeAlbumTitle: (() -> Unit)? = null,
    onChangeAlbumAuthors: (() -> Unit)? = null,
    onChangeAlbumCover: (() -> Unit)? = null,
    album: Album,
    modifier: Modifier = Modifier,
    onEnqueue: (() -> Unit)? = null,
    onAddToPlaylist: ((PlaylistPreview) -> Unit)? = null,

) {
    val (colorPalette, typography) = LocalAppearance.current
    val density = LocalDensity.current

    var isViewingPlaylists by remember {
        mutableStateOf(false)
    }

    var height by remember {
        mutableStateOf(0.dp)
    }

    AnimatedContent(
        targetState = isViewingPlaylists,
        transitionSpec = {
            val animationSpec = tween<IntOffset>(400)
            val slideDirection = if (targetState) AnimatedContentTransitionScope.SlideDirection.Left
            else AnimatedContentTransitionScope.SlideDirection.Right

            slideIntoContainer(slideDirection, animationSpec) togetherWith
                    slideOutOfContainer(slideDirection, animationSpec)
        }, label = ""
    ) { currentIsViewingPlaylists ->
        if (currentIsViewingPlaylists) {
            val sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
            val sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)
            val playlistPreviews by remember {
                Database.playlistPreviews(sortBy, sortOrder)
            }.collectAsState(initial = emptyList(), context = Dispatchers.IO)

            val pinnedPlaylists = playlistPreviews.filter {
                it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
            }

            val unpinnedPlaylists = playlistPreviews.filter {
                !it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
            }

            var isCreatingNewPlaylist by rememberSaveable {
                mutableStateOf(false)
            }

            if (isCreatingNewPlaylist && onAddToPlaylist != null) {
                InputTextDialog(
                    onDismiss = { isCreatingNewPlaylist = false },
                    title = stringResource(R.string.enter_the_playlist_name),
                    value = "",
                    placeholder = stringResource(R.string.enter_the_playlist_name),
                    setValue = { text ->
                        onDismiss()
                        transaction {
                            val playlistId = Database.insert( Playlist(name = text) )
                            onAddToPlaylist(PlaylistPreview(Playlist(id = playlistId, name = text),0))
                        }
                    }
                )

            }

            BackHandler {
                isViewingPlaylists = false
            }

            Menu(
                modifier = modifier
                    .requiredHeight(height)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { isViewingPlaylists = false },
                        icon = R.drawable.chevron_back,
                        color = colorPalette.textSecondary,
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .size(20.dp)
                    )

                    if (onAddToPlaylist != null) {
                        SecondaryTextButton(
                            text = stringResource(R.string.new_playlist),
                            onClick = { isCreatingNewPlaylist = true },
                            alternative = true
                        )
                    }
                }

                if (pinnedPlaylists.isNotEmpty()) {
                    BasicText(
                        text = stringResource(R.string.pinned_playlists),
                        style = typography.m.semiBold,
                        modifier = modifier.padding(start = 20.dp, top = 5.dp)
                    )

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        pinnedPlaylists.forEach { playlistPreview ->
                            MenuEntry(
                                icon = R.drawable.add_in_playlist,
                                text = playlistPreview.playlist.name.substringAfter(PINNED_PREFIX),
                                secondaryText = "${playlistPreview.songCount} " + stringResource(R.string.songs),
                                onClick = {
                                    onDismiss()
                                    onAddToPlaylist(PlaylistPreview(playlistPreview.playlist, playlistPreview.songCount))
                                }
                            )
                        }
                    }
                }

                if (unpinnedPlaylists.isNotEmpty()) {
                    BasicText(
                        text = stringResource(R.string.playlists),
                        style = typography.m.semiBold,
                        modifier = modifier.padding(start = 20.dp, top = 5.dp)
                    )

                    onAddToPlaylist?.let { onAddToPlaylist ->
                        unpinnedPlaylists.forEach { playlistPreview ->
                            MenuEntry(
                                icon = R.drawable.add_in_playlist,
                                text = playlistPreview.playlist.name.substringAfter(PINNED_PREFIX),
                                secondaryText = "${playlistPreview.songCount} " + stringResource(R.string.songs),
                                onClick = {
                                    onDismiss()
                                    onAddToPlaylist(PlaylistPreview(playlistPreview.playlist, playlistPreview.songCount))
                                }
                            )
                        }
                    }
                }
            }
        } else {
            Menu(
                modifier = modifier
                    .onPlaced { height = with(density) { it.size.height.toDp() } }
            ) {
                val thumbnailSizeDp = Dimensions.thumbnails.song + 20.dp
                val thumbnailSizePx = thumbnailSizeDp.px
                val thumbnailArtistSizeDp = Dimensions.thumbnails.song + 10.dp
                val thumbnailArtistSizePx = thumbnailArtistSizeDp.px

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(end = 12.dp)
                ) {
                    AlbumItem(album = album, thumbnailSizePx = thumbnailSizePx, thumbnailSizeDp = thumbnailSizeDp)

                    /*
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            //icon = if (likedAt == null) R.drawable.heart_outline else R.drawable.heart,
                            icon = R.drawable.heart,
                            //color = colorPalette.favoritesIcon,
                            color = if (likedAt == null) colorPalette.textDisabled else colorPalette.text,
                            onClick = {
                                query {
                                    if (Database.like(
                                            mediaItem.mediaId,
                                            if (likedAt == null) System.currentTimeMillis() else null
                                        ) == 0
                                    ) {
                                        Database.insert(mediaItem, Song::toggleLike)
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(all = 4.dp)
                                .size(24.dp)
                        )

                        if (!isLocal) IconButton(
                            icon = R.drawable.share_social,
                            color = colorPalette.text,
                            onClick = onShare,
                            modifier = Modifier
                                .padding(all = 4.dp)
                                .size(24.dp)
                        )

                    }
                    */
                }

                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )

                onSelect?.let { onSelect ->
                    MenuEntry(
                        icon = R.drawable.checked,
                        text = stringResource(R.string.item_select),
                        onClick = {
                            onDismiss()
                            onSelect()
                        }
                    )
                }
                onUncheck?.let { onUncheck ->
                    MenuEntry(
                        icon = R.drawable.unchecked,
                        text = stringResource(R.string.item_uncheck),
                        onClick = {
                            onDismiss()
                            onUncheck()
                        }
                    )
                }

                onChangeAlbumTitle?.let{
                    MenuEntry(
                        icon = R.drawable.title_edit,
                        text = stringResource(R.string.update_title),
                        onClick = {
                            onDismiss()
                            onChangeAlbumTitle()
                        }
                    )
                }
                onChangeAlbumAuthors?.let{
                    MenuEntry(
                        icon = R.drawable.artists_edit,
                        text = stringResource(R.string.update_authors),
                        onClick = {
                            onDismiss()
                            onChangeAlbumAuthors()
                        }
                    )
                }
                onChangeAlbumCover?.let{
                    MenuEntry(
                        icon = R.drawable.cover_edit,
                        text = stringResource(R.string.update_cover),
                        onClick = {
                            onDismiss()
                            onChangeAlbumCover()
                        }
                    )
                }

                onEnqueue?.let { onEnqueue ->
                    MenuEntry(
                        icon = R.drawable.enqueue,
                        text = stringResource(R.string.enqueue),
                        onClick = {
                            onDismiss()
                            onEnqueue()
                        }
                    )
                }


                if (onAddToPlaylist != null) {
                    MenuEntry(
                        icon = R.drawable.add_in_playlist,
                        text = stringResource(R.string.add_to_playlist),
                        onClick = { isViewingPlaylists = true },
                        trailingContent = {
                            Image(
                                painter = painterResource(R.drawable.chevron_forward),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(
                                    colorPalette.textSecondary
                                ),
                                modifier = Modifier
                                    .size(16.dp)
                            )
                        }
                    )
                }

            }
        }
    }
}