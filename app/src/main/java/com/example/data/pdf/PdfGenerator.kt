package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.MockPaperEntity
import com.example.data.local.PaymentEntity
import com.example.data.local.PdfNoteEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun createNotePdf(context: Context, note: PdfNoteEntity): File {
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val file = File(dir, "target_ssb_note_${note.id}.pdf")
        writeTextPdf(
            file = file,
            headerBrand = "TARGET SSB LEARNING PLATFORM",
            subHeader = "${note.courseTitle} • ${note.subject} • ${note.chapter}",
            title = note.title,
            body = note.contentBody
        )
        return file
    }

    fun createMockPaperPdf(context: Context, paper: MockPaperEntity): File {
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val file = File(dir, "target_ssb_paper_${paper.id}.pdf")
        val content = """
Exam Paper Type: ${paper.paperType} (${paper.yearLabel})
Course: ${paper.courseTitle}

Summary & Pattern:
${paper.questionsSummary}

--------------------------------------------------
${paper.answerKeyText}
        """.trimIndent()
        writeTextPdf(
            file = file,
            headerBrand = "TARGET SSB – QUESTION PAPER & ANSWER KEY",
            subHeader = "${paper.courseTitle} • ${paper.yearLabel}",
            title = paper.title,
            body = content
        )
        return file
    }

    fun createReceiptPdf(context: Context, payment: PaymentEntity): File {
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val file = File(dir, "receipt_${payment.transactionId}.pdf")
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(payment.createdAt))
        val body = """
OFFICIAL PAYMENT RECEIPT
==================================================

Student Name      : ${payment.userName}
Course Purchased  : ${payment.courseTitle}
Amount Paid       : INR ₹${payment.amountInr}/-
Payment Method    : ${payment.method}
Transaction ID    : ${payment.transactionId}
Payment Status    : ${payment.status}
Date & Time       : $dateStr
Verified          : ${if (payment.verifiedByAdmin) "Yes (Server Verified)" else "Pending"}

--------------------------------------------------
Headline: আপনার সফলতার প্রস্তুতি শুরু হোক Target SSB-এর সাথে
Tagline : Learn Today, Achieve Tomorrow.
Thank you for learning with Target SSB!
        """.trimIndent()
        writeTextPdf(
            file = file,
            headerBrand = "TARGET SSB – PAYMENT RECEIPT",
            subHeader = "Transaction ID: ${payment.transactionId}",
            title = "Course Enrollment Invoice",
            body = body
        )
        return file
    }

    fun shareOrOpenPdf(context: Context, file: File, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    private fun writeTextPdf(
        file: File,
        headerBrand: String,
        subHeader: String,
        title: String,
        body: String
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f

        val headerBgPaint = Paint().apply { color = Color.parseColor("#1A237E") }
        val accentPaint = Paint().apply { color = Color.parseColor("#FF6B2C") }
        val brandPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subHeaderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 10f
        }
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 11.5f
        }
        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9.5f
        }

        val wrappedLines = mutableListOf<String>()
        val maxTextWidth = pageWidth - margin * 2
        body.lines().forEach { rawLine ->
            if (rawLine.isBlank()) {
                wrappedLines.add("")
            } else {
                var current = ""
                for (word in rawLine.split(" ")) {
                    val candidate = if (current.isEmpty()) word else "$current $word"
                    if (bodyPaint.measureText(candidate) <= maxTextWidth) {
                        current = candidate
                    } else {
                        if (current.isNotEmpty()) wrappedLines.add(current)
                        current = word
                    }
                }
                if (current.isNotEmpty()) wrappedLines.add(current)
            }
        }

        val linesPerPage = 36
        val chunks = if (wrappedLines.isEmpty()) listOf(emptyList()) else wrappedLines.chunked(linesPerPage)

        chunks.forEachIndexed { pageIndex, lines ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(Color.WHITE)
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 76f, headerBgPaint)
            canvas.drawRect(0f, 76f, pageWidth.toFloat(), 81f, accentPaint)
            canvas.drawText(headerBrand, margin, 34f, brandPaint)
            canvas.drawText(subHeader.take(78), margin, 56f, subHeaderPaint)

            var y = 115f
            if (pageIndex == 0) {
                canvas.drawText(title.take(65), margin, y, titlePaint)
                y += 28f
            }

            lines.forEach { line ->
                canvas.drawText(line, margin, y, bodyPaint)
                y += 18f
            }

            canvas.drawText(
                "Target SSB • Learn Today, Achieve Tomorrow. • Page ${pageIndex + 1} of ${chunks.size}",
                margin,
                pageHeight - 24f,
                footerPaint
            )
            document.finishPage(page)
        }

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
    }
}
