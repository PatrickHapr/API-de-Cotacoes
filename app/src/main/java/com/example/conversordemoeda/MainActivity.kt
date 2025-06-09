package com.example.conversordemoeda

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvSaldoBRL: TextView
    private lateinit var tvSaldoUSD: TextView
    private lateinit var tvSaldoBTC: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSaldoBRL = findViewById(R.id.tvSaldoBRL)
        tvSaldoUSD = findViewById(R.id.tvSaldoUSD)
        tvSaldoBTC = findViewById(R.id.tvSaldoBTC)
    }

    override fun onResume() {
        super.onResume()
        tvSaldoBRL.text = "R$: %.2f".format(Carteira.saldoBRL)
        tvSaldoUSD.text = "USD: %.2f".format(Carteira.saldoUSD)
        tvSaldoBTC.text = "BTC: %.4f".format(Carteira.saldoBTC)
    }

    fun abrirConversor(view: View) {
        startActivity(Intent(this, ConverterActivity::class.java))
    }
}
