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