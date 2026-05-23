package id.biz.rrndev.appanalyzer

import android.content.Context
import id.biz.rrndev.appanalyzer.analysis.PermissionCatalog
import id.biz.rrndev.appanalyzer.analysis.PrivacyScoringEngine
import id.biz.rrndev.appanalyzer.model.RiskLevel
import id.biz.rrndev.appanalyzer.model.TrackerInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    private val context: Context
        get() = RuntimeEnvironment.getApplication().applicationContext

    @Test
    fun safePermissionSetKeepsHighScore() {
        val score = PrivacyScoringEngine.score(
            context = context,
            permissions = emptyList(),
            trackers = emptyList(),
            warnings = emptyList(),
            usesAccessibilityService = false
        )

        assertEquals(RiskLevel.Safer, score.riskLevel)
        assertTrue(score.value >= 90)
    }

    @Test
    fun smsContactsAndTrackerReduceScore() {
        val permissions = listOf(
            PermissionCatalog.describe(context, "android.permission.READ_SMS"),
            PermissionCatalog.describe(context, "android.permission.READ_CONTACTS")
        )
        val trackers = listOf(
            TrackerInfo(
                name = "Firebase Analytics",
                packageHints = listOf("com/google/firebase/analytics"),
                privacyImpact = "Used for analytics."
            )
        )

        val score = PrivacyScoringEngine.score(
            context = context,
            permissions = permissions,
            trackers = trackers,
            warnings = emptyList(),
            usesAccessibilityService = false
        )

        assertTrue(score.value < 70)
    }
}
