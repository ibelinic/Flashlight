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
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.SwitchCompat
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var toggleButton: Button
    private var isFlashlightOn = false
    private var animation: ObjectAnimator? = null
    private var isStrobeModeOn = false
    private lateinit var strobeButton: Button
    private lateinit var holdingButton: Button
    private lateinit var themeSwitcher: SwitchCompat
    private var strobeHandler: Handler? = null
    private lateinit var symbolImageView: ImageView
    private var strobeSpeed: Int = 0
    private lateinit var warningButton: Button
    private lateinit var brightnessSeekBar: SeekBar
    private var isWarningModeOn = false
    private var warningHandler: Handler? = null
    private var warningPattern: LongArray =
        longArrayOf(500, 500, 500, 500, 1000, 1000, 1000) // Podešavanje uzorka titranja

    private lateinit var languageSpinner: Spinner
    // Definiranje globalne varijable za spremanje trenutne teme
    private var currentThemeMode: Int = AppCompatDelegate.MODE_NIGHT_NO

    private lateinit var sharedPreferences : SharedPreferences

    private lateinit var constraintLayout : ConstraintLayout

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Definiranje elemenata
        toggleButton = findViewById(R.id.toggleButton)
        themeSwitcher = findViewById(R.id.themeSwitcher)
        holdingButton = findViewById(R.id.holdingButton)
        strobeButton = findViewById(R.id.strobeButton)
        warningButton = findViewById(R.id.warningButton)
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar)
        constraintLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)
        languageSpinner = findViewById(R.id.languageSpinner)
        symbolImageView = findViewById(R.id.symbolImageView)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val switcherState = sharedPreferences.getBoolean("themeSwitcherState", false)
        themeSwitcher.isChecked = switcherState

        //Promjena boje adaptera za spinner
        val textColors = intArrayOf(
            android.R.color.black,  // Boja teksta za prvi item
            android.R.color.black,  // Boja teksta za drugi item
            android.R.color.black   // Boja teksta za treći item
        )
        val adapter = CustomSpinnerAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.languages),
            textColors
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        //Dohvacanje prethodno odabranog jezika i postavljanje istog u spinner
        val selectedLanguage = sharedPreferences.getString("selectedLanguage", null)
        if (selectedLanguage != null) {
            val selectedIndex = adapter.getPosition(selectedLanguage)
            languageSpinner.setSelection(selectedIndex)
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLanguage = parent?.getItemAtPosition(position).toString()
                sharedPreferences.edit().putString("selectedLanguage", selectedLanguage).apply()
                setAppLanguage(selectedLanguage)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nista nije odabrano
            }
        }

        // Promjena teme aplikacije
        // Aktiviranje promjene teme klikom na prekidač
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            val newThemeMode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            sharedPreferences.edit().putBoolean("themeSwitcherState", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(newThemeMode)

            // Promjena boje pozadine, gumba i teksta
                val backgroundRes = when (newThemeMode) {
                    AppCompatDelegate.MODE_NIGHT_YES -> {
                        themeSwitcher.text = getString(R.string.text_dark_mode)
                        R.color.dark_background
                    }
                    else -> {
                        themeSwitcher.text = getString(R.string.text_light_mode)
                        R.color.light_background
                    }
                }
                constraintLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundRes))

                val buttonColor = when (newThemeMode) {
                    AppCompatDelegate.MODE_NIGHT_YES -> R.color.red
                    else -> R.color.baby_blue
                }
                val buttonBackground = ContextCompat.getColorStateList(this, buttonColor)
                themeSwitcher.thumbTintList = buttonBackground
                strobeButton.backgroundTintList = buttonBackground
                warningButton.backgroundTintList = buttonBackground
                holdingButton.backgroundTintList = buttonBackground

                val textColor = when (newThemeMode) {
                    AppCompatDelegate.MODE_NIGHT_YES -> android.R.color.white
                    else -> android.R.color.black
                }
                themeSwitcher.setTextColor(ContextCompat.getColor(this, textColor))
                strobeButton.setTextColor(ContextCompat.getColor(this, textColor))
                warningButton.setTextColor(ContextCompat.getColor(this, textColor))
                holdingButton.setTextColor(ContextCompat.getColor(this, textColor))

            // Promjena boje simbola
            val symbolColor = when (newThemeMode) {
                AppCompatDelegate.MODE_NIGHT_YES -> R.color.red
                else -> R.color.baby_blue
            }
            symbolImageView.setColorFilter(ContextCompat.getColor(this, symbolColor))

            // Promjena boje `SeekBar`-a
            val seekBarColor = when (newThemeMode) {
                AppCompatDelegate.MODE_NIGHT_YES -> R.color.seekbar_color2
                else -> R.color.someElementColor
            }
            brightnessSeekBar.progressTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, seekBarColor))

            // Promjena boje thumba na SeekBar-u
            val thumbColor = when (newThemeMode) {
                AppCompatDelegate.MODE_NIGHT_YES -> R.color.seekbar_color2
                else -> R.color.someElementColor
            }
            brightnessSeekBar.thumbTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, thumbColor))
        }
        onResume()

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

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
        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
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
        holdingButton.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> turnOnFlashlight() // Uključivanje bljeskalice kada se gumb drži
                MotionEvent.ACTION_UP -> turnOffFlashlight() // Gašenje bljeskalice kada se gumb otpusti
            }
            true
        }

        //Bljeskalica u načinu bljeskanja za upozorenje
        warningButton = findViewById(R.id.warningButton)
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
            symbolImageView = findViewById(R.id.symbolImageView)
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
                Toast.makeText(this@MainActivity, "Flashlight is turned ON", Toast.LENGTH_SHORT)
                    .show()
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
            symbolImageView = findViewById(R.id.symbolImageView)
            symbolImageView.clearAnimation()

            //Poruka kako je bljeskalica isključena (samo ako nije u strobe i warning modu)
            if (isStrobeModeOn && isWarningModeOn) {
                Toast.makeText(
                    this@MainActivity,
                    "Flashlight is turned OFF",
                    Toast.LENGTH_SHORT
                ).show()
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
                warningPatternIndex =
                    (warningPatternIndex + 1) % warningPattern.size // Inkrementiranje indeksa i omogućavanje cikličnog korištenja uzorka
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

    private fun setAppLanguage(language: String) {
        // Spremanje odabranog jezika
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("selectedLanguage", language).apply()

        val locale = when (language) {
            "English" -> Locale("en")
            "Deutsch" -> Locale("de")
            "Hrvatski" -> Locale("hr")
            else -> Locale.getDefault()
        }
        Locale.setDefault(locale)

        // Definiranje elemenata
        toggleButton = findViewById(R.id.toggleButton)
        themeSwitcher = findViewById(R.id.themeSwitcher)
        holdingButton = findViewById(R.id.holdingButton)
        strobeButton = findViewById(R.id.strobeButton)
        warningButton = findViewById(R.id.warningButton)

        val resources = resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Ažurirajte tekstove i resurse na sučelju
        // Ovdje možete postaviti nove vrijednosti za tekstove ili osvježiti prikaz

        toggleButton.text = getString(R.string.turn_on)
        strobeButton.text = getString(R.string.pulse_button)
        holdingButton.text = getString(R.string.continuous_light)
        if (themeSwitcher.isChecked) {
            themeSwitcher.text = getString(R.string.text_dark_mode)
        } else {
            themeSwitcher.text = getString(R.string.text_light_mode)
        }
        warningButton.text = getString(R.string.warning_button)

    }

    override fun onResume() {
        super.onResume()

        constraintLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)

        val switcherState = sharedPreferences.getBoolean("themeSwitcherState", false)
        themeSwitcher.isChecked = switcherState

        val currentThemeMode = if (switcherState) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(currentThemeMode)

        // Promjena boje pozadine, gumba i teksta
        val backgroundRes = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> {
                themeSwitcher.text = getString(R.string.text_dark_mode)
                R.color.dark_background
            }
            else -> {
                themeSwitcher.text = getString(R.string.text_light_mode)
                R.color.light_background
            }
        }
        constraintLayout.setBackgroundColor(ContextCompat.getColor(this, backgroundRes))

        val buttonColor = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> R.color.red
            else -> R.color.baby_blue
        }
        val buttonBackground = ContextCompat.getColorStateList(this, buttonColor)
        themeSwitcher.thumbTintList = buttonBackground
        strobeButton.backgroundTintList = buttonBackground
        themeSwitcher.thumbTintList = buttonBackground
        warningButton.backgroundTintList = buttonBackground
        holdingButton.backgroundTintList = buttonBackground

        val textColor = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> android.R.color.white
            else -> android.R.color.black
        }
        themeSwitcher.setTextColor(ContextCompat.getColor(this, textColor))
        strobeButton.setTextColor(ContextCompat.getColor(this, textColor))
        warningButton.setTextColor(ContextCompat.getColor(this, textColor))
        holdingButton.setTextColor(ContextCompat.getColor(this, textColor))

        // Promjena boje simbola
        val symbolColor = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> R.color.red
            else -> R.color.baby_blue
        }
        symbolImageView.setColorFilter(ContextCompat.getColor(this, symbolColor))

        // Promjena boje `SeekBar`-a
        val seekBarColor = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> R.color.seekbar_color2
            else -> R.color.someElementColor
        }
        brightnessSeekBar.progressTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, seekBarColor))

        // Promjena boje thumba na SeekBar-u
        val thumbColor = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> R.color.seekbar_color2
            else -> R.color.someElementColor
        }
        brightnessSeekBar.thumbTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, thumbColor))
    }

}


