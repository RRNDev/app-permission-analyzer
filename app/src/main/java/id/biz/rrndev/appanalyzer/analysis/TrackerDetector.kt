package id.biz.rrndev.appanalyzer.analysis

import android.content.Context
import androidx.annotation.StringRes
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.TrackerInfo
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipFile

object TrackerDetector {
    private data class TrackerSignature(
        @param:StringRes val nameRes: Int,
        val hints: List<String>,
        @param:StringRes val impactRes: Int
    )

    private val signatures = listOf(
        TrackerSignature(
            nameRes = R.string.tracker_name_firebase_analytics,
            hints = listOf(
                "com/google/firebase/analytics",
                "com.google.firebase.analytics",
                "firebase-analytics",
                "com/google/android/gms/measurement"
            ),
            impactRes = R.string.tracker_impact_firebase_analytics
        ),
        TrackerSignature(
            nameRes = R.string.tracker_name_admob,
            hints = listOf(
                "com/google/android/gms/ads",
                "com.google.android.gms.ads",
                "play-services-ads",
                "admob"
            ),
            impactRes = R.string.tracker_impact_admob
        ),
        TrackerSignature(
            nameRes = R.string.tracker_name_facebook_sdk,
            hints = listOf(
                "com/facebook",
                "com.facebook",
                "facebook.appevents",
                "FacebookSdk"
            ),
            impactRes = R.string.tracker_impact_facebook_sdk
        ),
        TrackerSignature(
            nameRes = R.string.tracker_name_appsflyer,
            hints = listOf(
                "com/appsflyer",
                "com.appsflyer",
                "appsflyer"
            ),
            impactRes = R.string.tracker_impact_appsflyer
        ),
        TrackerSignature(
            nameRes = R.string.tracker_name_adjust,
            hints = listOf(
                "com/adjust/sdk",
                "com.adjust.sdk",
                "adjust_config"
            ),
            impactRes = R.string.tracker_impact_adjust
        ),
        TrackerSignature(
            nameRes = R.string.tracker_name_onesignal,
            hints = listOf(
                "com/onesignal",
                "com.onesignal",
                "onesignal"
            ),
            impactRes = R.string.tracker_impact_onesignal
        )
    )

    fun detectFromApk(apkFile: File, deepScan: Boolean, context: Context): List<TrackerInfo> {
        if (!apkFile.exists() || apkFile.length() == 0L) return emptyList()
        val found = linkedMapOf<String, TrackerInfo>()

        runCatching {
            ZipFile(apkFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements() && found.size < signatures.size) {
                    val entry = entries.nextElement()
                    val entryName = entry.name.lowercase(Locale.US)
                    signatures.forEach { signature ->
                        val trackerName = signature.name(context)
                        if (trackerName !in found && signature.matches(entryName)) {
                            found[trackerName] = signature.toTracker(context)
                        }
                    }

                    if (!deepScan || entry.isDirectory || !entry.shouldScanContent()) continue

                    // DEX strings often contain SDK package names. Stream scanning avoids loading large APK files into memory.
                    zip.getInputStream(entry).use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var tail = ""
                        var scannedBytes = 0L
                        while (found.size < signatures.size) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            scannedBytes += read
                            if (scannedBytes > MAX_BYTES_PER_ENTRY) break

                            val chunk = tail + String(buffer, 0, read, StandardCharsets.ISO_8859_1)
                                .lowercase(Locale.US)
                            signatures.forEach { signature ->
                                val trackerName = signature.name(context)
                                if (trackerName !in found && signature.matches(chunk)) {
                                    found[trackerName] = signature.toTracker(context)
                                }
                            }
                            tail = chunk.takeLast(MAX_SIGNATURE_LENGTH)
                        }
                    }
                }
            }
        }

        return found.values.toList()
    }

    private fun TrackerSignature.matches(value: String): Boolean {
        return hints.any { hint -> value.contains(hint.lowercase(Locale.US)) }
    }

    private fun TrackerSignature.name(context: Context): String = context.getString(nameRes)

    private fun TrackerSignature.toTracker(context: Context): TrackerInfo {
        return TrackerInfo(
            name = context.getString(nameRes),
            packageHints = hints,
            privacyImpact = context.getString(impactRes)
        )
    }

    private fun java.util.zip.ZipEntry.shouldScanContent(): Boolean {
        val normalized = name.lowercase(Locale.US)
        return normalized.endsWith(".dex") ||
            normalized.endsWith(".xml") ||
            normalized.endsWith(".json") ||
            normalized.endsWith(".properties") ||
            normalized.startsWith("assets/") ||
            normalized.startsWith("res/xml/") ||
            normalized == "androidmanifest.xml"
    }

    private const val MAX_BYTES_PER_ENTRY = 24L * 1024L * 1024L
    private const val MAX_SIGNATURE_LENGTH = 96
}
