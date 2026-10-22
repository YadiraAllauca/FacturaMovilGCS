package dev.android.appfacturador

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import dev.android.appfacturador.databinding.ItemBillBinding
import dev.android.appfacturador.model.FACTURA

class BillAdapter(var bills: List<FACTURA> = emptyList()) :
    RecyclerView.Adapter<BillAdapter.BillAdapterViewHolder>() {
    lateinit var setOnClickListenerBillEdit: (FACTURA) -> Unit

    inner class BillAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: ItemBillBinding = ItemBillBinding.bind(itemView)

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(bill: FACTURA) = with(binding) {
            txtBillNumber.text = "Factura " + bill.numero_factura
            txtClientID.text = bill.cliente?.numero_dni
            txtDate.text = bill.fecha
            txtBillNumberInitials.text = bill.cliente?.primer_nombre.toString()
                .substring(0, 1) + bill.cliente?.apellido_paterno.toString().substring(0, 1)

            val resources = root.resources
            val currentNightMode =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            // Comprueba el modo actual
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // El modo actual es dark
                btnBillInfo.setCardBackgroundColor(Color.parseColor("#121212"))
                txtBillNumber.setTextColor(Color.parseColor("#ffffff"))
                txtBillNumberInitials.setTextColor(Color.parseColor("#121212"))
                btnContainer.setCardBackgroundColor(Color.parseColor("#47484a"))
                cardProduct.setCardBackgroundColor(Color.TRANSPARENT)
                cardProduct.outlineSpotShadowColor = Color.parseColor("#ffffff")
            }

            binding.btnBillInfo.setOnClickListener {
                setOnClickListenerBillEdit(bill)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BillAdapter.BillAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill, parent, false)
        return BillAdapterViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(holder: BillAdapterViewHolder, position: Int) {
        val membership: FACTURA = bills[position]
        holder.bind(membership)
    }

    override fun getItemCount(): Int {
        return bills.size
    }

    fun updateListbills(bills: List<FACTURA>) {
        this.bills = bills
        notifyDataSetChanged()
    }

}