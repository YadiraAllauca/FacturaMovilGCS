package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import dev.android.appfacturador.databinding.ActivityProfileBinding


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val loadImage =
            registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                binding.imgProfile.setImageURI(it)
            })

        binding.btnCamera.setOnClickListener {
            loadImage.launch("image/*")
        }

        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "profile")
            }
            startActivity(intent)
        }

        binding.btnAddEmployees.setOnClickListener {
            val intent = Intent(this, EmployeeActivity::class.java)
            startActivity(intent)
        }

        binding.btnInfo.setOnClickListener {
            val intent = Intent(this, EditInfoActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddSign.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }

        cerrarSesion()
        darkMode()
    }

    fun cerrarSesion() {
        binding.btnLogout.setOnClickListener {
            val preferencias: SharedPreferences.Editor =
                getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE).edit()
            preferencias.clear()
            preferencias.apply()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnContainer.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.btnCamera.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnInfo.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.btnInfo.outlineSpotShadowColor = Color.parseColor("#ffffff")
            binding.txtInfo.setTextColor(Color.parseColor("#ffffff"))
            binding.btnEditInfo.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnAddEmployees.setCardBackgroundColor(Color.parseColor("#1e1e1e"))
            binding.btnAddEmployees.outlineSpotShadowColor = Color.parseColor("#ffffff")
            binding.txtAddEmployees.setTextColor(Color.parseColor("#ffffff"))
            binding.btnEmployees.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnExtraInfo.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.btnAddSign.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.btnExtraInfo.outlineSpotShadowColor = Color.parseColor("#ffffff")
            binding.txtSign.setTextColor(Color.parseColor("#ffffff"))
            binding.txtAccounts.setTextColor(Color.parseColor("#ffffff"))
            binding.btnSign.setColorFilter(Color.parseColor("#ffffff"))
            binding.switch1.trackTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
            binding.btnClose.setCardBackgroundColor(Color.parseColor("#47484a"))
            binding.btnCloses.setColorFilter(Color.parseColor("#121212"))
            binding.btnLogout.setBackgroundResource(R.drawable.gradientdark)
            binding.btnLogout.setTextColor(Color.parseColor("#121212"))
        }
    }

    override fun onBackPressed() {
    }

}