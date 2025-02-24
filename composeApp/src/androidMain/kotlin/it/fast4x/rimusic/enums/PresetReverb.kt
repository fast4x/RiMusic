package it.fast4x.rimusic.enums

import android.media.audiofx.PresetReverb

enum class PresetsReverb {
    NONE,
    SMALLROOM,
    MEDIUMROOM,
    LARGEROOM,
    MEDIUMHALL,
    LARGEHALL,
    PLATE;

    val preset: Short
        get() = when (this) {
            NONE -> PresetReverb.PRESET_NONE
            SMALLROOM -> PresetReverb.PRESET_SMALLROOM
            MEDIUMROOM -> PresetReverb.PRESET_MEDIUMROOM
            LARGEROOM -> PresetReverb.PRESET_LARGEROOM
            MEDIUMHALL -> PresetReverb.PRESET_MEDIUMHALL
            LARGEHALL -> PresetReverb.PRESET_LARGEHALL
            PLATE -> PresetReverb.PRESET_PLATE
        }

    val textName: String
        get() = when (this) {
            NONE -> "None"
            SMALLROOM -> "Small Room"
            MEDIUMROOM -> "Medium Room"
            LARGEROOM -> "Large Room"
            MEDIUMHALL -> "Medium Hall"
            LARGEHALL -> "Large Hall"
            PLATE -> "Plate"
        }



}