package com.example.flashlight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.view.View
import android.widget.Button
import android.widget.SeekBar


class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var toggleButton: Button
    private var isFlashlightOn = false
    private lateinit var brightnessSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            cameraId =
                cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        toggleButton = findViewById(R.id.toggleButton)
        // Set the initial icon and text
        toggleButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.baseline_flashlight_on_24,
            0,
            0,
            0
        )
        toggleButton.text = getString(R.string.turn_on)
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar)

        toggleButton.setOnClickListener {
            if (isFlashlightOn) {
                turnOffFlashlight()
            } else {
                turnOnFlashlight()
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
            brightnessSeekBar.progress = brightnessSeekBar.max
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
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}