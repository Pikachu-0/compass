package com.example.compass


import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.SensorManager
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import android.view.*

//Extend from service. A Service is an application component that can perform long-running
// operations in the background. It does not provide a user interface. Once started,
// a service might continue running for some time, even after the user switches to another application.
class FloatingWindow : Service() {

    private lateinit var compassLogic: CompassLogic
    private var carryOverRotation: Int? = 0
    private lateinit var compassViewFloat: FloatingCompassView

    //variables of the floating window
    private lateinit var floatView: ViewGroup
    private lateinit var floatWindowLayoutParams: WindowManager.LayoutParams

    private lateinit var windowManager: WindowManager
    //TODO: mostrar el compassview desde t=0 apuntando al norte según lo hace activity main layout

    //This method it is needed to implement but it is not needed to do anything
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup

        compassViewFloat = floatView.findViewById(R.id.compass_view_float)

        floatWindowLayoutParams = WindowManager.LayoutParams(
            400,
            400,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, //TODO: add option for versions older than Oreo
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this window won't ever get key input focus
            PixelFormat.TRANSLUCENT
        )

        //The default gravity is UNSPECIFIED_GRAVITY, which is treated by FrameLayout as Gravity.TOP | Gravity.START
        //The gravity TOP fix the X value and can't be changed. The same happens with gravity START, it fix the Y value and can't be changed.
        //TODO: Why the floating window has an X offset?
        floatWindowLayoutParams.gravity = Gravity.CENTER
        //This modifies the Y coordinate but something weird happen when trying to drag
        //floatWindowLayoutParams.y = 1247

        //Compass
        compassLogic = CompassLogic(
            getSystemService(Context.SENSOR_SERVICE) as SensorManager,
            compassViewFloat
        )

        windowManager.addView(floatView, floatWindowLayoutParams)

        //Close floating window. The active area is all the window not just the circle
        compassViewFloat.setOnClickListener {
            stopSelf()
            windowManager.removeView(floatView)
            val back = Intent(this, MainActivity::class.java)
            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(back)
        }

        val updateFloatingWindowLayoutParams = floatWindowLayoutParams

        var enableMovingFloatingCompass = false

        //when long touch performed vibrates and enables moving floating compass
        compassViewFloat.setOnLongClickListener() {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
            enableMovingFloatingCompass = true
            true
        }

        //dragging and dropping floating compass
        compassViewFloat.setOnTouchListener(object : View.OnTouchListener {
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = updateFloatingWindowLayoutParams.x.toDouble()
                        y = updateFloatingWindowLayoutParams.y.toDouble()
                        px = event.rawX.toDouble()
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> if (enableMovingFloatingCompass) {
                        updateFloatingWindowLayoutParams.x = (x + event.rawX - px).toInt()
                        updateFloatingWindowLayoutParams.y = (y + event.rawY - py).toInt()
                        windowManager.updateViewLayout(floatView, updateFloatingWindowLayoutParams)
                    }
                    MotionEvent.ACTION_UP -> {
                        enableMovingFloatingCompass = false
                    }
                }
                return false
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        compassLogic.activityResume()
        carryOverRotation = intent?.extras?.getInt("lastRotation")
        //TODO: The angle still has to be adjusted!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        compassViewFloat.rotation = -carryOverRotation!!.toFloat() //TODO: No way to do it except with !! :(

        //Cambia la posición de la brújula. No funciona
        val testFloatingWindowLayoutParams = floatWindowLayoutParams
        testFloatingWindowLayoutParams.x = 20
        windowManager.updateViewLayout(floatView, testFloatingWindowLayoutParams)
        Log.v("compasslog", "i was here")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        compassLogic.activityPause()
        windowManager.removeViewImmediate(floatView)
    }

}
