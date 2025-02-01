package it.fast4x.rimusic.ui.screens.localplaylist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.github.doyaaaaaken.kotlincsv.client.KotlinCsvExperimental
import it.fast4x.compose.persist.PersistMapCleanup
import it.fast4x.rimusic.enums.PlayerPosition
import it.fast4x.rimusic.enums.TransitionEffect
import it.fast4x.rimusic.enums.UiType
import it.fast4x.rimusic.utils.playerPositionKey
import it.fast4x.rimusic.utils.rememberPreference
import it.fast4x.rimusic.utils.transitionEffectKey
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.ui.components.navigation.header.AppHeader

@OptIn(KotlinCsvExperimental::class)
@ExperimentalMaterialApi
@ExperimentalTextApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@UnstableApi
@Composable
fun LocalPlaylistScreen(
    navController: NavController,
    playlistId: Long,
    modifier: Modifier = Modifier,
    miniPlayer: @Composable () -> Unit = {}
) {
    val transitionEffect by rememberPreference(transitionEffectKey, TransitionEffect.Scale)
    val playerPosition by rememberPreference(playerPositionKey, PlayerPosition.Bottom)
    val saveableStateHolder = rememberSaveableStateHolder()
    PersistMapCleanup(tagPrefix = "localPlaylist/$playlistId/")

            androidx.compose.material3.Scaffold(
                modifier = modifier,
                containerColor = colorPalette().background0,
                topBar = {
                    if( UiType.RiMusic.isCurrent() )
                        AppHeader( navController ).Draw()
                }
            ) {
                //**
                Box(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {

                    Row(
                        modifier = modifier
                            .background(colorPalette().background0)
                            .fillMaxSize()
                    ) {
                        val topPadding = if ( UiType.ViMusic.isCurrent() ) 30.dp else 0.dp

                        AnimatedContent(
                            targetState = 0,
                            transitionSpec = {
                                when (transitionEffect) {
                                    TransitionEffect.None -> EnterTransition.None togetherWith ExitTransition.None
                                    TransitionEffect.Expand -> expandIn(
                                        animationSpec = tween(
                                            350,
                                            easing = LinearOutSlowInEasing
                                        ), expandFrom = Alignment.BottomStart
                                    ).togetherWith(
                                        shrinkOut(
                                            animationSpec = tween(
                                                350,
                                                easing = FastOutSlowInEasing
                                            ), shrinkTowards = Alignment.CenterStart
                                        )
                                    )

                                    TransitionEffect.Fade -> fadeIn(animationSpec = tween(350)).togetherWith(
                                        fadeOut(animationSpec = tween(350))
                                    )

                                    TransitionEffect.Scale -> scaleIn(animationSpec = tween(350)).togetherWith(
                                        scaleOut(animationSpec = tween(350))
                                    )

                                    TransitionEffect.SlideHorizontal, TransitionEffect.SlideVertical -> {
                                        val slideDirection = when (targetState > initialState) {
                                            true -> {
                                                if (transitionEffect == TransitionEffect.SlideHorizontal)
                                                    AnimatedContentTransitionScope.SlideDirection.Left
                                                else AnimatedContentTransitionScope.SlideDirection.Up
                                            }

                                            false -> {
                                                if (transitionEffect == TransitionEffect.SlideHorizontal)
                                                    AnimatedContentTransitionScope.SlideDirection.Right
                                                else AnimatedContentTransitionScope.SlideDirection.Down
                                            }
                                        }

                                        val animationSpec = spring(
                                            dampingRatio = 0.9f,
                                            stiffness = Spring.StiffnessLow,
                                            visibilityThreshold = IntOffset.VisibilityThreshold
                                        )

                                        slideIntoContainer(slideDirection, animationSpec) togetherWith
                                                slideOutOfContainer(slideDirection, animationSpec)
                                    }
                                }
                            },
                            label = "",
                            modifier = Modifier
                                //.fillMaxWidth()
                                .fillMaxHeight()
                                .padding(top = topPadding)
                        ) { currentTabIndex ->
                            saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                                when (currentTabIndex) {
                                    0 -> LocalPlaylistSongs(
                                        navController = navController,
                                        playlistId = playlistId,
                                        onDelete = {} //pop
                                    )
                                }
                            }
                        }
                    }
                    //**
                    Box(
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .align(if (playerPosition == PlayerPosition.Top) Alignment.TopCenter
                            else Alignment.BottomCenter)
                    ) {
                        miniPlayer.invoke()
                    }
                }
            }
}
