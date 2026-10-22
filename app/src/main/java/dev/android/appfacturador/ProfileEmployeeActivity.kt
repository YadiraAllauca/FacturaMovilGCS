package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.databinding.ActivityProfileEmployeeBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.utils.Constants

class ProfileEmployeeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEmployeeBinding
    private lateinit var employeeData: EMPLEADO

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEmployeeBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        getEmployeeData()
        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "profile")
            }
            startActivity(intent)
        }

        binding.btnInfo.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable(Constants.KEY_EMPLOYEE, employeeData)
            }
            val intent = Intent(this, AddEmployeeActivity::class.java).apply {
                putExtras(bundle)
            }
            startActivity(intent)
        }

        cerrarSesion()
        darkMode()
    }

    private fun getEmployeeData() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        val database = FirebaseDatabase.getInstance()
        val usuariosRef = database.getReference("Empleado")

        usuariosRef.orderByChild("correo_electronico").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val empleado = childSnapshot.getValue(EMPLEADO::class.java)
                            if (empleado != null) {
                                employeeData = EMPLEADO(
                                    empleado.id,
                                    empleado.apellido_materno,
                                    empleado.apellido_paterno,
                                    "******",
                                    empleado.correo_electronico,
                                    empleado.numero_dni,
                                    empleado.primer_nombre,
                                    empleado.segundo_nombre,
                                    empleado.tipo_dni, empleado.tipo_empleado,
                                    empleado.negocio
                                )
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@ProfileEmployeeActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
            binding.btnInfo.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.btnInfo.outlineSpotShadowColor = Color.parseColor("#ffffff")
            binding.txtInfo.setTextColor(Color.parseColor("#ffffff"))
            binding.btnEditInfo.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnClose.setCardBackgroundColor(Color.parseColor("#47484a"))
            binding.btnCloses.setColorFilter(Color.parseColor("#121212"))
            binding.btnLogout.setBackgroundResource(R.drawable.gradientdark)
            binding.btnLogout.setTextColor(Color.parseColor("#121212"))
        }
    }

    override fun onBackPressed() {
    }
}