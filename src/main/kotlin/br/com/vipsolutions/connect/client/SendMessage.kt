package br.com.vipsolutions.connect.client

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-26
 */

fun sendTextMessage(data: String, instance: Int){
    val request = HttpRequest.newBuilder(URI("http://localhost:$instance/whats/messages"))
        .POST(HttpRequest.BodyPublishers.ofString(data))
//            .POST(HttpRequest.BodyPublishers.ofString(jacksonObjectMapper().writeValueAsString(data)))
        .header("Content-Type", "application/json")
        .build()
    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
        println("Enviado $data RETORNO ${response.statusCode()}")
    }
}

fun getMessage() {
    val request = HttpRequest.newBuilder(URI("http://localhost/services/receive-call"))
        .GET().build()
    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
        println(response.body())
//            val pessoa = jacksonObjectMapper().writeValueAsString(Person("Local", 23))
//            println(pessoa)
//            val p = jacksonObjectMapper().readValue(response.body(), Person::class.java)
//            println("IDADE ${p.age} NOME ${p.name}")
    }
}