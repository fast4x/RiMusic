package it.fast4x.rimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.LocalPlayerAwareWindowInsets
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.NavigationBarPosition
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.internal
import it.fast4x.rimusic.path
import it.fast4x.rimusic.query
import it.fast4x.rimusic.service.MyDownloadService
import it.fast4x.rimusic.service.PlayerService
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.DefaultDialog
import it.fast4x.rimusic.ui.components.themed.HeaderWithIcon
import it.fast4x.rimusic.ui.components.themed.SmartToast
import it.fast4x.rimusic.ui.styling.Dimensions
import it.fast4x.rimusic.ui.styling.LocalAppearance
import it.fast4x.rimusic.ui.styling.shimmer
import it.fast4x.rimusic.utils.bold
import it.fast4x.rimusic.utils.intent
import it.fast4x.rimusic.utils.navigationBarPositionKey
import it.fast4x.rimusic.utils.preferences
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.semiBold
import it.fast4x.rimusic.utils.toast
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess
@UnstableApi
@ExperimentalAnimationApi
@Composable
fun DatabaseSettings() {
    val context = LocalContext.current
    val (colorPalette, typography) = LocalAppearance.current

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.sqlite3")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()

                context.applicationContext.contentResolver.openOutputStream(uri)
                    ?.use { outputStream ->
                        FileInputStream(Database.internal.path).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
            }
        }

    var exitAfterRestore by remember { mutableStateOf(false) }

    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            query {
                Database.checkpoint()
                Database.internal.close()

                context.applicationContext.contentResolver.openInputStream(uri)
                    ?.use { inputStream ->
                        FileOutputStream(Database.internal.path).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                context.stopService(context.intent<PlayerService>())
                context.stopService(context.intent<MyDownloadService>())

                exitAfterRestore = true
            }
        }

    if (exitAfterRestore)
        DefaultDialog(
            onDismiss = { exitAfterRestore = false },
            content = {
                BasicText(
                    text = stringResource(R.string.restore_completed),
                    style = typography.s.bold.copy(color = colorPalette.text),
                )
                Spacer(modifier = Modifier.height(20.dp))
                BasicText(
                    text = stringResource(R.string.click_to_close),
                    style = typography.xs.semiBold.copy(color = colorPalette.textSecondary),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    painter = painterResource(R.drawable.server),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.shimmer),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            exitAfterRestore = false
                            exitProcess(0)
                        }
                )
            }

        )

    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    if (isExporting) {
        ConfirmationDialog(
            text = stringResource(R.string.export_the_database),
            onDismiss = { isExporting = false },
            onConfirm = {
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                try {
                    backupLauncher.launch("rimusic_${dateFormat.format(Date())}.db")
                } catch (e: ActivityNotFoundException) {
                    SmartToast(context.resources.getString(R.string.info_not_find_app_create_doc), type = PopupType.Warning)
                }
            }
        )
    }
    if (isImporting) {
        ConfirmationDialog(
            text = stringResource(R.string.import_the_database),
            onDismiss = { isImporting = false },
            onConfirm = {
                try {
                    restoreLauncher.launch(
                        arrayOf(
                            "application/vnd.sqlite3",
                            "application/x-sqlite3",
                            "application/octet-stream"
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    SmartToast(context.resources.getString(R.string.info_not_find_app_open_doc), type = PopupType.Warning)
                }
            }
        )
    }

    val navigationBarPosition by rememberPreference(navigationBarPositionKey, NavigationBarPosition.Bottom)

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            //.fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth(if (navigationBarPosition == NavigationBarPosition.Left ||
                navigationBarPosition == NavigationBarPosition.Top ||
                navigationBarPosition == NavigationBarPosition.Bottom) 1f
            else Dimensions.contentWidthRightBar)
            .verticalScroll(rememberScrollState())
            /*
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )

             */
    ) {
        HeaderWithIcon(
            title = stringResource(R.string.database),
            iconId = R.drawable.server,
            enabled = false,
            showIcon = true,
            modifier = Modifier,
            onClick = {}
        )

        //SettingsEntryGroupText(title = stringResource(R.string.cleanup))



        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.backup))

        SettingsDescription(text = stringResource(R.string.personal_preference))

        SettingsEntry(
            title = stringResource(R.string.backup_1),
            text = stringResource(R.string.export_the_database),
            onClick = {
                isExporting = true
            }
        )


        SettingsGroupSpacer()

        SettingsEntryGroupText(title = stringResource(R.string.restore))

        ImportantSettingsDescription(text = stringResource(
            R.string.existing_data_will_be_overwritten,
            context.applicationInfo.nonLocalizedLabel
        ))

        SettingsEntry(
            title = stringResource(R.string.restore_1),
            text = stringResource(R.string.import_the_database),
            onClick = {
                isImporting = true
            }
        )
    }
}
