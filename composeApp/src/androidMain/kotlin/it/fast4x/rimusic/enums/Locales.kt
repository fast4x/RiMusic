package it.fast4x.rimusic.enums

enum class Languages {
    System,
    Afrikaans,
    Arabic,
    Azerbaijani,
    Bashkir,
    Bengali,
    Catalan,
    Danish,
    English,
    Esperanto,
    Estonian,
    ChineseSimplified,
    ChineseTraditional,
    Czech,
    Dutch,
    Filipino,
    Finnish,
    French,
    Galician,
    German,
    Greek,
    Hebrew,
    Hindi,
    Hungarian,
    Italian,
    Indonesian,
    Interlingua,
    Irish,
    Japanese,
    Korean,
    Malayalam,
    Norwegian,
    Odia,
    //Persian,
    Polish,
    PortugueseBrazilian,
    Portuguese,
    Romanian,
    //RomanianEmo,
    Russian,
    SerbianCyrillic,
    SerbianLatin,
    Sinhala,
    Spanish,
    Swedish,
    Tamil,
    Telugu,
    Turkish,
    Ukrainian,
    Vietnamese;

    val code: String
        get() = when (this) {
            System -> "system"
            Afrikaans -> "af"
            Azerbaijani -> "az"
            Arabic -> "ar"
            Bashkir -> "ba"
            Bengali -> "bn"
            Catalan -> "ca"
            ChineseSimplified -> "zh-CN"
            ChineseTraditional -> "zh-TW"
            Danish -> "da"
            Dutch -> "nl"
            English -> "en"
            Esperanto -> "eo"
            Estonian -> "et"
            Filipino -> "fil"
            Finnish -> "fi"
            Galician -> "gl"
            Italian -> "it"
            Indonesian -> "in"
            Irish -> "ga"
            Japanese -> "ja"
            Korean -> "ko"
            Czech -> "cs"
            German -> "de"
            Greek -> "el"
            Hebrew -> "iw" //Hebrew -> "he"
            Hindi -> "hi"
            Hungarian -> "hu"
            Interlingua -> "ia"
            Spanish -> "es"
            French -> "fr"
            Malayalam -> "ml"
            Norwegian -> "no"
            Odia -> "or"
            //Persian -> "fa"
            Polish -> "pl"
            Portuguese -> "pt"
            PortugueseBrazilian -> "pt-BR"
            Romanian -> "ro"
            //RomanianEmo -> "ro-RO"
            Russian -> "ru"
            SerbianCyrillic -> "sr"
            SerbianLatin -> "sr-CS"
            Sinhala -> "si"
            Swedish -> "sv"
            Tamil -> "ta"
            Telugu -> "te"
            Turkish -> "tr"
            Ukrainian -> "uk"
            Vietnamese -> "vi"
        }

    companion object {
        fun languageFromcode(code: String): Languages? = when (code) {
            "system" -> System
            "af" -> Afrikaans
            "ar" -> Arabic
            "ba" -> Bashkir
            "bn" -> Bengali
            "ca" -> Catalan
            "zh-CN" -> ChineseSimplified
            "zh-TW" -> ChineseTraditional
            "da" -> Danish
            "nl" -> Dutch
            "en" -> English
            "eo" -> Esperanto
            "et" -> Estonian
            "fil" -> Filipino
            "fi" -> Finnish
            "gl" -> Galician
            "it" -> Italian
            "in" -> Indonesian
            "ga" -> Irish
            "ja" -> Japanese
            "ko" -> Korean
            "cs" -> Czech
            "de" -> German
            "el" -> Greek
            "iw" -> Hebrew //Hebrew -> "he"
            "hi" -> Hindi
            "hu" -> Hungarian
            "ia" -> Interlingua
            "es" -> Spanish
            "fr" -> French
            "ml" -> Malayalam
            "no" -> Norwegian
            "or" -> Odia
            //"fa" -> Persian
            "pl" -> Polish
            "pt" -> Portuguese
            "pt-BR" -> PortugueseBrazilian
            "ro" -> Romanian
            //"ro-RO" -> RomanianEmo
            "ru" -> Russian
            "sr" -> SerbianCyrillic
            "sr-CS" -> SerbianLatin
            "si" -> Sinhala
            "sv" -> Swedish
            "ta" -> Tamil
            "te" -> Telugu
            "tr" -> Turkish
            "uk" -> Ukrainian
            "vi" -> Vietnamese
            else -> null
        }
    }


}

enum class Countries {
    ZZ,
    AR,
    DZ,
    AU,
    AT,
    AZ,
    BH,
    BD,
    BY,
    BE,
    BO,
    BA,
    BR,
    BG,
    KH,
    CA,
    CL,
    HK,
    CO,
    CR,
    HR,
    CY,
    CZ,
    DK,
    DO,
    EC,
    EG,
    SV,
    EE,
    FI,
    FR,
    GE,
    DE,
    GH,
    GR,
    GT,
    HN,
    HU,
    IS,
    IN,
    ID,
    IQ,
    IE,
    IL,
    IT,
    JM,
    JP,
    JO,
    KZ,
    KE,
    KR,
    KW,
    LA,
    LV,
    LB,
    LY,
    LI,
    LT,
    LU,
    MK,
    MY,
    MT,
    MX,
    ME,
    MA,
    NP,
    NL,
    NZ,
    NI,
    NG,
    NO,
    OM,
    PK,
    PA,
    PG,
    PY,
    PE,
    PH,
    PL,
    PT,
    PR,
    QA,
    RO,
    RU,
    SA,
    SN,
    RS,
    SG,
    SK,
    SI,
    ZA,
    ES,
    LK,
    SE,
    CH,
    TW,
    TZ,
    TH,
    TN,
    TR,
    UG,
    UA,
    AE,
    GB,
    US,
    UY,
    VE,
    VN,
    YE,
    ZW;

    val countryName: String
        get() = when (this) {
            ZZ -> "Global"
            AR -> "Argentina"
            DZ -> "Algeria"
            AU -> "Australia"
            AT -> "Austria"
            AZ -> "Azerbaijan"
            BH -> "Bahrain"
            BD -> "Bangladesh"
            BY -> "Belarus"
            BE -> "Belgium"
            BO -> "Bolivia"
            BA -> "Bosnia and Herzegovina"
            BR -> "Brazil"
            BG -> "Bulgaria"
            KH -> "Cambodia"
            CA -> "Canada"
            CL -> "Chile"
            HK -> "Hong Kong"
            CO -> "Colombia"
            CR -> "Costa Rica"
            HR -> "Croatia"
            CY -> "Cyprus"
            CZ -> "Czech Republic"
            DK -> "Denmark"
            DO -> "Dominican Republic"
            EC -> "Ecuador"
            EG -> "Egypt"
            SV -> "El Salvador"
            EE -> "Es->nia"
            FI -> "Finland"
            FR -> "France"
            GE -> "Georgia"
            DE -> "Germany"
            GH -> "Ghana"
            GR -> "Greece"
            GT -> "Guatemala"
            HN -> "Honduras"
            HU -> "Hungary"
            IS -> "Iceland"
            IN -> "India"
            ID -> "Indonesia"
            IQ -> "Iraq"
            IE -> "Ireland"
            IL -> "Israel"
            IT -> "Italy"
            JM -> "Jamaica"
            JP -> "Japan"
            JO -> "Jordan"
            KZ -> "Kazakhstan"
            KE -> "Kenya"
            KR -> "South Korea"
            KW -> "Kuwait"
            LA -> "Lao"
            LV -> "Latvia"
            LB -> "Lebanon"
            LY -> "Libya"
            LI -> "Liechtenstein"
            LT -> "Lithuania"
            LU -> "Luxembourg"
            MK -> "Macedonia"
            MY -> "Malaysia"
            MT -> "Malta"
            MX -> "Mexico"
            ME -> "Montenegro"
            MA -> "Morocco"
            NP -> "Nepal"
            NL -> "Netherlands"
            NZ -> "New Zealand"
            NI -> "Nicaragua"
            NG -> "Nigeria"
            NO -> "Norway"
            OM -> "Oman"
            PK -> "Pakistan"
            PA -> "Panama"
            PG -> "Papua New Guinea"
            PY -> "Paraguay"
            PE -> "Peru"
            PH -> "Philippines"
            PL -> "Poland"
            PT -> "Portugal"
            PR -> "Puer-> Rico"
            QA -> "Qatar"
            RO -> "Romania"
            RU -> "Russian Federation"
            SA -> "Saudi Arabia"
            SN -> "Senegal"
            RS -> "Serbia"
            SG -> "Singapore"
            SK -> "Slovakia"
            SI -> "Slovenia"
            ZA -> "South Africa"
            ES -> "Spain"
            LK -> "Sri Lanka"
            SE -> "Sweden"
            CH -> "Switzerland"
            TW -> "Taiwan"
            TZ -> "Tanzania"
            TH -> "Thailand"
            TN -> "Tunisia"
            TR -> "Turkey"
            UG -> "Uganda"
            UA -> "Ukraine"
            AE -> "United Arab Emirates"
            GB -> "United Kingdom"
            US -> "United States"
            UY -> "Uruguay"
            VE -> "Venezuela (Bolivarian Republic)"
            VN -> "Vietnam"
            YE -> "Yemen"
            ZW -> "Zimbabwe"
        }
}