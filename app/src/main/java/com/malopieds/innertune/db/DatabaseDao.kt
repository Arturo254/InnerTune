package com.malopieds.innertune.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import com.malopieds.innertube.models.SongItem
import com.malopieds.innertube.pages.AlbumPage
import com.malopieds.innertube.pages.ArtistPage
import com.malopieds.innertune.constants.AlbumSortType
import com.malopieds.innertune.constants.ArtistSongSortType
import com.malopieds.innertune.constants.ArtistSortType
import com.malopieds.innertune.constants.PlaylistSortType
import com.malopieds.innertune.constants.SongSortType
import com.malopieds.innertune.db.entities.Album
import com.malopieds.innertune.db.entities.AlbumArtistMap
import com.malopieds.innertune.db.entities.AlbumEntity
import com.malopieds.innertune.db.entities.AlbumWithSongs
import com.malopieds.innertune.db.entities.Artist
import com.malopieds.innertune.db.entities.ArtistEntity
import com.malopieds.innertune.db.entities.Event
import com.malopieds.innertune.db.entities.EventWithSong
import com.malopieds.innertune.db.entities.FormatEntity
import com.malopieds.innertune.db.entities.LyricsEntity
import com.malopieds.innertune.db.entities.Playlist
import com.malopieds.innertune.db.entities.PlaylistEntity
import com.malopieds.innertune.db.entities.PlaylistSong
import com.malopieds.innertune.db.entities.PlaylistSongMap
import com.malopieds.innertune.db.entities.RelatedSongMap
import com.malopieds.innertune.db.entities.SearchHistory
import com.malopieds.innertune.db.entities.Song
import com.malopieds.innertune.db.entities.SongAlbumMap
import com.malopieds.innertune.db.entities.SongArtistMap
import com.malopieds.innertune.db.entities.SongEntity
import com.malopieds.innertune.db.entities.SongWithStats
import com.malopieds.innertune.extensions.reversed
import com.malopieds.innertune.extensions.toSQLiteQuery
import com.malopieds.innertune.models.MediaMetadata
import com.malopieds.innertune.models.toMediaMetadata
import com.malopieds.innertune.ui.utils.resize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Collator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import javax.annotation.concurrent.Immutable

@Dao
interface DatabaseDao {
    @Transaction
    @Query("SELECT * FROM song WHERE inLibrary IS NOT NULL ORDER BY rowId")
    fun songsByRowIdAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song WHERE inLibrary IS NOT NULL ORDER BY inLibrary")
    fun songsByCreateDateAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song WHERE inLibrary IS NOT NULL ORDER BY title")
    fun songsByNameAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song WHERE inLibrary IS NOT NULL ORDER BY totalPlayTime")
    fun songsByPlayTimeAsc(): Flow<List<Song>>

    fun songs(
        sortType: SongSortType,
        descending: Boolean,
    ) = when (sortType) {
        SongSortType.CREATE_DATE -> songsByCreateDateAsc()
        SongSortType.NAME ->
            songsByNameAsc().map { songs ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                songs.sortedWith(compareBy(collator) { it.song.title })
            }
        SongSortType.ARTIST ->
            songsByRowIdAsc().map { songs ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                songs
                    .sortedWith(
                        compareBy(collator) { song ->
                            song.artists.joinToString(
                                "",
                            ) { it.name }
                        },
                    ).groupBy { it.album?.title }
                    .flatMap { (_, songsByAlbum) ->
                        songsByAlbum.sortedBy { album ->
                            album.artists.joinToString(
                                "",
                            ) { it.name }
                        }
                    }
            }
        SongSortType.PLAY_TIME -> songsByPlayTimeAsc()
    }.map { it.reversed(descending) }

    @Transaction
    @Query("SELECT * FROM song WHERE liked ORDER BY rowId")
    fun likedSongsByRowIdAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song WHERE liked ORDER BY inLibrary")
    fun likedSongsByCreateDateAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song WHERE liked ORDER BY title")
    fun likedSongsByNameAsc(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song WHERE liked ORDER BY totalPlayTime")
    fun likedSongsByPlayTimeAsc(): Flow<List<Song>>

    fun likedSongs(
        sortType: SongSortType,
        descending: Boolean,
    ) = when (sortType) {
        SongSortType.CREATE_DATE -> likedSongsByCreateDateAsc()
        SongSortType.NAME ->
            likedSongsByNameAsc().map { songs ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                songs.sortedWith(compareBy(collator) { it.song.title })
            }
        SongSortType.ARTIST ->
            likedSongsByRowIdAsc().map { songs ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                songs
                    .sortedWith(
                        compareBy(collator) { song ->
                            song.artists.joinToString(
                                "",
                            ) { it.name }
                        },
                    ).groupBy { it.album?.title }
                    .flatMap { (_, songsByAlbum) ->
                        songsByAlbum.sortedBy { album ->
                            album.artists.joinToString(
                                "",
                            ) { it.name }
                        }
                    }
            }
        SongSortType.PLAY_TIME -> likedSongsByPlayTimeAsc()
    }.map { it.reversed(descending) }

    @Query("SELECT COUNT(1) FROM song WHERE liked")
    fun likedSongsCount(): Flow<Int>

    @Transaction
    @Query("SELECT song.* FROM song JOIN song_album_map ON song.id = song_album_map.songId WHERE song_album_map.albumId = :albumId")
    fun albumSongs(albumId: String): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM playlist_song_map WHERE playlistId = :playlistId ORDER BY position")
    fun playlistSongs(playlistId: String): Flow<List<PlaylistSong>>

    @Transaction
    @Query(
        "SELECT song.* FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = :artistId AND inLibrary IS NOT NULL ORDER BY inLibrary",
    )
    fun artistSongsByCreateDateAsc(artistId: String): Flow<List<Song>>

    @Transaction
    @Query(
        "SELECT song.* FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = :artistId AND inLibrary IS NOT NULL ORDER BY title",
    )
    fun artistSongsByNameAsc(artistId: String): Flow<List<Song>>

    @Transaction
    @Query(
        "SELECT song.* FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = :artistId AND inLibrary IS NOT NULL ORDER BY totalPlayTime",
    )
    fun artistSongsByPlayTimeAsc(artistId: String): Flow<List<Song>>

    fun artistSongs(
        artistId: String,
        sortType: ArtistSongSortType,
        descending: Boolean,
    ) = when (sortType) {
        ArtistSongSortType.CREATE_DATE -> artistSongsByCreateDateAsc(artistId)
        ArtistSongSortType.NAME ->
            artistSongsByNameAsc(artistId).map { artistSongs ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                artistSongs.sortedWith(compareBy(collator) { it.song.title })
            }
        ArtistSongSortType.PLAY_TIME -> artistSongsByPlayTimeAsc(artistId)
    }.map { it.reversed(descending) }

    @Transaction
    @Query(
        "SELECT song.* FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = :artistId AND inLibrary IS NOT NULL LIMIT :previewSize",
    )
    fun artistSongsPreview(
        artistId: String,
        previewSize: Int = 3,
    ): Flow<List<Song>>

    @Transaction
    @Query(
        """
        SELECT song.*
        FROM (SELECT *, COUNT(1) AS referredCount
              FROM related_song_map
              GROUP BY relatedSongId) map
                 JOIN song ON song.id = map.relatedSongId
        WHERE songId IN (SELECT songId
                         FROM (SELECT songId
                               FROM event
                               ORDER BY ROWID DESC
                               LIMIT 5)
                         UNION
                         SELECT songId
                         FROM (SELECT songId
                               FROM event
                               WHERE timestamp > :now - 86400000 * 7
                               GROUP BY songId
                               ORDER BY SUM(playTime) DESC
                               LIMIT 5)
                         UNION
                         SELECT id
                         FROM (SELECT id
                               FROM song
                               ORDER BY totalPlayTime DESC
                               LIMIT 10))
        ORDER BY referredCount DESC
        LIMIT 100
    """,
    )
    fun quickPicks(now: Long = System.currentTimeMillis()): Flow<List<Song>>

    @Transaction
    @Query(
        """
                SELECT
                    song.*
                FROM
                    (
                        SELECT
                            n.songId AS eid,
                            SUM(playTime) AS oldPlayTime,
                            newPlayTime
                        FROM
                            event
                        JOIN
                            (
                              SELECT
                                songId,
                                SUM(playTime) AS newPlayTime
                              FROM
                                event
                            WHERE
                                timestamp > (:now - 86400000 * 30 * 1)
                              GROUP BY
                                songId
                              ORDER BY
                               newPlayTime
                            ) as n
                        ON event.songId = n.songId
                        WHERE
                            timestamp < (:now - 86400000 * 30 * 1)
                        GROUP BY
                            n.songId
                        ORDER BY
                            oldPlayTime
                    ) AS t
                JOIN song on song.id = t.eid
                WHERE 0.2 * t.oldPlayTime > t.newPlayTime
                LIMIT 100

        """,
    )
    fun forgottenFavorites(now: Long = System.currentTimeMillis()): Flow<List<Song>>

    @Transaction
    @Query(
        """
        SELECT
            song.*
        FROM
            event
        JOIN
            song ON event.songId = song.id
        WHERE
            event.timestamp > (:now - 86400000 * 7 * 2)
        GROUP BY
            song.albumId
        HAVING
            song.albumId IS NOT NULL
        ORDER BY
            sum(event.playTime) DESC
        LIMIT :limit
        OFFSET :offset
        
        """,
    )
    fun getRecommendationAlbum(
        now: Long = System.currentTimeMillis(),
        limit: Int = 5,
        offset: Int = 0,
    ): Flow<List<Song>>

    @Transaction
    @Query(
        """
             SELECT song.id, song.title, song.thumbnailUrl,
               (SELECT COUNT(1)
                FROM event
                WHERE songId = song.id
                  AND timestamp > :fromTimeStamp AND timestamp <= :toTimeStamp) AS songCountListened,
               (SELECT SUM(event.playTime)
                FROM event
                WHERE songId = song.id
                  AND timestamp > :fromTimeStamp AND timestamp <= :toTimeStamp) AS timeListened
        FROM song
        JOIN (SELECT songId
                     FROM event
                     WHERE timestamp > :fromTimeStamp
                     AND timestamp <= :toTimeStamp
                     GROUP BY songId
                     ORDER BY SUM(playTime) DESC
                     LIMIT :limit)
        ON song.id = songId
        LIMIT :limit
        OFFSET :offset
    """,
    )
    fun mostPlayedSongsStats(
        limit: Int = 6,
        offset: Int = 0,
        fromTimeStamp: Long,
        toTimeStamp: Long? = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(),
    ): Flow<List<SongWithStats>>

    @Transaction
    @Query(
        """
        SELECT song.*,
               (SELECT COUNT(1)
                FROM event
                WHERE songId = song.id
                  AND timestamp > :fromTimeStamp AND timestamp <= :toTimeStamp) AS songCountListened,
               (SELECT SUM(event.playTime)
                FROM event
                WHERE songId = song.id
                  AND timestamp > :fromTimeStamp AND timestamp <= :toTimeStamp) AS timeListened
        FROM song
        JOIN (SELECT songId
                     FROM event
                     WHERE timestamp > :fromTimeStamp
                     AND timestamp <= :toTimeStamp
                     GROUP BY songId
                     ORDER BY SUM(playTime) DESC
                     LIMIT :limit)
        ON song.id = songId
        LIMIT :limit
        OFFSET :offset
    """,
    )
    fun mostPlayedSongs(
        fromTimeStamp: Long,
        limit: Int = 6,
        offset: Int = 0,
        toTimeStamp: Long? = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(),
    ): Flow<List<Song>>

    @Transaction
    @Query(
        """
        SELECT artist.*,
               (SELECT COUNT(1)
                FROM song_artist_map
                         JOIN event ON song_artist_map.songId = event.songId
                WHERE artistId = artist.id
                  AND timestamp > :fromTimeStamp AND timestamp <= :toTimeStamp) AS songCount,
               (SELECT SUM(event.playTime)
                FROM song_artist_map
                         JOIN event ON song_artist_map.songId = event.songId
                WHERE artistId = artist.id
                  AND timestamp > :fromTimeStamp AND timestamp <= :toTimeStamp) AS timeListened
        FROM artist
                 JOIN(SELECT artistId, SUM(songTotalPlayTime) AS totalPlayTime
                      FROM song_artist_map
                               JOIN (SELECT songId, SUM(playTime) AS songTotalPlayTime
                                     FROM event
                                     WHERE timestamp > :fromTimeStamp
                                     AND timestamp <= :toTimeStamp
                                     GROUP BY songId) AS e
                                    ON song_artist_map.songId = e.songId
                      GROUP BY artistId
                      ORDER BY totalPlayTime DESC
                      LIMIT :limit
                      OFFSET :offset)
                     ON artist.id = artistId
    """,
    )
    fun mostPlayedArtists(
        fromTimeStamp: Long,
        limit: Int = 6,
        offset: Int = 0,
        toTimeStamp: Long? = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(),
    ): Flow<List<Artist>>

    @Transaction
    @Query(
        """
        SELECT album.*,
               (SELECT COUNT(1)
                FROM song_album_map
                         JOIN event ON song_album_map.songId = event.songId
                WHERE albumId = album.id
                  AND timestamp > :fromTimeStamp AND timestamp <= :toTimeStamp) AS songCountListened,
               (SELECT SUM(event.playTime)
                FROM song_album_map
                         JOIN event ON song_album_map.songId = event.songId
                WHERE albumId = album.id
                  AND timestamp > :fromTimeStamp AND timestamp <= :toTimeStamp) AS timeListened
        FROM album
                WHERE id IN (SELECT song.albumId
                     FROM event
                              JOIN
                          song
                          ON event.songId = song.id
                     WHERE event.timestamp > :fromTimeStamp
                     AND event.timestamp <= :toTimeStamp
                     GROUP BY song.albumId
                     HAVING song.albumId IS NOT NULL)
                ORDER BY timeListened DESC
                LIMIT :limit OFFSET :offset
    """,
    )
    fun mostPlayedAlbums(
        fromTimeStamp: Long,
        limit: Int = 6,
        offset: Int = 0,
        toTimeStamp: Long? = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(),
    ): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    fun song(songId: String?): Flow<Song?>

    @Query("SELECT * FROM song_artist_map WHERE songId = :songId")
    fun songArtistMap(songId: String): List<SongArtistMap>

    @Transaction
    @Query("SELECT * FROM song")
    fun allSongs(): Flow<List<Song>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT artist.*,
               (SELECT COUNT(1)
                FROM song_artist_map
                         JOIN event ON song_artist_map.songId = event.songId
                WHERE artistId = artist.id) AS songCount
        FROM artist
                 LEFT JOIN(SELECT artistId, SUM(songTotalPlayTime) AS totalPlayTime
                      FROM song_artist_map
                               JOIN (SELECT songId, SUM(playTime) AS songTotalPlayTime
                                     FROM event
                                     GROUP BY songId) AS e
                                    ON song_artist_map.songId = e.songId
                      GROUP BY artistId
                      ORDER BY totalPlayTime DESC) AS artistTotalPlayTime
                     ON artist.id = artistId
                     OR artist.bookmarkedAt IS NOT NULL
                     ORDER BY 
                      CASE 
                        WHEN artistTotalPlayTime.artistId IS NULL THEN 1 
                        ELSE 0 
                      END, 
                      artistTotalPlayTime.totalPlayTime DESC
    """,
    )
    fun allArtistsByPlayTime(): Flow<List<Artist>>

    @Query("SELECT * FROM format WHERE id = :id")
    fun format(id: String?): Flow<FormatEntity?>

    @Query("SELECT * FROM lyrics WHERE id = :id")
    fun lyrics(id: String?): Flow<LyricsEntity?>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(1) FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = artist.id AND song.inLibrary IS NOT NULL) AS songCount FROM artist WHERE songCount > 0 ORDER BY rowId",
    )
    fun artistsByCreateDateAsc(): Flow<List<Artist>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(1) FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = artist.id AND song.inLibrary IS NOT NULL) AS songCount FROM artist WHERE songCount > 0 ORDER BY name",
    )
    fun artistsByNameAsc(): Flow<List<Artist>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(1) FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = artist.id AND song.inLibrary IS NOT NULL) AS songCount FROM artist WHERE songCount > 0 ORDER BY songCount",
    )
    fun artistsBySongCountAsc(): Flow<List<Artist>>

    @Transaction
    @Query(
        """
        SELECT artist.*,
               (SELECT COUNT(1)
                FROM song_artist_map
                         JOIN song ON song_artist_map.songId = song.id
                WHERE artistId = artist.id
                  AND song.inLibrary IS NOT NULL) AS songCount
        FROM artist
                 JOIN(SELECT artistId, SUM(totalPlayTime) AS totalPlayTime
                      FROM song_artist_map
                               JOIN song
                                    ON song_artist_map.songId = song.id
                      GROUP BY artistId
                      ORDER BY totalPlayTime)
                     ON artist.id = artistId
        WHERE songCount > 0
    """,
    )
    fun artistsByPlayTimeAsc(): Flow<List<Artist>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(1) FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = artist.id AND song.inLibrary IS NOT NULL) AS songCount FROM artist WHERE bookmarkedAt IS NOT NULL ORDER BY bookmarkedAt",
    )
    fun artistsBookmarkedByCreateDateAsc(): Flow<List<Artist>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(1) FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = artist.id AND song.inLibrary IS NOT NULL) AS songCount FROM artist WHERE bookmarkedAt IS NOT NULL ORDER BY name",
    )
    fun artistsBookmarkedByNameAsc(): Flow<List<Artist>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(1) FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = artist.id AND song.inLibrary IS NOT NULL) AS songCount FROM artist WHERE bookmarkedAt IS NOT NULL ORDER BY songCount",
    )
    fun artistsBookmarkedBySongCountAsc(): Flow<List<Artist>>

    @Transaction
    @Query(
        """
        SELECT artist.*,
               (SELECT COUNT(1)
                FROM song_artist_map
                         JOIN song ON song_artist_map.songId = song.id
                WHERE artistId = artist.id
                  AND song.inLibrary IS NOT NULL) AS songCount
        FROM artist
                 JOIN(SELECT artistId, SUM(totalPlayTime) AS totalPlayTime
                      FROM song_artist_map
                               JOIN song
                                    ON song_artist_map.songId = song.id
                      GROUP BY artistId
                      ORDER BY totalPlayTime)
                     ON artist.id = artistId
        WHERE bookmarkedAt IS NOT NULL
    """,
    )
    fun artistsBookmarkedByPlayTimeAsc(): Flow<List<Artist>>

    fun artists(
        sortType: ArtistSortType,
        descending: Boolean,
    ) = when (sortType) {
        ArtistSortType.CREATE_DATE -> artistsByCreateDateAsc()
        ArtistSortType.NAME ->
            artistsByNameAsc().map { artist ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                artist.sortedWith(compareBy(collator) { it.artist.name })
            }
        ArtistSortType.SONG_COUNT -> artistsBySongCountAsc()
        ArtistSortType.PLAY_TIME -> artistsByPlayTimeAsc()
    }.map { artists ->
        artists
            .filter { it.artist.isYouTubeArtist }
            .reversed(descending)
    }

    fun artistsBookmarked(
        sortType: ArtistSortType,
        descending: Boolean,
    ) = when (sortType) {
        ArtistSortType.CREATE_DATE -> artistsBookmarkedByCreateDateAsc()
        ArtistSortType.NAME ->
            artistsBookmarkedByNameAsc().map { artist ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                artist.sortedWith(compareBy(collator) { it.artist.name })
            }
        ArtistSortType.SONG_COUNT -> artistsBookmarkedBySongCountAsc()
        ArtistSortType.PLAY_TIME -> artistsBookmarkedByPlayTimeAsc()
    }.map { artists ->
        artists
            .filter { it.artist.isYouTubeArtist }
            .reversed(descending)
    }

    @Query(
        "SELECT *, (SELECT COUNT(1) FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = artist.id AND song.inLibrary IS NOT NULL) AS songCount FROM artist WHERE id = :id",
    )
    fun artist(id: String): Flow<Artist?>

    @Transaction
    @Query(
        "SELECT * FROM album WHERE EXISTS(SELECT * FROM song WHERE song.albumId = album.id AND song.inLibrary IS NOT NULL) ORDER BY rowId",
    )
    fun albumsByCreateDateAsc(): Flow<List<Album>>

    @Transaction
    @Query(
        "SELECT * FROM album WHERE EXISTS(SELECT * FROM song WHERE song.albumId = album.id AND song.inLibrary IS NOT NULL) ORDER BY title",
    )
    fun albumsByNameAsc(): Flow<List<Album>>

    @Transaction
    @Query(
        "SELECT * FROM album WHERE EXISTS(SELECT * FROM song WHERE song.albumId = album.id AND song.inLibrary IS NOT NULL) ORDER BY year",
    )
    fun albumsByYearAsc(): Flow<List<Album>>

    @Transaction
    @Query(
        "SELECT * FROM album WHERE EXISTS(SELECT * FROM song WHERE song.albumId = album.id AND song.inLibrary IS NOT NULL) ORDER BY songCount",
    )
    fun albumsBySongCountAsc(): Flow<List<Album>>

    @Transaction
    @Query(
        "SELECT * FROM album WHERE EXISTS(SELECT * FROM song WHERE song.albumId = album.id AND song.inLibrary IS NOT NULL) ORDER BY duration",
    )
    fun albumsByLengthAsc(): Flow<List<Album>>

    @Transaction
    @Query(
        """
        SELECT album.*
        FROM album
                 JOIN song
                      ON song.albumId = album.id
        WHERE EXISTS(SELECT * FROM song WHERE song.albumId = album.id AND song.inLibrary IS NOT NULL)
        GROUP BY album.id
        ORDER BY SUM(song.totalPlayTime)
    """,
    )
    fun albumsByPlayTimeAsc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album WHERE bookmarkedAt IS NOT NULL ORDER BY rowId")
    fun albumsLikedByCreateDateAsc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album WHERE bookmarkedAt IS NOT NULL ORDER BY title")
    fun albumsLikedByNameAsc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album WHERE bookmarkedAt IS NOT NULL ORDER BY year")
    fun albumsLikedByYearAsc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album WHERE bookmarkedAt IS NOT NULL ORDER BY songCount")
    fun albumsLikedBySongCountAsc(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album WHERE bookmarkedAt IS NOT NULL ORDER BY duration")
    fun albumsLikedByLengthAsc(): Flow<List<Album>>

    @Transaction
    @Query(
        """
        SELECT album.*
        FROM album
                 JOIN song
                      ON song.albumId = album.id
        WHERE bookmarkedAt IS NOT NULL
        GROUP BY album.id
        ORDER BY SUM(song.totalPlayTime)
    """,
    )
    fun albumsLikedByPlayTimeAsc(): Flow<List<Album>>

    fun albums(
        sortType: AlbumSortType,
        descending: Boolean,
    ) = when (sortType) {
        AlbumSortType.CREATE_DATE -> albumsByCreateDateAsc()
        AlbumSortType.NAME ->
            albumsByNameAsc().map { albums ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                albums.sortedWith(compareBy(collator) { it.album.title })
            }
        AlbumSortType.ARTIST ->
            albumsByCreateDateAsc().map { albums ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                albums.sortedWith(compareBy(collator) { album -> album.artists.joinToString("") { it.name } })
            }
        AlbumSortType.YEAR -> albumsByYearAsc()
        AlbumSortType.SONG_COUNT -> albumsBySongCountAsc()
        AlbumSortType.LENGTH -> albumsByLengthAsc()
        AlbumSortType.PLAY_TIME -> albumsByPlayTimeAsc()
    }.map { it.reversed(descending) }

    fun albumsLiked(
        sortType: AlbumSortType,
        descending: Boolean,
    ) = when (sortType) {
        AlbumSortType.CREATE_DATE -> albumsLikedByCreateDateAsc()
        AlbumSortType.NAME ->
            albumsLikedByNameAsc().map { albums ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                albums.sortedWith(compareBy(collator) { it.album.title })
            }
        AlbumSortType.ARTIST ->
            albumsLikedByCreateDateAsc().map { albums ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                albums.sortedWith(compareBy(collator) { album -> album.artists.joinToString("") { it.name } })
            }
        AlbumSortType.YEAR -> albumsLikedByYearAsc()
        AlbumSortType.SONG_COUNT -> albumsLikedBySongCountAsc()
        AlbumSortType.LENGTH -> albumsLikedByLengthAsc()
        AlbumSortType.PLAY_TIME -> albumsLikedByPlayTimeAsc()
    }.map { it.reversed(descending) }

    @Transaction
    @Query("SELECT * FROM album WHERE id = :id")
    fun album(id: String): Flow<Album?>

    @Transaction
    @Query("SELECT * FROM album WHERE id = :albumId")
    fun albumWithSongs(albumId: String): Flow<AlbumWithSongs?>

    @Query("SELECT * FROM album_artist_map WHERE albumId = :albumId")
    fun albumArtistMaps(albumId: String): List<AlbumArtistMap>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist ORDER BY rowId")
    fun playlistsByCreateDateAsc(): Flow<List<Playlist>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist ORDER BY lastUpdateTime",
    )
    fun playlistsByUpdatedDateAsc(): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist ORDER BY name")
    fun playlistsByNameAsc(): Flow<List<Playlist>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist ORDER BY songCount",
    )
    fun playlistsBySongCountAsc(): Flow<List<Playlist>>

    fun playlists(
        sortType: PlaylistSortType,
        descending: Boolean,
    ) = when (sortType) {
        PlaylistSortType.CREATE_DATE -> playlistsByCreateDateAsc()
        PlaylistSortType.NAME ->
            playlistsByNameAsc().map { playlists ->
                val collator = Collator.getInstance(Locale.getDefault())
                collator.strength = Collator.PRIMARY
                playlists.sortedWith(compareBy(collator) { it.playlist.name })
            }
        PlaylistSortType.SONG_COUNT -> playlistsBySongCountAsc()
        PlaylistSortType.LAST_UPDATED -> playlistsByUpdatedDateAsc()
    }.map { it.reversed(descending) }

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist WHERE id = :playlistId",
    )
    fun playlist(playlistId: String): Flow<Playlist?>

    @Query("SELECT COUNT(*) from playlist_song_map WHERE playlistId = :playlistId AND songId = :songId LIMIT 1")
    fun checkInPlaylist(
        playlistId: String,
        songId: String,
    ): Int

    @Transaction
    @Query("SELECT * FROM song WHERE title LIKE '%' || :query || '%' AND inLibrary IS NOT NULL LIMIT :previewSize")
    fun searchSongs(
        query: String,
        previewSize: Int = Int.MAX_VALUE,
    ): Flow<List<Song>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(1) FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = artist.id AND song.inLibrary IS NOT NULL) AS songCount FROM artist WHERE name LIKE '%' || :query || '%' AND songCount > 0 LIMIT :previewSize",
    )
    fun searchArtists(
        query: String,
        previewSize: Int = Int.MAX_VALUE,
    ): Flow<List<Artist>>

    @Transaction
    @Query(
        "SELECT * FROM album WHERE title LIKE '%' || :query || '%' AND EXISTS(SELECT * FROM song WHERE song.albumId = album.id AND song.inLibrary IS NOT NULL) LIMIT :previewSize",
    )
    fun searchAlbums(
        query: String,
        previewSize: Int = Int.MAX_VALUE,
    ): Flow<List<Album>>

    @Transaction
    @Query(
        "SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist WHERE name LIKE '%' || :query || '%' LIMIT :previewSize",
    )
    fun searchPlaylists(
        query: String,
        previewSize: Int = Int.MAX_VALUE,
    ): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM event ORDER BY rowId DESC")
    fun events(): Flow<List<EventWithSong>>

    @Transaction
    @Query("SELECT * FROM event ORDER BY rowId ASC LIMIT 1")
    fun firstEvent(): Flow<EventWithSong>

    @Query("DELETE FROM event")
    fun clearListenHistory()

    @Query("SELECT * FROM search_history WHERE `query` LIKE :query || '%' ORDER BY id DESC")
    fun searchHistory(query: String = ""): Flow<List<SearchHistory>>

    @Query("DELETE FROM search_history")
    fun clearSearchHistory()

    @Query("UPDATE song SET totalPlayTime = totalPlayTime + :playTime WHERE id = :songId")
    fun incrementTotalPlayTime(
        songId: String,
        playTime: Long,
    )

    @Query("UPDATE song SET inLibrary = :inLibrary WHERE id = :songId")
    fun inLibrary(
        songId: String,
        inLibrary: LocalDateTime?,
    )

    @Query("SELECT COUNT(1) FROM related_song_map WHERE songId = :songId LIMIT 1")
    fun hasRelatedSongs(songId: String): Boolean

    @Query(
        "SELECT song.* FROM (SELECT * from related_song_map GROUP BY relatedSongId) map JOIN song ON song.id = map.relatedSongId where songId = :songId",
    )
    fun getRelatedSongs(songId: String): Flow<List<Song>>

    @Query(
        """
        UPDATE playlist_song_map SET position = 
            CASE 
                WHEN position < :fromPosition THEN position + 1
                WHEN position > :fromPosition THEN position - 1
                ELSE :toPosition
            END 
        WHERE playlistId = :playlistId AND position BETWEEN MIN(:fromPosition, :toPosition) AND MAX(:fromPosition, :toPosition)
    """,
    )
    fun move(
        playlistId: String,
        fromPosition: Int,
        toPosition: Int,
    )

    @Query("DELETE FROM playlist_song_map WHERE playlistId = :playlistId")
    fun clearPlaylist(playlistId: String)

    @Query("SELECT * FROM artist WHERE name = :name")
    fun artistByName(name: String): ArtistEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: SongEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: AlbumEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: SongArtistMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: SongAlbumMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: AlbumArtistMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: PlaylistSongMap)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(searchHistory: SearchHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(map: RelatedSongMap)

    @Transaction
    fun insert(
        mediaMetadata: MediaMetadata,
        block: (SongEntity) -> SongEntity = { it },
    ) {
        if (insert(mediaMetadata.toSongEntity().let(block)) == -1L) return
        mediaMetadata.artists.forEachIndexed { index, artist ->
            val artistId = artist.id ?: artistByName(artist.name)?.id ?: ArtistEntity.generateArtistId()
            insert(
                ArtistEntity(
                    id = artistId,
                    name = artist.name,
                ),
            )
            insert(
                SongArtistMap(
                    songId = mediaMetadata.id,
                    artistId = artistId,
                    position = index,
                ),
            )
        }
    }

    @Transaction
    fun insert(albumPage: AlbumPage) {
        if (insert(
                AlbumEntity(
                    id = albumPage.album.browseId,
                    title = albumPage.album.title,
                    year = albumPage.album.year,
                    thumbnailUrl = albumPage.album.thumbnail,
                    songCount = albumPage.songs.size,
                    duration = albumPage.songs.sumOf { it.duration ?: 0 },
                ),
            ) == -1L
        ) {
            return
        }
        albumPage.songs
            .map(SongItem::toMediaMetadata)
            .onEach(::insert)
            .onEach {
                update(
                    it.toSongEntity().copy(
                        title = it.title,
                        duration = it.duration,
                        thumbnailUrl = it.thumbnailUrl,
                        albumId = it.album?.id,
                        albumName = it.album?.title,
                    ),
                )
            }.mapIndexed { index, song ->
                SongAlbumMap(
                    songId = song.id,
                    albumId = albumPage.album.browseId,
                    index = index,
                )
            }.forEach(::upsert)
        albumPage.album.artists
            ?.map { artist ->
                ArtistEntity(
                    id = artist.id ?: artistByName(artist.name)?.id ?: ArtistEntity.generateArtistId(),
                    name = artist.name,
                )
            }?.onEach(::insert)
            ?.mapIndexed { index, artist ->
                AlbumArtistMap(
                    albumId = albumPage.album.browseId,
                    artistId = artist.id,
                    order = index,
                )
            }?.forEach(::insert)
    }

    @Transaction
    fun update(
        song: Song,
        mediaMetadata: MediaMetadata,
    ) {
        update(
            song.song.copy(
                title = mediaMetadata.title,
                duration = mediaMetadata.duration,
                thumbnailUrl = mediaMetadata.thumbnailUrl,
                albumId = mediaMetadata.album?.id,
                albumName = mediaMetadata.album?.title,
            ),
        )
        songArtistMap(song.id).forEach(::delete)
        mediaMetadata.artists.forEachIndexed { index, artist ->
            val artistId = artist.id ?: artistByName(artist.name)?.id ?: ArtistEntity.generateArtistId()
            insert(
                ArtistEntity(
                    id = artistId,
                    name = artist.name,
                ),
            )
            insert(
                SongArtistMap(
                    songId = song.id,
                    artistId = artistId,
                    position = index,
                ),
            )
        }
    }

    @Update
    fun update(song: SongEntity)

    @Update
    fun update(artist: ArtistEntity)

    @Update
    fun update(album: AlbumEntity)

    @Update
    fun update(playlist: PlaylistEntity)

    @Update
    fun update(map: PlaylistSongMap)

    fun update(
        artist: ArtistEntity,
        artistPage: ArtistPage,
    ) {
        update(
            artist.copy(
                name = artistPage.artist.title,
                thumbnailUrl = artistPage.artist.thumbnail.resize(544, 544),
                lastUpdateTime = LocalDateTime.now(),
            ),
        )
    }

    @Transaction
    fun update(
        album: AlbumEntity,
        albumPage: AlbumPage,
        artists: List<ArtistEntity>? = emptyList(),
    ) {
        update(
            album.copy(
                id = albumPage.album.browseId,
                title = albumPage.album.title,
                year = albumPage.album.year,
                thumbnailUrl = albumPage.album.thumbnail,
                songCount = albumPage.songs.size,
                duration = albumPage.songs.sumOf { it.duration ?: 0 },
            ),
        )
        if (artists?.size != albumPage.album.artists?.size) {
            artists?.forEach(::delete)
        }
        albumPage.songs
            .map(SongItem::toMediaMetadata)
            .onEach(::insert)
            .onEach {
                update(
                    it.toSongEntity().copy(
                        title = it.title,
                        duration = it.duration,
                        thumbnailUrl = it.thumbnailUrl,
                        albumId = it.album?.id,
                        albumName = it.album?.title,
                    ),
                )
            }.mapIndexed { index, song ->
                SongAlbumMap(
                    songId = song.id,
                    albumId = albumPage.album.browseId,
                    index = index,
                )
            }.forEach(::upsert)

        albumPage.album.artists?.let { artists ->
            // Recreate album artists
            albumArtistMaps(album.id).forEach(::delete)
            artists
                .map { artist ->
                    ArtistEntity(
                        id = artist.id ?: artistByName(artist.name)?.id ?: ArtistEntity.generateArtistId(),
                        name = artist.name,
                    )
                }.onEach(::insert)
                .mapIndexed { index, artist ->
                    AlbumArtistMap(
                        albumId = albumPage.album.browseId,
                        artistId = artist.id,
                        order = index,
                    )
                }.forEach(::insert)
        }
    }

    @Upsert
    fun upsert(map: SongAlbumMap)

    @Upsert
    fun upsert(lyrics: LyricsEntity)

    @Upsert
    fun upsert(format: FormatEntity)

    @Delete
    fun delete(song: SongEntity)

    @Delete
    fun delete(songArtistMap: SongArtistMap)

    @Delete
    fun delete(artist: ArtistEntity)

    @Delete
    fun delete(album: AlbumEntity)

    @Delete
    fun delete(albumArtistMap: AlbumArtistMap)

    @Delete
    fun delete(playlist: PlaylistEntity)

    @Delete
    fun delete(playlistSongMap: PlaylistSongMap)

    @Delete
    fun delete(lyrics: LyricsEntity)

    @Delete
    fun delete(searchHistory: SearchHistory)

    @Delete
    fun delete(event: Event)

    @Query("SELECT * FROM playlist_song_map WHERE songId = :songId")
    fun playlistSongMaps(songId: String): List<PlaylistSongMap>

    @Query("SELECT * FROM playlist_song_map WHERE playlistId = :playlistId AND position >= :from ORDER BY position")
    fun playlistSongMaps(
        playlistId: String,
        from: Int,
    ): List<PlaylistSongMap>

    @RawQuery
    fun raw(supportSQLiteQuery: SupportSQLiteQuery): Int

    fun checkpoint() {
        raw("PRAGMA wal_checkpoint(FULL)".toSQLiteQuery())
    }
}

