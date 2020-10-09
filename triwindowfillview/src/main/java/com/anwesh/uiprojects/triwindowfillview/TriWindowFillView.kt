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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TWFNode(var i : Int, val state : State = State()) {

        private var next : TWFNode? = null
        private var prev : TWFNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = TWFNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTWFNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TWFNode {
            var curr : TWFNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class TriWindowFill(var i : Int) {

        private var curr : TWFNode = TWFNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TriWindowFillView) {

        private val animator : Animator = Animator(view)
        private val tfw : TriWindowFill = TriWindowFill(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            tfw.draw(canvas, paint)
            animator.animate {
                tfw.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            tfw.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : TriWindowFillView {
            val view : TriWindowFillView = TriWindowFillView(activity)
            activity.setContentView(view)
            return view
        }
    }
}