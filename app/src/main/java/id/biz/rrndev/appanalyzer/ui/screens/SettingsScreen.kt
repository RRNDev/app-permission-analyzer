package id.biz.rrndev.appanalyzer.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.SettingsUiState
import id.biz.rrndev.appanalyzer.ui.components.GlassCard
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onIncludeSystemAppsChange: (Boolean) -> Unit,
    onDeepTrackerScanChange: (Boolean) -> Unit,
    onShowTechnicalNamesChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    showUpdateCta: Boolean,
    onOpenGooglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 4 switches + 1 optional CTA + 1 privacy card = max 6 slots
    val blockCount = 6
    val visible = remember { Array(blockCount) { mutableStateOf(false) } }
    LaunchedEffect(Unit) {
        visible.forEachIndexed { i, state ->
            delay(i * 70L)
            state.value = true
        }
    }

    @Composable
    fun StaggerBlock(index: Int, content: @Composable () -> Unit) {
        val alpha by animateFloatAsState(
            targetValue = if (visible[index].value) 1f else 0f,
            animationSpec = tween(300),
            label = "stagger_alpha_$index"
        )
        val offsetY by animateDpAsState(
            targetValue = if (visible[index].value) 0.dp else 24.dp,
            animationSpec = tween(300),
            label = "stagger_offset_$index"
        )
        Box(
            modifier = Modifier
                .offset(y = offsetY)
                .graphicsLayer { this.alpha = alpha }
        ) {
            content()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StaggerBlock(0) {
            SettingSwitch(
                title = stringResource(R.string.settings_system_apps_title),
                description = stringResource(R.string.settings_system_apps_description),
                checked = state.includeSystemApps,
                onCheckedChange = onIncludeSystemAppsChange
            )
        }
        StaggerBlock(1) {
            SettingSwitch(
                title = stringResource(R.string.settings_deep_tracker_scan_title),
                description = stringResource(R.string.settings_deep_tracker_scan_description),
                checked = state.scanTrackersDeeply,
                onCheckedChange = onDeepTrackerScanChange
            )
        }
        StaggerBlock(2) {
            SettingSwitch(
                title = stringResource(R.string.settings_technical_names_title),
                description = stringResource(R.string.settings_technical_names_description),
                checked = state.showTechnicalPermissionNames,
                onCheckedChange = onShowTechnicalNamesChange
            )
        }
        StaggerBlock(3) {
            SettingSwitch(
                title = stringResource(R.string.settings_warm_contrast_title),
                description = stringResource(R.string.settings_warm_contrast_description),
                checked = state.useDynamicColor,
                onCheckedChange = onDynamicColorChange
            )
        }
        if (showUpdateCta) {
            StaggerBlock(4) {
                LatestFeaturesCard(onOpenGooglePlay = onOpenGooglePlay)
            }
        }
        StaggerBlock(5) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.settings_privacy_by_design_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.settings_privacy_by_design_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun LatestFeaturesCard(
    onOpenGooglePlay: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.latest_features_update_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.latest_features_update_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onOpenGooglePlay) {
                Text(stringResource(R.string.action_open_google_play))
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
