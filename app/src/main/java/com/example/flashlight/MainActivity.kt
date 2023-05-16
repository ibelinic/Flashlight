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


class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var toggleButton: Button
    private var isFlashlightOn = false
    private lateinit var brightnessSeekBar: SeekBar
    private var animation: ObjectAnimator? = null // Dodano polje za animaciju

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
            brightnessSeekBar.progress = brightnessSeekBar.max
            val rootView = findViewById<View>(android.R.id.content)
            rootView.setBackgroundColor(getColorForFlashlightStatus(isFlashlightOn))
            toggleButton.setBackgroundColor(getButtonColorForFlashlightStatus(isFlashlightOn))

            // Create the button animation
            animation = ObjectAnimator.ofPropertyValuesHolder(
                toggleButton,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f, 1f)
            )
            animation?.duration = 100
            animation?.repeatCount = ObjectAnimator.INFINITE
            animation?.repeatMode = ObjectAnimator.REVERSE
            animation?.start()

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
                R.drawable.baseline_flashlight_on_24,0,
                0,
                0
            )
            brightnessSeekBar.visibility = View.GONE
            val rootView = findViewById<View>(android.R.id.content)
            rootView.setBackgroundColor(getColorForFlashlightStatus(isFlashlightOn))
            toggleButton.setBackgroundColor(getButtonColorForFlashlightStatus(isFlashlightOn))
            // Cancel the button animation
            animation?.cancel()
            animation = null

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
}