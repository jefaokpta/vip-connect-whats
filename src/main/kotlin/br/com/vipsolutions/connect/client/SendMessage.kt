package br.com.vipsolutions.connect.client

import br.com.vipsolutions.connect.model.Contact
import br.com.vipsolutions.connect.model.FileUpload
import br.com.vipsolutions.connect.model.WhatsChat
import br.com.vipsolutions.connect.model.WhatsMessage
import br.com.vipsolutions.connect.model.robot.Greeting
import br.com.vipsolutions.connect.model.robot.Ura
import com.google.gson.Gson
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import reactor.core.publisher.Mono
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-26
 */

fun sendTextMessage(whatsChat: WhatsChat, contact: Contact){
    val request = HttpRequest.newBuilder(URI("http://localhost:${contact.instanceId}/whats/messages"))
        .POST(HttpRequest.BodyPublishers.ofString(Gson().toJson(WhatsMessage(whatsChat.text, contact.whatsapp))))
        .header("Content-Type", "application/json")
        .build()
    try {
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
            println("ENVIANDO MENSAGEM RETORNO ${response.statusCode()}")
        }
    }catch (ex: Exception){
        println("DEU RUIM AO ENVIAR MENSAGEM PRO NODE INSTANCE_ID ${contact.instanceId} - ${ex.message}")
    }
}

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

fun sendMediaMessage(fileUpload: FileUpload) = WebClient.builder().baseUrl("http://localhost:${fileUpload.instanceId}").build()
        .post()
        .uri("/whats/messages/medias")
        .header("Content-Type", "application/json")
        .body(Mono.just(fileUpload), FileUpload::class.java)
        .retrieve()
        .bodyToMono(Void::class.java)

fun getRobotUra(company: Long) = WebClient.builder().baseUrl("http://localhost:8081").build()
    .get()
    .uri("/robot/ura/$company")
    .header("Content-Type", "application/json")
    .retrieve()
    .bodyToMono(Ura::class.java)
    .onErrorResume { Mono.empty() }

fun getRobotGreeting(company: Long) = WebClient.builder().baseUrl("http://localhost:8081").build()
    .get()
    .uri("/robot/greeting/$company")
    .header("Content-Type", "application/json")
    .retrieve()
    .bodyToMono(Greeting::class.java)
    .onErrorResume { Mono.empty() }