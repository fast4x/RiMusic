package it.fast4x.environment

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import com.zionhuang.innertube.pages.LibraryContinuationPage
import com.zionhuang.innertube.pages.LibraryPage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.brotli
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.serialization.kotlinx.xml.xml
import it.fast4x.environment.utils.YouTubeLocale
import it.fast4x.environment.models.AccountInfo
import it.fast4x.environment.models.AccountMenuResponse
import it.fast4x.environment.models.BrowseResponse
import it.fast4x.environment.models.Context
import it.fast4x.environment.models.Context.Client
import it.fast4x.environment.models.Context.Companion.DefaultWeb3
import it.fast4x.environment.models.Context.Companion.DefaultWeb
import it.fast4x.environment.models.Context.Companion.DefaultWeb2WithLocale
import it.fast4x.environment.models.Context.Companion.USER_AGENT1
import it.fast4x.environment.models.GridRenderer
import it.fast4x.environment.models.MediaType
import it.fast4x.environment.models.MusicNavigationButtonRenderer
import it.fast4x.environment.models.MusicShelfRenderer
import it.fast4x.environment.models.NavigationEndpoint
import it.fast4x.environment.models.NextResponse
import it.fast4x.environment.models.PipedResponse
import it.fast4x.environment.models.PlayerResponse
import it.fast4x.environment.models.ResponseContext
import it.fast4x.environment.models.ReturnYouTubeDislikeResponse
import it.fast4x.environment.models.Runs
import it.fast4x.environment.models.Thumbnail
import it.fast4x.environment.models.VideoOrSongInfo
import it.fast4x.environment.models.bodies.AccountMenuBody
import it.fast4x.environment.models.bodies.Action
import it.fast4x.environment.models.bodies.BrowseBody
import it.fast4x.environment.models.bodies.CreatePlaylistBody
import it.fast4x.environment.models.bodies.EditPlaylistBody
import it.fast4x.environment.models.bodies.LikeBody
import it.fast4x.environment.models.bodies.NextBody
import it.fast4x.environment.models.bodies.PlayerBody
import it.fast4x.environment.models.bodies.PlaylistDeleteBody
import it.fast4x.environment.models.bodies.SubscribeBody
import it.fast4x.environment.utils.EnvironmentPreferences
import it.fast4x.environment.utils.NewPipeUtils
import it.fast4x.environment.utils.NewPipeUtils.decodeSignatureCipher
import it.fast4x.environment.utils.ProxyPreferences
//import it.fast4x.environment.utils.YoutubePreferences
import it.fast4x.environment.utils.getProxy
import it.fast4x.environment.utils.parseCookieString
import it.fast4x.environment.utils.sha1
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.net.InetAddress
import java.net.Proxy
import java.util.Locale
import kotlin.random.Random

object Environment {

    val _7ZoUy0mkCP = EnvironmentPreferences.preference?.p37 ?: ""
    val _uMYwa66ycM = EnvironmentPreferences.preference?.p38 ?: ""

    val _3djbhqyLpE = EnvironmentPreferences.preference?.p1 ?: ""
    val _NXIvG4ve8N = EnvironmentPreferences.preference?.p8 ?: ""
    val _cdSL7DrPbA = EnvironmentPreferences.preference?.p5 ?: ""
    val _QPWiB5riY1 = EnvironmentPreferences.preference?.p11 ?: ""
    val _QPmE9fYezr = EnvironmentPreferences.preference?.p7 ?: ""
    val _Uwjb1AiI8t = EnvironmentPreferences.preference?.p13 ?: ""
    val _qkHMinedvm = EnvironmentPreferences.preference?.p3 ?: ""
    val _lvsJfaKiys = EnvironmentPreferences.preference?.p9 ?: ""
    val _YUxeqOcD7P = EnvironmentPreferences.preference?.p12 ?: ""
    val _Pb7oepZC3P = EnvironmentPreferences.preference?.p4 ?: ""
    val _EWGT63Xrf0 = EnvironmentPreferences.preference?.p14 ?: ""
    val _eR3hChvLRR = EnvironmentPreferences.preference?.p10 ?: ""
    val _wI7xC0jvaR = EnvironmentPreferences.preference?.p2 ?: ""
    val _1enRpaV4ei = EnvironmentPreferences.preference?.p6 ?: ""
    val _0y2BrSeuzt = EnvironmentPreferences.preference?.p15 ?: ""
    val _YfkInZQChm = EnvironmentPreferences.preference?.p16 ?: ""
    val _Aj104iDVho = EnvironmentPreferences.preference?.p17 ?: ""
    val _XsHo8IdebO = EnvironmentPreferences.preference?.p36 ?: ""
    val _1Vv31MecRl = EnvironmentPreferences.preference?.p0 ?: ""

    val getEnvironment = {
        println("EnvironmentPreferences: ${EnvironmentPreferences}")
    }

    var dnsToUse: String? = EnvironmentPreferences.dnsOverHttps.toString()  //YoutubePreferences.preference?.dnsOverHttps.toString()

    @OptIn(ExperimentalSerializationApi::class)
    val client = HttpClient(OkHttp) {
        //BrowserUserAgent()

        expectSuccess = true

        install(ContentNegotiation) {
            protobuf()
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
            xml(
                format =
                XML {
                    xmlDeclMode = XmlDeclMode.Charset
                    autoPolymorphic = true
                },
                contentType = ContentType.Text.Xml,
            )
        }

        install(ContentEncoding) {
            brotli(1.0F)
            gzip(0.9F)
            deflate(0.8F)
        }

        engine {
            addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )

            if (dnsToUse != null) {
                val appCache = Cache(File("cacheDir", "okhttpcache"), 10 * 1024 * 1024)
                val bootstrapClient = OkHttpClient.Builder().cache(appCache).build()
                val googleDns = DnsOverHttps.Builder().client(bootstrapClient)
                    .url("https://dns.google/dns-query".toHttpUrl())
                    .bootstrapDnsHosts(InetAddress.getByName("8.8.8.8"), InetAddress.getByName("8.8.4.4")).build()
                val cloudflareDns = DnsOverHttps.Builder().client(bootstrapClient)
                    .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
                    .bootstrapDnsHosts(InetAddress.getByName("1.1.1.1"), InetAddress.getByName("1.0.0.1")).build()
                val openDns = DnsOverHttps.Builder().client(bootstrapClient)
                    .url("https://doh.opendns.com/dns-query".toHttpUrl())
                    .bootstrapDnsHosts(InetAddress.getByName("208.67.222.222"), InetAddress.getByName("208.67.220.220")).build()
                val dns: DnsOverHttps = when (dnsToUse) {
                    "google" -> googleDns
                    "cloudflare" -> cloudflareDns
                    "opendns" -> openDns
                    else -> googleDns
                }

                val clientWithDns = bootstrapClient.newBuilder().dns(dns).build()
                preconfigured = clientWithDns
            }

        }

        ProxyPreferences.preference?.let {
            engine {
                proxy = getProxy(it)
            }
        }

        defaultRequest {
            url(scheme = "https", host = _1Vv31MecRl) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                parameters.append("prettyPrint", "false")
            }
        }
    }



    var proxy: Proxy? = null
        set(value) {
            field = value
            client.close()
            client
        }

    var locale = YouTubeLocale(
        gl = Locale.getDefault().country,
        hl = Locale.getDefault().toLanguageTag()
        //gl = LocalePreferences.preference?.gl ?: "US",
        //hl = LocalePreferences.preference?.hl ?: "en"
    )
    //var visitorData: String = YoutubePreferences.preference?.visitordata.toString()
    var visitorData: String = EnvironmentPreferences.visitordata.toString()
    var dataSyncId: String = EnvironmentPreferences.dataSyncId.toString()

    var cookie: String? = null
        set(value) {
            field = value
            cookieMap = if (value == null) emptyMap() else parseCookieString(value)
        }
    private var cookieMap = emptyMap<String, String>()

    private var poTokenChallengeRequestKey = "O43z0dpjhgX20SCx4KAo"

    private val listPipedInstances =
        listOf(
            "https://pipedapi.nosebs.ru",
            "https://pipedapi.kavin.rocks",
            "https://pipedapi.tokhmi.xyz",
            "https://pipedapi.syncpundit.io",
            "https://pipedapi.leptons.xyz",
            "https://pipedapi.r4fo.com",
            "https://yapi.vyper.me",
            "https://pipedapi-libre.kavin.rocks",
        )

    /**
     * Json deserializer for PO token request
     */
    private val poTokenJsonDeserializer =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true
            useArrayPolymorphism = true
        }

    private fun String.getPoToken(): String? =
        this
            .replace("[", "")
            .replace("]", "")
            .split(",")
            .findLast { it.contains("\"") }
            ?.replace("\"", "")

    private var poTokenObject: Pair<String?, Long> = Pair(null, 0)

    internal const val musicResponsiveListItemRendererMask = "musicResponsiveListItemRenderer(flexColumns,fixedColumns,thumbnail,navigationEndpoint,badges)"
    internal const val musicTwoRowItemRendererMask = "musicTwoRowItemRenderer(thumbnailRenderer,title,subtitle,navigationEndpoint)"
    const val playlistPanelVideoRendererMask = "playlistPanelVideoRenderer(title,navigationEndpoint,longBylineText,shortBylineText,thumbnail,lengthText)"

    internal fun HttpRequestBuilder.mask(value: String = "*") =
        header("X-Goog-FieldMask", value)


    @Serializable
    data class Info<T : NavigationEndpoint.Endpoint>(
        val name: String?,
        val endpoint: T?
    ) {
        @Suppress("UNCHECKED_CAST")
        constructor(run: Runs.Run) : this(
            name = run.text,
            endpoint = run.navigationEndpoint?.endpoint as T?
        )
    }

    @JvmInline
    value class SearchFilter(val value: String) {
        companion object {
            val Song = SearchFilter("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
            val Video = SearchFilter("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D")
            val Album = SearchFilter("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D")
            val Artist = SearchFilter("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D")
            val CommunityPlaylist = SearchFilter("EgeKAQQoAEABagoQAxAEEAoQCRAF")
            val FeaturedPlaylist = SearchFilter("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D")
            val Podcast = SearchFilter("EgWKAQJQAWoIEBAQERADEBU%3D")
        }
    }

    @Serializable
    sealed class Item {
        abstract val thumbnail: Thumbnail?
        abstract val key: String
        abstract val title: String?
    }

    @Serializable
    data class SongItem(
        val info: Info<NavigationEndpoint.Endpoint.Watch>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val album: Info<NavigationEndpoint.Endpoint.Browse>?,
        val durationText: String?,
        override val thumbnail: Thumbnail?,
        val explicit: Boolean = false,
        val setVideoId: String? = null
    ) : Item() {
        //override val key get() = info!!.endpoint!!.videoId!!
        override val key get() = info?.endpoint?.videoId ?: ""
        override val title get() = info?.name

        companion object
    }

    @Serializable
    data class VideoItem(
        val info: Info<NavigationEndpoint.Endpoint.Watch>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val viewsText: String?,
        val durationText: String?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.videoId!!
        override val title get() = info?.name

        val isOfficialMusicVideo: Boolean
            get() = info
                ?.endpoint
                ?.watchEndpointMusicSupportedConfigs
                ?.watchEndpointMusicConfig
                ?.musicVideoType == "MUSIC_VIDEO_TYPE_OMV"

        val isUserGeneratedContent: Boolean
            get() = info
                ?.endpoint
                ?.watchEndpointMusicSupportedConfigs
                ?.watchEndpointMusicConfig
                ?.musicVideoType == "MUSIC_VIDEO_TYPE_UGC"

        companion object
    }

    @Serializable
    data class AlbumItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        val playlistId: String? = null,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!
        override val title get() = info?.name

        companion object
    }

    @Serializable
    data class ArtistItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val subscribersCountText: String?,
        val channelId: String? = null,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!
        override val title get() = info?.name

        companion object
    }

    @Serializable
    data class PlaylistItem(
        val info: Info<NavigationEndpoint.Endpoint.Browse>?,
        val channel: Info<NavigationEndpoint.Endpoint.Browse>?,
        val songCount: Int?,
        val isEditable: Boolean?,
        override val thumbnail: Thumbnail?
    ) : Item() {
        override val key get() = info!!.endpoint!!.browseId!!
        override val title get() = info?.name

        companion object
    }

    data class ArtistInfoPage(
        val name: String?,
        val description: String?,
        val subscriberCountText: String?,
        val thumbnail: Thumbnail?,
        val shuffleEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val radioEndpoint: NavigationEndpoint.Endpoint.Watch?,
        val songs: List<SongItem>?,
        val songsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val albums: List<AlbumItem>?,
        val albumsEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val singles: List<AlbumItem>?,
        val singlesEndpoint: NavigationEndpoint.Endpoint.Browse?,
        val playlists: List<PlaylistItem>?,
    )

    data class PlaylistOrAlbumPage(
        val title: String?,
        val authors: List<Info<NavigationEndpoint.Endpoint.Browse>>?,
        val year: String?,
        val thumbnail: Thumbnail?,
        val url: String?,
        val songsPage: ItemsPage<SongItem>?,
        val otherVersions: List<AlbumItem>?,
        val description: String?,
        val otherInfo: String?
    )

    data class NextPage(
        val itemsPage: ItemsPage<SongItem>?,
        val playlistId: String?,
        val params: String? = null,
        val playlistSetVideoId: String? = null
    )

    @Serializable
    data class RelatedPage(
        val songs: List<SongItem>? = null,
        val playlists: List<PlaylistItem>? = null,
        val albums: List<AlbumItem>? = null,
        val artists: List<ArtistItem>? = null,
    )
    data class RelatedSongs(
        val songs: List<SongItem>? = null
    )

    @Serializable
    data class DiscoverPage(
        val newReleaseAlbums: List<AlbumItem>,
        val moods: List<Mood.Item>
    )

    data class DiscoverPageAlbums(
        val newReleaseAlbums: List<AlbumItem>

    )

    @Serializable
    data class Mood(
        val title: String,
        val items: List<Item>
    ) {
        @Serializable
        data class Item(
            val title: String,
            val stripeColor: Long,
            val endpoint: NavigationEndpoint.Endpoint.Browse
        )
    }

    data class ItemsPage<T : Item>(
        var items: List<T>?,
        val continuation: String?
    )

    @Serializable
    data class ChartsPage(
        val playlists: List<PlaylistItem>? = null,
        val artists: List<ArtistItem>? = null,
        val videos: List<VideoItem>? = null,
        val songs: List<SongItem>? = null,
        val trending: List<SongItem>? = null
    )

    data class Podcast(
        val title: String,
        //val author: ArtistItem,
        val author: String?,
        val authorThumbnail: String?,
        val thumbnail: List<Thumbnail>,
        val description: String?,
        val listEpisode: List<EpisodeItem>
    ) {
        data class EpisodeItem(
            val title: String,
            //val author: ArtistItem,
            val author: String?,
            val description: String?,
            val thumbnail: List<Thumbnail>,
            val createdDay: String?,
            val durationString: String?,
            val videoId: String
        )
    }

    data class SearchSuggestions(
        val queries: List<String>,
        val recommendedSong: SongItem?,
        val recommendedAlbum: AlbumItem?,
        val recommendedArtist: ArtistItem?,
        val recommendedPlaylist: PlaylistItem?,
        val recommendedVideo: VideoItem?,
    )

    fun MusicNavigationButtonRenderer.toMood(): Mood.Item? {
        return Mood.Item(
            title = buttonText.runs.firstOrNull()?.text ?: return null,
            stripeColor = solid?.leftStripeColor ?: return null,
            endpoint = clickCommand.browseEndpoint ?: return null
        )
    }

    fun List<Thumbnail>.getBestQuality() =
        maxByOrNull { (it.width ?: 0) * (it.height ?: 0) }

    @Serializable
    data class GhostResponse(
        val responseContext: ResponseContext,
        val playbackTracking: PlayerResponse.PlaybackTracking? = null
    )


    suspend fun accountInfo(): Result<AccountInfo?> = runCatching {
        println("YoutubeLogin accountInfo() response ${accountMenu().body<AccountMenuResponse>()
            .actions} ")
        accountMenu()
            .body<AccountMenuResponse>()
            .actions?.get(0)?.openPopupAction?.popup?.multiPageMenuRenderer
            ?.header?.activeAccountHeaderRenderer
            ?.toAccountInfo()
    }.onFailure {
        println("Error YoutubeLogin accountInfo(): ${it.stackTraceToString()}")
    }

    suspend fun accountInfoList(): Result<List<AccountInfo?>?> = runCatching {
        accountMenu()
            .body<AccountMenuResponse>()
            .actions?.get(0)?.openPopupAction?.popup?.multiPageMenuRenderer
            ?.header?.activeAccountHeaderRenderer
            ?.toAccountInfoList()
    }

    suspend fun accountMenu(): HttpResponse {
        val response =
            client.post(_qkHMinedvm) {
                setLogin(setLogin = true)
                setBody(AccountMenuBody())
            }

        println("YoutubeLogin Innertube accountMenuBody: ${AccountMenuBody()}")
        println("YoutubeLogin Innertube accountMenu RESPONSE: ${response.bodyAsText()}")

        return response
    }

    suspend fun getSwJsData() = client.get("https://$_1Vv31MecRl/sw.js_data")

    suspend fun visitorData(): Result<String> = runCatching {
        Json.parseToJsonElement(getSwJsData().bodyAsText().substring(5))
            .jsonArray[0]
            .jsonArray[2]
            .jsonArray.first { (it as? JsonPrimitive)?.content?.startsWith(_7ZoUy0mkCP) == true }
            .jsonPrimitive.content
    }

    fun HttpRequestBuilder.setLogin(clientType: Client = DefaultWeb.client, setLogin: Boolean = false) {
        println("HttpRequestBuilder.setLogin CALLED")
        contentType(ContentType.Application.Json)
        headers {
            append("X-YouTube-Client-Name", "${clientType.xClientName ?: 1}")
            append("X-YouTube-Client-Version", clientType.clientVersion)
            append("X-Origin", _XsHo8IdebO)
            if (clientType.referer != null) {
                append("Referer", clientType.referer)
            }
            if (setLogin) {
                println("HttpRequestBuilder.setLogin $setLogin")
                println("HttpRequestBuilder.setLogin beforeCreateHeaders cookie: $cookie")
                cookie?.let { cookieData ->
                    println("HttpRequestBuilder.setLogin cookie: $cookieData")
                    println("HttpRequestBuilder.setLogin xclientName ${clientType.xClientName ?: 1}")
                    println("HttpRequestBuilder.setLogin clientVerson ${clientType.clientVersion}")
                    println("HttpRequestBuilder.setLogin referer ${clientType.referer}")
                    println("HttpRequestBuilder.setLogin userAgent ${clientType.userAgent}")
                    println("HttpRequestBuilder.setLogin x-origin ${_XsHo8IdebO}")
                    println("HttpRequestBuilder.setLogin visitorData ${visitorData}")
                    cookieMap = parseCookieString(cookieData)
                    append("X-Goog-Authuser", "0")
                    append("X-Goog-Visitor-Id", visitorData)
                    append("Cookie", cookieData)
                    if ("SAPISID" !in cookieMap || "__Secure-3PAPISID" !in cookieMap) return@let
                    val currentTime = System.currentTimeMillis() / 1000
                    val sapisidCookie = cookieMap["SAPISID"] ?: cookieMap["__Secure-3PAPISID"]
                    val sapisidHash = sha1("$currentTime $sapisidCookie $_XsHo8IdebO")
                    println("HttpRequestBuilder.setLogin currentTime ${currentTime}")
                    println("HttpRequestBuilder.setLogin sapisidCookie ${sapisidCookie}")
                    println("HttpRequestBuilder.setLogin sapisidHash ${sapisidHash}")
                    append("Authorization", "SAPISIDHASH ${currentTime}_$sapisidHash")
                }
            }
        }
        clientType.userAgent?.let { userAgent(it) }
        parameter("prettyPrint", false)

    }

    fun HttpRequestBuilder.setHeaders(clientType: Client = DefaultWeb.client, setLogin: Boolean = false) {
        contentType(ContentType.Application.Json)
        headers {
            if (setLogin)
                append("X-Youtube-Bootstrap-Logged-In", "true")
            append("X-YouTube-Client-Name", clientType.xClientName.toString())
            append("X-YouTube-Client-Version", clientType.clientVersion)
            append("X-Origin", _XsHo8IdebO)
            if (clientType.referer != null) {
                append("Referer", clientType.referer)
            }
            if (setLogin) {
                cookie?.let { cookie ->
                    cookieMap = parseCookieString(cookie)
                    append("X-Goog-Authuser", "6")
                    append("X-Goog-Visitor-Id", visitorData)
                    append("cookie", cookie)
                    if ("SAPISID" !in cookieMap) return@let
                    val currentTime = System.currentTimeMillis() / 1000
                    val sapisidHash = sha1("$currentTime ${cookieMap["SAPISID"]} $_XsHo8IdebO")
                    append("Authorization", "SAPISIDHASH ${currentTime}_$sapisidHash SAPISID1PHASH ${currentTime}_$sapisidHash SAPISID3PHASH ${currentTime}_$sapisidHash")
                }
            }
        }
        clientType.userAgent?.let { userAgent(it) }
        parameter("prettyPrint", false)

    }

    /*******************************************
     * NEW CODE
     */

    suspend fun createPlaylist(
        ytClient: Client,
        title: String,
    ) = client.post(_lvsJfaKiys) {
        setLogin(ytClient, true)
        setBody(
            CreatePlaylistBody(
                context = Context.DefaultWebWithLocale,
                title = title
            )
        )
    }

    suspend fun deletePlaylist(
        ytClient: Client,
        playlistId: String,
    ) = client.post(_YUxeqOcD7P) {
        println("deleting $playlistId")
        setLogin(ytClient, setLogin = true)
        setBody(
            PlaylistDeleteBody(
                context = Context.DefaultWebWithLocale,
                playlistId = playlistId
            )
        )
    }

    suspend fun renamePlaylist(
        ytClient: Client,
        playlistId: String,
        name: String,
    ) = client.post(_Pb7oepZC3P) {
        setLogin(ytClient, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = Context.DefaultWebWithLocale,
                playlistId = playlistId,
                actions = listOf(
                    Action.RenamePlaylistAction(
                        playlistName = name
                    )
                )
            )
        )
    }

    suspend fun addToPlaylist(
        ytClient: Client,
        playlistId: String,
        videoId: String,
    ) = addToPlaylist(ytClient, playlistId, listOf(videoId))

    suspend fun addToPlaylist(
        ytClient: Client,
        playlistId: String,
        videoIds: List<String>,
    ) = client.post(_Pb7oepZC3P) {
        setLogin(ytClient, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = Context.DefaultWebWithLocale,
                playlistId = playlistId.removePrefix("VL"),
                actions = videoIds.map{ Action.AddVideoAction(addedVideoId = it)}
            )
        )
    }

    suspend fun removeFromPlaylist(
        ytClient: Client,
        playlistId: String,
        videoId: String,
        setVideoId: String? = null,
    ) = removeFromPlaylist(ytClient, playlistId, videoId, listOf(setVideoId))

    suspend fun removeFromPlaylist(
        ytClient: Client,
        playlistId: String,
        videoId: String,
        setVideoIds: List<String?>,
    ) = client.post(_Pb7oepZC3P) {
        setLogin(ytClient, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = Context.DefaultWebWithLocale,
                playlistId = playlistId.removePrefix("VL"),
                actions = setVideoIds.map {
                    Action.RemoveVideoAction(
                        removedVideoId = videoId,
                        setVideoId = it,
                    )
                }
            )
        )
    }

    suspend fun addPlaylistToPlaylist(
        ytClient: Client,
        playlistId: String,
        addPlaylistId: String,
    ) = client.post(_Pb7oepZC3P) {
        setLogin(ytClient, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = Context.DefaultWebWithLocale,
                playlistId = playlistId.removePrefix("VL"),
                actions = listOf(
                    Action.AddPlaylistAction(addedFullListId = addPlaylistId)
                )
            )
        )
    }

    suspend fun subscribeChannel(
        channelId: String,
    ) = client.post(_EWGT63Xrf0) {
        setLogin(setLogin = true)
        setBody(
            SubscribeBody(
                context = DefaultWeb,
                channelIds = listOf(channelId)
            )
        )
    }

    suspend fun unsubscribeChannel(
        channelId: String,
    ) = client.post(_eR3hChvLRR) {
        setLogin(setLogin = true)
        setBody(
            SubscribeBody(
                context = DefaultWeb,
                channelIds = listOf(channelId)
            )
        )
    }


    suspend fun likePlaylistOrAlbum(
        playlistId: String,
    ) = client.post(_wI7xC0jvaR) {
        setLogin(setLogin = true)
        setBody(
            LikeBody(
                context = DefaultWeb,
                target = LikeBody.Target.PlaylistTarget(playlistId = playlistId)
            )
        )
    }

    suspend fun removelikePlaylistOrAlbum(
        playlistId: String,
    ) = client.post(_1enRpaV4ei) {
        setLogin(setLogin = true)
        setBody(
            LikeBody(
                context = DefaultWeb,
                target = LikeBody.Target.PlaylistTarget(playlistId = playlistId)
            )
        )
    }

    suspend fun likeVideoOrSong(
        videoId: String,
    ) = client.post(_wI7xC0jvaR) {
        setLogin(setLogin = true)
        setBody(
            LikeBody(
                context = DefaultWeb,
                target = LikeBody.Target.VideoTarget(videoId = videoId)
            )
        )
    }

    suspend fun removelikeVideoOrSong(
        videoId: String,
    ) = client.post(_1enRpaV4ei) {
        setLogin(setLogin = true)
        setBody(
            LikeBody(
                context = DefaultWeb,
                target = LikeBody.Target.VideoTarget(videoId = videoId)
            )
        )
    }



    suspend fun browse(
        ytClient: Client = DefaultWeb.client,
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        setLogin: Boolean = false,
    ) = client.post(_3djbhqyLpE) {
        setLogin(ytClient, true)
        setBody(
            BrowseBody(
                context = Context.DefaultWebWithLocale,
                browseId = browseId,
                params = params,
            )
        )
        parameter("continuation", continuation)
        parameter("ctoken", continuation)
        if (continuation != null) {
            parameter("type", "next")
        }
    }

    suspend fun customBrowse(
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        setLogin: Boolean = true,
    ) = runCatching {
        browse(DefaultWeb.client, browseId, params, continuation, setLogin).body<BrowseResponse>()
    }

    suspend fun next(
        context: Context = DefaultWeb2WithLocale,
        videoId: String?,
        playlistId: String?,
        playlistSetVideoId: String?,
        index: Int?,
        params: String?,
        continuation: String? = null,
    ) = client.post(_NXIvG4ve8N) {
        setLogin(context.client, false)
        setBody(
            NextBody(
                context = context,
                videoId = videoId,
                playlistId = playlistId,
                playlistSetVideoId = playlistSetVideoId,
                index = index,
                params = params,
                continuation = continuation,
            )
        )
        parameter("continuation", continuation)
        parameter("ctoken", continuation)
        if (continuation != null) {
            parameter("type", "next")
        }
    }

    suspend fun player(
        videoId: String,
        playlistId: String?,
        signatureTimestamp: Int?,
    ) = client.post(_cdSL7DrPbA) {
        setLogin(setLogin = true)
        setBody(
            PlayerBody(
                videoId = videoId,
                playlistId = playlistId,
                playbackContext =
                if (signatureTimestamp != null) {
                    PlayerBody.PlaybackContext(PlayerBody.PlaybackContext.ContentPlaybackContext(
                        signatureTimestamp = signatureTimestamp
                    ))
                } else null
            ),
        )
    }

    suspend fun playerWithWebPoToken(
        videoId: String,
        playlistId: String?,
        signatureTimestamp: Int?,
        webPlayerPot: String? = null
    ) = client.post(_cdSL7DrPbA) {
        setLogin(setLogin = true)
        setBody(
            PlayerBody(
                videoId = videoId,
                playlistId = playlistId,
                playbackContext =
                if (signatureTimestamp != null) {
                    PlayerBody.PlaybackContext(PlayerBody.PlaybackContext.ContentPlaybackContext(
                        signatureTimestamp = signatureTimestamp
                    ))
                } else null,
                serviceIntegrityDimensions = if (webPlayerPot != null) {
                    PlayerBody.ServiceIntegrityDimensions(webPlayerPot)
                } else null
            ),
        )
    }

    suspend fun playerWithPotoken(
        videoId: String,
        playlistId: String?,
        cpn: String?,
        poToken: String? = null,
        signatureTimestamp: Int? = null,
        params: String? = null,
    ) = client.post(_cdSL7DrPbA) {
        setHeaders(setLogin = true)
        setBody(
            PlayerBody(
                videoId = videoId,
                playlistId = playlistId,
                cpn = cpn,
                params = params,
                playbackContext =
                PlayerBody.PlaybackContext(
                    contentPlaybackContext =
                    PlayerBody.PlaybackContext.ContentPlaybackContext(
                        signatureTimestamp = signatureTimestamp ?: 20073,
                    ),
                ),
                serviceIntegrityDimensions =
                if (poToken != null) {
                    PlayerBody.ServiceIntegrityDimensions(
                        poToken = poToken,
                    )
                } else {
                    null
                },
            ),
        )
    }

    suspend fun noLogInPlayer(videoId: String) =
        client.post(_cdSL7DrPbA) {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            header("Host", _1Vv31MecRl)
            setBody(
                PlayerBody(
                    context = DefaultWeb3,
                    playlistId = null,
                    videoId = videoId,
                ),
            )
        }

    suspend fun noLogInPlayer(
        videoId: String,
        cookie: String,
        visitorData: String?,
        poToken: String,
    ) = client.post(_0y2BrSeuzt) {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        header("Host", _YfkInZQChm)
        header("Origin", _Aj104iDVho)
        header("Sec-Fetch-Mode", "navigate")
        header(HttpHeaders.UserAgent, USER_AGENT1)
        header(
            "Set-Cookie",
            cookie,
        )
        header("X-Goog-Visitor-Id", visitorData ?: this@Environment.visitorData)
        header("X-YouTube-Client-Name", DefaultWeb3.client.clientName)
        header("X-YouTube-Client-Version", DefaultWeb3.client.clientVersion)
        setBody(
            PlayerBody(
                context = DefaultWeb3,
                playlistId = null,
                cpn = null,
                videoId = videoId,
                playbackContext = PlayerBody.PlaybackContext(),
                serviceIntegrityDimensions =
                PlayerBody.ServiceIntegrityDimensions(
                    poToken = poToken,
                ),
            ),
        )
        parameter("prettyPrint", false)
    }

    suspend fun ghostRequest(
        videoId: String,
        playlistId: String?,
    ) = client
        .get(
            "$_Aj104iDVho/watch?v=$videoId&bpctr=9999999999&has_verified=1"
                .let {
                    if (playlistId != null) "$it&list=$playlistId" else it
                },
        ) {
            headers {
                header("Connection", "close")
                header("Host", _YfkInZQChm)
                header("Cookie", if (cookie.isNullOrEmpty()) "PREF=hl=en&tz=UTC; SOCS=CAI" else cookie)
                header("Sec-Fetch-Mode", "navigate")
                header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36",
                )
            }
        }

    private fun HttpRequestBuilder.poHeader() {
        headers {
            header("accept", "*/*")
            header("origin", _Aj104iDVho)
            header("content-type", "application/json+protobuf")
            header("priority", "u=1, i")
            header("referer", "$_Aj104iDVho/")
            header("sec-ch-ua", "\"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"")
            header("sec-ch-ua-mobile", "?0")
            header("sec-ch-ua-platform", "\"macOS\"")
            header("sec-fetch-dest", "empty")
            header("sec-fetch-mode", "cors")
            header("sec-fetch-site", "cross-site")
            header(
                "user-agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
            )
            header("x-goog-api-key", "AIzaSyDyT5W0Jh49F30Pqqtyfdf7pDLFKLJoAnw")
            header("x-user-agent", "grpc-web-javascript/0.1")
        }
    }

    suspend fun createPoTokenChallenge() =
        client.post(
            "https://jnn-pa.googleapis.com/\$rpc/google.internal.waa.v1.Waa/Create",
        ) {
            poHeader()
            setBody("[\"$poTokenChallengeRequestKey\"]")
        }

    suspend fun generatePoToken(challenge: String) =
        client.post(
            "https://jnn-pa.googleapis.com/\$rpc/google.internal.waa.v1.Waa/GenerateIT",
        ) {
            poHeader()
            setBody("[\"$poTokenChallengeRequestKey\", \"$challenge\"]")
        }

    suspend fun pipedStreams(
        videoId: String,
        pipedInstance: String,
    ) = client.get("$pipedInstance/streams/$videoId") {
        contentType(ContentType.Application.Json)
    }

    private suspend fun getVisitorData(
        videoId: String,
        playlistId: String?,
    ): Triple<String, String, PlayerResponse.PlaybackTracking?> {
        try {
            val pId = if (playlistId?.startsWith("VL") == true) playlistId.removeRange(0..1) else playlistId
            val ghostRequest = ghostRequest(videoId, pId)
            val cookie =
                "PREF=hl=en&tz=UTC; SOCS=CAI; ${ghostRequest.headers
                    .getAll("set-cookie")
                    ?.map {
                        it.split(";").first()
                    }?.filter {
                        it.lastOrNull() != '='
                    }?.joinToString("; ")}"
            var response = ""
            var data = ""
            val ksoupHtmlParser =
                KsoupHtmlParser(
                    object : KsoupHtmlHandler {
                        override fun onText(text: String) {
                            super.onText(text)
                            if (text.contains("var ytInitialPlayerResponse")) {
                                val temp = text.replace("var ytInitialPlayerResponse = ", "").split(";var").firstOrNull()
                                temp?.let {
                                    response = it.trimIndent()
                                }
                            } else if (text.contains("var ytInitialData = ")) {
                                val temp = text.replace("var ytInitialData = ", "").dropLast(1)
                                temp.let {
                                    data = it.trimIndent()
                                }
                            }
                        }
                    },
                )
            ksoupHtmlParser.write(ghostRequest.bodyAsText())
            ksoupHtmlParser.end()
            val ytInitialData = poTokenJsonDeserializer.decodeFromString<GhostResponse>(data)
            val ytInitialPlayerResponse = poTokenJsonDeserializer.decodeFromString<GhostResponse>(response)
            val playbackTracking = ytInitialPlayerResponse.playbackTracking
            val loggedIn =
                ytInitialData.responseContext.serviceTrackingParams
                    ?.find { it.service == "GFEEDBACK" }
                    ?.params
                    ?.find { it.key == "logged_in" }
                    ?.value == "1"
            println("Innertube getVisitorData Logged In $loggedIn")
            val visitorData =
                ytInitialPlayerResponse.responseContext.serviceTrackingParams
                    ?.find { it.service == "GFEEDBACK" }
                    ?.params
                    ?.find { it.key == "visitor_data" }
                    ?.value
                    ?: ytInitialData.responseContext.webResponseContextExtensionData
                        ?.ytConfigData
                        ?.visitorData
            println("Innertube getVisitorData Visitor Data $visitorData")
            println("Innertube getVisitorData New Cookie $cookie")
            println("Innertube getVisitorData Playback Tracking $playbackTracking")
            if (!visitorData.isNullOrEmpty()) this@Environment.visitorData = visitorData
            return Triple(cookie, visitorData ?: this@Environment.visitorData, playbackTracking)
        } catch (e: Exception) {
            e.printStackTrace()
            return Triple("", "", null)
        }
    }

    suspend fun player(
        videoId: String,
        playlistId: String? = null,
        withLogin: Boolean = false,
    ): Result<Triple<String?, PlayerResponse?, MediaType>> =
        runCatching {
            println("PLAYERADVANCED player $videoId $playlistId withLogin $withLogin")
            val cpn =
                (1..16)
                    .map {
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"[
                            Random.Default.nextInt(
                                0,
                                64,
                            ),
                        ]
                    }.joinToString("")
            val now = System.currentTimeMillis()
            val poToken =
                if (now < poTokenObject.second) {
                    println("PLAYERADVANCED player Use saved PoToken")
                    poTokenObject.first
                } else {
                    createPoTokenChallenge()
                        .bodyAsText()
                        .let { challenge ->
                            val listChallenge = poTokenJsonDeserializer.decodeFromString<List<String?>>(challenge)
                            listChallenge.filterIsInstance<String>().firstOrNull()
                        }?.let { poTokenChallenge ->
                            generatePoToken(poTokenChallenge).bodyAsText().getPoToken().also { poToken ->
                                if (poToken != null) {
                                    poTokenObject = Pair(poToken, now + 21600000)
                                }
                            }
                        }
                }
            println("PLAYERADVANCED player PoToken $poToken")

            // if no login required, use noLoginPlyer with potoken (potoken is required in online also with login enabled... dev console shows that)
            if (!withLogin) {
                println("PLAYERADVANCED player with login $withLogin")
                val (tempCookie, visitorData, playbackTracking) = getVisitorData(videoId, playlistId)

                val playerResponse = noLogInPlayer(videoId, tempCookie, visitorData, poToken ?: "").body<PlayerResponse>()
                println("PLAYERADVANCED player Player Response $playerResponse")
                println("PLAYERADVANCED player Thumbnails " + playerResponse.videoDetails?.thumbnail)
                println("PLAYERADVANCED player Player Response status: ${playerResponse.playabilityStatus?.status}")
                val firstThumb =
                    playerResponse.videoDetails
                        ?.thumbnail
                        ?.thumbnails
                        ?.firstOrNull()
                val thumbnails =
                    if (firstThumb?.height == firstThumb?.width && firstThumb != null) MediaType.Song else MediaType.Video
                val formatList = playerResponse.streamingData?.formats?.map { Pair(it.itag, it.isAudio) }
                println("PLAYERADVANCED player Player Response formatList $formatList")
                val adaptiveFormatsList = playerResponse.streamingData?.adaptiveFormats?.map { Pair(it.itag, it.isAudio) }
                println("PLAYERADVANCED player Player Response adaptiveFormat $adaptiveFormatsList")

                if (playerResponse.playabilityStatus?.status == "OK" && (formatList != null || adaptiveFormatsList != null)) {
                    return@runCatching Triple(
                        cpn,
                        playerResponse.copy(
                            videoDetails = playerResponse.videoDetails?.copy(),
                            playbackTracking = playbackTracking ?: playerResponse.playbackTracking,
                        ),
                        thumbnails,
                    )
                } else {
                    for (instance in listPipedInstances) {
                        try {
                            val piped = pipedStreams(videoId, instance).body<PipedResponse>()
                            val audioStreams = piped.audioStreams
                            val videoStreams = piped.videoStreams
                            val stream = audioStreams + videoStreams
                            return@runCatching Triple(
                                null,
                                playerResponse.copy(
                                    streamingData =
                                    PlayerResponse.StreamingData(
                                        formats = stream.toListFormat(),
                                        adaptiveFormats = stream.toListFormat(),
                                        expiresInSeconds = 0,
                                    ),
                                    videoDetails = playerResponse.videoDetails?.copy(),
                                    playbackTracking = playbackTracking ?: playerResponse.playbackTracking,
                                ),
                                thumbnails,
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            continue
                        }
                    }
                }
                throw Exception(playerResponse.playabilityStatus?.status ?: "PLAYERADVANCED player ERROR Unknown error")
            } else {
                //WITH LOGIN
                val sigTimestamp = NewPipeUtils.getSignatureTimestamp(videoId).getOrNull()
                println("PLAYERADVANCED player with login $withLogin sigTimestamp $sigTimestamp")

                val sigResponse = playerWithPotoken(
                    videoId = videoId,
                    playlistId = playlistId,
                    cpn = cpn,
                    signatureTimestamp = sigTimestamp,
                    poToken = poToken,
                    params = null
                ).body<PlayerResponse>()
                println("PLAYERADVANCED player sigResponse $sigResponse")

                val decodedSigResponse =
                    sigResponse.copy(
                        streamingData =
                        sigResponse.streamingData?.copy(
                            formats =
                            sigResponse.streamingData.formats?.map { format ->
                                format.copy(
                                    url = format.signatureCipher?.let {
                                        decodeSignatureCipher(videoId, it)
                                    },
                                )
                            },
                            adaptiveFormats =
                            sigResponse.streamingData.adaptiveFormats?.map { adaptiveFormats ->
                                adaptiveFormats.copy(
                                    url = adaptiveFormats.signatureCipher?.let {
                                        decodeSignatureCipher(videoId, it)
                                    },
                                )
                            },
                        ),
                    )



//            listUrlSig = decodedSigResponse.streamingData
//                            ?.adaptiveFormats
//                            ?.mapNotNull { it.url }
//                            ?.toMutableList() ?: mutableListOf<String>()
//                            .apply {
//                                decodedSigResponse.streamingData
//                                    ?.formats
//                                    ?.mapNotNull { it.url }
//                                    ?.let { addAll(it) }
//                            }


//                val firstThumb =
//                    decodedSigResponse.videoDetails
//                        ?.thumbnail
//                        ?.thumbnails
//                        ?.firstOrNull()
//                val thumbnails =
//                    if (firstThumb?.height == firstThumb?.width && firstThumb != null) MediaType.Song else MediaType.Video

                println("PLAYERADVANCED player return Triple")

                return@runCatching Triple(
                    cpn,
                    decodedSigResponse.copy(
                        videoDetails = decodedSigResponse.videoDetails?.copy(),
                        playbackTracking = decodedSigResponse.playbackTracking,
                    ),
                    MediaType.Song
                )
            }
        }.onFailure {
            println("PLAYERADVANCED player ERROR ${it.stackTraceToString()}")
        }

    suspend fun noLoginPlayerWithPoToken(
        videoId: String,
        playlistId: String? = null
    ): Result<Triple<String?, PlayerResponse?, MediaType>> =
        runCatching {
            println("noLoginPlayerWithPoToken player $videoId $playlistId")
            val cpn =
                (1..16)
                    .map {
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"[
                            Random.Default.nextInt(
                                0,
                                64,
                            ),
                        ]
                    }.joinToString("")
            val now = System.currentTimeMillis()
            val poToken =
                if (now < poTokenObject.second) {
                    println("noLoginPlayerWithPoToken player Use saved PoToken")
                    poTokenObject.first
                } else {
                    createPoTokenChallenge()
                        .bodyAsText()
                        .let { challenge ->
                            val listChallenge = poTokenJsonDeserializer.decodeFromString<List<String?>>(challenge)
                            listChallenge.filterIsInstance<String>().firstOrNull()
                        }?.let { poTokenChallenge ->
                            generatePoToken(poTokenChallenge).bodyAsText().getPoToken().also { poToken ->
                                if (poToken != null) {
                                    poTokenObject = Pair(poToken, now + 21600000)
                                }
                            }
                        }
                }
                println("noLoginPlayerWithPoToken player PoToken $poToken")

                val (tempCookie, visitorData, playbackTracking) = getVisitorData(videoId, playlistId)

                val playerResponse = noLogInPlayer(videoId, tempCookie, visitorData, poToken ?: "").body<PlayerResponse>()
                println("noLoginPlayerWithPoToken player Player Response $playerResponse")
                println("noLoginPlayerWithPoToken player Player Response status: ${playerResponse.playabilityStatus?.status}")
                val firstThumb =
                    playerResponse.videoDetails
                        ?.thumbnail
                        ?.thumbnails
                        ?.firstOrNull()
                val thumbnails =
                    if (firstThumb?.height == firstThumb?.width && firstThumb != null) MediaType.Song else MediaType.Video
                val formatList = playerResponse.streamingData?.formats?.map { Pair(it.itag, it.isAudio) }
                println("noLoginPlayerWithPoToken player Player Response formatList $formatList")
                val adaptiveFormatsList = playerResponse.streamingData?.adaptiveFormats?.map { Pair(it.itag, it.isAudio) }
                println("noLoginPlayerWithPoToken player Player Response adaptiveFormat $adaptiveFormatsList")

                if (playerResponse.playabilityStatus?.status == "OK" && (formatList != null || adaptiveFormatsList != null)) {
                    return@runCatching Triple(
                        cpn,
                        playerResponse.copy(
                            videoDetails = playerResponse.videoDetails?.copy(),
                            playbackTracking = playbackTracking ?: playerResponse.playbackTracking,
                        ),
                        thumbnails,
                    )
                } else {
                    throw Exception("noLoginPlayerWithPoToken player ERROR not format playable error")
//                    for (instance in listPipedInstances) {
//                        try {
//                            val piped = pipedStreams(videoId, instance).body<PipedResponse>()
//                            val audioStreams = piped.audioStreams
//                            val videoStreams = piped.videoStreams
//                            val stream = audioStreams + videoStreams
//                            return@runCatching Triple(
//                                null,
//                                playerResponse.copy(
//                                    streamingData =
//                                    PlayerResponse.StreamingData(
//                                        formats = stream.toListFormat(),
//                                        adaptiveFormats = stream.toListFormat(),
//                                        expiresInSeconds = 0,
//                                    ),
//                                    videoDetails = playerResponse.videoDetails?.copy(),
//                                    playbackTracking = playbackTracking ?: playerResponse.playbackTracking,
//                                ),
//                                thumbnails,
//                            )
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                            continue
//                        }
//                    }
                }
                //throw Exception(playerResponse.playabilityStatus?.status ?: "noLoginPlayerWithPoToken player ERROR Unknown error")


        }.onFailure {
            println("noLoginPlayerWithPoToken player ERROR ${it.stackTraceToString()}")
        }


    private fun List<PipedResponse.AudioStream>.toListFormat(): List<PlayerResponse.StreamingData.Format> {
        val list = mutableListOf<PlayerResponse.StreamingData.Format>()
        this.forEach {
            list.add(
                PlayerResponse.StreamingData.Format(
                    itag = it.itag,
                    url = it.url,
                    mimeType = it.mimeType ?: "",
                    bitrate = it.bitrate,
                    width = it.width,
                    height = it.height,
                    contentLength = it.contentLength.toLong(),
                    quality = it.quality,
                    fps = it.fps,
                    qualityLabel = "",
                    averageBitrate = it.bitrate,
                    audioQuality = it.quality,
                    approxDurationMs = "",
                    audioSampleRate = 0,
                    audioChannels = 0,
                    loudnessDb = 0.0,
                    lastModified = 0,
                    signatureCipher = null,
                ),
            )
        }

        return list
    }

    suspend fun library(browseId: String, tabIndex: Int = 0) = runCatching {
        val response = browse(
            browseId = browseId,
            setLogin = true
        ).body<BrowseResponse>()

        val tabs = response.contents?.singleColumnBrowseResultsRenderer?.tabs

        val contents = if (tabs != null && tabs.size >= tabIndex) {
            tabs[tabIndex].tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
        }
        else {
            null
        }

        when {
            contents?.gridRenderer != null -> {
                contents.gridRenderer.items
                    ?.mapNotNull (GridRenderer.Item::musicTwoRowItemRenderer)
                    ?.mapNotNull { LibraryPage.fromMusicTwoRowItemRenderer(it) }?.let {
                        LibraryPage(
                            items = it,
                            continuation = contents.gridRenderer.continuations?.firstOrNull()?.nextContinuationData?.continuation
                        )
                    }
            }

            else -> {
                LibraryPage(
                    items = contents?.musicShelfRenderer?.contents!!
                        .mapNotNull (MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                        .mapNotNull { LibraryPage.fromMusicResponsiveListItemRenderer(it) },
                    continuation = contents.musicShelfRenderer.continuations?.firstOrNull()?.
                    nextContinuationData?.continuation
                )
            }
        }
    }

    suspend fun libraryContinuation(continuation: String) = runCatching {
        val response = browse(
            continuation = continuation,
            setLogin = true
        ).body<BrowseResponse>()

        val contents = response.continuationContents

        when {
            contents?.gridContinuation != null -> {
                contents.gridContinuation.items
                    ?.mapNotNull (GridRenderer.Item::musicTwoRowItemRenderer)
                    ?.mapNotNull { LibraryPage.fromMusicTwoRowItemRenderer(it) }?.let {
                        LibraryContinuationPage(
                            items = it,
                            continuation = contents.gridContinuation.continuations?.firstOrNull()?.nextContinuationData?.continuation
                        )
                    }
            }

            else -> {
                LibraryContinuationPage(
                    items = contents?.musicShelfContinuation?.contents!!
                        .mapNotNull (MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
                        .mapNotNull { LibraryPage.fromMusicResponsiveListItemRenderer(it) },
                    continuation = contents.musicShelfContinuation.continuations?.firstOrNull()?.
                    nextContinuationData?.continuation
                )
            }
        }
    }

    suspend fun returnYouTubeDislike(videoId: String) =
        client.get("https://returnyoutubedislikeapi.com/Votes?videoId=$videoId") {
            contentType(ContentType.Application.Json)
        }

    suspend fun getVideoOrSongInfo(videoId: String): Result<VideoOrSongInfo> =
        runCatching {
            val response = next(context = DefaultWeb2WithLocale, videoId, null, null, null, null, null).body<NextResponse>()
            val videoSecondary =
                response.contents?.twoColumnWatchNextResults
                    ?.results
                    ?.results
                    ?.content
                    ?.find {
                        it?.videoSecondaryInfoRenderer != null
                    }?.videoSecondaryInfoRenderer
            val videoPrimary =
                response.contents?.twoColumnWatchNextResults
                    ?.results
                    ?.results
                    ?.content
                    ?.find {
                        it?.videoPrimaryInfoRenderer != null
                    }?.videoPrimaryInfoRenderer
            val returnYouTubeDislikeResponse =
                returnYouTubeDislike(videoId).body<ReturnYouTubeDislikeResponse>()
            return@runCatching VideoOrSongInfo(
                videoId = videoId,
                title = videoPrimary
                    ?.title
                    ?.runs
                    ?.firstOrNull()
                    ?.text,
                author = videoSecondary
                    ?.owner
                    ?.videoOwnerRenderer
                    ?.title
                    ?.runs
                    ?.firstOrNull()
                    ?.text,
                authorId =
                videoSecondary
                    ?.owner
                    ?.videoOwnerRenderer
                    ?.navigationEndpoint
                    ?.browseEndpoint
                    ?.browseId,
                authorThumbnail =
                videoSecondary
                    ?.owner
                    ?.videoOwnerRenderer
                    ?.thumbnail
                    ?.thumbnails
                    ?.find {
                        it.height == 48
                    }?.url
                    ?.replace("s48", "s960"),
                description = videoSecondary?.attributedDescription?.content,
                subscribers =
                videoSecondary
                    ?.owner
                    ?.videoOwnerRenderer
                    ?.subscriberCountText
                    ?.simpleText?.split(" ")?.firstOrNull(),
                uploadDate = videoPrimary?.dateText?.simpleText,
                viewCount = returnYouTubeDislikeResponse.viewCount,
                like = returnYouTubeDislikeResponse.likes,
                dislike = returnYouTubeDislikeResponse.dislikes,
            )

        }





}
