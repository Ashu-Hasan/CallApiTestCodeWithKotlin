package com.ashu.callapitestcode.other.graphs

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class AQISeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private var isUserInteractionEnabled = true

    private var progress = 100f
    private var max = 600f
    private var thumbStrokeWidth = 6f

    companion object {
        const val TEXT_GRAVITY_START = 0
        const val TEXT_GRAVITY_END = 1

        const val BOTTOM_NONE = 0
        const val BOTTOM_NUMBERS = 1
        const val BOTTOM_CUSTOM = 2
    }

    private var bottomTextGravity = TEXT_GRAVITY_START

    private var thumbRadius = 22f
    private var trackHeight = 14f
    private var padding = 100f

    private var showLabels = true
    private var showBubble = true

    private var customThumbDrawable: Drawable? = null

    private var bottomMode = BOTTOM_NUMBERS
    private var customBottomTexts = mutableListOf<String>()
    private var bottomTextList = mutableListOf<BottomText>()

    data class BottomText(
        val startText: String?,
        val endText: String?
    )

    data class Level(
        val min: Float,
        val max: Float,
        val color: Int,
        val label: String
    )

    private var levels = mutableListOf(
        Level(0f, 50f, Color.GREEN, "Good"),
        Level(50f, 100f, Color.YELLOW, "Moderate"),
        Level(100f, 200f, Color.parseColor("#FFA500"), "Poor"),
        Level(200f, 300f, Color.MAGENTA, "Unhealthy"),
        Level(300f, 400f, Color.parseColor("#FF6666"), "Severe"),
        Level(400f, 500f, Color.RED, "Hazardous")
    )

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val centerY = height / 2f

        val safePadding = maxOf(padding, thumbRadius + 20)
        val startX = safePadding
        val endX = width - safePadding
        val totalWidth = endX - startX

        val segmentWidth = totalWidth / levels.size

        // 🎨 segments
        levels.forEachIndexed { i, level ->
            val left = startX + (i * segmentWidth)
            val right = left + segmentWidth

            paint.color = level.color
            paint.strokeWidth = trackHeight
            paint.strokeCap = if (i == 0 || i == levels.lastIndex)
                Paint.Cap.ROUND else Paint.Cap.BUTT

            canvas.drawLine(left, centerY, right, centerY, paint)
        }

        val progressX = startX + ((progress / max) * totalWidth)
        val currentColor = getColorForProgress(progress)

        // 🎯 Thumb
        customThumbDrawable?.let {
            val size = 60
            it.setBounds(
                (progressX - size / 2).toInt(),
                (centerY - size / 2).toInt(),
                (progressX + size / 2).toInt(),
                (centerY + size / 2).toInt()
            )
            it.draw(canvas)
        } ?: run {
            paint.setShadowLayer(8f, 0f, 2f, Color.GRAY)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = thumbStrokeWidth
            paint.color = currentColor
            canvas.drawCircle(progressX, centerY, thumbRadius, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            canvas.drawCircle(progressX, centerY, thumbRadius - thumbStrokeWidth, paint)

            paint.clearShadowLayer()
        }

        // 🔤 Labels
        if (showLabels) {
            levels.forEachIndexed { i, level ->
                val x = startX + (i * segmentWidth) + (segmentWidth / 2)
                canvas.drawText(level.label, x, centerY - 60, textPaint)
            }
        }

        // 🔢 Bottom text
        if (bottomMode != BOTTOM_NONE) {
            var lastDrawnRight = Float.MIN_VALUE

            levels.forEachIndexed { i, level ->
                val text = when (bottomMode) {
                    BOTTOM_NUMBERS -> level.min.toInt().toString()
                    BOTTOM_CUSTOM -> {
                        val bt = bottomTextList.getOrNull(i) ?: return@forEachIndexed
                        bt.startText ?: bt.endText ?: return@forEachIndexed
                    }
                    else -> return@forEachIndexed
                }

                val x = if (bottomTextGravity == TEXT_GRAVITY_END)
                    startX + ((i + 1) * segmentWidth)
                else
                    startX + (i * segmentWidth)

                val textWidth = textPaint.measureText(text)
                val left = x - textWidth / 2
                val right = x + textWidth / 2

                if (left < lastDrawnRight + 10) return@forEachIndexed

                canvas.drawText(text, x, centerY + 80, textPaint)
                lastDrawnRight = right
            }
        }

        // 💬 Bubble
        if (showBubble) {
            drawBubble(canvas, progressX, centerY, currentColor)
        }
    }

    private fun drawBubble(canvas: Canvas, x: Float, centerY: Float, color: Int) {
        val text = "${progress.toInt()} - ${getLabelForProgress(progress)}"

        textPaint.textSize = 30f

        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)

        val padding = 20f
        val w = bounds.width() + padding * 2
        val h = bounds.height() + padding

        val left = x - w / 2
        val top = centerY - 150

        paint.color = color
        canvas.drawRoundRect(left, top, left + w, top + h, 20f, 20f, paint)

        textPaint.color = Color.WHITE
        canvas.drawText(text, x, top + h - 20, textPaint)

        textPaint.color = Color.BLACK
    }

    private fun getColorForProgress(progress: Float): Int {
        return levels.find { progress in it.min..it.max }?.color ?: Color.GRAY
    }

    private fun getLabelForProgress(progress: Float): String {
        return levels.find { progress in it.min..it.max }?.label ?: ""
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isUserInteractionEnabled) return false

        val width = width.toFloat()
        val startX = padding
        val endX = width - padding

        var x = event.x.coerceIn(startX, endX)
        val totalWidth = endX - startX

        progress = ((x - startX) / totalWidth) * max
        invalidate()

        return true
    }

    // 🔧 Public APIs
    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, max)
        invalidate()
    }

    fun getProgress(): Float = progress

    fun setLevels(list: List<Level>) {
        levels = list.toMutableList()
        invalidate()
    }

    fun setShowLabels(show: Boolean) {
        showLabels = show
        invalidate()
    }

    fun setShowBubble(show: Boolean) {
        showBubble = show
        invalidate()
    }

    fun setCustomThumbDrawable(drawable: Drawable?) {
        customThumbDrawable = drawable
        invalidate()
    }

    fun setUserInteractionEnabled(enabled: Boolean) {
        isUserInteractionEnabled = enabled
    }

    fun setBottomTexts(list: List<BottomText>) {
        bottomTextList = list.toMutableList()
        invalidate()
    }

    fun setBottomMode(mode: Int) {
        bottomMode = mode
        invalidate()
    }
}