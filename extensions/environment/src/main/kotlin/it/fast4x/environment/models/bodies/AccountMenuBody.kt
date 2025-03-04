package it.fast4x.environment.models.bodies

import it.fast4x.environment.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class AccountMenuBody(
    val context: Context = Context.DefaultWeb,
    val deviceTheme: String = "DEVICE_THEME_SELECTED",
    val userInterfaceTheme: String = "USER_INTERFACE_THEME_DARK",
)
