/*
 * App Permission Analyzer
 *
 * Copyright (C) 2026 RRNDev
 *
 * This file is part of App Permission Analyzer.
 *
 * App Permission Analyzer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * App Permission Analyzer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with App Permission Analyzer. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
 
package id.biz.rrndev.appanalyzer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import id.biz.rrndev.appanalyzer.ui.AppPermissionAnalyzerApp

class MainActivity : ComponentActivity() {
    private val incomingApkUri = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        incomingApkUri.value = intent.extractApkUri()

        setContent {
            AppPermissionAnalyzerApp(
                incomingApkUri = incomingApkUri.value,
                onIncomingApkHandled = { handledUri ->
                    if (incomingApkUri.value == handledUri) {
                        incomingApkUri.value = null
                    }
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingApkUri.value = intent.extractApkUri()
    }
}

private fun Intent.extractApkUri(): Uri? {
    return when (action) {
        Intent.ACTION_VIEW -> data ?: firstClipUri()
        Intent.ACTION_SEND -> streamUri() ?: firstClipUri()
        else -> null
    }
}

private fun Intent.firstClipUri(): Uri? {
    val clip = clipData ?: return null
    return if (clip.itemCount > 0) clip.getItemAt(0).uri else null
}

private fun Intent.streamUri(): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
    }
}
