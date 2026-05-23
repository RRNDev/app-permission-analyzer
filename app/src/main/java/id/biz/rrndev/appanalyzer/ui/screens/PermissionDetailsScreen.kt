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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.PermissionItem
import id.biz.rrndev.appanalyzer.ui.components.GlassCard
import id.biz.rrndev.appanalyzer.ui.components.WarningPill
import kotlinx.coroutines.delay

@Composable
fun PermissionDetailsScreen(
    permission: PermissionItem,
    modifier: Modifier = Modifier
) {
    val blockCount = 4
    val visible = remember(permission.name) { Array(blockCount) { mutableStateOf(false) } }
    LaunchedEffect(permission.name) {
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StaggerBlock(0) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = permission.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        WarningPill(level = permission.warningLevel)
                    }
                    Text(
                        text = stringResource(permission.category.labelRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = permission.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        StaggerBlock(1) {
            DetailCard(
                title = stringResource(R.string.permission_details_explanation),
                body = permission.explanation
            )
        }
        StaggerBlock(2) {
            DetailCard(
                title = stringResource(R.string.permission_details_privacy_impact),
                body = permission.privacyImpact
            )
        }
        StaggerBlock(3) {
            DetailCard(
                title = stringResource(R.string.permission_details_common_legitimate_use),
                body = permission.legitimateUseCase
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DetailCard(
    title: String,
    body: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
