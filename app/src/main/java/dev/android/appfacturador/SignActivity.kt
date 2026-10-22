package dev.android.appfacturador

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dev.android.appfacturador.databinding.ActivitySignBinding

class SignActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignBinding
    private lateinit var storageReference: StorageReference
    private val storage_path = "firma/*"
    private val progressDialog: ProgressDialog? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val window = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(window)
        val width = window.widthPixels

        getWindow().setLayout(width, 700)
        getWindow().decorView.setBackgroundResource(android.R.color.transparent)
        getWindow().setGravity(Gravity.BOTTOM)

        storageReference = FirebaseStorage.getInstance().reference

        val loadImage =
            registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
                binding.txtSignName.text = it?.let { it1 -> getName(it1, this) }
                if (it != null) {
                    uploadImage(it)
//                    binding.imgSign.setImageURI(it)
                }
            })

        binding.btnUpload.setOnClickListener {
            loadImage.launch("application/x-pkcs12")
        }

        darkMode()
    }

    private fun uploadImage(imageUrl: Uri) {
        val storagePath = "$storage_path firma_electronica${System.currentTimeMillis()}.p12"
        val reference: StorageReference = storageReference.child(storagePath)

        reference.putFile(imageUrl)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                }.addOnFailureListener { exception ->
                    showToast("Error al obtener la URL de descarga")
                    progressDialog?.dismiss()
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error al cargar la firma")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@SignActivity, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("Range")
    fun getName(uri: Uri, context: Context): String? {
        var result: String? = null
        if (uri != null) {
            if (uri.scheme.equals("content")) {
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor?.close()
                }
                if (result == null) {
                    result = uri.path.toString()
                    val cutt = result.lastIndexOf('/')
                    if (cutt != -1) {
                        result = result.substring(cutt + 1)
                    }
                }
            }
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ResourceAsColor", "ResourceType")
    fun darkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        // Comprueba el modo actual
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El modo actual es dark
            binding.txtTitle.setTextColor(Color.parseColor("#ffffff"))
            val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.loaddark)
            binding.imgSign.setImageDrawable(drawable)
            binding.imgSign.alpha = 0.6f
            binding.btnProfile.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.cardSign.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.btnUpload.setCardBackgroundColor(Color.parseColor("#121212"))
            binding.btnUp.setColorFilter(Color.parseColor("#ffffff"))
        }
    }
}