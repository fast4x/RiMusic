package it.fast4x.environment.models.v0624.charts

// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                 = Json { allowStructuredMapKeys = true }
// val browseChartsResponse = json.parse(BrowseChartsResponse.serializer(), jsonString)

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrowseChartsResponse0624 (
    val contents: Contents? = null,
    val header: BrowseChartsResponseHeader? = null,
    val trackingParams: String? = null,
    val frameworkUpdates: FrameworkUpdates? = null
)

@Serializable
data class Contents (
    val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer? = null
)

@Serializable
data class SingleColumnBrowseResultsRenderer (
    val tabs: List<Tab>? = null
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
    val sectionListRenderer: SectionListRenderer? = null
)

@Serializable
data class SectionListRenderer (
    val contents: List<SectionListRendererContent>? = null,
    val trackingParams: String? = null
)

@Serializable
data class SectionListRendererContent (
    val musicShelfRenderer: MusicShelfRenderer? = null,
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer? = null
)

@Serializable
data class MusicCarouselShelfRenderer (
    val header: MusicCarouselShelfRendererHeader? = null,
    val contents: List<MusicCarouselShelfRendererContent>? = null,
    val trackingParams: String? = null,
    val itemSize: String? = null,
    val numItemsPerColumn: String? = null
)

@Serializable
data class MusicCarouselShelfRendererContent (
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer? = null,
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null
)

@Serializable
data class MusicResponsiveListItemRenderer (
    val trackingParams: String? = null,
    val thumbnail: ThumbnailRendererClass? = null,
    val flexColumns: List<FlexColumn>? = null,
    val menu: MusicResponsiveListItemRendererMenu? = null,
    val flexColumnDisplayStyle: String? = null,
    val navigationEndpoint: MusicResponsiveListItemRendererNavigationEndpoint? = null,
    val itemHeight: ItemHeight? = null,
    val customIndexColumn: CustomIndexColumn? = null,
    val overlay: Overlay? = null,
    val playlistItemData: PlaylistItemData? = null
)

@Serializable
data class CustomIndexColumn (
    val musicCustomIndexColumnRenderer: MusicCustomIndexColumnRenderer? = null
)

@Serializable
data class MusicCustomIndexColumnRenderer (
    val text: TextClass? = null,
    //val icon: Icon? = null,
    //val iconColorStyle: IconColorStyle? = null,
    val accessibilityData: AccessibilityData? = null
)

@Serializable
data class AccessibilityData (
    val accessibilityData: AccessibilityDataAccessibilityData? = null
)

@Serializable
data class AccessibilityDataAccessibilityData (
    val label: String? = null
)
/*
@Serializable
data class Icon (
    val iconType: IconType? = null
)


@Serializable
enum class IconType(val value: String) {
    @SerialName("ADD_TO_PLAYLIST") AddToPlaylist("ADD_TO_PLAYLIST"),
    @SerialName("ADD_TO_REMOTE_QUEUE") AddToRemoteQueue("ADD_TO_REMOTE_QUEUE"),
    @SerialName("ARROW_CHART_NEUTRAL") ArrowChartNeutral("ARROW_CHART_NEUTRAL"),
    @SerialName("ARROW_DROP_DOWN") ArrowDropDown("ARROW_DROP_DOWN"),
    @SerialName("ARROW_DROP_UP") ArrowDropUp("ARROW_DROP_UP"),
    @SerialName("ARTIST") Artist("ARTIST"),
    @SerialName("CHECK") Check("CHECK"),
    @SerialName("FAVORITE") Favorite("FAVORITE"),
    @SerialName("MIX") Mix("MIX"),
    @SerialName("MUSIC_SHUFFLE") MusicShuffle("MUSIC_SHUFFLE"),
    @SerialName("PAUSE") Pause("PAUSE"),
    @SerialName("PLAY_ARROW") PlayArrow("PLAY_ARROW"),
    @SerialName("QUEUE_PLAY_NEXT") QueuePlayNext("QUEUE_PLAY_NEXT"),
    @SerialName("SHARE") Share("SHARE"),
    @SerialName("SUBSCRIBE") Subscribe("SUBSCRIBE"),
    @SerialName("UNFAVORITE") Unfavorite("UNFAVORITE"),
    @SerialName("VOLUME_UP") VolumeUp("VOLUME_UP");
}

@Serializable
enum class IconColorStyle(val value: String) {
    @SerialName("CUSTOM_INDEX_COLUMN_ICON_COLOR_STYLE_GREEN") CustomIndexColumnIconColorStyleGreen("CUSTOM_INDEX_COLUMN_ICON_COLOR_STYLE_GREEN"),
    @SerialName("CUSTOM_INDEX_COLUMN_ICON_COLOR_STYLE_GREY") CustomIndexColumnIconColorStyleGrey("CUSTOM_INDEX_COLUMN_ICON_COLOR_STYLE_GREY"),
    @SerialName("CUSTOM_INDEX_COLUMN_ICON_COLOR_STYLE_RED") CustomIndexColumnIconColorStyleRed("CUSTOM_INDEX_COLUMN_ICON_COLOR_STYLE_RED");
}

 */
@Serializable
data class TextClass (
    val runs: List<DefaultTextRun>? = null
)

@Serializable
data class DefaultTextRun (
    val text: String? = null
)

@Serializable
data class FlexColumn (
    val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer? = null
)

@Serializable
data class MusicResponsiveListItemFlexColumnRenderer (
    val text: Text? = null,
    val displayPriority: DisplayPriority? = null
)

@Serializable
enum class DisplayPriority(val value: String) {
    @SerialName("MUSIC_RESPONSIVE_LIST_ITEM_COLUMN_DISPLAY_PRIORITY_HIGH") MusicResponsiveListItemColumnDisplayPriorityHigh("MUSIC_RESPONSIVE_LIST_ITEM_COLUMN_DISPLAY_PRIORITY_HIGH"),
    @SerialName("MUSIC_RESPONSIVE_LIST_ITEM_COLUMN_DISPLAY_PRIORITY_MEDIUM") MusicResponsiveListItemColumnDisplayPriorityMedium("MUSIC_RESPONSIVE_LIST_ITEM_COLUMN_DISPLAY_PRIORITY_MEDIUM");
}

@Serializable
data class Text (
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
    val watchEndpoint: WatchEndpoint? = null,
    val browseEndpoint: NavigationEndpointBrowseEndpoint? = null
)

@Serializable
data class NavigationEndpointBrowseEndpoint (
    @SerialName("browseId")
    val browseID: String? = null,

    //val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null
)
/*
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
    @SerialName("MUSIC_PAGE_TYPE_ARTIST") MusicPageTypeArtist("MUSIC_PAGE_TYPE_ARTIST"),
    @SerialName("MUSIC_PAGE_TYPE_PLAYLIST") MusicPageTypePlaylist("MUSIC_PAGE_TYPE_PLAYLIST"),
    @SerialName("MUSIC_PAGE_TYPE_USER_CHANNEL") MusicPageTypeUserChannel("MUSIC_PAGE_TYPE_USER_CHANNEL");
}
*/

@Serializable
data class WatchEndpoint (
    @SerialName("videoId")
    val videoID: String? = null,

    @SerialName("playlistId")
    val playlistID: String? = null,

    val loggingContext: LoggingContext? = null,
    //val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs? = null,
    //val params: Params? = null
)

@Serializable
data class LoggingContext (
    val vssLoggingContext: VssLoggingContext? = null
)

@Serializable
data class VssLoggingContext (
    val serializedContextData: String? = null
)

/*
@Serializable
enum class Params(val value: String) {
    @SerialName("wAEB") WAEB("wAEB"),
    @SerialName("wAEB8gECGAE%3D") WAEB8GECGAE3D("wAEB8gECGAE%3D");
}
 */

/*
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
    @SerialName("MUSIC_VIDEO_TYPE_OMV") MusicVideoTypeOmv("MUSIC_VIDEO_TYPE_OMV"),
    @SerialName("MUSIC_VIDEO_TYPE_UGC") MusicVideoTypeUgc("MUSIC_VIDEO_TYPE_UGC");
}

 */

@Serializable
enum class ItemHeight(val value: String) {
    @SerialName("MUSIC_RESPONSIVE_LIST_ITEM_HEIGHT_MEDIUM_COMPACT") MusicResponsiveListItemHeightMediumCompact("MUSIC_RESPONSIVE_LIST_ITEM_HEIGHT_MEDIUM_COMPACT");
}

@Serializable
data class MusicResponsiveListItemRendererMenu (
    val menuRenderer: PurpleMenuRenderer? = null
)

@Serializable
data class PurpleMenuRenderer (
    val items: List<ItemElement>? = null,
    val trackingParams: String? = null,
    val accessibility: AccessibilityData? = null,
    val topLevelButtons: List<TopLevelButton>? = null
)

@Serializable
data class ItemElement (
    val menuNavigationItemRenderer: MenuItemRenderer? = null,
    val toggleMenuServiceItemRenderer: ToggleMenuServiceItemRenderer? = null,
    val menuServiceItemRenderer: MenuItemRenderer? = null
)

@Serializable
data class MenuItemRenderer (
    val text: TextClass? = null,
    //val icon: Icon? = null,
    val navigationEndpoint: MenuNavigationItemRendererNavigationEndpoint? = null,
    val trackingParams: String? = null,
    val serviceEndpoint: ServiceEndpoint? = null
)

@Serializable
data class MenuNavigationItemRendererNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val watchPlaylistEndpoint: WatchPlaylistEndpoint? = null,
    val shareEntityEndpoint: ShareEntityEndpoint? = null,
    val watchEndpoint: WatchEndpoint? = null,
    val modalEndpoint: ModalEndpoint? = null,
    val browseEndpoint: NavigationEndpointBrowseEndpoint? = null
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
    val title: TextClass? = null,
    val content: TextClass? = null,
    val button: Button? = null
)

@Serializable
data class Button (
    val buttonRenderer: ButtonButtonRenderer? = null
)

@Serializable
data class ButtonButtonRenderer (
    val style: Style? = null,
    val isDisabled: Boolean? = null,
    val text: TextClass? = null,
    val navigationEndpoint: FluffyNavigationEndpoint? = null,
    val trackingParams: String? = null
)

@Serializable
data class FluffyNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val signInEndpoint: MusicMenuItemDividerRenderer? = null
)

@Serializable
data class MusicMenuItemDividerRenderer (
    val hack: Boolean? = null
)

@Serializable
enum class Style(val value: String) {
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
data class WatchPlaylistEndpoint (
    @SerialName("playlistId")
    val playlistID: String? = null,

    //val params: Params? = null
)

@Serializable
data class ServiceEndpoint (
    val clickTrackingParams: String? = null,
    val queueAddEndpoint: QueueAddEndpoint? = null
)

@Serializable
data class QueueAddEndpoint (
    val queueTarget: QueueTarget? = null,
    val queueInsertPosition: QueueInsertPosition? = null,
    val commands: List<QueueAddEndpointCommand>? = null
)

@Serializable
data class QueueAddEndpointCommand (
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
    val successResponseText: TextClass? = null,
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
    val watchEndpoint: PlaylistItemData? = null
)

@Serializable
data class PlaylistItemData (
    @SerialName("videoId")
    val videoID: String? = null
)

@Serializable
data class ToggleMenuServiceItemRenderer (
    val defaultText: TextClass? = null,
    //val defaultIcon: Icon? = null,
    val defaultServiceEndpoint: DefaultServiceEndpoint? = null,
    val toggledText: TextClass? = null,
    //val toggledIcon: Icon? = null,
    val trackingParams: String? = null
)

@Serializable
data class DefaultServiceEndpoint (
    val clickTrackingParams: String? = null,
    val modalEndpoint: ModalEndpoint? = null
)

@Serializable
data class TopLevelButton (
    val likeButtonRenderer: LikeButtonRenderer? = null
)

@Serializable
data class LikeButtonRenderer (
    val target: PlaylistItemData? = null,
    val likeStatus: LikeStatus? = null,
    val trackingParams: String? = null,
    val likesAllowed: Boolean? = null,
    val dislikeNavigationEndpoint: DefaultServiceEndpoint? = null,
    val likeCommand: DefaultServiceEndpoint? = null
)

@Serializable
enum class LikeStatus(val value: String) {
    @SerialName("INDIFFERENT") Indifferent("INDIFFERENT");
}

@Serializable
data class MusicResponsiveListItemRendererNavigationEndpoint (
    val clickTrackingParams: String? = null,
    val browseEndpoint: NavigationEndpointBrowseEndpoint? = null
)

@Serializable
data class Overlay (
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class MusicItemThumbnailOverlayRenderer (
    val background: Background? = null,
    val content: MusicItemThumbnailOverlayRendererContent? = null,
    val contentPosition: ContentPosition? = null,
    val displayStyle: DisplayStyle? = null
)

@Serializable
data class Background (
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
    val playNavigationEndpoint: NavigationEndpoint? = null,
    val trackingParams: String? = null,
    //val playIcon: Icon? = null,
    //val pauseIcon: Icon? = null,
    val iconColor: Long? = null,
    val backgroundColor: Long? = null,
    val activeBackgroundColor: Long? = null,
    val loadingIndicatorColor: Long? = null,
    //val playingIcon: Icon? = null,
    val iconLoadingColor: Long? = null,
    val activeScaleFactor: Long? = null,
    val buttonSize: ButtonSize? = null,
    val rippleTarget: RippleTarget? = null,
    val accessibilityPlayData: AccessibilityData? = null,
    val accessibilityPauseData: AccessibilityData? = null
)

@Serializable
enum class ButtonSize(val value: String) {
    @SerialName("MUSIC_PLAY_BUTTON_SIZE_HUGE") MusicPlayButtonSizeHuge("MUSIC_PLAY_BUTTON_SIZE_HUGE"),
    @SerialName("MUSIC_PLAY_BUTTON_SIZE_SMALL") MusicPlayButtonSizeSmall("MUSIC_PLAY_BUTTON_SIZE_SMALL");
}

@Serializable
data class NavigationEndpoint (
    val clickTrackingParams: String? = null,
    val watchEndpoint: WatchEndpoint? = null
)

@Serializable
enum class RippleTarget(val value: String) {
    @SerialName("MUSIC_PLAY_BUTTON_RIPPLE_TARGET_ANCESTOR") MusicPlayButtonRippleTargetAncestor("MUSIC_PLAY_BUTTON_RIPPLE_TARGET_ANCESTOR"),
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
data class ThumbnailRendererClass (
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
) {
    fun toThumbnail(): it.fast4x.environment.models.Thumbnail? {
        return it.fast4x.environment.models.Thumbnail(
            url = url ?: "",
            width = width?.toInt(),
            height = height?.toInt()
        )
    }
}

@Serializable
enum class ThumbnailCrop(val value: String) {
    @SerialName("MUSIC_THUMBNAIL_CROP_CIRCLE") MusicThumbnailCropCircle("MUSIC_THUMBNAIL_CROP_CIRCLE"),
    @SerialName("MUSIC_THUMBNAIL_CROP_UNSPECIFIED") MusicThumbnailCropUnspecified("MUSIC_THUMBNAIL_CROP_UNSPECIFIED");
}

@Serializable
enum class ThumbnailScale(val value: String) {
    @SerialName("MUSIC_THUMBNAIL_SCALE_ASPECT_FILL") MusicThumbnailScaleAspectFill("MUSIC_THUMBNAIL_SCALE_ASPECT_FILL"),
    @SerialName("MUSIC_THUMBNAIL_SCALE_ASPECT_FIT") MusicThumbnailScaleAspectFit("MUSIC_THUMBNAIL_SCALE_ASPECT_FIT");
}

@Serializable
data class MusicTwoRowItemRenderer (
    val thumbnailRenderer: ThumbnailRendererClass? = null,
    val aspectRatio: AspectRatio? = null,
    val title: TextClass? = null,
    val subtitle: SubtitleClass? = null,
    val navigationEndpoint: NavigationEndpoint? = null,
    val trackingParams: String? = null,
    val menu: MusicTwoRowItemRendererMenu? = null,
    val thumbnailOverlay: Overlay? = null,
    val customIndexColumn: CustomIndexColumn? = null
)

@Serializable
enum class AspectRatio(val value: String) {
    @SerialName("MUSIC_TWO_ROW_ITEM_THUMBNAIL_ASPECT_RATIO_RECTANGLE_16_9") MusicTwoRowItemThumbnailAspectRatioRectangle16_9("MUSIC_TWO_ROW_ITEM_THUMBNAIL_ASPECT_RATIO_RECTANGLE_16_9");
}

@Serializable
data class MusicTwoRowItemRendererMenu (
    val menuRenderer: FluffyMenuRenderer? = null
)

@Serializable
data class FluffyMenuRenderer (
    val items: List<ItemElement>? = null,
    val trackingParams: String? = null,
    val accessibility: AccessibilityData? = null
)

@Serializable
data class SubtitleClass (
    val runs: List<SubtitleRun>? = null
)

@Serializable
data class SubtitleRun (
    val text: String? = null,
    val navigationEndpoint: MusicResponsiveListItemRendererNavigationEndpoint? = null
)

@Serializable
data class MusicCarouselShelfRendererHeader (
    val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer? = null
)

@Serializable
data class MusicCarouselShelfBasicHeaderRenderer (
    val title: SubtitleClass? = null,
    val accessibilityData: AccessibilityData? = null,
    val headerStyle: String? = null,
    val moreContentButton: MoreContentButton? = null,
    val trackingParams: String? = null
)

@Serializable
data class MoreContentButton (
    val buttonRenderer: MoreContentButtonButtonRenderer? = null
)

@Serializable
data class MoreContentButtonButtonRenderer (
    val style: String? = null,
    val text: TextClass? = null,
    val navigationEndpoint: MusicResponsiveListItemRendererNavigationEndpoint? = null,
    val trackingParams: String? = null,
    val accessibilityData: AccessibilityData? = null
)

@Serializable
data class MusicShelfRenderer (
    val trackingParams: String? = null,
    val shelfDivider: ShelfDivider? = null,
    val subheaders: List<Subheader>? = null
)

@Serializable
data class ShelfDivider (
    val musicShelfDividerRenderer: MusicShelfDividerRenderer? = null
)

@Serializable
data class MusicShelfDividerRenderer (
    val hidden: Boolean? = null
)

@Serializable
data class Subheader (
    val musicSideAlignedItemRenderer: MusicSideAlignedItemRenderer? = null
)

@Serializable
data class MusicSideAlignedItemRenderer (
    val startItems: List<StartItem>? = null,
    val trackingParams: String? = null
)

@Serializable
data class StartItem (
    val musicSortFilterButtonRenderer: MusicSortFilterButtonRenderer? = null
)

@Serializable
data class MusicSortFilterButtonRenderer (
    val title: TextClass? = null,
    //val icon: Icon? = null,
    val menu: MusicSortFilterButtonRendererMenu? = null,
    val accessibility: AccessibilityData? = null,
    val trackingParams: String? = null
)

@Serializable
data class MusicSortFilterButtonRendererMenu (
    val musicMultiSelectMenuRenderer: MusicMultiSelectMenuRenderer? = null
)

@Serializable
data class MusicMultiSelectMenuRenderer (
    val title: MusicMultiSelectMenuRendererTitle? = null,
    val options: List<Option>? = null,
    val trackingParams: String? = null,
    val formEntityKey: ID? = null
)

@Serializable
enum class ID(val value: String) {
    @SerialName("EiVleHBsb3JlX2NoYXJ0c19jb3VudHJ5X21lbnVfMzE2NzY2NTY3IJABKAE%3D") EiVleHBsb3JlX2NoYXJ0C19Jb3VudHJ5X21LbnVfMzE2NzY2NTY3IJABKAE3D("EiVleHBsb3JlX2NoYXJ0c19jb3VudHJ5X21lbnVfMzE2NzY2NTY3IJABKAE%3D");
}

@Serializable
data class Option (
    val musicMultiSelectMenuItemRenderer: MusicMultiSelectMenuItemRenderer? = null,
    val musicMenuItemDividerRenderer: MusicMenuItemDividerRenderer? = null
)

@Serializable
data class MusicMultiSelectMenuItemRenderer (
    val title: TextClass? = null,
    val formItemEntityKey: String? = null,
    val trackingParams: String? = null,
    //val selectedIcon: Icon? = null,
    val selectedAccessibility: AccessibilityData? = null,
    val deselectedAccessibility: AccessibilityData? = null,
    val selectedCommand: SelectedCommand? = null
)

@Serializable
data class SelectedCommand (
    val clickTrackingParams: String? = null,
    val commandExecutorCommand: CommandExecutorCommand? = null
)

@Serializable
data class CommandExecutorCommand (
    val commands: List<CommandExecutorCommandCommand>? = null
)

@Serializable
data class CommandExecutorCommandCommand (
    val clickTrackingParams: String? = null,
    val musicCheckboxFormItemMutatedCommand: MusicCheckboxFormItemMutatedCommand? = null,
    val musicBrowseFormBinderCommand: MusicBrowseFormBinderCommand? = null
)

@Serializable
data class MusicBrowseFormBinderCommand (
    val browseEndpoint: MusicBrowseFormBinderCommandBrowseEndpoint? = null,
    val formEntityKey: ID? = null
)

@Serializable
data class MusicBrowseFormBinderCommandBrowseEndpoint (
    @SerialName("browseId")
    val browseID: BrowseID? = null,

    val navigationType: NavigationType? = null
)

@Serializable
enum class BrowseID(val value: String) {
    @SerialName("FEmusic_charts") FEmusicCharts("FEmusic_charts");
}

@Serializable
enum class NavigationType(val value: String) {
    @SerialName("BROWSE_NAVIGATION_TYPE_LOAD_IN_PLACE") BrowseNavigationTypeLoadInPlace("BROWSE_NAVIGATION_TYPE_LOAD_IN_PLACE");
}

@Serializable
data class MusicCheckboxFormItemMutatedCommand (
    val formItemEntityKey: String? = null,
    val newCheckedState: Boolean? = null
)

@Serializable
data class MusicMultiSelectMenuRendererTitle (
    val musicMenuTitleRenderer: MusicMenuTitleRenderer? = null
)

@Serializable
data class MusicMenuTitleRenderer (
    val primaryText: TextClass? = null
)

@Serializable
data class FrameworkUpdates (
    val entityBatchUpdate: EntityBatchUpdate? = null
)

@Serializable
data class EntityBatchUpdate (
    val mutations: List<Mutation>? = null,
    val timestamp: Timestamp? = null
)

@Serializable
data class Mutation (
    val entityKey: String? = null,
    val type: Type? = null,
    val payload: Payload? = null
)

@Serializable
data class Payload (
    val musicForm: MusicForm? = null,
    val musicFormBooleanChoice: MusicFormBooleanChoice? = null
)

@Serializable
data class MusicForm (
    val id: ID? = null,
    val booleanChoiceEntityKeys: List<String>? = null
)

@Serializable
data class MusicFormBooleanChoice (
    val id: String? = null,
    val parentFormEntityKey: ID? = null,
    val selected: Boolean? = null,
    val opaqueToken: String? = null
)

@Serializable
enum class Type(val value: String) {
    @SerialName("ENTITY_MUTATION_TYPE_REPLACE") EntityMutationTypeReplace("ENTITY_MUTATION_TYPE_REPLACE");
}

@Serializable
data class Timestamp (
    val seconds: String? = null,
    val nanos: Long? = null
)

@Serializable
data class BrowseChartsResponseHeader (
    val musicHeaderRenderer: MusicHeaderRenderer? = null
)

@Serializable
data class MusicHeaderRenderer (
    val title: TextClass? = null,
    val trackingParams: String? = null
)
