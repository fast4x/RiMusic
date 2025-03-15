package it.fast4x.rimusic

import android.content.ContentValues
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.os.Parcel
import androidx.core.database.getFloatOrNull
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import it.fast4x.environment.Environment
import it.fast4x.rimusic.enums.AlbumSortBy
import it.fast4x.rimusic.enums.ArtistSortBy
import it.fast4x.rimusic.enums.BuiltInPlaylist
import it.fast4x.rimusic.enums.PlaylistSongSortBy
import it.fast4x.rimusic.enums.PlaylistSortBy
import it.fast4x.rimusic.enums.SongSortBy
import it.fast4x.rimusic.enums.SortOrder
import it.fast4x.rimusic.models.Album
import it.fast4x.rimusic.models.Artist
import it.fast4x.rimusic.models.Event
import it.fast4x.rimusic.models.EventWithSong
import it.fast4x.rimusic.models.Format
import it.fast4x.rimusic.models.Info
import it.fast4x.rimusic.models.Lyrics
import it.fast4x.rimusic.models.Playlist
import it.fast4x.rimusic.models.PlaylistPreview
import it.fast4x.rimusic.models.PlaylistWithSongs
import it.fast4x.rimusic.models.QueuedMediaItem
import it.fast4x.rimusic.models.SearchQuery
import it.fast4x.rimusic.models.Song
import it.fast4x.rimusic.models.SongAlbumMap
import it.fast4x.rimusic.models.SongArtistMap
import it.fast4x.rimusic.models.SongEntity
import it.fast4x.rimusic.models.SongPlaylistMap
import it.fast4x.rimusic.models.SongWithContentLength
import it.fast4x.rimusic.models.SortedSongPlaylistMap
import it.fast4x.rimusic.service.LOCAL_KEY_PREFIX
import it.fast4x.rimusic.utils.isExplicit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.intellij.lang.annotations.MagicConstant
import kotlin.collections.sortedBy

@Dao
interface Database {
    companion object : Database by DatabaseInitializer.Instance.database

    private val _internal: RoomDatabase
        get() = DatabaseInitializer.Instance

    val getInstance: RoomDatabase
        get() = _internal

    //**********************************************
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song")
    fun listAllSongsAsFlow(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs")
    fun songsOfflineByPlayTimeAsc(): Flow<List<SongEntity>>

    fun songsOfflineByRelativePlayTimeAsc(): Flow<List<SongEntity>>{
        val songs = songsOfflineByPlayTimeAsc()
        songs.map { it }
        return songs.map {
            it.sortedBy { se ->
                se.relativePlayTime()
            }
        }
    }

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs DESC")
    fun songsOfflineByPlayTimeDesc(): Flow<List<SongEntity>>

    fun songsOfflineByRelativePlayTimeDesc(): Flow<List<SongEntity>>{
        val songs = songsOfflineByPlayTimeDesc()
        songs.map { it }
        return songs.map {
            it.sortedBy { se ->
                se.relativePlayTime()
            }
        }
    }

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.title")
    fun songsOfflineByTitleAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.title DESC")
    fun songsOfflineByTitleDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.ROWID")
    fun songsOfflineByRowIdAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.ROWID DESC")
    fun songsOfflineByRowIdDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.likedAt")
    fun songsOfflineByLikedAtAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.likedAt DESC")
    fun songsOfflineByLikedAtDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.artistsText")
    fun songsOfflineByArtistAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.artistsText DESC")
    fun songsOfflineByArtistDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.durationText")
    fun songsOfflineByDurationAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song INNER JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.durationText DESC")
    fun songsOfflineByDurationDesc(): Flow<List<SongEntity>>

    fun songsOffline(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<SongEntity>> {
        return when (sortBy) {
            SongSortBy.PlayTime, SongSortBy.DatePlayed -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByPlayTimeAsc()
                SortOrder.Descending -> songsOfflineByPlayTimeDesc()
            }
            SongSortBy.Title, SongSortBy.AlbumName -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByTitleAsc()
                SortOrder.Descending -> songsOfflineByTitleDesc()
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByRowIdAsc()
                SortOrder.Descending -> songsOfflineByRowIdDesc()
            }
            SongSortBy.DateLiked -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByLikedAtAsc()
                SortOrder.Descending -> songsOfflineByLikedAtDesc()
            }
            SongSortBy.Artist -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByArtistAsc()
                SortOrder.Descending -> songsOfflineByArtistDesc()
            }
            SongSortBy.Duration -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByDurationAsc()
                SortOrder.Descending -> songsOfflineByDurationDesc()
            }

            SongSortBy.RelativePlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsOfflineByRelativePlayTimeAsc()
                SortOrder.Descending -> songsOfflineByRelativePlayTimeDesc()
            }
        }
    }

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY artistsText")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByArtistAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY artistsText DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByArtistDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY totalPlayTimeMs")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByPlayTimeAsc(): Flow<List<SongEntity>>

    fun songsFavoritesByRelativePlayTimeAsc(): Flow<List<SongEntity>> {
        val songs = songsFavoritesByPlayTimeAsc()
        songs.map { it }
        return songs.map {
            it.sortedBy { se ->
                se.relativePlayTime()
            }
        }
    }

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY totalPlayTimeMs DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByPlayTimeDesc(): Flow<List<SongEntity>>

    fun songsFavoritesByRelativePlayTimeDesc(): Flow<List<SongEntity>> {
        val songs = songsFavoritesByPlayTimeDesc()
        songs.map { it }
        return songs.map {
            it.sortedByDescending { se ->
                se.relativePlayTime()
            }
        }
    }

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY title COLLATE NOCASE ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByTitleAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY title COLLATE NOCASE DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByTitleDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY ROWID")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByRowIdAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByRowIdDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY likedAt")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByLikedAtAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY likedAt DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByLikedAtDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query(
        "SELECT DISTINCT S.* FROM Song S LEFT JOIN Event E ON E.songId=S.id " +
                "WHERE likedAt IS NOT NULL AND likedAt > 0 " +
                "ORDER BY E.timestamp DESC"
    )
    fun songsFavoritesByDatePlayedDesc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query(
        "SELECT DISTINCT S.* FROM Song S LEFT JOIN Event E ON E.songId=S.id " +
                "WHERE likedAt IS NOT NULL AND likedAt > 0 " +
                "ORDER BY E.timestamp"
    )
    fun songsFavoritesByDatePlayedAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY durationText")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByDurationAsc(): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL AND likedAt > 0 ORDER BY durationText DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsFavoritesByDurationDesc(): Flow<List<SongEntity>>

    fun songsFavorites(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<SongEntity>> {
        return when (sortBy) {
            SongSortBy.PlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByPlayTimeAsc()
                SortOrder.Descending -> songsFavoritesByPlayTimeDesc()
            }
            SongSortBy.RelativePlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByRelativePlayTimeAsc()
                SortOrder.Descending -> songsFavoritesByRelativePlayTimeDesc()
            }
            SongSortBy.Title, SongSortBy.AlbumName -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByTitleAsc()
                SortOrder.Descending -> songsFavoritesByTitleDesc()
            }
            SongSortBy.DateLiked -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByLikedAtAsc()
                SortOrder.Descending -> songsFavoritesByLikedAtDesc()
            }
            SongSortBy.DatePlayed -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByDatePlayedAsc()
                SortOrder.Descending -> songsFavoritesByDatePlayedDesc()
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByRowIdAsc()
                SortOrder.Descending -> songsFavoritesByRowIdDesc()
            }
            SongSortBy.Artist -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByArtistAsc()
                SortOrder.Descending -> songsFavoritesByArtistDesc()
            }
            SongSortBy.Duration -> when (sortOrder) {
                SortOrder.Ascending -> songsFavoritesByDurationAsc()
                SortOrder.Descending -> songsFavoritesByDurationDesc()
            }
        }
    }

    @Transaction
    @Query("SELECT DISTINCT Song.*, Format.contentLength, Album.title FROM Song " +
            "LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "LEFT JOIN Format ON Format.songId = Song.id " +
            "WHERE likedAt IS NOT NULL AND likedAt = -1 " +
            " ORDER BY "+
            "    CASE :sortOrder WHEN 'ASC' THEN totalPlayTimeMs END ASC," +
            "    CASE :sortOrder WHEN 'DESC' THEN totalPlayTimeMs END DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsDislikedByPlayTime(sortOrder: String): Flow<List<SongEntity>>

    fun songsDislikedByRelativePlayTime(sortOrder: SortOrder): Flow<List<SongEntity>> {
        val songs = songsDislikedByPlayTime(sortOrder.toSQLString())
        songs.map { it }
        return songs.map {
            when(sortOrder) {
               SortOrder.Ascending -> it.sortedBy { se -> se.relativePlayTime() }
                else -> it.sortedByDescending { se -> se.relativePlayTime() }
            }
        }
    }

    @Transaction
    @Query("SELECT DISTINCT Song.*, Format.contentLength, Album.title FROM Song " +
            "LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "LEFT JOIN Format ON Format.songId = Song.id " +
            "WHERE likedAt IS NOT NULL AND likedAt = -1 " +
            " ORDER BY "+
            "    CASE :sortOrder WHEN 'ASC' THEN Song.title END COLLATE NOCASE ASC," +
            "    CASE :sortOrder WHEN 'DESC' THEN Song.title END COLLATE NOCASE DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsDislikedByTitle(sortOrder: String): Flow<List<SongEntity>>

    @Transaction
    @Query("SELECT DISTINCT Song.*, Format.contentLength, Album.title FROM Song " +
            "LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "LEFT JOIN Format ON Format.songId = Song.id " +
            "WHERE likedAt IS NOT NULL AND likedAt = -1 " +
            " ORDER BY "+
            "    CASE :sortOrder WHEN 'ASC' THEN Song.ROWID END ASC," +
            "    CASE :sortOrder WHEN 'DESC' THEN Song.ROWID END DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsDislikedByRowId(sortOrder: String): Flow<List<SongEntity>>

    @Transaction
    @Query("SELECT DISTINCT Song.*, Format.contentLength, Album.title FROM Song " +
            "LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "LEFT JOIN Format ON Format.songId = Song.id " +
            "LEFT JOIN Event E ON E.songId=Song.id " +
            "WHERE likedAt IS NOT NULL AND likedAt = -1" +
            " ORDER BY "+
            "    CASE :sortOrder WHEN 'ASC' THEN E.timestamp END ASC," +
            "    CASE :sortOrder WHEN 'DESC' THEN E.timestamp END DESC")
    fun songsDislikedByDatePlayed(sortOrder: String): Flow<List<SongEntity>>

    @Transaction
    @Query("SELECT DISTINCT Song.*, Format.contentLength, Album.title FROM Song " +
            "LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "LEFT JOIN Format ON Format.songId = Song.id " +
            "WHERE likedAt IS NOT NULL AND likedAt = -1" +
            " ORDER BY " +
            "    CASE :sortOrder WHEN 'ASC' THEN artistsText END ASC," +
            "    CASE :sortOrder WHEN 'DESC' THEN artistsText END DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsDislikedByArtist(sortOrder: String): Flow<List<SongEntity>>

    @Transaction
    @Query("SELECT DISTINCT Song.*, Format.contentLength, Album.title FROM Song " +
            "LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "LEFT JOIN Format ON Format.songId = Song.id " +
            "WHERE likedAt IS NOT NULL AND likedAt = -1 "+
            "ORDER BY " +
            "    CASE :sortOrder WHEN 'ASC' THEN durationText END ASC," +
            "    CASE :sortOrder WHEN 'DESC' THEN durationText END DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsDislikedByDuration(sortOrder: String): Flow<List<SongEntity>>

    @Transaction
    @Query("SELECT DISTINCT Song.*, Format.contentLength, Album.title FROM Song " +
            "LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "LEFT JOIN Format ON Format.songId = Song.id " +
            "WHERE Song.likedAt = -1 AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' " +
            "ORDER BY " +
            "    CASE :sortOrder WHEN 'ASC' THEN Album.title END COLLATE NOCASE ASC," +
            "    CASE :sortOrder WHEN 'DESC' THEN Album.title END COLLATE NOCASE DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsDislikedByAlbumName(sortOrder: String): Flow<List<SongEntity>>

    fun SortOrder.toSQLString(): String {
        return when(this){
            SortOrder.Ascending -> "ASC"
            SortOrder.Descending -> "DESC"
        }
    }

    fun songsDisliked(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<SongEntity>> {
        return when(sortBy){
            SongSortBy.PlayTime -> songsDislikedByPlayTime(sortOrder.toSQLString())
            SongSortBy.RelativePlayTime -> songsDislikedByRelativePlayTime(sortOrder)
            SongSortBy.Title -> songsDislikedByTitle(sortOrder.toSQLString())
            SongSortBy.DateAdded -> songsDislikedByRowId(sortOrder.toSQLString())
            SongSortBy.DatePlayed -> songsDislikedByDatePlayed(sortOrder.toSQLString())
            SongSortBy.DateLiked -> flowOf(emptyList()) // TODO correct text, method
            SongSortBy.Artist -> songsDislikedByArtist(sortOrder.toSQLString())
            SongSortBy.Duration -> songsDislikedByDuration(sortOrder.toSQLString())
            SongSortBy.AlbumName -> songsDislikedByAlbumName(sortOrder.toSQLString())
        }
    }

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT DISTINCT Song.*, Album.title as albumTitle FROM Song " +
            "LEFT JOIN Event E ON E.songId=Song.id LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' " +
            "ORDER BY E.timestamp DESC")
    fun songsByDatePlayedDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT DISTINCT Song.*, Album.title as albumTitle FROM Song " +
            "LEFT JOIN Event E ON E.songId=Song.id LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' " +
            "ORDER BY E.timestamp")
    @RewriteQueriesToDropUnusedColumns
    fun songsByDatePlayedAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.likedAt ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByLikedAtAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.likedAt DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByLikedAtDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.artistsText ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByArtistAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.artistsText DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByArtistDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.durationText ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByDurationAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.durationText DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByDurationDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Album.title COLLATE NOCASE ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByAlbumNameAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Album.title COLLATE NOCASE DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByAlbumNameDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.ROWID ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByRowIdAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByRowIdDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.title COLLATE NOCASE ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.title COLLATE NOCASE DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByTitleDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.totalPlayTimeMs ASC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    fun songsByRelativePlayTimeAsc(showHiddenSongs: Int = 0): Flow<List<SongEntity>> {
        val songs = songsByPlayTimeAsc(showHiddenSongs)
        songs.map { it }
        return songs.map {
            it.sortedBy { se ->
                se.relativePlayTime()
            }
        }
    }

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.*, Album.title as albumTitle FROM Song LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
            "LEFT JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.totalPlayTimeMs DESC")
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    fun songsByRelativePlayTimeDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>{
        val songs = songsByPlayTimeDesc(showHiddenSongs)
        songs.map { it }
        return songs.map {
            it.sortedByDescending { se ->
                se.relativePlayTime()
            }
        }
    }

    fun songs(sortBy: SongSortBy, sortOrder: SortOrder, showHiddenSongs: Int): Flow<List<SongEntity>> {
        return when (sortBy) {
            SongSortBy.AlbumName -> when (sortOrder) {
                SortOrder.Ascending -> songsByAlbumNameAsc(showHiddenSongs)
                SortOrder.Descending -> songsByAlbumNameDesc(showHiddenSongs)
            }
            SongSortBy.PlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsByPlayTimeAsc(showHiddenSongs)
                SortOrder.Descending -> songsByPlayTimeDesc(showHiddenSongs)
            }
            SongSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> songsByTitleAsc(showHiddenSongs)
                SortOrder.Descending -> songsByTitleDesc(showHiddenSongs)
            }
            SongSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> songsByRowIdAsc(showHiddenSongs)
                SortOrder.Descending -> songsByRowIdDesc(showHiddenSongs)
            }
            SongSortBy.DatePlayed -> when (sortOrder) {
                SortOrder.Ascending -> songsByDatePlayedAsc(showHiddenSongs)
                SortOrder.Descending -> songsByDatePlayedDesc(showHiddenSongs)
            }
            SongSortBy.DateLiked -> when (sortOrder) {
                SortOrder.Ascending -> songsByLikedAtAsc(showHiddenSongs)
                SortOrder.Descending -> songsByLikedAtDesc(showHiddenSongs)
            }
            SongSortBy.Artist -> when (sortOrder) {
                SortOrder.Ascending -> songsByArtistAsc(showHiddenSongs)
                SortOrder.Descending -> songsByArtistDesc(showHiddenSongs)
            }
            SongSortBy.Duration -> when (sortOrder) {
                SortOrder.Ascending -> songsByDurationAsc(showHiddenSongs)
                SortOrder.Descending -> songsByDurationDesc(showHiddenSongs)
            }

            SongSortBy.RelativePlayTime -> when (sortOrder) {
                SortOrder.Ascending -> songsByRelativePlayTimeAsc(showHiddenSongs)
                SortOrder.Descending -> songsByRelativePlayTimeDesc(showHiddenSongs)
            }
        }
    }
    //**********************************************



    @Transaction
    @Query("SELECT * FROM Format WHERE songId = :songId ORDER BY bitrate DESC LIMIT 1")
    fun getBestFormat(songId: String): Flow<Format?>

    @Transaction
    @Query("SELECT * FROM Format ORDER BY lastModified DESC LIMIT 1")
    fun getLastBestFormat(): Flow<Format?>

    @Transaction
    @Query("SELECT * FROM Song WHERE id in (SELECT songId FROM Format ORDER BY lastModified DESC LIMIT 1)")
    fun getLastSongPlayed(): Flow<Song?>

    @Transaction
    @Query("SELECT COUNT(id) from Song WHERE id = :id and title LIKE '${EXPLICIT_PREFIX}%'")
    fun isSongExplicit(id: String): Int

    @Transaction
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT DISTINCT (timestamp / 86400000) as timestampDay, event.* FROM event ORDER BY rowId DESC")
    fun events(): Flow<List<EventWithSong>>

    @Transaction
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT Event.* FROM Event JOIN Song ON Song.id = songId WHERE " +
            "Event.timestamp / 86400000 = :date / 86400000 LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun eventWithSongByPeriod(date: Long, limit:Long = Long.MAX_VALUE): Flow<List<EventWithSong>>

    @Transaction
    @Query("SELECT * FROM Playlist WHERE isYoutubePlaylist = 1")
    fun ytmPrivatePlaylists(): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM Song WHERE totalPlayTimeMs > 0 ORDER BY totalPlayTimeMs DESC LIMIT :count")
    @RewriteQueriesToDropUnusedColumns
    fun topSongs(count: Int = 10): Flow<List<Song>>

    @Transaction
    @Query("SELECT count(playlistId) FROM SongPlaylistMap WHERE songId = :id")
    fun songUsedInPlaylists(id: String): Int

    data class PlayListIdPosition(val playlistId: Long, val position: Int)
    @Transaction
    @Query("SELECT playlistId, position FROM SongPlaylistMap WHERE songId = :id")
    fun playlistsUsedForSong(id: String): List<PlayListIdPosition>

    @Transaction
    @Query("SELECT position FROM SongPlaylistMap WHERE playlistId = :playlistId AND songId = :id")
    fun positionInPlaylist(id: String, playlistId: Long): Int

    @Query("SELECT COUNT(1) FROM Song WHERE likedAt IS NOT NULL")
    fun likedSongsCount(): Flow<Int>

    @Query("SELECT COUNT(1) FROM Song WHERE id LIKE '$LOCAL_KEY_PREFIX%'")
    fun onDeviceSongsCount(): Flow<Int>

    @Transaction
    @Query("SELECT * FROM Song WHERE artistsText = :name ")
    fun artistSongsByname(name: String): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song")
    fun flowListAllSongs(): Flow<List<Song>>

    @Query("SELECT id FROM Playlist WHERE name = :playlistName")
    fun playlistExistByName(playlistName: String): Long

    @Query("UPDATE Playlist SET name = :playlistName WHERE id = :playlistId")
    fun updatePlaylistName(playlistName: String, playlistId: Long): Int

    @Transaction
    @Query("UPDATE Song SET title = :title WHERE id = :id")
    fun updateSongTitle(id: String, title: String): Int

    @Transaction
    @Query("UPDATE Song SET artistsText = :artist WHERE id = :id")
    fun updateSongArtist(id: String, artist: String): Int

    @Query("UPDATE Album SET thumbnailUrl = :thumb WHERE id = :id")
    fun updateAlbumCover(id: String, thumb: String): Int

    @Query("UPDATE Album SET authorsText = :artist WHERE id = :id")
    fun updateAlbumAuthors(id: String, artist: String): Int

    @Query("UPDATE Album SET title = :title WHERE id = :id")
    fun updateAlbumTitle(id: String, title: String): Int

    @Query("UPDATE Artist SET name = :name WHERE id = :id")
    fun updateArtistName(id: String, name: String): Int

    @Transaction
    @Query("SELECT * FROM Artist WHERE id in (:idsList)")
    @RewriteQueriesToDropUnusedColumns
    fun getArtistsList(idsList: List<String>): Flow<List<Artist?>>

    @Transaction
    @Query("SELECT * FROM Artist")
    @RewriteQueriesToDropUnusedColumns
    fun getArtistsList(): Flow<List<Artist?>>

    @Transaction
    @Query("SELECT * FROM Song WHERE id in (:idsList) ")
    @RewriteQueriesToDropUnusedColumns
    fun getSongsList(idsList: List<String>): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM Song WHERE id in (:idsList) ")
    @RewriteQueriesToDropUnusedColumns
    fun getSongsListNoFlow(idsList: List<String>): List<Song>

    @Query("SELECT thumbnailUrl FROM Song WHERE id in (:idsList) ")
    fun getSongsListThumbnailUrls(idsList: List<String>): Flow<List<String?>>

    @Transaction
    @Query("SELECT * FROM Song WHERE ROWID='wooowww' ")
    @RewriteQueriesToDropUnusedColumns
    fun fakeSongsList(): Flow<List<Song>>

    @Transaction
    //@Query("SELECT Playlist.*, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = Playlist.id) as songCount " +
    //        "FROM Song JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
    //        "JOIN Event ON Song.id = Event.songId JOIN Playlist ON Playlist.id = SongPlaylistMap.playlistId " +
    //        "WHERE Event.timestamp BETWEEN :from AND :to GROUP BY Playlist.id ORDER BY Event.timestamp DESC LIMIT :limit")
    @Query("SELECT Playlist.*, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = Playlist.id) as songCount " +
            "FROM Song JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "JOIN Event ON Song.id = Event.songId JOIN Playlist ON Playlist.id = SongPlaylistMap.playlistId " +
            "WHERE (:to - Event.timestamp) <= :from GROUP BY Playlist.id ORDER BY SUM(Event.playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun playlistsMostPlayedByPeriod(from: Long,to: Long, limit:Int): Flow<List<PlaylistPreview>>

    @Transaction
    //@Query("SELECT Album.* FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
    //        "JOIN Event ON Song.id = Event.songId JOIN Album ON Album.id = SongAlbumMap.albumId " +
    //        "WHERE Event.timestamp BETWEEN :from AND :to GROUP BY Album.id ORDER BY Event.timestamp DESC LIMIT :limit")
    @Query("SELECT Album.* FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId " +
            "JOIN Event ON Song.id = Event.songId JOIN Album ON Album.id = SongAlbumMap.albumId " +
            "WHERE (:to - Event.timestamp) <= :from GROUP BY Album.id ORDER BY SUM(Event.playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun albumsMostPlayedByPeriod(from: Long,to: Long, limit:Int): Flow<List<Album>>

    @Transaction
    //@Query("SELECT Artist.* FROM Song JOIN SongArtistMap ON Song.id = SongArtistMap.songId " +
    //        "JOIN Event ON Song.id = Event.songId JOIN Artist ON Artist.id = SongArtistMap.artistId " +
    //        "WHERE Event.timestamp BETWEEN :from AND :to GROUP BY Artist.id ORDER BY Event.timestamp DESC LIMIT :limit")
    @Query("SELECT Artist.* FROM Song JOIN SongArtistMap ON Song.id = SongArtistMap.songId " +
            "JOIN Event ON Song.id = Event.songId JOIN Artist ON Artist.id = SongArtistMap.artistId " +
            "WHERE (:to - Event.timestamp) <= :from GROUP BY Artist.id ORDER BY SUM(Event.playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun artistsMostPlayedByPeriod(from: Long,to: Long, limit:Int): Flow<List<Artist>>

    @Transaction
    //@Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE timestamp " +
    //        "BETWEEN :from AND :to GROUP BY songId  ORDER BY timestamp DESC LIMIT :limit")
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE " +
            "(:to - Event.timestamp) <= :from GROUP BY songId  ORDER BY SUM(playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun songsMostPlayedByPeriod(from: Long, to: Long, limit:Long = Long.MAX_VALUE): Flow<List<Song>>

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE " +
            "CAST(strftime('%m',timestamp / 1000,'unixepoch') AS INTEGER) = :month AND CAST(strftime('%Y',timestamp / 1000,'unixepoch') as INTEGER) = :year " +
            "GROUP BY songId  ORDER BY timestamp DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun songsMostPlayedByYearMonth(year: Long, month: Long, limit:Long = Long.MAX_VALUE): Flow<List<Song>>

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE " +
            "CAST(strftime('%m',timestamp / 1000,'unixepoch') AS INTEGER) = :month AND CAST(strftime('%Y',timestamp / 1000,'unixepoch') as INTEGER) = :year " +
            "GROUP BY songId  ORDER BY timestamp DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun songsMostPlayedByYearMonthNoFlow(year: Long, month: Long, limit:Long = Long.MAX_VALUE): List<Song>

    @Transaction
    @Query("SELECT * FROM Song WHERE id LIKE '$LOCAL_KEY_PREFIX%'")
    @RewriteQueriesToDropUnusedColumns
    fun songsOnDevice(): Flow<List<Song>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Song WHERE id LIKE '$LOCAL_KEY_PREFIX%'")
    @RewriteQueriesToDropUnusedColumns
    fun songsEntityOnDevice(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.likedAt IS NOT NULL 
        ORDER BY artistsText
    """)
    fun sortFavoriteSongsByArtist(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.likedAt IS NOT NULL 
        ORDER BY totalPlayTimeMs
    """)
    fun sortFavoriteSongsByPlayTime(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.likedAt IS NOT NULL 
        ORDER BY 
            CASE
                WHEN Song.title LIKE "$EXPLICIT_PREFIX%" THEN SUBSTR(Song.title, LENGTH('$EXPLICIT_PREFIX') + 1)
                ELSE Song.title
            END
        COLLATE NOCASE
    """)
    fun sortFavoriteSongsByTitle(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.likedAt IS NOT NULL 
        ORDER BY Song.ROWID
    """)
    fun sortFavoriteSongsByRowId(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.likedAt IS NOT NULL 
        ORDER BY likedAt
    """)
    fun sortFavoriteSongsByLikedAt(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        LEFT JOIN Event E ON E.songId = Song.id 
        WHERE Song.likedAt IS NOT NULL 
        ORDER BY E.timestamp
    """)
    fun sortFavoriteSongsByDatePlayed(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.likedAt IS NOT NULL 
        ORDER BY durationText
    """)
    fun sortFavoriteSongsByDuration(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.likedAt IS NOT NULL 
        ORDER BY Album.title
    """)
    fun sortFavoriteSongsByAlbum(): Flow<List<SongEntity>>

    /**
     * Fetch all songs that are liked by the user
     * from the database and sort them according to
     * [sortBy] and [sortOrder].
     *
     * [sortBy] sorts all based on each song's property
     * such as [SongSortBy.Title], [SongSortBy.PlayTime], etc.
     * While [sortOrder] arranges order of sorted songs
     * to follow alphabetical order A to Z, or numerical order 0 to 9, etc.
     *
     *
     * @param sortBy which song's property is used to sort
     * @param sortOrder what order should results be in
     *
     * @return a **SORTED** list of [SongEntity]'s that are continuously
     * updated to reflect changes within the database - wrapped by [Flow]
     *
     * @see SongSortBy
     * @see SortOrder
     */
    fun listFavoriteSongs(
        sortBy: SongSortBy,
        sortOrder: SortOrder
    ): Flow<List<SongEntity>> = when (sortBy) {
        SongSortBy.PlayTime -> sortFavoriteSongsByPlayTime()
        SongSortBy.Title -> sortFavoriteSongsByTitle()
        SongSortBy.DateAdded -> sortFavoriteSongsByRowId()
        SongSortBy.DatePlayed -> sortFavoriteSongsByDatePlayed()
        SongSortBy.DateLiked -> sortFavoriteSongsByLikedAt()
        SongSortBy.Artist -> sortFavoriteSongsByArtist()
        SongSortBy.Duration -> sortFavoriteSongsByDuration()
        SongSortBy.AlbumName -> sortFavoriteSongsByAlbum()
        SongSortBy.RelativePlayTime -> TODO()
    }.map(sortOrder::applyTo)

    @Query("SELECT thumbnailUrl FROM Song WHERE likedAt IS NOT NULL AND id NOT LIKE '$LOCAL_KEY_PREFIX%'  LIMIT 4")
    fun preferitesThumbnailUrls(): Flow<List<String?>>

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song LEFT JOIN Format ON id = songId WHERE songId = :songId")
    fun songCached(songId: String): Flow<SongWithContentLength?>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Format.contentLength IS NOT NULL 
        ORDER BY totalPlayTimeMs
    """)
    fun sortOfflineSongsByPlayTime(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Format.contentLength IS NOT NULL 
        ORDER BY 
            CASE
                WHEN Song.title LIKE "$EXPLICIT_PREFIX%" THEN SUBSTR(Song.title, LENGTH('$EXPLICIT_PREFIX') + 1)
                ELSE Song.title
            END
        COLLATE NOCASE
    """)
    fun sortOfflineSongsByTitle(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Format.contentLength IS NOT NULL 
        ORDER BY Song.ROWID
    """)
    fun sortOfflineSongsByRowId(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Format.contentLength IS NOT NULL 
        ORDER BY Song.likedAt
    """)
    fun sortOfflineSongsByLikedAt(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Format.contentLength IS NOT NULL 
        ORDER BY Song.artistsText
    """)
    fun sortOfflineSongsByArtist(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Format.contentLength IS NOT NULL 
        ORDER BY Song.durationText
    """)
    fun sortOfflineSongsByDuration(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN Event E ON E.songId=Song.id 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Format.contentLength IS NOT NULL 
        ORDER BY E.timestamp
    """)
    fun sortOfflineSongsByDatePlayed(): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Format.contentLength IS NOT NULL 
        ORDER BY Album.title COLLATE NOCASE
    """)
    fun sortOfflineSongsByAlbum(): Flow<List<SongEntity>>

    /**
     * Fetch all songs from that are cached the database
     * and sort them according to [sortBy] and [sortOrder].
     *
     * [sortBy] sorts all based on each song's property
     * such as [SongSortBy.Title], [SongSortBy.PlayTime], etc.
     * While [sortOrder] arranges order of sorted songs
     * to follow alphabetical order A to Z, or numerical order 0 to 9, etc.
     *
     * @param sortBy which song's property is used to sort
     * @param sortOrder what order should results be in
     *
     * @return a **SORTED** list of [SongEntity]'s that are continuously
     * updated to reflect changes within the database - wrapped by [Flow]
     *
     * @see SongSortBy
     * @see SortOrder
     */
    fun listOfflineSongs(
        sortBy: SongSortBy,
        sortOrder: SortOrder
    ): Flow<List<SongEntity>> = when (sortBy) {
        SongSortBy.PlayTime -> sortOfflineSongsByPlayTime()
        SongSortBy.Title -> sortOfflineSongsByTitle()
        SongSortBy.DateAdded -> sortOfflineSongsByRowId()
        SongSortBy.DatePlayed -> sortOfflineSongsByDatePlayed()
        SongSortBy.DateLiked -> sortOfflineSongsByLikedAt()
        SongSortBy.Artist -> sortOfflineSongsByArtist()
        SongSortBy.Duration -> sortOfflineSongsByDuration()
        SongSortBy.AlbumName -> sortOfflineSongsByAlbum()
        SongSortBy.RelativePlayTime -> TODO()
    }.map( sortOrder::applyTo )

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden 
        ORDER BY Song.ROWID
    """)
    fun sortAllSongsByRowId(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.id in (:filterList)
        ORDER BY Song.ROWID
    """)
    fun sortAllSongsByRowId_Filtered(filterList: List<String>): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden 
        ORDER BY 
            CASE
                WHEN Song.title LIKE "$EXPLICIT_PREFIX%" THEN SUBSTR(Song.title, LENGTH('$EXPLICIT_PREFIX') + 1)
                ELSE Song.title
            END
        COLLATE NOCASE
    """)
    fun sortAllSongsByTitle(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.id in (:filterList)
        ORDER BY 
            CASE
                WHEN Song.title LIKE "$EXPLICIT_PREFIX%" THEN SUBSTR(Song.title, LENGTH('$EXPLICIT_PREFIX') + 1)
                ELSE Song.title
            END
        COLLATE NOCASE
    """)
    fun sortAllSongsByTitle_Filtered(filterList: List<String>): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden 
        ORDER BY Song.totalPlayTimeMs
    """)
    fun sortAllSongsByPlayTime(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.id in (:filterList)
        ORDER BY Song.totalPlayTimeMs
    """)
    fun sortAllSongsByPlayTime_Filtered(
        filterList: List<String>): Flow<List<SongEntity>>

    fun sortAllSongsByRelativePlayTime(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>{
        val songs = sortAllSongsByPlayTime(showHidden)
        return songs.map {
                it.sortedBy { se ->
                    val totalPlayTimeMs = se.song.totalPlayTimeMs
                    if(totalPlayTimeMs > 0) se.contentLength?.div(totalPlayTimeMs) ?: 0L else 0L
            }
        }
    }

    fun sortAllSongsByRelativePlayTime_Filtered(
        filterList: List<String>): Flow<List<SongEntity>>{
        val songs = sortAllSongsByPlayTime_Filtered(filterList)
        return songs.map {
            it.sortedBy { se ->
                val totalPlayTimeMs = se.song.totalPlayTimeMs
                if(totalPlayTimeMs > 0) se.contentLength?.div(totalPlayTimeMs) ?: 0L else 0L
            }
        }
    }

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN Event E ON E.songId=Song.id 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden 
        ORDER BY E.timestamp
    """)
    fun sortAllSongsByDatePlayed(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN Event E ON E.songId=Song.id 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.id in (:filterList)
        ORDER BY E.timestamp
    """)
    fun sortAllSongsByDatePlayed_Filtered(filterList: List<String>): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden 
        ORDER BY Song.likedAt
    """)
    fun sortAllSongsByLikedAt(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.id in (:filterList)
        ORDER BY Song.likedAt
    """)
    fun sortAllSongsByLikedAt_Filtered(filterList: List<String>): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden 
        ORDER BY Song.artistsText COLLATE NOCASE
    """)
    fun sortAllSongsByArtist(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.id in (:filterList)
        ORDER BY Song.artistsText COLLATE NOCASE
    """)
    fun sortAllSongsByArtist_Filtered(filterList: List<String>): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden 
        ORDER BY Song.durationText
    """)
    fun sortAllSongsByDuration(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.id in (:filterList)
        ORDER BY Song.durationText
    """)
    fun sortAllSongsByDuration_Filtered(filterList: List<String>): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden 
        ORDER BY Album.title COLLATE NOCASE
    """)
    fun sortAllSongsByAlbum(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song 
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.id in (:filterList)
        ORDER BY Album.title COLLATE NOCASE
    """)
    fun sortAllSongsByAlbum_Filtered(filterList: List<String>): Flow<List<SongEntity>>

    /**
     * Fetch all songs from the database and sort them
     * according to [sortBy] and [sortOrder]. It also
     * excludes songs if condition of [showHidden] is met.
     *
     * [sortBy] sorts all based on each song's property
     * such as [SongSortBy.Title], [SongSortBy.PlayTime], etc.
     * While [sortOrder] arranges order of sorted songs
     * to follow alphabetical order A to Z, or numerical order 0 to 9, etc.
     *
     * [showHidden] is an optional parameter that indicates
     * whether the final results contain songs that are hidden
     * (in)directly by the user.
     * `-1` shows hidden while `0` does not.
     *
     * @param sortBy which song's property is used to sort
     * @param sortOrder what order should results be in
     * @param showHidden include hidden songs to final results or not
     *
     * @return a **SORTED** list of [SongEntity]'s that are continuously
     * updated to reflect changes within the database - wrapped by [Flow]
     *
     * @see SongSortBy
     * @see SortOrder
     */
    fun listAllSongs(
        sortBy: SongSortBy,
        sortOrder: SortOrder,
        @MagicConstant(intValues = [1, 0]) showHidden: Int,
        filterList: List<String>,
        playList: BuiltInPlaylist
    ): Flow<List<SongEntity>> = when( sortBy ) {
        // Due to the unknown amount of songs, letting SQLite handle
        // the sorting is a better idea
        SongSortBy.PlayTime -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByPlayTime( showHidden )
        else sortAllSongsByPlayTime_Filtered(filterList )
        SongSortBy.RelativePlayTime -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByRelativePlayTime(showHidden)
        else sortAllSongsByRelativePlayTime_Filtered(filterList)
        SongSortBy.Title -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByTitle( showHidden )
        else sortAllSongsByTitle_Filtered(filterList )
        SongSortBy.DateAdded -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByRowId( showHidden )
        else sortAllSongsByRowId_Filtered(filterList )
        SongSortBy.DatePlayed -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByDatePlayed( showHidden )
        else sortAllSongsByDatePlayed_Filtered(filterList )
        SongSortBy.DateLiked -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByLikedAt( showHidden )
        else sortAllSongsByLikedAt_Filtered(filterList )
        SongSortBy.Artist -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByArtist( showHidden )
        else sortAllSongsByArtist_Filtered(filterList )
        SongSortBy.Duration -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByDuration( showHidden )
        else sortAllSongsByDuration_Filtered(filterList )
        SongSortBy.AlbumName -> if (filterList.isEmpty() && playList != BuiltInPlaylist.Downloaded) sortAllSongsByAlbum( showHidden )
        else sortAllSongsByAlbum_Filtered(filterList )
    }.map( sortOrder::applyTo )

    /**
     * Fetch all songs from the database, 
     * excludes songs if condition of [showHidden] is met.
     *
     * [showHidden] is an optional parameter that indicates
     * whether the final results contain songs that are hidden
     * (in)directly by the user.
     * `-1` shows hidden while `0` does not.
     *
     * @param showHidden include hidden songs to final results or not
     *
     * @return an **UNSORTED** list of [SongEntity]'s that are continuously
     * updated to reflect changes within the database - wrapped by [Flow]
     */
    @Query("""
        SELECT DISTINCT Song.*, Format.contentLength, Album.title
        FROM Song
        LEFT JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId
        LEFT JOIN Format ON Format.songId = Song.id
        WHERE Song.totalPlayTimeMs >= :showHidden
    """)
    fun listAllSongs(
        @MagicConstant(intValues = [1, 0]) showHidden: Int
    ): Flow<List<SongEntity>>

    @Transaction
    @Query(
        """
        SELECT * FROM Song
        WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%'
        ORDER BY totalPlayTimeMs DESC
        LIMIT :limit
        """
    )
    @RewriteQueriesToDropUnusedColumns
    fun songsByPlayTimeWithLimitDesc(limit: Int = -1): Flow<List<Song>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query(
        """
        SELECT * FROM Song
        WHERE id NOT LIKE '$LOCAL_KEY_PREFIX%'
        ORDER BY totalPlayTimeMs DESC
        LIMIT :limit
        """
    )
    @RewriteQueriesToDropUnusedColumns
    fun songsEntityByPlayTimeWithLimitDesc(limit: Int = -1): Flow<List<SongEntity>>

//    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
//    @Transaction
//    @Query("SELECT Song.*, Album.title as albumTitle FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId  " +
//            "JOIN Album ON Album.id = SongAlbumMap.albumId " +
//            "WHERE (Song.totalPlayTimeMs > :showHiddenSongs OR Song.likedAt NOT NULL) AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY Song.artistsText DESC")
//    @RewriteQueriesToDropUnusedColumns
//    fun songsWithAlbumByPlayTimeDesc(showHiddenSongs: Int = 0): Flow<List<SongEntity>>

    @Query("SELECT thumbnailUrl FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0  LIMIT 4")
    fun offlineThumbnailUrls(): Flow<List<String?>>

    @Transaction
    @Query("SELECT * FROM Song WHERE likedAt IS NOT NULL ORDER BY likedAt DESC")
    @RewriteQueriesToDropUnusedColumns
    fun favorites(): Flow<List<Song>>

    @Query("SELECT * FROM QueuedMediaItem")
    fun queue(): List<QueuedMediaItem>

    @Query("DELETE FROM QueuedMediaItem")
    fun clearQueue()

    @Query("SELECT * FROM SearchQuery WHERE `query` LIKE :query ORDER BY id DESC")
    fun queries(query: String): Flow<List<SearchQuery>>

    @Query("SELECT COUNT (*) FROM SearchQuery")
    fun queriesCount(): Flow<Int>

    @Query("DELETE FROM SearchQuery")
    fun clearQueries()

    @Query("UPDATE Playlist SET name = '${PINNED_PREFIX}'||name WHERE id = :playlistId")
    fun pinPlaylist(playlistId: Long): Int
    @Query("UPDATE Playlist SET name = REPLACE(name,'${PINNED_PREFIX}','') WHERE id = :playlistId")
    fun unPinPlaylist(playlistId: Long): Int

    @Query("SELECT count(id) FROM Song WHERE id = :songId and likedAt IS NOT NULL and likedAt > 0")
    fun songliked(songId: String): Int

    @Query("SELECT * FROM Song WHERE id = :id")
    fun song(id: String?): Flow<Song?>

    @Query("SELECT count(id) FROM Song WHERE id = :id")
    fun songExist(id: String): Int

    @Query("SELECT likedAt FROM Song WHERE id = :songId")
    fun likedAt(songId: String): Flow<Long?>

    @Query("SELECT likedAt FROM Song WHERE id = :songId")
    fun getLikedAt(songId: String): Long?

    @Query("SELECT id FROM Song WHERE id = :songId AND likedAt < 0")
    fun songDisliked(songId: String): String?

    @Query("UPDATE Album SET bookmarkedAt = :bookmarkedAt WHERE id = :id")
    fun bookmarkAlbum(id: String, bookmarkedAt: Long?): Int

    @Query("UPDATE Song SET likedAt = :likedAt WHERE id = :songId")
    fun like(songId: String, likedAt: Long?): Int

    @Query("UPDATE Song SET durationText = :durationText WHERE id = :songId")
    fun updateDurationText(songId: String, durationText: String): Int

    @Query("SELECT * FROM Lyrics WHERE songId = :songId")
    fun lyrics(songId: String): Flow<Lyrics?>

    @Query("SELECT * FROM Artist WHERE id = :id")
    fun artist(id: String): Flow<Artist?>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY name")
    fun preferitesArtistsByName(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY name DESC")
    fun artistsByNameDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY name ASC")
    fun artistsByNameAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt DESC")
    fun artistsByRowIdDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt ASC")
    fun artistsByRowIdAsc(): Flow<List<Artist>>

    fun artists(sortBy: ArtistSortBy, sortOrder: SortOrder): Flow<List<Artist>> {
        return when (sortBy) {
            ArtistSortBy.Name -> when (sortOrder) {
                SortOrder.Ascending -> artistsByNameAsc()
                SortOrder.Descending -> artistsByNameDesc()
            }
            ArtistSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> artistsByRowIdAsc()
                SortOrder.Descending -> artistsByRowIdDesc()
            }
        }
    }

    @Query("SELECT * FROM Artist A WHERE A.id in ( " +
            "SELECT DISTINCT artistId FROM SongArtistMap INNER JOIN Song " +
            "ON Song.id = SongArtistMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.name ASC")
    fun artistsInLibraryByNameAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist A WHERE A.id in ( " +
            "SELECT DISTINCT artistId FROM SongArtistMap INNER JOIN Song " +
            "ON Song.id = SongArtistMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.name DESC")
    fun artistsInLibraryByNameDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist A WHERE A.id in ( " +
            "SELECT DISTINCT artistId FROM SongArtistMap INNER JOIN Song " +
            "ON Song.id = SongArtistMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.bookmarkedAt ASC")
    fun artistsInLibraryByRowIdAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist A WHERE A.id in ( " +
            "SELECT DISTINCT artistId FROM SongArtistMap INNER JOIN Song " +
            "ON Song.id = SongArtistMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.bookmarkedAt DESC")
    fun artistsInLibraryByRowIdDesc(): Flow<List<Artist>>

    fun artistsInLibrary(sortBy: ArtistSortBy, sortOrder: SortOrder): Flow<List<Artist>> {
        return when (sortBy) {
            ArtistSortBy.Name -> when (sortOrder) {
                SortOrder.Ascending -> artistsInLibraryByNameAsc()
                SortOrder.Descending -> artistsInLibraryByNameDesc()
            }
            ArtistSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> artistsInLibraryByRowIdAsc()
                SortOrder.Descending -> artistsInLibraryByRowIdDesc()
            }
        }
    }

    @Query("SELECT * FROM Artist A " +
            "WHERE A.id in ( SELECT DISTINCT artistId FROM SongArtistMap ) " +
            "ORDER BY A.name ASC")
    fun artistsWithSongsSavedByNameAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist A " +
            "WHERE A.id in ( SELECT DISTINCT artistId FROM SongArtistMap ) " +
            "ORDER BY A.name DESC")
    fun artistsWithSongsSavedByNameDesc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist A " +
            "WHERE A.id in ( SELECT DISTINCT artistId FROM SongArtistMap ) " +
            "ORDER BY A.bookmarkedAt ASC")
    fun artistsWithSongsSavedByRowIdAsc(): Flow<List<Artist>>

    @Query("SELECT * FROM Artist A " +
            "WHERE A.id in ( SELECT DISTINCT artistId FROM SongArtistMap ) " +
            "ORDER BY A.bookmarkedAt DESC")
    fun artistsWithSongsSavedByRowIdDesc(): Flow<List<Artist>>

    fun artistsWithSongsSaved(sortBy: ArtistSortBy, sortOrder: SortOrder): Flow<List<Artist>> {
        return when (sortBy) {
            ArtistSortBy.Name -> when (sortOrder) {
                SortOrder.Ascending -> artistsWithSongsSavedByNameAsc()
                SortOrder.Descending -> artistsWithSongsSavedByNameDesc()
            }
            ArtistSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> artistsWithSongsSavedByRowIdAsc()
                SortOrder.Descending -> artistsWithSongsSavedByRowIdDesc()
            }
        }
    }

    @Query("SELECT * FROM Album WHERE id = :id")
    fun album(id: String): Flow<Album?>

    @Query("SELECT * FROM Album")
    fun getAlbumsList(): Flow<List<Album?>>

    @Query("SELECT timestamp FROM Album WHERE id = :id")
    fun albumTimestamp(id: String): Long?

    @Query("SELECT bookmarkedAt FROM Album WHERE id = :id")
    fun albumBookmarkedAt(id: String): Flow<Long?>

    @Query("SELECT count(id) FROM Album WHERE id = :id and bookmarkedAt IS NOT NULL")
    fun albumBookmarked(id: String): Int

    @Query("SELECT count(id) FROM Album WHERE id = :id")
    fun albumExist(id: String): Int

    @Transaction
    @Query("SELECT DISTINCT * FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = :albumId ORDER BY position")
    @RewriteQueriesToDropUnusedColumns
    fun albumSongsList(albumId: String): List<Song>

    @Transaction
    @Query("SELECT DISTINCT * FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = :albumId AND position IS NOT NULL ORDER BY position")
    @RewriteQueriesToDropUnusedColumns
    fun albumSongs(albumId: String): Flow<List<Song>>

    @Transaction
    @Query("SELECT *, (SELECT SUM(CAST(REPLACE(durationText, ':', '') AS INTEGER)) FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = Album.id AND position IS NOT NULL) as totalDuration " +
            "FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY totalDuration ASC" )
    @RewriteQueriesToDropUnusedColumns
    fun albumsByTotalDurationAsc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT *, (SELECT SUM(CAST(REPLACE(durationText, ':', '') AS INTEGER)) FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = Album.id AND position IS NOT NULL) as totalDuration " +
            "FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY totalDuration DESC" )
    @RewriteQueriesToDropUnusedColumns
    fun albumsByTotalDurationDesc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongAlbumMap WHERE albumId = Album.id) as songCount " +
            "FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY songCount ASC" )
    @RewriteQueriesToDropUnusedColumns
    fun albumsBySongsCountAsc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongAlbumMap WHERE albumId = Album.id) as songCount " +
            "FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY songCount DESC" )
    @RewriteQueriesToDropUnusedColumns
    fun albumsBySongsCountDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY authorsText COLLATE NOCASE ASC")
    fun albumsByArtistAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY authorsText COLLATE NOCASE DESC")
    fun albumsByArtistDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY title COLLATE NOCASE ASC")
    fun albumsByTitleAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY year ASC")
    fun albumsByYearAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt ASC")
    fun albumsByRowIdAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY title COLLATE NOCASE DESC")
    fun albumsByTitleDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY year DESC")
    fun albumsByYearDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt DESC")
    fun albumsByRowIdDesc(): Flow<List<Album>>

    fun albums(sortBy: AlbumSortBy, sortOrder: SortOrder): Flow<List<Album>> {
        return when (sortBy) {
            AlbumSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> albumsByTitleAsc()
                SortOrder.Descending -> albumsByTitleDesc()
            }
            AlbumSortBy.Year -> when (sortOrder) {
                SortOrder.Ascending -> albumsByYearAsc()
                SortOrder.Descending -> albumsByYearDesc()
            }
            AlbumSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> albumsByRowIdAsc()
                SortOrder.Descending -> albumsByRowIdDesc()
            }
            AlbumSortBy.Artist -> when (sortOrder) {
                SortOrder.Ascending -> albumsByArtistAsc()
                SortOrder.Descending -> albumsByArtistDesc()
            }
            AlbumSortBy.Songs -> when (sortOrder) {
                SortOrder.Ascending -> albumsBySongsCountAsc()
                SortOrder.Descending -> albumsBySongsCountDesc()
            }
            AlbumSortBy.Duration -> when (sortOrder) {
                SortOrder.Ascending -> albumsByTotalDurationAsc()
                SortOrder.Descending -> albumsByTotalDurationDesc()
            }
        }
    }

    @Query("SELECT * FROM Album A WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.title COLLATE NOCASE ASC")
    fun albumsInLibraryByTitleAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.title COLLATE NOCASE DESC")
    fun albumsInLibraryByTitleDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.year ASC")
    fun albumsInLibraryByYearAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.year DESC")
    fun albumsInLibraryByYearDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.bookmarkedAt ASC")
    fun albumsInLibraryByRowIdAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.bookmarkedAt DESC")
    fun albumsInLibraryByRowIdDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.authorsText COLLATE NOCASE ASC")
    fun albumsInLibraryByArtistAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY A.authorsText COLLATE NOCASE DESC")
    fun albumsInLibraryByArtistDesc(): Flow<List<Album>>

    @Query("SELECT *, (SELECT COUNT(*) FROM SongAlbumMap WHERE albumId = A.id) as songCount FROM Album A " +
            "WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
                "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
                "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
                ") " +
            "ORDER BY songCount ASC" )
    @RewriteQueriesToDropUnusedColumns
    fun albumsInLibraryBySongsCountAsc(): Flow<List<Album>>

    @Query("SELECT *, (SELECT COUNT(*) FROM SongAlbumMap WHERE albumId = A.id) as songCount FROM Album A " +
            "WHERE A.id in (" +
            "SELECT DISTINCT albumId FROM SongAlbumMap INNER JOIN Song " +
            "ON Song.id = SongAlbumMap.songId " +
            "LEFT JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE (Song.totalPlayTimeMs > 0 AND Song.likedAt > 0) OR SongPlaylistMap.playlistId IS NOT NULL " +
            ") " +
            "ORDER BY songCount DESC" )
    @RewriteQueriesToDropUnusedColumns
    fun albumsInLibraryBySongsCountDesc(): Flow<List<Album>>

    fun albumsInLibrary(sortBy: AlbumSortBy, sortOrder: SortOrder): Flow<List<Album>> {
        return when (sortBy) {
            AlbumSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> albumsInLibraryByTitleAsc()
                SortOrder.Descending -> albumsInLibraryByTitleDesc()
            }
            AlbumSortBy.Year -> when (sortOrder) {
                SortOrder.Ascending -> albumsInLibraryByYearAsc()
                SortOrder.Descending -> albumsInLibraryByYearDesc()
            }
            AlbumSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> albumsInLibraryByRowIdAsc()
                SortOrder.Descending -> albumsInLibraryByRowIdDesc()
            }
            AlbumSortBy.Artist -> when (sortOrder) {
                SortOrder.Ascending -> albumsInLibraryByArtistAsc()
                SortOrder.Descending -> albumsInLibraryByArtistDesc()
            }
            AlbumSortBy.Songs -> when (sortOrder) {
                SortOrder.Ascending -> albumsInLibraryBySongsCountAsc()
                SortOrder.Descending -> albumsInLibraryBySongsCountDesc()
            }
            AlbumSortBy.Duration -> when (sortOrder) {
                SortOrder.Ascending -> albumsByTotalDurationAsc()
                SortOrder.Descending -> albumsByTotalDurationDesc()
            }
        }
    }

    @Query("SELECT * FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY A.title COLLATE NOCASE ASC")
    fun albumsWithSongsSavedByTitleAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY A.title COLLATE NOCASE DESC")
    fun albumsWithSongsSavedByTitleDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY A.year ASC")
    fun albumsWithSongsSavedByYearAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY A.year DESC")
    fun albumsWithSongsSavedByYearDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY A.bookmarkedAt ASC")
    fun albumsWithSongsSavedByRowIdAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY A.bookmarkedAt DESC")
    fun albumsWithSongsSavedByRowIdDesc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY A.authorsText COLLATE NOCASE ASC")
    fun albumsWithSongsSavedByArtistAsc(): Flow<List<Album>>

    @Query("SELECT * FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY A.authorsText COLLATE NOCASE DESC")
    fun albumsWithSongsSavedByArtistDesc(): Flow<List<Album>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT *, (SELECT COUNT(*) FROM SongAlbumMap WHERE albumId = A.id) as songCount  FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY songCount ASC")
    fun albumsWithSongsSavedBySongsCountAsc(): Flow<List<Album>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT *, (SELECT COUNT(*) FROM SongAlbumMap WHERE albumId = A.id) as songCount  FROM Album A" +
            " WHERE A.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            " ORDER BY songCount DESC")
    fun albumsWithSongsSavedBySongsCountDesc(): Flow<List<Album>>

    @Query("SELECT *, (SELECT SUM(CAST(REPLACE(durationText, ':', '') AS INTEGER)) FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = Album.id AND position IS NOT NULL) as totalDuration " +
            "FROM Album WHERE Album.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            "ORDER BY totalDuration ASC" )
    @RewriteQueriesToDropUnusedColumns
    fun albumsWithSongsSavedByTotalDurationAsc(): Flow<List<Album>>

    @Query("SELECT *, (SELECT SUM(CAST(REPLACE(durationText, ':', '') AS INTEGER)) FROM Song JOIN SongAlbumMap ON Song.id = SongAlbumMap.songId WHERE SongAlbumMap.albumId = Album.id AND position IS NOT NULL) as totalDuration " +
            "FROM Album WHERE Album.id in ( SELECT DISTINCT albumId FROM SongAlbumMap ) " +
            "ORDER BY totalDuration DESC" )
    @RewriteQueriesToDropUnusedColumns
    fun albumsWithSongsSavedByTotalDurationDesc(): Flow<List<Album>>

    fun albumsWithSongsSaved(sortBy: AlbumSortBy, sortOrder: SortOrder): Flow<List<Album>> {
        return when (sortBy) {
            AlbumSortBy.Title -> when (sortOrder) {
                SortOrder.Ascending -> albumsWithSongsSavedByTitleAsc()
                SortOrder.Descending -> albumsWithSongsSavedByTitleDesc()
            }
            AlbumSortBy.Year -> when (sortOrder) {
                SortOrder.Ascending -> albumsWithSongsSavedByYearAsc()
                SortOrder.Descending -> albumsWithSongsSavedByYearDesc()
            }
            AlbumSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> albumsWithSongsSavedByRowIdAsc()
                SortOrder.Descending -> albumsWithSongsSavedByRowIdDesc()
            }
            AlbumSortBy.Artist -> when (sortOrder) {
                SortOrder.Ascending -> albumsWithSongsSavedByArtistAsc()
                SortOrder.Descending -> albumsWithSongsSavedByArtistDesc()
            }
            AlbumSortBy.Songs -> when (sortOrder) {
                SortOrder.Ascending -> albumsWithSongsSavedBySongsCountAsc()
                SortOrder.Descending -> albumsWithSongsSavedBySongsCountDesc()
            }
            AlbumSortBy.Duration -> when (sortOrder) {
                SortOrder.Ascending -> albumsWithSongsSavedByTotalDurationAsc()
                SortOrder.Descending -> albumsWithSongsSavedByTotalDurationDesc()
            }
        }
    }

    @Query("UPDATE Song SET totalPlayTimeMs = 0 WHERE id = :id")
    fun resetTotalPlayTimeMs(id: String)

    @Query("UPDATE Song SET totalPlayTimeMs = totalPlayTimeMs + :addition WHERE id = :id")
    fun incrementTotalPlayTimeMs(id: String, addition: Long)

    @Transaction
    @Query("SELECT max(position) maxPos FROM SongPlaylistMap WHERE playlistId = :id")
    fun getSongMaxPositionToPlaylist(id: Long): Int

    @Transaction
    @Query("SELECT PM.playlistId FROM SongPlaylistMap PM WHERE PM.songId = :id")
    fun getPlaylistsWithSong(id: String): Flow<List<Long>>

    @Transaction
    @Query("SELECT max(position) maxPos FROM SongPlaylistMap WHERE playlistId = :id")
    fun updateSongMaxPositionToPlaylist(id: Long): Int

    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :id")
    fun playlistWithSongs(id: Long): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT * FROM Playlist WHERE browseId = :browseId")
    fun playlistWithBrowseId(browseId: String): Playlist?

    @Transaction
    @Query("SELECT * FROM Playlist WHERE trim(name) COLLATE NOCASE = trim(:name) COLLATE NOCASE")
    fun playlistWithSongsNoFlow(name: String): PlaylistWithSongs?

    @Transaction
    @Query("SELECT * FROM Playlist WHERE trim(name) COLLATE NOCASE = trim(:name) COLLATE NOCASE")
    fun playlistWithSongs(name: String): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT * FROM Playlist WHERE browseId = :browseId")
    fun playlistWithSongsByBrowseId(browseId: String): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT * FROM Playlist WHERE name LIKE '${MONTHLY_PREFIX}' || :name || '%'  ")
    fun monthlyPlaylists(name: String?): Flow<List<PlaylistWithSongs?>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist WHERE name LIKE '${MONTHLY_PREFIX}' || :name || '%'  ")
    fun monthlyPlaylistsPreview(name: String?): Flow<List<PlaylistPreview>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        """
        SELECT * FROM SortedSongPlaylistMap SPLM
        INNER JOIN Song on Song.id = SPLM.songId
        WHERE playlistId = :id
        ORDER BY SPLM.position
        """
    )
    fun playlistSongs(id: Long): Flow<List<Song>?>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        """
        SELECT * FROM SortedSongPlaylistMap SPLM
        INNER JOIN Song on Song.id = SPLM.songId
        WHERE playlistId = :id
        ORDER BY SPLM.position
        """
    )
    fun songsInPlaylist(id: Long): Flow<List<SongEntity>?>

    @Transaction
    @Query("SELECT DISTINCT S.* FROM Song S INNER JOIN SongAlbumMap SM ON S.id=SM.songId " +
            "INNER JOIN Album A ON A.id=SM.albumId WHERE A.bookmarkedAt IS NOT NULL")
    fun songsInAllBookmarkedAlbums(): Flow<List<Song>>

    @Transaction
    @Query("SELECT DISTINCT S.* FROM Song S INNER JOIN SongArtistMap SM ON S.id=SM.songId " +
            "INNER JOIN Artist A ON A.id=SM.artistId WHERE A.bookmarkedAt IS NOT NULL")
    fun songsInAllFollowedArtists(): Flow<List<Song>>

    @Transaction
    @Query("SELECT DISTINCT S.* FROM Song S INNER JOIN songplaylistmap SM ON S.id=SM.songId")
    fun songsInAllPlaylists(): Flow<List<Song>>

    @Transaction
    @Query("SELECT DISTINCT S.* FROM Song S INNER JOIN songplaylistmap SM ON S.id=SM.songId " +
            "INNER JOIN Playlist P ON P.id=SM.playlistId WHERE P.isYoutubePlaylist = 1")
    fun songsInAllYTPrivatePlaylists(): Flow<List<Song>>

    @Transaction
    @Query("SELECT DISTINCT S.* FROM Song S INNER JOIN songplaylistmap SM ON S.id=SM.songId " +
            "INNER JOIN Playlist P ON P.id=SM.playlistId WHERE P.name LIKE '${PIPED_PREFIX}' || '%'")
    fun songsInAllPipedPlaylists(): Flow<List<Song>>

    @Transaction
    @Query("SELECT DISTINCT S.* FROM Song S INNER JOIN songplaylistmap SM ON S.id=SM.songId " +
            "INNER JOIN Playlist P ON P.id=SM.playlistId WHERE P.name LIKE '${PINNED_PREFIX}' || '%'")
    fun songsInAllPinnedPlaylists(): Flow<List<Song>>

    @Transaction
    @Query("SELECT DISTINCT S.* FROM Song S INNER JOIN songplaylistmap SM ON S.id=SM.songId " +
            "INNER JOIN Playlist P ON P.id=SM.playlistId WHERE P.name LIKE '${MONTHLY_PREFIX}' || '%'")
    fun songsInAllMonthlyPlaylists(): Flow<List<Song>>

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY S.artistsText COLLATE NOCASE
    """)
    fun sortSongsFromPlaylistByArtist( id: Long ): Flow<List<SongEntity>>

    /**
     * Fetch all records from data that have playlist id matches [id]
     * and sort them by their titles.
     *
     * [EXPLICIT_PREFIX] is removed during the sort process to make
     * this sorting more accurate
     */
    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY 
            CASE
                WHEN S.title LIKE "$EXPLICIT_PREFIX%" THEN SUBSTR(S.title, LENGTH('$EXPLICIT_PREFIX') + 1)
                ELSE S.title
            END
        COLLATE NOCASE
    """)
    fun sortSongsFromPlaylistByTitle( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY SP.position
    """)
    fun sortSongsPlaylistByPosition( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY SP.position
    """)
    fun sortSongsPlaylistByPositionNoFlow( id: Long ): List<SongEntity>

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY S.totalPlayTimeMs
    """)
    fun sortSongsFromPlaylistByPlaytime( id: Long ): Flow<List<SongEntity>>

    fun sortSongsFromPlaylistByRelativePlaytime( id: Long ): Flow<List<SongEntity>> {
        val songs = sortSongsFromPlaylistByPlaytime(id)
        songs.map { it }
        return songs.map {
            it.sortedBy { se ->
                se.relativePlayTime()
            }
        }
    }

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN Event E ON E.songId=S.id 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY E.timestamp
    """)
    fun sortSongsFromPlaylistByDatePlayed( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, A.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN songalbummap SA ON SA.songId=SP.songId 
        LEFT JOIN Album A ON A.Id=SA.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY CAST(A.year AS INTEGER)
    """)
    fun sortSongsFromPlaylistByYear( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY S.durationText
    """)
    fun sortSongsFromPlaylistByDuration( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, A.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN songalbummap SA ON SA.songId=SP.songId 
        LEFT JOIN Album A ON A.Id=SA.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY S.artistsText COLLATE NOCASE, A.title COLLATE NOCASE
    """)
    fun sortSongsFromPlaylistByArtistAndAlbum( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, A.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN songalbummap SA ON SA.songId=SP.songId 
        LEFT JOIN Album A ON A.Id=SA.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY A.title COLLATE NOCASE
    """)
    fun sortSongsFromPlaylistByAlbum( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY S.ROWID
    """)
    fun sortSongsFromPlaylistByRowId( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN SongPlaylistMap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY SP.dateAdded
    """)
    fun sortSongsFromPlaylistByDateAdded( id: Long ): Flow<List<SongEntity>>

    @Query("""
        SELECT DISTINCT S.*, Album.title as albumTitle, Format.contentLength as contentLength
        FROM Song S 
        INNER JOIN songplaylistmap SP ON S.id = SP.songId 
        LEFT JOIN SongAlbumMap ON SongAlbumMap.songId = S.id 
        LEFT JOIN Album ON Album.id = SongAlbumMap.albumId 
        LEFT JOIN Format ON Format.songId = S.id
        WHERE SP.playlistId = :id 
        ORDER BY S.LikedAt COLLATE NOCASE
    """)
    fun sortSongsFromPlaylistByLikedAt( id: Long ): Flow<List<SongEntity>>

    fun songsPlaylist(id: Long, sortBy: PlaylistSongSortBy, sortOrder: SortOrder): Flow<List<SongEntity>> =
        when( sortBy ) {
            PlaylistSongSortBy.Album -> sortSongsFromPlaylistByAlbum( id )
            PlaylistSongSortBy.AlbumYear -> sortSongsFromPlaylistByYear( id )
            PlaylistSongSortBy.Artist -> sortSongsFromPlaylistByArtist( id )
            PlaylistSongSortBy.ArtistAndAlbum -> sortSongsFromPlaylistByArtistAndAlbum( id )
            PlaylistSongSortBy.DatePlayed -> sortSongsFromPlaylistByDatePlayed( id )
            PlaylistSongSortBy.PlayTime -> sortSongsFromPlaylistByPlaytime( id )
            PlaylistSongSortBy.RelativePlayTime -> sortSongsFromPlaylistByRelativePlaytime( id )
            PlaylistSongSortBy.Position -> sortSongsPlaylistByPosition( id )
            PlaylistSongSortBy.Title -> sortSongsFromPlaylistByTitle( id )
            PlaylistSongSortBy.Duration -> sortSongsFromPlaylistByDuration( id )
            PlaylistSongSortBy.DateLiked -> sortSongsFromPlaylistByLikedAt( id )
            PlaylistSongSortBy.DateAdded -> sortSongsFromPlaylistByDateAdded( id )
        }.map {
            it.run {
                if( sortOrder == SortOrder.Descending )
                    reversed()
                else
                    this
            }
        }

    @Transaction
    @Query("SELECT S.* FROM Song S INNER JOIN songplaylistmap SP ON S.id=SP.songId WHERE SP.playlistId=:id ORDER BY SP.position LIMIT 4")
    fun songsPlaylistTop4Positions(id: Long): Flow<List<Song>>

    @Transaction
    @Query("SELECT SP.position FROM Song S INNER JOIN songplaylistmap SP ON S.id=SP.songId WHERE SP.playlistId=:id AND S.id NOT LIKE '$LOCAL_KEY_PREFIX%' ORDER BY SP.position")
    fun songsPlaylistMap(id: Long): Flow<List<Int>>

    @Transaction
    @Query("SELECT id FROM SONG WHERE likedAt IS NOT NULL AND likedAt < 0")
    fun dislikedSongsById(): Flow<List<String>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT * FROM Playlist WHERE browseId = :browseId")
    fun playlist(browseId: String): Flow<Playlist?>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist WHERE id=:id")
    fun singlePlaylistPreview(id: Long): Flow<PlaylistPreview?>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist WHERE name LIKE '${PINNED_PREFIX}%' ORDER BY name COLLATE NOCASE ASC")
    fun playlistPinnedPreviewsByNameAsc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY name COLLATE NOCASE ASC")
    fun playlistPreviewsByNameAsc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY ROWID ASC")
    fun playlistPreviewsByDateAddedAsc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY songCount ASC")
    fun playlistPreviewsByDateSongCountAsc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY name COLLATE NOCASE DESC")
    fun playlistPreviewsByNameDesc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY ROWID DESC")
    fun playlistPreviewsByDateAddedDesc(): Flow<List<PlaylistPreview>>
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount FROM Playlist ORDER BY songCount DESC")
    fun playlistPreviewsByDateSongCountDesc(): Flow<List<PlaylistPreview>>
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, " +
            "(SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount, " +
            "(SELECT SUM(Song.totalPlayTimeMs) FROM Song " +
            "JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE SongPlaylistMap.playlistId = Playlist.id ) as TotPlayTime " +
            "FROM Playlist " +
            "ORDER BY 6")
    fun playlistPreviewsByMostPlayedSongsAsc(): Flow<List<PlaylistPreview>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT *, " +
            "(SELECT COUNT(*) FROM SongPlaylistMap WHERE playlistId = id) as songCount, " +
            "(SELECT SUM(Song.totalPlayTimeMs) FROM Song " +
            "JOIN SongPlaylistMap ON Song.id = SongPlaylistMap.songId " +
            "WHERE SongPlaylistMap.playlistId = Playlist.id ) as TotPlayTime " +
            "FROM Playlist " +
            "ORDER BY 6 DESC")
    fun playlistPreviewsByMostPlayedSongsDesc(): Flow<List<PlaylistPreview>>

    fun playlistPreviews(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ): Flow<List<PlaylistPreview>> {
        return when (sortBy) {
            PlaylistSortBy.Name -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByNameAsc()
                SortOrder.Descending -> playlistPreviewsByNameDesc()
            }
            PlaylistSortBy.SongCount -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByDateSongCountAsc()
                SortOrder.Descending -> playlistPreviewsByDateSongCountDesc()
            }
            PlaylistSortBy.DateAdded -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByDateAddedAsc()
                SortOrder.Descending -> playlistPreviewsByDateAddedDesc()
            }
            PlaylistSortBy.MostPlayed -> when (sortOrder) {
                SortOrder.Ascending -> playlistPreviewsByMostPlayedSongsAsc()
                SortOrder.Descending -> playlistPreviewsByMostPlayedSongsDesc()
            }
        }
    }

    @Query("SELECT thumbnailUrl FROM Song JOIN SongPlaylistMap ON id = songId WHERE playlistId = :id AND thumbnailUrl <>'' ORDER BY position LIMIT 4")
    fun playlistThumbnailUrls(id: Long): Flow<List<String?>>



    @Transaction
    @Query("SELECT * FROM Song JOIN SongArtistMap ON Song.id = SongArtistMap.songId WHERE SongArtistMap.artistId = :artistId AND totalPlayTimeMs > 0 ORDER BY Song.ROWID DESC")
    @RewriteQueriesToDropUnusedColumns
    fun artistSongs(artistId: String): Flow<List<Song>>

    @Query("SELECT * FROM Format WHERE songId = :songId")
    fun format(songId: String): Flow<Format?>

    @Query("SELECT contentLength FROM Format WHERE songId = :songId")
    fun formatContentLength(songId: String): Long

    @Transaction
    @Query("UPDATE Format SET contentLength = 0 WHERE songId = :songId")
    fun resetFormatContentLength(songId: String)

    @Transaction
    @Query("DELETE FROM Song WHERE totalPlayTimeMs = 0")
    fun deleteHiddenSongs()
    // USEFUL FOR MAINTENANCE
    @Transaction
    @Query("DELETE FROM SongArtistMap WHERE songId NOT IN (SELECT DISTINCT id FROM Song)")
    fun cleanSongArtistMap()
    @Transaction
    @Query("DELETE FROM SongPlaylistMap WHERE songId NOT IN (SELECT DISTINCT id FROM Song)")
    fun cleanSongPlaylistMap()
    @Transaction
    @Query("DELETE FROM SongAlbumMap WHERE songId NOT IN (SELECT DISTINCT id FROM Song)")
    fun cleanSongAlbumMap()
    @Transaction
    @Query("DELETE FROM Format WHERE songId = :songId")
    fun deleteFormat(songId: String)

    @Transaction
    @Query("SELECT Song.*, contentLength FROM Song JOIN Format ON id = songId WHERE contentLength IS NOT NULL AND totalPlayTimeMs > 0 ORDER BY Song.ROWID DESC")
    fun songsWithContentLength(): Flow<List<SongWithContentLength>>

    @Transaction
    @Query("""
        UPDATE SongPlaylistMap SET position = 
          CASE 
            WHEN position < :fromPosition THEN position + 1
            WHEN position > :fromPosition THEN position - 1
            ELSE :toPosition
          END 
        WHERE playlistId = :playlistId AND position BETWEEN MIN(:fromPosition,:toPosition) and MAX(:fromPosition,:toPosition)
    """)
    fun move(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Transaction
    @Query("UPDATE SongPlaylistMap SET position = :toPosition WHERE playlistId = :playlistId and songId = :songId")
    fun updateSongPosition(playlistId: Long, songId: String, toPosition: Int)

    @Query("DELETE FROM SongPlaylistMap WHERE playlistId = :id")
    fun clearPlaylist(id: Long)

    @Query("DELETE FROM SongPlaylistMap WHERE songId = :id")
    fun deleteSongFromPlaylists(id: String)

    @Query("DELETE FROM SongPlaylistMap WHERE songId = :id and playlistId = :playlistId")
    fun deleteSongFromPlaylist(id: String, playlistId: Long)

    @Query("SELECT setVideoId FROM SongPlaylistMap WHERE songId = :id and playlistId = :playlistId")
    fun getSetVideoIdFromPlaylist(id: String, playlistId: Long): Flow<String?>

    @Query("DELETE FROM SongAlbumMap WHERE albumId = :id")
    fun clearAlbum(id: String)

    @Query("SELECT loudnessDb FROM Format WHERE songId = :songId")
    fun loudnessDb(songId: String): Flow<Float?>

    @Query("SELECT * FROM Song WHERE title LIKE :query OR artistsText LIKE :query")
    fun search(query: String): Flow<List<Song>>

    @Query("SELECT albumId AS id, Album.title AS name, 0 AS size FROM SongAlbumMap LEFT JOIN Album ON id=albumId WHERE songId = :songId")
    fun songAlbumInfo(songId: String): Info?

    @Query("SELECT thumbnailUrl FROM Song LEFT JOIN SongAlbumMap ON id=songId WHERE albumId = :albumId")
    fun albumThumbnailFromSong(albumId: String): String?

    @Query("SELECT id, name, 0 AS size FROM Artist LEFT JOIN SongArtistMap ON id = artistId WHERE songId = :songId")
    fun songArtistInfo(songId: String): List<Info>

    /*
        @Transaction
        @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId GROUP BY songId ORDER BY SUM(CAST(playTime AS REAL) / (((:now - timestamp) / 86400000) + 1)) DESC LIMIT 1")
    //    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId GROUP BY songId ORDER BY timestamp DESC LIMIT 1")
        @RewriteQueriesToDropUnusedColumns
        fun trending(now: Long = System.currentTimeMillis()): Flow<Song?>
    //    fun trending(): Flow<Song?>
     */

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY SUM(CAST(playTime AS REAL) / (((:now - timestamp) / 86400000) + 1)) DESC LIMIT 1")
    @RewriteQueriesToDropUnusedColumns
    fun trendingReal(now: Long = System.currentTimeMillis()): Flow<List<Song>>

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY SUM(playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun trending(limit: Int = 3): Flow<List<Song>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY SUM(playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun trendingSongEntity(limit: Int = 3): Flow<List<SongEntity>>

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE (:now - Event.timestamp) <= :period AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY SUM(playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun trending(
        limit: Int = 3,
        now: Long = System.currentTimeMillis(),
        period: Long
    ): Flow<List<Song>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE (:now - Event.timestamp) <= :period AND Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY SUM(playTime) DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun trendingSongEntity(
        limit: Int = 3,
        now: Long = System.currentTimeMillis(),
        period: Long
    ): Flow<List<SongEntity>>

    @Transaction
    @Query("SELECT Song.* FROM Event JOIN Song ON Song.id = songId WHERE playTime > 0 and Song.id NOT LIKE '$LOCAL_KEY_PREFIX%' GROUP BY songId ORDER BY timestamp DESC LIMIT :limit")
    @RewriteQueriesToDropUnusedColumns
    fun lastPlayed( limit: Int = 10 ): Flow<List<Song>>

    @Query("SELECT COUNT (*) FROM Event")
    fun eventsCount(): Flow<Int>

    @Query("DELETE FROM Event")
    fun clearEvents()

    @Query("DELETE FROM Event WHERE songId = :songId")
    fun clearEventsFor(songId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Throws(SQLException::class)
    fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(format: Format)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(artist: Artist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(album: Album)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchQuery: SearchQuery)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(songPlaylistMap: SongPlaylistMap): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(songArtistMap: SongArtistMap): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(queuedMediaItems: List<QueuedMediaItem>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSongPlaylistMaps(songPlaylistMaps: List<SongPlaylistMap>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: Album, songAlbumMap: SongAlbumMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artists: List<Artist>, songArtistMaps: List<SongArtistMap>)

    @Transaction
    fun insert(mediaItem: MediaItem, block: (Song) -> Song = { it }) {
        var title = mediaItem.mediaMetadata.title!!.toString()
        if(!title.startsWith(EXPLICIT_PREFIX, true) && mediaItem.isExplicit){
            title = EXPLICIT_PREFIX + title
        }
        val song = Song(
            id = mediaItem.mediaId,
            title = title,
            artistsText = mediaItem.mediaMetadata.artist?.toString(),
            durationText = mediaItem.mediaMetadata.extras?.getString("durationText"),
            thumbnailUrl = mediaItem.mediaMetadata.artworkUri?.toString()
        ).let(block).also { song ->
            if (insert(song) == -1L) return
        }

        mediaItem.mediaMetadata.extras?.getString("albumId")?.let { albumId ->
            insert(
                Album(id = albumId, title = mediaItem.mediaMetadata.albumTitle?.toString()),
                SongAlbumMap(songId = song.id, albumId = albumId, position = null)
            )
        }

        mediaItem.mediaMetadata.extras?.getStringArrayList("artistNames")?.let { artistNames ->
            mediaItem.mediaMetadata.extras?.getStringArrayList("artistIds")?.let { artistIds ->
                if (artistNames.size == artistIds.size) {
                    insert(
                        artistNames.mapIndexed { index, artistName ->
                            Artist(id = artistIds[index], name = artistName)
                        },
                        artistIds.map { artistId ->
                            SongArtistMap(songId = song.id, artistId = artistId)
                        }
                    )
                }
            }
        }
    }

    @Update
    fun update(artist: Artist)

    @Update
    fun update(album: Album)

    @Update
    fun update(playlist: Playlist)

    @Update
    fun update(playlist: Playlist, playlistItem: Environment.PlaylistItem) {
        update(playlist.copy(
            name = playlistItem.title ?: "",
            browseId = playlistItem.key
        ))
    }

    @Upsert
    fun upsert(lyrics: Lyrics)

    @Upsert
    fun upsert(album: Album, songAlbumMaps: List<SongAlbumMap>)

    @Upsert
    fun upsert(songAlbumMap: SongAlbumMap)

    @Upsert
    fun upsert(artist: Artist)

    @Upsert
    fun upsert(format: Format)

    @Upsert
    fun upsert(song: Song)

    @Delete
    fun delete(searchQuery: SearchQuery)

    @Delete
    fun delete(playlist: Playlist)

    @Delete
    fun delete(songPlaylistMap: SongPlaylistMap)

    @Delete
    fun delete(song: Song)

    @Delete
    fun delete(album: Album)

    @Delete
    fun delete(artist: Artist)

    /**
     * Reset [Format.contentLength] of provided song.
     *
     * This method is already wrapped by [Transaction] call,
     * therefore, it's unnecessary to wrap it with a coroutine
     * or other transaction call.
     *
     * To use it inside another [Transaction] wrapper, please
     * refer to [resetFormatContentLength].
     *
     * @param songId id of song to have its [Format.contentLength] reset
     */
    @Transaction
    fun resetContentLength( songId: String ) = asyncTransaction {
        resetFormatContentLength( songId )
    }

    /**
     * Commit statements in BULK. If anything goes wrong during the transaction,
     * other statements will be cancelled and reversed to preserve database's integrity.
     * [Read more](https://sqlite.org/lang_transaction.html)
     *
     * [asyncTransaction] runs all statements on non-blocking
     * thread to prevent UI from going unresponsive.
     *
     * ## Best use cases:
     * - Commit multiple write statements that require data integrity
     * - Processes that take longer time to complete
     *
     * > Do NOT use this to retrieve data from the database.
     * > Use [asyncQuery] to retrieve records.
     *
     * @param block of statements to write to database
     */
    fun asyncTransaction( block: Database.() -> Unit ) =
        _internal.transactionExecutor.execute {
            this.block()
        }


    /**
     * Access and retrieve records from database.
     *
     * [asyncQuery] runs all statements asynchronously to
     * prevent blocking UI thread from going unresponsive.
     *
     * ## Best use cases:
     * - Background data retrieval
     * - Non-immediate UI component update (i.e. count number of songs)
     *
     * > Do NOT use this method to write data to database
     * > because it offers no fail-safe during write.
     * > Use [asyncTransaction] to modify database.
     *
     * @param block of statements to retrieve data from database
     */
    fun asyncQuery( block: Database.() -> Unit ) =
        _internal.queryExecutor.execute {
            this.block()
        }

    @RawQuery
    fun raw(supportSQLiteQuery: SupportSQLiteQuery): Int

    fun checkpoint() {
        raw(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
    }

    fun path() = _internal.openHelper.writableDatabase.path

    fun close() = _internal.close()
}

@androidx.room.Database(
    entities = [
        Song::class,
        SongPlaylistMap::class,
        Playlist::class,
        Artist::class,
        SongArtistMap::class,
        Album::class,
        SongAlbumMap::class,
        SearchQuery::class,
        QueuedMediaItem::class,
        Format::class,
        Event::class,
        Lyrics::class,
    ],
    views = [
        SortedSongPlaylistMap::class
    ],
    version = 27,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = DatabaseInitializer.From3To4Migration::class),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8, spec = DatabaseInitializer.From7To8Migration::class),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 11, to = 12, spec = DatabaseInitializer.From11To12Migration::class),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 17, to = 18),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21, spec = DatabaseInitializer.From20To21Migration::class),
        AutoMigration(from = 21, to = 22, spec = DatabaseInitializer.From21To22Migration::class),
    ],
)
@TypeConverters(Converters::class)
abstract class DatabaseInitializer protected constructor() : RoomDatabase() {
    abstract val database: Database

    companion object {

        lateinit var Instance: DatabaseInitializer

        private fun getDatabase() = Room
            .databaseBuilder(appContext(), DatabaseInitializer::class.java, "data.db")
            .addMigrations(
                From8To9Migration(),
                From10To11Migration(),
                From14To15Migration(),
                From22To23Migration(),
                From23To24Migration(),
                From24To25Migration(),
                From25To26Migration(),
                From26To27Migration()
            )
            .build()


        operator fun invoke() {
            if (!::Instance.isInitialized) reload()
        }

        fun reload() = synchronized(this) {
            Instance = getDatabase()
        }
    }

    @DeleteTable.Entries(DeleteTable(tableName = "QueuedMediaItem"))
    class From3To4Migration : AutoMigrationSpec

    @RenameColumn.Entries(RenameColumn("Song", "albumInfoId", "albumId"))
    class From7To8Migration : AutoMigrationSpec

    class From8To9Migration : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.query(SimpleSQLiteQuery("SELECT DISTINCT browseId, text, Info.id FROM Info JOIN Song ON Info.id = Song.albumId;"))
                .use { cursor ->
                    val albumValues = ContentValues(2)
                    while (cursor.moveToNext()) {
                        albumValues.put("id", cursor.getString(0))
                        albumValues.put("title", cursor.getString(1))
                        db.insert("Album", CONFLICT_IGNORE, albumValues)

                        db.execSQL(
                            "UPDATE Song SET albumId = '${cursor.getString(0)}' WHERE albumId = ${
                                cursor.getLong(
                                    2
                                )
                            }"
                        )
                    }
                }

            db.query(SimpleSQLiteQuery("SELECT GROUP_CONCAT(text, ''), SongWithAuthors.songId FROM Info JOIN SongWithAuthors ON Info.id = SongWithAuthors.authorInfoId GROUP BY songId;"))
                .use { cursor ->
                    val songValues = ContentValues(1)
                    while (cursor.moveToNext()) {
                        songValues.put("artistsText", cursor.getString(0))
                        db.update(
                            "Song",
                            CONFLICT_IGNORE,
                            songValues,
                            "id = ?",
                            arrayOf(cursor.getString(1))
                        )
                    }
                }

            db.query(SimpleSQLiteQuery("SELECT browseId, text, Info.id FROM Info JOIN SongWithAuthors ON Info.id = SongWithAuthors.authorInfoId WHERE browseId NOT NULL;"))
                .use { cursor ->
                    val artistValues = ContentValues(2)
                    while (cursor.moveToNext()) {
                        artistValues.put("id", cursor.getString(0))
                        artistValues.put("name", cursor.getString(1))
                        db.insert("Artist", CONFLICT_IGNORE, artistValues)

                        db.execSQL(
                            "UPDATE SongWithAuthors SET authorInfoId = '${cursor.getString(0)}' WHERE authorInfoId = ${
                                cursor.getLong(
                                    2
                                )
                            }"
                        )
                    }
                }

            db.execSQL("INSERT INTO SongArtistMap(songId, artistId) SELECT songId, authorInfoId FROM SongWithAuthors")

            db.execSQL("DROP TABLE Info;")
            db.execSQL("DROP TABLE SongWithAuthors;")
        }
    }

    class From10To11Migration : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.query(SimpleSQLiteQuery("SELECT id, albumId FROM Song;")).use { cursor ->
                val songAlbumMapValues = ContentValues(2)
                while (cursor.moveToNext()) {
                    songAlbumMapValues.put("songId", cursor.getString(0))
                    songAlbumMapValues.put("albumId", cursor.getString(1))
                    db.insert("SongAlbumMap", CONFLICT_IGNORE, songAlbumMapValues)
                }
            }

            db.execSQL("CREATE TABLE IF NOT EXISTS `Song_new` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artistsText` TEXT, `durationText` TEXT NOT NULL, `thumbnailUrl` TEXT, `lyrics` TEXT, `likedAt` INTEGER, `totalPlayTimeMs` INTEGER NOT NULL, `loudnessDb` REAL, `contentLength` INTEGER, PRIMARY KEY(`id`))")

            db.execSQL("INSERT INTO Song_new(id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs, loudnessDb, contentLength) SELECT id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs, loudnessDb, contentLength FROM Song;")
            db.execSQL("DROP TABLE Song;")
            db.execSQL("ALTER TABLE Song_new RENAME TO Song;")
        }
    }

    @RenameTable("SongInPlaylist", "SongPlaylistMap")
    @RenameTable("SortedSongInPlaylist", "SortedSongPlaylistMap")
    class From11To12Migration : AutoMigrationSpec

    class From14To15Migration : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.query(SimpleSQLiteQuery("SELECT id, loudnessDb, contentLength FROM Song;"))
                .use { cursor ->
                    val formatValues = ContentValues(3)
                    while (cursor.moveToNext()) {
                        formatValues.put("songId", cursor.getString(0))
                        formatValues.put("loudnessDb", cursor.getFloatOrNull(1))
                        formatValues.put("contentLength", cursor.getFloatOrNull(2))
                        db.insert("Format", CONFLICT_IGNORE, formatValues)
                    }
                }

            db.execSQL("CREATE TABLE IF NOT EXISTS `Song_new` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artistsText` TEXT, `durationText` TEXT NOT NULL, `thumbnailUrl` TEXT, `lyrics` TEXT, `likedAt` INTEGER, `totalPlayTimeMs` INTEGER NOT NULL, PRIMARY KEY(`id`))")

            db.execSQL("INSERT INTO Song_new(id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs) SELECT id, title, artistsText, durationText, thumbnailUrl, lyrics, likedAt, totalPlayTimeMs FROM Song;")
            db.execSQL("DROP TABLE Song;")
            db.execSQL("ALTER TABLE Song_new RENAME TO Song;")
        }
    }

    @DeleteColumn.Entries(
        DeleteColumn("Artist", "shuffleVideoId"),
        DeleteColumn("Artist", "shufflePlaylistId"),
        DeleteColumn("Artist", "radioVideoId"),
        DeleteColumn("Artist", "radioPlaylistId"),
    )
    class From20To21Migration : AutoMigrationSpec

    @DeleteColumn.Entries(DeleteColumn("Artist", "info"))
    class From21To22Migration : AutoMigrationSpec

    class From22To23Migration : Migration(22, 23) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS Lyrics (`songId` TEXT NOT NULL, `fixed` TEXT, `synced` TEXT, PRIMARY KEY(`songId`), FOREIGN KEY(`songId`) REFERENCES `Song`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")

            db.query(SimpleSQLiteQuery("SELECT id, lyrics, synchronizedLyrics FROM Song;")).use { cursor ->
                val lyricsValues = ContentValues(3)
                while (cursor.moveToNext()) {
                    lyricsValues.put("songId", cursor.getString(0))
                    lyricsValues.put("fixed", cursor.getString(1))
                    lyricsValues.put("synced", cursor.getString(2))
                    db.insert("Lyrics", CONFLICT_IGNORE, lyricsValues)
                }
            }

            db.execSQL("CREATE TABLE IF NOT EXISTS Song_new (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artistsText` TEXT, `durationText` TEXT, `thumbnailUrl` TEXT, `likedAt` INTEGER, `totalPlayTimeMs` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("INSERT INTO Song_new(id, title, artistsText, durationText, thumbnailUrl, likedAt, totalPlayTimeMs) SELECT id, title, artistsText, durationText, thumbnailUrl, likedAt, totalPlayTimeMs FROM Song;")
            db.execSQL("DROP TABLE Song;")
            db.execSQL("ALTER TABLE Song_new RENAME TO Song;")
        }
    }


    class From23To24Migration : Migration(23, 24) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE SongPlaylistMap ADD COLUMN setVideoId TEXT;")
            } catch (e: Exception) {
                println("Database From23To24Migration error ${e.stackTraceToString()}")
            }

        }
    }

    class From24To25Migration : Migration(24, 25) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE Playlist ADD COLUMN isEditable INTEGER NOT NULL DEFAULT 0;")
            } catch (e: Exception) {
                println("Database From24To25Migration error ${e.stackTraceToString()}")
            }

        }
    }

    class From25To26Migration : Migration(25, 26) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE Playlist ADD COLUMN isYoutubePlaylist INTEGER NOT NULL DEFAULT 0;")
            } catch (e: Exception) {
                println("Database From25To26Migration error ${e.stackTraceToString()}")
            }

        }
    }
    class From26To27Migration : Migration(26, 27) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("ALTER TABLE Album ADD COLUMN isYoutubeAlbum INTEGER NOT NULL DEFAULT 0;")
            } catch (e: Exception) {
                println("Database From26To27Migration error ${e.stackTraceToString()}")
            }
            try {
                db.execSQL("ALTER TABLE Artist ADD COLUMN isYoutubeArtist INTEGER NOT NULL DEFAULT 0;")
            } catch (e: Exception) {
                println("Database From26To27Migration error ${e.stackTraceToString()}")
            }
            try {
                db.execSQL("ALTER TABLE SongPlaylistMap ADD COLUMN dateAdded INTEGER NULL;")
            } catch (e: Exception) {
                println("Database From26To27Migration error ${e.stackTraceToString()}")
            }

        }
    }
}


@TypeConverters
object Converters {

    @TypeConverter
    @JvmStatic
    fun fromString(stringListString: String): List<String> {
        return stringListString.split(",").map { it }
    }

    @TypeConverter
    @JvmStatic
    fun toString(stringList: List<String>): String {
        return stringList.joinToString(separator = ",")
    }

    @TypeConverter
    @JvmStatic
    @UnstableApi
    fun mediaItemFromByteArray(value: ByteArray?): MediaItem? {
        return value?.let { byteArray ->
            runCatching {
                val parcel = Parcel.obtain()
                parcel.unmarshall(byteArray, 0, byteArray.size)
                parcel.setDataPosition(0)
                val bundle = parcel.readBundle(MediaItem::class.java.classLoader)
                parcel.recycle()

                bundle?.let(MediaItem::fromBundle)
            }.getOrNull()
        }
    }

    @TypeConverter
    @JvmStatic
    @UnstableApi
    fun mediaItemToByteArray(mediaItem: MediaItem?): ByteArray? {
        return mediaItem?.toBundle()?.let { persistableBundle ->
            val parcel = Parcel.obtain()
            parcel.writeBundle(persistableBundle)
            val bytes = parcel.marshall()
            parcel.recycle()

            bytes
        }
    }
}
