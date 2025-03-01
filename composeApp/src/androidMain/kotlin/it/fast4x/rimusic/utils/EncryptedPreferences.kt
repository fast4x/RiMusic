package it.fast4x.rimusic.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber


//const val pipedUsernameKey = "pipedUsername"
//const val pipedPasswordKey = "pipedPassword"
//const val pipedInstanceNameKey = "pipedInstanceName"
//const val pipedApiBaseUrlKey = "pipedApiBaseUrl"
//const val pipedApiTokenKey = "pipedApiToken"
//const val discordPersonalAccessTokenKey = "DiscordPersonalAccessToken"

inline fun <reified T : Enum<T>> EncryptedSharedPreferences.getEnum(
    key: String,
    defaultValue: T
): T =
    getString(key, null)?.let {
        try {
            enumValueOf<T>(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    } ?: defaultValue

/*
inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T
): SharedPreferences.Editor =
    putString(key, value.name)
 */


val Context.encryptedPreferences: SharedPreferences
    get() = getEncryptedSharedPreferencesResult().onFailure {
        // idea based on https://gist.github.com/rynkowsg/86ebd680a67669dfcece4cc9ec9974df
        run {
            Timber.w("Cannot retrieve preferences encrypted with current master key. Deleting and recreating.")

            /**
             * can only delete preferences this way on high enough API level.
             * the code should behave the same as before for lower api levels
             * (maybe this bug is only present on devices with high API levels anyway).
             */
            if (isAtLeastAndroid7) {
                runCatching {
                    deleteSharedPreferences("secure_preferences")
                }.onFailure {
                    Timber.e(it, "Error while deleting encrypted preferences")
                }
            }
            return getEncryptedSharedPreferencesResult().getOrThrow()
        }
    }.getOrThrow()

fun Context.getEncryptedSharedPreferencesResult(): Result<SharedPreferences> = runCatching {
        EncryptedSharedPreferences.create(
            applicationContext,
            "secure_preferences",
            MasterKey.Builder(applicationContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

@Composable
fun rememberEncryptedPreference(key: String, defaultValue: Boolean): MutableState<Boolean> {
    val context = LocalContext.current
    return remember {
        mutableStateEncryptedPreferenceOf(context.encryptedPreferences.getBoolean(key, defaultValue)) {
            context.encryptedPreferences.edit { putBoolean(key, it) }
        }
    }
}

@Composable
fun rememberEncryptedPreference(key: String, defaultValue: Int): MutableState<Int> {
    val context = LocalContext.current
    return remember {
        mutableStateEncryptedPreferenceOf(context.encryptedPreferences.getInt(key, defaultValue)) {
            context.encryptedPreferences.edit { putInt(key, it) }
        }
    }
}



@Composable
fun rememberEncryptedPreference(key: String, defaultValue: Float): MutableState<Float> {
    val context = LocalContext.current
    return remember {
        mutableStateEncryptedPreferenceOf(context.encryptedPreferences.getFloat(key, defaultValue)) {
            context.encryptedPreferences.edit { putFloat(key, it) }
        }
    }
}

@Composable
fun rememberEncryptedPreference(key: String, defaultValue: String): MutableState<String> {
    val context = LocalContext.current
    return remember {
        mutableStateEncryptedPreferenceOf(context.encryptedPreferences.getString(key, null) ?: defaultValue) {
            context.encryptedPreferences.edit { putString(key, it) }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> rememberEncryptedPreference(key: String, defaultValue: T): MutableState<T> {
    val context = LocalContext.current
    return remember {
        mutableStateEncryptedPreferenceOf(context.encryptedPreferences.getEnum(key, defaultValue)) {
            context.encryptedPreferences.edit { putEnum(key, it) }
        }
    }
}

inline fun <T> mutableStateEncryptedPreferenceOf(
    value: T,
    crossinline onStructuralInequality: (newValue: T) -> Unit
) =
    mutableStateOf(
        value = value,
        policy = object : SnapshotMutationPolicy<T> {
            override fun equivalent(a: T, b: T): Boolean {
                val areEquals = a == b
                if (!areEquals) onStructuralInequality(b)
                return areEquals
            }
        })