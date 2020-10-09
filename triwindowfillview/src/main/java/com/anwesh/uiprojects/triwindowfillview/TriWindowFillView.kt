package com.anwesh.uiprojects.triwindowfillview

/**
 * Created by anweshmishra on 10/10/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.graphics.Paint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF

val colors : Array<Int> = arrayOf(
        "#F44336",
        "#673AB7",
        "#009688",
        "#3F51B5",
        "#4CAF50"
).map({Color.parseColor(it)}).toTypedArray()
val parts : Int = 3
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawFillPath(scale : Float, w : Float, h : Float, size : Float, paint : Paint) {
    val path : Path = Path()
    path.moveTo(w / 2 - size / 2, h)
    path.lineTo(w / 2 - size / 2, h / 2)
    path.lineTo(w / 2, 0f)
    path.lineTo(w / 2 + size / 2, h / 2)
    path.lineTo(w / 2 + size / 2, h)
    path.lineTo(w / 2 - size / 2, h)
    clipPath(path)
    drawRect(RectF(0f, h * (1 - scale), w, h), paint)
}

fun Canvas.drawTriWindowFill(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    save()
    translate(w / 2 - size / 2, h)
    for (j in 0..1) {
        save()
        translate(size * j, 0f)
        drawLine(0f, 0f, 0f, -h * 0.5f * sf1, paint)
        drawLine(0f, h / 2, size * (1f - 2 * j) * sf2,  h * 0.5f * (1 - sf2), paint)
        restore()
    }
    restore()
    drawFillPath(sf3, w, h, size, paint)
}

fun Canvas.drawTWFNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawTriWindowFill(scale, w, h, paint)
}

class TriWindowFillView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}