package dev.android.appfacturador

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.android.appfacturador.databinding.ActivityEditInfoBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.NEGOCIO
import dev.android.appfacturador.utils.Constants

class EditInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditInfoBinding
    private lateinit var email: String
    private lateinit var shop: String
    private lateinit var spinner: Spinner
    private val instanceFirebase = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()

        spinner = binding.spnDNI
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, Constants.TYPE_DNI)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(1)
        spinner.isEnabled = false

        getShop()
        setupActions()
        darkMode()
    }

    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnBack.setColorFilter(Color.parseColor("#ffffff"))
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.txtName.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDNI.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDNINumber.setTextColor(Color.parseColor("#ffffff"))
            binding.txtEmail.setTextColor(Color.parseColor("#ffffff"))
            binding.txtPhone.setTextColor(Color.parseColor("#ffffff"))
            binding.txtAddress.setTextColor(Color.parseColor("#ffffff"))
            binding.edtName.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.edtNumDNI.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.edtEmail.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.edtPhone.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.edtAddress.setBackgroundResource(dev.android.appfacturador.R.drawable.text_info_dark)
            binding.btnAdd.setBackgroundResource(dev.android.appfacturador.R.drawable.gradientdark)
            binding.btnAdd.setTextColor(Color.parseColor("#121212"))
        }
    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        val usuariosRef = instanceFirebase.getReference("Empleado")

        usuariosRef.orderByChild("correo_electronico").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val empleado = childSnapshot.getValue(EMPLEADO::class.java)
                            if (empleado != null) {
                                shop = empleado.negocio
                                getShopData()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@EditInfoActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun setupActions() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAdd.setOnClickListener {
            finish()
        }
    }

    private fun getShopData() {
        val database = FirebaseDatabase.getInstance()
        val tiendaRef = database.getReference("Negocios/${shop}")

        tiendaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val tienda = dataSnapshot.getValue(NEGOCIO::class.java)
                    if (tienda != null) {
                        binding.edtName.setText(tienda.nombre)
                        binding.edtNumDNI.setText(tienda.ruc)
                        binding.edtEmail.setText(tienda.email)
                        binding.edtAddress.setText(tienda.direccion)
                        binding.edtPhone.setText(tienda.celular)
                    }
                } else {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@EditInfoActivity,
                    "Error en la solicitud: " + databaseError.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}