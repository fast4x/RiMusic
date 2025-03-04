package it.fast4x.rimusic.extensions.configuration

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.JsonArray
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.extensions.configuration.models.Configuration
import it.fast4x.rimusic.extensions.contributors.models.Developer
import it.fast4x.rimusic.extensions.contributors.models.Translator
import timber.log.Timber
import java.io.InputStream
import java.net.URL

private val GSON = Gson()
private lateinit var configurationList: List<Configuration>

private fun initConfiguration( context: Context) {
    try {
        val fileStream: InputStream =
            context.resources.openRawResource(R.raw.configuration)

        val json: JsonArray =
            GSON.fromJson(fileStream.bufferedReader(), JsonArray::class.java)

        configurationList = json.map { GSON.fromJson(it, Configuration::class.java) }
            .sortedBy { it.name }
    } catch ( e: Exception ) {
        Timber.e( e.stackTraceToString() )
        println("Configuration initConfiguration Exception: ${e.message}")
        configurationList = emptyList()
    }
}

fun getConfiguration(name: String? = null): String {

    if( !::configurationList.isInitialized )
        initConfiguration( appContext() )

    return configurationList.find { it.name == name }?.value ?: ""
}

fun getConfigurationsList(): List<Configuration> {

    if( !::configurationList.isInitialized )
        initConfiguration( appContext() )

    return configurationList
}

