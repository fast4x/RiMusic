package it.fast4x.environment.models

import io.ktor.http.headers
import it.fast4x.environment.Environment
import it.fast4x.environment.utils.EnvironmentPreferences
import it.fast4x.environment.utils.LocalePreferences
import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null,
    private val request: Request = Request(),
    val user: User? = User()
) {

    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val platform: String? = null,
        val hl: String? = "en",
        val gl: String? = "US",
        val visitorData: String? = null,
        val androidSdkVersion: Int? = null,
        val userAgent: String? = null,
        val referer: String? = null,
        val deviceMake: String? = null,
        val deviceModel: String? = null,
        val osName: String? = null,
        val osVersion: String? = null,
        val acceptHeader: String? = null,
        val xClientName: Int? = null,
    )

    @Serializable
    data class ThirdParty(
        val embedUrl: String,
    )

    @Serializable
    data class User(
        val lockedSafetyMode: Boolean = false
    )

    @Serializable
    data class Request(
        val internalExperimentFlags: Array<String> = emptyArray(),
        val useSsl: Boolean = true,
    )

    fun apply() {
        client.userAgent

        headers {
            client.referer?.let { append("Referer", it) }
            append("X-Youtube-Bootstrap-Logged-In", "false")
            append("X-YouTube-Client-Name", client.clientName)
            append("X-YouTube-Client-Version", client.clientVersion)
        }
    }



    companion object {

        val USER_AGENT = EnvironmentPreferences.preference?.p33 ?: ""
        val USER_AGENT1 = EnvironmentPreferences.preference?.p32 ?: ""

        val REFERER1 = EnvironmentPreferences.preference?.p34 ?: ""
        val REFERER2 = EnvironmentPreferences.preference?.p35 ?: ""

        val cname = EnvironmentPreferences.preference?.p18 ?: ""
        val cver = EnvironmentPreferences.preference?.p19 ?: ""
        val cplatform = EnvironmentPreferences.preference?.p20 ?: ""
        val cxname = EnvironmentPreferences.preference?.p21 ?: ""


        val DefaultWeb = Context(
            client = Client(
                clientName = cname,
                clientVersion = cver,
                platform = cplatform,
                userAgent = USER_AGENT,
                referer = REFERER1,
                visitorData = Environment.visitorData,
                xClientName = cxname.toIntOrNull()
            )
        )


        //val hl = if (LocalePreferences.preference?.useLocale == true) LocalePreferences.preference!!.hl else ""
        val hl = LocalePreferences.preference?.hl
        //val gl = LocalePreferences.preference?.gl


        val DefaultWebWithLocale = DefaultWeb.copy(
            client = DefaultWeb.client.copy(hl = hl)
        )

        val cname2 = EnvironmentPreferences.preference?.p22 ?: ""
        val cver2 = EnvironmentPreferences.preference?.p23 ?: ""


        val DefaultWeb2 = Context(
            client = Client(
                clientName = cname2,
                clientVersion = cver2,
                userAgent = USER_AGENT,
            )
        )

        val DefaultWeb2WithLocale = DefaultWeb2.copy(
            client = DefaultWeb2.client.copy(hl = hl)
        )

        val cname3 = EnvironmentPreferences.preference?.p24 ?: ""
        val cver3 = EnvironmentPreferences.preference?.p25 ?: ""
        val dmake = EnvironmentPreferences.preference?.p26 ?: ""
        val dmodel = EnvironmentPreferences.preference?.p27 ?: ""
        val osname = EnvironmentPreferences.preference?.p28 ?: ""
        val osversion = EnvironmentPreferences.preference?.p29 ?: ""
        val accept = EnvironmentPreferences.preference?.p30 ?: ""
        val cxname3 = EnvironmentPreferences.preference?.p31 ?: ""

        val DefaultWeb3 = Context(
            client = Client(
                clientName = cname3,
                clientVersion = cver3,
                deviceMake = dmake,
                deviceModel = dmodel,
                osName = osname,
                osVersion = osversion,
                acceptHeader = accept,
                userAgent = USER_AGENT1,
                xClientName = cxname3.toIntOrNull()
            )
        )

    }
}
