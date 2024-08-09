package com.malopieds.innertune.ui.screens.playlist

import android.icu.text.Transliterator
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastSumBy
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.malopieds.innertube.YouTube
import com.malopieds.innertube.models.SongItem
import com.malopieds.innertube.utils.completed
import com.malopieds.innertune.LocalDatabase
import com.malopieds.innertune.LocalDownloadUtil
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.LocalPlayerConnection
import com.malopieds.innertune.R
import com.malopieds.innertune.constants.AlbumThumbnailSize
import com.malopieds.innertune.constants.PlaylistEditLockKey
import com.malopieds.innertune.constants.PlaylistSongSortDescendingKey
import com.malopieds.innertune.constants.PlaylistSongSortType
import com.malopieds.innertune.constants.PlaylistSongSortTypeKey
import com.malopieds.innertune.constants.ThumbnailCornerRadius
import com.malopieds.innertune.db.entities.PlaylistSong
import com.malopieds.innertune.db.entities.PlaylistSongMap
import com.malopieds.innertune.extensions.move
import com.malopieds.innertune.extensions.toMediaItem
import com.malopieds.innertune.extensions.togglePlayPause
import com.malopieds.innertune.models.toMediaMetadata
import com.malopieds.innertune.playback.ExoDownloadService
import com.malopieds.innertune.playback.queues.ListQueue
import com.malopieds.innertune.ui.component.AutoResizeText
import com.malopieds.innertune.ui.component.DefaultDialog
import com.malopieds.innertune.ui.component.EmptyPlaceholder
import com.malopieds.innertune.ui.component.FontSizeRange
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.component.LocalMenuState
import com.malopieds.innertune.ui.component.SongListItem
import com.malopieds.innertune.ui.component.SortHeader
import com.malopieds.innertune.ui.component.TextFieldDialog
import com.malopieds.innertune.ui.menu.SelectionSongMenu
import com.malopieds.innertune.ui.menu.SongMenu
import com.malopieds.innertune.ui.utils.ItemWrapper
import com.malopieds.innertune.ui.utils.backToMain
import com.malopieds.innertune.utils.makeTimeString
import com.malopieds.innertune.utils.rememberEnumPreference
import com.malopieds.innertune.utils.rememberPreference
import com.malopieds.innertune.viewmodels.LocalPlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocalPlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: LocalPlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()
    val mutableSongs = remember { mutableStateListOf<PlaylistSong>() }
    val playlistLength =
        remember(songs) {
            songs.fastSumBy { it.song.song.duration }
        }
    val (sortType, onSortTypeChange) = rememberEnumPreference(PlaylistSongSortTypeKey, PlaylistSongSortType.CUSTOM)
    val (sortDescending, onSortDescendingChange) = rememberPreference(PlaylistSongSortDescendingKey, true)
    var locked by rememberPreference(PlaylistEditLockKey, defaultValue = false)
    val wrappedSongs = songs.map { item -> ItemWrapper(item) }.toMutableList()
    var searchQuery by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var selection by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    LaunchedEffect(songs) {
        mutableSongs.apply {
            clear()
            addAll(songs)
        }
        if (songs.isEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs.all { downloads[it.song.id]?.state == Download.STATE_COMPLETED }) {
                    Download.STATE_COMPLETED
                } else if (songs.all {
                        downloads[it.song.id]?.state == Download.STATE_QUEUED ||
                                downloads[it.song.id]?.state == Download.STATE_DOWNLOADING ||
                                downloads[it.song.id]?.state == Download.STATE_COMPLETED
                    }
                ) {
                    Download.STATE_DOWNLOADING
                } else {
                    Download.STATE_STOPPED
                }
        }
    }

    var showEditDialog by remember {
        mutableStateOf(false)
    }

    if (showEditDialog) {
        playlist?.playlist?.let { playlistEntity ->
            TextFieldDialog(
                icon = { Icon(painter = painterResource(R.drawable.edit), contentDescription = null) },
                title = { Text(text = stringResource(R.string.edit_playlist)) },
                onDismiss = { showEditDialog = false },
                initialTextFieldValue = TextFieldValue(playlistEntity.name, TextRange(playlistEntity.name.length)),
                onDone = { name ->
                    database.query {
                        update(playlistEntity.copy(name = name, lastUpdateTime = LocalDateTime.now()))
                    }
                },
            )
        }
    }

    var showRemoveDownloadDialog by remember {
        mutableStateOf(false)
    }

    if (showRemoveDownloadDialog) {
        DefaultDialog(
            onDismiss = { showRemoveDownloadDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.remove_download_playlist_confirm, playlist?.playlist!!.name),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            },
            buttons = {
                TextButton(
                    onClick = { showRemoveDownloadDialog = false },
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }

                TextButton(
                    onClick = {
                        showRemoveDownloadDialog = false
                        songs.forEach { song ->
                            DownloadService.sendRemoveDownload(
                                context,
                                ExoDownloadService::class.java,
                                song.song.id,
                                false,
                            )
                        }
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
        )
    }

    val headerItems = 2
    val reorderableState =
        rememberReorderableLazyListState(
            onMove = { from, to ->
                if (to.index >= headerItems && from.index >= headerItems) {
                    mutableSongs.move(from.index - headerItems, to.index - headerItems)
                }
            },
            onDragEnd = { fromIndex, toIndex ->
                val from = if (fromIndex < 2) 2 else fromIndex
                val to = if (toIndex < 2) 2 else toIndex
                database.transaction {
                    move(viewModel.playlistId, from - headerItems, to - headerItems)
                }
            },
        )

    val showTopBarTitle by remember {
        derivedStateOf {
            reorderableState.listState.firstVisibleItemIndex > 0
        }
    }

    var dismissJob: Job? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = reorderableState.listState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
            modifier = Modifier.reorderable(reorderableState),
        ) {
            playlist?.let { playlist ->
                if (playlist.songCount == 0) {
                    item {
                        EmptyPlaceholder(
                            icon = R.drawable.music_note,
                            text = stringResource(R.string.playlist_is_empty),
                        )
                    }
                } else {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (playlist.thumbnails.size == 1) {
                                    AsyncImage(
                                        model = playlist.thumbnails[0],
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                        Modifier
                                            .size(AlbumThumbnailSize)
                                            .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                                    )
                                } else if (playlist.thumbnails.size > 1) {
                                    Box(
                                        modifier =
                                        Modifier
                                            .size(AlbumThumbnailSize)
                                            .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                                    ) {
                                        listOf(
                                            Alignment.TopStart,
                                            Alignment.TopEnd,
                                            Alignment.BottomStart,
                                            Alignment.BottomEnd,
                                        ).fastForEachIndexed { index, alignment ->
                                            AsyncImage(
                                                model = playlist.thumbnails.getOrNull(index),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier =
                                                Modifier
                                                    .align(alignment)
                                                    .size(AlbumThumbnailSize / 2),
                                            )
                                        }
                                    }
                                }

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    AutoResizeText(
                                        text = playlist.playlist.name,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSizeRange = FontSizeRange(16.sp, 22.sp),
                                    )

                                    Text(
                                        text = pluralStringResource(R.plurals.n_song, playlist.songCount, playlist.songCount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Normal,
                                    )

                                    Text(
                                        text = makeTimeString(playlistLength * 1000L),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Normal,
                                    )

                                    Row {
                                        IconButton(
                                            onClick = { showEditDialog = true },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.edit),
                                                contentDescription = null,
                                            )
                                        }

                                        if (playlist.playlist.browseId != null) {
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        val playlistPage =
                                                            YouTube.playlist(playlist.playlist.browseId).completed().getOrNull()
                                                                ?: return@launch
                                                        database.transaction {
                                                            clearPlaylist(playlist.id)
                                                            playlistPage.songs
                                                                .map(SongItem::toMediaMetadata)
                                                                .onEach(::insert)
                                                                .mapIndexed { position, song ->
                                                                    PlaylistSongMap(
                                                                        songId = song.id,
                                                                        playlistId = playlist.id,
                                                                        position = position,
                                                                    )
                                                                }.forEach(::insert)
                                                        }
                                                        snackbarHostState.showSnackbar(context.getString(R.string.playlist_synced))
                                                    }
                                                },
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.sync),
                                                    contentDescription = null,
                                                )
                                            }
                                        }

                                        when (downloadState) {
                                            Download.STATE_COMPLETED -> {
                                                IconButton(
                                                    onClick = {
                                                        showRemoveDownloadDialog = true
                                                    },
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.offline),
                                                        contentDescription = null,
                                                    )
                                                }
                                            }

                                            Download.STATE_DOWNLOADING -> {
                                                IconButton(
                                                    onClick = {
                                                        songs.forEach { song ->
                                                            DownloadService.sendRemoveDownload(
                                                                context,
                                                                ExoDownloadService::class.java,
                                                                song.song.id,
                                                                false,
                                                            )
                                                        }
                                                    },
                                                ) {
                                                    CircularProgressIndicator(
                                                        strokeWidth = 2.dp,
                                                        modifier = Modifier.size(24.dp),
                                                    )
                                                }
                                            }

                                            else -> {
                                                IconButton(
                                                    onClick = {
                                                        songs.forEach { song ->
                                                            val downloadRequest =
                                                                DownloadRequest
                                                                    .Builder(song.song.id, song.song.id.toUri())
                                                                    .setCustomCacheKey(song.song.id)
                                                                    .setData(
                                                                        song.song.song.title
                                                                            .toByteArray(),
                                                                    ).build()
                                                            DownloadService.sendAddDownload(
                                                                context,
                                                                ExoDownloadService::class.java,
                                                                downloadRequest,
                                                                false,
                                                            )
                                                        }
                                                    },
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.download),
                                                        contentDescription = null,
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                playerConnection.addToQueue(
                                                    items = songs.map { it.song.toMediaItem() },
                                                )
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.queue_music),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = playlist.playlist.name,
                                                items = songs.map { it.song.toMediaItem() },
                                            ),
                                        )
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.play),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(R.string.play))
                                }

                                OutlinedButton(
                                    onClick = {
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = playlist.playlist.name,
                                                items = songs.shuffled().map { it.song.toMediaItem() },
                                            ),
                                        )
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(R.string.shuffle))
                                }
                            }
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(context.getString(R.string.search)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                                shape = MaterialTheme.shapes.large,
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.search),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp),
                        ) {
                            if (selection) {
                                val count = wrappedSongs.count { it.isSelected }
                                Text(text = "$count elements selected", modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        if (count == wrappedSongs.size) {
                                            wrappedSongs.forEach { it.isSelected = false }
                                        } else {
                                            wrappedSongs.forEach { it.isSelected = true }
                                        }
                                    },
                                ) {
                                    Icon(
                                        painter =
                                        painterResource(
                                            if (count ==
                                                wrappedSongs.size
                                            ) {
                                                R.drawable.deselect
                                            } else {
                                                R.drawable.select_all
                                            },
                                        ),
                                        contentDescription = null,
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            SelectionSongMenu(
                                                songSelection = wrappedSongs.filter { it.isSelected }.map { it.item.song },
                                                songPosition = wrappedSongs.filter { it.isSelected }.map { it.item.map },
                                                onDismiss = menuState::dismiss,
                                                clearAction = {
                                                    selection = false
                                                    wrappedSongs.clear()
                                                },
                                            )
                                        }
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_vert),
                                        contentDescription = null,
                                    )
                                }

                                IconButton(
                                    onClick = { selection = false },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.close),
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                SortHeader(
                                    sortType = sortType,
                                    sortDescending = sortDescending,
                                    onSortTypeChange = onSortTypeChange,
                                    onSortDescendingChange = onSortDescendingChange,
                                    sortTypeText = { sortType ->
                                        when (sortType) {
                                            PlaylistSongSortType.CUSTOM -> R.string.sort_by_custom
                                            PlaylistSongSortType.CREATE_DATE -> R.string.sort_by_create_date
                                            PlaylistSongSortType.NAME -> R.string.sort_by_name
                                            PlaylistSongSortType.ARTIST -> R.string.sort_by_artist
                                            PlaylistSongSortType.PLAY_TIME -> R.string.sort_by_play_time
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )

                                IconButton(
                                    onClick = { selection = !selection },
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(if (selection) R.drawable.deselect else R.drawable.select_all),
                                        contentDescription = null,
                                    )
                                }

                                IconButton(
                                    onClick = { locked = !locked },
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(if (locked) R.drawable.lock else R.drawable.lock_open),
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    }

                    if (selection) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 16.dp),
                            ) {
                            }
                        }
                    }
                }
            }
            val searchQueryStr = textNoAccentsOrPunctMark(searchQuery.text.trim())
            val filteredSongs = if (searchQueryStr.isEmpty())
            { mutableSongs }
            else{
                mutableSongs.filter {
                    textNoAccentsOrPunctMark(it.song.song.title).contains(searchQueryStr, ignoreCase = true) or
                            textNoAccentsOrPunctMark(it.song.artists.joinToString("").trim()).contains(searchQueryStr, ignoreCase = true)
                }
            }
            if (!selection) {
                itemsIndexed(
                    items = filteredSongs,
                    key = { _, song -> song.map.id },
                ) { index, song ->
                    ReorderableItem(
                        reorderableState = reorderableState,
                        key = song.map.id,
                    ) {
                        val currentItem by rememberUpdatedState(song)
                        val dismissBoxState =
                            rememberSwipeToDismissBoxState(
                                positionalThreshold = { totalDistance ->
                                    totalDistance
                                },
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd ||
                                        dismissValue == SwipeToDismissBoxValue.EndToStart
                                    ) {
                                        database.transaction {
                                            move(
                                                currentItem.map.playlistId,
                                                currentItem.map.position,
                                                Int.MAX_VALUE,
                                            )
                                            delete(currentItem.map.copy(position = Int.MAX_VALUE))
                                        }
                                        dismissJob?.cancel()
                                        dismissJob =
                                            coroutineScope.launch {
                                                val snackbarResult =
                                                    snackbarHostState.showSnackbar(
                                                        message =
                                                        context.getString(
                                                            R.string.removed_song_from_playlist,
                                                            currentItem.song.song.title,
                                                        ),
                                                        actionLabel = context.getString(R.string.undo),
                                                        duration = SnackbarDuration.Short,
                                                    )
                                                if (snackbarResult == SnackbarResult.ActionPerformed) {
                                                    database.transaction {
                                                        insert(currentItem.map.copy(position = playlistLength))
                                                        move(
                                                            currentItem.map.playlistId,
                                                            playlistLength,
                                                            currentItem.map.position,
                                                        )
                                                    }
                                                }
                                            }
                                    }
                                    true
                                },
                            )

                        val content: @Composable () -> Unit = {
                            SongListItem(
                                song = song.song,
                                isActive = song.song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                showInLibraryIcon = true,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song.song,
                                                    navController = navController,
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

                                    if (sortType == PlaylistSongSortType.CUSTOM && !locked) {
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.detectReorder(reorderableState),
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.drag_handle),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                },
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (song.song.id == mediaMetadata?.id) {
                                                playerConnection.player.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = playlist!!.playlist.name,
                                                        items = songs.map { it.song.toMediaItem() },
                                                        startIndex = index,
                                                    ),
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song.song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ),
                            )
                        }

                        if (locked) {
                            content()
                        } else {
                            SwipeToDismissBox(
                                state = dismissBoxState,
                                backgroundContent = {},
                            ) {
                                content()
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = wrappedSongs,
                    key = { _, song -> song.item.map.id },
                ) { index, songWrapper ->
                    ReorderableItem(
                        reorderableState = reorderableState,
                        key = songWrapper.item.map.id,
                    ) {
                        val currentItem by rememberUpdatedState(songWrapper.item)
                        val dismissBoxState =
                            rememberSwipeToDismissBoxState(
                                positionalThreshold = { totalDistance ->
                                    totalDistance
                                },
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd ||
                                        dismissValue == SwipeToDismissBoxValue.EndToStart
                                    ) {
                                        database.transaction {
                                            move(
                                                currentItem.map.playlistId,
                                                currentItem.map.position,
                                                Int.MAX_VALUE,
                                            )
                                            delete(currentItem.map.copy(position = Int.MAX_VALUE))
                                        }
                                        dismissJob?.cancel()
                                        dismissJob =
                                            coroutineScope.launch {
                                                val snackbarResult =
                                                    snackbarHostState.showSnackbar(
                                                        message =
                                                        context.getString(
                                                            R.string.removed_song_from_playlist,
                                                            currentItem.song.song.title,
                                                        ),
                                                        actionLabel = context.getString(R.string.undo),
                                                        duration = SnackbarDuration.Short,
                                                    )
                                                if (snackbarResult == SnackbarResult.ActionPerformed) {
                                                    database.transaction {
                                                        insert(currentItem.map.copy(position = playlistLength))
                                                        move(
                                                            currentItem.map.playlistId,
                                                            playlistLength,
                                                            currentItem.map.position,
                                                        )
                                                    }
                                                }
                                            }
                                    }
                                    true
                                },
                            )

                        val content: @Composable () -> Unit = {
                            SongListItem(
                                song = songWrapper.item.song,
                                isActive = songWrapper.item.song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                showInLibraryIcon = true,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = songWrapper.item.song,
                                                    navController = navController,
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

                                    if (sortType == PlaylistSongSortType.CUSTOM && !locked) {
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.detectReorder(reorderableState),
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.drag_handle),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                },
                                isSelected = songWrapper.isSelected && selection,
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (!selection) {
                                                if (songWrapper.item.song.id == mediaMetadata?.id) {
                                                    playerConnection.player.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        ListQueue(
                                                            title = playlist!!.playlist.name,
                                                            items = songs.map { it.song.toMediaItem() },
                                                            startIndex = index,
                                                        ),
                                                    )
                                                }
                                            } else {
                                                songWrapper.isSelected = !songWrapper.isSelected
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = songWrapper.item.song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ),
                            )
                        }

                        if (locked) {
                            content()
                        } else {
                            SwipeToDismissBox(
                                state = dismissBoxState,
                                backgroundContent = {},
                            ) {
                                content()
                            }
                        }
                    }
                }
            }
        }

        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showTopBarTitle) AutoResizeText(
                        text = playlist?.playlist?.name.orEmpty(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSizeRange = FontSizeRange(16.sp, 22.sp)
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = navController::navigateUp,
                    onLongClick = navController::backToMain,
                ) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                    )
                }
            },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
            Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter),
        )
    }
}
fun textNoAccentsOrPunctMark(text: String): String {
    var temp: String = text
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val transliterator = Transliterator.getInstance("Russian-Latin/BGN")
        temp = transliterator
            .transliterate(text)
    }
    return temp
        .lowercase()
        .replace("á", "a")
        .replace("à", "a")
        .replace("ä", "a")
        .replace("é", "e")
        .replace("è", "e")
        .replace("ê", "e")
        .replace("ë", "e")
        .replace("í", "i")
        .replace("î", "i")
        .replace("ï", "i")
        .replace("ó", "o")
        .replace("ô", "o")
        .replace("ö", "o")
        .replace("ú", "u")
        .replace("û", "u")
        .replace("ü", "u")
        .replace("ç", "c")
        .replace("[\\s\\p{P}]".toRegex(), "")
}