package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import dev.android.appfacturador.databinding.ActivityAddItemBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.PRODUCTO
import dev.android.appfacturador.utils.SpeechToTextUtil

class AddItemActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddItemBinding
    lateinit var email: String
    lateinit var shop: String
    private var list: MutableList<PRODUCTO> = mutableListOf()
    private val adapter: ProductItemBillAdapter by lazy {
        ProductItemBillAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val fb = Firebase.database
    private val dr = fb.getReference("Product")
    private var addedList: MutableList<PRODUCTO> = mutableListOf()
    lateinit var searchEditText: EditText
    lateinit var barcode: String
    private val REQUEST_CODE_SPEECH_TO_TEXT1 = 1

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        searchEditText = binding.edtSearch
        binding.btnAddItems.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))

        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        if (email.isEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        getShop()
        setupViews()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().trim()
                updateProductList(searchTerm)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setupActions()
        darkMode()
    }

    private fun getShop() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        val usuariosRef = FirebaseDatabase.getInstance().getReference("Empleado")

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
                        loadData()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddItemActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun setupViews() {
        recyclerView = binding.rvProducts
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter.onCheckedChangeListener = { product, isChecked ->
            if (isChecked) {
                val productItem =
                    ProductHolder.ProductItem(product, 1, product.max_descuento)
                ProductHolder.productList.add(productItem)
                addedList.add(product)
            } else {
                addedList.remove(product)
                val position =
                    ProductHolder.productList.indexOfFirst { it.product?.nombre == product.nombre }

                if (position != -1) {
                    ProductHolder.productList.removeAt(position)
                }
            }
        }

        recyclerView.adapter = adapter
    }

    fun setupActions() {
        binding.btnScanner.setOnClickListener {
            initScanner()
        }

        binding.btnAddedProducts.background = getDrawable(R.drawable.gradienttwo)
        binding.btnAddedProducts.setTextColor(Color.parseColor("#686868"))

        binding.btnAllProducts.setOnClickListener {
            binding.btnAddedProducts.background = getDrawable(R.drawable.gradienttwo)
            binding.btnAddedProducts.setTextColor(Color.parseColor("#686868"))
            binding.btnAllProducts.background = getDrawable(R.drawable.gradient)
            binding.btnAllProducts.setTextColor(Color.parseColor("#ffffff"))
            adapter.updateListProducts(list)
            buttonsDarkMode(binding.btnAllProducts, binding.btnAddedProducts)
        }

        binding.btnAddedProducts.setOnClickListener {
            binding.btnAddedProducts.background = getDrawable(R.drawable.gradient)
            binding.btnAddedProducts.setTextColor(Color.parseColor("#ffffff"))
            binding.btnAllProducts.background = getDrawable(R.drawable.gradienttwo)
            binding.btnAllProducts.setTextColor(Color.parseColor("#686868"))

            val selectedProducts = ProductHolder.productList.mapNotNull { it.product }
            val filteredProducts = list.filter { selectedProducts.contains(it) }
            adapter.updateListProducts(filteredProducts)
            buttonsDarkMode(binding.btnAddedProducts, binding.btnAllProducts)
        }

        binding.btnAddItems.setOnClickListener {
            finish()

        }

        binding.btnMicSearch.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this@AddItemActivity, REQUEST_CODE_SPEECH_TO_TEXT1)
        }
    }

    private fun loadData() {
        var listen = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList: MutableList<PRODUCTO> = mutableListOf()
                snapshot.children.forEach { child ->
                    val negocio = child.child("negocio").value?.toString()
                    if (negocio == shop) {
                        val product: PRODUCTO? = child.getValue(PRODUCTO::class.java)
                        product?.let { productList.add(it) }
                    }
                }
                list = productList
                adapter.updateListProducts(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        dr.addValueEventListener(listen)
    }

    private fun updateProductList(searchTerm: String) {
        val filteredList = list.filter { product ->
            product.nombre.contains(searchTerm, ignoreCase = true)
                    || product.codigo_barras.contains(searchTerm, ignoreCase = true)
        }
        adapter.updateListProducts(filteredList)
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13)
        integrator.setPrompt("Código de Barras")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            } else {
                barcode = result.contents
                binding.edtSearch.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SPEECH_TO_TEXT1 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtSearch.setText(spokenText)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error en el reconocimiento de voz.", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor", "ResourceType")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.edtSearch.setBackgroundResource(R.drawable.searchdark)
            binding.edtSearch.setTextColor(Color.parseColor("#ffffff"))
            binding.edtSearch.outlineSpotShadowColor = Color.parseColor("#ffffff")
            binding.btnMicSearch.setColorFilter(Color.parseColor("#47484a"))
            val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.scanner_white)
            binding.btnScanner.setImageDrawable(drawable)
            binding.btnAddItems.imageTintList = ColorStateList.valueOf(Color.parseColor("#121212"))
            binding.btnAddItems.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#47484a"))
            binding.btnAllProducts.setTextColor(Color.parseColor("#121212"))
            binding.btnAllProducts.setBackgroundResource(R.drawable.gradientdarkwhite)
            binding.btnAddedProducts.setTextColor(Color.parseColor("#ffffff"))
            binding.btnAddedProducts.setBackgroundResource(R.drawable.gradientdark)
            binding.divider.setBackgroundColor(Color.parseColor("#242424"))
        }
    }

    fun buttonsDarkMode(buttonClicked: Button, buttonNotClicked: Button) {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            buttonNotClicked.background = getDrawable(R.drawable.gradientdark)
            buttonNotClicked.setTextColor(Color.parseColor("#ffffff"))
            buttonClicked.background = getDrawable(R.drawable.gradientdarkwhite)
            buttonClicked.setTextColor(Color.parseColor("#121212"))
        }
    }

    override fun onBackPressed() {
    }
}