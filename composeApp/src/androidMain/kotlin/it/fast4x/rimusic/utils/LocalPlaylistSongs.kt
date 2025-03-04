package it.fast4x.rimusic.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.PINNED_PREFIX
import it.fast4x.rimusic.PIPED_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.MenuStyle
import it.fast4x.rimusic.enums.PlaylistSongSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.models.PipedSession
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.ui.components.LocalMenuState
import it.fast4x.rimusic.ui.components.MenuState
import it.fast4x.rimusic.ui.components.themed.MenuEntry
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.cleanPrefix
import it.fast4x.rimusic.ui.components.themed.DeleteDialog
import it.fast4x.rimusic.ui.components.themed.IDialog
import it.fast4x.rimusic.ui.components.tab.Sort
import it.fast4x.rimusic.ui.components.tab.toolbar.ConfirmDialog
import it.fast4x.rimusic.ui.components.tab.toolbar.Descriptive
import it.fast4x.rimusic.ui.components.tab.toolbar.DualIcon
import it.fast4x.rimusic.ui.components.tab.toolbar.DynamicColor
import it.fast4x.rimusic.ui.components.tab.toolbar.MenuIcon
import it.fast4x.rimusic.typography
import java.util.UUID


@Composable
fun pin(
    playlistPreview: PlaylistPreview?,
    playlistId: Long
): MenuIcon = object: MenuIcon, DynamicColor, Descriptive {

    override val iconId: Int = R.drawable.pin
    override val messageId: Int = R.string.info_pin_unpin_playlist
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    override var isFirstColor: Boolean by rememberSaveable( playlistPreview ) { mutableStateOf( isPinned() ) }

    fun isPinned(): Boolean {
        return playlistPreview?.playlist
                              ?.name
                              ?.startsWith( PINNED_PREFIX, true ) == true
    }

    override fun onShortClick() {
        Database.asyncTransaction {
            val playlistName = playlistPreview?.playlist?.name ?: return@asyncTransaction
            if( playlistName.startsWith( PINNED_PREFIX ) )
                unPinPlaylist( playlistId )
            else
                pinPlaylist( playlistId )

            isFirstColor = isPinned()
        }
    }
}

class PositionLock private constructor(
    private val iconState: MutableState<Boolean>,
    private val colorState: MutableState<Boolean>
): MenuIcon, DualIcon, DynamicColor, Descriptive {

    companion object {
        @JvmStatic
        @Composable
        fun init( sortOrder: SortOrder ) = PositionLock(
            rememberPreference( reorderInQueueEnabledKey, true ),
            rememberSaveable( sortOrder ) { mutableStateOf( sortOrder == SortOrder.Ascending ) }
        )
    }

    override val secondIconId: Int = R.drawable.unlocked
    override val iconId: Int = R.drawable.locked
    override val messageId: Int = R.string.info_lock_unlock_reorder_songs
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    override var isFirstIcon: Boolean = iconState.value
        set(value) {
            iconState.value = value
            field = value
        }
    override var isFirstColor: Boolean = colorState.value
        set(value) {
            colorState.value = value
            field = value
        }

    fun isLocked(): Boolean = isFirstIcon

    override fun onShortClick() {
        if( !isFirstColor )
            SmartMessage(
                message = appContext().resources.getString( R.string.info_reorder_is_possible_only_in_ascending_sort ),
                context = appContext()
            )
        else
            isFirstIcon = !isFirstIcon
    }
}

class PlaylistSongsSort private constructor(
    sortOrderState: MutableState<SortOrder>,
    sortByState: MutableState<PlaylistSongSortBy>,
    menuState: MenuState,
    styleState: MutableState<MenuStyle>
): Sort<PlaylistSongSortBy>( sortOrderState, PlaylistSongSortBy.entries, sortByState, menuState, styleState) {

    companion object {
        @JvmStatic
        @Composable
        fun init() = PlaylistSongsSort(
            rememberPreference( songSortOrderKey, SortOrder.Descending ),
            rememberPreference ( playlistSongSortByKey, PlaylistSongSortBy.Title ),
            LocalMenuState.current,
            rememberPreference( menuStyleKey, MenuStyle.List )
        )
    }

    @Composable
    private fun sortTitle( sortBy: PlaylistSongSortBy ): String =
        when( sortBy ) {
            PlaylistSongSortBy.ArtistAndAlbum ->
                "${stringResource(R.string.sort_artist)}, ${stringResource(R.string.sort_album)}"

            else -> stringResource( sortBy.titleId )
        }

    @Composable
    override fun MenuComponent() {
        super.Menu( sortByEntries ) {
            val icon = it.icon

            MenuEntry(
                painter = icon,
                text = sortTitle( it ),
                onClick = {
                    // Don't pass menuState::hide, it won't work
                    menuState.hide()
                    sortByState.value = it
                }
            )
        }
    }

    @Composable
    override fun ToolBarButton() {
        super.ToolBarButton()

        BasicText(
            text = sortTitle( this.sortBy ),
            style = typography().xs.semiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { super.onLongClick() }
        )
    }

    override fun onLongClick() { /* Does nothing */ }
}

@SuppressLint("ComposableNaming")
@Composable
fun DeletePlaylist(
    activeState: MutableState<Boolean> = rememberSaveable { mutableStateOf( false ) },
    menuState: MenuState = LocalMenuState.current,
    onEvent: DeleteDialog.() -> Unit
): DeleteDialog = object : DeleteDialog( activeState, menuState ) {

    override val dialogTitle: String
        @Composable
        get() = stringResource( R.string.delete_playlist )

    override fun onConfirm() = onEvent()
}

@SuppressLint("ComposableNaming")
@Composable
fun Reposition(
    playlistId: () -> Long?,
    songs: () -> List<Song>
): MenuIcon = object: ConfirmDialog, MenuIcon, Descriptive {

    val menuState: MenuState = LocalMenuState.current
    override val messageId: Int = R.string.renumber_songs_positions
    override val iconId: Int = R.drawable.position
    override val dialogTitle: String
        @Composable
        get() = stringResource( R.string.do_you_really_want_to_renumbering_positions_in_this_playlist )
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    override var isActive: Boolean by rememberSaveable { mutableStateOf( false ) }

    override fun onShortClick() = super.onShortClick()

    override fun onConfirm() {
        val pId = playlistId() ?: return

        Database.asyncTransaction {
            runBlocking {
                songs().shuffled()
            }.forEachIndexed { index, song ->
                Database.updateSongPosition( pId, song.id, index )
            }
        }

        onDismiss()
        menuState.hide()
    }
}

class RenameDialog private constructor(
    private val menuState: MenuState,
    private val playlistNameState: MutableState<String>,
    private val activeState: MutableState<Boolean>,
    private val pipedSession: PipedSession,
    private val coroutineScope: CoroutineScope,
    private val isPipedEnabled: () -> Boolean,
    private val playlistPreview: () -> PlaylistPreview?
): IDialog, Descriptive, MenuIcon {

    companion object {
        @JvmStatic
        @Composable
        fun init(
            pipedSession: PipedSession,
            coroutineScope: CoroutineScope,
            isPipedEnabled: () -> Boolean,
            playlistNameState: MutableState<String>,
            playlistPreview: () -> PlaylistPreview?
        ) = RenameDialog(
            LocalMenuState.current,
            playlistNameState,
            rememberSaveable { mutableStateOf( false ) },
            pipedSession,
            coroutineScope,
            isPipedEnabled,
            playlistPreview
        )
    }

    override val messageId: Int = R.string.rename
    override val iconId: Int = R.drawable.title_edit
    override val dialogTitle: String
        @Composable
        get() = stringResource( R.string.enter_the_playlist_name )
    override val menuIconTitle: String
        @Composable
        get() = stringResource( messageId )

    var playlistName: String = playlistNameState.value
        set(value) {
            playlistNameState.value = value
            field = value
        }
    override var isActive: Boolean = activeState.value
        set(value) {
            activeState.value = value
            field = value
        }
    override var value: String = playlistNameState.value
        set(value) {
            playlistName = value
            field = value
        }

    override fun onShortClick() = super.onShortClick()

    override fun onSet( newValue: String ) {
        val playlist = playlistPreview()?.playlist ?: return

        val isPipedPlaylist =
            playlist.name.startsWith(PIPED_PREFIX)
                    && isPipedEnabled()
                    && pipedSession.token.isNotEmpty()
        val prefix = if( isPipedPlaylist ) PIPED_PREFIX else ""

        Database.asyncTransaction {
            playlist.copy( name = "$prefix$newValue" )
                    .let( ::update )
        }

        if ( isPipedPlaylist )
            renamePipedPlaylist(
                context = appContext(),
                coroutineScope = coroutineScope,
                pipedSession = pipedSession.toApiSession(),
                id = UUID.fromString( cleanPrefix(playlist.browseId ?: "") ),
                name = "$PIPED_PREFIX$newValue"
            )

        onDismiss()
        menuState.hide()
    }
}