package com.malopieds.innertune.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.malopieds.innertune.LocalDatabase
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.R
import com.malopieds.innertune.constants.CONTENT_TYPE_HEADER
import com.malopieds.innertune.constants.CONTENT_TYPE_PLAYLIST
import com.malopieds.innertune.constants.GridThumbnailHeight
import com.malopieds.innertune.constants.LibraryViewType
import com.malopieds.innertune.constants.PlaylistSortDescendingKey
import com.malopieds.innertune.constants.PlaylistSortType
import com.malopieds.innertune.constants.PlaylistSortTypeKey
import com.malopieds.innertune.constants.PlaylistViewTypeKey
import com.malopieds.innertune.db.entities.Playlist
import com.malopieds.innertune.db.entities.PlaylistEntity
import com.malopieds.innertune.ui.component.HideOnScrollFAB
import com.malopieds.innertune.ui.component.LocalMenuState
import com.malopieds.innertune.ui.component.PlaylistGridItem
import com.malopieds.innertune.ui.component.PlaylistListItem
import com.malopieds.innertune.ui.component.SortHeader
import com.malopieds.innertune.ui.component.TextFieldDialog
import com.malopieds.innertune.ui.menu.PlaylistMenu
import com.malopieds.innertune.utils.rememberEnumPreference
import com.malopieds.innertune.utils.rememberPreference
import com.malopieds.innertune.viewmodels.LibraryPlaylistsViewModel
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryPlaylistsScreen(
    navController: NavController,
    filterContent: @Composable () -> Unit,
    viewModel: LibraryPlaylistsViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current

    val coroutineScope = rememberCoroutineScope()

    var viewType by rememberEnumPreference(PlaylistViewTypeKey, LibraryViewType.GRID)
    val (sortType, onSortTypeChange) = rememberEnumPreference(PlaylistSortTypeKey, PlaylistSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(PlaylistSortDescendingKey, true)

    val playlists by viewModel.allPlaylists.collectAsState()

    val topSize by viewModel.topValue.collectAsState(initial = 50)
    val likedPlaylist =
        Playlist(
            playlist = PlaylistEntity(id = UUID.randomUUID().toString(), name = stringResource(R.string.liked)),
            songCount = 0,
            thumbnails = emptyList(),
        )

    val downloadPlaylist =
        Playlist(
            playlist = PlaylistEntity(id = UUID.randomUUID().toString(), name = stringResource(R.string.offline)),
            songCount = 0,
            thumbnails = emptyList(),
        )

    val topPlaylist =
        Playlist(
            playlist = PlaylistEntity(id = UUID.randomUUID().toString(), name = stringResource(R.string.my_top) + " $topSize"),
            songCount = 0,
            thumbnails = emptyList(),
        )

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

    var showAddPlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showAddPlaylistDialog) {
        TextFieldDialog(
            icon = { Icon(painter = painterResource(R.drawable.add), contentDescription = null) },
            title = { Text(text = stringResource(R.string.create_playlist)) },
            onDismiss = { showAddPlaylistDialog = false },
            onDone = { playlistName ->
                database.query {
                    insert(
                        PlaylistEntity(
                            name = playlistName,
                        ),
                    )
                }
            },
        )
    }

    val headerContent = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp),
        ) {
            SortHeader(
                sortType = sortType,
                sortDescending = sortDescending,
                onSortTypeChange = onSortTypeChange,
                onSortDescendingChange = onSortDescendingChange,
                sortTypeText = { sortType ->
                    when (sortType) {
                        PlaylistSortType.CREATE_DATE -> R.string.sort_by_create_date
                        PlaylistSortType.NAME -> R.string.sort_by_name
                        PlaylistSortType.SONG_COUNT -> R.string.sort_by_song_count
                        PlaylistSortType.LAST_UPDATED -> R.string.sort_by_last_updated
                    }
                },
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = pluralStringResource(R.plurals.n_playlist, playlists.size, playlists.size),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            IconButton(
                onClick = {
                    viewType = viewType.toggle()
                },
                modifier = Modifier.padding(start = 6.dp, end = 6.dp),
            ) {
                Icon(
                    painter =
                        painterResource(
                            when (viewType) {
                                LibraryViewType.LIST -> R.drawable.list
                                LibraryViewType.GRID -> R.drawable.grid_view
                            },
                        ),
                    contentDescription = null,
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (viewType) {
            LibraryViewType.LIST -> {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                ) {
                    item(
                        key = "filter",
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        filterContent()
                    }

                    item(
                        key = "header",
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        headerContent()
                    }

                    item(
                        key = "likedPlaylist",
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) {
                        PlaylistListItem(
                            playlist = likedPlaylist,
                            autoPlaylist = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("auto_playlist/liked")
                                    }.animateItemPlacement(),
                            context = LocalContext.current
                        )
                    }

                    item(
                        key = "downloadedPlaylist",
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) {
                        PlaylistListItem(
                            playlist = downloadPlaylist,
                            autoPlaylist = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("auto_playlist/downloaded")
                                    }.animateItemPlacement(),
                            context = LocalContext.current
                        )
                    }

                    item(
                        key = "TopPlaylist",
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) {
                        PlaylistListItem(
                            playlist = topPlaylist,
                            autoPlaylist = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("top_playlist/$topSize")
                                    }.animateItemPlacement(),
                            context = LocalContext.current
                        )
                    }

                    items(
                        items = playlists,
                        key = { it.id },
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) { playlist ->
                        PlaylistListItem(
                            playlist = playlist,
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            PlaylistMenu(
                                                playlist = playlist,
                                                coroutineScope = coroutineScope,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_vert),
                                        contentDescription = null,
                                    )
                                }
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("local_playlist/${playlist.id}")
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                PlaylistMenu(
                                                    playlist = playlist,
                                                    coroutineScope = coroutineScope,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ).animateItemPlacement(),
                            context = LocalContext.current
                        )
                    }
                }

                HideOnScrollFAB(
                    lazyListState = lazyListState,
                    icon = R.drawable.add,
                    onClick = {
                        showAddPlaylistDialog = true
                    },
                )
            }

            LibraryViewType.GRID -> {
                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
                    contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                ) {
                    item(
                        key = "filter",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        filterContent()
                    }

                    item(
                        key = "header",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        headerContent()
                    }

                    item(
                        key = "likedPlaylist",
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) {
                        PlaylistGridItem(
                            playlist = likedPlaylist,
                            fillMaxWidth = true,
                            autoPlaylist = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("auto_playlist/liked")
                                        },
                                    ).animateItemPlacement(),
                            context = null
                        )
                    }

                    item(
                        key = "downloadedPlaylist",
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) {
                        PlaylistGridItem(
                            playlist = downloadPlaylist,
                            fillMaxWidth = true,
                            autoPlaylist = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("auto_playlist/downloaded")
                                        },
                                    ).animateItemPlacement(),
                            context = null
                        )
                    }

                    item(
                        key = "TopPlaylist",
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) {
                        PlaylistGridItem(
                            playlist = topPlaylist,
                            fillMaxWidth = true,
                            autoPlaylist = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("top_playlist/$topSize")
                                        },
                                    ).animateItemPlacement(),
                            context = null
                        )
                    }

                    items(
                        items = playlists,
                        key = { it.id },
                        contentType = { CONTENT_TYPE_PLAYLIST },
                    ) { playlist ->
                        val context = LocalContext.current


                        PlaylistGridItem(
                            playlist = playlist,
                            context = context,
                            fillMaxWidth = true,
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate("local_playlist/${playlist.id}")
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            PlaylistMenu(
                                                playlist = playlist,
                                                coroutineScope = coroutineScope,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                )
                                .animateItemPlacement(),
                        )

                    }
                }

                HideOnScrollFAB(
                    lazyListState = lazyGridState,
                    icon = R.drawable.add,
                    onClick = {
                        showAddPlaylistDialog = true
                    },
                )
            }
        }
    }
}
