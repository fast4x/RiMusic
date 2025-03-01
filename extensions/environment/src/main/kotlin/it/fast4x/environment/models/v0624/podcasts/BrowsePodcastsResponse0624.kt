package it.fast4x.environment.models.v0624.podcasts

// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                  = Json { allowStructuredMapKeys = true }
// val browseExploreResponse = json.parse(BrowseExploreResponse.serializer(), jsonString)

import kotlinx.serialization.*


@Serializable
data class BrowsePodcastsResponse0624 (
    val contents: Contents? = null,
    val trackingParams: String? = null,
    val background: ThumbnailClass? = null
)

@Serializable
data class ThumbnailClass (
    val musicThumbnailRenderer: MusicThumbnailRenderer? = null
)

@Serializable
data class MusicThumbnailRenderer (
    val thumbnail: MusicThumbnailRendererThumbnail? = null,
    val thumbnailCrop: ThumbnailCrop? = null,
    val thumbnailScale: ThumbnailScale? = null,
    val trackingParams: String? = null
)

@Serializable
data class MusicThumbnailRendererThumbnail (
    val thumbnails: List<Thumbnail>? = null
)

@Serializable
data class Thumbnail (
    val url: String? = null,
    val width: Long? = null,
    val height: Long? = null
)

@Serializable
enum class ThumbnailCrop(val value: String) {
    @SerialName("MUSIC_THUMBNAIL_CROP_UNSPECIFIED") MusicThumbnailCropUnspecified("MUSIC_THUMBNAIL_CROP_UNSPECIFIED");
}

@Serializable
enum class ThumbnailScale(val value: String) {
    @SerialName("MUSIC_THUMBNAIL_SCALE_ASPECT_FIT") MusicThumbnailScaleAspectFit("MUSIC_THUMBNAIL_SCALE_ASPECT_FIT"),
    @SerialName("MUSIC_THUMBNAIL_SCALE_UNSPECIFIED") MusicThumbnailScaleUnspecified("MUSIC_THUMBNAIL_SCALE_UNSPECIFIED");
}

@Serializable
data class Contents (
    val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer? = null
)

@Serializable
data class TwoColumnBrowseResultsRenderer (
    val secondaryContents: SecondaryContents? = null,
    val tabs: List<Tab>? = null
)

@Serializable
data class SecondaryContents (
    val sectionListRenderer: SecondaryContentsSectionListRenderer? = null
)

@Serializable
data class SecondaryContentsSectionListRenderer (
    val contents: List<PurpleContent>? = null,
    val trackingParams: String? = null,
    val header: Header? = null,

    /*
    @SerialName("targetId")
    val targetID: TargetID? = null

     */
)

@Serializable
data class PurpleContent (
    val musicShelfRenderer: MusicShelfRenderer? = null
)

@Serializable
data class MusicShelfRenderer (
    val contents: List<MusicShelfRendererContent>? = null,
    val trackingParams: String? = null,
    val shelfDivider: ShelfDivider? = null
)

@Serializable
data class MusicShelfRendererContent (
    val musicMultiRowListItemRenderer: MusicMultiRowListItemRenderer? = null
)

@Serializable
data class MusicMultiRowListItemRenderer (
    val trackingParams: String? = null,
    val thumbnail: ThumbnailClass? = null,
    val overlay: Overlay? = null,
    val onTap: OnTap? = null,
    val menu: Menu? = null,
    val subtitle: SubtitleClass? = null,
    val playbackProgress: PlaybackProgress? = null,
    val title: Title? = null,
    val description: SubtitleClass? = null,
    val displayStyle: MusicMultiRowListItemRendererDisplayStyle? = null
)

@Serializable
data class SubtitleClass (
    val runs: List<DescriptionRun>? = null
)

@Serializable
data class DescriptionRun (
    val text: String? = null
)

@Serializable
enum class MusicMultiRowListItemRendererDisplayStyle(val value: String) {
    @SerialName("MUSIC_MULTI_ROW_LIST_ITEM_DISPLAY_STYLE_DETAILED") MusicMultiRowListItemDisplayStyleDetailed("MUSIC_MULTI_ROW_LIST_ITEM_DISPLAY_STYLE_DETAILED");
}

@Serializable
data class Menu (
    val menuRenderer: MenuMenuRenderer? = null
)

@Serializable
data class MenuMenuRenderer (
    val items: List<PurpleItem>? = null,
    val trackingParams: String? = null,
    val accessibility: AccessibilityPauseDataClass? = null
)

@Serializable
data class AccessibilityPauseDataClass (
    val accessibilityData: AccessibilityAccessibilityData? = null
)

@Serializable
data class AccessibilityAccessibilityData (
    val label: String? = null
)

@Serializable
data class PurpleItem (
    val menuServiceItemRenderer: MenuItemRenderer? = null,
    val menuNavigationItemRenderer: MenuItemRenderer? = null
)

@Serializable
data class MenuItemRenderer (
    val text: SubtitleClass? = null,
    val icon: Icon? = null,
    val navigationEndpoint: MenuNavigationItemRendererNavigationEndpoint? = null,
    val trackingParams: String? = null,
    val serviceEndpoint: ServiceEndpoint? = null
)

@Serializable
data class Icon (
    val iconType: IconType? = null
)

@Serializable
enum class IconType(val value: String) {
    @SerialName("ADD_TO_PLAYLIST") AddToPlaylist("ADD_TO_PLAYLIST"),
    @SerialName("ADD_TO_REMOTE_QUEUE") AddToRemoteQueue("ADD_TO_REMOTE_QUEUE"),
    @SerialName("CHECK") Check("CHECK"),
    @SerialName("COLLAPSE") Collapse("COLLAPSE"),
    @SerialName("EXPAND") Expand("EXPAND"),
    @SerialName("LIBRARY_ADD") LibraryAdd("LIBRARY_ADD"),
    @SerialName("LIBRARY_SAVED") LibrarySaved("LIBRARY_SAVED"),
    @SerialName("PAUSE") Pause("PAUSE"),
    @SerialName("PLAY_ARROW") PlayArrow("PLAY_ARROW"),
    @SerialName("QUEUE_PLAY_NEXT") QueuePlayNext("QUEUE_PLAY_NEXT"),
    @SerialName("SHARE") Share("SHARE"),
    @SerialName("VOLUME_UP") VolumeUp("VOLUME_UP");
}

@Serializable
data class MenuNavigationItemRendererNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val modalEndpoint: ModalEndpoint? = null,
    val shareEntityEndpoint: ShareEntityEndpoint? = null
)

@Serializable
data class ModalEndpoint (
    val modal: Modal? = null
)

@Serializable
data class Modal (
    val modalWithTitleAndButtonRenderer: ModalWithTitleAndButtonRenderer? = null
)

@Serializable
data class ModalWithTitleAndButtonRenderer (
    val title: SubtitleClass? = null,
    val content: SubtitleClass? = null,
    val button: ModalWithTitleAndButtonRendererButton? = null
)

@Serializable
data class ModalWithTitleAndButtonRendererButton (
    val buttonRenderer: PurpleButtonRenderer? = null
)

@Serializable
data class PurpleButtonRenderer (
    val style: StyleEnum? = null,
    val isDisabled: Boolean? = null,
    val text: SubtitleClass? = null,
    val navigationEndpoint: ButtonRendererNavigationEndpoint? = null,
    val trackingParams: String? = null
)

@Serializable
data class ButtonRendererNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val signInEndpoint: SignInEndpoint? = null
)

@Serializable
data class SignInEndpoint (
    val hack: Boolean? = null
)

@Serializable
enum class StyleEnum(val value: String) {
    @SerialName("STYLE_BLUE_TEXT") StyleBlueText("STYLE_BLUE_TEXT");
}

@Serializable
data class ShareEntityEndpoint (
    val serializedShareEntity: String? = null,
    val sharePanelType: SharePanelType? = null
)

@Serializable
enum class SharePanelType(val value: String) {
    @SerialName("SHARE_PANEL_TYPE_UNIFIED_SHARE_PANEL") SharePanelTypeUnifiedSharePanel("SHARE_PANEL_TYPE_UNIFIED_SHARE_PANEL");
}

@Serializable
data class ServiceEndpoint (
    val clickTrackingParams: String? = null,
    val queueAddEndpoint: QueueAddEndpoint? = null
)

@Serializable
data class QueueAddEndpoint (
    val queueTarget: QueueTarget? = null,
    val queueInsertPosition: QueueInsertPosition? = null,
    val commands: List<CommandElement>? = null
)

@Serializable
data class CommandElement (
    val clickTrackingParams: String? = null,
    val addToToastAction: AddToToastAction? = null
)

@Serializable
data class AddToToastAction (
    val item: AddToToastActionItem? = null
)

@Serializable
data class AddToToastActionItem (
    val notificationTextRenderer: NotificationTextRenderer? = null
)

@Serializable
data class NotificationTextRenderer (
    val successResponseText: SubtitleClass? = null,
    val trackingParams: String? = null
)

@Serializable
enum class QueueInsertPosition(val value: String) {
    @SerialName("INSERT_AFTER_CURRENT_VIDEO") InsertAfterCurrentVideo("INSERT_AFTER_CURRENT_VIDEO"),
    @SerialName("INSERT_AT_END") InsertAtEnd("INSERT_AT_END");
}

@Serializable
data class QueueTarget (
    @SerialName("videoId")
    val videoID: String? = null,

    val onEmptyQueue: OnEmptyQueue? = null
)

@Serializable
data class OnEmptyQueue (
    val clickTrackingParams: String? = null,
    val watchEndpoint: OnEmptyQueueWatchEndpoint? = null
)

@Serializable
data class OnEmptyQueueWatchEndpoint (
    @SerialName("videoId")
    val videoID: String? = null
)

@Serializable
data class OnTap (
    val clickTrackingParams: String? = null,
    val watchEndpoint: OnTapWatchEndpoint? = null
)

@Serializable
data class OnTapWatchEndpoint (
    @SerialName("videoId")
    val videoID: String? = null,

    //@SerialName("playlistId")
    //val playlistID: PlaylistID? = null,

    val index: Long? = null,
    //val params: Params? = null,
    //val loggingContext: LoggingContext? = null,
    val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs? = null
)
/*
@Serializable
data class LoggingContext (
    val vssLoggingContext: VssLoggingContext? = null
)

@Serializable
data class VssLoggingContext (
    val serializedContextData: SerializedContextData? = null
)

@Serializable
enum class SerializedContextData(val value: String) {
    @SerialName("GiJQTDlLMGZ5dE5GMjhNTHNOWTY4M09tMmJDLU9aRmc3QWYy") GiJQTDLLMGZ5DE5GMjhNTHNOWTY4M09TMmJDLU9ARmc3QWYy("GiJQTDlLMGZ5dE5GMjhNTHNOWTY4M09tMmJDLU9aRmc3QWYy");
}
 */
/*
@Serializable
enum class Params(val value: String) {
    @SerialName("8gEDmAEI8gQECAEQAYoFAgg2") The8GEDmAEI8GQECAEQAYoFAgg2("8gEDmAEI8gQECAEQAYoFAgg2");
}

 */

/*
@Serializable
enum class PlaylistID(val value: String) {
    @SerialName("PL9K0fytNF28MLsNY683Om2bC-OZFg7Af2") PL9K0FytNF28MLsNY683Om2BCOZFg7Af2("PL9K0fytNF28MLsNY683Om2bC-OZFg7Af2");
}

 */

@Serializable
data class WatchEndpointMusicSupportedConfigs (
    val watchEndpointMusicConfig: WatchEndpointMusicConfig? = null
)

@Serializable
data class WatchEndpointMusicConfig (
    val musicVideoType: MusicVideoType? = null
)

@Serializable
enum class MusicVideoType(val value: String) {
    @SerialName("MUSIC_VIDEO_TYPE_PODCAST_EPISODE") MusicVideoTypePodcastEpisode("MUSIC_VIDEO_TYPE_PODCAST_EPISODE");
}

@Serializable
data class Overlay (
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class MusicItemThumbnailOverlayRenderer (
    val background: MusicItemThumbnailOverlayRendererBackground? = null,
    val content: MusicItemThumbnailOverlayRendererContent? = null,
    val contentPosition: ContentPosition? = null,
    val displayStyle: MusicItemThumbnailOverlayRendererDisplayStyle? = null
)

@Serializable
data class MusicItemThumbnailOverlayRendererBackground (
    val verticalGradient: VerticalGradient? = null
)

@Serializable
data class VerticalGradient (
    val gradientLayerColors: List<String>? = null
)

@Serializable
data class MusicItemThumbnailOverlayRendererContent (
    val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null
)

@Serializable
data class MusicPlayButtonRenderer (
    val playNavigationEndpoint: OnTap? = null,
    val trackingParams: String? = null,
    val playIcon: Icon? = null,
    val pauseIcon: Icon? = null,
    val iconColor: Long? = null,
    val backgroundColor: Long? = null,
    val activeBackgroundColor: Long? = null,
    val loadingIndicatorColor: Long? = null,
    val playingIcon: Icon? = null,
    val iconLoadingColor: Long? = null,
    val activeScaleFactor: Long? = null,
    val buttonSize: ButtonSize? = null,
    val rippleTarget: RippleTarget? = null,
    val accessibilityPlayData: AccessibilityPauseDataClass? = null,
    val accessibilityPauseData: AccessibilityPauseDataClass? = null
)

@Serializable
enum class ButtonSize(val value: String) {
    @SerialName("MUSIC_PLAY_BUTTON_SIZE_SMALL") MusicPlayButtonSizeSmall("MUSIC_PLAY_BUTTON_SIZE_SMALL");
}

@Serializable
enum class RippleTarget(val value: String) {
    @SerialName("MUSIC_PLAY_BUTTON_RIPPLE_TARGET_SELF") MusicPlayButtonRippleTargetSelf("MUSIC_PLAY_BUTTON_RIPPLE_TARGET_SELF");
}

@Serializable
enum class ContentPosition(val value: String) {
    @SerialName("MUSIC_ITEM_THUMBNAIL_OVERLAY_CONTENT_POSITION_CENTERED") MusicItemThumbnailOverlayContentPositionCentered("MUSIC_ITEM_THUMBNAIL_OVERLAY_CONTENT_POSITION_CENTERED");
}

@Serializable
enum class MusicItemThumbnailOverlayRendererDisplayStyle(val value: String) {
    @SerialName("MUSIC_ITEM_THUMBNAIL_OVERLAY_DISPLAY_STYLE_PERSISTENT") MusicItemThumbnailOverlayDisplayStylePersistent("MUSIC_ITEM_THUMBNAIL_OVERLAY_DISPLAY_STYLE_PERSISTENT");
}

@Serializable
data class PlaybackProgress (
    val musicPlaybackProgressRenderer: MusicPlaybackProgressRenderer? = null
)

@Serializable
data class MusicPlaybackProgressRenderer (
    val playbackProgressPercentage: Long? = null,
    val playbackProgressText: SubtitleClass? = null,
    val videoPlaybackPositionFeedbackToken: String? = null,
    val durationText: SubtitleClass? = null,
    val playedText: PlayedText? = null
)

@Serializable
data class PlayedText (
    val runs: List<PlayedTextRun>? = null
)

@Serializable
data class PlayedTextRun (
    val text: TextEnum? = null,
    val textColor: Long? = null
)

@Serializable
enum class TextEnum(val value: String) {
    @SerialName(" • ") Empty(" • "),
    @SerialName("Played") Played("Played");
}

@Serializable
data class Title (
    val runs: List<PurpleRun>? = null
)

@Serializable
data class PurpleRun (
    val text: String? = null,
    val navigationEndpoint: PurpleNavigationEndpoint? = null
)

@Serializable
data class PurpleNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val browseEndpoint: PurpleBrowseEndpoint? = null
)

@Serializable
data class PurpleBrowseEndpoint (
    @SerialName("browseId")
    val browseID: String? = null,

    val params: String? = null,
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class BrowseEndpointContextSupportedConfigs (
    val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig? = null
)

@Serializable
data class BrowseEndpointContextMusicConfig (
    val pageType: PageType? = null
)

@Serializable
enum class PageType(val value: String) {
    @SerialName("MUSIC_PAGE_TYPE_NON_MUSIC_AUDIO_TRACK_PAGE") MusicPageTypeNonMusicAudioTrackPage("MUSIC_PAGE_TYPE_NON_MUSIC_AUDIO_TRACK_PAGE"),
    @SerialName("MUSIC_PAGE_TYPE_USER_CHANNEL") MusicPageTypeUserChannel("MUSIC_PAGE_TYPE_USER_CHANNEL");
}

@Serializable
data class ShelfDivider (
    val musicShelfDividerRenderer: MusicShelfDividerRenderer? = null
)

@Serializable
data class MusicShelfDividerRenderer (
    val hidden: Boolean? = null
)

@Serializable
data class Header (
    val chipCloudRenderer: ChipCloudRenderer? = null
)

@Serializable
data class ChipCloudRenderer (
    val chips: List<Chip>? = null,
    val trackingParams: String? = null,
    val horizontalScrollable: Boolean? = null,
    val selectionBehavior: String? = null
)

@Serializable
data class Chip (
    val chipCloudChipRenderer: ChipCloudChipRenderer? = null
)

@Serializable
data class ChipCloudChipRenderer (
    val style: StyleClass? = null,
    val text: ChipCloudChipRendererText? = null,
    val navigationEndpoint: ChipCloudChipRendererNavigationEndpoint? = null,
    val trackingParams: String? = null,
    val icon: Icon? = null,

    @SerialName("uniqueId")
    val uniqueID: String? = null,

    val notSelectable: Boolean? = null,
    val isSelected: Boolean? = null,
    val onDeselectedCommand: OnDeselectedCommand? = null
)

@Serializable
data class ChipCloudChipRendererNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val openPopupAction: OpenPopupAction? = null,
    val browseSectionListReloadEndpoint: BrowseSectionListReloadEndpoint? = null
)

@Serializable
data class BrowseSectionListReloadEndpoint (
    val continuation: Continuation? = null,

    /*
    @SerialName("targetId")
    val targetID: TargetID? = null

     */
)

@Serializable
data class Continuation (
    val reloadContinuationData: ReloadContinuationData? = null
)

@Serializable
data class ReloadContinuationData (
    val continuation: String? = null,
    val clickTrackingParams: String? = null,
    val showSpinnerOverlay: Boolean? = null
)

/*
@Serializable
enum class TargetID(val value: String) {
    @SerialName("browse-feedMPSPPL9K0fytNF28MLsNY683Om2bC-OZFg7Af2") BrowseFeedMPSPPL9K0FytNF28MLsNY683Om2BCOZFg7Af2("browse-feedMPSPPL9K0fytNF28MLsNY683Om2bC-OZFg7Af2");
}

 */

@Serializable
data class OpenPopupAction (
    val popup: Popup? = null,
    val popupType: String? = null,
    val reusePopup: Boolean? = null
)

@Serializable
data class Popup (
    val menuPopupRenderer: MenuPopupRenderer? = null
)

@Serializable
data class MenuPopupRenderer (
    val items: List<MenuPopupRendererItem>? = null
)

@Serializable
data class MenuPopupRendererItem (
    val menuNavigationItemRenderer: MenuNavigationItemRenderer? = null
)

@Serializable
data class MenuNavigationItemRenderer (
    val text: MenuNavigationItemRendererText? = null,
    val icon: Icon? = null,
    val navigationEndpoint: OnDeselectedCommand? = null,
    val trackingParams: String? = null
)

@Serializable
data class OnDeselectedCommand (
    val clickTrackingParams: String? = null,
    val browseSectionListReloadEndpoint: BrowseSectionListReloadEndpoint? = null
)

@Serializable
data class MenuNavigationItemRendererText (
    val simpleText: String? = null
)

@Serializable
data class StyleClass (
    val styleType: String? = null
)

@Serializable
data class ChipCloudChipRendererText (
    val runs: List<DescriptionRun>? = null,
    val simpleText: String? = null
)

@Serializable
data class Tab (
    val tabRenderer: TabRenderer? = null
)

@Serializable
data class TabRenderer (
    val content: TabRendererContent? = null,
    val trackingParams: String? = null
)

@Serializable
data class TabRendererContent (
    val sectionListRenderer: ContentSectionListRenderer? = null
)

@Serializable
data class ContentSectionListRenderer (
    val contents: List<FluffyContent>? = null,
    val trackingParams: String? = null,

    @SerialName("targetId")
    val targetID: String? = null
)

@Serializable
data class FluffyContent (
    val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer? = null
)

@Serializable
data class MusicResponsiveHeaderRenderer (
    val thumbnail: ThumbnailClass? = null,
    val buttons: List<ButtonElement>? = null,
    val title: SubtitleClass? = null,
    val subtitle: Subtitle? = null,
    val trackingParams: String? = null,
    val straplineTextOne: StraplineTextOne? = null,
    val straplineThumbnail: ThumbnailClass? = null,
    val description: PurpleDescription? = null
)

@Serializable
data class ButtonElement (
    val buttonRenderer: FluffyButtonRenderer? = null,
    val toggleButtonRenderer: ButtonToggleButtonRenderer? = null,
    val menuRenderer: ButtonMenuRenderer? = null
)

@Serializable
data class FluffyButtonRenderer (
    val style: String? = null,
    val icon: Icon? = null,
    val accessibility: AccessibilityAccessibilityData? = null,
    val trackingParams: String? = null,
    val accessibilityData: AccessibilityPauseDataClass? = null,
    val command: ButtonRendererCommand? = null
)

@Serializable
data class ButtonRendererCommand (
    val clickTrackingParams: String? = null,
    val shareEntityEndpoint: ShareEntityEndpoint? = null
)

@Serializable
data class ButtonMenuRenderer (
    val items: List<FluffyItem>? = null,
    val trackingParams: String? = null,
    val accessibility: AccessibilityPauseDataClass? = null
)

@Serializable
data class FluffyItem (
    val toggleMenuServiceItemRenderer: ToggleMenuServiceItemRenderer? = null,
    val menuNavigationItemRenderer: MenuItemRenderer? = null
)

@Serializable
data class ToggleMenuServiceItemRenderer (
    val defaultText: SubtitleClass? = null,
    val defaultIcon: Icon? = null,
    val defaultServiceEndpoint: DefaultEndpoint? = null,
    val toggledText: SubtitleClass? = null,
    val toggledIcon: Icon? = null,
    val toggledServiceEndpoint: ToggledServiceEndpoint? = null,
    val trackingParams: String? = null
)

@Serializable
data class DefaultEndpoint (
    val clickTrackingParams: String? = null,
    val modalEndpoint: ModalEndpoint? = null
)

@Serializable
data class ToggledServiceEndpoint (
    val clickTrackingParams: String? = null,
    val likeEndpoint: LikeEndpoint? = null
)

@Serializable
data class LikeEndpoint (
    val status: String? = null,
    //val target: Target? = null
)

/*
@Serializable
data class Target (
    @SerialName("playlistId")
    val playlistID: PlaylistID? = null
)

 */

@Serializable
data class ButtonToggleButtonRenderer (
    val isToggled: Boolean? = null,
    val isDisabled: Boolean? = null,
    val defaultIcon: Icon? = null,
    val defaultText: Text? = null,
    val toggledIcon: Icon? = null,
    val toggledText: Text? = null,
    val trackingParams: String? = null,
    val defaultNavigationEndpoint: DefaultEndpoint? = null
)

@Serializable
data class Text (
    val runs: List<DescriptionRun>? = null,
    val accessibility: AccessibilityPauseDataClass? = null
)

@Serializable
data class PurpleDescription (
    val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null
)

@Serializable
data class MusicDescriptionShelfRenderer (
    val header: SubtitleClass? = null,
    val description: SubtitleClass? = null,
    val moreButton: MoreButton? = null,
    val trackingParams: String? = null,
    val shelfStyle: String? = null
)

@Serializable
data class MoreButton (
    val toggleButtonRenderer: MoreButtonToggleButtonRenderer? = null
)

@Serializable
data class MoreButtonToggleButtonRenderer (
    val isToggled: Boolean? = null,
    val isDisabled: Boolean? = null,
    val defaultIcon: Icon? = null,
    val defaultText: SubtitleClass? = null,
    val toggledIcon: Icon? = null,
    val toggledText: SubtitleClass? = null,
    val trackingParams: String? = null
)

@Serializable
data class StraplineTextOne (
    val runs: List<StraplineTextOneRun>? = null
)

@Serializable
data class StraplineTextOneRun (
    val text: String? = null,
    val navigationEndpoint: FluffyNavigationEndpoint? = null
)

@Serializable
data class FluffyNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val browseEndpoint: FluffyBrowseEndpoint? = null
)

@Serializable
data class FluffyBrowseEndpoint (
    @SerialName("browseId")
    val browseID: String? = null,

    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null
)

@Serializable
class Subtitle()
