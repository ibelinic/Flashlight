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
import android.os.Handler


class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var toggleButton: Button
    private var isFlashlightOn = false
    private lateinit var brightnessSeekBar: SeekBar
    private var animation: ObjectAnimator? = null
    private lateinit var whiteButton: Button
    private lateinit var redButton: Button
    private lateinit var greenButton: Button
    private lateinit var blueButton: Button
    private var isStrobeModeOn = false
    private lateinit var strobeButton: Button
    private lateinit var stopStrobeButton: Button
    private var strobeHandler: Handler? = null
    private val STROBE_DELAY = 100

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
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar)

        val rootView = findViewById<View>(android.R.id.content)
        rootView.setBackgroundColor(getColorForFlashlightStatus(isFlashlightOn))

        toggleButton.setOnClickListener {
            if (isFlashlightOn) {
                turnOffFlashlight()
            } else {
                turnOnFlashlight()
            }
        }
        toggleButton.setBackgroundColor(getButtonColorForFlashlightStatus(isFlashlightOn))

        whiteButton = findViewById(R.id.whiteButton)
        redButton = findViewById(R.id.redButton)
        greenButton = findViewById(R.id.greenButton)
        blueButton = findViewById(R.id.blueButton)

        whiteButton.setBackgroundColor(getColor(R.color.yellow))
        redButton.setBackgroundColor(getColor(R.color.yellow))
        greenButton.setBackgroundColor(getColor(R.color.yellow))
        blueButton.setBackgroundColor(getColor(R.color.yellow))

        stopStrobeButton = findViewById(R.id.stopStrobeButton)
        stopStrobeButton.visibility = View.GONE
        stopStrobeButton.setOnClickListener {
            disableStrobeMode()
        }

        strobeButton = findViewById<Button>(R.id.strobeButton)

        whiteButton.setOnClickListener {
            changeFlashlightColor(getColor(R.color.white))
        }

        redButton.setOnClickListener {
            changeFlashlightColor(getColor(R.color.red))
        }

        greenButton.setOnClickListener {
            changeFlashlightColor(getColor(R.color.green))
        }

        blueButton.setOnClickListener {
            changeFlashlightColor(getColor(R.color.blue))
        }

        strobeButton = findViewById<Button>(R.id.strobeButton)
        strobeButton.setOnClickListener {
            if (isStrobeModeOn) {
                disableStrobeMode()
            } else {
                enableStrobeMode()
            }
        }
    }

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
            brightnessSeekBar.visibility = View.VISIBLE
            val rootView = findViewById<View>(android.R.id.content)
            rootView.setBackgroundColor(getColorForFlashlightStatus(isFlashlightOn))
            toggleButton.setBackgroundColor(getButtonColorForFlashlightStatus(isFlashlightOn))
            animation = ObjectAnimator.ofPropertyValuesHolder(
                toggleButton,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f, 1f)
            )
            animation?.duration = 100
            animation?.repeatCount = ObjectAnimator.INFINITE
            animation?.repeatMode = ObjectAnimator.REVERSE
            animation?.start()
            whiteButton.visibility = View.VISIBLE
            redButton.visibility = View.VISIBLE
            greenButton.visibility = View.VISIBLE
            blueButton.visibility = View.VISIBLE

            strobeButton.visibility = View.VISIBLE
            strobeButton.setOnClickListener {
                if (isStrobeModeOn) {
                    disableStrobeMode()
                } else {
                    enableStrobeMode()
                    whiteButton.visibility = View.GONE
                    redButton.visibility = View.GONE
                    greenButton.visibility = View.GONE
                    blueButton.visibility = View.GONE
                }
            }

            Toast.makeText(this@MainActivity, "Flashlight is turned ON", Toast.LENGTH_SHORT).show()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

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
            brightnessSeekBar.visibility = View.GONE
            val rootView = findViewById<View>(android.R.id.content)
            rootView.setBackgroundColor(getColorForFlashlightStatus(isFlashlightOn))
            toggleButton.setBackgroundColor(getButtonColorForFlashlightStatus(isFlashlightOn))
            animation?.cancel()
            animation = null

            whiteButton.visibility = View.GONE
            redButton.visibility = View.GONE
            greenButton.visibility = View.GONE
            blueButton.visibility = View.GONE

            strobeButton.visibility = View.GONE

            Toast.makeText(this@MainActivity, "Flashlight is turned OFF", Toast.LENGTH_SHORT).show()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun getColorForFlashlightStatus(isFlashlightOn: Boolean): Int {
        return if (isFlashlightOn) {
            getColor(R.color.white)
        } else {
            getColor(R.color.black)
        }
    }

    private fun getButtonColorForFlashlightStatus(isFlashlightOn: Boolean): Int {
        return if (isFlashlightOn) {
            getColor(R.color.baby_blue)
        } else {
            getColor(R.color.yellow)
        }
    }

    private fun changeFlashlightColor(color: Int) {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.setBackgroundColor(color)
    }

    override fun onStop() {
        super.onStop()

        if (isFlashlightOn) {
            turnOffFlashlight()
        }
    }

    private fun toggleFlashlight() {
        if (isFlashlightOn) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }

    private fun enableStrobeMode() {
        isStrobeModeOn = true
        strobeButton.visibility = View.GONE
        stopStrobeButton.visibility = View.VISIBLE
        startStrobeMode()
    }

    private fun disableStrobeMode() {
        isStrobeModeOn = false
        strobeButton.visibility = View.VISIBLE
        stopStrobeButton.visibility = View.GONE
        stopStrobeMode()
    }

    private fun startStrobeMode() {
        strobeHandler = Handler()
        strobeHandler?.postDelayed({
            if (isStrobeModeOn) {
                toggleFlashlight()
                startStrobeMode()
            }
        }, STROBE_DELAY.toLong())
    }

    private fun stopStrobeMode() {
        strobeHandler?.removeCallbacksAndMessages(null)
        strobeHandler = null
        Toast.makeText(this@MainActivity, "Strobe mode stopped", Toast.LENGTH_SHORT).show()
    }

}

