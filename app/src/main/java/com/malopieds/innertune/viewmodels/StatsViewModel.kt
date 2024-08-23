package com.malopieds.innertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malopieds.innertube.YouTube
import com.malopieds.innertune.constants.StatPeriod
import com.malopieds.innertune.db.MusicDatabase
import com.malopieds.innertune.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel
    @Inject
    constructor(
        val database: MusicDatabase,
    ) : ViewModel() {
        val statPeriod = MutableStateFlow(StatPeriod.`WEEK_1`)

        val mostPlayedSongs =
            statPeriod
                .flatMapLatest { period ->
                    database.mostPlayedSongs(period.toTimeMillis())
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        val mostPlayedArtists =
            statPeriod
                .flatMapLatest { period ->
                    database.mostPlayedArtists(period.toTimeMillis()).map { artists ->
                        artists.filter { it.artist.isYouTubeArtist }
                    }
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        val mostPlayedAlbums =
            statPeriod
                .flatMapLatest { period ->
                    database.mostPlayedAlbums(period.toTimeMillis())
                }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        init {
            viewModelScope.launch {
                mostPlayedArtists.collect { artists ->
                    artists
                        .map { it.artist }
                        .filter {
                            it.thumbnailUrl == null || Duration.between(it.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)
                        }.forEach { artist ->
                            YouTube.artist(artist.id).onSuccess { artistPage ->
                                database.query {
                                    update(artist, artistPage)
                                }
                            }
                        }
                }
            }
            viewModelScope.launch {
                mostPlayedAlbums.collect { albums ->
                    albums
                        .filter {
                            it.album.songCount == 0
                        }.forEach { album ->
                            YouTube
                                .album(album.id)
                                .onSuccess { albumPage ->
                                    database.query {
                                        update(album.album, albumPage, album.artists)
                                    }
                                }.onFailure {
                                    reportException(it)
                                    if (it.message?.contains("NOT_FOUND") == true) {
                                        database.query {
                                            delete(album.album)
                                        }
                                    }
                                }
                        }
                }
            }
        }
    }
