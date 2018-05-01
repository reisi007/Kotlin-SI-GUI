package at.reisisoft.sigui.android

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.google.gson.Gson

private const val key = "settings"

internal data class AndroidSettings(
    val sdremote: List<DisplayAbleDownloadInformation> = listOf(),
    val main: List<DisplayAbleDownloadInformation> = listOf(),
    val curSelection: DisplayAbleDownloadInformation? = null
) {
    fun store(appContext: Context) {
        Log.i("SETTINGS", "Storing settings: " + this)
        appContext.getSharedPreferences(key, MODE_PRIVATE).edit().also { editor ->
            JSON.toJson(this).let {
                editor.putString(key, it)
            }
        }.apply()
    }

    companion object {
        fun load(appContext: Context): AndroidSettings = appContext.getSharedPreferences(key, MODE_PRIVATE).getString(
            key, null
        )?.let {
            Log.i("SETTINGS", "Loading settings")
            JSON.fromJson(it, AndroidSettings::class.java)
        } ?: AndroidSettings()
    }
}

private val JSON by lazy { Gson() }