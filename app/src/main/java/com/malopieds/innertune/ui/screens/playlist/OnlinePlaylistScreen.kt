package com.malopieds.innertune.ui.screens.playlist

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.malopieds.innertube.models.SongItem
import com.malopieds.innertube.models.WatchEndpoint
import com.malopieds.innertune.LocalDatabase
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.LocalPlayerConnection
import com.malopieds.innertune.R
import com.malopieds.innertune.constants.AlbumThumbnailSize
import com.malopieds.innertune.constants.ThumbnailCornerRadius
import com.malopieds.innertune.db.entities.PlaylistEntity
import com.malopieds.innertune.db.entities.PlaylistSongMap
import com.malopieds.innertune.extensions.metadata
import com.malopieds.innertune.extensions.toMediaItem
import com.malopieds.innertune.extensions.togglePlayPause
import com.malopieds.innertune.models.toMediaMetadata
import com.malopieds.innertune.playback.queues.YouTubeQueue
import com.malopieds.innertune.ui.component.AutoResizeText
import com.malopieds.innertune.ui.component.FontSizeRange
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.component.LocalMenuState
import com.malopieds.innertune.ui.component.YouTubeListItem
import com.malopieds.innertune.ui.component.shimmer.ButtonPlaceholder
import com.malopieds.innertune.ui.component.shimmer.ListItemPlaceHolder
import com.malopieds.innertune.ui.component.shimmer.ShimmerHost
import com.malopieds.innertune.ui.component.shimmer.TextPlaceholder
import com.malopieds.innertune.ui.menu.SelectionMediaMetadataMenu
import com.malopieds.innertune.ui.menu.YouTubePlaylistMenu
import com.malopieds.innertune.ui.menu.YouTubeSongMenu
import com.malopieds.innertune.ui.utils.ItemWrapper
import com.malopieds.innertune.ui.utils.backToMain
import com.malopieds.innertune.viewmodels.OnlinePlaylistViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlinePlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: OnlinePlaylistViewModel = hiltViewModel(),
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

    val wrappedSongs = songs.map { item -> ItemWrapper(item) }.toMutableList()
    var searchQuery by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var selection by remember {
        mutableStateOf(false)
    }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val showTopBarTitle by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
        ) {
            playlist.let { playlist ->
                if (playlist != null) {
                    item {
                        Column(
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(
                                    model = playlist.thumbnail,
                                    contentDescription = null,
                                    modifier =
                                    Modifier
                                        .size(AlbumThumbnailSize)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                                )

                                Spacer(Modifier.width(16.dp))

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    AutoResizeText(
                                        text = playlist.title,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSizeRange = FontSizeRange(16.sp, 22.sp),
                                    )

                                    playlist.author?.let { artist ->
                                        val annotatedString =
                                            buildAnnotatedString {
                                                withStyle(
                                                    style =
                                                    MaterialTheme.typography.titleMedium
                                                        .copy(
                                                            fontWeight = FontWeight.Normal,
                                                            color = MaterialTheme.colorScheme.onBackground,
                                                        ).toSpanStyle(),
                                                ) {
                                                    if (artist.id != null) {
                                                        pushStringAnnotation(artist.id!!, artist.name)
                                                        append(artist.name)
                                                        pop()
                                                    } else {
                                                        append(artist.name)
                                                    }
                                                }
                                            }
                                        ClickableText(annotatedString) { offset ->
                                            annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { range ->
                                                navController.navigate("artist/${range.tag}")
                                            }
                                        }
                                    }

                                    playlist.songCountText?.let { songCountText ->
                                        Text(
                                            text = songCountText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Normal,
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                database.transaction {
                                                    val playlistEntity =
                                                        PlaylistEntity(
                                                            name = playlist.title,
                                                            browseId = playlist.id,
                                                        )
                                                    insert(playlistEntity)
                                                    songs
                                                        .map(SongItem::toMediaMetadata)
                                                        .onEach(::insert)
                                                        .mapIndexed { index, song ->
                                                            PlaylistSongMap(
                                                                songId = song.id,
                                                                playlistId = playlistEntity.id,
                                                                position = index,
                                                            )
                                                        }.forEach(::insert)
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(context.getString(R.string.playlist_imported))
                                                    }
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.input),
                                                contentDescription = null,
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    YouTubePlaylistMenu(
                                                        playlist = playlist,
                                                        songs = songs,
                                                        coroutineScope = coroutineScope,
                                                        onDismiss = menuState::dismiss,
                                                        selectAction = { selection = true },
                                                        canSelect = true,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        playerConnection.playQueue(YouTubeQueue(playlist.shuffleEndpoint))
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

                                playlist.radioEndpoint?.let { radioEndpoint ->
                                    OutlinedButton(
                                        onClick = {
                                            playerConnection.playQueue(YouTubeQueue(radioEndpoint))
                                        },
                                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.radio),
                                            contentDescription = null,
                                            modifier = Modifier.size(ButtonDefaults.IconSize),
                                        )
                                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Text(stringResource(R.string.radio))
                                    }
                                }
                            }
                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(context.getString(R.string.search)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .focusRequester(focusRequester),  // Attach the FocusRequester to the TextField
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.hideSoftInputFromWindow((context as Activity).currentFocus?.windowToken, 0)
                                        focusManager.clearFocus()
                                    }
                                ),
                                shape = MaterialTheme.shapes.large,
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.search),
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchQuery = TextFieldValue("")
                                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.hideSoftInputFromWindow((context as Activity).currentFocus?.windowToken, 0)
                                        focusManager.clearFocus()
                                    }) {
                                        Icon(
                                            painterResource(R.drawable.close),
                                            contentDescription = null
                                        )
                                    }
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
                                        }else {
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
                                        wrappedSongs[0].item.toMediaItem()
                                        menuState.show {
                                            wrappedSongs
                                                .filter { it.isSelected }
                                                .map { it.item.toMediaItem().metadata!! }
                                                .let {
                                                    SelectionMediaMetadataMenu(
                                                        songSelection = it,
                                                        onDismiss = menuState::dismiss,
                                                        clearAction = { selection = false },
                                                        currentItems = emptyList(),
                                                    )
                                                }
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
                            }
                        }
                    }
                    val searchQueryStr = textNoAccentsOrPunctMark(searchQuery.text.trim())
                    val filteredSongs = if (searchQuery.text.isEmpty())
                    { wrappedSongs }
                    else{
                        wrappedSongs.filter {
                            textNoAccentsOrPunctMark(it.item.title).contains(searchQueryStr, ignoreCase = true) or
                                    textNoAccentsOrPunctMark(it.item.artists.joinToString("")).contains(searchQueryStr, ignoreCase = true)
                        }
                    }
                    items(
                        items = filteredSongs,
                    ) { song ->
                        YouTubeListItem(
                            item = song.item,
                            isActive = mediaMetadata?.id == song.item.id,
                            isPlaying = isPlaying,
                            isSelected = song.isSelected && selection,
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            YouTubeSongMenu(
                                                song = song.item,
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
                            },
                            modifier =
                            Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (!selection) {
                                            if (song.item.id == mediaMetadata?.id) {
                                                playerConnection.player.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        song.item.endpoint
                                                            ?: WatchEndpoint(videoId = song.item.id),
                                                        song.item.toMediaMetadata(),
                                                    ),
                                                )
                                            }
                                        } else {
                                            song.isSelected = !song.isSelected
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeSongMenu(
                                                song = song.item,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                ).animateItemPlacement(),
                        )
                    }
                } else {
                    item {
                        ShimmerHost {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(
                                        modifier =
                                        Modifier
                                            .size(AlbumThumbnailSize)
                                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                            .background(MaterialTheme.colorScheme.onSurface),
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        TextPlaceholder()
                                        TextPlaceholder()
                                        TextPlaceholder()
                                    }
                                }

                                Spacer(Modifier.padding(8.dp))

                                Row {
                                    ButtonPlaceholder(Modifier.weight(1f))

                                    Spacer(Modifier.width(12.dp))

                                    ButtonPlaceholder(Modifier.weight(1f))
                                }
                            }

                            repeat(6) {
                                ListItemPlaceHolder()
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
                        text = playlist?.title.orEmpty(),
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
