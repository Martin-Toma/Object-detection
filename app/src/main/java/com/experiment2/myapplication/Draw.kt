package com.experiment2.myapplication

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View

class Draw(context: Context?, private val rect: RectF, private val text: String, private val i_width: Int, private val i_height: Int, private val viewWidth: Int, private val viewHeight: Int): View(context) {
    lateinit var boundaryPaint: Paint
    lateinit var textPaint: Paint

    init {
        init()
    }

    private fun init(){
        boundaryPaint = Paint()
        boundaryPaint.color = Color.BLUE
        boundaryPaint.strokeWidth = 10f
        boundaryPaint.style = Paint.Style.STROKE

        textPaint = Paint()
        textPaint.color = Color.BLUE
        textPaint.textSize = 50f
        textPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        Log.d("Position", "${rect.left.toFloat()}")

        canvas?.drawText(text, rect.centerX().toFloat(), rect.centerY().toFloat(), textPaint)
        val x1 = rect.left.toFloat() * viewWidth / i_width
        val y1 =  rect.top.toFloat() * viewHeight / i_height
        val x2 =  rect.right.toFloat() * viewWidth / i_width
        val y2 = rect.bottom.toFloat() * viewHeight / i_height
        canvas?.drawRect(x1, y1, x2, y2, boundaryPaint)
    }
}