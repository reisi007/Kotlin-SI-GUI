package at.reisisoft.sigui.android

import android.app.Activity
import android.os.Build
import android.support.annotation.StringRes
import android.util.Log
import android.widget.Toast
import at.reisisoft.sigui.android.License.APACHE2
import at.reisisoft.sigui.android.License.MIT_JSOUP
import at.reisisoft.sigui.commons.downloads.DownloadType
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.util.*

internal fun getDownloadTypes(): Set<DownloadType> =
    Build.SUPPORTED_ABIS.apply { Log.i("HARDWARE INFO", Arrays.toString(this)) }.asSequence().map {
        when {
            it.contains("arm", true) -> DownloadType.ANDROID_LIBREOFFICE_ARM
            it.contains("x86", true) -> DownloadType.ANDROID_LIBREOFFICE_X86
            else -> null
        }
    }.filterNotNull().plus(DownloadType.ANDROID_REMOTE).toSet()


internal fun String.extractFileName() = substring(lastIndexOf('/') + 1, length)

internal val PERMISSION_INSTALL_APKs = 141521

internal fun Activity.deleteCache() = applicationContext.cacheDir.listFiles().forEach { it.deleteRecursively() }

internal fun Activity.showToast(@StringRes resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

internal val LEGAL_HTML by lazy {
    buildString {
        appendHTML(true).html {
            head {
                title("Legal")
            }
            body {
                h3 { text("Contributors") }
                h4 {
                    text("Developers")
                }
                ul {
                    sequenceOf("Florian Reisinger").forEach {
                        li {
                            text(it)
                        }
                    }

                }
                h4 {
                    text("Translators")
                    sequenceOf("English" to sequenceOf("Florian Reisinger")).forEach { (heading, values) ->
                        h5 {
                            text(heading)
                        }
                        ul {
                            values.forEach {
                                li { text(it) }
                            }
                        }
                    }
                }
                h3 {
                    text("Third party licenses")
                }
                ul {
                    sequenceOf(
                        Artifact("Android Dependencies", "https://developer.android.com/") to APACHE2,
                        Artifact("Kotlin", "https://kotlinlang.org/") to APACHE2,
                        Artifact("kotlinx.html", "https://github.com/Kotlin/kotlinx.html") to APACHE2,
                        Artifact("google-gson", "https://github.com/google/gson/") to APACHE2,
                        Artifact("jsoup", "https://jsoup.org/") to MIT_JSOUP
                    ).sortedBy { it.first.htmlName }.forEach { (artifact, license) ->
                        li {
                            span {
                                a(artifact.htmlUrl) { text(artifact.htmlName) }
                                text(" - ")
                                a(license.htmlUrl) { text(license.htmlName) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Artifact(val htmlName: String, val htmlUrl: String)

private enum class License(
    val htmlName: String,
    val htmlUrl: String,
    val isCopyrightNotice: Boolean = false
) {
    APACHE2("Apache 2", "https://www.apache.org/licenses/LICENSE-2.0"),
    MIT_JSOUP("The MIT License", "https://jsoup.org/license"),
    LGPL3("LGPL v3", "https://www.gnu.org/licenses/lgpl-3.0.html")
}