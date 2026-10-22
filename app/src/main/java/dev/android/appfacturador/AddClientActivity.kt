package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.database.ClientDao
import dev.android.appfacturador.databinding.ActivityAddClientBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.utils.Constants
import dev.android.appfacturador.utils.Constants.Companion.KEY_CLIENT
import dev.android.appfacturador.utils.SpeechToTextUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddClientActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddClientBinding
    private lateinit var email: String
    private lateinit var shop: String
    private var id = ""
    private lateinit var spinner: Spinner
    private val REQUEST_CODE_SPEECH_TO_TEXT1 = 1
    private val REQUEST_CODE_SPEECH_TO_TEXT2 = 2
    private val REQUEST_CODE_SPEECH_TO_TEXT3 = 3
    private val REQUEST_CODE_SPEECH_TO_TEXT4 = 4
    private val REQUEST_CODE_SPEECH_TO_TEXT5 = 5
    private val REQUEST_CODE_SPEECH_TO_TEXT6 = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClientBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        initialize()
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        getShop()
        events()
        darkMode()
    }

    private fun initialize() {
        spinner = binding.spnDNI
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.TYPE_DNI)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val bundle = intent.extras
        bundle?.let {
            val client = it.getSerializable(KEY_CLIENT) as CLIENTE
            id = client.id
            binding.txtInitials.text =
                client.primer_nombre.first().toString() + client.apellido_paterno.first().toString()
            binding.btnAdd.text = "ACTUALIZAR"
            binding.edtNameClient.setText(client.primer_nombre + " " + client.segundo_nombre)
            binding.edtLastNameClient.setText(client.apellido_paterno + " " + client.apellido_materno)
            binding.edtNumDNI.setText(client.numero_dni)
            binding.edtEmailClient.setText(client.correo_electronico)
            binding.edtPhoneClient.setText(client.telefono)
            binding.edtAddressClient.setText(client.direccion)
            val clientTypeDNI = client.tipo_dni
            val position = Constants.TYPE_DNI.indexOf(clientTypeDNI)
            spinner.setSelection(position)

        } ?: run {
            binding.btnAdd.text = "AGREGAR"
            binding.edtNameClient.setText("")
            binding.edtLastNameClient.setText("")
            binding.edtNumDNI.setText("")
            binding.edtEmailClient.setText("")
            binding.edtPhoneClient.setText("")
            binding.edtAddressClient.setText("")
        }
        binding.edtNameClient.requestFocus()
    }

    private fun getShop() {
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
                                shop = empleado.negocio
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddClientActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun events() {
        binding.btnAdd.setOnClickListener {
            val typeDNI = spinner.selectedItem.toString()
            val numberDNI = binding.edtNumDNI.text.toString()
            val fullName = (binding.edtNameClient.text.toString()).split(" ")
            var firstName = ""
            var secondName = ""
            if (fullName.size == 2) {
                firstName = fullName[0].toLowerCase().capitalize()
                secondName = fullName[1].toLowerCase().capitalize()
            } else {
                Toast.makeText(this, "Ingrese nombre completo", Toast.LENGTH_SHORT).show()
            }
            val fullLastName = (binding.edtLastNameClient.text.toString()).split(" ")
            var firstLastName = ""
            var secondLastName = ""
            if (fullLastName.size == 2) {
                firstLastName = fullLastName[0].toLowerCase().capitalize()
                secondLastName = fullLastName[1].toLowerCase().capitalize()
            } else {
                Toast.makeText(this, "Ingrese nombre completo", Toast.LENGTH_SHORT).show()
            }
            val email = binding.edtEmailClient.text.toString()
            val phone = binding.edtPhoneClient.text.toString()
            val address = binding.edtAddressClient.text.toString()
            if (fullName.isEmpty() || fullLastName.isEmpty() || numberDNI.isEmpty() ||
                email.isEmpty() || phone.isEmpty() || address.isEmpty()
            ) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            } else {
                var clientData =
                    CLIENTE(
                        id,
                        typeDNI,
                        numberDNI,
                        firstName,
                        secondName,
                        firstLastName,
                        secondLastName,
                        email,
                        phone,
                        address,
                        shop
                    )
                if (clientData.id.isEmpty()) {
                    addClient(clientData)
                    Toast.makeText(this, "¡Cliente agregado exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    updateClient(clientData)
                    Toast.makeText(
                        this,
                        "¡Datos actualizados exitosamente!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                finish()
            }

        }
        eventsMicro()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun eventsMicro() {
        binding.btnMicClientNames.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT1)
        }
        binding.btnMicClientLastNames.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT2)
        }
        binding.btnMicIDNumber.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT3)
        }
        binding.btnMicEmail.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT4)
        }
        binding.btnMicPhoneNumber.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT5)
        }
        binding.btnMicAddres.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT6)
        }
    }

    private fun addClient(cliente: CLIENTE) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(Constants.DATABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClientDao::class.java)
        val retrofit = retrofitBuilder.addClient(cliente)

        retrofit.enqueue(
            object : Callback<CLIENTE> {
                override fun onFailure(call: Call<CLIENTE>, t: Throwable) {
                    Log.d("Agregar", "Error al agregar cliente")
                }

                override fun onResponse(call: Call<CLIENTE>, response: Response<CLIENTE>) {
                    Log.d("Agregar", "Cliente agregado con éxito")
                }
            }
        )
    }

    private fun updateClient(cliente: CLIENTE) {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(Constants.DATABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClientDao::class.java)

        val retrofit = retrofitBuilder.updateClient(cliente.id, cliente)
        retrofit.enqueue(
            object : Callback<CLIENTE> {
                override fun onFailure(call: Call<CLIENTE>, t: Throwable) {
                    Log.d("Actualizar", "Error al actualizar datos")
                }

                override fun onResponse(call: Call<CLIENTE>, response: Response<CLIENTE>) {
                    Log.d("Actualizar", "Datos actualizados")
                }
            }
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SPEECH_TO_TEXT1 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtNameClient.setText(spokenText)
                    }
                }

                REQUEST_CODE_SPEECH_TO_TEXT2 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtLastNameClient.setText(spokenText)
                    }
                }

                REQUEST_CODE_SPEECH_TO_TEXT3 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        val filteredText = spokenText.replace("\\s".toRegex(), "")
                        binding.edtNumDNI.setText(filteredText)
                    }
                }

                REQUEST_CODE_SPEECH_TO_TEXT4 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        val filteredText = spokenText.replace("\\s".toRegex(), "")
                        binding.edtEmailClient.setText(filteredText)
                    }
                }

                REQUEST_CODE_SPEECH_TO_TEXT5 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtPhoneClient.setText(spokenText)
                    }
                }

                REQUEST_CODE_SPEECH_TO_TEXT6 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtAddressClient.setText(spokenText)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error en el reconocimiento de voz.", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnNewClient.setCardBackgroundColor(Color.parseColor("#47484a"))
            binding.txtInitials.setTextColor(Color.parseColor("#121212"))
            binding.btnBack.setColorFilter(Color.parseColor("#ffffff"))
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.txtClientName.setTextColor(Color.parseColor("#ffffff"))
            binding.txtLastClientName.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDNI.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDNINumber.setTextColor(Color.parseColor("#ffffff"))
            binding.txtEmail.setTextColor(Color.parseColor("#ffffff"))
            binding.txtPhone.setTextColor(Color.parseColor("#ffffff"))
            binding.txtAddress.setTextColor(Color.parseColor("#ffffff"))
            binding.btnMicClientNames.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicClientLastNames.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicIDNumber.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicEmail.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicPhoneNumber.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicAddres.setColorFilter(Color.parseColor("#ffffff"))
            binding.edtNameClient.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtLastNameClient.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtNumDNI.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtEmailClient.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtPhoneClient.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtAddressClient.setBackgroundResource(R.drawable.text_info_dark)
            binding.btnAdd.setBackgroundResource(R.drawable.gradientdark)
            binding.btnAdd.setTextColor(Color.parseColor("#121212"))
        }
    }

    private fun showExitConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Advertencia")
        alertDialogBuilder.setMessage("Todos los cambios se perderán. ¿Desea continuar?")
        alertDialogBuilder.setPositiveButton("Salir") { dialogInterface: DialogInterface, _: Int ->
            // Salir de la aplicación
            finish()
        }
        alertDialogBuilder.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int ->
            // Cancelar la acción de salida
            dialogInterface.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onBackPressed() {
        showExitConfirmationDialog()
    }
}