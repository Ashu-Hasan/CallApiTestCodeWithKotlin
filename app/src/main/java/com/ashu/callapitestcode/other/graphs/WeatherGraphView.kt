package com.ashu.callapitestcode.other.graphs

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.ashu.callapitestcode.data.model.WeatherItem

class WeatherGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // =========================
    // DATA
    // =========================
    private var data = mutableListOf<WeatherItem>()
    private var icons = mutableListOf<Bitmap?>()

    // =========================
    // CUSTOMIZATION
    // =========================
    private var itemWidth = 180f

    private var timeY = 40f
    private var iconY = 75f
    private var tempY = 200f
    private var graphTop = 240f
    private var graphHeight = 60f
    private var rainY = 360f

    private var iconSize = 60

    // =========================
    // PAINTS
    // =========================
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    private val rainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    private val path = Path()

    // =========================
    // PUBLIC METHODS
    // =========================
    fun setData(list: List<WeatherItem>) {
        data = list.toMutableList()
        loadIcons()
        requestLayout()
        invalidate()
    }

    fun setTextSizes(tempSize: Float, timeSize: Float, rainSize: Float) {
        tempPaint.textSize = tempSize
        timePaint.textSize = timeSize
        rainPaint.textSize = rainSize
        invalidate()
    }

    fun setColors(lineColor: Int, textColor: Int) {
        linePaint.color = lineColor
        tempPaint.color = textColor
        timePaint.color = textColor
        rainPaint.color = textColor
        dotPaint.color = textColor
        invalidate()
    }

    fun setVerticalSpacing(time: Float, icon: Float, temp: Float, graph: Float, rain: Float) {
        timeY = time
        iconY = icon
        tempY = temp
        graphTop = graph
        rainY = rain
        invalidate()
    }

    fun setItemWidth(width: Float) {
        itemWidth = width
        requestLayout()
    }

    // =========================
    // ICON LOADING
    // =========================
    private fun loadIcons() {
        icons = MutableList(data.size) { null }

        data.forEachIndexed { index, item ->
            ImageLoaderUtil.loadSvgBitmap(context, item.iconUrl) { bitmap ->
                bitmap?.let {
                    val scaled = Bitmap.createScaledBitmap(it, iconSize, iconSize, true)
                    icons[index] = scaled
                    invalidate()
                }
            }
        }
    }

    // =========================
    // MEASURE (SCROLL)
    // =========================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (itemWidth * data.size).toInt()
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    // =========================
    // DRAW
    // =========================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val max = getMax()
        val min = getMin()
        val range = if (max - min == 0f) 1f else (max - min)

        path.reset()

        var prevX = 0f
        var prevY = 0f

        data.forEachIndexed { i, item ->

            val x = i * itemWidth + itemWidth / 2
            val y = graphTop + (1 - (item.temp - min) / range) * graphHeight

            // Smooth curve
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val midX = (prevX + x) / 2
                path.cubicTo(midX, prevY, midX, y, x, y)
            }

            prevX = x
            prevY = y

            // TIME
            canvas.drawText(item.time, x, timeY, timePaint)

            // ICON
            icons.getOrNull(i)?.let {
                canvas.drawBitmap(it, x - iconSize / 2, iconY, null)
            }

            // TEMP
            canvas.drawText("${item.temp.toInt()}°", x, tempY, tempPaint)

            // DOT
            canvas.drawCircle(x, y, 6f, dotPaint)

            // RAIN
            canvas.drawText("☁ ${item.rain}%", x, rainY, rainPaint)
        }

        canvas.drawPath(path, linePaint)
    }

    // =========================
    // HELPERS
    // =========================
    private fun getMax(): Float = data.maxOf { it.temp }

    private fun getMin(): Float = data.minOf { it.temp }

    fun adjustLayout(iconGap: Float, tempGap: Float, graphSize: Float) {
        iconY = timeY + iconGap
        tempY = iconY + tempGap
        graphHeight = graphSize
        invalidate()
    }
}