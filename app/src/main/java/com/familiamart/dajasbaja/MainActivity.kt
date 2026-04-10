package com.familiamart.dajasbaja

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    // -------------------------------------------------------------------------
    // Modelo de un producto: Uri de la foto + valor de Kg + vista asignada
    // -------------------------------------------------------------------------
    data class ProductEntry(
        var photoUri: Uri? = null,
        var view: View? = null
    )

    private val products = mutableListOf<ProductEntry>()
    private var currentProductIndex = -1          // índice activo al pedir foto
    private var tempCameraFile: File? = null       // fichero temporal de cámara

    private lateinit var productsContainer: LinearLayout
    private lateinit var btnCocina: Button
    private lateinit var btnRecepciones: Button
    private var selectedDepartment = "Cocina"

    // -------------------------------------------------------------------------
    // Launchers de Activity Result (deben registrarse antes de onCreate)
    // -------------------------------------------------------------------------
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { assignPhoto(currentProductIndex, it) }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                tempCameraFile?.let { file ->
                    val uri = FileProvider.getUriForFile(
                        this, "${packageName}.provider", file
                    )
                    assignPhoto(currentProductIndex, uri)
                }
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera() else
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }

    // -------------------------------------------------------------------------
    // onCreate
    // -------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si no hay credenciales configuradas → pantalla de ajustes primero
        if (!isConfigured()) {
            startActivity(Intent(this, ConfigActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        productsContainer = findViewById(R.id.productsContainer)
        btnCocina         = findViewById(R.id.btnCocina)
        btnRecepciones    = findViewById(R.id.btnRecepciones)

        // Toggle departamento
        btnCocina.setOnClickListener      { selectDepartment("Cocina") }
        btnRecepciones.setOnClickListener { selectDepartment("Recepciones") }
        selectDepartment("Cocina")

        // Añadir primer producto por defecto
        addProduct()

        // Botón añadir producto
        findViewById<Button>(R.id.btnAddProduct).setOnClickListener { addProduct() }

        // Botón enviar
        findViewById<Button>(R.id.btnSend).setOnClickListener { validateAndSend() }

        // Botón ajustes (icono en la esquina)
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
        }
    }

    // -------------------------------------------------------------------------
    // Selección de departamento (toggle visual)
    // -------------------------------------------------------------------------
    private fun selectDepartment(dept: String) {
        selectedDepartment = dept
        if (dept == "Cocina") {
            btnCocina.setBackgroundResource(R.drawable.bg_btn_selected)
            btnCocina.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnRecepciones.setBackgroundResource(R.drawable.bg_btn_unselected)
            btnRecepciones.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
        } else {
            btnRecepciones.setBackgroundResource(R.drawable.bg_btn_selected)
            btnRecepciones.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnCocina.setBackgroundResource(R.drawable.bg_btn_unselected)
            btnCocina.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
        }
    }

    // -------------------------------------------------------------------------
    // Añadir fila de producto dinámicamente
    // -------------------------------------------------------------------------
    private fun addProduct() {
        val index = products.size
        val entry = ProductEntry()
        products.add(entry)

        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_product, productsContainer, false)
        entry.view = view

        view.findViewById<TextView>(R.id.tvProductLabel).text = "Producto ${index + 1}"

        // Botón seleccionar foto
        view.findViewById<Button>(R.id.btnSelectPhoto).setOnClickListener {
            currentProductIndex = index
            showPhotoSourceDialog()
        }

        // Botón eliminar (el primero no se puede eliminar)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDeleteProduct)
        if (index == 0) {
            btnDelete.visibility = View.GONE
        } else {
            btnDelete.setOnClickListener { removeProduct(index) }
        }

        productsContainer.addView(view)

        // Scroll al nuevo producto
        val scroll = findViewById<ScrollView>(R.id.scrollView)
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    // -------------------------------------------------------------------------
    // Eliminar producto y re-etiquetar los restantes
    // -------------------------------------------------------------------------
    private fun removeProduct(index: Int) {
        if (index < 0 || index >= products.size) return
        productsContainer.removeView(products[index].view)
        products.removeAt(index)
        relabelProducts()
    }

    private fun relabelProducts() {
        for (i in products.indices) {
            products[i].view?.findViewById<TextView>(R.id.tvProductLabel)?.text =
                "Producto ${i + 1}"
        }
    }

    // -------------------------------------------------------------------------
    // Diálogo: cámara o galería
    // -------------------------------------------------------------------------
    private fun showPhotoSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Seleccionar foto")
            .setItems(arrayOf("📷  Cámara", "🖼️  Galería")) { _, which ->
                if (which == 0) checkCameraPermissionAndLaunch() else galleryLauncher.launch("image/*")
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile(
            "photo_${System.currentTimeMillis()}", ".jpg", cacheDir
        )
        tempCameraFile = photoFile
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        cameraLauncher.launch(uri)
    }

    // -------------------------------------------------------------------------
    // Asignar foto a un producto y mostrar preview
    // -------------------------------------------------------------------------
    private fun assignPhoto(index: Int, uri: Uri) {
        if (index < 0 || index >= products.size) return
        products[index].photoUri = uri

        val view = products[index].view ?: return
        view.findViewById<ImageView>(R.id.imgPreview).apply {
            setImageURI(uri)
            visibility = View.VISIBLE
        }
        view.findViewById<Button>(R.id.btnSelectPhoto).text = "Cambiar foto"
    }

    // -------------------------------------------------------------------------
    // Validar y enviar email
    // -------------------------------------------------------------------------
    private fun validateAndSend() {
        // Validar que todos los productos tienen foto y Kg
        val kgValues = mutableListOf<String>()
        for ((i, product) in products.withIndex()) {
            val view = product.view ?: continue

            if (product.photoUri == null) {
                Toast.makeText(this, "El Producto ${i + 1} no tiene foto", Toast.LENGTH_SHORT).show()
                return
            }

            val kg = view.findViewById<EditText>(R.id.etKg).text.toString().trim()
            if (kg.isEmpty()) {
                Toast.makeText(this, "Introduce los Kg del Producto ${i + 1}", Toast.LENGTH_SHORT).show()
                return
            }
            kgValues.add(kg)
        }

        if (products.isEmpty()) {
            Toast.makeText(this, "Añade al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener credenciales cifradas
        val prefs    = getEncryptedPrefs()
        val email    = prefs.getString("email", "") ?: ""
        val password = prefs.getString("password", "") ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Configura las credenciales primero", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ConfigActivity::class.java))
            return
        }

        val photoUris = products.map { it.photoUri!! }

        // Diálogo de progreso
        val progress = AlertDialog.Builder(this)
            .setTitle("Enviando pedido…")
            .setMessage("Por favor espera")
            .setCancelable(false)
            .create()
        progress.show()

        // Enviar en background (coroutine)
        lifecycleScope.launch {
            try {
                EmailSender.sendEmail(
                    context       = this@MainActivity,
                    smtpHost      = "smtp.ethereal.email",
                    smtpPort      = 587,
                    fromEmail     = email,
                    password      = password,
                    toEmails      = listOf("procesos@platostradicionales.com"),
                    department    = selectedDepartment,
                    kgValues      = kgValues,
                    photoUris     = photoUris
                )
                progress.dismiss()
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("✓ Correo enviado")
                    .setMessage("Correo capturado por el servidor de pruebas.\n\nPara verlo: abre ethereal.email → Messages (sesión rhoda.hand@ethereal.email).\n\nCuando uses el servidor definitivo llegará directamente a procesos@platostradicionales.com")
                    .setPositiveButton("Nuevo pedido") { _, _ -> resetForm() }
                    .setCancelable(false)
                    .show()
            } catch (e: Exception) {
                progress.dismiss()
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Error al enviar")
                    .setMessage(e.message ?: "Error desconocido. Comprueba las credenciales y la conexión.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    // -------------------------------------------------------------------------
    // Resetear formulario tras envío exitoso
    // -------------------------------------------------------------------------
    private fun resetForm() {
        productsContainer.removeAllViews()
        products.clear()
        selectDepartment("Cocina")
        addProduct()
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private fun isConfigured(): Boolean {
        val email = getEncryptedPrefs().getString("email", "") ?: ""
        return email.isNotEmpty()
    }

    private fun getEncryptedPrefs(): android.content.SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "app_prefs",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
