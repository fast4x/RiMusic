package it.fast4x.environment.utils

object LocalePreferences {
    var preference: LocalePreferenceItem? = null
}

data class LocalePreferenceItem(
    var hl: String,
    var gl: String
)