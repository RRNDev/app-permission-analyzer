package id.biz.rrndev.appanalyzer.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import id.biz.rrndev.appanalyzer.model.InstalledAppsUiState
import id.biz.rrndev.appanalyzer.ui.components.GlassCard
import id.biz.rrndev.appanalyzer.ui.components.StatCard
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    installedState: InstalledAppsUiState,
    onOpenApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blockCount = 4
    val visible = remember { Array(blockCount) { mutableStateOf(false) } }
    LaunchedEffect(Unit) {
        visible.forEachIndexed { i, state ->
            delay(i * 80L)
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StaggerBlock(0) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        StaggerBlock(1) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = stringResource(R.string.home_stat_scanned),
                    value = installedState.apps.size.toString(),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    label = stringResource(R.string.home_stat_high_risk),
                    value = installedState.highRiskCount.toString(),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        StaggerBlock(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = stringResource(R.string.home_stat_medium),
                    value = installedState.mediumRiskCount.toString(),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary
                )
                StatCard(
                    label = stringResource(R.string.home_stat_safer),
                    value = installedState.saferCount.toString(),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        StaggerBlock(3) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenApps)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.home_scan_apps),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.home_scan_apps_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(96.dp))
    }
}
