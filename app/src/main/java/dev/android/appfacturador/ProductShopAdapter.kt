package dev.android.appfacturador

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.android.appfacturador.databinding.ItemShopBinding
import dev.android.appfacturador.model.PRODUCTO

class ProductShopAdapter(var products: List<ProductHolder.ProductItem> = emptyList()) :
    RecyclerView.Adapter<ProductShopAdapter.ProductShopAdapterViewHolder>() {

    lateinit var setOnClickListenerProductDelete: (PRODUCTO) -> Unit
    lateinit var setOnClickListenerProductQuit: (Int, Int) -> Unit
    lateinit var setOnClickListenerProductAdd: (Int, Int) -> Unit

    inner class ProductShopAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemShopBinding = ItemShopBinding.bind(itemView)

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(productItem: ProductHolder.ProductItem, position: Int) = with(binding) {
            val product = productItem.product
            val quantity = productItem.quantity

            binding.txtAmount.text = quantity.toString()
            binding.txtProductNameShop.text = product?.nombre
            if (product != null) {
                binding.txtPriceShop.text = "$" + String.format("%.2f", product.precio * quantity)
                if (!product.imagen.isNullOrEmpty()) {
                    Glide.with(binding.root.context)
                        .load(product.imagen)
                        .override(300, 300) // Establece el tamaño deseado
                        .centerCrop()
                        .placeholder(R.drawable.load)
                        .into(imgProductShop)
                }
            }

            val resources = root.resources
            val currentNightMode =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            // Comprueba el modo actual
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // El modo actual es dark
                btnProductInfo.setCardBackgroundColor(Color.parseColor("#121212"))
                txtProductNameShop.setTextColor(Color.parseColor("#ffffff"))
                btnQuitProduct.setColorFilter(Color.parseColor("#ffffff"))
                txtAmount.setTextColor(Color.parseColor("#ffffff"))
                imgProductShop.alpha = 0.6f
                cardProduct.outlineSpotShadowColor = Color.parseColor("#ffffff")
                btnLess.setCardBackgroundColor(Color.parseColor("#121212"))
                txtLess.setTextColor(Color.parseColor("#ffffff"))
                btnMore.setCardBackgroundColor(Color.parseColor("#121212"))
                txtMore.setTextColor(Color.parseColor("#ffffff"))
                btnImage.setCardBackgroundColor(Color.parseColor("#121212"))
            }

            binding.btnQuitProduct.setOnClickListener {
                if (product != null) {
                    setOnClickListenerProductDelete(product)
                }
            }

            binding.btnLess.setOnClickListener {
                if (quantity > 1) {
                    val updatedQuantity = quantity - 1
                    txtAmount.text = updatedQuantity.toString()
                    if (product != null) {
                        txtPriceShop.text = "$" + (product.precio * updatedQuantity).toString()
                    }
                    setOnClickListenerProductQuit(position, updatedQuantity)
                }
            }

            binding.btnMore.setOnClickListener {
                val updatedQuantity = quantity + 1
                txtAmount.text = updatedQuantity.toString()
                if (product != null) {
                    txtPriceShop.setText("$" + (product.precio * updatedQuantity).toString())
                }
                setOnClickListenerProductAdd(position, updatedQuantity)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductShopAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ProductShopAdapterViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(holder: ProductShopAdapterViewHolder, position: Int) {
        val productItem = products[position]
        holder.bind(productItem, position)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun updateListProducts(products: List<ProductHolder.ProductItem>) {
        this.products = products
        notifyDataSetChanged()
    }
}
