package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import dev.android.appfacturador.databinding.ActivityProductBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.PRODUCTO
import dev.android.appfacturador.utils.Constants
import dev.android.appfacturador.utils.SpeechToTextUtil

class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding
    private lateinit var email: String
    private lateinit var shop: String
    private var list: MutableList<PRODUCTO> = mutableListOf()
    private val adapter: ProductAdapter by lazy {
        ProductAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private val fb = Firebase.database
    private val dr = fb.getReference("Product")
    private lateinit var searchEditText: EditText
    lateinit var barcode: String
    private val REQUEST_CODE_SPEECH_TO_TEXT1 = 1

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        searchEditText = binding.edtSearch
        binding.btnAddProduct.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))

        //usuario y negocio actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()
        if (email.isEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        getUserData()
        setupViews()
        darkMode()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().trim()
                updateProductList(searchTerm)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setupActions()
        swipeToAddShopCar()
        shoppingCardActive()
    }

    private fun getUserData() {
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
                                loadData()
                                if (empleado.tipo_empleado == "V") {
                                    binding.btnAddProduct.isVisible = false
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@ProductActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupViews() {
        recyclerView = binding.rvProducts
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter.setOnClickListenerProductDelete = { product ->
            deleteProduct(product)
            adapter.notifyDataSetChanged()
        }

        adapter.setOnClickListenerProductEdit = { product ->
            val bundle = Bundle().apply {
                putSerializable(Constants.KEY_PRODUCT, product)
            }
            val intent =
                Intent(applicationContext, AddProductActivity::class.java).putExtras(bundle)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    fun loadData() {
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
                adapter.setCurrentUserEmailProduct(email)
                list = productList
                adapter.updateListProducts(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }
        dr.addValueEventListener(listen)
    }

    fun setupActions() {
        binding.btnCloses.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("option", "product")
            }
            startActivity(intent)
        }

        binding.btnScanner.setOnClickListener {
            initScanner()
        }

        binding.btnShop.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        binding.btnMicSearch.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this@ProductActivity, REQUEST_CODE_SPEECH_TO_TEXT1)
        }
    }

    fun updateProductList(searchTerm: String) {
        val filteredList = list.filter { product ->
            product.nombre.contains(
                searchTerm,
                ignoreCase = true
            ) || product.codigo_barras.contains(searchTerm, ignoreCase = true)
        }
        adapter.updateListProducts(filteredList)
    }

    fun deleteProduct(producto: PRODUCTO) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar registro")
        builder.setMessage("¿Desea continuar?")
        builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
            val database = FirebaseDatabase.getInstance()
            val productoRef = database.getReference("Product/${producto.id}")

            productoRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar el registro", Toast.LENGTH_SHORT).show()
                }
        })
        builder.show()
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

    private fun swipeToAddShopCar() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val product = adapter.products[position]
                val quantity = 1
                val discount = adapter.products[position].max_descuento
                val productItem = ProductHolder.ProductItem(product, quantity, discount)
                val existingProduct =
                    ProductHolder.productList.find { it.product?.nombre == product.nombre }
                if (existingProduct == null) {
                    ProductHolder.productList.add(productItem)
                }
                adapter.notifyDataSetChanged()
                binding.imgFull.visibility = View.VISIBLE
            }
        }).attachToRecyclerView(binding.rvProducts)
    }

    private fun shoppingCardActive() {
        binding.imgFull.visibility =
            if (ProductHolder.productList.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onRestart() {
        super.onRestart()
        shoppingCardActive()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor", "ResourceType")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.imgFull.setColorFilter(Color.parseColor("#47484a"))
            binding.edtSearch.setBackgroundResource(R.drawable.searchdark)
            binding.edtSearch.setTextColor(Color.parseColor("#ffffff"))
            binding.edtSearch.outlineSpotShadowColor = Color.parseColor("#ffffff")
            binding.btnShop.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnMicSearch.setColorFilter(Color.parseColor("#47484a"))
            val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.scanner_white)
            binding.btnScanner.setImageDrawable(drawable)
            binding.btnAddProduct.imageTintList =
                ColorStateList.valueOf(Color.parseColor("#121212"))
            binding.btnAddProduct.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#47484a"))
            binding.btnClose.setCardBackgroundColor(Color.parseColor("#47484a"))
            binding.btnCloses.setColorFilter(Color.parseColor("#121212"))
        }
    }

    override fun onBackPressed() {
    }
}