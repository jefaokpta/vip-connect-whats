package br.com.vipsolutions.connect.client

import br.com.vipsolutions.connect.model.*
import br.com.vipsolutions.connect.model.dto.ContactDTO
import br.com.vipsolutions.connect.model.robot.Quiz
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-26
 */
 // todo: verificar memory leak no cache de new messages
private const val CONTENT_TYPE = "Content-Type"
private const val APP_JSON = "application/json"
private const val CONTAINER_NODE = "http://localhost"

fun sendTextMessage(whatsChat: WhatsChat, contact: Contact){
    val request = HttpRequest.newBuilder(URI("$CONTAINER_NODE:${contact.instanceId}/whats/messages"))
        .POST(HttpRequest.BodyPublishers.ofString(Gson().toJson(WhatsMessage(whatsChat.text, contact.whatsapp))))
        .header(CONTENT_TYPE, APP_JSON)
        .build()
    try {
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
            println("SEND CHAT $whatsChat RETORNO ${response.statusCode()}")
        }
    }catch (ex: Exception){
        println("DEU RUIM AO ENVIAR MENSAGEM PRO NODE INSTANCE_ID ${contact.instanceId} - ${ex.message}")
    }
}

fun sendTextMessage(remoteJid: String, message: String, instance: Int){
    val json = JsonObject()
    json.addProperty("remoteJid", remoteJid)
    json.addProperty("message", message)
    val request = HttpRequest.newBuilder(URI("$CONTAINER_NODE:$instance/whats/messages"))
        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
//            .POST(HttpRequest.BodyPublishers.ofString(jacksonObjectMapper().writeValueAsString(data)))
        .header(CONTENT_TYPE, APP_JSON)
        .build()
    try {
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
            println("Enviado ${json.toString()} RETORNO ${response.statusCode()}")
        }
    }catch (ex: Exception){
        println("DEU RUIM AO ENVIAR MENSAGEM PRO NODE INSTANCE_ID $instance - ${ex.message}")
    }
}

fun sendQuizButtonsMessage(contact: Contact, quiz: Quiz) {
    val json = JsonObject()
    json.addProperty("remoteJid", contact.whatsapp)
    json.addProperty("btnText", quiz.question)
    if (!quiz.btnFooterText.isNullOrBlank()){
        json.addProperty("btnFooterText", quiz.btnFooterText)
    }
    val request = HttpRequest.newBuilder(URI("$CONTAINER_NODE:${contact.instanceId}/whats/messages/buttons"))
        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
        .header(CONTENT_TYPE, APP_JSON)
        .build()
    try {
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
            println("Enviado $json RETORNO ${response.statusCode()}")
        }
    }catch (ex: Exception){
        println("DEU RUIM AO ENVIAR BTN MSG PRO NODE INSTANCE_ID ${contact.instanceId} - ${ex.message}")
    }
}

fun sendQuizAnswerToVip(contactAndQuiz: ContactAndQuiz, selectedBtn: Int){
    val json = JsonObject()
    json.addProperty("protocol", contactAndQuiz.contact.protocol)
    json.addProperty("controlNumber", contactAndQuiz.quiz.controlNumber)
    json.addProperty("score", selectedBtn.toString())
    val request = HttpRequest.newBuilder(URI("${contactAndQuiz.quiz.urlServer}/whats-api/protocol-score"))
        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
        .header(CONTENT_TYPE, APP_JSON)
        .build()
    try {
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
            println("Enviado $json RETORNO ${response.statusCode()}")
        }
    }catch (ex: Exception){
        println("DEU RUIM AO ENVIAR QUIZ ANSWER PRO SERVER VIP - ${ex.message}")
    }
}

fun sendMediaMessage(fileUpload: FileUpload) = WebClient.builder().baseUrl("$CONTAINER_NODE:${fileUpload.instanceId}").build()
    .post()
    .uri("/whats/messages/medias")
    .header(CONTENT_TYPE, APP_JSON)
    .body(Mono.just(fileUpload), FileUpload::class.java)
    .retrieve()
    .bodyToMono(Void::class.java)
    .doFirst { println("SEND MEDIA MSG $fileUpload") }

fun checkIfContactIsOnWhatsapp(contactDTO: ContactDTO, company: Company) = WebClient.builder().baseUrl("$CONTAINER_NODE:${company.instance}").build()
    .post()
    .uri("/whats/contacts/is-on-whats")
    .header(CONTENT_TYPE, APP_JSON)
    .body(Mono.just(JsonObject().apply { addProperty("telNumber", contactDTO.whatsapp) }.toString()), String::class.java)
    .retrieve()
    .bodyToMono(Boolean::class.java)
    .map { if (it) return@map company else throw Exception("Contato não existe no Whatsapp") }
    .doFirst { println("CHECK IF CONTACT IS ON WHATSAPP ${contactDTO.whatsapp}") }

fun blockUnblockContact(contact: Contact, action: String): Mono<Contact> {
    val json = JsonObject()
    json.addProperty("remoteJid", contact.whatsapp)
    json.addProperty("action", action)

    return WebClient.builder().baseUrl("$CONTAINER_NODE:${contact.instanceId}").build()
        .post()
        .uri("/whats/contacts/block")
        .header(CONTENT_TYPE, APP_JSON)
        .body(Mono.just(json.toString()), String::class.java)
        .retrieve()
        .bodyToMono(Contact::class.java)
        .switchIfEmpty { Mono.just(contact) }
        .doFirst { println("BLOCK/UNBLOCK CONTACT $contact") }
}

