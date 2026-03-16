package com.miftakhulrizky.yearprogress.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.miftakhulrizky.yearprogress.R
import com.miftakhulrizky.yearprogress.model.DayCell
import com.miftakhulrizky.yearprogress.model.MonthProgress
import com.miftakhulrizky.yearprogress.model.YearProgressState
import kotlin.math.min

private object Palette {
    const val BACKGROUND = 0xFF111214.toInt()
    const val CARD = 0xFF111214.toInt()
    const val INNER_CARD = 0xFF111214.toInt()
    const val BORDER = 0x00000000
    const val TEXT_PRIMARY = 0xFFFFFFFF.toInt()
    const val TEXT_MUTED = 0x33FFFFFF
    const val DOT_PENDING = 0x33FFFFFF // 20% opacity white
    const val DOT_FILLED = 0xFFFFFFFF.toInt()
    const val DOT_HOLIDAY = 0xFFFD2639.toInt() // AccentRed
    const val DOT_HOLIDAY_PENDING = 0x33FD2639 // 20% opacity AccentRed
}

class YearProgressRenderer(context: Context) {
    private val regularTypeface = ResourcesCompat.getFont(context, R.font.ibm_plex_mono_regular)
        ?: Typeface.MONOSPACE

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.BACKGROUND }
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.CARD }
    private val innerCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Palette.INNER_CARD }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.BORDER
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.TEXT_PRIMARY
        textAlign = Paint.Align.LEFT
        typeface = regularTypeface
    }
    private val mutedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.TEXT_MUTED
        textAlign = Paint.Align.LEFT
        typeface = regularTypeface
    }
    private val accentTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = regularTypeface
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        state: YearProgressState,
        pulseProgress: Float,
        accentColor: Int
    ) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val scale = min(width / 412f, height / 915f)
        val cardWidth = 380f * scale
        val cardHeight = 522f * scale
        val cardLeft = (width - cardWidth) / 2f
        val cardTop = (height - cardHeight) / 2f
        val cardRect = RectF(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight)
        val footerHeight = 32f * scale
        val footerGap = 10f * scale
        val innerRect = RectF(
            cardRect.left + 4f * scale,
            cardRect.top + 4f * scale,
            cardRect.right - 4f * scale,
            cardRect.bottom - 4f * scale - footerGap - footerHeight
        )

        borderPaint.strokeWidth = scale
        canvas.drawRoundRect(cardRect, 14f * scale, 14f * scale, cardPaint)
        canvas.drawRoundRect(cardRect, 14f * scale, 14f * scale, borderPaint)
        canvas.drawRoundRect(innerRect, 10f * scale, 10f * scale, innerCardPaint)

        val contentLeft = innerRect.left + 16f * scale
        val contentTop = innerRect.top + 16f * scale
        val dotSize = 6f * scale
        val gap = 8f * scale
        val monthGridWidth = (dotSize * 7f) + (gap * 6f)
        val maxMonthGridHeight = (dotSize * 6f) + (gap * 5f)
        val monthBlockHeight = 22f * scale + maxMonthGridHeight
        val columnGap = (innerRect.width() - 32f * scale - monthGridWidth * 3f) / 2f
        val availableHeight = innerRect.height() - 32f * scale
        val rowGap = ((availableHeight - monthBlockHeight * 4f) / 3f).coerceAtLeast(12f * scale)

        state.monthItems.chunked(3).forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, month ->
                val left = contentLeft + colIndex * (monthGridWidth + columnGap)
                val top = contentTop + rowIndex * (monthBlockHeight + rowGap)
                drawMonth(
                    canvas = canvas,
                    month = month,
                    left = left,
                    top = top,
                    scale = scale,
                    accentColor = accentColor
                )
            }
        }

        drawFooter(
            canvas = canvas,
            state = state,
            rect = cardRect,
            footerTop = innerRect.bottom + footerGap,
            footerHeight = footerHeight,
            scale = scale,
            accentColor = accentColor
        )
    }

    private fun drawFooter(
        canvas: Canvas,
        state: YearProgressState,
        rect: RectF,
        footerTop: Float,
        footerHeight: Float,
        scale: Float,
        accentColor: Int
    ) {
        textPaint.textSize = 12f * scale
        mutedTextPaint.textSize = textPaint.textSize
        accentTextPaint.textSize = textPaint.textSize
        accentTextPaint.color = accentColor

        val separator = "  ·  "
        val totalWidth = state.headerDate.let { textPaint.measureText(it) } +
            textPaint.measureText(separator) +
            accentTextPaint.measureText("${state.daysLeft} DAYS LEFT") +
            textPaint.measureText(separator) +
            textPaint.measureText("${state.percent}%")
        val footerPadding = 16f * scale
        val availableWidth = rect.width() - (footerPadding * 2f)
        val shrinkRatio = if (totalWidth > availableWidth) availableWidth / totalWidth else 1f
        textPaint.textSize *= shrinkRatio
        mutedTextPaint.textSize = textPaint.textSize
        accentTextPaint.textSize = textPaint.textSize
        val adjustedWidth = state.headerDate.let { textPaint.measureText(it) } +
            textPaint.measureText(separator) +
            accentTextPaint.measureText("${state.daysLeft} DAYS LEFT") +
            textPaint.measureText(separator) +
            textPaint.measureText("${state.percent}%")
        val baseline = footerTop + (footerHeight / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        var x = rect.left + ((rect.width() - adjustedWidth) / 2f)

        val parts = listOf(
            state.headerDate to textPaint,
            separator to textPaint,
            "${state.daysLeft} DAYS LEFT" to accentTextPaint,
            separator to textPaint,
            "${state.percent}%" to textPaint
        )

        parts.forEach { (label, paint) ->
            canvas.drawText(label, x, baseline, paint)
            x += paint.measureText(label)
        }
    }

    private fun drawMonth(
        canvas: Canvas,
        month: MonthProgress,
        left: Float,
        top: Float,
        scale: Float,
        accentColor: Int
    ) {
        textPaint.textSize = 12f * scale
        mutedTextPaint.textSize = textPaint.textSize
        accentTextPaint.textSize = textPaint.textSize
        accentTextPaint.color = accentColor

        val labelPaint = when {
            month.isCurrent -> accentTextPaint
            month.isPast -> textPaint
            else -> mutedTextPaint
        }

        val dotSize = 6f * scale
        val gap = 8f * scale
        val gridWidth = (dotSize * 7f) + (gap * 6f)
        val labelX = left + (gridWidth - labelPaint.measureText(month.label)) / 2f
        val labelBaseline = top + 6f * scale - ((labelPaint.descent() + labelPaint.ascent()) / 2f)
        canvas.drawText(month.label, labelX, labelBaseline, labelPaint)

        val startX = left
        val startY = top + 22f * scale

        month.cells.forEachIndexed { index, cell ->
            val column = index % 7
            val row = index / 7
            if (cell == DayCell.Empty) return@forEachIndexed

            dotPaint.alpha = 255
            dotPaint.color = when (cell) {
                DayCell.Filled -> Palette.DOT_FILLED
                DayCell.Pending -> Palette.DOT_PENDING
                DayCell.Current -> accentColor
                DayCell.Holiday -> Palette.DOT_HOLIDAY
                DayCell.HolidayPending -> Palette.DOT_HOLIDAY_PENDING
                DayCell.Empty -> Palette.DOT_PENDING
            }

            val cx = startX + column * (dotSize + gap) + dotSize / 2f
            val cy = startY + row * (dotSize + gap) + dotSize / 2f
            canvas.drawRoundRect(
                cx - dotSize / 2f,
                cy - dotSize / 2f,
                cx + dotSize / 2f,
                cy + dotSize / 2f,
                2f * scale,
                2f * scale,
                dotPaint
            )
        }
    }
}
