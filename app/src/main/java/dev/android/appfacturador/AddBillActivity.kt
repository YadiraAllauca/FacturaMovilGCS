package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.databinding.ActivityAddBillBinding
import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.FACTURA
import dev.android.appfacturador.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class AddBillActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBillBinding
    lateinit var email: String
    lateinit var shop: String
    private val adapter: ProductBillAdapter by lazy {
        ProductBillAdapter()
    }

    private lateinit var recyclerView: RecyclerView
    lateinit var searchClienteEditText: EditText
    private var clienteEncontrado: CLIENTE? = null
    private var empleadoEncontrado: EMPLEADO? = null
    private lateinit var spinner: Spinner
    var contadorNegocio = 0

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fechaActual = dateFormat.format(calendar.time)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBillBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        //usuario y negocio actual
        val sharedPreferences = getSharedPreferences("PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", "").toString()

        // Inicializar vistas
        initViews()

        getUserData()

        // Configurar listeners
        setupActions()
        darkMode()
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    private fun initViews() {
        recyclerView = binding.rvProducts
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter.updateListProducts(ProductHolder.productList)
        recyclerView.adapter = adapter

        spinner = binding.spinnerPay
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.TYPE_PAY)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupActions() {
        searchClienteEditText = binding.edtID
        searchClienteEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().trim()
                getClienteData(searchTerm)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnAddClient.setOnClickListener {
            val intent = Intent(this, AddClientActivity::class.java)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, BillActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddItem.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }

        binding.btnGenerateBill.setOnClickListener {
            getShopCounter(shop)
            val billData = createBillData()
            if (billData != null) {
                addBill(billData)
            } else {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserData() {
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
                                empleadoEncontrado = empleado
                                shop = empleado.negocio
                                loadData()
                                getShopCounter(shop)
                                if (empleado.tipo_empleado == "V") {
                                    binding.btnAddClient.isVisible = false
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddBillActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadData() {
        adapter.updateListProducts(ProductHolder.productList)
        updateValues()

        adapter.addTextChangedListenerAmount = { position, quantity ->
            ProductHolder.updateQuantity(position, quantity)
            updateValues()
        }

        adapter.addTextChangedListenerDiscount = { position, newDiscount ->
            ProductHolder.updateDiscount(position, newDiscount)
            updateValues()
        }
    }

    private fun getClienteData(searchCliente: String) {
        val database = FirebaseDatabase.getInstance()
        val clientesRef = database.getReference("Cliente")

        clientesRef.orderByChild("numero_dni").equalTo(searchCliente)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnapshot in dataSnapshot.children) {
                            val cliente = childSnapshot.getValue(CLIENTE::class.java)
                            if (cliente != null) {
                                clienteEncontrado = cliente
                                binding.edtID.setTextColor(Color.BLACK)
                                binding.txtClientName.text =
                                    cliente.primer_nombre + " " + cliente.apellido_paterno
                                binding.txtClientName.setTextColor(Color.BLACK)
                                binding.txtClientEmail.text = cliente.correo_electronico
                                binding.txtClientEmail.setTextColor(Color.BLACK)
                                darkMode()
                            }
                        }
                    } else {
                        binding.edtID.setTextColor(Color.RED)
                        binding.txtClientName.text = "Cliente no Identificado"
                        binding.txtClientName.setTextColor(Color.RED)
                        binding.txtClientEmail.text = "---"
                        binding.txtClientEmail.setTextColor(Color.RED)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddBillActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun createBillData(): FACTURA? {
        val id = ""
        val numero_factura = contadorNegocio.toString()
        val estado = "enviado"
        val fecha = fechaActual
        val cliente = clienteEncontrado
        val empleado = empleadoEncontrado
        val forma_pago = spinner.selectedItem.toString()
        val subtotal = calculateSubtotal()
        val descuento = calculateTotalDiscount()
        val iva = calculateTotalIVA()
        val total = calculateTotalBill()
        val items = ProductHolder.productList
        val negocio = shop

        if (cliente != null && empleado != null && !negocio.isEmpty()) {
            return FACTURA(
                id, numero_factura, estado,
                fecha, cliente, empleado,
                forma_pago, subtotal, descuento, iva, total,
                items, negocio
            )
        }

        return null
    }

    private fun addBill(bill: FACTURA) {
        val database = FirebaseDatabase.getInstance()
        val billsRef = database.getReference("Factura")

        val newBillRef = billsRef.push()
        val newBillId = newBillRef.key
        bill.id = newBillId.toString()

        newBillRef.setValue(bill)
            .addOnSuccessListener {
                ProductHolder.productList.clear()
                clienteEncontrado = null
                updateShopCounter()
                Toast.makeText(
                    this@AddBillActivity,
                    "¡Factura registrada exitosamente!",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@AddBillActivity, BillActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@AddBillActivity,
                    "Error al agregar la factura: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getShopCounter(shopId: String) {
        val database = FirebaseDatabase.getInstance()
        val negocioRef = database.getReference("Negocios")

        negocioRef.child(shopId).child("contador")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val contador = dataSnapshot.getValue(Long::class.java)?.toInt()
                    if (contador != null) {
                        contadorNegocio = contador + 1
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddBillActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateShopCounter() {
        val database = FirebaseDatabase.getInstance()
        val negocioRef = database.getReference("Negocios")

        negocioRef.child(shop).child("contador")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val contador = dataSnapshot.getValue(Long::class.java)?.toInt() ?: 0
                    negocioRef.child(shop).child("contador").setValue(contador + 1)
                        .addOnSuccessListener {
                            // Éxito al actualizar el contador del negocio
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this@AddBillActivity,
                                "Error al actualizar el contador del negocio: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@AddBillActivity,
                        "Error en la solicitud: " + databaseError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateValues() {
        binding.txtDiscount.text = "$" + String.format("%.2f", calculateTotalDiscount())
        binding.txtSubtotal.text = "$" + String.format("%.2f", calculateSubtotal())
        binding.txtIva.text = "$" + String.format("%.2f", calculateTotalIVA())
        binding.txtTotalBill.text = "$" + String.format("%.2f", calculateTotalBill())
    }

    private fun calculateSubtotal(): Float {
        var totalSubtotal = 0f
        for (product in ProductHolder.productList) {
            val subtotal = product.quantity * (product.product?.precio ?: 0f)
            totalSubtotal += subtotal
        }
        return totalSubtotal
    }

    private fun calculateTotalDiscount(): Float {
        var totalDiscount = 0f
        for (product in ProductHolder.productList) {
            val discount =
                ((product.product?.precio ?: 0f) * product.discount / 100) * product.quantity
            totalDiscount += discount
        }
        return totalDiscount
    }

    private fun calculateTotalIVA(): Float {
        var totalIVA = 0f
        for (product in ProductHolder.productList) {
            val iva =
                (product.product?.id_categoria_impuesto?.toFloat() ?: 0f) * (product.product?.precio
                    ?: 0f) / 100
            totalIVA += iva
        }
        return totalIVA
    }

    private fun calculateTotalBill(): Float {
        return calculateSubtotal() + calculateTotalIVA() - calculateTotalDiscount()
    }

    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnBack.setColorFilter(Color.parseColor("#ffffff"))
            binding.edtID.setTextColor(Color.parseColor("#ffffff"))
            binding.edtID.setBackgroundResource(R.drawable.textdark)
            binding.btnAddClient.setColorFilter(Color.parseColor("#47484a"))
            binding.txtClientName.setTextColor(Color.parseColor("#ffffff"))
            binding.txtClientEmail.setTextColor(Color.parseColor("#ffffff"))
            binding.btnAddItem.setTextColor(Color.parseColor("#121212"))
            binding.btnAddItem.setBackgroundResource(R.drawable.gradientdark)
            binding.txtSubtotal.setTextColor(Color.parseColor("#ffffff"))
            binding.txtIva.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDiscount.setTextColor(Color.parseColor("#ffffff"))
            binding.txtTotalBill.setTextColor(Color.parseColor("#ffffff"))
            binding.btnGenerateBill.setBackgroundResource(R.drawable.textdark)
            binding.btnGenerateBill.setTextColor(Color.parseColor("#ffffff"))
            binding.divider.setBackgroundColor(Color.parseColor("#242424"))
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
