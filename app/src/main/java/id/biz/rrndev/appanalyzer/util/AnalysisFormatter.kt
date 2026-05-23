package id.biz.rrndev.appanalyzer.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import id.biz.rrndev.appanalyzer.R
import id.biz.rrndev.appanalyzer.model.AppAnalysis
import id.biz.rrndev.appanalyzer.model.RiskLevel
import id.biz.rrndev.appanalyzer.model.TrackerScanStatus
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AnalysisFormatter {

    // ── Plain text (existing) ──────────────────────────────────────────────

    fun toShareText(context: Context, analysis: AppAnalysis): String {
        val permissions = analysis.permissions
            .groupBy { context.getString(it.category.labelRes) }
            .entries
            .joinToString(separator = "\n") { (category, items) ->
                context.getString(
                    R.string.share_group_line_format,
                    category,
                    items.joinToString { it.displayName }
                )
            }
            .ifBlank { context.getString(R.string.share_permissions_none_requested) }

        val trackers = if (analysis.trackerScanStatus == TrackerScanStatus.Scanned) {
            analysis.trackers
                .joinToString { it.name }
                .ifBlank { context.getString(R.string.share_none_detected) }
        } else {
            context.getString(R.string.share_trackers_not_scanned)
        }

        val warnings = analysis.warnings
            .joinToString(separator = "\n") {
                context.getString(R.string.share_warning_line_format, it.title, it.description)
            }
            .ifBlank { context.getString(R.string.share_no_risky_combinations) }

        return buildString {
            appendLine(context.getString(R.string.app_name))
            appendLine()
            appendLine(context.getShareLabelLine(R.string.share_label_app, analysis.appName))
            appendLine(context.getShareLabelLine(R.string.share_label_package, analysis.packageName))
            appendLine(context.getShareLabelLine(R.string.share_label_version, analysis.versionName))
            appendLine(
                context.getShareLabelLine(
                    R.string.share_label_privacy_score,
                    context.getString(
                        R.string.share_privacy_score_value,
                        analysis.score.value,
                        context.getString(analysis.score.riskLevel.labelRes)
                    )
                )
            )
            appendLine(context.getShareLabelLine(R.string.share_label_summary, analysis.score.summary))
            appendLine()
            appendLine(context.getShareLabelLine(R.string.share_label_trackers, trackers))
            appendLine()
            appendLine(context.getShareSectionHeader(R.string.share_label_permissions))
            appendLine(permissions)
            appendLine()
            appendLine(context.getShareSectionHeader(R.string.share_label_warnings))
            appendLine(warnings)
            appendLine()
            append(context.getString(R.string.share_report_generated_locally))
        }.trimEnd()
    }

    // ── Markdown ───────────────────────────────────────────────────────────

    fun toMarkdownText(context: Context, analysis: AppAnalysis): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val riskLabel = context.getString(analysis.score.riskLevel.labelRes)

        return buildString {
            // Header
            appendLine("# App Analyzer Report: ${analysis.appName}")
            appendLine()
            appendLine("---")
            appendLine()

            // App Information table
            appendLine("## App Information")
            appendLine()
            appendLine("| Field | Value |")
            appendLine("|-------|-------|")
            appendLine("| **App Name** | ${analysis.appName} |")
            appendLine("| **Package** | `${analysis.packageName}` |")
            appendLine("| **Version** | ${analysis.versionName} (${analysis.versionCode}) |")
            appendLine("| **Privacy Score** | **${analysis.score.value}/100** — $riskLabel |")
            appendLine("| **Source** | ${analysis.source.name} |")
            appendLine("| **Generated** | $dateStr |")
            appendLine()

            // Summary
            appendLine("## Summary")
            appendLine()
            appendLine(analysis.score.summary)
            appendLine()
            if (analysis.score.factors.isNotEmpty()) {
                appendLine("**Score Factors:**")
                appendLine()
                analysis.score.factors.forEach { factor ->
                    appendLine("- $factor")
                }
                appendLine()
            }

            // Trackers
            val trackerCount = if (analysis.trackerScanStatus == TrackerScanStatus.Scanned)
                analysis.trackers.size else null
            val trackerHeader = if (trackerCount != null)
                "## Trackers Detected ($trackerCount)"
            else
                "## Trackers"
            appendLine(trackerHeader)
            appendLine()
            when {
                analysis.trackerScanStatus != TrackerScanStatus.Scanned -> {
                    appendLine("> Tracker scan was not performed for this analysis.")
                }
                analysis.trackers.isEmpty() -> {
                    appendLine("> No common tracker SDKs detected.")
                }
                else -> {
                    analysis.trackers.forEach { tracker ->
                        appendLine("### ${tracker.name}")
                        appendLine()
                        appendLine(tracker.privacyImpact)
                        appendLine()
                    }
                }
            }

            // Risk Warnings
            appendLine("## Risk Warnings (${analysis.warnings.size})")
            appendLine()
            if (analysis.warnings.isEmpty()) {
                appendLine("> No risky permission combinations detected.")
            } else {
                analysis.warnings.forEach { warning ->
                    val levelLabel = context.getString(warning.level.labelRes).uppercase()
                    appendLine("### ${warning.title}")
                    appendLine()
                    appendLine("**Risk Level:** `$levelLabel`")
                    appendLine()
                    appendLine("> ${warning.description}")
                    appendLine()
                }
            }

            // Permissions
            appendLine("## Permissions (${analysis.permissionCount} total)")
            appendLine()
            if (analysis.permissions.isEmpty()) {
                appendLine("> No permissions requested.")
            } else {
                analysis.permissions
                    .groupBy { it.category }
                    .forEach { (category, items) ->
                        val categoryLabel = context.getString(category.labelRes)
                        appendLine("### $categoryLabel (${items.size})")
                        appendLine()
                        items.forEach { perm ->
                            appendLine("- **${perm.displayName}** — ${perm.explanation}")
                        }
                        appendLine()
                    }
            }

            // Footer
            appendLine("---")
            appendLine()
            append("*Report generated locally by App Analyzer on $dateStr. No data was sent to external servers.*")
        }
    }

    // ── PDF ────────────────────────────────────────────────────────────────

    fun toPdfBytes(context: Context, analysis: AppAnalysis): ByteArray {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val riskLabel = context.getString(analysis.score.riskLevel.labelRes)

        // Page dimensions (A4 at 72dpi: 595 x 842)
        val pageWidth = 595
        val pageHeight = 842
        val marginH = 40f
        val contentWidth = pageWidth - marginH * 2
        val bottomMargin = 40f
        val footerY = pageHeight - bottomMargin

        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = 0f

        // ── Color palette ──
        val colorPrimary = Color.parseColor("#FF7418")
        val colorAccent = Color.parseColor("#C97B2F")
        val colorText = Color.parseColor("#1C1008")
        val colorSubtext = Color.parseColor("#7A6355")
        val colorDivider = Color.parseColor("#D4C5BB")
        val colorRiskHigh = Color.parseColor("#D32F2F")
        val colorRiskMedium = Color.parseColor("#F57C00")
        val colorRiskSafer = Color.parseColor("#388E3C")
        val colorBgSection = Color.parseColor("#FFF5EE")
        val colorWhite = Color.WHITE

        val scoreColor = when (analysis.score.riskLevel) {
            RiskLevel.High -> colorRiskHigh
            RiskLevel.Medium -> colorRiskMedium
            RiskLevel.Safer -> colorRiskSafer
        }

        // ── Paint helpers ──
        fun textPaint(size: Float, color: Int = colorText, bold: Boolean = false, italic: Boolean = false): Paint {
            return Paint().apply {
                this.color = color
                textSize = size
                typeface = when {
                    bold && italic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                    bold -> Typeface.DEFAULT_BOLD
                    italic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    else -> Typeface.DEFAULT
                }
                isAntiAlias = true
            }
        }

        fun fillPaint(color: Int): Paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Measure text width
        fun Paint.measureStr(text: String): Float = measureText(text)

        // Word-wrap text into lines that fit contentWidth
        fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
            val words = text.split(" ")
            val lines = mutableListOf<String>()
            var current = StringBuilder()
            for (word in words) {
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (paint.measureStr(candidate) <= maxWidth) {
                    current = StringBuilder(candidate)
                } else {
                    if (current.isNotEmpty()) lines.add(current.toString())
                    current = StringBuilder(word)
                }
            }
            if (current.isNotEmpty()) lines.add(current.toString())
            return lines
        }

        // Draw wrapped text block, return new y
        fun Canvas.drawWrappedText(
            text: String,
            x: Float,
            startY: Float,
            paint: Paint,
            maxWidth: Float,
            lineSpacing: Float = 4f
        ): Float {
            var cy = startY
            val lines = wrapText(text, paint, maxWidth)
            val lineH = paint.textSize + lineSpacing
            for (line in lines) {
                drawText(line, x, cy, paint)
                cy += lineH
            }
            return cy
        }

        // Page break helper — returns true if new page was created
        fun checkPageBreak(neededHeight: Float): Boolean {
            if (y + neededHeight > footerY - 20f) {
                // Draw footer on current page
                val footerPaint = textPaint(8f, colorSubtext, italic = true)
                canvas.drawText(
                    "App Analyzer  •  Generated locally  •  $dateStr  •  Page $pageNumber",
                    marginH,
                    footerY,
                    footerPaint
                )
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = marginH
                return true
            }
            return false
        }

        // ── HEADER ──────────────────────────────────────────────────────────
        // Background banner
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, fillPaint(colorPrimary))

        // App name
        val appNamePaint = textPaint(20f, colorWhite, bold = true)
        canvas.drawText(analysis.appName, marginH, 38f, appNamePaint)

        // Package + version
        val metaPaint = textPaint(9f, Color.parseColor("#FFE0C8"))
        canvas.drawText("${analysis.packageName}  •  v${analysis.versionName} (${analysis.versionCode})", marginH, 54f, metaPaint)

        // Score badge
        val scoreBgPaint = fillPaint(colorWhite)
        canvas.drawRoundRect(
            pageWidth - marginH - 90f, 22f,
            pageWidth - marginH, 98f,
            8f, 8f, scoreBgPaint
        )
        val scorePaint = textPaint(26f, scoreColor, bold = true)
        val scoreText = "${analysis.score.value}"
        val scoreTextW = scorePaint.measureStr(scoreText)
        canvas.drawText(scoreText, pageWidth - marginH - 45f - scoreTextW / 2, 62f, scorePaint)
        val scoreLabelPaint = textPaint(8f, scoreColor, bold = true)
        val riskLabelW = scoreLabelPaint.measureStr(riskLabel)
        canvas.drawText(riskLabel, pageWidth - marginH - 45f - riskLabelW / 2, 76f, scoreLabelPaint)
        val outOf100Paint = textPaint(7f, colorSubtext)
        val outOf100 = "/100"
        canvas.drawText(outOf100, pageWidth - marginH - 45f - outOf100Paint.measureStr(outOf100) / 2, 88f, outOf100Paint)

        // Source chip
        val sourceLabel = "  ${analysis.source.name}  "
        val sourceLabelPaint = textPaint(8f, colorWhite)
        val sourceLabelW = sourceLabelPaint.measureStr(sourceLabel)
        canvas.drawRoundRect(marginH, 60f, marginH + sourceLabelW, 74f, 4f, 4f, fillPaint(Color.parseColor("#80FFFFFF")))
        canvas.drawText(sourceLabel, marginH, 71f, sourceLabelPaint)

        y = 120f

        // ── SUMMARY ─────────────────────────────────────────────────────────
        checkPageBreak(60f)

        val sectionTitlePaint = textPaint(12f, colorPrimary, bold = true)
        val bodyPaint = textPaint(9f, colorText)
        val bodyGrayPaint = textPaint(9f, colorSubtext)
        val dividerPaint = fillPaint(colorDivider)

        canvas.drawText("Summary", marginH, y, sectionTitlePaint)
        y += 4f
        canvas.drawRect(marginH, y, marginH + contentWidth, y + 1f, dividerPaint)
        y += 10f

        y = canvas.drawWrappedText(analysis.score.summary, marginH, y, bodyPaint, contentWidth)
        y += 6f

        if (analysis.score.factors.isNotEmpty()) {
            analysis.score.factors.forEach { factor ->
                checkPageBreak(14f)
                val bulletPaint = textPaint(9f, colorAccent, bold = true)
                canvas.drawText("•", marginH, y, bulletPaint)
                y = canvas.drawWrappedText(factor, marginH + 12f, y, bodyGrayPaint, contentWidth - 12f)
            }
            y += 8f
        }

        // ── INFO TABLE ──────────────────────────────────────────────────────
        checkPageBreak(80f)
        canvas.drawText("App Information", marginH, y, sectionTitlePaint)
        y += 4f
        canvas.drawRect(marginH, y, marginH + contentWidth, y + 1f, dividerPaint)
        y += 8f

        val labelPaint = textPaint(8f, colorSubtext)
        val valuePaint = textPaint(9f, colorText, bold = true)
        val col1 = marginH
        val col2 = marginH + 100f
        val rowH = 16f

        fun tableRow(label: String, value: String) {
            checkPageBreak(rowH)
            canvas.drawText(label, col1, y, labelPaint)
            canvas.drawText(value, col2, y, valuePaint)
            y += rowH
        }

        tableRow("Generated", dateStr)
        tableRow("Package", analysis.packageName)
        tableRow("Version", "${analysis.versionName} (${analysis.versionCode})")
        tableRow("Privacy Score", "${analysis.score.value}/100 — $riskLabel")
        tableRow("Permissions", "${analysis.permissionCount} requested")
        tableRow("Trackers", if (analysis.trackerScanStatus == TrackerScanStatus.Scanned) "${analysis.trackerCount} detected" else "Not scanned")
        tableRow("Risk Warnings", "${analysis.warnings.size} found")
        y += 10f

        // ── RISK WARNINGS ──────────────────────────────────────────────────
        checkPageBreak(30f)
        canvas.drawText("Risk Warnings (${analysis.warnings.size})", marginH, y, sectionTitlePaint)
        y += 4f
        canvas.drawRect(marginH, y, marginH + contentWidth, y + 1f, dividerPaint)
        y += 10f

        if (analysis.warnings.isEmpty()) {
            canvas.drawText("No risky permission combinations detected.", marginH, y, bodyGrayPaint)
            y += 16f
        } else {
            analysis.warnings.forEach { warning ->
                checkPageBreak(50f)

                // Warning card background
                val cardBottom = y + 10f + warning.description.length / 60 * 12f + 36f
                canvas.drawRoundRect(marginH, y - 12f, marginH + contentWidth, cardBottom, 6f, 6f, fillPaint(colorBgSection))

                val warningTitlePaint = textPaint(10f, colorText, bold = true)
                canvas.drawText(warning.title, marginH + 8f, y, warningTitlePaint)

                // Level badge
                val levelColor = when (warning.level.weight) {
                    in 0..3 -> colorRiskSafer
                    in 4..7 -> colorRiskMedium
                    else -> colorRiskHigh
                }
                val levelLabel = context.getString(warning.level.labelRes).uppercase()
                val levelPaint = textPaint(7f, colorWhite, bold = true)
                val levelW = levelPaint.measureStr("  $levelLabel  ")
                canvas.drawRoundRect(
                    marginH + contentWidth - levelW - 4f, y - 10f,
                    marginH + contentWidth - 4f, y + 2f,
                    3f, 3f, fillPaint(levelColor)
                )
                canvas.drawText("  $levelLabel  ", marginH + contentWidth - levelW - 4f, y, levelPaint)

                y += 6f
                y = canvas.drawWrappedText(warning.description, marginH + 8f, y, bodyGrayPaint, contentWidth - 16f)
                y += 12f
            }
        }
        y += 6f

        // ── TRACKERS ────────────────────────────────────────────────────────
        checkPageBreak(30f)
        val trackerTitle = if (analysis.trackerScanStatus == TrackerScanStatus.Scanned)
            "Trackers Detected (${analysis.trackerCount})" else "Trackers"
        canvas.drawText(trackerTitle, marginH, y, sectionTitlePaint)
        y += 4f
        canvas.drawRect(marginH, y, marginH + contentWidth, y + 1f, dividerPaint)
        y += 10f

        when {
            analysis.trackerScanStatus != TrackerScanStatus.Scanned -> {
                canvas.drawText("Tracker scan was not performed for this analysis.", marginH, y, bodyGrayPaint)
                y += 16f
            }
            analysis.trackers.isEmpty() -> {
                canvas.drawText("No common tracker SDKs detected.", marginH, y, bodyGrayPaint)
                y += 16f
            }
            else -> {
                analysis.trackers.forEach { tracker ->
                    checkPageBreak(40f)
                    val trackerNamePaint = textPaint(10f, colorAccent, bold = true)
                    canvas.drawText("• ${tracker.name}", marginH, y, trackerNamePaint)
                    y += 6f
                    y = canvas.drawWrappedText(tracker.privacyImpact, marginH + 10f, y, bodyGrayPaint, contentWidth - 10f)
                    y += 8f
                }
            }
        }
        y += 6f

        // ── PERMISSIONS ─────────────────────────────────────────────────────
        checkPageBreak(30f)
        canvas.drawText("Permissions (${analysis.permissionCount} total)", marginH, y, sectionTitlePaint)
        y += 4f
        canvas.drawRect(marginH, y, marginH + contentWidth, y + 1f, dividerPaint)
        y += 10f

        if (analysis.permissions.isEmpty()) {
            canvas.drawText("No permissions requested.", marginH, y, bodyGrayPaint)
            y += 16f
        } else {
            analysis.permissions.groupBy { it.category }.forEach { (category, items) ->
                checkPageBreak(30f)
                val catLabel = context.getString(category.labelRes)
                val catPaint = textPaint(10f, colorAccent, bold = true)
                canvas.drawText("$catLabel  (${items.size})", marginH, y, catPaint)
                y += 6f

                items.forEach { perm ->
                    checkPageBreak(14f)
                    val permNamePaint = textPaint(9f, colorText, bold = true)
                    canvas.drawText("  ${perm.displayName}", marginH, y, permNamePaint)
                    y += 3f
                    y = canvas.drawWrappedText("  ${perm.explanation}", marginH + 6f, y, bodyGrayPaint, contentWidth - 6f)
                    y += 4f
                }
                y += 6f
            }
        }

        // ── FOOTER on last page ─────────────────────────────────────────────
        val footerPaint = textPaint(8f, colorSubtext, italic = true)
        canvas.drawText(
            "App Analyzer  •  Generated locally  •  $dateStr  •  Page $pageNumber",
            marginH,
            footerY,
            footerPaint
        )

        document.finishPage(page)

        val out = ByteArrayOutputStream()
        document.writeTo(out)
        document.close()
        return out.toByteArray()
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun Context.getShareLabelLine(labelRes: Int, value: String): String {
        return getString(R.string.share_label_line_format, getString(labelRes), value)
    }

    private fun Context.getShareSectionHeader(labelRes: Int): String {
        return getString(R.string.share_section_header_format, getString(labelRes))
    }
}
