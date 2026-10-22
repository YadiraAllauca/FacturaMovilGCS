package dev.android.appfacturador

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.appfacturador.databinding.ItemProductBinding
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.PRODUCTO

class ProductAdapter(var products: List<PRODUCTO> = emptyList()) :
    RecyclerView.Adapter<ProductAdapter.ProductAdapterViewHolder>() {
    lateinit var setOnClickListenerProductDelete: (PRODUCTO) -> Unit
    lateinit var setOnClickListenerProductEdit: (PRODUCTO) -> Unit
    private var currentUserEmail: String = ""

    inner class ProductAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemProductBinding = ItemProductBinding.bind(itemView)

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(product: PRODUCTO) = with(binding) {
            txtProduct.text = product.nombre
            txtPrice.text = "$" + product.precio.toString()
            txtIVA.text = product.id_categoria_impuesto + "%"

            if (!product.imagen.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(product.imagen)
                    .override(300, 300) // Establece el tamaño deseado
                    .centerCrop()
                    .placeholder(R.drawable.load)
                    .into(imgProduct)
            }

            val resources = root.resources
            val currentNightMode =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            // Comprueba el modo actual
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // El modo actual es dark
//                btnProductInfo.setCardBackgroundColor(Color.parseColor("#202427"))
                btnProductInfo.setCardBackgroundColor(Color.parseColor("#121212"))
//                cardProduct.outlineSpotShadowColor = Color.TRANSPARENT
                txtProduct.setTextColor(Color.parseColor("#ffffff"))
                txtPrice.setTextColor(Color.parseColor("#ffffff"))
                btnDeleteProduct.setColorFilter(Color.parseColor("#ffffff"))
                imgProduct.alpha = 0.6f
                cardProduct.outlineSpotShadowColor = Color.parseColor("#ffffff")
            }
            if (currentUserEmail.isNotEmpty()) {
                val usuariosRef = FirebaseDatabase.getInstance().getReference("Empleado")

                usuariosRef.orderByChild("correo_electronico").equalTo(currentUserEmail)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (childSnapshot in dataSnapshot.children) {
                                    val empleado = childSnapshot.getValue(EMPLEADO::class.java)
                                    if (empleado != null && empleado.tipo_empleado == "V") {
                                        root.isClickable = false
                                        btnDeleteProduct.isVisible = false
                                    }
                                    break
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
            }

            btnDeleteProduct.setOnClickListener {
                setOnClickListenerProductDelete(product)
            }
            root.setOnClickListener {
                setOnClickListenerProductEdit(product)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductAdapter.ProductAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductAdapterViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(holder: ProductAdapterViewHolder, position: Int) {
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

    fun setCurrentUserEmailProduct(email: String) {
        this.currentUserEmail = email
        notifyDataSetChanged()
    }


}