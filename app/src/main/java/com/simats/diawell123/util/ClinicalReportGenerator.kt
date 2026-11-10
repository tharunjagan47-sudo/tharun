package com.simats.diawell123.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import androidx.core.content.FileProvider
import com.simats.diawell123.data.model.ExportRecordsData
import com.simats.diawell123.data.model.ExportReportOptions
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GeneratedReportResult(
    val file: File,
    val fileName: String,
    val mimeType: String,
    val previewBitmaps: List<Bitmap>,
    val csvPreviewText: String? = null
)

/**
 * Hospital-grade Clinical Medical Report Generator (multi-page A4 PDF & RFC-4180 CSV),
 * supporting PdfRenderer page previews, FileProvider sharing/viewing, MediaStore Downloads export,
 * and native Android PrintManager integration.
 */
object ClinicalReportGenerator {

    private const val TAG = "ClinicalReportGen"
    private const val PAGE_WIDTH = 595   // A4 width in PostScript points (72 dpi)
    private const val PAGE_HEIGHT = 842  // A4 height in PostScript points (72 dpi)
    private const val MARGIN_X = 34f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2f)
    private const val FOOTER_Y = PAGE_HEIGHT - 28f
    private const val BOTTOM_LIMIT = PAGE_HEIGHT - 52f

    // Clinical Palette
    private val COLOR_NAVY = Color.parseColor("#0F172A")
    private val COLOR_PRIMARY = Color.parseColor("#6B156E")
    private val COLOR_ACCENT = Color.parseColor("#B81D85")
    private val COLOR_TEAL = Color.parseColor("#0D9488")
    private val COLOR_BG_SUBTLE = Color.parseColor("#F8FAFC")
    private val COLOR_BG_TINT = Color.parseColor("#FDF2F8")
    private val COLOR_BORDER = Color.parseColor("#CBD5E1")
    private val COLOR_TEXT_DARK = Color.parseColor("#1E293B")
    private val COLOR_TEXT_MUTED = Color.parseColor("#64748B")
    private val COLOR_GREEN = Color.parseColor("#15803D")
    private val COLOR_GREEN_BG = Color.parseColor("#DCFCE7")
    private val COLOR_RED = Color.parseColor("#B91C1C")
    private val COLOR_RED_BG = Color.parseColor("#FEE2E2")
    private val COLOR_AMBER = Color.parseColor("#B45309")
    private val COLOR_AMBER_BG = Color.parseColor("#FEF3C7")
    private val COLOR_BLUE = Color.parseColor("#1D4ED8")
    private val COLOR_BLUE_BG = Color.parseColor("#DBEAFE")

    fun generateReport(
        context: Context,
        data: ExportRecordsData,
        options: ExportReportOptions
    ): GeneratedReportResult {
        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        // Clean older cached exports to prevent unbounded cache bloat
        reportsDir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(5)?.forEach {
            try { it.delete() } catch (_: Exception) {}
        }

        val safePatient = (data.patient.name.ifBlank { "DiaWell_Patient" })
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "Patient" }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val isCsv = options.format.equals("CSV", ignoreCase = true)
        val ext = if (isCsv) "csv" else "pdf"
        val fileName = "${safePatient}_Clinical_Report_${stamp}.$ext"
        val targetFile = File(reportsDir, fileName)

        return if (isCsv) {
            val csvText = buildCsvString(data, options)
            FileOutputStream(targetFile).use { fos ->
                fos.write(csvText.toByteArray(Charsets.UTF_8))
            }
            GeneratedReportResult(
                file = targetFile,
                fileName = fileName,
                mimeType = "text/csv",
                previewBitmaps = emptyList(),
                csvPreviewText = csvText
            )
        } else {
            generatePdfFile(targetFile, data, options)
            val bitmaps = renderPdfPreviewPages(targetFile, maxPages = 6)
            GeneratedReportResult(
                file = targetFile,
                fileName = fileName,
                mimeType = "application/pdf",
                previewBitmaps = bitmaps,
                csvPreviewText = null
            )
        }
    }

    private fun generatePdfFile(
        outputFile: File,
        data: ExportRecordsData,
        options: ExportReportOptions
    ) {
        val pdfDocument = PdfDocument()
        val patient = data.patient
        val summary = data.summary

        var pageNumber = 1
        var currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = currentPage.canvas
        var y = 0f

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = COLOR_BORDER
        }
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        fun drawFooter(targetCanvas: Canvas, pageNum: Int) {
            strokePaint.color = COLOR_BORDER
            targetCanvas.drawLine(MARGIN_X, FOOTER_Y - 12f, PAGE_WIDTH - MARGIN_X, FOOTER_Y - 12f, strokePaint)

            textPaint.color = COLOR_TEXT_MUTED
            textPaint.textSize = 8f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.textAlign = Paint.Align.LEFT
            targetCanvas.drawText(
                "CONFIDENTIAL CLINICAL SUMMARY • DiaWell Diabetes Care System • Printed: ${
                    SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date())
                }",
                MARGIN_X,
                FOOTER_Y,
                textPaint
            )
            textPaint.textAlign = Paint.Align.RIGHT
            targetCanvas.drawText("Page $pageNum", PAGE_WIDTH - MARGIN_X, FOOTER_Y, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }

        fun ensureSpace(neededHeight: Float, continuationTitle: String? = null) {
            if (y + neededHeight > BOTTOM_LIMIT) {
                drawFooter(canvas, pageNumber)
                pdfDocument.finishPage(currentPage)
                pageNumber++
                currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = currentPage.canvas

                // Continuation slim header bar
                fillPaint.color = COLOR_NAVY
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 36f, fillPaint)
                textPaint.color = Color.WHITE
                textPaint.textSize = 10f
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                canvas.drawText(
                    "DIAWELL CLINICAL REPORT — ${patient.name.ifBlank { "Patient" }} (${options.timeRangeLabel})",
                    MARGIN_X,
                    22f,
                    textPaint
                )
                y = 54f
                if (!continuationTitle.isNullOrBlank()) {
                    textPaint.color = COLOR_PRIMARY
                    textPaint.textSize = 10.5f
                    textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    canvas.drawText("$continuationTitle (continued)", MARGIN_X, y, textPaint)
                    y += 14f
                }
            }
        }

        fun drawSectionHeader(title: String, badgeText: String = "") {
            ensureSpace(46f)
            fillPaint.color = COLOR_PRIMARY
            canvas.drawRoundRect(RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 22f), 4f, 4f, fillPaint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(title.uppercase(Locale.getDefault()), MARGIN_X + 10f, y + 14.5f, textPaint)

            if (badgeText.isNotBlank()) {
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.textSize = 8.5f
                canvas.drawText(badgeText, PAGE_WIDTH - MARGIN_X - 10f, y + 14.5f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT
            }
            y += 28f
        }

        // =====================================================================
        // 1. HOSPITAL-GRADE TOP HEADER BANNER
        // =====================================================================
        fillPaint.color = COLOR_NAVY
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 84f, fillPaint)

        // Accent top stripe
        fillPaint.color = COLOR_ACCENT
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 5f, fillPaint)

        textPaint.color = Color.parseColor("#F9A8D4")
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("DIAWELL ENDOCRINOLOGY & DIABETES WELLNESS RECORD", MARGIN_X, 26f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 18f
        canvas.drawText("PATIENT CLINICAL HEALTH REPORT", MARGIN_X, 49f, textPaint)

        val dateRangeText = if (summary.startDate.isNotBlank() && summary.endDate.isNotBlank()) {
            "${options.timeRangeLabel} (${summary.startDate} to ${summary.endDate})"
        } else {
            options.timeRangeLabel
        }
        textPaint.color = Color.parseColor("#CBD5E1")
        textPaint.textSize = 9.5f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Reporting Period: $dateRangeText", MARGIN_X, 68f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("PID-${data.userId.coerceAtLeast(1).toString().padStart(4, '0')}", PAGE_WIDTH - MARGIN_X, 46f, textPaint)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.color = Color.parseColor("#94A3B8")
        textPaint.textSize = 8.5f
        canvas.drawText(
            "Generated: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}",
            PAGE_WIDTH - MARGIN_X,
            66f,
            textPaint
        )
        textPaint.textAlign = Paint.Align.LEFT

        y = 100f

        // =====================================================================
        // 2. PATIENT DEMOGRAPHICS CARD
        // =====================================================================
        val demoHeight = 76f
        fillPaint.color = COLOR_BG_SUBTLE
        canvas.drawRoundRect(RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + demoHeight), 6f, 6f, fillPaint)
        strokePaint.color = COLOR_BORDER
        canvas.drawRoundRect(RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + demoHeight), 6f, 6f, strokePaint)

        textPaint.color = COLOR_ACCENT
        textPaint.textSize = 8.5f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("PATIENT IDENTIFICATION & BASELINE VITALS", MARGIN_X + 12f, y + 15f, textPaint)

        val ageStr = patient.age?.takeIf { it > 0 }?.let { "$it yrs" } ?: "N/A"
        val genderStr = patient.gender.ifBlank { "Not specified" }
        val wtStr = patient.weightKg?.takeIf { it > 0f }?.let { "${formatNum(it)} kg" } ?: "N/A"
        val htStr = patient.heightCm?.takeIf { it > 0f }?.let { "${formatNum(it)} cm" } ?: "N/A"
        val bmiStr = if (patient.weightKg != null && patient.weightKg > 0f && patient.heightCm != null && patient.heightCm > 0f) {
            val hm = patient.heightCm / 100f
            String.format(Locale.US, "%.1f kg/m²", patient.weightKg / (hm * hm))
        } else "N/A"
        val targetStr = patient.targetGlucose?.takeIf { it > 0f }?.let { "${it.toInt()} mg/dL" } ?: "120 mg/dL"

        val col1X = MARGIN_X + 12f
        val col2X = MARGIN_X + CONTENT_WIDTH * 0.36f
        val col3X = MARGIN_X + CONTENT_WIDTH * 0.69f

        drawLabelValue(canvas, textPaint, "Patient Name:", patient.name.ifBlank { "Patient" }, col1X, y + 32f)
        drawLabelValue(canvas, textPaint, "Diagnosis:", patient.diabetesType.ifBlank { "Type 2 Diabetes" }, col2X, y + 32f)
        drawLabelValue(canvas, textPaint, "Age / Gender:", "$ageStr / $genderStr", col3X, y + 32f)

        drawLabelValue(canvas, textPaint, "Height / Weight:", "$htStr / $wtStr", col1X, y + 49f)
        drawLabelValue(canvas, textPaint, "Body Mass Index:", bmiStr, col2X, y + 49f)
        drawLabelValue(canvas, textPaint, "Glycemic Target:", "< $targetStr", col3X, y + 49f)

        val contactInfo = listOfNotNull(
            patient.email.takeIf { it.isNotBlank() },
            patient.phone.takeIf { it.isNotBlank() },
            patient.bloodGroup.takeIf { it.isNotBlank() }?.let { "Blood Group: $it" }
        ).joinToString("  •  ").ifBlank { "DiaWell Mobile Account" }
        drawLabelValue(canvas, textPaint, "Contact / Group:", contactInfo, col1X, y + 66f)

        y += demoHeight + 14f

        // =====================================================================
        // 3. CLINICAL EXECUTIVE KPI STRIP (4 CARDS)
        // =====================================================================
        val cardGap = 8f
        val cardW = (CONTENT_WIDTH - cardGap * 3f) / 4f
        val cardH = 58f

        val avgG = if (summary.averageGlucose > 0f) "${summary.averageGlucose.toInt()} mg/dL" else "--"
        val gSub = if (summary.totalGlucoseReadings > 0) {
            "${summary.inTargetPercent}% in target (${summary.totalGlucoseReadings} logs)"
        } else "No logs in period"

        val hba1cVal = if (summary.estimatedHba1c > 0f) "${summary.estimatedHba1c}%" else "--"
        val hba1cSub = if (summary.minGlucose > 0f && summary.maxGlucose > 0f) {
            "Range ${summary.minGlucose.toInt()}-${summary.maxGlucose.toInt()}"
        } else "ADAG Estimate"

        val adhVal = if (summary.totalMedications > 0) "${summary.adherencePercent}%" else "--"
        val adhSub = "${summary.takenMedications}/${summary.totalMedications} Doses Taken"

        val lifeVal = "${summary.totalMealsLogged} Meals"
        val lifeSub = "${summary.totalWorkouts} Workouts • ${summary.totalActivityMins}m"

        drawKpiCard(canvas, fillPaint, strokePaint, textPaint, MARGIN_X, y, cardW, cardH, "AVG BLOOD GLUCOSE", avgG, gSub, COLOR_ACCENT)
        drawKpiCard(canvas, fillPaint, strokePaint, textPaint, MARGIN_X + (cardW + cardGap), y, cardW, cardH, "EST. eHbA1c", hba1cVal, hba1cSub, COLOR_TEAL)
        drawKpiCard(canvas, fillPaint, strokePaint, textPaint, MARGIN_X + (cardW + cardGap) * 2f, y, cardW, cardH, "MED ADHERENCE", adhVal, adhSub, COLOR_BLUE)
        drawKpiCard(canvas, fillPaint, strokePaint, textPaint, MARGIN_X + (cardW + cardGap) * 3f, y, cardW, cardH, "DIET & ACTIVITY", lifeVal, lifeSub, COLOR_GREEN)

        y += cardH + 14f

        // =====================================================================
        // 4. SECTION 1: BLOOD GLUCOSE READINGS
        // =====================================================================
        if (options.includeGlucose) {
            drawSectionHeader(
                title = "1. Blood Glucose Monitoring Log",
                badgeText = "${data.glucoseReadings.size} Records"
            )
            if (data.glucoseReadings.isEmpty()) {
                drawEmptyNotice(canvas, fillPaint, strokePaint, textPaint, "No blood glucose readings recorded during ${options.timeRangeLabel}.", y)
                y += 30f
            } else {
                val cols = floatArrayOf(130f, 80f, 82f, 92f, CONTENT_WIDTH - 384f)
                val headers = listOf("Date & Time", "Glucose", "Test Context", "Status", "Notes")
                drawTableHeader(canvas, fillPaint, textPaint, cols, headers, y)
                y += 20f

                data.glucoseReadings.forEachIndexed { idx, row ->
                    if (y + 22f > BOTTOM_LIMIT) {
                        ensureSpace(45f, "1. Blood Glucose Monitoring Log")
                        drawTableHeader(canvas, fillPaint, textPaint, cols, headers, y)
                        y += 20f
                    }
                    val valNum = row.numFloat("glucose_value", "reading", "glucose_level")
                    val gStatus = row.str("status").ifBlank { classifyGlucose(valNum) }
                    val vals = listOf(
                        row.str("logged_at").ifBlank { "${row.str("reading_date")} ${row.str("reading_time")}".trim() },
                        "${valNum.toInt()} mg/dL",
                        row.str("test_type", "reading_type").ifBlank { "Random" },
                        gStatus,
                        row.str("notes").ifBlank { "—" }
                    )
                    val statusTint = when {
                        gStatus.contains("Target", true) -> COLOR_GREEN to COLOR_GREEN_BG
                        gStatus.contains("Elevated", true) -> COLOR_AMBER to COLOR_AMBER_BG
                        else -> COLOR_RED to COLOR_RED_BG
                    }
                    drawTableRow(canvas, fillPaint, strokePaint, textPaint, cols, vals, y, idx % 2 == 1, statusColIndex = 3, statusPair = statusTint)
                    y += 19f
                }
                y += 12f
            }
        }

        // =====================================================================
        // 5. SECTION 2: MEDICATION SCHEDULE & ADHERENCE
        // =====================================================================
        if (options.includeMedications) {
            drawSectionHeader(
                title = "2. Prescribed Medication Regimen",
                badgeText = "${data.medications.size} Prescriptions"
            )
            if (data.medications.isEmpty()) {
                drawEmptyNotice(canvas, fillPaint, strokePaint, textPaint, "No active medications recorded during ${options.timeRangeLabel}.", y)
                y += 30f
            } else {
                val cols = floatArrayOf(155f, 115f, 95f, 85f, CONTENT_WIDTH - 450f)
                val headers = listOf("Medication Name", "Dosage", "Schedule Time", "Meal Timing", "Status")
                drawTableHeader(canvas, fillPaint, textPaint, cols, headers, y)
                y += 20f

                data.medications.forEachIndexed { idx, row ->
                    if (y + 22f > BOTTOM_LIMIT) {
                        ensureSpace(45f, "2. Prescribed Medication Regimen")
                        drawTableHeader(canvas, fillPaint, textPaint, cols, headers, y)
                        y += 20f
                    }
                    val mStatus = row.str("status").ifBlank { "Upcoming" }
                    val timeFreq = "${row.str("reminder_time")} (${row.str("frequency").ifBlank { "Daily" }})"
                    val vals = listOf(
                        row.str("medicine_name", "name"),
                        row.str("dosage", "dose"),
                        timeFreq,
                        row.str("meal_timing", "timing").ifBlank { "After Meal" },
                        mStatus
                    )
                    val statusTint = when (mStatus.lowercase(Locale.US)) {
                        "taken" -> COLOR_GREEN to COLOR_GREEN_BG
                        "snoozed" -> COLOR_BLUE to COLOR_BLUE_BG
                        "skipped", "missed" -> COLOR_RED to COLOR_RED_BG
                        else -> COLOR_ACCENT to COLOR_BG_TINT
                    }
                    drawTableRow(canvas, fillPaint, strokePaint, textPaint, cols, vals, y, idx % 2 == 1, statusColIndex = 4, statusPair = statusTint)
                    y += 19f
                }
                y += 12f
            }
        }

        // =====================================================================
        // 6. SECTION 3: DIET & NUTRITION LOGS
        // =====================================================================
        if (options.includeMeals) {
            drawSectionHeader(
                title = "3. Dietary & Carbohydrate Intake Log",
                badgeText = "${data.foodLogs.size} Meals"
            )
            if (data.foodLogs.isEmpty()) {
                drawEmptyNotice(canvas, fillPaint, strokePaint, textPaint, "No dietary or meal entries logged during ${options.timeRangeLabel}.", y)
                y += 30f
            } else {
                val cols = floatArrayOf(115f, 75f, 170f, 75f, CONTENT_WIDTH - 435f)
                val headers = listOf("Logged At", "Meal Type", "Food Items", "Calories", "Carbs (g)")
                drawTableHeader(canvas, fillPaint, textPaint, cols, headers, y)
                y += 20f

                data.foodLogs.forEachIndexed { idx, row ->
                    if (y + 22f > BOTTOM_LIMIT) {
                        ensureSpace(45f, "3. Dietary & Carbohydrate Intake Log")
                        drawTableHeader(canvas, fillPaint, textPaint, cols, headers, y)
                        y += 20f
                    }
                    val calVal = row.numFloat("calories").toInt()
                    val carbsVal = row.numFloat("carbs", "carbs_g")
                    val vals = listOf(
                        row.str("logged_at").ifBlank { "${row.str("log_date")} ${row.str("log_time")}".trim() },
                        row.str("meal_type", "meal_category"),
                        row.str("food_items", "food_name"),
                        "$calVal kcal",
                        "${formatNum(carbsVal)} g"
                    )
                    drawTableRow(canvas, fillPaint, strokePaint, textPaint, cols, vals, y, idx % 2 == 1)
                    y += 19f
                }
                y += 12f
            }
        }

        // =====================================================================
        // 7. SECTION 4: PHYSICAL ACTIVITY LOGS
        // =====================================================================
        if (options.includeActivity) {
            drawSectionHeader(
                title = "4. Physical Activity & Exercise Log",
                badgeText = "${data.activityLogs.size} Sessions"
            )
            if (data.activityLogs.isEmpty()) {
                drawEmptyNotice(canvas, fillPaint, strokePaint, textPaint, "No exercise sessions recorded during ${options.timeRangeLabel}.", y)
                y += 30f
            } else {
                val cols = floatArrayOf(125f, 130f, 80f, 80f, CONTENT_WIDTH - 415f)
                val headers = listOf("Date & Time", "Activity", "Duration", "Intensity", "Calories Burned")
                drawTableHeader(canvas, fillPaint, textPaint, cols, headers, y)
                y += 20f

                data.activityLogs.forEachIndexed { idx, row ->
                    if (y + 22f > BOTTOM_LIMIT) {
                        ensureSpace(45f, "4. Physical Activity & Exercise Log")
                        drawTableHeader(canvas, fillPaint, textPaint, cols, headers, y)
                        y += 20f
                    }
                    val durVal = row.numFloat("duration_mins").toInt()
                    val metVal = row.numFloat("met_value").takeIf { it > 0f } ?: 3.5f
                    val burnVal = row.numFloat("calories_burned", "calories_kcal").toInt()
                    val vals = listOf(
                        row.str("logged_at").ifBlank { "${row.str("activity_date")} ${row.str("activity_time")}".trim() },
                        row.str("activity_name", "activity_type"),
                        "$durVal mins",
                        "${formatNum(metVal)} MET",
                        "$burnVal kcal"
                    )
                    drawTableRow(canvas, fillPaint, strokePaint, textPaint, cols, vals, y, idx % 2 == 1)
                    y += 19f
                }
                y += 12f
            }
        }

        // =====================================================================
        // 8. CLINICAL ASSESSMENT & PHYSICIAN SIGN-OFF BLOCK
        // =====================================================================
        ensureSpace(92f)
        fillPaint.color = COLOR_BG_TINT
        canvas.drawRoundRect(RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 76f), 6f, 6f, fillPaint)
        strokePaint.color = COLOR_ACCENT
        canvas.drawRoundRect(RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 76f), 6f, 6f, strokePaint)

        textPaint.color = COLOR_PRIMARY
        textPaint.textSize = 9.5f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("CLINICAL ASSESSMENT & CARE NOTES", MARGIN_X + 12f, y + 16f, textPaint)

        textPaint.color = COLOR_TEXT_DARK
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val statusMsg = summary.careStatus.ifBlank { "Patient self-monitoring records compiled for clinical consultation." }
        canvas.drawText("Overall Status: $statusMsg", MARGIN_X + 12f, y + 32f, textPaint)

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 8.5f
        canvas.drawText(
            "Physician Review Notes: ________________________________________________________",
            MARGIN_X + 12f,
            y + 50f,
            textPaint
        )
        canvas.drawText(
            "Medical Disclaimer: Generated from patient-entered DiaWell telemetry. Verify with laboratory HbA1c prior to regimen adjustments.",
            MARGIN_X + 12f,
            y + 66f,
            textPaint
        )

        drawFooter(canvas, pageNumber)
        pdfDocument.finishPage(currentPage)

        FileOutputStream(outputFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()
        Log.d(TAG, "Generated clinical PDF report at ${outputFile.absolutePath} ($pageNumber pages)")
    }

    private fun buildCsvString(data: ExportRecordsData, options: ExportReportOptions): String {
        val sb = StringBuilder()
        val patient = data.patient
        val summary = data.summary

        fun row(vararg cols: Any?) {
            sb.append(
                cols.joinToString(",") { cell ->
                    val str = cell?.toString() ?: ""
                    val escaped = str.replace("\"", "\"\"")
                    "\"$escaped\""
                }
            ).append("\n")
        }

        row("DIAWELL CLINICAL PATIENT HEALTH REPORT")
        row("Patient Name", patient.name.ifBlank { "Patient" }, "Diabetes Type", patient.diabetesType, "Period", options.timeRangeLabel)
        row("Age", patient.age ?: "", "Weight (kg)", patient.weightKg ?: "", "Height (cm)", patient.heightCm ?: "", "Target Glucose", patient.targetGlucose ?: 120f)
        row("Average Glucose (mg/dL)", summary.averageGlucose, "Est. HbA1c (%)", summary.estimatedHba1c, "Med Adherence (%)", summary.adherencePercent, "Care Status", summary.careStatus)
        sb.append("\n")

        if (options.includeGlucose) {
            row("=== BLOOD GLUCOSE READINGS ===")
            row("Date & Time", "Glucose (mg/dL)", "Test Type", "Clinical Status", "Notes")
            if (data.glucoseReadings.isEmpty()) {
                row("No glucose readings recorded in selected period")
            } else {
                data.glucoseReadings.forEach { r ->
                    row(
                        r.str("logged_at").ifBlank { "${r.str("reading_date")} ${r.str("reading_time")}".trim() },
                        r.numFloat("glucose_value", "reading", "glucose_level").toInt(),
                        r.str("test_type", "reading_type"),
                        r.str("status"),
                        r.str("notes")
                    )
                }
            }
            sb.append("\n")
        }

        if (options.includeMedications) {
            row("=== ACTIVE MEDICATIONS ===")
            row("Medicine Name", "Dosage", "Frequency", "Reminder Time", "Meal Timing", "Status", "Notes")
            if (data.medications.isEmpty()) {
                row("No medication records in selected period")
            } else {
                data.medications.forEach { m ->
                    row(
                        m.str("medicine_name", "name"),
                        m.str("dosage", "dose"),
                        m.str("frequency"),
                        m.str("reminder_time"),
                        m.str("meal_timing", "timing"),
                        m.str("status"),
                        m.str("notes")
                    )
                }
            }
            sb.append("\n")
        }

        if (options.includeMeals) {
            row("=== DIET & NUTRITION LOGS ===")
            row("Logged At", "Meal Type", "Food Items", "Portion", "Calories (kcal)", "Carbs (g)", "Protein (g)", "Fat (g)")
            if (data.foodLogs.isEmpty()) {
                row("No diet logs recorded in selected period")
            } else {
                data.foodLogs.forEach { f ->
                    row(
                        f.str("logged_at").ifBlank { "${f.str("log_date")} ${f.str("log_time")}".trim() },
                        f.str("meal_type", "meal_category"),
                        f.str("food_items", "food_name"),
                        f.str("portion"),
                        f.numFloat("calories").toInt(),
                        f.numFloat("carbs", "carbs_g"),
                        f.numFloat("protein_g"),
                        f.numFloat("fat_g")
                    )
                }
            }
            sb.append("\n")
        }

        if (options.includeActivity) {
            row("=== PHYSICAL ACTIVITY LOGS ===")
            row("Logged At", "Activity", "Duration (mins)", "Distance (km)", "Calories Burned (kcal)", "MET", "Notes")
            if (data.activityLogs.isEmpty()) {
                row("No activity sessions recorded in selected period")
            } else {
                data.activityLogs.forEach { a ->
                    row(
                        a.str("logged_at").ifBlank { "${a.str("activity_date")} ${a.str("activity_time")}".trim() },
                        a.str("activity_name", "activity_type"),
                        a.numFloat("duration_mins").toInt(),
                        a.numFloat("distance_km"),
                        a.numFloat("calories_burned", "calories_kcal").toInt(),
                        a.numFloat("met_value"),
                        a.str("notes")
                    )
                }
            }
        }
        return sb.toString()
    }

    /**
     * Renders actual PDF pages into Bitmaps using PdfRenderer for live in-app preview.
     */
    fun renderPdfPreviewPages(pdfFile: File, maxPages: Int = 5): List<Bitmap> {
        if (!pdfFile.exists() || pdfFile.length() == 0L) return emptyList()
        val bitmaps = mutableListOf<Bitmap>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val count = minOf(renderer.pageCount, maxPages)
            for (i in 0 until count) {
                renderer.openPage(i).use { page ->
                    val renderW = 960
                    val renderH = ((renderW.toFloat() / page.width.coerceAtLeast(1)) * page.height).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888)
                    val c = Canvas(bitmap)
                    c.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "PdfRenderer preview failed: ${e.message}", e)
        } finally {
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }
        return bitmaps
    }

    /**
     * Saves the generated report file into the device's Downloads folder reliably across Android 7–15.
     */
    fun saveReportToDownloads(context: Context, sourceFile: File, mimeType: String): Result<String> {
        if (!sourceFile.exists()) {
            return Result.failure(Exception("Report file not found. Please generate it again."))
        }
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, sourceFile.name)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/DiaWell")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return Result.failure(Exception("Could not create Downloads entry."))

                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(sourceFile).use { input ->
                        input.copyTo(out)
                    }
                } ?: return Result.failure(Exception("Could not open Downloads output stream."))

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                Result.success("Downloads/DiaWell/${sourceFile.name}")
            } else {
                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "DiaWell"
                )
                val destDir = if ((publicDir.exists() || publicDir.mkdirs()) && publicDir.canWrite()) {
                    publicDir
                } else {
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                }
                if (!destDir.exists()) destDir.mkdirs()
                val destFile = File(destDir, sourceFile.name)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { out ->
                        input.copyTo(out)
                    }
                }
                Result.success(destFile.absolutePath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveReportToDownloads failed: ${e.message}", e)
            Result.failure(Exception("Unable to save report: ${e.message}"))
        }
    }

    fun getFileProviderUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Opens the generated report using an installed PDF/spreadsheet viewer app via FileProvider.
     */
    fun openReportFile(context: Context, file: File, mimeType: String): Result<Unit> {
        if (!file.exists()) {
            return Result.failure(Exception("Report file is missing. Generate again."))
        }
        return try {
            val uri = getFileProviderUri(context, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Health Report"))
            Result.success(Unit)
        } catch (_: ActivityNotFoundException) {
            Result.failure(Exception("No viewer app installed for $mimeType. Use Share or Download instead."))
        } catch (e: Exception) {
            Log.e(TAG, "openReportFile error: ${e.message}", e)
            Result.failure(Exception("Could not open report: ${e.message}"))
        }
    }

    /**
     * Shares the actual PDF/CSV file via FileProvider URI.
     */
    fun shareReportFile(context: Context, file: File, mimeType: String, patientName: String): Result<Unit> {
        if (!file.exists()) {
            return Result.failure(Exception("Report file is missing. Generate again."))
        }
        return try {
            val uri = getFileProviderUri(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_SUBJECT, "DiaWell Clinical Health Report - ${patientName.ifBlank { "Patient" }}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the DiaWell Clinical Health Report for ${patientName.ifBlank { "Patient" }}."
                )
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newUri(context.contentResolver, file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Clinical Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "shareReportFile error: ${e.message}", e)
            Result.failure(Exception("Could not share report: ${e.message}"))
        }
    }

    /**
     * Sends the generated PDF directly to Android's native PrintManager.
     */
    fun printPdfReport(context: Context, pdfFile: File, jobName: String): Result<Unit> {
        if (!pdfFile.exists()) {
            return Result.failure(Exception("PDF file not found."))
        }
        return try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                ?: return Result.failure(Exception("Print service unavailable on this device."))

            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder(pdfFile.name)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()
                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    try {
                        FileInputStream(pdfFile).use { input ->
                            FileOutputStream(destination.fileDescriptor).use { output ->
                                val buf = ByteArray(8192)
                                var len: Int
                                while (input.read(buf).also { len = it } > 0) {
                                    if (cancellationSignal?.isCanceled == true) {
                                        callback.onWriteCancelled()
                                        return
                                    }
                                    output.write(buf, 0, len)
                                }
                            }
                        }
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.message)
                    }
                }
            }
            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "printPdfReport error: ${e.message}", e)
            Result.failure(Exception("Unable to start print job: ${e.message}"))
        }
    }

    // =========================================================================
    // DRAWING HELPERS
    // =========================================================================

    private fun drawLabelValue(
        canvas: Canvas,
        paint: TextPaint,
        label: String,
        value: String,
        x: Float,
        y: Float
    ) {
        paint.color = COLOR_TEXT_MUTED
        paint.textSize = 8.2f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText(label, x, y, paint)
        val labelWidth = paint.measureText("$label ")

        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val clippedVal = TextUtils.ellipsize(value, paint, 155f, TextUtils.TruncateAt.END).toString()
        canvas.drawText(clippedVal, x + labelWidth, y, paint)
    }

    private fun drawKpiCard(
        canvas: Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: TextPaint,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        subtitle: String,
        accentColor: Int
    ) {
        fillPaint.color = Color.WHITE
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 6f, 6f, fillPaint)
        strokePaint.color = COLOR_BORDER
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 6f, 6f, strokePaint)

        // Left vertical accent bar
        fillPaint.color = accentColor
        canvas.drawRoundRect(RectF(x, y, x + 3.5f, y + h), 2f, 2f, fillPaint)

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 7.2f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(title, x + 9f, y + 14f, textPaint)

        textPaint.color = accentColor
        textPaint.textSize = 13.5f
        val clippedValue = TextUtils.ellipsize(value, textPaint, w - 14f, TextUtils.TruncateAt.END).toString()
        canvas.drawText(clippedValue, x + 9f, y + 34f, textPaint)

        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 7.2f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val clippedSub = TextUtils.ellipsize(subtitle, textPaint, w - 14f, TextUtils.TruncateAt.END).toString()
        canvas.drawText(clippedSub, x + 9f, y + 48f, textPaint)
    }

    private fun drawEmptyNotice(
        canvas: Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: TextPaint,
        message: String,
        y: Float
    ) {
        fillPaint.color = COLOR_BG_SUBTLE
        canvas.drawRoundRect(RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 22f), 4f, 4f, fillPaint)
        strokePaint.color = COLOR_BORDER
        canvas.drawRoundRect(RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 22f), 4f, 4f, strokePaint)
        textPaint.color = COLOR_TEXT_MUTED
        textPaint.textSize = 8.5f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
        canvas.drawText(message, MARGIN_X + 10f, y + 14.5f, textPaint)
    }

    private fun drawTableHeader(
        canvas: Canvas,
        fillPaint: Paint,
        textPaint: TextPaint,
        colWidths: FloatArray,
        headers: List<String>,
        y: Float
    ) {
        fillPaint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 20f, fillPaint)

        textPaint.color = COLOR_NAVY
        textPaint.textSize = 8f
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        var curX = MARGIN_X + 6f
        headers.forEachIndexed { i, title ->
            val clipped = TextUtils.ellipsize(title, textPaint, colWidths[i] - 8f, TextUtils.TruncateAt.END).toString()
            canvas.drawText(clipped, curX, y + 13.5f, textPaint)
            curX += colWidths[i]
        }
    }

    private fun drawTableRow(
        canvas: Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: TextPaint,
        colWidths: FloatArray,
        values: List<String>,
        y: Float,
        shaded: Boolean,
        statusColIndex: Int = -1,
        statusPair: Pair<Int, Int>? = null
    ) {
        fillPaint.color = if (shaded) COLOR_BG_SUBTLE else Color.WHITE
        canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 19f, fillPaint)
        strokePaint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(MARGIN_X, y + 19f, PAGE_WIDTH - MARGIN_X, y + 19f, strokePaint)

        var curX = MARGIN_X + 6f
        values.forEachIndexed { i, cellVal ->
            if (i == statusColIndex && statusPair != null) {
                val (fg, bg) = statusPair
                textPaint.textSize = 7.5f
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                val pillW = minOf(colWidths[i] - 8f, textPaint.measureText(cellVal) + 10f)
                fillPaint.color = bg
                canvas.drawRoundRect(RectF(curX, y + 3.5f, curX + pillW, y + 15.5f), 4f, 4f, fillPaint)
                textPaint.color = fg
                val clipped = TextUtils.ellipsize(cellVal, textPaint, pillW - 6f, TextUtils.TruncateAt.END).toString()
                canvas.drawText(clipped, curX + 4f, y + 12.2f, textPaint)
            } else {
                textPaint.color = COLOR_TEXT_DARK
                textPaint.textSize = 8f
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                val clipped = TextUtils.ellipsize(cellVal, textPaint, colWidths[i] - 8f, TextUtils.TruncateAt.END).toString()
                canvas.drawText(clipped, curX, y + 13f, textPaint)
            }
            curX += colWidths[i]
        }
    }

    private fun classifyGlucose(valMgDl: Float): String {
        return when {
            valMgDl < 70f -> "Low (Hypo)"
            valMgDl <= 140f -> "In Target"
            valMgDl <= 180f -> "Elevated"
            else -> "High"
        }
    }

    private fun formatNum(value: Float): String {
        return if (value % 1f == 0f) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
    }

    private fun Map<String, Any>.str(vararg keys: String): String {
        for (k in keys) {
            val v = this[k] ?: continue
            val s = v.toString().trim()
            if (s.isNotEmpty() && !s.equals("null", ignoreCase = true)) return s
        }
        return ""
    }

    private fun Map<String, Any>.numFloat(vararg keys: String): Float {
        for (k in keys) {
            val v = this[k] ?: continue
            when (v) {
                is Number -> return v.toFloat()
                is String -> v.toFloatOrNull()?.let { return it }
            }
        }
        return 0f
    }
}
