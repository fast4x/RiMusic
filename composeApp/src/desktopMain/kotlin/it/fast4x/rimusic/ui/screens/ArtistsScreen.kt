package it.fast4x.rimusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.fast4x.environment.Environment
import it.fast4x.environment.models.bodies.NextBody
import it.fast4x.environment.requests.relatedPage

@Composable
fun ArtistsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Artists")

            val relatedPage = remember { mutableStateOf<Environment.RelatedPage?>(null) }
            LaunchedEffect(Unit) {
                Environment.relatedPage(
                    NextBody(
                        videoId = ("HZnNt9nnEhw")
                    )
                )?.onSuccess {
                    relatedPage.value = it
                }
            }

            //BasicText(text = "Songs: ${relatedPage.value?.songs?.size.toString()}")
            //BasicText(text = "commonDao db song ${MusicDatabaseDesktop.getAllSongs()}")


        }

    }

}