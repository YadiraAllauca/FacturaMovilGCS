package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.databinding.ActivityProductBinding
import dev.android.appfacturador.databinding.ActivityShopBinding

class ShopActivity : AppCompatActivity() {
    lateinit var binding: ActivityShopBinding
    lateinit var bindingProductBinding: ActivityProductBinding
    private val adapter: ProductShopAdapter by lazy {
        ProductShopAdapter()
    }
    private lateinit var recyclerView: RecyclerView
    private var total: Float = 0f

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        bindingProductBinding = ActivityProductBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        initialize()
        loadData()
        actions()
        darkMode()
    }

    private fun initialize() {
        recyclerView = binding.rvHistory
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
    }

    private fun loadData() {
        total = ProductHolder.productList.sumByDouble {
            ((it.product?.precio ?: 0f) * it.quantity).toDouble()
        }.toFloat()

        adapter.updateListProducts(ProductHolder.productList)
        recyclerView.adapter = adapter
        updateTotalShop()

        adapter.setOnClickListenerProductDelete = { product ->
            ProductHolder.productList.removeAll { it.product == product }
            updateTotalShop()
        }

        adapter.setOnClickListenerProductAdd = { position, quantity ->
            ProductHolder.updateQuantity(position, quantity)
            updateTotalShop()
        }

        adapter.setOnClickListenerProductQuit = { position, quantity ->
            ProductHolder.updateQuantity(position, quantity)
            updateTotalShop()
        }
    }

    fun actions() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnClear.setOnClickListener {
            bindingProductBinding.imgFull.visibility = View.GONE
            ProductHolder.productList.clear()
            updateTotalShop()
        }

        binding.btnBill.setOnClickListener {
            val intent = Intent(this, AddBillActivity::class.java).apply {}
            startActivity(intent)
            finish()
        }
    }

    fun updateTotalShop() {
        total = ProductHolder.productList.sumByDouble {
            ((it.product?.precio ?: 0f) * it.quantity).toDouble()
        }.toFloat()
        adapter.notifyDataSetChanged()
        binding.txtTotalShop.text = "$" + String.format("%.2f", total)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnContainerTotal.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.btnContainerTotal.outlineSpotShadowColor = Color.parseColor("#ffffff")
            binding.btnBill.setBackgroundResource(R.drawable.gradientdark)
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.btnBack.setColorFilter(Color.parseColor("#ffffff"))
            binding.btnClear.setColorFilter(Color.parseColor("#ffffff"))
            binding.txtTotal.setTextColor(Color.parseColor("#ffffff"))
            binding.txtTotalShop.setTextColor(Color.parseColor("#ffffff"))
        }
    }
}
