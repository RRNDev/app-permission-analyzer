package id.biz.rrndev.appanalyzer.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.model.RiskLevel
import id.biz.rrndev.appanalyzer.model.WarningLevel
import id.biz.rrndev.appanalyzer.util.DrawableTools.toAppIconBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFFF5F0EC),
                        Color(0xFFEEE8E2)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color(0x00FFFFFF)
                        )
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
fun AppIconBitmap(
    bitmap: Bitmap?,
    fallbackText: String,
    modifier: Modifier = Modifier
) {
    val fallbackLetter = stringResource(R.string.app_icon_fallback_letter)
    val contentDescription = stringResource(R.string.app_icon_content_description, fallbackText)
    val label = fallbackText.trim().take(1).uppercase().ifBlank {
        fallbackLetter
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp)
                )
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun PackageAppIcon(
    packageName: String,
    bitmap: Bitmap?,
    fallbackText: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loadedBitmap = produceState<Bitmap?>(initialValue = bitmap, packageName, bitmap) {
        value = bitmap ?: withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager
                    .getApplicationIcon(packageName)
                    .toAppIconBitmap(context.resources.displayMetrics.density)
            }.getOrNull()
        }
    }
    AppIconBitmap(
        bitmap = loadedBitmap.value,
        fallbackText = fallbackText,
        modifier = modifier
    )
}

@Composable
fun ScoreBadge(
    score: Int,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    Surface(
        color = riskContainerColor(riskLevel),
        contentColor = riskContentColor(riskLevel),
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(riskLevel.labelRes),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PrivacyScoreCard(
    analysis: AppAnalysis,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = analysis.score.summary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.privacy_score_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ScoreBadge(score = analysis.score.value, riskLevel = analysis.score.riskLevel)
            }
            LinearProgressIndicator(
                progress = { analysis.score.value / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = riskProgressColor(analysis.score.riskLevel),
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
            Text(
                text = stringResource(R.string.privacy_score_range_legend),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    GlassCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
                    .align(Alignment.TopEnd)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WarningPill(level: WarningLevel) {
    Surface(
        color = when (level) {
            WarningLevel.Critical -> MaterialTheme.colorScheme.error
            WarningLevel.High -> MaterialTheme.colorScheme.errorContainer
            WarningLevel.Medium -> MaterialTheme.colorScheme.secondary
            WarningLevel.Low -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when (level) {
            WarningLevel.Critical -> MaterialTheme.colorScheme.onError
            WarningLevel.High -> MaterialTheme.colorScheme.onErrorContainer
            WarningLevel.Medium -> MaterialTheme.colorScheme.onSecondary
            WarningLevel.Low -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = stringResource(level.labelRes),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun VerticalSpacer() {
    Spacer(Modifier.height(12.dp))
}

@Composable
fun riskContainerColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.High -> MaterialTheme.colorScheme.error
        RiskLevel.Medium -> MaterialTheme.colorScheme.secondary
        RiskLevel.Safer -> MaterialTheme.colorScheme.primary
    }
}

@Composable
fun riskContentColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.High -> Color.White
        RiskLevel.Medium -> MaterialTheme.colorScheme.onSecondary
        RiskLevel.Safer -> MaterialTheme.colorScheme.onPrimary
    }
}

@Composable
fun riskProgressColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.High -> MaterialTheme.colorScheme.error
        RiskLevel.Medium -> MaterialTheme.colorScheme.secondary
        RiskLevel.Safer -> MaterialTheme.colorScheme.primary
    }
}
