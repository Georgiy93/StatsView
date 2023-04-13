package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes

import ru.netology.statsview.utils.AndroidUtils
import ru.netology.statsview2.R
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null,
    deffStyleAttr: Int = 0,
    deffStyleRes: Int = 0
) : View(
    context,
    attributeSet,
    deffStyleAttr,
    deffStyleRes
) {
    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5)
    private var colors = emptyList<Int>()

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor())
            )

        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            calculatePercentages()
            invalidate()
            update()
        }
    private var percentages: List<Float> = emptyList()
    private fun calculatePercentages() {
        val sum = data.sum()
        percentages = if (sum == 0F) {
            List(data.size) { 0F }
        } else {
            data.map { it / sum * 100 }
        }
    }

    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            strokeWidth = lineWidth.toFloat()
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            textSize = this@StatsView.textSize
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null
    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }
        var startAngle = -90F + 360F * progress

        percentages.forEachIndexed { index, percentage ->
            var angle = percentage * 3.6F

            paint.color = colors.getOrElse(index) { generateRandomColor() }



            canvas.drawArc(oval, startAngle, angle * progress, false, paint)

            startAngle += angle
            if (index == percentages.size - 1) {
                paint.color = colors.get(0)
                canvas.drawArc(oval, startAngle, -1F, false, paint)
            }
        }


        canvas.drawText(
            "%.2f%%".format(percentages.sum()),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F
        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 5000
            interpolator = LinearInterpolator()


        }.also { it.start() }
    }

}