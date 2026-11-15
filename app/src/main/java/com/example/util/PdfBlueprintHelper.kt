package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.SiteWalkDefectEntity
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "PdfBlueprintHelper"
private const val PLANS_DIR = "site_photos"

data class PdfPageRenderResult(
    val bitmap: Bitmap,
    val pageIndex: Int,
    val pageCount: Int
)

object PdfBlueprintHelper {

    private fun getPlansDir(context: Context): File {
        val dir = File(context.filesDir, PLANS_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Copies a user-picked PDF from SAF (OpenDocument) into internal FileProvider storage
     * so the inspector's active architectural plan survives restarts without needing storage permissions.
     */
    suspend fun persistPickedPdf(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val destFile = File(getPlansDir(context), "PLAN_${timeStamp}_${System.nanoTime()}.pdf")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destFile
            ).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist picked PDF blueprint", e)
            null
        }
    }

    /**
     * Renders a single page of a PDF blueprint onto a crisp white-backed Bitmap via Android PdfRenderer.
     */
    suspend fun renderPdfPage(
        context: Context,
        pdfUriString: String,
        pageIndex: Int = 0,
        maxWidth: Int = 1440
    ): PdfPageRenderResult? = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(pdfUriString)
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext null
            pfd.use { fd ->
                PdfRenderer(fd).use { renderer ->
                    val count = renderer.pageCount
                    if (count <= 0) return@withContext null
                    val safeIndex = pageIndex.coerceIn(0, count - 1)
                    renderer.openPage(safeIndex).use { page ->
                        val ratio = if (page.width > 0) {
                            (maxWidth.toFloat() / page.width.toFloat()).coerceIn(1f, 3f)
                        } else {
                            2f
                        }
                        val bmpWidth = (page.width * ratio).toInt().coerceIn(600, 2200)
                        val bmpHeight = (page.height * ratio).toInt().coerceIn(400, 2800)
                        val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        PdfPageRenderResult(
                            bitmap = bitmap,
                            pageIndex = safeIndex,
                            pageCount = count
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to render PDF page $pageIndex from $pdfUriString", e)
            null
        }
    }

    /**
     * Generates a realistic 2-page Sample Architectural Floor Plan & Door/Window Schedule PDF
     * locally using Android PdfDocument so inspectors or judges can immediately test PDF pinning
     * and Cloud Multimodal Photo-vs-PDF comparison in 1 tap.
     */
    suspend fun createSampleArchitecturalPdf(
        context: Context,
        language: AppLanguage = AppLanguage.TH
    ): String? = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument()
            val pageW = 1190
            val pageH = 842

            // --- Page 1: Level 2 Architectural Floor Plan ---
            val page1Info = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
            val page1 = pdfDoc.startPage(page1Info)
            drawSampleFloorPlanPage(page1.canvas, pageW.toFloat(), pageH.toFloat(), language)
            pdfDoc.finishPage(page1)

            // --- Page 2: Door & Window Opening Schedule ---
            val page2Info = PdfDocument.PageInfo.Builder(pageW, pageH, 2).create()
            val page2 = pdfDoc.startPage(page2Info)
            drawSampleSchedulePage(page2.canvas, pageW.toFloat(), pageH.toFloat(), language)
            pdfDoc.finishPage(page2)

            val file = File(getPlansDir(context), "SAMPLE_ARCH_PLAN_${language.code.uppercase()}.pdf")
            FileOutputStream(file).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            ).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate sample architectural PDF", e)
            null
        }
    }

    private fun drawSampleFloorPlanPage(canvas: Canvas, w: Float, h: Float, language: AppLanguage) {
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 16f
        }
        val openingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0284C7")
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        val badgeFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0F2FE")
            style = Paint.Style.FILL
        }
        val badgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0284C7")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0369A1")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Outer sheet border & grid
        canvas.drawRect(36f, 36f, w - 36f, h - 36f, borderPaint)
        for (x in 140..1080 step 220) {
            canvas.drawLine(x.toFloat(), 110f, x.toFloat(), 690f, gridPaint)
        }
        for (y in 160..660 step 170) {
            canvas.drawLine(80f, y.toFloat(), 1110f, y.toFloat(), gridPaint)
        }

        // Header block
        val headerText = if (language == AppLanguage.TH) {
            "แบบแปลนสถาปัตยกรรม ชั้น 2 (A-102) • บ้านพักอาศัย 2 ชั้น • มาตรส่วน 1:50"
        } else {
            "LEVEL 2 ARCHITECTURAL FLOOR PLAN (A-102) • 2-STORY RESIDENCE • SCALE 1:50"
        }
        canvas.drawText(headerText, 64f, 82f, titlePaint)
        canvas.drawLine(36f, 100f, w - 36f, 100f, borderPaint)

        // Main floor outline
        val planRect = RectF(140f, 160f, 1020f, 650f)
        canvas.drawRect(planRect, wallPaint)
        // Interior partitions
        canvas.drawLine(580f, 160f, 580f, 650f, wallPaint) // vertical split
        canvas.drawLine(580f, 410f, 1020f, 410f, wallPaint) // bath/bedroom split

        // Room labels
        canvas.drawText(
            if (language == AppLanguage.TH) "ห้องนอนใหญ่ (MASTER BEDROOM)" else "MASTER BEDROOM",
            220f,
            390f,
            labelPaint
        )
        canvas.drawText("EL +3.20 m • 24.5 sqm", 260f, 420f, subPaint)

        canvas.drawText(
            if (language == AppLanguage.TH) "โถงบันได / ห้องนั่งเล่นบน" else "UPPER LIVING / HALLWAY",
            650f,
            280f,
            labelPaint
        )
        canvas.drawText("EL +3.20 m • 16.0 sqm", 680f, 310f, subPaint)

        canvas.drawText(
            if (language == AppLanguage.TH) "ห้องน้ำชั้น 2 (BATHROOM)" else "BATHROOM 2",
            680f,
            530f,
            labelPaint
        )
        canvas.drawText("Floor Drop -50 mm (Waterproof)", 680f, 560f, subPaint)

        // Window W1 (Top wall left)
        canvas.drawLine(280f, 160f, 420f, 160f, openingPaint)
        canvas.drawRoundRect(RectF(270f, 114f, 465f, 150f), 8f, 8f, badgeFillPaint)
        canvas.drawRoundRect(RectF(270f, 114f, 465f, 150f), 8f, 8f, badgeStrokePaint)
        canvas.drawText("W1 (1200x2000)", 284f, 138f, badgeTextPaint)

        // Window W2 (Left wall)
        canvas.drawLine(140f, 320f, 140f, 490f, openingPaint)
        canvas.drawRoundRect(RectF(52f, 368f, 134f, 434f), 8f, 8f, badgeFillPaint)
        canvas.drawRoundRect(RectF(52f, 368f, 134f, 434f), 8f, 8f, badgeStrokePaint)
        canvas.drawText("W2", 78f, 396f, badgeTextPaint)
        canvas.drawText("180x150", 58f, 422f, subPaint)

        // Door D1 (Partition to Master Bedroom)
        canvas.drawLine(580f, 230f, 580f, 330f, openingPaint)
        canvas.drawRoundRect(RectF(476f, 255f, 570f, 305f), 8f, 8f, badgeFillPaint)
        canvas.drawRoundRect(RectF(476f, 255f, 570f, 305f), 8f, 8f, badgeStrokePaint)
        canvas.drawText("D1 90x210", 482f, 286f, badgeTextPaint)

        // Door D2 (Bathroom)
        canvas.drawLine(720f, 410f, 810f, 410f, openingPaint)
        canvas.drawRoundRect(RectF(700f, 422f, 835f, 462f), 8f, 8f, badgeFillPaint)
        canvas.drawRoundRect(RectF(700f, 422f, 835f, 462f), 8f, 8f, badgeStrokePaint)
        canvas.drawText("D2 (800x2000)", 708f, 448f, badgeTextPaint)

        // Footer note
        val note = if (language == AppLanguage.TH) {
            "หมายเหตุหน้างาน: ระยะช่องเปิดข้างต้นเป็นระยะสุทธิหลังฉาบ (Finished Opening) • แตะบนแปลนเพื่อปักหมุดจุด Defect"
        } else {
            "NOTE: Dimensions shown are Finished Rough Openings after plastering (+/- 5mm max tolerance). Tap drawing to drop Defect Pin."
        }
        canvas.drawText(note, 64f, 765f, subPaint)
        canvas.drawText("Page 1 / 2 • Floor Plan A-102", w - 270f, 795f, subPaint)
    }

    private fun drawSampleSchedulePage(canvas: Canvas, w: Float, h: Float, language: AppLanguage) {
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        val rowAltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val thTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
        }
        val boldCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0369A1")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawRect(36f, 36f, w - 36f, h - 36f, borderPaint)
        canvas.drawText(
            if (language == AppLanguage.TH) {
                "ตารางแบบขยายประตู-หน้าต่าง (A-501 DOOR & WINDOW SCHEDULE)"
            } else {
                "ARCHITECTURAL DOOR & WINDOW SCHEDULE (A-501)"
            },
            64f,
            84f,
            titlePaint
        )

        val tableLeft = 64f
        val tableRight = w - 64f
        var y = 130f
        val rowH = 92f

        // Table Header
        canvas.drawRect(tableLeft, y, tableRight, y + 64f, headerBgPaint)
        canvas.drawText("CODE", tableLeft + 24f, y + 40f, thTextPaint)
        canvas.drawText("SPEC SIZE (W x H)", tableLeft + 170f, y + 40f, thTextPaint)
        canvas.drawText("FRAME / TYPE", tableLeft + 460f, y + 40f, thTextPaint)
        canvas.drawText("MASONRY & ALUM TOLERANCE", tableLeft + 740f, y + 40f, thTextPaint)
        y += 64f

        val rows = listOf(
            listOf("W1", "1200 x 2000 mm (120 x 200 cm)", "Powder-Coat Black Alum", "Plaster opening +10mm max; Sill slope 1:50 out"),
            listOf("W2", "1800 x 1500 mm (180 x 150 cm)", "Sliding Glass 6mm Tempered", "Plumb <= 3mm/m; Silicone perimeter seal"),
            listOf("D1", "900 x 2100 mm (90 x 210 cm)", "Solid Timber + WPC Subframe", "Finished lintel 2110 mm; Gap <= 5mm"),
            listOf("D2", "800 x 2000 mm (80 x 200 cm)", "uPVC / Moisture-Rated Door", "Bathroom drop 50mm; Waterproof curb")
        )

        rows.forEachIndexed { idx, row ->
            if (idx % 2 == 1) {
                canvas.drawRect(tableLeft, y, tableRight, y + rowH, rowAltPaint)
            }
            canvas.drawRect(tableLeft, y, tableRight, y + rowH, borderPaint)
            canvas.drawText(row[0], tableLeft + 24f, y + 54f, boldCellPaint)
            canvas.drawText(row[1], tableLeft + 170f, y + 54f, cellPaint)
            canvas.drawText(row[2], tableLeft + 460f, y + 54f, cellPaint)
            canvas.drawText(row[3], tableLeft + 740f, y + 54f, cellPaint)
            y += rowH
        }

        val ruleSummary = if (language == AppLanguage.TH) {
            "ข้อกำหนดตรวจรับ: หากช่องปูนหลังฉาบแคบกว่าขนาด Spec ข้างต้น > 5 มม. ฝั่งงานปูนต้องแก้ไขเปิดช่องให้ได้มาตรฐานก่อนติดตั้งวงกบอลูมิเนียม"
        } else {
            "INSPECTION RULE: If plastered masonry rough opening is narrower than Spec by >5mm, Masonry sub-contractor must rectify prior to Aluminum frame installation."
        }
        canvas.drawText(ruleSummary, 64f, 640f, cellPaint)
        canvas.drawText("Page 2 / 2 • Door & Window Schedule A-501", w - 360f, 795f, cellPaint)
    }

    /**
     * Composites all numbered defect pins (#1, #2...) + an immutable real-time timestamp footer
     * onto a copy of the PDF page bitmap and stores it via FileProvider for Email dispatch.
     */
    suspend fun renderAnnotatedBlueprintSnapshot(
        context: Context,
        baseBitmap: Bitmap,
        pins: List<SiteWalkDefectEntity>,
        floorZoneLabel: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val outBmp = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(outBmp)
            val w = outBmp.width.toFloat()
            val h = outBmp.height.toFloat()
            val radius = (w.coerceAtMost(h) * 0.026f).coerceIn(18f, 40f)

            val pinFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }
            val pinBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            val pinTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = radius * 0.95f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            pins.forEach { defect ->
                val xRatio = defect.pinXRatio ?: return@forEach
                val yRatio = defect.pinYRatio ?: return@forEach
                val cx = (xRatio * w).coerceIn(radius, w - radius)
                val cy = (yRatio * h).coerceIn(radius, h - radius - 42f)
                pinFillPaint.color = when (defect.severityLevel.uppercase()) {
                    "CRITICAL" -> Color.parseColor("#DC2626")
                    "MINOR" -> Color.parseColor("#0284C7")
                    else -> Color.parseColor("#D97706")
                }
                canvas.drawCircle(cx, cy, radius, pinFillPaint)
                canvas.drawCircle(cx, cy, radius, pinBorderPaint)
                val textY = cy - (pinTextPaint.descent() + pinTextPaint.ascent()) / 2f
                canvas.drawText("#${defect.pinNumber}", cx, textY, pinTextPaint)
            }

            // Bottom timestamp lock banner
            val footerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(225, 15, 23, 42)
                style = Paint.Style.FILL
            }
            val footerText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00E5FF")
                textSize = (w * 0.018f).coerceIn(16f, 28f)
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }
            val nowFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            canvas.drawRect(0f, h - 46f, w, h, footerBg)
            canvas.drawText(
                "NANO 4 INSPECT • $floorZoneLabel • LOCKED: $nowFormatted",
                20f,
                h - 15f,
                footerText
            )

            val file = File(getPlansDir(context), "ANNOTATED_PLAN_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                outBmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            outBmp.recycle()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to render annotated blueprint snapshot", e)
            null
        }
    }
}
