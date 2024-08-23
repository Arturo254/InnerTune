package com.malopieds.innertune.models

import androidx.compose.runtime.Immutable
import com.malopieds.innertube.models.SongItem
import com.malopieds.innertune.db.entities.Song
import com.malopieds.innertune.db.entities.SongEntity
import com.malopieds.innertune.ui.utils.resize
import java.io.Serializable

@Immutable
data class MediaMetadata(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val duration: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
    val explicit: Boolean = false,
) : Serializable {
    data class Artist(
        val id: String?,
        val name: String,
    ) : Serializable

    data class Album(
        val id: String,
        val title: String,
    ) : Serializable

    fun toSongEntity() =
        SongEntity(
            id = id,
            title = title,
            duration = duration,
            thumbnailUrl = thumbnailUrl,
            albumId = album?.id,
            albumName = album?.title,
        )
}

fun Song.toMediaMetadata() =
    MediaMetadata(
        id = song.id,
        title = song.title,
        artists =
            artists.map {
                MediaMetadata.Artist(
                    id = it.id,
                    name = it.name,
                )
            },
        duration = song.duration,
        thumbnailUrl = song.thumbnailUrl,
        album =
            album?.let {
                MediaMetadata.Album(
                    id = it.id,
                    title = it.title,
                )
            } ?: song.albumId?.let { albumId ->
                MediaMetadata.Album(
                    id = albumId,
                    title = song.albumName.orEmpty(),
                )
            },
    )

fun SongItem.toMediaMetadata() =
    MediaMetadata(
        id = id,
        title = title,
        artists =
            artists.map {
                MediaMetadata.Artist(
                    id = it.id,
                    name = it.name,
                )
            },
        duration = duration ?: -1,
        thumbnailUrl = thumbnail.resize(544, 544),
        album =
            album?.let {
                MediaMetadata.Album(
                    id = it.id,
                    title = it.name,
                )
            },
        explicit = explicit,
    )
