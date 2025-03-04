package it.fast4x.rimusic.enums

import it.fast4x.rimusic.enums.WallpaperType.Both
import it.fast4x.rimusic.enums.WallpaperType.Home
import it.fast4x.rimusic.enums.WallpaperType.Lockscreen

enum class ViewType {
    List,
    Grid;

    val displayName: String
        get() = when (this) {
            List -> "List"
            Grid -> "Grid"
        }
}