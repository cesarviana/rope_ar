package com.rope.ropelandia.config

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.rope.ropelandia.R
import com.rope.ropelandia.capture.ImageQuality

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val imageQualityKey = getString(R.string.image_quality_key)

        val sharedPreferences =
            getSharedPreferences(applicationContext.packageName, Context.MODE_PRIVATE)

        val imageQualityPrefString = sharedPreferences.getString(imageQualityKey, ImageQuality.MEDIUM.name)
        val imageQualityPref = ImageQuality.valueOf(imageQualityPrefString!!)

        activeRadioButton(imageQualityPref)

        findViewById<RadioGroup>(R.id.radioGroup).
            setOnCheckedChangeListener(storeCheckedPreference(sharedPreferences, imageQualityKey))

    }

    private fun activeRadioButton(imageQuality: ImageQuality) {
        val radioButtonId = when (imageQuality) {
            ImageQuality.HIGH -> R.id.radioHigh
            ImageQuality.MEDIUM -> R.id.radioMedium
            ImageQuality.LOW -> R.id.radioLow
        }
        findViewById<RadioButton>(radioButtonId).isChecked = true
    }

    private fun storeCheckedPreference(
        sharedPreferences: SharedPreferences,
        imageQualityKey: String
    ): (group: RadioGroup, checkedId: Int) -> Unit = { _, checkedId ->
        val imageQuality = when (checkedId) {
            R.id.radioHigh -> ImageQuality.HIGH
            R.id.radioMedium -> ImageQuality.MEDIUM
            R.id.radioLow -> ImageQuality.LOW
            else -> ImageQuality.MEDIUM
        }
        sharedPreferences.apply {
            edit {
                putString(imageQualityKey, imageQuality.name)
            }
        }
    }
}