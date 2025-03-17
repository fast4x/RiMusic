package it.fast4x.rimusic.utils


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.Languages
import me.bush.translator.Language
import java.util.Locale

fun getSystemlanguage(): Languages {
    val locale = Locale.getDefault()
    val languageTag = locale.toLanguageTag().replace("-Hant", "")
    return Languages.languageFromcode (languageTag.take(2))
        ?: Languages.languageFromcode (languageTag)
        ?: Languages.English
}

@Composable
fun languageDestination (
    language: Languages? = null,
): Language {
    //val languageApp  by rememberPreference(languageAppKey, Languages.English)
    val otherLanguageApp  by rememberPreference(otherLanguageAppKey, Languages.English)
    //Timber.d("LanguageDestination: language $language otherLanguageApp $otherLanguageApp")

    return when (language ?: otherLanguageApp) {
        Languages.Afrikaans -> Language.AFRIKAANS
        Languages.Arabic -> Language.ARABIC
        Languages.Azerbaijani -> Language.AZERBAIJANI
        Languages.Bashkir -> Language.BASQUE
        Languages.Basque -> Language.BASQUE
        Languages.Bengali -> Language.BENGALI
        Languages.Catalan -> Language.CATALAN
        Languages.ChineseSimplified -> Language.CHINESE_SIMPLIFIED
        Languages.ChineseTraditional -> Language.CHINESE_TRADITIONAL
        Languages.Czech -> Language.CZECH
        Languages.Danish -> Language.DANISH
        Languages.Dutch -> Language.DUTCH
        Languages.English -> Language.ENGLISH
        Languages.Esperanto -> Language.ESPERANTO
        Languages.Estonian -> Language.ESTONIAN
        Languages.Filipino -> Language.FILIPINO
        Languages.Finnish -> Language.FINNISH
        Languages.French -> Language.FRENCH
        Languages.Galician -> Language.GALICIAN
        Languages.German -> Language.GERMAN
        Languages.Greek -> Language.GREEK
        Languages.Hebrew -> Language.HEBREW_HE
        Languages.Hindi -> Language.HINDI
        Languages.Hungarian -> Language.HUNGARIAN
        Languages.Indonesian -> Language.INDONESIAN
        Languages.Interlingua -> Language.LATIN
        Languages.Irish -> Language.IRISH
        Languages.Japanese -> Language.JAPANESE
        Languages.Korean -> Language.KOREAN
        Languages.Italian -> Language.ITALIAN
        Languages.Malayalam -> Language.MALAYALAM
        Languages.Norwegian -> Language.NORWEGIAN
        Languages.Odia -> Language.ODIA
        //Languages.Persian -> Language.PERSIAN
        Languages.Polish -> Language.POLISH
        Languages.PortugueseBrazilian -> Language.PORTUGUESE
        Languages.Portuguese -> Language.PORTUGUESE
        Languages.Romanian -> Language.ROMANIAN
        Languages.Russian -> Language.RUSSIAN
        Languages.SerbianCyrillic, Languages.SerbianLatin -> Language.SERBIAN
        Languages.Sinhala -> Language.SINHALA
        Languages.Spanish -> Language.SPANISH
        Languages.Swedish -> Language.SWEDISH
        Languages.Tamil -> Language.TAMIL
        Languages.Telugu -> Language.TELUGU
        Languages.Turkish -> Language.TURKISH
        Languages.Ukrainian -> Language.UKRAINIAN
        Languages.Vietnamese -> Language.VIETNAMESE
        else -> Language.ENGLISH
    }
}

@Composable
fun languageDestinationName (
    language: Languages? = null,
): String {
    //val languageApp  by rememberPreference(languageAppKey, Languages.English)
    val otherLanguageApp  by rememberPreference(otherLanguageAppKey, Languages.English)
    //Timber.d("LanguageDestination: language $language otherLanguageApp $otherLanguageApp")

    return when (language ?: otherLanguageApp) {
        Languages.System -> stringResource(R.string.system_language)
        Languages.Afrikaans -> stringResource(R.string.lang_afrikaans)
        Languages.Arabic -> stringResource(R.string.arabic)
        Languages.Azerbaijani -> stringResource(R.string.lang_azerbaijani)
        Languages.Bashkir -> stringResource(R.string.bashkir)
        Languages.Basque -> stringResource(R.string.lang_basque)
        Languages.Bengali -> stringResource(R.string.lang_bengali)
        Languages.Catalan -> stringResource(R.string.catalan)
        Languages.ChineseSimplified -> stringResource(R.string.chinese_simplified)
        Languages.ChineseTraditional -> stringResource(R.string.chinese_traditional)
        Languages.Czech -> stringResource(R.string.czech)
        Languages.Danish -> stringResource(R.string.lang_danish)
        Languages.Dutch -> stringResource(R.string.lang_dutch)
        Languages.English -> stringResource(R.string.english)
        Languages.Esperanto -> stringResource(R.string.esperanto)
        Languages.Estonian -> stringResource(R.string.lang_estonian)
        Languages.Filipino -> stringResource(R.string.lang_filipino)
        Languages.Finnish -> stringResource(R.string.lang_finnish)
        Languages.French -> stringResource(R.string.french)
        Languages.Galician -> stringResource(R.string.lang_galician)
        Languages.German -> stringResource(R.string.german)
        Languages.Greek -> stringResource(R.string.greek)
        Languages.Hebrew -> stringResource(R.string.lang_hebrew)
        Languages.Hindi -> stringResource(R.string.lang_hindi)
        Languages.Hungarian -> stringResource(R.string.hungarian)
        Languages.Indonesian -> stringResource(R.string.indonesian)
        Languages.Interlingua -> stringResource(R.string.lang_interlingua)
        Languages.Irish -> stringResource(R.string.lang_irish)
        Languages.Japanese -> stringResource(R.string.lang_japanese)
        Languages.Korean -> stringResource(R.string.korean)
        Languages.Italian -> stringResource(R.string.italian)
        Languages.Malayalam -> stringResource(R.string.lang_malayalam)
        Languages.Norwegian -> stringResource(R.string.lang_norwegian)
        Languages.Odia -> stringResource(R.string.odia)
        //Languages.Persian -> stringResource(R.string.persian)
        Languages.Polish -> stringResource(R.string.polish)
        Languages.PortugueseBrazilian -> stringResource(R.string.portuguese_brazilian)
        Languages.Portuguese -> stringResource(R.string.portuguese)
        Languages.Romanian -> stringResource(R.string.romanian)
        //Languages.RomanianEmo -> stringResource(R.string.romanian_emoticons_rom_n)
        Languages.Russian -> stringResource(R.string.russian)
        Languages.SerbianCyrillic -> stringResource(R.string.lang_serbian_cyrillic)
        Languages.SerbianLatin -> stringResource(R.string.lang_serbian_latin)
        Languages.Sinhala -> stringResource(R.string.lang_sinhala)
        Languages.Spanish -> stringResource(R.string.spanish)
        Languages.Swedish -> stringResource(R.string.lang_swedish)
        Languages.Tamil -> stringResource(R.string.lang_tamil)
        Languages.Telugu -> stringResource(R.string.lang_telugu)
        Languages.Turkish -> stringResource(R.string.turkish)
        Languages.Ukrainian -> stringResource(R.string.lang_ukrainian)
        Languages.Vietnamese -> stringResource(R.string.lang_vietnamese)
    }
}