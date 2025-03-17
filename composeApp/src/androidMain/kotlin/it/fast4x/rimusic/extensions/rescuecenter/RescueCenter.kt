package it.fast4x.rimusic.extensions.rescuecenter

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.R
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.PopupType
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog
import it.fast4x.rimusic.ui.components.themed.InputTextDialog
import it.fast4x.rimusic.ui.components.themed.SmartMessage
import it.fast4x.rimusic.ui.components.themed.Title
import it.fast4x.rimusic.utils.bold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess


@Composable
fun RescueScreen(
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {

    var isExportingCrashLog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val noLogAvailable = stringResource(R.string.no_log_available)
    var fileName by remember {
        mutableStateOf("")
    }
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val file =
                File(context.filesDir.resolve("logs"),
                    "RiMusic_crash_log.txt"
                )
            if (!file.exists()) {
                SmartMessage(noLogAvailable, type = PopupType.Info, context = context)
                return@rememberLauncherForActivityResult
            }

            context.applicationContext.contentResolver.openOutputStream(uri)
                ?.use { outputStream ->
                    FileInputStream( file ).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

        }

    if (isExportingCrashLog) {
        InputTextDialog(
            onDismiss = {
                isExportingCrashLog = false
            },
            title = "Enter the name of log export",
            value = "",
            placeholder = "Enter the name of log export",
            setValue = { txt ->
                fileName = txt
                try {
                    @SuppressLint("SimpleDateFormat")
                    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                    exportLauncher.launch("RiMusic_CrashLog_${txt.take(20)}_${dateFormat.format(
                        Date()
                    )}")
                } catch (e: ActivityNotFoundException) {
                    SmartMessage("Couldn't find an application to create documents",
                        type = PopupType.Warning, context = context)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorPalette().background0)
            .navigationBarsPadding()
            .padding(all = 30.dp)
            .padding(top = 10.dp)
    ) {


        Column(
            verticalArrangement = Arrangement.SpaceAround
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.app_icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette().accent),
                    modifier = Modifier
                        .padding(all = 5.dp)
                        .size(30.dp)
                )
                BasicText(
                    text = stringResource(R.string.rescue_center),
                    style = typography().xl.bold
                )
            }

            Title(
                title = stringResource(R.string.export_the_database),
                icon = R.drawable.rescue,
                modifier = Modifier.fillMaxWidth()
                    .background(colorPalette().background1),
                onClick = {
                    isExporting = true
                }
            )
            Spacer( modifier = Modifier.height(20.dp) )
            Title(
                title = stringResource(R.string.import_the_database),
                icon = R.drawable.rescue,
                modifier = Modifier.fillMaxWidth()
                    .background(colorPalette().background1),
                onClick = {
                    isImporting = true
                }
            )
            Spacer( modifier = Modifier.height(20.dp) )
            Title(
                title = stringResource(R.string.export_crash_log),
                icon = R.drawable.rescue,
                modifier = Modifier.fillMaxWidth()
                    .background(colorPalette().background1),
                onClick = {
                    isExportingCrashLog = true
                }
            )
            Spacer( modifier = Modifier.height(50.dp) )
            Title(
                title = stringResource(R.string.click_to_close),
                icon = R.drawable.close,
                modifier = Modifier.fillMaxWidth()
                    .background(colorPalette().background1),
                onClick = {
                    exit()
                }
            )

        }



        if (isExporting)
            ExportRescueBackup(
                onDismiss = { isExporting = false },
                onbackup = onBackup
            )

        if (isImporting)
            ImportRescueBackup(
                onDismiss = { isImporting = false },
                onRestore = onRestore
            )

    }
}


@Composable
fun ExportRescueBackup(
    onDismiss: () -> Unit,
    onbackup: () -> Unit = {}
) {
    ConfirmationDialog(
        text = stringResource(R.string.export_the_database),
        onDismiss = onDismiss,
        onConfirm =  onbackup
    )

}

@Composable
fun ImportRescueBackup(
    onDismiss: () -> Unit = {},
    onRestore: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(true) }
        ConfirmationDialog(
            text = stringResource(R.string.import_the_database),
            onDismiss = {
                showDialog = false
                onDismiss()
            },
            onConfirm =  onRestore
        )

}

fun exit(){
    CoroutineScope(Dispatchers.IO).launch{
        delay(2000)
    }

    // Terminate the app or perform any other necessary action
    android.os.Process.killProcess(android.os.Process.myPid())
    exitProcess(0)
}