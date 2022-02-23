package com.example.compass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class FloatingCompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val paint = Paint()
        val paint2 = Paint()

        paint.color = Color.WHITE
        paint.textSize = 50f
        paint2.color = Color.DKGRAY

        canvas?.drawCircle(width/2f, width/2f, 200F, paint2 )

        canvas?.drawLine(width/2f, height/2f - 100, width/2f, height/2f + 100, paint)
        canvas?.drawLine(width/2f - 100, height/2f, width/2f + 100, height/2f, paint)

        canvas?.drawText("N", width/2f - 15, height/2f - 120, paint)
        canvas?.drawText("S", width/2f - 15, height/2f + 120 + 40, paint)
        canvas?.drawText("E", width/2f  + 120, height/2f + 15, paint)
        canvas?.drawText("W", width/2f  - 120 - 40, height/2f + 15, paint)

        //Log.v("compasslog", "height ${canvas?.height}")

        //TODO: What reference width and height?
        //TODO: Modify CompassView avoiding @Jvm...
    }

}