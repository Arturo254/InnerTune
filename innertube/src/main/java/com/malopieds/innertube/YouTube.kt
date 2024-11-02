package com.malopieds.innertube

import com.malopieds.innertube.models.AccountInfo
import com.malopieds.innertube.models.AlbumItem
import com.malopieds.innertube.models.Artist
import com.malopieds.innertube.models.ArtistItem
import com.malopieds.innertube.models.BrowseEndpoint
import com.malopieds.innertube.models.GridRenderer
import com.malopieds.innertube.models.MusicResponsiveListItemRenderer
import com.malopieds.innertube.models.MusicTwoRowItemRenderer
import com.malopieds.innertube.models.PlaylistItem
import com.malopieds.innertube.models.SearchSuggestions
import com.malopieds.innertube.models.SongItem
import com.malopieds.innertube.models.WatchEndpoint
import com.malopieds.innertube.models.WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig.Companion.MUSIC_VIDEO_TYPE_ATV
import com.malopieds.innertube.models.YouTubeClient.Companion.IOS
import com.malopieds.innertube.models.YouTubeClient.Companion.TVHTML5
import com.malopieds.innertube.models.YouTubeClient.Companion.WEB
import com.malopieds.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.malopieds.innertube.models.YouTubeLocale
import com.malopieds.innertube.models.getContinuation
import com.malopieds.innertube.models.oddElements
import com.malopieds.innertube.models.response.AccountMenuResponse
import com.malopieds.innertube.models.response.BrowseResponse
import com.malopieds.innertube.models.response.GetQueueResponse
import com.malopieds.innertube.models.response.GetSearchSuggestionsResponse
import com.malopieds.innertube.models.response.GetTranscriptResponse
import com.malopieds.innertube.models.response.NextResponse
import com.malopieds.innertube.models.response.PipedResponse
import com.malopieds.innertube.models.response.PlayerResponse
import com.malopieds.innertube.models.response.SearchResponse
import com.malopieds.innertube.pages.AlbumPage
import com.malopieds.innertube.pages.AlbumUtils
import com.malopieds.innertube.pages.ArtistItemsContinuationPage
import com.malopieds.innertube.pages.ArtistItemsPage
import com.malopieds.innertube.pages.ArtistPage
import com.malopieds.innertube.pages.BrowseResult
import com.malopieds.innertube.pages.ExplorePage
import com.malopieds.innertube.pages.HomeAlbumRecommendation
import com.malopieds.innertube.pages.HomePlayList
import com.malopieds.innertube.pages.MoodAndGenres
import com.malopieds.innertube.pages.NewReleaseAlbumPage
import com.malopieds.innertube.pages.NextPage
import com.malopieds.innertube.pages.NextResult
import com.malopieds.innertube.pages.PlaylistContinuationPage
import com.malopieds.innertube.pages.PlaylistPage
import com.malopieds.innertube.pages.RecommendationAlbumBundle
import com.malopieds.innertube.pages.RelatedPage
import com.malopieds.innertube.pages.SearchPage
import com.malopieds.innertube.pages.SearchResult
import com.malopieds.innertube.pages.SearchSuggestionPage
import com.malopieds.innertube.pages.SearchSummary
import com.malopieds.innertube.pages.SearchSummaryPage
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.net.Proxy

/**
 * Parse useful data with [InnerTube] sending requests.
 * Modified from [ViMusic](https://github.com/vfsfitvnm/ViMusic)
 */

object YouTube {
    private val innerTube = InnerTube()

    var locale: YouTubeLocale
        get() = innerTube.locale
        set(value) {
            innerTube.locale = value
        }
    var visitorData: String
        get() = innerTube.visitorData
        set(value) {
            innerTube.visitorData = value
        }
    var cookie: String?
        get() = innerTube.cookie
        set(value) {
            innerTube.cookie = value
        }
    var proxy: Proxy?
        get() = innerTube.proxy
        set(value) {
            innerTube.proxy = value
        }

    suspend fun searchSuggestions(query: String): Result<SearchSuggestions> =
        runCatching {
            val response = innerTube.getSearchSuggestions(WEB_REMIX, query).body<GetSearchSuggestionsResponse>()
            SearchSuggestions(
                queries =
                    response.contents
                        ?.getOrNull(0)
                        ?.searchSuggestionsSectionRenderer
                        ?.contents
                        ?.mapNotNull { content ->
                            content.searchSuggestionRenderer
                                ?.suggestion
                                ?.runs
                                ?.joinToString(separator = "") { it.text }
                        }.orEmpty(),
                recommendedItems =
                    response.contents
                        ?.getOrNull(1)
                        ?.searchSuggestionsSectionRenderer
                        ?.contents
                        ?.mapNotNull {
                            it.musicResponsiveListItemRenderer?.let { renderer ->
                                SearchSuggestionPage.fromMusicResponsiveListItemRenderer(renderer)
                            }
                        }.orEmpty(),
            )
        }

    suspend fun searchSummary(query: String): Result<SearchSummaryPage> =
        runCatching {
            val response = innerTube.search(WEB_REMIX, query).body<SearchResponse>()
            SearchSummaryPage(
                summaries =
                    response.contents
                        ?.tabbedSearchResultsRenderer
                        ?.tabs
                        ?.firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents
                        ?.mapNotNull { it ->
                            if (it.musicCardShelfRenderer != null) {
                                SearchSummary(
                                    title =
                                        it.musicCardShelfRenderer.header.musicCardShelfHeaderBasicRenderer.title.runs
                                            ?.firstOrNull()
                                            ?.text
                                            ?: return@mapNotNull null,
                                    items =
                                        listOfNotNull(SearchSummaryPage.fromMusicCardShelfRenderer(it.musicCardShelfRenderer))
                                            .plus(
                                                it.musicCardShelfRenderer.contents
                                                    ?.mapNotNull { it.musicResponsiveListItemRenderer }
                                                    ?.mapNotNull(SearchSummaryPage.Companion::fromMusicResponsiveListItemRenderer)
                                                    .orEmpty(),
                                            ).distinctBy { it.id }
                                            .ifEmpty { null } ?: return@mapNotNull null,
                                )
                            } else {
                                SearchSummary(
                                    title =
                                        it.musicShelfRenderer
                                            ?.title
                                            ?.runs
                                            ?.firstOrNull()
                                            ?.text ?: return@mapNotNull null,
                                    items =
                                        it.musicShelfRenderer.contents
                                            ?.mapNotNull {
                                                SearchSummaryPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                                            }?.distinctBy { it.id }
                                            ?.ifEmpty { null } ?: return@mapNotNull null,
                                )
                            }
                        }!!,
            )
        }

    suspend fun search(
        query: String,
        filter: SearchFilter,
    ): Result<SearchResult> =
        runCatching {
            val response = innerTube.search(WEB_REMIX, query, filter.value).body<SearchResponse>()
            SearchResult(
                items =
                    response.contents
                        ?.tabbedSearchResultsRenderer
                        ?.tabs
                        ?.firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents
                        ?.lastOrNull()
                        ?.musicShelfRenderer
                        ?.contents
                        ?.mapNotNull {
                            SearchPage.toYTItem(it.musicResponsiveListItemRenderer)
                        }.orEmpty(),
                continuation =
                    response.contents
                        ?.tabbedSearchResultsRenderer
                        ?.tabs
                        ?.firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents
                        ?.lastOrNull()
                        ?.musicShelfRenderer
                        ?.continuations
                        ?.getContinuation(),
            )
        }

    suspend fun searchContinuation(continuation: String): Result<SearchResult> =
        runCatching {
            val response = innerTube.search(WEB_REMIX, continuation = continuation).body<SearchResponse>()
            SearchResult(
                items =
                    response.continuationContents
                        ?.musicShelfContinuation
                        ?.contents
                        ?.mapNotNull {
                            SearchPage.toYTItem(it.musicResponsiveListItemRenderer)
                        }!!,
                continuation =
                    response.continuationContents.musicShelfContinuation.continuations
                        ?.getContinuation(),
            )
        }

    suspend fun album(
        browseId: String,
        withSongs: Boolean = true,
    ): Result<AlbumPage> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, browseId).body<BrowseResponse>()
            val playlistId =
                response.microformat
                    ?.microformatDataRenderer
                    ?.urlCanonical
                    ?.substringAfterLast('=')
                    ?: response.contents
                        ?.twoColumnBrowseResultsRenderer
                        ?.secondaryContents
                        ?.sectionListRenderer
                        ?.contents
                        ?.firstOrNull()
                        ?.musicPlaylistShelfRenderer
                        ?.playlistId!!
            AlbumPage(
                album =
                    AlbumItem(
                        browseId = browseId,
                        playlistId = playlistId,
                        title =
                            response.contents
                                ?.twoColumnBrowseResultsRenderer
                                ?.tabs
                                ?.firstOrNull()
                                ?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                                ?.contents
                                ?.firstOrNull()
                                ?.musicResponsiveHeaderRenderer
                                ?.title
                                ?.runs
                                ?.firstOrNull()
                                ?.text!!,
                        artists =
                            response.contents.twoColumnBrowseResultsRenderer.tabs
                                .firstOrNull()
                                ?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                                ?.contents
                                ?.firstOrNull()
                                ?.musicResponsiveHeaderRenderer
                                ?.straplineTextOne
                                ?.runs
                                ?.oddElements()
                                ?.map {
                                    Artist(
                                        name = it.text,
                                        id =
                                            it.navigationEndpoint
                                                ?.browseEndpoint
                                                ?.browseId,
                                    )
                                }!!,
                        year =
                            response.contents.twoColumnBrowseResultsRenderer.tabs
                                .firstOrNull()
                                ?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                                ?.contents
                                ?.firstOrNull()
                                ?.musicResponsiveHeaderRenderer
                                ?.subtitle
                                ?.runs
                                ?.lastOrNull()
                                ?.text
                                ?.toIntOrNull(),
                        thumbnail =
                            response.contents.twoColumnBrowseResultsRenderer.tabs
                                .firstOrNull()
                                ?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                                ?.contents
                                ?.firstOrNull()
                                ?.musicResponsiveHeaderRenderer
                                ?.thumbnail
                                ?.musicThumbnailRenderer
                                ?.thumbnail
                                ?.thumbnails
                                ?.lastOrNull()
                                ?.url!!,
                        otherVersions =
                            response.contents.twoColumnBrowseResultsRenderer.secondaryContents
                                ?.sectionListRenderer
                                ?.contents
                                ?.getOrNull(
                                    1,
                                )?.musicCarouselShelfRenderer
                                ?.contents
                                ?.mapNotNull { it.musicTwoRowItemRenderer }
                                ?.mapNotNull(NewReleaseAlbumPage::fromMusicTwoRowItemRenderer)
                                .orEmpty(),
                    ),
                songs = if (withSongs) albumSongs(playlistId).getOrThrow() else emptyList(),
            )
        }

    suspend fun albumSongs(playlistId: String): Result<List<SongItem>> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, "VL$playlistId").body<BrowseResponse>()
            response.contents
                ?.twoColumnBrowseResultsRenderer
                ?.secondaryContents
                ?.sectionListRenderer
                ?.contents
                ?.firstOrNull()
                ?.musicPlaylistShelfRenderer
                ?.contents
                ?.mapNotNull {
                    AlbumPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                }!!
        }

    suspend fun artist(browseId: String): Result<ArtistPage> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, browseId).body<BrowseResponse>()
            ArtistPage(
                artist =
                    ArtistItem(
                        id = browseId,
                        title =
                            response.header
                                ?.musicImmersiveHeaderRenderer
                                ?.title
                                ?.runs
                                ?.firstOrNull()
                                ?.text
                                ?: response.header
                                    ?.musicVisualHeaderRenderer
                                    ?.title
                                    ?.runs
                                    ?.firstOrNull()
                                    ?.text!!,
                        thumbnail =
                            response.header
                                ?.musicImmersiveHeaderRenderer
                                ?.thumbnail
                                ?.musicThumbnailRenderer
                                ?.getThumbnailUrl()
                                ?: response.header
                                    ?.musicVisualHeaderRenderer
                                    ?.foregroundThumbnail
                                    ?.musicThumbnailRenderer
                                    ?.getThumbnailUrl()!!,
                        shuffleEndpoint =
                            response.header
                                ?.musicImmersiveHeaderRenderer
                                ?.playButton
                                ?.buttonRenderer
                                ?.navigationEndpoint
                                ?.watchEndpoint,
                        radioEndpoint =
                            response.header
                                ?.musicImmersiveHeaderRenderer
                                ?.startRadioButton
                                ?.buttonRenderer
                                ?.navigationEndpoint
                                ?.watchEndpoint,
                    ),
                sections =
                    response.contents
                        ?.singleColumnBrowseResultsRenderer
                        ?.tabs
                        ?.firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents
                        ?.mapNotNull(ArtistPage::fromSectionListRendererContent)!!,
                description =
                    response.header
                        ?.musicImmersiveHeaderRenderer
                        ?.description
                        ?.runs
                        ?.firstOrNull()
                        ?.text,
            )
        }

    suspend fun artistItems(endpoint: BrowseEndpoint): Result<ArtistItemsPage> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, endpoint.browseId, endpoint.params).body<BrowseResponse>()
            val gridRenderer =
                response.contents
                    ?.singleColumnBrowseResultsRenderer
                    ?.tabs
                    ?.firstOrNull()
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.contents
                    ?.firstOrNull()
                    ?.gridRenderer
            if (gridRenderer != null) {
                ArtistItemsPage(
                    title =
                        gridRenderer.header
                            ?.gridHeaderRenderer
                            ?.title
                            ?.runs
                            ?.firstOrNull()
                            ?.text
                            .orEmpty(),
                    items =
                        gridRenderer.items.mapNotNull {
                            it.musicTwoRowItemRenderer?.let { renderer ->
                                ArtistItemsPage.fromMusicTwoRowItemRenderer(renderer)
                            }
                        },
                    continuation = null,
                )
            } else {
                ArtistItemsPage(
                    title =
                        response.header
                            ?.musicHeaderRenderer
                            ?.title
                            ?.runs
                            ?.firstOrNull()
                            ?.text!!,
                    items =
                        response.contents
                            ?.singleColumnBrowseResultsRenderer
                            ?.tabs
                            ?.firstOrNull()
                            ?.tabRenderer
                            ?.content
                            ?.sectionListRenderer
                            ?.contents
                            ?.firstOrNull()
                            ?.musicPlaylistShelfRenderer
                            ?.contents
                            ?.mapNotNull {
                                ArtistItemsPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                            }!!,
                    continuation =
                        response.contents.singleColumnBrowseResultsRenderer.tabs
                            .firstOrNull()
                            ?.tabRenderer
                            ?.content
                            ?.sectionListRenderer
                            ?.contents
                            ?.firstOrNull()
                            ?.musicPlaylistShelfRenderer
                            ?.continuations
                            ?.getContinuation(),
                )
            }
        }

    suspend fun artistItemsContinuation(continuation: String): Result<ArtistItemsContinuationPage> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, continuation = continuation).body<BrowseResponse>()
            ArtistItemsContinuationPage(
                items =
                    response.continuationContents?.musicPlaylistShelfContinuation?.contents?.mapNotNull {
                        ArtistItemsContinuationPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                    }!!,
                continuation =
                    response.continuationContents.musicPlaylistShelfContinuation.continuations
                        ?.getContinuation(),
            )
        }

    suspend fun playlist(playlistId: String): Result<PlaylistPage> =
        runCatching {
            val response =
                innerTube
                    .browse(
                        client = WEB_REMIX,
                        browseId = "VL$playlistId",
                        setLogin = true,
                    ).body<BrowseResponse>()
            val tabsStart =
                response.contents
                    ?.twoColumnBrowseResultsRenderer
                    ?.tabs
                    ?.firstOrNull()
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.contents
                    ?.firstOrNull()
            val base =
                tabsStart?.musicResponsiveHeaderRenderer
                    ?: tabsStart?.musicEditablePlaylistDetailHeaderRenderer?.header?.musicResponsiveHeaderRenderer
            PlaylistPage(
                playlist =
                    PlaylistItem(
                        id = playlistId,
                        title =
                            base
                                ?.title
                                ?.runs
                                ?.firstOrNull()
                                ?.text!!,
                        author =
                            base.straplineTextOne?.runs?.firstOrNull()?.let {
                                Artist(
                                    name = it.text,
                                    id = it.navigationEndpoint?.browseEndpoint?.browseId,
                                )
                            },
                        songCountText =
                            base.secondSubtitle
                                ?.runs
                                ?.firstOrNull()
                                ?.text,
                        thumbnail =
                            base.thumbnail
                                ?.musicThumbnailRenderer
                                ?.thumbnail
                                ?.thumbnails
                                ?.lastOrNull()
                                ?.url!!,
                        playEndpoint = null,
                        shuffleEndpoint =
                            base.buttons
                                ?.lastOrNull()
                                ?.menuRenderer
                                ?.items
                                ?.firstOrNull()
                                ?.menuNavigationItemRenderer
                                ?.navigationEndpoint
                                ?.watchPlaylistEndpoint!!,
                        radioEndpoint =
                            base.buttons
                                .lastOrNull()
                                ?.menuRenderer
                                ?.items!!
                                .find {
                                    it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
                                }?.menuNavigationItemRenderer
                                ?.navigationEndpoint
                                ?.watchPlaylistEndpoint!!,
                    ),
                songs =
                    response.contents
                        ?.twoColumnBrowseResultsRenderer
                        ?.secondaryContents
                        ?.sectionListRenderer
                        ?.contents
                        ?.firstOrNull()
                        ?.musicPlaylistShelfRenderer
                        ?.contents
                        ?.mapNotNull {
                            PlaylistPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                        }!!,
                songsContinuation =
                    response.contents.twoColumnBrowseResultsRenderer.secondaryContents.sectionListRenderer
                        .contents
                        .firstOrNull()
                        ?.musicPlaylistShelfRenderer
                        ?.continuations
                        ?.getContinuation(),
                continuation =
                    response.contents.twoColumnBrowseResultsRenderer.secondaryContents.sectionListRenderer
                        .continuations
                        ?.getContinuation(),
            )
        }

    suspend fun playlistContinuation(continuation: String) =
        runCatching {
            val response =
                innerTube
                    .browse(
                        client = WEB_REMIX,
                        continuation = continuation,
                        setLogin = true,
                    ).body<BrowseResponse>()
            PlaylistContinuationPage(
                songs =
                    response.continuationContents?.musicPlaylistShelfContinuation?.contents?.mapNotNull {
                        PlaylistPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                    }!!,
                continuation =
                    response.continuationContents.musicPlaylistShelfContinuation.continuations
                        ?.getContinuation(),
            )
        }

    suspend fun explore(): Result<ExplorePage> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, browseId = "FEmusic_explore").body<BrowseResponse>()
            ExplorePage(
                newReleaseAlbums =
                    newReleaseAlbums().getOrElse {
                        response.contents
                            ?.singleColumnBrowseResultsRenderer
                            ?.tabs
                            ?.firstOrNull()
                            ?.tabRenderer
                            ?.content
                            ?.sectionListRenderer
                            ?.contents
                            ?.find {
                                it.musicCarouselShelfRenderer
                                    ?.header
                                    ?.musicCarouselShelfBasicHeaderRenderer
                                    ?.moreContentButton
                                    ?.buttonRenderer
                                    ?.navigationEndpoint
                                    ?.browseEndpoint
                                    ?.browseId ==
                                    "FEmusic_new_releases_albums"
                            }?.musicCarouselShelfRenderer
                            ?.contents
                            ?.mapNotNull { it.musicTwoRowItemRenderer }
                            ?.mapNotNull(NewReleaseAlbumPage::fromMusicTwoRowItemRenderer)
                            .orEmpty()
                    },
                moodAndGenres =
                    response.contents
                        ?.singleColumnBrowseResultsRenderer
                        ?.tabs
                        ?.firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents
                        ?.find {
                            it.musicCarouselShelfRenderer
                                ?.header
                                ?.musicCarouselShelfBasicHeaderRenderer
                                ?.moreContentButton
                                ?.buttonRenderer
                                ?.navigationEndpoint
                                ?.browseEndpoint
                                ?.browseId ==
                                "FEmusic_moods_and_genres"
                        }?.musicCarouselShelfRenderer
                        ?.contents
                        ?.mapNotNull { it.musicNavigationButtonRenderer }
                        ?.mapNotNull(MoodAndGenres.Companion::fromMusicNavigationButtonRenderer)
                        .orEmpty(),
            )
        }

    suspend fun newReleaseAlbums(): Result<List<AlbumItem>> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, browseId = "FEmusic_new_releases_albums").body<BrowseResponse>()
            response.contents
                ?.singleColumnBrowseResultsRenderer
                ?.tabs
                ?.firstOrNull()
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.contents
                ?.firstOrNull()
                ?.gridRenderer
                ?.items
                ?.mapNotNull { it.musicTwoRowItemRenderer }
                ?.mapNotNull(NewReleaseAlbumPage::fromMusicTwoRowItemRenderer)
                .orEmpty()
        }

    suspend fun moodAndGenres(): Result<List<MoodAndGenres>> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, browseId = "FEmusic_moods_and_genres").body<BrowseResponse>()
            response.contents
                ?.singleColumnBrowseResultsRenderer
                ?.tabs
                ?.firstOrNull()
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.contents!!
                .mapNotNull(MoodAndGenres.Companion::fromSectionListRendererContent)
        }

    suspend fun recommendAlbum(
        browseId: String,
        albumUtils: AlbumUtils,
    ): Result<HomeAlbumRecommendation> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, browseId = browseId).body<BrowseResponse>()
            HomeAlbumRecommendation(
                albums =
                    RecommendationAlbumBundle(
                        recommendedAlbum = albumUtils,
                        recommendationAlbum =
                            response.contents
                                ?.sectionListRenderer
                                ?.contents
                                ?.getOrNull(1)
                                ?.musicCarouselShelfRenderer
                                ?.contents!!
                                .mapNotNull { it.musicTwoRowItemRenderer }
                                .mapNotNull {
                                    ArtistItemsPage.fromMusicTwoRowItemRenderer(it) as? PlaylistItem
                                },
                    ),
            )
        }

    suspend fun home(): Result<List<HomePlayList>> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, browseId = "FEmusic_home").body<BrowseResponse>()
            val continuation =
                response.contents
                    ?.singleColumnBrowseResultsRenderer
                    ?.tabs
                    ?.firstOrNull()
                    ?.tabRenderer
                    ?.content
                    ?.sectionListRenderer
                    ?.continuations
                    ?.firstOrNull()
                    ?.nextContinuationData
                    ?.continuation
            response.contents
                ?.singleColumnBrowseResultsRenderer
                ?.tabs
                ?.firstOrNull()
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.contents!!
                .mapNotNull { it.musicCarouselShelfRenderer }
                .map {
                    HomePlayList.fromMusicCarouselShelfRenderer(it, continuation)
                }
        }

    suspend fun browse(
        browseId: String,
        params: String?,
    ): Result<BrowseResult> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, browseId = browseId, params = params).body<BrowseResponse>()
            BrowseResult(
                title =
                    response.header
                        ?.musicHeaderRenderer
                        ?.title
                        ?.runs
                        ?.firstOrNull()
                        ?.text,
                items =
                    response.contents
                        ?.singleColumnBrowseResultsRenderer
                        ?.tabs
                        ?.firstOrNull()
                        ?.tabRenderer
                        ?.content
                        ?.sectionListRenderer
                        ?.contents
                        ?.mapNotNull { content ->
                            when {
                                content.gridRenderer != null -> {
                                    BrowseResult.Item(
                                        title =
                                            content.gridRenderer.header
                                                ?.gridHeaderRenderer
                                                ?.title
                                                ?.runs
                                                ?.firstOrNull()
                                                ?.text,
                                        items =
                                            content.gridRenderer.items
                                                .mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
                                                .mapNotNull(RelatedPage.Companion::fromMusicTwoRowItemRenderer),
                                    )
                                }

                                content.musicCarouselShelfRenderer != null -> {
                                    BrowseResult.Item(
                                        title =
                                            content.musicCarouselShelfRenderer.header
                                                ?.musicCarouselShelfBasicHeaderRenderer
                                                ?.title
                                                ?.runs
                                                ?.firstOrNull()
                                                ?.text,
                                        items =
                                            content.musicCarouselShelfRenderer.contents
                                                .mapNotNull { content2 ->
                                                    val renderer =
                                                        content2.musicTwoRowItemRenderer ?: content2.musicResponsiveListItemRenderer
                                                    renderer?.let {
                                                        when (renderer) {
                                                            is MusicTwoRowItemRenderer -> RelatedPage.fromMusicTwoRowItemRenderer(renderer)
                                                            is MusicResponsiveListItemRenderer ->
                                                                SearchSummaryPage.fromMusicResponsiveListItemRenderer(
                                                                    renderer,
                                                                )
                                                            else -> null // Handle other cases if necessary
                                                        }
                                                    }
                                                },
                                    )
                                }

                                else -> null
                            }
                        }.orEmpty(),
            )
        }

    suspend fun browseContinuation(continuation: String): Result<List<HomePlayList>> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, continuation = continuation).body<BrowseResponse>()
            val newContinuation =
                response.continuationContents
                    ?.sectionListContinuation
                    ?.continuations
                    ?.firstOrNull()
                    ?.nextContinuationData
                    ?.continuation
            response.continuationContents
                ?.sectionListContinuation
                ?.contents!!
                .mapNotNull { it.musicCarouselShelfRenderer }
                .map {
                    HomePlayList.fromMusicCarouselShelfRenderer(it, newContinuation)
                }
        }

    suspend fun likedPlaylists(): Result<List<PlaylistItem>> =
        runCatching {
            val response =
                innerTube
                    .browse(
                        client = WEB_REMIX,
                        browseId = "FEmusic_liked_playlists",
                        setLogin = true,
                    ).body<BrowseResponse>()
            response.contents
                ?.singleColumnBrowseResultsRenderer
                ?.tabs
                ?.firstOrNull()
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.contents
                ?.firstOrNull()
                ?.gridRenderer
                ?.items!!
                .drop(1) // the first item is "create new playlist"
                .mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
                .mapNotNull {
                    ArtistItemsPage.fromMusicTwoRowItemRenderer(it) as? PlaylistItem
                }
        }

    suspend fun player(
        videoId: String,
        playlistId: String? = null,
    ): Result<PlayerResponse> =
        runCatching {
            val playerResponse = innerTube.player(IOS, videoId, playlistId).body<PlayerResponse>()
            if (playerResponse.playabilityStatus.status == "OK") {
                return@runCatching playerResponse
            }
            val safePlayerResponse = innerTube.player(TVHTML5, videoId, playlistId).body<PlayerResponse>()
            if (safePlayerResponse.playabilityStatus.status != "OK") {
                return@runCatching playerResponse
            }
            val audioStreams = innerTube.pipedStreams(videoId).body<PipedResponse>().audioStreams
            safePlayerResponse.copy(
                streamingData =
                    safePlayerResponse.streamingData?.copy(
                        adaptiveFormats =
                            safePlayerResponse.streamingData.adaptiveFormats.mapNotNull { adaptiveFormat ->
                                audioStreams.find { it.bitrate == adaptiveFormat.bitrate }?.let {
                                    adaptiveFormat.copy(
                                        url = it.url,
                                    )
                                }
                            },
                    ),
            )
        }

    suspend fun next(
        endpoint: WatchEndpoint,
        continuation: String? = null,
    ): Result<NextResult> =
        runCatching {
            val response =
                innerTube
                    .next(
                        WEB_REMIX,
                        endpoint.videoId,
                        endpoint.playlistId,
                        endpoint.playlistSetVideoId,
                        endpoint.index,
                        endpoint.params,
                        continuation,
                    ).body<NextResponse>()
            val playlistPanelRenderer =
                response.continuationContents?.playlistPanelContinuation
                    ?: response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[0]
                        .tabRenderer.content
                        ?.musicQueueRenderer
                        ?.content
                        ?.playlistPanelRenderer!!
            // load automix items
            playlistPanelRenderer.contents
                .lastOrNull()
                ?.automixPreviewVideoRenderer
                ?.content
                ?.automixPlaylistVideoRenderer
                ?.navigationEndpoint
                ?.watchPlaylistEndpoint
                ?.let { watchPlaylistEndpoint ->
                    return@runCatching next(watchPlaylistEndpoint).getOrThrow().let { result ->
                        result.copy(
                            title = playlistPanelRenderer.title,
                            items =
                                playlistPanelRenderer.contents.mapNotNull {
                                    it.playlistPanelVideoRenderer?.let { renderer ->
                                        NextPage.fromPlaylistPanelVideoRenderer(renderer)
                                    }
                                } + result.items,
                            lyricsEndpoint =
                                response.contents
                                    .singleColumnMusicWatchNextResultsRenderer
                                    .tabbedRenderer
                                    .watchNextTabbedResultsRenderer
                                    .tabs
                                    .getOrNull(
                                        1,
                                    )?.tabRenderer
                                    ?.endpoint
                                    ?.browseEndpoint,
                            relatedEndpoint =
                                response.contents
                                    .singleColumnMusicWatchNextResultsRenderer
                                    .tabbedRenderer
                                    .watchNextTabbedResultsRenderer
                                    .tabs
                                    .getOrNull(
                                        2,
                                    )?.tabRenderer
                                    ?.endpoint
                                    ?.browseEndpoint,
                            currentIndex = playlistPanelRenderer.currentIndex,
                            endpoint = watchPlaylistEndpoint,
                        )
                    }
                }
            NextResult(
                title = playlistPanelRenderer.title,
                items =
                    playlistPanelRenderer.contents.mapNotNull {
                        it.playlistPanelVideoRenderer?.let(NextPage::fromPlaylistPanelVideoRenderer)
                    },
                currentIndex = playlistPanelRenderer.currentIndex,
                lyricsEndpoint =
                    response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs
                        .getOrNull(
                            1,
                        )?.tabRenderer
                        ?.endpoint
                        ?.browseEndpoint,
                relatedEndpoint =
                    response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs
                        .getOrNull(
                            2,
                        )?.tabRenderer
                        ?.endpoint
                        ?.browseEndpoint,
                continuation = playlistPanelRenderer.continuations?.getContinuation(),
                endpoint = endpoint,
            )
        }

    suspend fun lyrics(endpoint: BrowseEndpoint): Result<String?> =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, endpoint.browseId, endpoint.params).body<BrowseResponse>()
            response.contents
                ?.sectionListRenderer
                ?.contents
                ?.firstOrNull()
                ?.musicDescriptionShelfRenderer
                ?.description
                ?.runs
                ?.firstOrNull()
                ?.text
        }

    suspend fun related(endpoint: BrowseEndpoint) =
        runCatching {
            val response = innerTube.browse(WEB_REMIX, endpoint.browseId).body<BrowseResponse>()
            val songs = mutableListOf<SongItem>()
            val albums = mutableListOf<AlbumItem>()
            val artists = mutableListOf<ArtistItem>()
            val playlists = mutableListOf<PlaylistItem>()
            response.contents?.sectionListRenderer?.contents?.forEach { sectionContent ->
                sectionContent.musicCarouselShelfRenderer?.contents?.forEach { content ->
                    when (
                        val item =
                            content.musicResponsiveListItemRenderer?.let(RelatedPage.Companion::fromMusicResponsiveListItemRenderer)
                                ?: content.musicTwoRowItemRenderer?.let(RelatedPage.Companion::fromMusicTwoRowItemRenderer)
                    ) {
                        is SongItem ->
                            if (content.musicResponsiveListItemRenderer
                                    ?.overlay
                                    ?.musicItemThumbnailOverlayRenderer
                                    ?.content
                                    ?.musicPlayButtonRenderer
                                    ?.playNavigationEndpoint
                                    ?.watchEndpoint
                                    ?.watchEndpointMusicSupportedConfigs
                                    ?.watchEndpointMusicConfig
                                    ?.musicVideoType == MUSIC_VIDEO_TYPE_ATV
                            ) {
                                songs.add(item)
                            }

                        is AlbumItem -> albums.add(item)
                        is ArtistItem -> artists.add(item)
                        is PlaylistItem -> playlists.add(item)
                        else -> {}
                    }
                }
            }
            RelatedPage(songs, albums, artists, playlists)
        }

    suspend fun queue(
        videoIds: List<String>? = null,
        playlistId: String? = null,
    ): Result<List<SongItem>> =
        runCatching {
            if (videoIds != null) {
                assert(videoIds.size <= MAX_GET_QUEUE_SIZE) // Max video limit
            }
            innerTube
                .getQueue(WEB_REMIX, videoIds, playlistId)
                .body<GetQueueResponse>()
                .queueDatas
                .mapNotNull {
                    it.content.playlistPanelVideoRenderer?.let { renderer ->
                        NextPage.fromPlaylistPanelVideoRenderer(renderer)
                    }
                }
        }

    suspend fun transcript(videoId: String): Result<String> =
        runCatching {
            val response = innerTube.getTranscript(WEB, videoId).body<GetTranscriptResponse>()
            response.actions
                ?.firstOrNull()
                ?.updateEngagementPanelAction
                ?.content
                ?.transcriptRenderer
                ?.body
                ?.transcriptBodyRenderer
                ?.cueGroups
                ?.joinToString(
                    separator = "\n",
                ) { group ->
                    val time =
                        group.transcriptCueGroupRenderer.cues[0]
                            .transcriptCueRenderer.startOffsetMs
                    val text =
                        group.transcriptCueGroupRenderer.cues[0]
                            .transcriptCueRenderer.cue.simpleText
                            .trim('♪')
                            .trim(' ')
                    "[%02d:%02d.%03d]$text".format(time / 60000, (time / 1000) % 60, time % 1000)
                }!!
        }

    suspend fun visitorData(): Result<String> =
        runCatching {
            Json
                .parseToJsonElement(innerTube.getSwJsData().bodyAsText().substring(5))
                .jsonArray[0]
                .jsonArray[2]
                .jsonArray
                .first { (it as? JsonPrimitive)?.content?.startsWith(VISITOR_DATA_PREFIX) == true }
                .jsonPrimitive.content
        }

    suspend fun accountInfo(): Result<AccountInfo> =
        runCatching {
            innerTube
                .accountMenu(WEB_REMIX)
                .body<AccountMenuResponse>()
                .actions[0]
                .openPopupAction.popup.multiPageMenuRenderer
                .header
                ?.activeAccountHeaderRenderer
                ?.toAccountInfo()!!
        }

    @JvmInline
    value class SearchFilter(
        val value: String,
    ) {
        companion object {
            val FILTER_SONG = SearchFilter("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
            val FILTER_VIDEO = SearchFilter("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D")
            val FILTER_ALBUM = SearchFilter("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D")
            val FILTER_ARTIST = SearchFilter("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D")
            val FILTER_FEATURED_PLAYLIST = SearchFilter("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D")
            val FILTER_COMMUNITY_PLAYLIST = SearchFilter("EgeKAQQoAEABagoQAxAEEAoQCRAF")
        }
    }

    const val MAX_GET_QUEUE_SIZE = 1000

    private const val VISITOR_DATA_PREFIX = "Cgt"

    const val DEFAULT_VISITOR_DATA = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"
}
