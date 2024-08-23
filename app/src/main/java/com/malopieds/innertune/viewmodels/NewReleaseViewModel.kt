package com.malopieds.innertune.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malopieds.innertube.YouTube
import com.malopieds.innertube.models.AlbumItem
import com.malopieds.innertube.models.filterExplicit
import com.malopieds.innertune.constants.HideExplicitKey
import com.malopieds.innertune.db.MusicDatabase
import com.malopieds.innertune.utils.dataStore
import com.malopieds.innertune.utils.get
import com.malopieds.innertune.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewReleaseViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        database: MusicDatabase,
    ) : ViewModel() {
        private val _newReleaseAlbums = MutableStateFlow<List<AlbumItem>>(emptyList())
        val newReleaseAlbums = _newReleaseAlbums.asStateFlow()

        init {
            viewModelScope.launch {
                YouTube
                    .newReleaseAlbums()
                    .onSuccess { albums ->
                        val artists: MutableMap<Int, String> = mutableMapOf()
                        val favouriteArtists: MutableMap<Int, String> = mutableMapOf()
                        database.allArtistsByPlayTime().first().let { list ->
                            var favIndex = 0
                            for ((artistsIndex, artist) in list.withIndex()) {
                                artists[artistsIndex] = artist.id
                                if (artist.artist.bookmarkedAt != null) {
                                    favouriteArtists[favIndex] = artist.id
                                    favIndex++
                                }
                            }
                        }
                        _newReleaseAlbums.value =
                            albums
                                .sortedBy { album ->
                                    val artistIds = album.artists.orEmpty().mapNotNull { it.id }
                                    val firstArtistKey =
                                        artistIds.firstNotNullOfOrNull { artistId ->
                                            if (artistId in favouriteArtists.values) {
                                                favouriteArtists.entries.firstOrNull { it.value == artistId }?.key
                                            } else {
                                                artists.entries.firstOrNull { it.value == artistId }?.key
                                            }
                                        } ?: Int.MAX_VALUE
                                    firstArtistKey
                                }.filterExplicit(context.dataStore.get(HideExplicitKey, false))
                    }.onFailure {
                        reportException(it)
                    }
            }
        }
    }
