package com.example.conversordemoeda

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface AwesomeApiService {
    @GET("{moedaOrigem}-{moedaDestino}")
    fun getCotacao(
        @Path("moedaOrigem") origem: String,
        @Path("moedaDestino") destino: String
    ): Call<Map<String, CotacaoResponse>>
}