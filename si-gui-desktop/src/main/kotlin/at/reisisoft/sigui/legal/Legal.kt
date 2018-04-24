package at.reisisoft.sigui.legal

import at.reisisoft.sigui.legal.License.*
import at.reisisoft.sigui.ui.ResourceKey
import at.reisisoft.ui.doLocalizedReturn
import at.reisisoft.ui.getReplacedString
import at.reisisoft.ui.getString
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.util.*

internal object Legal {
    internal fun getLicenseHTML(languageSupport: ResourceBundle): String =
        listOf(
            Artifact("Kotlin", "https://kotlinlang.org/") to APACHE2,
            Artifact("kotlinx.html", "https://github.com/Kotlin/kotlinx.html") to APACHE2,
            Artifact("google-gson", "https://github.com/google/gson/") to APACHE2,
            Artifact("jsoup", "https://jsoup.org/") to MIT_JSOUP,
            Artifact("jshortcut", "https://github.com/jimmc/jshortcut") to LGPL3,
            Artifact("controlsfx", "http://fxexperience.com/controlsfx/") to CONTROLSFX
        ).let {
            TreeSet<Pair<Artifact, License>>(kotlin.Comparator { (thizArtifact, _), (otherArtifact, _) ->
                thizArtifact.htmlName.toLowerCase().compareTo(otherArtifact.htmlName.toLowerCase())
            }).apply { addAll(it) }
        }.let { data ->
            //Create HTML
            languageSupport.doLocalizedReturn(ResourceKey.LICENSE_LINK) { licenseAsString ->
                buildString {
                    appendHTML(true).html {
                        head {
                            title(languageSupport.getString(ResourceKey.MENU_LICENSE))
                        }
                        body {
                            h1 {
                                text(languageSupport.getString(ResourceKey.MENU_LICENSE))
                            }
                            text(
                                languageSupport.getReplacedString(
                                    ResourceKey.LICENSE_PRE,
                                    languageSupport.getString(ResourceKey.APPNAME)
                                )
                            )

                            data.forEach { (artifact, license) ->
                                h2 {
                                    a(artifact.htmlUrl, "_blank") {
                                        text(artifact.htmlName)
                                    }
                                }
                                if (license.isCopyrightNotice)
                                    span {
                                        small {
                                            text(license.htmlName)
                                        }
                                        br
                                        br
                                        a(license.htmlUrl, "_blank") {
                                            text(" ($licenseAsString) ")
                                        }
                                    }
                                else
                                    a(license.htmlUrl, "_blank") {
                                        text("(${license.htmlName})")
                                    }
                            }
                        }
                    }

                }
            }
        }
}

private enum class License(
    val htmlName: String,
    val htmlUrl: String,
    val isCopyrightNotice: Boolean = false
) {
    APACHE2("Apache 2", "https://www.apache.org/licenses/LICENSE-2.0"),
    MIT_JSOUP("The MIT License", "https://jsoup.org/license"),
    LGPL3("LGPL v3", "https://www.gnu.org/licenses/lgpl-3.0.html"),
    CONTROLSFX(
        """Copyright (c) 2013, 2014, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    """.trimMargin("*"),
        "https://bitbucket.org/controlsfx/controlsfx/src/420064652b363e39d63dc5e8e2bf3ddf350013c9/license.txt?at=default&fileviewer=file-view-default"
        , true
    )
}

private data class Artifact(val htmlName: String, val htmlUrl: String)