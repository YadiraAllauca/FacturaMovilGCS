package dev.android.appfacturador

import androidx.databinding.BaseObservable
import dev.android.appfacturador.model.PRODUCTO
import java.io.Serializable

object ProductHolder : BaseObservable() {
    data class ProductItem(val product: PRODUCTO?, var quantity: Int, var discount: Int) :
        Serializable {
        constructor() : this(null, 0, 0)
    }

    var productList: MutableList<ProductItem> = mutableListOf()

    fun updateQuantity(index: Int, quantity: Int) {
        if (index in 0 until productList.size) {
            productList[index].quantity = quantity
        }
    }

    fun updateDiscount(index: Int, discount: Int) {
        if (index in 0 until productList.size) {
            productList[index].discount = discount
        }
    }
}