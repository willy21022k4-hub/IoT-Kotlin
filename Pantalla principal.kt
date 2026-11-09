package com.example.app_iot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter //gestion de bt
import android.bluetooth.BluetoothDevice//gestion de bt
import android.bluetooth.BluetoothSocket//gestion de bt
import android.content.Intent //Navegación y Estructura
import android.graphics.Color //Interfaz de Usuario
import android.os.Bundle
import android.view.View //Interfaz de Usuario
import android.widget.Button //Interfaz de Usuario
import android.widget.Switch //Interfaz de Usuario
import android.widget.TextView //Interfaz de Usuario
import android.widget.Toast //Interfaz de Usuario
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException //transmision de datos
import java.io.InputStream//transmision de datos
import java.io.OutputStream//transmision de datos
import java.util.UUID //gestion de bt identificador universal

class MonitorActivity : AppCompatActivity() {
    //lateinit: iniciacion tardia, no tendran un valor incial al instante

    private lateinit var tvEstadoConexion: TextView
    private lateinit var tvPPM: TextView
    private lateinit var swExtractor: Switch
    private lateinit var btnConfigurar: Button
    private lateinit var tvAlerta: TextView

    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // identificador universal
    private var bluetoothAdapter: BluetoothAdapter? = null // para buscar dispositivos
    private var bluetoothSocket: BluetoothSocket? = null // el tunel de comunicacion
    private var inputStream: InputStream? = null // recibe datos del sensor de gas
    private var outputStream: OutputStream? = null // entrega comandos al arduino
    private var isConnected = false // ve si no colapso la conexion para enviar o recibir datos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //construccion visual
        setContentView(R.layout.activity_monitor) //construir en pantalla loq ue se hizo en el activity monitor.xml
        //enlace de componentes
        tvEstadoConexion = findViewById(R.id.tvEstadoConexion) //conectamos la variables con los botones, textos e interruptores
        tvPPM = findViewById(R.id.tvPPM) //se busca el elemento por su id para que el codigo interactue con lo que el usuario hace
        swExtractor = findViewById(R.id.swExtractor)
        btnConfigurar = findViewById(R.id.btnConfigurar)
        tvAlerta = findViewById(R.id.tvAlerta)
        //navegacion
        btnConfigurar.setOnClickListener {
            val intent = Intent(this, ConfiguracionActivity::class.java)
            startActivity(intent) //para que se ejecute la configuracion si apretamos el boton
        }

        swExtractor.setOnCheckedChangeListener { _, isChecked -> //"escucha" la pantalla
            if (isChecked) {
                enviarComando("1")
                Toast.makeText(this, "Comando enviado: Extractor ON", Toast.LENGTH_SHORT).show()
            } else {
                enviarComando("0")
                Toast.makeText(this, "Comando enviado: Extractor OFF", Toast.LENGTH_SHORT).show()
            }
        }

        //conectarBluetooth()
    }

    @SuppressLint("MissingPermission") //silenciar alarma de persmisos de bt
    private fun conectarBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) { //si no se conectó
            Toast.makeText(this, "Tu dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices // pide historial de aparatos conectados
        var moduloHC05: BluetoothDevice? = null // preparo un espacio para cuando encuentre el dispositivo y lo guardo

        pairedDevices?.forEach { device -> //busca hata encontrar el hc-05
            if (device.name == "HC-05") { //CAMBIAR NOMBRE
                moduloHC05 = device // lo asigna al espacio vacio
            }
        }

        if (moduloHC05 != null) { //si el espacio no esta vacio (que encontro el modulo) entonces avanza
            try {
                bluetoothSocket = moduloHC05!!.createRfcommSocketToServiceRecord(MY_UUID) //se crea el tunel de comunicacion
                bluetoothSocket?.connect() //se ejecuta la conexion
                inputStream = bluetoothSocket?.inputStream // via de entrada
                outputStream = bluetoothSocket?.outputStream // via de salida
                isConnected = true

                tvEstadoConexion.text = "🟢 Conectado al Sistema"
                tvEstadoConexion.setTextColor(Color.parseColor("#27AE60"))
                recibirDatosDelArduino() // empezamos a recibir datos (las PPM)

            } catch (e: IOException) { //si no encuentra el modulo
                e.printStackTrace()
                tvEstadoConexion.text = "🔴 Error de conexión"
            }
        } else {
            tvEstadoConexion.text = "🔴 HC-05 no encontrado"
            Toast.makeText(this, "Empareja el HC-05 en los ajustes de tu teléfono", Toast.LENGTH_LONG).show()
        }
    }
    //"escucha" los niveles de gas de la placa
    private fun recibirDatosDelArduino() {
        val buffer = ByteArray(1024)
        var bytes: Int

        Thread { //hilo de trabajo secundario
            while (isConnected) {
                try {
                    bytes = inputStream?.read(buffer) ?: 0
                    if (bytes > 0) {
                        val mensajeLeido = String(buffer, 0, bytes)
                        runOnUiThread { // mensajero que toma el numero del gas y se los entrega a actualizacolores...
                            actualizarColoresPPM(mensajeLeido)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    isConnected = false
                    break
                }
            }
        }.start()
    }

    private fun actualizarColoresPPM(valorRecibido: String) {
        try {
            //trimp para limpiar el valor
            //toInt para convertir de string a int
            val ppm = valorRecibido.trim().toInt()
            tvPPM.text = "$ppm PPM"

            when {
                ppm < 300 -> {
                    tvPPM.setTextColor(Color.parseColor("#27AE60"))
                    tvAlerta.visibility = View.GONE
                }
                ppm in 300..600 -> {
                    tvPPM.setTextColor(Color.parseColor("#F1C40F"))
                    tvAlerta.visibility = View.GONE
                }
                else -> {
                    tvPPM.setTextColor(Color.parseColor("#E74C3C"))
                    tvAlerta.visibility = View.VISIBLE
                    tvAlerta.text = "¡ALERTA! Concentración de gas crítica"
                }
            }
        } catch (e: NumberFormatException) {
            // Ignorar lecturas incompletas
        }
    }

    private fun enviarComando(comando: String) {
        if (isConnected && outputStream != null) { //que este conectado y que exista el tunel
            try {
                outputStream?.write(comando.toByteArray()) //transforma de string a bytes
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() { //para evitar consumo innecesario de recursos despues de cerrar la app
        super.onDestroy()
        try {
            bluetoothSocket?.close()
            isConnected = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
