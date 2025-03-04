package it.fast4x.rimusic.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.MONTHLY_PREFIX
import it.fast4x.rimusic.R
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.PlaylistWithSongs
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongPlaylistMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


@Composable
fun monthlyPLaylist(playlist: String?): State<PlaylistWithSongs?> {
    val monthlyPlaylist = remember {
        Database.playlistWithSongs("${MONTHLY_PREFIX}${playlist}")
    }.collectAsState(initial = null, context = Dispatchers.IO)

    return monthlyPlaylist
}

@Composable
fun monthlyPLaylists(playlist: String? = ""): State<List<PlaylistWithSongs?>?> {
    val monthlyPlaylists = remember {
        Database.monthlyPlaylists(playlist)
    }.collectAsState(initial = null, context = Dispatchers.IO)

    return monthlyPlaylists
}

@Composable
fun CheckMonthlyPlaylist() {
    val ym = getCalculatedMonths(1)
    val y = ym?.substring(0,4)?.toLong() ?: 0
    val m = ym?.substring(5,7)?.toLong() ?: 0
    var canCreateMonthlyPlaylist by remember { mutableStateOf(false) }

    var monthlyPlaylist by remember { mutableStateOf<PlaylistWithSongs?>(null) }
    LaunchedEffect(ym) {
        monthlyPlaylist = withContext(Dispatchers.IO) {
            Database.playlistWithSongsNoFlow("${MONTHLY_PREFIX}${ym}")
        }
        canCreateMonthlyPlaylist = monthlyPlaylist == null
    }

    Timber.d("CheckMonthlyPlaylist $canCreateMonthlyPlaylist")

        if (canCreateMonthlyPlaylist) {
                var songsMostPlayed by remember { mutableStateOf<List<Song>?>(null) }
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    songsMostPlayed = Database.songsMostPlayedByYearMonthNoFlow(y, m)
                }
            }

            Timber.d("SongsMostPlayed ${songsMostPlayed?.size}")

            songsMostPlayed.let {songs ->
                        if (songs?.isNotEmpty() == true) {
                            Database.asyncTransaction {
                                val playlistId = insert(Playlist(name = "${MONTHLY_PREFIX}${ym}"))
                                playlistId.let {
                                    songs.forEachIndexed{ position, song ->
                                        insert(
                                            SongPlaylistMap(
                                                songId = song.id,
                                                playlistId = it,
                                                position = position
                                            ).default()
                                        )
                                    }
                                }

                            }

                        }
                    }




        }
    //println("mediaItem internal $monthlyPlaylist")
}

/*
@Composable
fun CheckMonthlyPlaylist() {
    val ym = getCalculatedMonths(1)
    val y = ym?.substring(0,4)?.toLong() ?: 0
    val m = ym?.substring(5,7)?.toLong() ?: 0
    val monthlyPlaylist by remember { mutableStateOf(
        transaction {
            Database.playlistWithSongsNoFlow("${MONTHLY_PREFIX}${ym}")
        }
    ) }

    if (monthlyPlaylist == null) {
        val songsMostPlayed = remember {
            Database.songsMostPlayedByYearMonth(y, m)
        }.collectAsState(initial = null, context = Dispatchers.IO)

        if (songsMostPlayed.value?.isNotEmpty() == true) {
            transaction {
                val playlistId = Database.insert(Playlist(name = "${MONTHLY_PREFIX}${ym}"))
                playlistId.let {
                    songsMostPlayed.value!!.forEachIndexed{ position, song ->
                        Database.insert(
                            SongPlaylistMap(
                                songId = song.id,
                                playlistId = it,
                                position = position
                            )
                        )
                    }
                }

            }

        }


    }
    //println("mediaItem internal $monthlyPlaylist")
}
*/

@Composable
fun CreateMonthlyPlaylist() {
    val ym = getCalculatedMonths(1)
    val y = ym?.substring(0,4)?.toLong() ?: 0
    val m = ym?.substring(5,7)?.toLong() ?: 0
    //var monthlyPlaylist by remember { mutableStateOf<PlaylistWithSongs?>(null) }


    val monthlyPlaylist = remember {
        Database.playlistWithSongs("${MONTHLY_PREFIX}${ym}")
    }.collectAsState(initial = null, context = Dispatchers.IO)
        .also {
            if (it.value == null) {
                val songsMostPlayed = remember {
                    Database.songsMostPlayedByYearMonth(y, m)
                }.collectAsState(initial = null, context = Dispatchers.IO)

                if (songsMostPlayed.value?.isNotEmpty() == true) {
                    Database.asyncTransaction {
                        val playlistId = insert(Playlist(name = "${MONTHLY_PREFIX}${ym}"))
                        playlistId.let {
                            songsMostPlayed.value!!.forEachIndexed{ position, song ->
                                insert(
                                    SongPlaylistMap(
                                        songId = song.id,
                                        playlistId = it,
                                        position = position
                                    ).default()
                                )
                            }
                        }

                    }

                }

            }
        }
    //println("mediaItem internal $monthlyPlaylist")
}

@Composable
fun getTitleMonthlyPlaylist(playlist: String): String {

    val y = playlist.substring(0,4)
    val m = playlist.substring(5,7).toInt()
    return when (m) {
        1 -> stringResource(R.string.month_january_s).format(y)
        2 -> stringResource(R.string.month_february_s).format(y)
        3 -> stringResource(R.string.month_march_s).format(y)
        4 -> stringResource(R.string.month_april_s).format(y)
        5 -> stringResource(R.string.month_may_s).format(y)
        6 -> stringResource(R.string.month_june_s).format(y)
        7 -> stringResource(R.string.month_july_s).format(y)
        8 -> stringResource(R.string.month_august_s).format(y)
        9 -> stringResource(R.string.month_september_s).format(y)
        10 -> stringResource(R.string.month_october_s).format(y)
        11 -> stringResource(R.string.month_november_s).format(y)
        12 -> stringResource(R.string.month_december_s).format(y)
        else -> playlist
    }
}

fun getTitleMonthlyPlaylist( context: Context, playlist: String ): String {
    
    fun getMonthString( @DrawableRes monthId: Int, year: String ) =
        context.resources.getString( monthId ).format( year )

    val y = playlist.substring(0,4)
    val m = playlist.substring(5,7).toInt()
    return when( m ) {
        1 -> getMonthString( R.string.month_january_s, y )
        2 -> getMonthString( R.string.month_february_s, y )
        3 -> getMonthString( R.string.month_march_s, y )
        4 -> getMonthString( R.string.month_april_s, y )
        5 -> getMonthString( R.string.month_may_s, y )
        6 -> getMonthString( R.string.month_june_s, y )
        7 -> getMonthString( R.string.month_july_s, y )
        8 -> getMonthString( R.string.month_august_s, y )
        9 -> getMonthString( R.string.month_september_s, y )
        10 -> getMonthString( R.string.month_october_s, y )
        11 -> getMonthString( R.string.month_november_s, y )
        12 -> getMonthString( R.string.month_december_s, y )
        else -> playlist
    }
}


fun getTitleMonthlyPlaylistFromContext(playlist: String, context: Context): String {

    val y = playlist.substring(0,4)
    val m = playlist.substring(5,7).toInt()
    return when (m) {
        1 -> context.resources.getString(R.string.month_january_s).format(y)
        2 -> context.resources.getString(R.string.month_february_s).format(y)
        3 -> context.resources.getString(R.string.month_march_s).format(y)
        4 -> context.resources.getString(R.string.month_april_s).format(y)
        5 -> context.resources.getString(R.string.month_may_s).format(y)
        6 -> context.resources.getString(R.string.month_june_s).format(y)
        7 -> context.resources.getString(R.string.month_july_s).format(y)
        8 -> context.resources.getString(R.string.month_august_s).format(y)
        9 -> context.resources.getString(R.string.month_september_s).format(y)
        10 -> context.resources.getString(R.string.month_october_s).format(y)
        11 -> context.resources.getString(R.string.month_november_s).format(y)
        12 -> context.resources.getString(R.string.month_december_s).format(y)
        else -> playlist
    }
}