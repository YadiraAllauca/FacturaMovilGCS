package dev.android.appfacturador

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import dev.android.appfacturador.databinding.ActivityCreditNoteBinding
import dev.android.appfacturador.model.FACTURA
import dev.android.appfacturador.utils.Constants
import dev.android.appfacturador.utils.SpeechToTextUtil

class CreditNoteActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreditNoteBinding
    lateinit var email: String
    lateinit var shop: String
    lateinit var creditNotePriceEditText: EditText
    var price: Float = 0f
    private val REQUEST_CODE_SPEECH_TO_TEXT1 = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreditNoteBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.edtClient.isEnabled = false
        binding.edtBill.isEnabled = false

        initialize()
        setupActions()
        darkMode()
        eventMicro()
    }

    fun initialize() {
        val bundle = intent.extras
        bundle?.let {
            val bill = bundle.getSerializable(Constants.KEY_BILL) as FACTURA
            binding.edtBill.setText(bill.numero_factura)
            binding.edtClient.setText(bill.cliente?.numero_dni.toString())
            binding.edtPrice2.setText("$" + String.format("%.2f", bill.total))
            binding.txtSubtotal.text = binding.edtPrice2.text
            binding.txtTotalBill.text = binding.edtPrice2.text
            price = bill.total
        }
    }

    fun setupActions() {
        val dollarSign = "$"
        creditNotePriceEditText = binding.edtPrice2
        creditNotePriceEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    val priceWithoutDollar = s.toString().substring(1)
                    val newPrice = priceWithoutDollar.toFloatOrNull()

                    if (newPrice != null) {
                        binding.txtSubtotal.text = "$" + String.format("%.2f", newPrice)
                        binding.txtTotalBill.text = "$" + String.format("%.2f", newPrice)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.edtPrice2.setText(dollarSign)
                    binding.edtPrice2.setSelection(binding.edtPrice2.length())
                }
            }
        })

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCreditNote.setOnClickListener {
            val bundle = intent.extras
            val bill = bundle?.getSerializable(Constants.KEY_BILL) as FACTURA
            updateInvoiceStatus(bill.id)
            finish()
        }
    }

    private fun updateInvoiceStatus(invoiceKey: String) {
        val database = FirebaseDatabase.getInstance()
        val invoicesRef = database.getReference("Factura")
        val invoiceRef = invoicesRef.child(invoiceKey)

        invoiceRef.child("estado").setValue("-1")
            .addOnSuccessListener {
                showToast("Factura anulada correctamente")
            }
            .addOnFailureListener { exception ->
                showToast("Error al actualizar la factura: ${exception.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ResourceAsColor", "Range")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.btnBack.setColorFilter(Color.parseColor("#ffffff"))
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.txtClientName.setTextColor(Color.parseColor("#ffffff"))
            binding.txtBillNumber.setTextColor(Color.parseColor("#ffffff"))
            binding.txtReason.setTextColor(Color.parseColor("#ffffff"))
            binding.txtPrice.setTextColor(Color.parseColor("#ffffff"))
            binding.edtClient.setTextColor(Color.parseColor("#ffffff"))
            binding.edtClient.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtBill.setTextColor(Color.parseColor("#ffffff"))
            binding.edtBill.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtPrice2.setTextColor(Color.parseColor("#ffffff"))
            binding.edtPrice2.setBackgroundResource(R.drawable.text_info_dark)
            binding.edtReason.setTextColor(Color.parseColor("#ffffff"))
            binding.edtReason.setBackgroundResource(R.drawable.text_info_dark)
            binding.btnMicReason.setColorFilter(Color.parseColor("#ffffff"))
            binding.txtSubtotal.setTextColor(Color.parseColor("#ffffff"))
            binding.txtIva.setTextColor(Color.parseColor("#ffffff"))
            binding.txtDiscount.setTextColor(Color.parseColor("#ffffff"))
            binding.txtTotalBill.setTextColor(Color.parseColor("#ffffff"))
            binding.btnCreditNote.setBackgroundResource(R.drawable.textdark)
            binding.btnCreditNote.setTextColor(Color.parseColor("#ffffff"))
        }
    }

    fun eventMicro() {
        binding.btnMicReason.setOnClickListener {
            SpeechToTextUtil.startSpeechToText(this, REQUEST_CODE_SPEECH_TO_TEXT1)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SPEECH_TO_TEXT1 -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        val spokenText = results[0]
                        binding.edtReason.setText(spokenText)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Error en el reconocimiento de voz.", Toast.LENGTH_SHORT).show()
        }
    }
}