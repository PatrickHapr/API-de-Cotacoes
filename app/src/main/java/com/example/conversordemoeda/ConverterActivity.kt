// ConverterActivity.kt - Versão Corrigida
package com.example.conversordemoeda

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback

class ConverterActivity : AppCompatActivity() {

    private lateinit var spOrigem: Spinner
    private lateinit var spDestino: Spinner
    private lateinit var etValor: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultado: TextView
    private lateinit var api: AwesomeApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter)

        spOrigem = findViewById(R.id.spMoedaOrigem)
        spDestino = findViewById(R.id.spMoedaDestino)
        etValor = findViewById(R.id.etValor)
        progressBar = findViewById(R.id.progressBar)
        tvResultado = findViewById(R.id.tvResultado)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://economia.awesomeapi.com.br/json/last/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AwesomeApiService::class.java)

        val moedas = arrayOf("BRL", "USD", "BTC")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, moedas)
        spOrigem.adapter = adapter
        spDestino.adapter = adapter
    }

    fun converterMoeda(view: View) {
        val origem = spOrigem.selectedItem.toString()
        val destino = spDestino.selectedItem.toString()

        if (origem == destino) {
            Toast.makeText(this, "Escolha moedas diferentes", Toast.LENGTH_SHORT).show()
            return
        }

        val valor = etValor.text.toString().toDoubleOrNull()
        if (valor == null || valor <= 0.0) {
            Toast.makeText(this, "Informe um valor válido", Toast.LENGTH_SHORT).show()
            return
        }

        val saldo = when (origem) {
            "BRL" -> Carteira.saldoBRL
            "USD" -> Carteira.saldoUSD
            "BTC" -> Carteira.saldoBTC
            else -> 0.0
        }

        if (valor > saldo) {
            Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        when {
            origem == "USD" && destino == "BRL" -> buscarCotacao("USD", "BRL", valor, false)
            origem == "BRL" && destino == "USD" -> buscarCotacao("USD", "BRL", valor, true)
            origem == "BTC" && destino == "BRL" -> buscarCotacao("BTC", "BRL", valor, false)
            origem == "BTC" && destino == "USD" -> buscarCotacao("BTC", "USD", valor, false)
            origem == "USD" && destino == "BTC" -> buscarCotacao("BTC", "USD", valor, true)
            origem == "BRL" && destino == "BTC" -> buscarCotacao("BTC", "BRL", valor, true)
        }
    }


    private fun buscarCotacao(moedaApi1: String, moedaApi2: String, valor: Double, inverter: Boolean) {
        val par = "$moedaApi1$moedaApi2"

        api.getCotacao(moedaApi1, moedaApi2).enqueue(object : Callback<Map<String, CotacaoResponse>> {
            override fun onResponse(
                call: Call<Map<String, CotacaoResponse>>,
                response: Response<Map<String, CotacaoResponse>>
            ) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val cotacao = response.body()?.get(par)
                    val taxa = cotacao?.bid?.toDoubleOrNull()

                    if (taxa != null) {
                        val convertido = if (inverter) {
                            valor / taxa
                        } else {
                            valor * taxa
                        }

                        val origemReal = if (inverter) moedaApi2 else moedaApi1
                        val destinoReal = if (inverter) moedaApi1 else moedaApi2

                        atualizarSaldos(origemReal, destinoReal, valor, convertido)
                        Toast.makeText(
                            this@ConverterActivity,
                            "Convertido: %.6f $destinoReal".format(convertido),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(this@ConverterActivity, "Erro ao processar taxa", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ConverterActivity, "Falha ao obter resposta da API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, CotacaoResponse>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ConverterActivity, "Erro na conversão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun atualizarSaldos(origem: String, destino: String, valorOrigem: Double, valorDestino: Double) {
        when (origem) {
            "BRL" -> Carteira.saldoBRL -= valorOrigem
            "USD" -> Carteira.saldoUSD -= valorOrigem
            "BTC" -> Carteira.saldoBTC -= valorOrigem
        }
        when (destino) {
            "BRL" -> Carteira.saldoBRL += valorDestino
            "USD" -> Carteira.saldoUSD += valorDestino
            "BTC" -> Carteira.saldoBTC += valorDestino
        }
    }
}