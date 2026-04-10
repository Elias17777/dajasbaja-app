package com.familiamart.dajasbaja

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        supportActionBar?.apply {
            title = getString(R.string.config_title)
            setDisplayHomeAsUpEnabled(true)
        }

        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSave    = findViewById<Button>(R.id.btnSave)

        // Cargar valores guardados (si existen)
        val prefs = getEncryptedPrefs()
        etEmail.setText(prefs.getString("email", ""))
        // No precargamos la contraseña por seguridad

        btnSave.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty()) {
                etEmail.error = "Introduce el email remitente"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Email no válido"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Introduce la contraseña"
                return@setOnClickListener
            }

            prefs.edit()
                .putString("email", email)
                .putString("password", password)
                .apply()

            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
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
