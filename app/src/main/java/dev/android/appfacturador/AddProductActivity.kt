package dev.android.appfacturador

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.integration.android.IntentIntegrator
import dev.android.appfacturador.R.drawable.load
import dev.android.appfacturador.databinding.ActivityAddProductBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.PRODUCTO
import dev.android.appfacturador.utils.Constants
import dev.android.appfacturador.utils.SpeechToTextUtil

class AddProductActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddProductBinding
    lateinit var email: String
    lateinit var shop: String
    private val typeIVA = arrayOf("0", "12", "14")
    lateinit var spinner: Spinner
    lateinit var codigoBarras: String
    var id = ""
    private lateinit var storageReference: StorageReference
    private val storage_path = "products/*"
    var imageStorage: String = ""
    var image: Uri? = null
    private val progressDialog: ProgressDialog? = null
    private val REQUEST_CODE_SPEECH_TO_TEXT1 = 1
    private val REQUEST_CODE_SPEECH_TO_TEXT2 = 2
    private val REQUEST_CODE_SPEECH_TO_TEXT3 = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        storageReference = FirebaseStorage.getInstance().reference

        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        if (email.isEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        getShop()
        initialize()
        setupActions()
        eventsMicro()
        darkMode()
    }

    private fun eventsMicro() {
        binding.btnMicProduct.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT1)
        }
        binding.btnMicPrice.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT2)
        }
        binding.btnMicDiscount.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT3)
        }
    }

    fun initialize() {
        spinner = binding.spnIVA
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, typeIVA)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val bundle = intent.extras
        bundle?.let {
            val product = bundle.getSerializable(Constants.KEY_PRODUCT) as PRODUCTO
            id = product.id
            binding.btnAdd.text = "ACTUALIZAR"
            binding.txtTitle.text = "Editar producto"
            binding.edtProduct.setText(product.nombre)
            binding.edtPrice.setText(product.precio.toString())
            binding.edtDiscount.setText(product.max_descuento.toString())
            binding.edtBarCode.setText(product.codigo_barras.toString())
            if (!product.imagen.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(product.imagen) // Establece el tamaño deseado
                    .centerCrop()
                    .placeholder(R.drawable.load)
                    .into(binding.imgProduct)
            }
            imageStorage = product.imagen //agregar la imagen a una variable
            val productTypeIVA = product.id_categoria_impuesto
            val position = typeIVA.indexOf(productTypeIVA)
            spinner.setSelection(position)
        } ?: run {
            binding.btnAdd.text = "AGREGAR"
            binding.edtProduct.setText("")
            binding.edtPrice.setText("")
            binding.edtDiscount.setText("")
            binding.edtBarCode.setText("")
            binding.imgProduct.setImageResource(load)
        }
        binding.edtProduct.requestFocus()
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13)
        integrator.setPrompt("Código de Barras")
        integrator.initiateScan()
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
                        this@AddProductActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun setupActions() {
        val loadImage =
            registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                if (it != null) {
                    binding.imgProduct.setImageURI(it)
                    image = it
                }
            })

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCamera.setOnClickListener {
            loadImage.launch("image/*")
        }

        binding.btnScanner.setOnClickListener {
            initScanner()
        }

        binding.btnAdd.setOnClickListener {
            val product = binding.edtProduct.text.toString()
            val price = binding.edtPrice.text.toString()
            val iva = spinner.selectedItem.toString()
            val discount = binding.edtDiscount.text.toString()
            val barcode = binding.edtBarCode.text.toString()
            var img = ""

            if (product.isEmpty() || price.isEmpty() || iva.isEmpty() || discount.isEmpty() || barcode.isEmpty() || (image == null && imageStorage.isEmpty())) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            } else {
                if (!imageStorage.isEmpty() && image == null) {
                    //actualizar pero misma imagen
                    img = imageStorage
                } else {
                    img = "null"
                }

                var productData = PRODUCTO(
                    id,
                    product,
                    price.toFloat(),
                    discount.toInt(),
                    iva,
                    barcode,
                    img,
                    shop
                )

                if (id.isEmpty()) {
                    addNewProduct(productData)
                    Toast.makeText(this, "¡Producto agregado exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (!imageStorage.isEmpty() && image != null) {
                        uploadImage(image!!, productData.id)
                    }
                    updateProduct(productData)
                    Toast.makeText(this, "¡Producto actualizado exitosamente!", Toast.LENGTH_SHORT)
                        .show()
                }
                val intent = Intent(baseContext, ProductActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun addNewProduct(producto: PRODUCTO) {
        val database = FirebaseDatabase.getInstance()
        val productsRef = database.getReference("Product")

        val newProductRef = productsRef.push() // Crea un nuevo nodo en la referencia de "Product"
        val newProductId = newProductRef.key // Obtiene el ID del nuevo producto

        producto.id = newProductId.toString()
        // Guarda el producto en la base de datos
        newProductRef.setValue(producto)
            .addOnSuccessListener {
                Log.d("Agregar", "Producto agregado con éxito. ID: $newProductId")
                if (imageStorage.isEmpty() && image != null) {
                    uploadImage(image!!, newProductId.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Agregar", "Error al agregar producto: ${exception.message}")
            }
    }

    private fun updateProduct(producto: PRODUCTO) {
        val database = FirebaseDatabase.getInstance()
        val productsRef = database.getReference("Product")

        productsRef.child(producto.id).setValue(producto)
            .addOnSuccessListener {
                Log.d("Actualizar", "Producto actualizado con éxito. ID: ${producto.id}")
            }
            .addOnFailureListener { exception ->
                Log.d("Actualizar", "Error al actualizar producto: ${exception.message}")
            }
    }

    private fun uploadImage(imageUrl: Uri, productId: String) {
        val storagePath = "$storage_path imagen${System.currentTimeMillis()}"
        val reference: StorageReference = storageReference.child(storagePath)

        reference.putFile(imageUrl)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    updateImageUrlInDatabase(productId, downloadUrl)
                }.addOnFailureListener { exception ->
                    showToast("Error al obtener la URL de descarga")
                    progressDialog?.dismiss()
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error al cargar foto")
            }
    }

    private fun updateImageUrlInDatabase(productId: String, imageUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val productsRef = database.getReference("Product")

        productsRef.child(productId).child("imagen").setValue(imageUrl)
            .addOnSuccessListener {}
            .addOnFailureListener { exception ->
                showToast("Error al subir la imagen: ${exception.message}")
            }
            .addOnCompleteListener {
                progressDialog?.dismiss()
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@AddProductActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SPEECH_TO_TEXT1 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtProduct.setText(spokenText)
                    }
                }

                REQUEST_CODE_SPEECH_TO_TEXT2 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtPrice.setText(spokenText)
                    }
                }

                REQUEST_CODE_SPEECH_TO_TEXT3 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        val filteredText = spokenText.replace("\\s".toRegex(), "")
                        binding.edtDiscount.setText(filteredText)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error en el reconocimiento de voz.", Toast.LENGTH_SHORT).show()
        }

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            } else {
                codigoBarras = result.contents
                binding.edtBarCode.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            if (id == "") {
                val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.loaddark)
                binding.imgProduct.setImageDrawable(drawable)
            }
            binding.imgProduct.alpha = 0.6f
            binding.btnEditImage.setCardBackgroundColor(Color.parseColor("#121212"))
//            binding.btnCamera.setColorFilter(Color.parseColor("#65696d"))
            binding.btnCamera.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnBack.setColorFilter(Color.parseColor("#ffffff"))
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.txtProduct.setTextColor(Color.parseColor("#ffffff"))
            binding.txtPrice.setTextColor(Color.parseColor("#ffffff"))
            binding.txtIVA.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDiscount.setTextColor(Color.parseColor("#ffffff"))
            binding.txtCode.setTextColor(Color.parseColor("#ffffff"))
            binding.btnMicProduct.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicPrice.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicDiscount.setColorFilter(Color.parseColor("#ffffff"))
            val drawableScannr: Drawable? =
                ContextCompat.getDrawable(this, R.drawable.scanner_white)
            binding.btnScanner.setImageDrawable(drawableScannr)
            binding.edtProduct.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtPrice.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtDiscount.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtBarCode.setBackgroundResource(R.drawable.text_info_dark)
            binding.btnAdd.setBackgroundResource(R.drawable.gradientdark)
            binding.btnAdd.setTextColor(Color.parseColor("#121212"))
            binding.btnImageProduct.setCardBackgroundColor(Color.parseColor("#121212"))
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