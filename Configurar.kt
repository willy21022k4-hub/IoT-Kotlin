package com.example.app_iot

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion) //ir a buscar el diseño del .xml y ajecutarlo

        // 1. Enlazamos los componentes del XML con los IDs correctos
        val sbVelocidadMotor = findViewById<SeekBar>(R.id.seekBarRPM)
        val swAlertaSonora = findViewById<Switch>(R.id.switchBuzzer)
        val swAlertaVisual = findViewById<Switch>(R.id.switchLED)

        // Componentes nuevos para el mensaje del LCD
        val etMensaje = findViewById<EditText>(R.id.etMensajePersonalizado)
        val switchMensaje = findViewById<Switch>(R.id.switchActivarMensaje)

        val btnGuardarConfig = findViewById<Button>(R.id.btnGuardarVolver)

        // 2. Lógica del Slider (SeekBar) para controlar la velocidad
        sbVelocidadMotor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Aquí el progress es un número del 0 al 100
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 3. Lógica del botón Guardar y Volver
        btnGuardarConfig.setOnClickListener {
            val mensaje = etMensaje.text.toString()
            val activarMensaje = switchMensaje.isChecked

            // Simulación del envío de mensaje para la pantalla LCD
            if (activarMensaje && mensaje.isNotEmpty()) {
                Toast.makeText(this, "Mensaje enviado a LCD: $mensaje", Toast.LENGTH_SHORT).show()
            } else if (!activarMensaje) {
                Toast.makeText(this, "Mensaje LCD desactivado", Toast.LENGTH_SHORT).show()
            }

            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
            finish() // Cierra esta ventana y regresa al MonitorActivity
        }
    }
}
