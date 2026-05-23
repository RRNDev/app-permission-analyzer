package id.biz.rrndev.appanalyzer.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.ui.components.GlassCard
import id.biz.rrndev.appanalyzer.ui.components.PrivacyScoreCard
import kotlinx.coroutines.delay

@Composable
fun ScoreResultScreen(
    analysis: AppAnalysis,
    modifier: Modifier = Modifier
) {
    val blockCount = 4
    val visible = remember(analysis.packageName) { Array(blockCount) { mutableStateOf(false) } }
    LaunchedEffect(analysis.packageName) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScoreSectionIcon(icon = Icons.Outlined.Shield)
                Text(
                    text = stringResource(R.string.privacy_score_result_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        StaggerBlock(1) {
            PrivacyScoreCard(analysis = analysis)
        }

        StaggerBlock(2) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScoreSectionHeader(
                        icon = Icons.Outlined.Info,
                        title = stringResource(R.string.score_how_it_works_title)
                    )
                    Text(
                        text = stringResource(R.string.score_how_it_works_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.score_range_multiline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        StaggerBlock(3) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScoreSectionHeader(
                        icon = Icons.Outlined.AutoGraph,
                        title = stringResource(R.string.score_factors_title)
                    )
                    analysis.score.factors.forEach { factor ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                            Text(
                                text = factor,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ScoreSectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ScoreSectionIcon(icon = icon)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ScoreSectionIcon(
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
