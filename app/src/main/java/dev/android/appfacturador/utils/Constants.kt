package dev.android.appfacturador.utils

import com.google.firebase.database.FirebaseDatabase

class Constants {
    companion object {
        // Keep REST requests on the database configured for the Firebase SDK.
        val DATABASE_URL: String
            get() = FirebaseDatabase.getInstance().reference.toString().trimEnd('/') + "/"

        val KEY_BILL = "key_bill"
        val KEY_PRODUCT = "key_product"
        val KEY_CLIENT = "key_client"
        val KEY_EMPLOYEE = "key_employee"
        val TYPE_DNI = arrayOf("Cédula", "RUC", "Pasaporte")
        val TYPE_PAY = arrayOf("Efectivo", "Depósito", "Transferencia")
        val TYPE_EMPLOYEE = arrayOf("Vendedor", "Administrador")
    }
}
