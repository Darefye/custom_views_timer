package com.example.timer.clock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.*
import kotlin.math.cos
import kotlin.math.sin

const val ONE_DAY = 86400000L

class AnalogClockView : View {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    private var h = 0
    private var w: Int = 0
    private val rect = Rect()
    private var numbers = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private var padding = 0
    private var numeralSpacing = 0
    private var handTruncation = 0
    private var hourHandTruncation: Int = 0
    private var radius = 0
    private var paint = Paint()
    private var isInit = false
    private var timeListeners = mutableSetOf<(TimeState) -> Unit>()
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var currentTime = 0L
    private var timeState = TimeState(0L, false)
        set(value) {
            if (value == field) return
            field = value
            timeListeners.forEach { it(value) }
        }


    private fun init() {
        paint = Paint()
    }

    override fun onDraw(canvas: Canvas) {
        if (!isInit) {
            h = height
            w = width
            padding = numeralSpacing + 50

            val minAttr = h.coerceAtMost(w)
            radius = minAttr / 2 - padding

            handTruncation = minAttr / 20
            hourHandTruncation = minAttr / 17

            isInit = true
        }


        // Circle clock
        setPaintAttributes(Color.BLACK, Paint.Style.STROKE, 4F)
        canvas.drawCircle(width.toFloat() / 2, height.toFloat() / 2, radius + padding - 10F, paint)

        // Center point
        setPaintAttributes(Color.BLACK, Paint.Style.FILL, 2F)
        canvas.drawCircle(w / 2F, h / 2F, 12F, paint)

        // Border of hours
        setPaintAttributes(Color.BLACK, Paint.Style.STROKE, 2F)
        paint.textSize = 32F
        paint.style = Paint.Style.FILL

        for (number in numbers) {
            val num = number.toString()
            paint.getTextBounds(num, 0, num.length, rect)
            val angle = Math.PI / 6 * (number - 3)
            canvas.drawText(
                num,
                (h / 2F + cos(angle) * radius - rect.width() / 2).toFloat(),
                (w / 2F + sin(angle) * radius + rect.height() / 2).toFloat(),
                paint
            )
        }

        // draw hands clock
        val hour = currentTime / 1000 / 3600
        val minutes = currentTime / 1000 / 60 % 60
        val second = currentTime / 1000 % 60

        //draw hours
        setPaintAttributes(Color.BLACK, Paint.Style.STROKE, 4F)
        drawHandLine(canvas, hour * 5, isHour = true)

        //draw minutes
        setPaintAttributes(Color.BLACK, Paint.Style.STROKE, 2F)
        drawHandLine(canvas, minutes, isHour = false)

        //draw seconds
        setPaintAttributes(Color.RED, Paint.Style.STROKE, 2F)
        drawHandLine(canvas, second, isHour = false)

        postInvalidateDelayed(500)
        invalidate()
    }

    private fun drawHandLine(canvas: Canvas, moment: Long, isHour: Boolean) {
        val angle = Math.PI * moment / 30 - Math.PI / 2
        val handRadius =
            if (isHour) radius - handTruncation - hourHandTruncation else radius - handTruncation
        canvas.drawLine(
            (w / 2).toFloat(),
            (h / 2).toFloat(),
            (w / 2 + cos(angle) * handRadius).toFloat(),
            (h / 2 + sin(angle) * handRadius).toFloat(),
            paint
        )

    }

    fun addUpdateListener(listener: (TimeState) -> Unit) {
        timeListeners.add(listener)
        listener(timeState)
    }

    fun removeUpdateListener(listener: (TimeState) -> Unit) = run {
        mainScope.coroutineContext.cancelChildren()
        timeListeners.remove(listener)
    }

    fun currentTime(): Long = timeState.time

    //Start timer
    fun start(time: Long) {
        addSeconds(isStart = true, time)
        mainScope.launch {
            while (timeState.isPlayed) {
                addSeconds()
                delay(1000)
            }
        }
    }

    //Stop timer
    fun stop() {
        timeState = TimeState(currentTime(), false)
    }

    //Reset timer
    fun reset() {
        timeState = TimeState(0, false)
        currentTime = 0L
    }

    private fun addSeconds(isStart: Boolean = false, time: Long = 0L) {
        if (isStart.not()) currentTime += 1000
        if (time != 0L) currentTime = time
        if (currentTime >= ONE_DAY) reset()
        timeState = TimeState(currentTime, isPlayed = true)
    }

    private fun setPaintAttributes(colour: Int, stroke: Paint.Style, strokeWidth: Float) {
        paint.reset()
        paint.color = colour
        paint.style = stroke
        paint.strokeWidth = strokeWidth
        paint.isAntiAlias = true
    }


}