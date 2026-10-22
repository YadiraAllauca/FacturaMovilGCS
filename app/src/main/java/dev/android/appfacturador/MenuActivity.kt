package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.android.appfacturador.databinding.ActivityMenuBinding
import dev.android.appfacturador.model.EMPLEADO

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private var isSeller = false
    private val instanceFirebase = Firebase.database

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        checkTypeEmployee()
        darkMode()

        binding.btnArrow.setOnClickListener {
            finish()
        }

        val bundle = intent.extras
        val option = bundle?.getString("option")

        if (option == "profile") {
            binding.btnProfile.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blues))
            binding.btnProfiles.setColorFilter(ContextCompat.getColor(this, R.color.white))
            darkModeOptions(binding.btnProfile, binding.btnProfiles)
        } else if (option == "bill") {
            binding.btnBill.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blues))
            binding.btnBills.setColorFilter(ContextCompat.getColor(this, R.color.white))
            darkModeOptions(binding.btnBill, binding.btnBills)
        } else if (option == "client") {
            binding.btnClient.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blues))
            binding.btnClients.setColorFilter(ContextCompat.getColor(this, R.color.white))
            darkModeOptions(binding.btnClient, binding.btnClients)
        } else if (option == "product") {
            binding.btnProduct.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blues))
            binding.btnProducts.setColorFilter(ContextCompat.getColor(this, R.color.white))
            darkModeOptions(binding.btnProduct, binding.btnProducts)
        }

        binding.btnProducts.setOnClickListener {
            val intent = Intent(this, ProductActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnClients.setOnClickListener {
            val intent = Intent(this, ClientActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnBills.setOnClickListener {
            val intent = Intent(this, BillActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnProfiles.setOnClickListener {
            val intent: Intent
            if (isSeller) {
                intent = Intent(this, ProfileEmployeeActivity::class.java)
            } else {
                intent = Intent(this, ProfileActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        val window = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(window)

        getWindow().setLayout(370, 950)
        getWindow().decorView.setBackgroundResource(android.R.color.transparent)
        getWindow().setGravity(Gravity.START)
    }

    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnContainer.setCardBackgroundColor(Color.parseColor("#353536"))
            binding.btnClose.setCardBackgroundColor(Color.parseColor("#353536"))
            binding.btnArrow.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnProducts.setColorFilter(Color.parseColor("#121212"))
            binding.btnProduct.setCardBackgroundColor(Color.parseColor("#5c5d5e"))
            binding.btnClients.setColorFilter(Color.parseColor("#121212"))
            binding.btnClient.setCardBackgroundColor(Color.parseColor("#5c5d5e"))
            binding.btnBills.setColorFilter(Color.parseColor("#121212"))
            binding.btnBill.setCardBackgroundColor(Color.parseColor("#5c5d5e"))
            binding.btnProfiles.setColorFilter(Color.parseColor("#121212"))
            binding.btnProfile.setCardBackgroundColor(Color.parseColor("#5c5d5e"))
        }
    }

    fun darkModeOptions(card: CardView, icon: ImageView) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            card.setCardBackgroundColor(Color.parseColor("#121212"))
            icon.setColorFilter(Color.parseColor("#ffffff"))
        }
    }

    private fun checkTypeEmployee() {
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
                                if (empleado.tipo_empleado == "V") {
                                    isSeller = true
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@MenuActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

}