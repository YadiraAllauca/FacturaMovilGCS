package dev.android.appfacturador

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.android.appfacturador.databinding.ItemAddProductBillBinding
import dev.android.appfacturador.model.PRODUCTO

class ProductItemBillAdapter(var products: List<PRODUCTO> = emptyList()) :
    RecyclerView.Adapter<ProductItemBillAdapter.ProductItemBillAdapterViewHolder>() {
    lateinit var onCheckedChangeListener: (product: PRODUCTO, isChecked: Boolean) -> Unit

    inner class ProductItemBillAdapterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private var binding: ItemAddProductBillBinding = ItemAddProductBillBinding.bind(itemView)

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(product: PRODUCTO) = with(binding) {
            binding.txtProductName.text = product.nombre
            binding.txtPrice.text = "$" + product.precio.toString()

            val resources = root.resources
            val currentNightMode =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            // Comprueba el modo actual
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // El modo actual es dark
                btnProductInfo.setCardBackgroundColor(Color.parseColor("#121212"))
                txtProductName.setTextColor(Color.parseColor("#ffffff"))
                txtPrice.setTextColor(Color.parseColor("#ffffff"))
                cbItemBill.buttonTintList = ColorStateList.valueOf(Color.parseColor("#ffffff"))
                imgProduct.alpha = 0.6f
                cardProduct.outlineSpotShadowColor = Color.parseColor("#ffffff")
            }

            if (!product.imagen.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(product.imagen)
                    .centerCrop()
                    .placeholder(R.drawable.load)
                    .into(imgProduct)
            }

            val isProductSelected =
                ProductHolder.productList.any { it.product?.nombre == product.nombre }
            binding.cbItemBill.isChecked = isProductSelected

            binding.cbItemBill.setOnClickListener { view ->
                val isChecked = (view as CheckBox).isChecked
                onCheckedChangeListener(product, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductItemBillAdapter.ProductItemBillAdapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_product_bill, parent, false)
        return ProductItemBillAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductItemBillAdapterViewHolder, position: Int) {
        val membership: PRODUCTO = products[position]
        holder.bind(membership)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun updateListProducts(products: List<PRODUCTO>) {
        this.products = products
        notifyDataSetChanged()
    }

}