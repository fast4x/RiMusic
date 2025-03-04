package it.fast4x.rimusic.ui.screens.settings

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ValidationType
import it.fast4x.rimusic.ui.components.themed.DialogColorPicker
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.Slider
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.StringListDialog
import it.fast4x.rimusic.ui.components.themed.Switch
import it.fast4x.rimusic.ui.components.themed.ValueSelectorDialog
import it.fast4x.rimusic.utils.color
import it.fast4x.rimusic.utils.secondary
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.ui.components.Skeleton
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.ui.components.themed.IDialog
import it.fast4x.rimusic.typography

@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun SettingsScreen(
    navController: NavController,
    miniPlayer: @Composable () -> Unit = {},
) {
    //val context = LocalContext.current
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabChanged) = rememberSaveable {
        mutableStateOf(0)
    }

            Skeleton(
                navController,
                tabIndex,
                onTabChanged,
                miniPlayer,
                navBarContent = { item ->
                    item(0, stringResource(R.string.tab_general), R.drawable.app_icon)
                    item(1, stringResource(R.string.ui_tab), R.drawable.ui)
                    item(2, stringResource(R.string.player_appearance), R.drawable.color_palette)
                    item(3, if (!isYouTubeLoggedIn()) stringResource(R.string.quick_picks)
                    else stringResource(R.string.home), if (!isYouTubeLoggedIn()) R.drawable.sparkles
                    else R.drawable.ytmusic)
                    item(4, stringResource(R.string.tab_data), R.drawable.server)
                    item(5, stringResource(R.string.tab_accounts), R.drawable.person)
                    item(6, stringResource(R.string.tab_miscellaneous), R.drawable.equalizer)
                    item(7, stringResource(R.string.about), R.drawable.information)

                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> GeneralSettings(navController = navController)
                        1 -> UiSettings(navController = navController)
                        2 -> AppearanceSettings(navController = navController)
                        3 -> QuickPicsSettings()
                        4 -> DataSettings()
                        5 -> AccountsSettings()
                        6 -> OtherSettings()
                        7 -> About()

                    }
                }
            }
}

@Composable
inline fun StringListValueSelectorSettingsEntry(
    title: String,
    text: String,
    addTitle: String,
    addPlaceholder: String,
    conflictTitle: String,
    removeTitle: String,
    context: Context,
    list: List<String>,
    crossinline add: (String) -> Unit,
    crossinline remove: (String) -> Unit
) {
    var showStringListDialog by remember {
        mutableStateOf(false)
    }


    if (showStringListDialog) {
        StringListDialog(
            title = title,
            addTitle = addTitle,
            addPlaceholder = addPlaceholder,
            removeTitle = removeTitle,
            conflictTitle = conflictTitle,
            list = list,
            add = add,
            remove = remove,
            onDismiss = { showStringListDialog = false },
        )
    }
    SettingsEntry(
        title = title,
        text = text,
        onClick = {
            showStringListDialog = true
        }
    )
}



@Composable
inline fun <reified T : Enum<T>> EnumValueSelectorSettingsEntry(
    title: String,
    titleSecondary: String? = null,
    text: String? = null,
    selectedValue: T,
    noinline onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    noinline valueText: @Composable (T) -> String  = { it.name },
    noinline trailingContent: (@Composable () -> Unit) = {}
) {
    ValueSelectorSettingsEntry(
        title = title,
        titleSecondary = titleSecondary,
        text = text,
        selectedValue = selectedValue,
        values = enumValues<T>().toList(),
        onValueSelected = onValueSelected,
        modifier = modifier,
        isEnabled = isEnabled,
        valueText = valueText,
        trailingContent = trailingContent,
    )
}

@Composable
fun <T> ValueSelectorSettingsEntry(
    title: String,
    titleSecondary: String? = null,
    text: String? = null,
    selectedValue: T,
    values: List<T>,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    valueText: @Composable (T) -> String = { it.toString() },
    trailingContent: (@Composable () -> Unit) = {}
) {
    var isShowingDialog by remember {
        mutableStateOf(false)
    }

    if (isShowingDialog) {
        ValueSelectorDialog(
            onDismiss = { isShowingDialog = false },
            title = title,
            selectedValue = selectedValue,
            values = values,
            onValueSelected = onValueSelected,
            valueText = valueText
        )
    }

    SettingsEntry(
        title = title,
        titleSecondary = titleSecondary,
        text = valueText(selectedValue),
        modifier = modifier,
        isEnabled = isEnabled,
        onClick = { isShowingDialog = true },
        trailingContent = trailingContent
    )

    text?.let {
        BasicText(
            text = it,
            style = typography().xs.semiBold.copy(color = colorPalette().textSecondary),
            modifier = Modifier
                .padding(start = 12.dp)
        )
    }
}

@Composable
fun SwitchSettingEntry(
    title: String,
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    SettingsEntry(
        title = title,
        text = text,
        isEnabled = isEnabled,
        onClick = { onCheckedChange(!isChecked) },
        trailingContent = { Switch(isChecked = isChecked) },
        modifier = modifier
    )
}

@Composable
fun SettingsEntry(
    title: String,
    titleSecondary: String? = null,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(enabled = isEnabled, onClick = onClick)
            .alpha(if (isEnabled) 1f else 0.5f)
            //.padding(start = 16.dp)
            //.padding(all = 16.dp)
            .padding(all = 12.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            BasicText(
                text = title,
                style = typography().xs.semiBold.copy(color = colorPalette().text),
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
            if (text != "")
                BasicText(
                    text = text,
                    style = typography().xs.semiBold.copy(color = colorPalette().textSecondary),
                )
        }

        trailingContent?.invoke()

        if (titleSecondary != null) {
            BasicText(
                text = titleSecondary,
                style = typography().xxs.secondary,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                //modifier = Modifier
                //    .padding(vertical = 8.dp, horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun SettingsTopDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        style = typography().xs.secondary,
        modifier = modifier
            .padding(start = 12.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsDescription(
    text: String,
    modifier: Modifier = Modifier,
    important: Boolean = false,
) {
    BasicText(
        text = text,
        style = if (important) typography().xxs.semiBold.color(colorPalette().red)
        else typography().xxs.secondary,
        modifier = modifier
            .padding(start = 12.dp)
            //.padding(horizontal = 12.dp)
            .padding(bottom = 8.dp)
    )
}

@Composable
fun ImportantSettingsDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        style = typography().xxs.semiBold.color(colorPalette().red),
        modifier = modifier
            .padding(start = 12.dp)
            .padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsEntryGroupText(
    title: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = title.uppercase(),
        style = typography().xs.semiBold.copy(colorPalette().accent),
        modifier = modifier
            .padding(start = 12.dp)
            //.padding(horizontal = 12.dp)
    )
}

@Composable
fun SettingsGroupSpacer(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .height(24.dp)
    )
}

@Composable
fun TextDialogSettingEntry(
    title: String,
    text: String,
    currentText: String,
    onTextSave: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    validationType: ValidationType = ValidationType.None
) {
    var showDialog by remember { mutableStateOf(false) }
    //val context = LocalContext.current

    if (showDialog) {
        InputTextDialog(
            onDismiss = { showDialog = false },
            title = title,
            value = currentText,
            placeholder = title,
            setValue = {
                onTextSave(it)
                //context.toast("Preference Saved")
            },
            validationType = validationType
        )
        /*
        TextFieldDialog(hintText = title ,
            onDismiss = { showDialog = false },
            onDone ={ value ->
                onTextSave(value)
                //context.toast("Preference Saved")
            },
            //doneText = "Save",
            initialTextInput = currentText
        )
         */
    }
    SettingsEntry(
        title = title,
        text = text,
        isEnabled = isEnabled,
        onClick = { showDialog = true },
        trailingContent = { },
        modifier = modifier
    )
}

@Composable
fun ColorSettingEntry(
    title: String,
    text: String,
    color: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    SettingsEntry(
        title = title,
        text = text,
        isEnabled = isEnabled,
        onClick = { showColorPicker = true },
        trailingContent = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(color)
                    .border(BorderStroke(1.dp, Color.LightGray))
            )
        },
        modifier = modifier
    )

    if (showColorPicker)
        DialogColorPicker(onDismiss = { showColorPicker = false }) {
            onColorSelected(it)
            showColorPicker = false
            SmartMessage(context.resources.getString(R.string.info_color_s_applied).format(title), context = context)
        }

}

@Composable
fun ButtonBarSettingEntry(
    title: String,
    text: String,
    icon: Int,
    iconSize: Dp = 24.dp,
    iconColor: Color? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    SettingsEntry(
        title = title,
        text = text,
        isEnabled = isEnabled,
        onClick = onClick,
        trailingContent = {
            Image(
                painter = painterResource(icon),
                colorFilter = ColorFilter.tint(iconColor ?: colorPalette().text),
                modifier = Modifier.size(iconSize),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        },
        modifier = modifier
    )

}

@Composable
fun SliderSettingsEntry(
    title: String,
    text: String,
    state: Float,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    onSlide: (Float) -> Unit = { },
    onSlideComplete: () -> Unit = { },
    toDisplay: @Composable (Float) -> String = { it.toString() },
    steps: Int = 0,
    isEnabled: Boolean = true,
    usePadding: Boolean = true
) = Column(modifier = modifier) {

    val manualEnterDialog = object: IDialog {

        var valueFloat: Float by remember( state ) { mutableFloatStateOf( state ) }

        override val dialogTitle: String
            @Composable
            get() = stringResource( R.string.enter_the_value )

        override var isActive: Boolean by rememberSaveable { mutableStateOf(false) }
        override var value: String by remember( valueFloat ) {
            mutableStateOf( "%.1f".format( valueFloat ).replace(",", ".") )
        }

        override fun onSet( newValue: String ) {
            this.valueFloat = newValue.toFloatOrNull() ?: return
            onSlide( this.valueFloat )
            onSlideComplete()

            onDismiss()
        }
    }
    manualEnterDialog.Render()

    SettingsEntry(
        title = title,
        text = "$text (${toDisplay(state)})",
        onClick = manualEnterDialog::onShortClick,
        isEnabled = isEnabled,
        //usePadding = usePadding
    )

    Slider(
        state = state,
        setState = { value: Float ->
            manualEnterDialog.valueFloat = value
            onSlide(value)
        },
        onSlideComplete = onSlideComplete,
        range = range,
        steps = steps,
        modifier = Modifier
            .height(36.dp)
            .alpha(if (isEnabled) 1f else 0.5f)
            .let { if (usePadding) it.padding(start = 32.dp, end = 16.dp) else it }
            .padding(vertical = 16.dp)
            .fillMaxWidth()
    )
}

@Composable
fun SettingsGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    description: String? = null,
    important: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) = Column(modifier = modifier) {
    if (title != null) {
        SettingsEntryGroupText(title = title)
    }

    description?.let { description ->
        SettingsDescription(
            text = description,
            important = important
        )
    }

    content()

    SettingsGroupSpacer()
}