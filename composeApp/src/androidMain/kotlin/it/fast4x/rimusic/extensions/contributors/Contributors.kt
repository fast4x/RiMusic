package it.fast4x.rimusic.extensions.contributors

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
import it.fast4x.rimusic.extensions.contributors.models.Developer
import it.fast4x.rimusic.extensions.contributors.models.Translator
import timber.log.Timber
import java.io.InputStream
import java.net.URL

private val GSON = Gson()
private lateinit var developersList: List<Developer>
private lateinit var translatorsList: List<Translator>


@Composable
fun countDevelopers(): Int {
    val context = LocalContext.current

    if( !::developersList.isInitialized )
        initDevelopers( context )

    return if ( developersList.isEmpty() ) 0
    else
        developersList.size
}



private fun initDevelopers( context: Context) {
    try {
        val fileStream: InputStream =
            context.resources.openRawResource(R.raw.contributors)

        val json: JsonArray =
            GSON.fromJson(fileStream.bufferedReader(), JsonArray::class.java)

//        val result = URL("https://raw.githubusercontent.com/fast4x/RiMusic/master/composeApp/src/androidMain/res/raw/contributors.json").readText()
//        val json = GSON.fromJson(result, JsonArray::class.java)

        developersList = json.map { GSON.fromJson(it, Developer::class.java) }
            .sortedBy { it.username }
    } catch ( e: Exception ) {
        Timber.e( e.stackTraceToString() )
        println("Contributors initDevelopers Exception: ${e.message}")
        developersList = emptyList()
    }
}

@Composable
fun ShowDevelopers() {
    val context = LocalContext.current

    if( !::developersList.isInitialized )
        initDevelopers( context )

    if ( developersList.isEmpty() ) return

    Box( Modifier.fillMaxWidth().height( 600.dp ) ) {
        LazyColumn( Modifier.fillMaxWidth().padding( top = 15.dp ) ) {
            items( developersList.filterNot { it.username == "eddisch2010" } ) { it.Draw() }
        }
    }
}


private fun initTranslators( context: Context) {
    try {
        val fileStream: InputStream =
            context.resources.openRawResource(R.raw.translators)
        val json: JsonArray =
            GSON.fromJson(fileStream.bufferedReader(), JsonArray::class.java)

//        val result = URL("https://raw.githubusercontent.com/fast4x/RiMusic/master/composeApp/src/androidMain/res/raw/translators.json").readText()
//        val json = GSON.fromJson(result, JsonArray::class.java)

        translatorsList = json.map { GSON.fromJson(it, Translator::class.java) }
            .sortedBy { it.displayName }
    } catch ( e: Exception ) {
        Timber.e( e.stackTraceToString() )
        println("Contributors initTranslators Exception: ${e.message}")
        translatorsList = emptyList()
    }
}

@Composable
fun countTranslators(): Int {
    val context = LocalContext.current

    if( !::translatorsList.isInitialized )
        initTranslators( context )

    return if ( translatorsList.isEmpty() ) 0
    else
        translatorsList.size
}

@Composable
fun ShowTranslators() {
    val context = LocalContext.current

    if( !::translatorsList.isInitialized )
        initTranslators( context )

    if ( translatorsList.isEmpty() ) return

    Box( Modifier.fillMaxWidth().height( 600.dp ) ) {
        LazyColumn( Modifier.fillMaxWidth().padding( top = 15.dp ) ) {
            items( translatorsList.filterNot { it.username == "eddisch2010" } ) { it.Draw() }
        }
    }
}