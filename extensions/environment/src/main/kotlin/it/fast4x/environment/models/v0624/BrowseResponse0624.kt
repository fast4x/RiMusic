package it.fast4x.environment.models.v0624

// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json               = Json { allowStructuredMapKeys = true }
// val browseResponse0624 = json.parse(BrowseResponse0624.serializer(), jsonString)

import kotlinx.serialization.*

@Serializable
data class BrowseResponse0624 (
    val contents: Contents,
    val trackingParams: String,
    val microformat: Microformat? = null,
    val background: StraplineThumbnailClass? = null
)

@Serializable
data class StraplineThumbnailClass (
    val musicThumbnailRenderer: MusicThumbnailRenderer
)

@Serializable
data class MusicThumbnailRenderer (
    val thumbnail: MusicThumbnailRendererThumbnail,
    val thumbnailCrop: String,
    val thumbnailScale: String,
    val trackingParams: String
)

@Serializable
data class MusicThumbnailRendererThumbnail (
    val thumbnails: List<ThumbnailElement>
)

@Serializable
data class ThumbnailElement (
    val url: String,
    val width: Long,
    val height: Long
)

@Serializable
data class Contents (
    val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer
)

@Serializable
data class TwoColumnBrowseResultsRenderer (
    val secondaryContents: SecondaryContents,
    val tabs: List<Tab>
)

@Serializable
data class SecondaryContents (
    val sectionListRenderer: SecondaryContentsSectionListRenderer
)

@Serializable
data class SecondaryContentsSectionListRenderer (
    val contents: List<PurpleContent>,
    val trackingParams: String
)

@Serializable
data class PurpleContent (
    val musicShelfRenderer: MusicShelfRenderer? = null,
)

@Serializable
data class MusicShelfRenderer (
    val contents: List<MusicShelfRendererContent>,
    val trackingParams: String,
    val shelfDivider: ShelfDivider,
    val contentsMultiSelectable: Boolean
)

@Serializable
data class MusicShelfRendererContent (
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer
)

@Serializable
data class MusicResponsiveListItemRenderer (
    val trackingParams: String,
    val overlay: Overlay,
    val flexColumns: List<FlexColumn>,
    val fixedColumns: List<FixedColumn>,
    val menu: Menu? = null,
    val badges: List<Badge>? = null,
    val playlistItemData: PlaylistItemData? = null,
    val itemHeight: ItemHeight,
    val index: Index,
    val multiSelectCheckbox: MultiSelectCheckbox? = null
)

@Serializable
data class Badge (
    val musicInlineBadgeRenderer: MusicInlineBadgeRenderer
)

@Serializable
data class MusicInlineBadgeRenderer (
    val trackingParams: String,
    val icon: Icon,
    val accessibilityData: Accessibility
)

@Serializable
data class Accessibility (
    val accessibilityData: AccessibilityData
)

@Serializable
data class AccessibilityData (
    val label: String
)

@Serializable
data class Icon (
    val iconType: String
)

@Serializable
data class FixedColumn (
    val musicResponsiveListItemFixedColumnRenderer: MusicResponsiveListItemFixedColumnRenderer
)

@Serializable
data class MusicResponsiveListItemFixedColumnRenderer (
    val text: Index,
    val displayPriority: DisplayPriority,
    val size: Size
)

@Serializable
enum class DisplayPriority(val value: String) {
    @SerialName("MUSIC_RESPONSIVE_LIST_ITEM_COLUMN_DISPLAY_PRIORITY_HIGH") MusicResponsiveListItemColumnDisplayPriorityHigh("MUSIC_RESPONSIVE_LIST_ITEM_COLUMN_DISPLAY_PRIORITY_HIGH");
}

@Serializable
enum class Size(val value: String) {
    @SerialName("MUSIC_RESPONSIVE_LIST_ITEM_FIXED_COLUMN_SIZE_SMALL") MusicResponsiveListItemFixedColumnSizeSmall("MUSIC_RESPONSIVE_LIST_ITEM_FIXED_COLUMN_SIZE_SMALL");
}

@Serializable
data class Index (
    val runs: List<IndexRun>
)

@Serializable
data class IndexRun (
    val text: String
)

@Serializable
data class FlexColumn (
    val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer
)

@Serializable
data class MusicResponsiveListItemFlexColumnRenderer (
    val text: Text,
    val displayPriority: DisplayPriority
)

@Serializable
data class Text (
    val runs: List<PurpleRun>? = null
)

@Serializable
data class PurpleRun (
    val text: String,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class NavigationEndpoint (
    val clickTrackingParams: String,
    val watchEndpoint: PlayNavigationEndpointWatchEndpoint? = null,
)

@Serializable
data class PlayNavigationEndpointWatchEndpoint (
    @SerialName("videoId")
    val videoID: String,

    @SerialName("playlistId")
    val playlistID: String,

    val loggingContext: LoggingContext,
    val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs,
    val params: Params? = null,
    val index: Long? = null
)

@Serializable
data class LoggingContext (
    val vssLoggingContext: VssLoggingContext
)

@Serializable
data class VssLoggingContext (
    val serializedContextData: String
)

@Serializable
enum class Params(val value: String) {
    @SerialName("wAEB") WAEB("wAEB");
}

@Serializable
data class WatchEndpointMusicSupportedConfigs (
    val watchEndpointMusicConfig: WatchEndpointMusicConfig
)

@Serializable
data class WatchEndpointMusicConfig (
    val musicVideoType: MusicVideoType
)

@Serializable
enum class MusicVideoType(val value: String) {
    @SerialName("MUSIC_VIDEO_TYPE_ATV") MusicVideoTypeAtv("MUSIC_VIDEO_TYPE_ATV"),
    @SerialName("MUSIC_VIDEO_TYPE_OMV") MusicVideoTypeOmv("MUSIC_VIDEO_TYPE_OMV");
}

@Serializable
enum class ItemHeight(val value: String) {
    @SerialName("MUSIC_RESPONSIVE_LIST_ITEM_HEIGHT_MEDIUM") MusicResponsiveListItemHeightMedium("MUSIC_RESPONSIVE_LIST_ITEM_HEIGHT_MEDIUM");
}

@Serializable
data class Menu (
    val menuRenderer: MenuMenuRenderer
)

@Serializable
data class MenuMenuRenderer (
    val items: List<PurpleItem>,
    val trackingParams: String,
    val topLevelButtons: List<TopLevelButton>,
    val accessibility: Accessibility
)

@Serializable
data class PurpleItem (
    val menuNavigationItemRenderer: MenuItemRenderer? = null,
    val menuServiceItemRenderer: MenuItemRenderer? = null
)

@Serializable
data class MenuItemRenderer (
    val text: Index,
    val icon: Icon,
    val navigationEndpoint: MenuNavigationItemRendererNavigationEndpoint? = null,
    val trackingParams: String,
    val serviceEndpoint: ServiceEndpoint? = null
)

@Serializable
data class MenuNavigationItemRendererNavigationEndpoint (
    val clickTrackingParams: String,
    val watchEndpoint: PlayNavigationEndpointWatchEndpoint? = null,
    val modalEndpoint: ModalEndpoint? = null,
    val browseEndpoint: BrowseEndpoint? = null,
    val shareEntityEndpoint: ShareEntityEndpoint? = null,
    val watchPlaylistEndpoint: WatchPlaylistEndpoint? = null
)

@Serializable
data class BrowseEndpoint (
    @SerialName("browseId")
    val browseID: String,

    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs
)

@Serializable
data class BrowseEndpointContextSupportedConfigs (
    val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig
)

@Serializable
data class BrowseEndpointContextMusicConfig (
    val pageType: PageType
)

@Serializable
enum class PageType(val value: String) {
    @SerialName("MUSIC_PAGE_TYPE_TRACK_CREDITS") MusicPageTypeTrackCredits("MUSIC_PAGE_TYPE_TRACK_CREDITS"),
    @SerialName("MUSIC_PAGE_TYPE_ARTIST") MusicPageTypeArtist("MUSIC_PAGE_TYPE_ARTIST");
}

@Serializable
enum class BrowseID(val value: String) {
    @SerialName("UCMHHDXMvnMKRzuEKtMe8ZRA") UCMHHDXMvnMKRzuEKtMe8ZRA("UCMHHDXMvnMKRzuEKtMe8ZRA");
}

@Serializable
data class ModalEndpoint (
    val modal: Modal
)

@Serializable
data class Modal (
    val modalWithTitleAndButtonRenderer: ModalWithTitleAndButtonRenderer
)

@Serializable
data class ModalWithTitleAndButtonRenderer (
    val title: Index,
    val content: Index,
    val button: ModalWithTitleAndButtonRendererButton
)

@Serializable
data class ModalWithTitleAndButtonRendererButton (
    val buttonRenderer: ButtonRenderer
)

@Serializable
data class ButtonRenderer (
    val style: Style,
    val isDisabled: Boolean,
    val text: Index,
    val navigationEndpoint: ButtonRendererNavigationEndpoint,
    val trackingParams: String
)

@Serializable
data class ButtonRendererNavigationEndpoint (
    val clickTrackingParams: String,
    val signInEndpoint: SignInEndpoint
)

@Serializable
data class SignInEndpoint (
    val hack: Boolean
)

@Serializable
enum class Style(val value: String) {
    @SerialName("STYLE_BLUE_TEXT") StyleBlueText("STYLE_BLUE_TEXT");
}

@Serializable
data class ShareEntityEndpoint (
    val serializedShareEntity: String,
    val sharePanelType: SharePanelType
)

@Serializable
enum class SharePanelType(val value: String) {
    @SerialName("SHARE_PANEL_TYPE_UNIFIED_SHARE_PANEL") SharePanelTypeUnifiedSharePanel("SHARE_PANEL_TYPE_UNIFIED_SHARE_PANEL");
}

@Serializable
data class WatchPlaylistEndpoint (
    @SerialName("playlistId")
    val playlistID: String,

    val params: String
)

@Serializable
data class ServiceEndpoint (
    val clickTrackingParams: String,
    val queueAddEndpoint: QueueAddEndpoint
)

@Serializable
data class QueueAddEndpoint (
    val queueTarget: QueueTarget,
    val queueInsertPosition: QueueInsertPosition,
    val commands: List<Command>
)

@Serializable
data class Command (
    val clickTrackingParams: String,
    val addToToastAction: AddToToastAction
)

@Serializable
data class AddToToastAction (
    val item: AddToToastActionItem
)

@Serializable
data class AddToToastActionItem (
    val notificationTextRenderer: NotificationTextRenderer
)

@Serializable
data class NotificationTextRenderer (
    val successResponseText: Index,
    val trackingParams: String
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

    val onEmptyQueue: OnEmptyQueue,

    @SerialName("playlistId")
    val playlistID: String? = null
)

@Serializable
data class OnEmptyQueue (
    val clickTrackingParams: String,
    val watchEndpoint: OnEmptyQueueWatchEndpoint
)

@Serializable
data class OnEmptyQueueWatchEndpoint (
    @SerialName("videoId")
    val videoID: String? = null,

    @SerialName("playlistId")
    val playlistID: String? = null
)

@Serializable
data class TopLevelButton (
    val likeButtonRenderer: LikeButtonRenderer
)

@Serializable
data class LikeButtonRenderer (
    val target: LikeButtonRendererTarget,
    val likeStatus: Status,
    val trackingParams: String,
    val likesAllowed: Boolean,
    val dislikeNavigationEndpoint: DefaultNavigationEndpoint,
    val likeCommand: DefaultNavigationEndpoint
)

@Serializable
data class DefaultNavigationEndpoint (
    val clickTrackingParams: String,
    val modalEndpoint: ModalEndpoint
)

@Serializable
enum class Status(val value: String) {
    @SerialName("INDIFFERENT") Indifferent("INDIFFERENT");
}

@Serializable
data class LikeButtonRendererTarget (
    @SerialName("videoId")
    val videoID: String
)

@Serializable
data class MultiSelectCheckbox (
    val checkboxRenderer: CheckboxRenderer
)

@Serializable
data class CheckboxRenderer (
    val onSelectionChangeCommand: OnSelectionChangeCommand,
    val checkedState: CheckedState,
    val trackingParams: String
)

@Serializable
enum class CheckedState(val value: String) {
    @SerialName("CHECKBOX_CHECKED_STATE_UNCHECKED") CheckboxCheckedStateUnchecked("CHECKBOX_CHECKED_STATE_UNCHECKED");
}

@Serializable
data class OnSelectionChangeCommand (
    val clickTrackingParams: String,
    val updateMultiSelectStateCommand: UpdateMultiSelectStateCommand
)

@Serializable
data class UpdateMultiSelectStateCommand (
    val multiSelectParams: String,
    val multiSelectItem: String
)

@Serializable
enum class MultiSelectParams(val value: String) {
    @SerialName("CAMSKU9MQUs1dXlfa0RxT2c0ejZTWkNRT2k1SVA4Yzh5cGNGQjNHbTN4d2hR") CAMSKU9MQUs1DXlfa0RxT2C0EjZTWkNRT2K1SVA4Yzh5CGNGQjNHbTN4D2HR("CAMSKU9MQUs1dXlfa0RxT2c0ejZTWkNRT2k1SVA4Yzh5cGNGQjNHbTN4d2hR");
}

@Serializable
data class Overlay (
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer
)

@Serializable
data class MusicItemThumbnailOverlayRenderer (
    val background: MusicItemThumbnailOverlayRendererBackground,
    val content: MusicItemThumbnailOverlayRendererContent,
    val contentPosition: ContentPosition,
    val displayStyle: DisplayStyle
)

@Serializable
data class MusicItemThumbnailOverlayRendererBackground (
    val verticalGradient: VerticalGradient
)

@Serializable
data class VerticalGradient (
    val gradientLayerColors: List<String>
)

@Serializable
data class MusicItemThumbnailOverlayRendererContent (
    val musicPlayButtonRenderer: ContentMusicPlayButtonRenderer
)

@Serializable
data class ContentMusicPlayButtonRenderer (
    val playNavigationEndpoint: NavigationEndpoint? = null,
    val trackingParams: String,
    val playIcon: Icon,
    val pauseIcon: Icon,
    val iconColor: Long,
    val backgroundColor: Long,
    val activeBackgroundColor: Long,
    val loadingIndicatorColor: Long,
    val playingIcon: Icon,
    val iconLoadingColor: Long,
    val activeScaleFactor: Long,
    val buttonSize: ButtonSize,
    val rippleTarget: RippleTarget,
    val accessibilityPlayData: Accessibility? = null,
    val accessibilityPauseData: Accessibility? = null
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
enum class DisplayStyle(val value: String) {
    @SerialName("MUSIC_ITEM_THUMBNAIL_OVERLAY_DISPLAY_STYLE_PERSISTENT") MusicItemThumbnailOverlayDisplayStylePersistent("MUSIC_ITEM_THUMBNAIL_OVERLAY_DISPLAY_STYLE_PERSISTENT");
}

@Serializable
data class PlaylistItemData (
    @SerialName("playlistSetVideoId")
    val playlistSetVideoID: String,

    @SerialName("videoId")
    val videoID: String
)

@Serializable
data class ShelfDivider (
    val musicShelfDividerRenderer: MusicShelfDividerRenderer
)

@Serializable
data class MusicShelfDividerRenderer (
    val hidden: Boolean
)

@Serializable
data class Tab (
    val tabRenderer: TabRenderer
)

@Serializable
data class TabRenderer (
    val content: TabRendererContent,
    val trackingParams: String
)

@Serializable
data class TabRendererContent (
    val sectionListRenderer: ContentSectionListRenderer
)

@Serializable
data class ContentSectionListRenderer (
    val contents: List<FluffyContent>? = null,
    val trackingParams: String
)

@Serializable
data class FluffyContent (
    val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer
)

@Serializable
data class MusicResponsiveHeaderRenderer (
    val thumbnail: StraplineThumbnailClass,
    val buttons: List<ButtonElement>,
    val title: Index,
    val subtitle: Index,
    val trackingParams: String,
    val straplineTextOne: StraplineTextOne? = null,
    val straplineThumbnail: StraplineThumbnailClass? = null,
    val subtitleBadge: List<Badge>? = null,
    val description: MusicResponsiveHeaderRendererDescription? = null,
    val secondSubtitle: Index
)

@Serializable
data class ButtonElement (
    val toggleButtonRenderer: ButtonToggleButtonRenderer? = null,
    val musicPlayButtonRenderer: ButtonMusicPlayButtonRenderer? = null,
    val menuRenderer: ButtonMenuRenderer? = null
)

@Serializable
data class ButtonMenuRenderer (
    val items: List<FluffyItem>,
    val trackingParams: String,
    val accessibility: Accessibility
)

@Serializable
data class FluffyItem (
    val menuNavigationItemRenderer: MenuItemRenderer? = null,
    val menuServiceItemRenderer: MenuItemRenderer? = null,
    val toggleMenuServiceItemRenderer: ToggleMenuServiceItemRenderer? = null
)

@Serializable
data class ToggleMenuServiceItemRenderer (
    val defaultText: Index,
    val defaultIcon: Icon,
    val defaultServiceEndpoint: DefaultNavigationEndpoint,
    val toggledText: Index,
    val toggledIcon: Icon,
    val toggledServiceEndpoint: ToggledServiceEndpoint,
    val trackingParams: String
)

@Serializable
data class ToggledServiceEndpoint (
    val clickTrackingParams: String,
    val likeEndpoint: LikeEndpoint
)

@Serializable
data class LikeEndpoint (
    val status: Status,
    val target: LikeEndpointTarget
)

@Serializable
data class LikeEndpointTarget (
    @SerialName("playlistId")
    val playlistID: String
)

@Serializable
data class ButtonMusicPlayButtonRenderer (
    val playNavigationEndpoint: NavigationEndpoint,
    val trackingParams: String,
    val playIcon: Icon,
    val pauseIcon: Icon,
    val iconColor: Long,
    val backgroundColor: Long,
    val activeBackgroundColor: Long,
    val loadingIndicatorColor: Long,
    val playingIcon: Icon,
    val iconLoadingColor: Long,
    val activeScaleFactor: Long,
    val accessibilityPlayData: Accessibility,
    val accessibilityPauseData: Accessibility
)

@Serializable
data class ButtonToggleButtonRenderer (
    val isToggled: Boolean,
    val isDisabled: Boolean,
    val defaultIcon: Icon,
    val toggledIcon: Icon,
    val trackingParams: String,
    val defaultNavigationEndpoint: DefaultNavigationEndpoint,
    val accessibilityData: Accessibility,
    val toggledAccessibilityData: Accessibility
)

@Serializable
data class MusicResponsiveHeaderRendererDescription (
    val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer
)

@Serializable
data class MusicDescriptionShelfRenderer (
    val description: MusicDescriptionShelfRendererDescription,
    val moreButton: MoreButton,
    val trackingParams: String,
    val shelfStyle: String,
    val straplineBadge: List<Badge>? = null
)

@Serializable
data class MusicDescriptionShelfRendererDescription (
    val runs: List<DescriptionRun>
)

@Serializable
data class DescriptionRun (
    val text: String,
    val navigationEndpoint: PurpleNavigationEndpoint? = null
)

@Serializable
data class PurpleNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val urlEndpoint: URLEndpoint? = null
)

@Serializable
data class URLEndpoint (
    val url: String,
    val target: String
)

@Serializable
data class MoreButton (
    val toggleButtonRenderer: MoreButtonToggleButtonRenderer
)

@Serializable
data class MoreButtonToggleButtonRenderer (
    val isToggled: Boolean,
    val isDisabled: Boolean,
    val defaultIcon: Icon,
    val defaultText: Index,
    val toggledIcon: Icon,
    val toggledText: Index,
    val trackingParams: String
)

@Serializable
data class StraplineTextOne (
    val runs: List<StraplineTextOneRun>
)

@Serializable
data class StraplineTextOneRun (
    val text: String,
    val navigationEndpoint: FluffyNavigationEndpoint? = null
)

@Serializable
data class FluffyNavigationEndpoint (
    val clickTrackingParams: String,
    val browseEndpoint: BrowseEndpoint
)

@Serializable
data class Microformat (
    val microformatDataRenderer: MicroformatDataRenderer
)

@Serializable
data class MicroformatDataRenderer (
    val urlCanonical: String
)
