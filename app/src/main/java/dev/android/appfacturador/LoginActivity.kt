package dev.android.appfacturador

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        checkSession()
        hiddeVisiblePassword("light")
        db = FirebaseDatabase.getInstance()
        login()
        session()
        darkMode()
    }

    private fun checkSession() {
        val loadingMessage = "Verificando sesión..."
        val progressDialog = ProgressDialog.show(this, "", loadingMessage, true)
        Handler().postDelayed({
            progressDialog.dismiss()
        }, 1000)
    }

    private fun hiddeVisiblePassword(mode: String) {
        var passwordHidden = false
        binding.btnHide.setOnClickListener {
            if (passwordHidden) {
                binding.edtPaassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                passwordHidden = false
                binding.btnHide.setColorFilter(ContextCompat.getColor(this, R.color.gray))
            } else {
                binding.edtPaassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                passwordHidden = true
                if (mode == "light") {
                    binding.btnHide.setColorFilter(ContextCompat.getColor(this, R.color.blues))
                } else {
                    binding.btnHide.setColorFilter(Color.parseColor("#ffffff"))
                }
            }
        }
    }

    private fun login() {
        binding.btnNext.setOnClickListener {
            if (binding.edtEmail.text.isNotEmpty() && binding.edtPaassword.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.edtEmail.text.toString(),

                    binding.edtPaassword.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val email = binding.edtEmail.text.toString()
                        showNewActivity(email ?: "")
                    } else {
                        showAlert()
                    }
                }
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Ingrese correctamente su usuario y contraseña")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showNewActivity(email: String) {
        val usuariosRef = db.getReference("Empleado")

        usuariosRef.orderByChild("correo_electronico").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val preferences: SharedPreferences.Editor =
                            getSharedPreferences(
                                "PREFERENCE_FILE_KEY",
                                Context.MODE_PRIVATE
                            ).edit()
                        preferences.putString("email", email)
                        preferences.apply()

                        val intent = Intent(this@LoginActivity, ProductActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Usuario no encontrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun session() {
        val preferences: SharedPreferences =
            getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        val email: String? = preferences.getString("email", null)
        if (email != null) {
            showNewActivity(email)
        }
    }

    @SuppressLint("ResourceAsColor")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            hiddeVisiblePassword("dark")
            // El modo actual es dark
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.txtLogin.setTextColor(Color.parseColor("#ffffff"))
            val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.accessdarkcolor)
            binding.imageView2.setImageDrawable(drawable)
            binding.edtEmail.setBackgroundResource(R.drawable.textdark)
            binding.edtEmail.setTextColor(Color.parseColor("#ffffff"))
            binding.edtPaassword.setBackgroundResource(R.drawable.textdark)
            binding.edtPaassword.setTextColor(Color.parseColor("#ffffff"))
            binding.btnNext.setBackgroundResource(R.drawable.gradientdark)
            binding.btnNext.setTextColor(Color.parseColor("#121212"))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}