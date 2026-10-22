package dev.android.appfacturador

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Window
import dev.android.appfacturador.databinding.ActivityFilterBillBinding

class FilterBillActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBillBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBillBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val window = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(window)
        val width = window.widthPixels
        val height = window.heightPixels

        getWindow().setLayout(((width * 0.85).toInt()), ((height * 0.2).toInt()))
        getWindow().decorView.setBackgroundResource(android.R.color.transparent)

        binding.txtNumber.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("filter", "number")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.txtID.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("filter", "id")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.txtDate.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("filter", "date")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        darkMode()
    }

    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnContainer.setCardBackgroundColor(Color.parseColor("#353536"))
            binding.txtNumber.setTextColor(Color.parseColor("#ffffff"))
            binding.txtID.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDate.setTextColor(Color.parseColor("#ffffff"))
        }
    }
}