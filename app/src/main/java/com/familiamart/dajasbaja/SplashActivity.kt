package com.familiamart.dajasbaja

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultar ActionBar en la splash
        supportActionBar?.hide()

        setContentView(R.layout.activity_splash)

        val content  = findViewById<View>(R.id.splashContent)
        val tvChef   = findViewById<TextView>(R.id.tvChef)
        val tvFire   = findViewById<TextView>(R.id.tvFire)
        val tvBrand  = findViewById<TextView>(R.id.tvBrand)

        // --- Secuencia de animación ---

        // 1. El contenido aparece con fade + slide-up
        content.translationY = 80f
        content.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(700)
            .setStartDelay(200)
            .start()

        // 2. El chef hace "pop" (escala de 0.5 → 1.1 → 1.0)
        tvChef.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(500)
            .setStartDelay(400)
            .withEndAction {
                tvChef.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()

        // 3. Las llamas aparecen con pulso repetido
        tvFire.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(700)
            .withEndAction { pulseFire(tvFire) }
            .start()

        // 4. Marca inferior aparece al final
        tvBrand.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(900)
            .start()

        // 5. Navegar a la siguiente pantalla tras 2.8 segundos
        content.postDelayed({ navigateNext() }, 2800)
    }

    /** Efecto de pulso en las llamas (loop) */
    private fun pulseFire(view: TextView) {
        view.animate()
            .alpha(0.5f)
            .setDuration(500)
            .withEndAction {
                view.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .withEndAction { pulseFire(view) }
                    .start()
            }
            .start()
    }

    private fun navigateNext() {
        // Autoconfigurar credenciales de prueba si aún no hay ninguna
        autoConfigureIfNeeded()
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    /**
     * En el primer arranque guarda automáticamente las credenciales de prueba
     * (Ethereal Email — servidor SMTP de test, los correos se ven en ethereal.email/messages)
     */
    private fun autoConfigureIfNeeded() {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                "app_prefs",
                masterKeyAlias,
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            if ((prefs.getString("email", "") ?: "").isEmpty()) {
                prefs.edit()
                    .putString("email",    "rhoda.hand@ethereal.email")
                    .putString("password", "NWKgJy8DD4fsBurXfF")
                    .apply()
            }
        } catch (_: Exception) { }
    }
}
