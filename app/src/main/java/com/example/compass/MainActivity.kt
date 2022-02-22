package com.example.compass

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.example.compass.databinding.ActivityMainBinding
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToInt

//TODO: Qué ha pasado que la brújula no marcaba el norte cuando esta descalibrada?. Pasa también con las otras apps

class MainActivity : AppCompatActivity() {

    private lateinit var alertDialog: AlertDialog
    private lateinit var compassLogic: CompassLogic
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        //COMPASS
        compassLogic = CompassLogic (
            getSystemService(Context.SENSOR_SERVICE) as SensorManager,
            binding.compassView,
            binding.textView,
            binding.textViewAccuracy,
            binding.constraintLayoutView
        )

        setContentView(binding.root)

        //Si hay un servicio lo para, si no hay no para nada y continua
        stopService(Intent(this, FloatingWindow::class.java))

        //Click on FLOAT button
        findViewById<Button>(R.id.button).setOnClickListener {
            if (checkOverlayPermission()) {
                val intent: Intent = Intent(this, FloatingWindow::class.java)
                intent.putExtra("lastRotation", compassLogic.angle)
                startService(intent)
                finish()
            } else { requestFloatingWindowPermission() }
        }

        //POPUP WINDOW
        findViewById<Button>(R.id.button_calibrate).setOnClickListener {
            val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: ViewGroup = inflater.inflate(R.layout.popup_window, null) as ViewGroup
            val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAtLocation(binding.constraintLayoutView, Gravity.CENTER, 0, 0)
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
        //TODO: Rotate compass before going from float to main
    }

    private fun requestFloatingWindowPermission() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Screen Overlay Permission Needed")
        alertDialogBuilder.setMessage("Enable 'Display over the App' from settings")
        alertDialogBuilder.setPositiveButton(
            "Open Settings"
        ) { _, _ ->
            //manda un intent para abrir las settings
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, //TODO: Fix android version
                Uri.parse("package:$packageName") //TODO: No abre la aplicación en concreto, solo la lista
            )
            startActivity(intent)
        }
        alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    //COMPASS LOCATION
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        val viewLocation = intArrayOf(2,3)
        binding.textView.getLocationOnScreen(viewLocation)
        Log.v("compasslog","compass loc:${viewLocation[0]}, ${viewLocation[1]}")
    }

    //COMPASS
    override fun onResume() {
        super.onResume()
        compassLogic.activityResume()
    }

    //COMPASS
    override fun onPause() {
        super.onPause()
        compassLogic.activityPause()
    }

}