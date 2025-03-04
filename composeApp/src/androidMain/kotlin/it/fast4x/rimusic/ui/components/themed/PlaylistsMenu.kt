package it.fast4x.rimusic.ui.components.themed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.navigation.NavController
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.MenuStyle
import it.fast4x.rimusic.enums.NavRoutes
import it.fast4x.rimusic.enums.PlaylistSortBy
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.MenuState
import it.fast4x.rimusic.utils.menuStyleKey
import it.fast4x.rimusic.utils.playlistSortByKey
import it.fast4x.rimusic.utils.playlistSortOrderKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.ui.components.tab.toolbar.Button
import it.fast4x.rimusic.ui.components.tab.toolbar.Descriptive
import it.fast4x.rimusic.ui.components.tab.toolbar.Menu
import it.fast4x.rimusic.ui.components.tab.toolbar.MenuIcon
import it.fast4x.rimusic.typography

class PlaylistsMenu private constructor(
    private val navController: NavController,
    private val mediaItems: (PlaylistPreview) -> List<MediaItem>,
    private val onFailure: (Throwable, PlaylistPreview) -> Unit,
    private val finalAction: (PlaylistPreview) -> Unit,
    override val menuState: MenuState,
    override val styleState: MutableState<MenuStyle>
): MenuIcon, Descriptive, Menu {

    companion object {
        @JvmStatic
        @Composable
        fun init(
            navController: NavController,
            mediaItems: (PlaylistPreview) -> List<MediaItem>,
            onFailure: (Throwable, PlaylistPreview) -> Unit,
            finalAction: (PlaylistPreview) -> Unit
        ) = PlaylistsMenu(
            navController,
            mediaItems,
            onFailure,
            finalAction,
            LocalMenuState.current,
            rememberPreference( menuStyleKey, MenuStyle.List )
        )
    }

    override val iconId: Int = R.drawable.add_in_playlist
    override val messageId: Int = R.string.add_to_playlist
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    private fun onAdd( preview: PlaylistPreview ) {
        val startPos = preview.songCount

        Database.asyncTransaction {
            /*
                Suspend this block until all songs are
                inserted into database before going to
                SmartMessage
            */
            runBlocking( Dispatchers.IO ) {
                try {
                    mediaItems( preview ).forEachIndexed { index, mediaItem ->
                        insert(mediaItem)
                        insert(
                            SongPlaylistMap(
                                songId = mediaItem.mediaId,
                                playlistId = preview.playlist.id,
                                position = startPos + index
                            ).default()
                        )
                    }
                } catch ( e: Throwable ) {
                    onFailure( e, preview )
                } finally {
                    finalAction( preview )
                }
            }
            runBlocking( Dispatchers.Main ) {
                SmartMessage(
                    appContext().resources.getString(R.string.done),
                    type = PopupType.Success, context = appContext()
                )
            }
        }
    }

    @Composable
    private fun PlaylistCard( playlistPreview: PlaylistPreview ) {
        val playlist = playlistPreview.playlist
        val songsCount = playlistPreview.songCount

        MenuEntry(
            icon = R.drawable.add_in_playlist,
            text = playlist.name.substringAfter( PINNED_PREFIX ),
            secondaryText = "$songsCount ${stringResource( R.string.songs )}",
            onClick = {
                onAdd( playlistPreview )
                menuState.hide()
            },
            trailingContent = {
                IconButton(
                    icon = R.drawable.open,
                    color = colorPalette().text,
                    onClick = {
                        menuState.hide()
                        navController.navigate(route = "${NavRoutes.localPlaylist.name}/${playlistPreview.playlist.id}")
                    },
                    modifier = Modifier.size( 24.dp )
                )
            }
        )
    }

    override fun onShortClick() {
        menuState.hide()
        super.onShortClick()
    }

    @Composable
    override fun ListMenu() { /* Does nothing */ }

    @Composable
    override fun GridMenu() { /* Does nothing */ }

    @Composable
    override fun MenuComponent() {
        val sortBy by rememberPreference( playlistSortByKey, PlaylistSortBy.DateAdded )
        val sortOrder by rememberPreference( playlistSortOrderKey, SortOrder.Descending )

        val playlistPreviews by remember {
            Database.playlistPreviews( sortBy, sortOrder )
        }.collectAsState( emptyList(), Dispatchers.IO )

        val pinnedPlaylists = playlistPreviews.filter {
            it.playlist.name.startsWith(PINNED_PREFIX, 0, true)
        }
        val unpinnedPlaylists = playlistPreviews.filter {
            !it.playlist.name.startsWith(PINNED_PREFIX, 0, true) &&
                    !it.playlist.name.startsWith(MONTHLY_PREFIX, 0, true)
        }

        val newPlaylistButton = object : IDialog, Button {

            override val dialogTitle: String
                @Composable
                get() = stringResource( R.string.enter_the_playlist_name )
            override var isActive: Boolean by rememberSaveable { mutableStateOf( false ) }
            // TODO: Add a random name generator
            override var value: String = ""

            override fun onSet( newValue: String ) {
                Database.asyncTransaction {
                    val playlistId = insert( Playlist(name = newValue) )
                    onAdd(
                        PlaylistPreview(
                            Playlist(playlistId, newValue),
                            0
                        )
                    )
                }
                onDismiss()
                menuState.hide()
            }

            @Composable
            override fun ToolBarButton() {
                SecondaryTextButton(
                    text = stringResource( R.string.new_playlist ),
                    onClick = ::onShortClick,
                    alternative = true
                )
            }
        }
        newPlaylistButton.Render()

        Menu {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = ::onShortClick,
                    icon = R.drawable.chevron_back,
                    color = colorPalette().textSecondary,
                    modifier = Modifier.padding(all = 4.dp)
                        .size(20.dp)
                )

                newPlaylistButton.ToolBarButton()
            }
            if (pinnedPlaylists.isNotEmpty()) {
                BasicText(
                    text = stringResource(R.string.pinned_playlists),
                    style = typography().m.semiBold,
                    modifier = Modifier.padding(start = 20.dp, top = 5.dp)
                )

                pinnedPlaylists.forEach { PlaylistCard(it) }
            }

            if (unpinnedPlaylists.isNotEmpty()) {
                BasicText(
                    text = stringResource(R.string.playlists),
                    style = typography().m.semiBold,
                    modifier = Modifier.padding(start = 20.dp, top = 5.dp)
                )

                unpinnedPlaylists.forEach { PlaylistCard(it) }
            }
        }
    }
}