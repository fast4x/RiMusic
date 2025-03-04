package it.fast4x.environment

import io.ktor.client.call.body
import it.fast4x.environment.Environment.getBestQuality
import it.fast4x.environment.models.BrowseEndpoint
import it.fast4x.environment.models.BrowseResponse
import it.fast4x.environment.models.Context
import it.fast4x.environment.models.CreatePlaylistResponse
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.VideoOrSongInfo
import it.fast4x.environment.models.getContinuation
import it.fast4x.environment.models.oddElements
import it.fast4x.environment.requests.AlbumPage
import it.fast4x.environment.requests.ArtistItemsContinuationPage
import it.fast4x.environment.requests.ArtistItemsPage
import it.fast4x.environment.requests.ArtistPage
import it.fast4x.environment.requests.HistoryPage
import it.fast4x.environment.requests.HomePage
import it.fast4x.environment.requests.NewReleaseAlbumPage
import it.fast4x.environment.requests.PlaylistContinuationPage
import it.fast4x.environment.requests.PlaylistPage

object EnvironmentExt {



    const val PLAYLIST_SIZE_LIMIT = 5000

    suspend fun createPlaylist(title: String) = runCatching {
        Environment.createPlaylist(Context.DefaultWeb.client, title).body<CreatePlaylistResponse>().playlistId
    }.onFailure {
        println("YtMusic createPlaylist error: ${it.stackTraceToString()}")
    }

    suspend fun deletePlaylist(playlistId: String) = runCatching {
        Environment.deletePlaylist(Context.DefaultWeb.client, playlistId)
    }.onFailure {
        println("YtMusic deletePlaylist error: ${it.stackTraceToString()}")
    }

    suspend fun renamePlaylist(playlistId: String, name: String) = runCatching {
        Environment.renamePlaylist(Context.DefaultWeb.client, playlistId, name)
    }.onFailure {
        println("YtMusic renamePlaylist error: ${it.stackTraceToString()}")
    }

    suspend fun addToPlaylist(playlistId: String, videoId: String) = runCatching {
        Environment.addToPlaylist(Context.DefaultWeb.client, playlistId, videoId)
    }.onFailure {
        println("YtMusic addToPlaylist(single) error: ${it.stackTraceToString()}")
    }

    suspend fun addToPlaylist(playlistId: String, videoIds: List<String>) = runCatching {
        val requestedVideoIds = videoIds.take(PLAYLIST_SIZE_LIMIT)
        val difference = videoIds.size - requestedVideoIds.size
        if (difference > 0) {
            println("YtMusic addToPlaylist warning: only adding (at most) $PLAYLIST_SIZE_LIMIT ids, (surpassed limit by $difference)")
        }
        Environment.addToPlaylist(Context.DefaultWeb.client, playlistId, requestedVideoIds)
    }.onFailure {
        println("YtMusic addToPlaylist (list of size ${videoIds.size}) error: ${it.stackTraceToString()}")
    }

    suspend fun removeFromPlaylist(playlistId: String, videoId: String, setVideoId: String? = null) = runCatching {
        println("YtMusic removeFromPlaylist params: playlistId: $playlistId, videoId: $videoId, setVideoId: $setVideoId")
            Environment.removeFromPlaylist(Context.DefaultWeb.client, playlistId, videoId, setVideoId)
        }.onFailure {
            println("YtMusic removeFromPlaylist error: ${it.stackTraceToString()}")
        }

    suspend fun addPlaylistToPlaylist(playlistId: String, videoId: String) = runCatching {
        Environment.addPlaylistToPlaylist(Context.DefaultWeb.client, playlistId, videoId)
    }.onFailure {
        println("YtMusic addPlaylistToPlaylist error: ${it.stackTraceToString()}")
    }

    suspend fun removeFromPlaylist(playlistId: String, videoId: String, setVideoIds: List<String?>) = runCatching {
        Environment.removeFromPlaylist(Context.DefaultWeb.client, playlistId, videoId, setVideoIds)
    }.onFailure {
        println("YtMusic removeFromPlaylist (list of size ${setVideoIds.size}) error: ${it.stackTraceToString()}")
    }

    suspend fun subscribeChannel(channelId: String) = runCatching {
        println("YtMusic subscribeChannel channelId: $channelId")
        Environment.subscribeChannel(channelId)
    }.onFailure {
        println("YtMusic subscribeChannel error: ${it.stackTraceToString()}")
    }

    suspend fun unsubscribeChannel(channelId: String) = runCatching {
        println("YtMusic unsubscribeChannel channelId: $channelId")
        Environment.unsubscribeChannel(channelId)
    }.onFailure {
        println("YtMusic unsubscribeChannel error: ${it.stackTraceToString()}")
    }

    suspend fun likePlaylistOrAlbum(playlistId: String) = runCatching {
        println("YtMusic likePlaylistOrAlbum playlistId: $playlistId")
        Environment.likePlaylistOrAlbum(playlistId)
    }.onFailure {
        println("YtMusic likePlaylistOrAlbum error: ${it.stackTraceToString()}")
    }

    suspend fun removelikePlaylistOrAlbum(playlistId: String) = runCatching {
        println("YtMusic removelikePlaylistOrAlbum playlistId: $playlistId")
        Environment.removelikePlaylistOrAlbum(playlistId)
    }.onFailure {
        println("YtMusic removelikePlaylistOrAlbum error: ${it.stackTraceToString()}")
    }

    suspend fun likeVideoOrSong(VideoId: String) = runCatching {
        println("YtMusic likeVideoOrSong VideoId: $VideoId")
        Environment.likeVideoOrSong(VideoId)
    }.onFailure {
        println("YtMusic likeVideoOrSong error: ${it.stackTraceToString()}")
    }

    suspend fun removelikeVideoOrSong(VideoId: String) = runCatching {
        println("YtMusic removelikeVideoOrSong playlistIdId: $VideoId")
        Environment.removelikeVideoOrSong(VideoId)
    }.onFailure {
        println("YtMusic removelikeVideoOrSong error: ${it.stackTraceToString()}")
    }

    suspend fun getHomePage(setLogin: Boolean = false): Result<HomePage> = runCatching {

        var response = Environment.browse(browseId = "FEmusic_home", setLogin = setLogin).body<BrowseResponse>()

        println("homePage() response sections: ${response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents}" )


        var continuation = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.continuations?.getContinuation()

        val sections = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents!!
            .mapNotNull { it.musicCarouselShelfRenderer }
            .mapNotNull {
                HomePage.Section.fromMusicCarouselShelfRenderer(it)
            }.toMutableList()
        while (continuation != null) {
            println("gethomePage() continuation before:  ${continuation}" )
            response = Environment.browse(continuation = continuation).body<BrowseResponse>()
            continuation = response.continuationContents?.sectionListContinuation?.continuations?.getContinuation()
            println("gethomePage() continuation after:  ${continuation}" )

            sections += response.continuationContents?.sectionListContinuation?.contents
                ?.mapNotNull { it.musicCarouselShelfRenderer }
                ?.mapNotNull {
                    HomePage.Section.fromMusicCarouselShelfRenderer(it)
                }.orEmpty()

        }
        HomePage( sections = sections )
    }

    suspend fun getHistory(setLogin: Boolean = false): Result<HistoryPage> = runCatching {

        val response = Environment.browse(browseId = "FEmusic_history", setLogin = setLogin)
            .body<BrowseResponse>()

        println("getHistory() response sections: ${response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents}" )

        HistoryPage(
            sections = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents
                ?.mapNotNull {
                    it.musicShelfRenderer?.let { musicShelfRenderer ->
                        HistoryPage.fromMusicShelfRenderer(musicShelfRenderer)
                    }
                }
        )

    }

    suspend fun getArtistPage(browseId: String, setLogin: Boolean = false): Result<ArtistPage> = runCatching {
        val response = Environment.browse(browseId = browseId, setLogin = setLogin).body<BrowseResponse>()
        val sections = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents
            ?.mapNotNull(ArtistPage::fromSectionListRendererContent)!!

        ArtistPage(
            artist = Environment.ArtistItem(
                info = Environment.Info(
                    name = response.header?.musicImmersiveHeaderRenderer?.title?.runs?.firstOrNull()?.text
                        ?: response.header?.musicVisualHeaderRenderer?.title?.runs?.firstOrNull()?.text
                        ?: response.header?.musicHeaderRenderer?.title?.runs?.firstOrNull()?.text!!,
                    endpoint = NavigationEndpoint.Endpoint.Browse(
                        browseId = browseId,
                        params = response.header?.musicImmersiveHeaderRenderer?.title?.runs?.firstOrNull()?.navigationEndpoint?.browseEndpoint?.params
                    )
                ),
                thumbnail = response.header?.musicImmersiveHeaderRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                    ?: response.header?.musicVisualHeaderRenderer?.foregroundThumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality()
                    ?: response.header?.musicDetailHeaderRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                channelId = response.header?.musicImmersiveHeaderRenderer?.subscriptionButton?.subscribeButtonRenderer?.channelId,
                subscribersCountText = response.header?.musicImmersiveHeaderRenderer?.subscriptionButton?.subscribeButtonRenderer?.subscriberCountText?.runs?.firstOrNull()?.text,
            ),
            sections = sections,
            description = response.header?.musicImmersiveHeaderRenderer?.description?.runs?.firstOrNull()?.text,
            subscribers = response.header?.musicImmersiveHeaderRenderer?.subscriptionButton?.subscribeButtonRenderer?.subscriberCountText?.text,
            shuffleEndpoint = response.header?.musicImmersiveHeaderRenderer?.playButton?.buttonRenderer?.navigationEndpoint?.watchEndpoint,
            radioEndpoint = response.header?.musicImmersiveHeaderRenderer?.startRadioButton?.buttonRenderer?.navigationEndpoint?.watchEndpoint,
        )
    }

    suspend fun getArtistItemsPage(endpoint: BrowseEndpoint): Result<ArtistItemsPage> = runCatching {
        val response = Environment.browse(browseId = endpoint.browseId, params = endpoint.params).body<BrowseResponse>()

        println("getArtistItemsPage() response continuation: " +
                "${
                    response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                        ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                        ?.musicPlaylistShelfRenderer?.contents?.lastOrNull()
                        ?.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
        }")

        val gridRenderer = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
            ?.gridRenderer
        if (gridRenderer != null) {
            ArtistItemsPage(
                title = gridRenderer.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()?.text.orEmpty(),
                items = gridRenderer.items!!.mapNotNull {
                    it.musicTwoRowItemRenderer?.let { renderer ->
                        ArtistItemsPage.fromMusicTwoRowItemRenderer(renderer)
                    }
                },
                continuation = gridRenderer.continuations?.getContinuation()
            )
        } else {
            ArtistItemsPage(
                title = response.header?.musicHeaderRenderer?.title?.runs?.firstOrNull()?.text!!,
                items = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                    ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                    ?.musicPlaylistShelfRenderer?.contents?.mapNotNull {
                        it.musicResponsiveListItemRenderer?.let { it1 ->
                            ArtistItemsPage.fromMusicResponsiveListItemRenderer(
                                it1
                            )
                        }
                    }!!,
//                continuation = response.contents.singleColumnBrowseResultsRenderer.tabs.firstOrNull()
//                    ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
//                    ?.musicPlaylistShelfRenderer?.continuations?.getContinuation()
                continuation = response.contents.singleColumnBrowseResultsRenderer.tabs.firstOrNull()
                    ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                    ?.musicPlaylistShelfRenderer?.contents?.lastOrNull()
                    ?.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
            )
        }
    }.onFailure {
        println("YtMusic getArtistItemsPage() error: ${it.stackTraceToString()}")
    }

    suspend fun getPlaylist(playlistId: String): Result<PlaylistPage> = runCatching {
        val playlistIdChecked = if (playlistId.startsWith("VL")) playlistId else "VL$playlistId"
        println("YtMusic getPlaylist playlistId: $playlistId Checked: $playlistIdChecked")
        val response = Environment.browse(
            browseId = playlistIdChecked,
            setLogin = true
        ).body<BrowseResponse>()


        if (response.header != null)
            getPlaylistPreviousMode(playlistIdChecked, response)
        else
            getPlaylistNewMode(playlistIdChecked, response)
    }.onFailure {
        println("YtMusic getPlaylist error: ${it.stackTraceToString()}")
    }

    private fun getPlaylistPreviousMode(playlistId: String, response: BrowseResponse): PlaylistPage {
        val header = response.header?.musicDetailHeaderRenderer ?:
            response.header?.musicEditablePlaylistDetailHeaderRenderer?.header?.musicDetailHeaderRenderer


        val editable = response.header?.musicEditablePlaylistDetailHeaderRenderer != null

        return PlaylistPage(
            playlist = Environment.PlaylistItem(
                info = Environment.Info(
                    name = header?.title?.runs?.firstOrNull()?.text!!,
                    endpoint = NavigationEndpoint.Endpoint.Browse(
                        browseId = playlistId,
                    )
                ),
                songCount = 0, //header.secondSubtitle.runs?.firstOrNull()?.text,
                thumbnail = header.thumbnail.croppedSquareThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                channel = null,
                isEditable = editable,
//                playEndpoint = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
//                    ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
//                    ?.musicPlaylistShelfRenderer?.contents?.firstOrNull()?.musicResponsiveListItemRenderer
//                    ?.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint,
//                shuffleEndpoint = header.menu.menuRenderer.topLevelButtons?.firstOrNull()?.buttonRenderer?.navigationEndpoint?.watchPlaylistEndpoint!!,
//                radioEndpoint = header.menu.menuRenderer.items?.find {
//                    it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
//                }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint!!,

            ),
            description = response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer
                ?.description?.musicDescriptionShelfRenderer?.description?.text,
            songs = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                ?.musicPlaylistShelfRenderer?.contents?.mapNotNull {
                    it.musicResponsiveListItemRenderer?.let { it1 ->
                        PlaylistPage.fromMusicResponsiveListItemRenderer(
                            it1
                        )
                    }
                }!!,
            songsContinuation = response.contents.singleColumnBrowseResultsRenderer.tabs.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                ?.musicPlaylistShelfRenderer?.continuations?.getContinuation(),
            continuation = response.contents.singleColumnBrowseResultsRenderer.tabs.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.continuations?.getContinuation()
        )
    }

    private fun getPlaylistNewMode(playlistId: String, response: BrowseResponse): PlaylistPage {
        val header = response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer
            ?: response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                ?.musicEditablePlaylistDetailHeaderRenderer?.header?.musicResponsiveHeaderRenderer

        val isEditable = response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
            ?.musicEditablePlaylistDetailHeaderRenderer != null

        println("getPlaylist new mode editable : ${isEditable}")

//        println("getPlaylist new mode description: ${response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()
//            ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.description?.musicDescriptionShelfRenderer?.description}")

        return PlaylistPage(
            playlist = Environment.PlaylistItem(
                info = Environment.Info(
                    name = header?.title?.runs?.firstOrNull()?.text!!,
                    endpoint = NavigationEndpoint.Endpoint.Browse(
                        browseId = playlistId,
                    )
                ),
                songCount = 0,//header.secondSubtitle?.runs?.firstOrNull()?.text,
                thumbnail = response.background?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
                channel = null,
                isEditable = isEditable,
//                playEndpoint = header.buttons.getOrNull(1)?.musicPlayButtonRenderer
//                    ?.playNavigationEndpoint?.watchEndpoint,
//                shuffleEndpoint = header.buttons.getOrNull(2)?.menuRenderer?.items?.find {
//                    it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE"
//                }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint,
//                radioEndpoint = header.buttons.getOrNull(2)?.menuRenderer?.items?.find {
//                    it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
//                }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint,
            ),
            description = response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer
                ?.description?.musicDescriptionShelfRenderer?.description?.text,
            songs = response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
                ?.contents?.firstOrNull()?.musicPlaylistShelfRenderer?.contents?.mapNotNull {
                    it.musicResponsiveListItemRenderer?.let { it1 ->
                        PlaylistPage.fromMusicResponsiveListItemRenderer(
                            it1
                        )
                    }
                }!!,
//            songsContinuation = response.contents.twoColumnBrowseResultsRenderer.secondaryContents.sectionListRenderer
//                .contents.firstOrNull()?.musicPlaylistShelfRenderer?.continuations?.getContinuation(),
            songsContinuation = response.contents.twoColumnBrowseResultsRenderer.secondaryContents.sectionListRenderer
                .contents.firstOrNull()?.musicPlaylistShelfRenderer?.contents!!.lastOrNull()
                    ?.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
                ,
            continuation = response.contents.twoColumnBrowseResultsRenderer.secondaryContents.sectionListRenderer
                .continuations?.getContinuation(),
            isEditable = isEditable
        )
    }

    suspend fun getPlaylistContinuation(continuation: String) = runCatching {
        val response = Environment.browse(
            continuation = continuation,
            setLogin = true
        ).body<BrowseResponse>()

        println("YtMusic getPlaylistContinuation response: ${response.onResponseReceivedActions?.firstOrNull()
            ?.appendContinuationItemsAction?.continuationItems?.lastOrNull()?.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token}")

//        response.continuationContents?.musicPlaylistShelfContinuation?.contents?.mapNotNull {
//            it.musicResponsiveListItemRenderer?.let { it1 ->
//                PlaylistPage.fromMusicResponsiveListItemRenderer( it1 )
//            }
//        }?.let {
//            PlaylistContinuationPage(
//                songs = it,
//                continuation = response.continuationContents.musicPlaylistShelfContinuation.continuations?.getContinuation()
//            )
//        }

        response.onResponseReceivedActions?.map {
            it.appendContinuationItemsAction?.continuationItems?.mapNotNull { it1 ->
                it1.musicResponsiveListItemRenderer?.let { it2 ->
                    PlaylistPage.fromMusicResponsiveListItemRenderer(
                        it2
                    )
                }
            }
        }?.let {
            it.firstOrNull()?.let { it1 ->
                PlaylistContinuationPage(
                    songs = it1,
                    continuation = response.onResponseReceivedActions.firstOrNull()
                        ?.appendContinuationItemsAction?.continuationItems?.lastOrNull()?.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
                )
            }
        }

    }.onFailure {
        println("YtMusic getPlaylistContinuation error: ${it.stackTraceToString()}")
    }

    suspend fun getArtistItemsContinuation(continuation: String) = runCatching {
        val response = Environment.browse(
            continuation = continuation,
            setLogin = true
        ).body<BrowseResponse>()

        response.onResponseReceivedActions?.map {
            it.appendContinuationItemsAction?.continuationItems?.mapNotNull { it1 ->
                it1.musicResponsiveListItemRenderer?.let { it2 ->
                    ArtistItemsPage.fromMusicResponsiveListItemRenderer(
                        it2
                    )
                }
            }
        }?.let {
            it.firstOrNull()?.let { it1 ->
                ArtistItemsContinuationPage(
                    items = it1,
                    continuation = response.onResponseReceivedActions.firstOrNull()
                        ?.appendContinuationItemsAction?.continuationItems?.lastOrNull()
                        ?.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
                )
            }
        }

    }.onFailure {
        println("YtMusic getArtistItemsContinuation error: ${it.stackTraceToString()}")
    }

    suspend fun getAlbum(browseId: String, withSongs: Boolean = true): Result<AlbumPage> = runCatching {
        val response = Environment.browse(browseId = browseId).body<BrowseResponse>()
        val playlistId = response.microformat?.microformatDataRenderer?.urlCanonical?.substringAfterLast('=')!!

        AlbumPage(
            album = Environment.AlbumItem(
                playlistId = playlistId,
                info = Environment.Info(
                    name = response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.title?.runs?.firstOrNull()?.text!!,
                    endpoint = NavigationEndpoint.Endpoint.Browse(
                        browseId = browseId,
                    )
                ),
                authors = response.contents.twoColumnBrowseResultsRenderer.tabs.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.straplineTextOne?.runs?.oddElements()
                    ?.map {
                        Environment.Info(
                            name = it.text,
                            endpoint = it.navigationEndpoint?.browseEndpoint,
                        )
                    }!!,
                year = response.contents.twoColumnBrowseResultsRenderer.tabs.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.subtitle?.runs?.lastOrNull()?.text,
                thumbnail = response.contents.twoColumnBrowseResultsRenderer.tabs.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.getBestQuality(),
            ),
            songs = if (withSongs) getAlbumSongs(playlistId).getOrThrow() else emptyList(),
            otherVersions = response.contents.twoColumnBrowseResultsRenderer.secondaryContents?.sectionListRenderer?.contents?.getOrNull(
                1
            )?.musicCarouselShelfRenderer?.contents
                ?.mapNotNull { it.musicTwoRowItemRenderer }
                ?.map(NewReleaseAlbumPage::fromMusicTwoRowItemRenderer)
                .orEmpty(),
            url = response.microformat.microformatDataRenderer.urlCanonical,
            description = response.contents.twoColumnBrowseResultsRenderer.tabs
                .firstOrNull()
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.contents
                ?.firstOrNull()
                ?.musicResponsiveHeaderRenderer
                ?.description
                ?.musicDescriptionShelfRenderer
                ?.description?.text,
        )
    }

    suspend fun getAlbumSongs(playlistId: String): Result<List<Environment.SongItem>> = runCatching {
        val response = Environment.browse(browseId = "VL$playlistId").body<BrowseResponse>()

        val contents =
            response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                ?.musicPlaylistShelfRenderer?.contents ?:
            response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
                ?.contents?.firstOrNull()?.musicPlaylistShelfRenderer?.contents

        val songs = contents?.mapNotNull {
            it.musicResponsiveListItemRenderer?.let { it1 -> AlbumPage.getSong(it1) }
        }
        println("mediaItem getAlbumSongs songs: $songs")
        songs!!
    }

    suspend fun getVideOrSongInfo(videoId: String): Result<VideoOrSongInfo> = runCatching {
        val response = Environment.getVideoOrSongInfo(videoId)
        return response
    }.onFailure {
        println("YtMusic getVideOrSongInfo error: ${it.stackTraceToString()}")
    }


}