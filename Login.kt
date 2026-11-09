package com.example.app_iot


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Enlazar el código con los elementos de tu diseño XML
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)

        // 2. Definir qué pasa cuando el usuario presiona el botón "ENTRAR"
        btnEntrar.setOnClickListener {
            val usuario = etUsuario.text.toString()
            val password = etPassword.text.toString()

            // 3. Validación de seguridad básica (Simulando un login OT real)
            if (usuario == "admin" && password == "1234") {
                // Si es correcto, abre la Pantalla de Monitoreo
                val intent = Intent(this, MonitorActivity::class.java)
                startActivity(intent)
                finish() // Cierra la pantalla de login para que no se pueda volver atrás con el botón del celular
            } else {
                // Si es incorrecto, muestra un mensaje de error en pantalla
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
