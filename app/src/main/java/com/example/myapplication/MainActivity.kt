/*
    Raphael Ferreira Regner
    Curso Técnico em Eletrônica Integrado
    7º Semestre
    IFSUL - Câmpus Pelotas
 */

package com.example.myapplication

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import kotlinx.coroutines.Runnable

class MainActivity : AppCompatActivity() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var barraBatNivel: ProgressBar
    private lateinit var barraUsoRAM: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        barraBatNivel = findViewById(R.id.barraBat)
        barraUsoRAM = findViewById(R.id.barraRAM)

        //Modifica a cor das barras de status e navegação do sistema (Acima do Android 5.0(Lollipop)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = getWindow()
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.fundo)
            window.navigationBarColor = resources.getColor(R.color.fundo)
        }

        //Executa o loop
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            printBatNivel()
            printBatVolts()
            printBatTemp()
            printCPUTemp()
            printRAMUso()

            //Oculta a barra de navegação
            val decorView: View = getWindow().getDecorView()
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

            handler.postDelayed(runnable, 2000) //Define o intervalo entre execuções do loop
        }

        handler.post(runnable)
    }

    //Encerra o loop
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }



    //Nível Bateria
    fun leBatNivel(context: Context): Int {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)

        //Lê nível bateria
        val percBat = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        return percBat
    }

    fun printBatNivel() {
        val textoBatNivel = findViewById<TextView>(R.id.textBatNivel)
        val batNivel = leBatNivel(this)
        textoBatNivel.text = "Nível: $batNivel %"

        barraBatNivel.max = 100
        barraBatNivel.progress = batNivel
    }



    //Tensão Bateria
    fun leBatVolts(context: Context): Float {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)

        //Lê tensão bateria
        val voltsBat = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        return voltsBat / 1000f
    }

    fun printBatVolts() {
        val textoBatTens = findViewById<TextView>(R.id.textBatTens)
        val batTens = leBatVolts(this)
        textoBatTens.text = "Tensão: $batTens V"
    }



    //Temperatura Bateria
    fun leBatTemp(context: Context): Float {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        //Lê temperatura bateria
        val tempBat = batteryStatus?.getIntExtra (BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        //Converte temperatura
        val tempCelsius = tempBat / 10f
        return tempCelsius
    }

    fun printBatTemp() {
        val textoBatTemp = findViewById<TextView>(R.id.textBatTemp)
        val batTemp = leBatTemp(this)
        textoBatTemp.text = "Temperatura: $batTemp ºC"
    }



    //Temperatura CPU
    fun leCPUTemp(): Float {
        var temperatura = Float.NaN
        try {
            val leitura = File("/sys/class/thermal/thermal_zone0/temp")
            if (leitura.exists()) {
                val leitor = BufferedReader(FileReader(leitura))
                val linha = leitor.readLine()
                if (linha != null) {
                    temperatura = linha.toFloat() / 1000.0f
                }
                leitor.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return temperatura
    }

    fun printCPUTemp() {
        val textoCPUTemp = findViewById<TextView>(R.id.textCPUTemp)
        val cpuTemp = leCPUTemp()
        textoCPUTemp.text = "Temperatura: $cpuTemp ºC"
    }
    /*Fontes
    developer.android.com/reference/kotlin/java/io/FileReader
     */



    //RAM Uso
    fun leRAMUso (context: Context): String {
        val activityManager = context.getSystemService (Context.ACTIVITY_SERVICE) as ActivityManager
        val infoRAM = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(infoRAM)

        val totalRAM = infoRAM.totalMem
        val dispoRAM = infoRAM.availMem
        val usadoRAM = totalRAM - dispoRAM

        //Converte B para MB
        val textRAMUso = usadoRAM / (1048576L)
        val textRAMTotal = totalRAM / (1048576L)

        barraUsoRAM.max = textRAMTotal.toInt()
        barraUsoRAM.progress = textRAMUso.toInt()

        return "Uso: $textRAMUso MB / $textRAMTotal MB"
    }

    fun printRAMUso() {
        val textoRAMUso = findViewById<TextView>(R.id.textRAMUso)
        textoRAMUso.text = leRAMUso(this)
    }
    /*Fontes:
    developer.android.com/reference/android/app/ActivityManager.MemoryInfo
     */
}