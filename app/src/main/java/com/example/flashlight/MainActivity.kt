package com.example.flashlight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.os.Handler
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView


class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var toggleButton: Button
    private var isFlashlightOn = false
    private var animation: ObjectAnimator? = null
    private var isStrobeModeOn = false
    private lateinit var strobeButton: Button
    private var strobeHandler: Handler? = null
    private lateinit var symbolImageView: ImageView
    private var strobeSpeed: Int = 0
    private lateinit var warningButton: Button
    private var isWarningModeOn = false
    private var warningHandler: Handler? = null
    private var warningPattern: LongArray = longArrayOf(500, 500, 500, 500, 1000, 1000, 1000) // Podešavanje uzorka titranja

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        toggleButton = findViewById(R.id.toggleButton)
        toggleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.baseline_flashlight_on_24,
            0,
            0,
            0
        )
        toggleButton.text = getString(R.string.turn_on)

        //Uključivanje/Isključivanje bljeskalice klikom na gumb
        toggleButton.setOnClickListener {
            if (isFlashlightOn) {
                turnOffFlashlight()
            } else {
                turnOnFlashlight()
            }
        }

        //Aktiviranje titranja bljeskalice klikom na gumb
        strobeButton = findViewById<Button>(R.id.strobeButton)
        strobeButton.setOnClickListener {
            if (isStrobeModeOn) {
                isStrobeModeOn = false
                stopStrobeMode()
            } else {
                isStrobeModeOn = true
                startStrobeMode()
            }
        }

        //Titranje bljeskalice ovisno o vrijednosti u seekbar-u
        val brightnessSeekBar = findViewById<SeekBar>(R.id.brightnessSeekBar)
        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                strobeSpeed = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Nema potrebe za implementacijom ovih metoda
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Nema potrebe za implementacijom ovih metoda
            }
        })

        //Bljeskalica uključena samo kada se drži gumb, a inače je ugašena
        val holdingButton = findViewById<Button>(R.id.holdingButton)
        holdingButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> turnOnFlashlight() // Uključivanje bljeskalice kada se gumb drži
                MotionEvent.ACTION_UP -> turnOffFlashlight() // Gašenje bljeskalice kada se gumb otpusti
            }
            true
        }

        //Bljeskalica u načinu bljeskanja za upozorenje
        warningButton = findViewById<Button>(R.id.warningButton)
        warningButton.setOnClickListener {
            if (isWarningModeOn) {
                isWarningModeOn = false
                stopWarningMode()
            } else {
                isWarningModeOn = true
                startWarningMode()
            }
        }

    }

    //Uključivanje bljeskalice
    private fun turnOnFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId!!, true)
            isFlashlightOn = true
            toggleButton.text = getString(R.string.turn_off)
            toggleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.baseline_flashlight_off_24,
                0,
                0,
                0
            )
            animation = ObjectAnimator.ofPropertyValuesHolder(
                toggleButton,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f, 1f)
            )
            animation?.duration = 100
            animation?.repeatCount = ObjectAnimator.INFINITE
            animation?.repeatMode = ObjectAnimator.REVERSE
            animation?.start()

            //Simbol koji predstavlja uključenu bljeskalicu (rotacija simbola) ili isključenu bljeskalicu (statičan simbol)
            symbolImageView = findViewById<ImageView>(R.id.symbolImageView)
            //Kreiranje animacije rotacije
            val rotateAnimation = RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotateAnimation.duration = 1000 //Postavljanje trajanja animacije
            rotateAnimation.repeatCount = Animation.INFINITE //Ponavljanje animacije beskonačno
            //Pokretanje animacije
            symbolImageView.startAnimation(rotateAnimation)

            //Poruka kako je bljeskalica uključena (samo ako nije u strobe i warning modu)
            if (isStrobeModeOn && isWarningModeOn) {
                Toast.makeText(this@MainActivity, "Flashlight is turned ON", Toast.LENGTH_SHORT).show()
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    //Isključivanje bljeskalice
    private fun turnOffFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId!!, false)
            isFlashlightOn = false
            toggleButton.text = getString(R.string.turn_on)
            toggleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.baseline_flashlight_on_24,
                0,
                0,
                0
            )
            animation?.cancel()
            animation = null

            //Prekidanje rotacije simbola
            symbolImageView = findViewById<ImageView>(R.id.symbolImageView)
            symbolImageView.clearAnimation()

            //Poruka kako je bljeskalica isključena (samo ako nije u strobe i warning modu)
            if (isStrobeModeOn && isWarningModeOn) {
                Toast.makeText(this@MainActivity, "Flashlight is turned OFF", Toast.LENGTH_SHORT).show()
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    //Isključivanje bljeskalice izlaskom iz aplikacije
    override fun onStop() {
        super.onStop()
        if (isFlashlightOn) {
            turnOffFlashlight()
        }
    }

    //Uključivanje/Isključivanje bljeskalice pomoću jednog gumba
    private fun toggleFlashlight() {
        if (isFlashlightOn) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }

    //Početak titranja bljeskalice
    private fun startStrobeMode() {
        strobeHandler = Handler()
        strobeHandler?.postDelayed({
            if (isStrobeModeOn) {
                toggleFlashlight()
                startStrobeMode()
            }
        }, strobeSpeed.toLong())
    }

    //Prekidanje titranja bljeskalice
    private fun stopStrobeMode() {
        strobeHandler?.removeCallbacksAndMessages(null)
        strobeHandler = null
        Toast.makeText(this@MainActivity, "Strobe mode stopped", Toast.LENGTH_SHORT).show()
    }

    //Početak načina upozorenja bljeskalice
    private var warningPatternIndex = 0 // Dodan indeks za praćenje trenutne odgode iz uzorka
    //Početak načina upozorenja bljeskalice
    private fun startWarningMode() {
        warningHandler = Handler()
        warningHandler?.postDelayed({
            if (isWarningModeOn) {
                toggleFlashlight()
                val delay = warningPattern[warningPatternIndex]
                warningPatternIndex = (warningPatternIndex + 1) % warningPattern.size // Inkrementiranje indeksa i omogućavanje cikličnog korištenja uzorka
                warningHandler?.postDelayed({
                    startWarningMode()
                }, delay)
            }
        }, 0)
    }

    //Prekidanje načina upozorenja bljeskalice
    private fun stopWarningMode() {
        warningHandler?.removeCallbacksAndMessages(null)
        warningHandler = null
        turnOffFlashlight()
    }


}

