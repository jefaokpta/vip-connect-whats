package br.com.vipsolutions.connect.client

import br.com.vipsolutions.connect.model.WhatsProfilePicture
import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-29
 */

fun getProfilePicture(instanceId: Int, remoteJid: String): WhatsProfilePicture {
    val request = HttpRequest.newBuilder(URI("http://localhost:$instanceId/whats/profile/picture/$remoteJid"))
        .GET().timeout(Duration.ofSeconds(5)).build()
    try {
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
            return Gson().fromJson(response.body(), WhatsProfilePicture::class.java)
        }
    }catch (ex: Exception){
        println("DEU RUIM AO BUSCAR IMAGEM DO PERFIL INSTANCE_ID $instanceId - ${ex.message}")
        return WhatsProfilePicture(null, ex.message)
    }
}