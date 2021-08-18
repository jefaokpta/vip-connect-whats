package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.client.sendMediaMessage
import br.com.vipsolutions.connect.model.FileUpload
import com.google.gson.Gson
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.nio.file.Paths

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 17/08/21
 */
@RestController
@RequestMapping("/api/upload")
class FileUploadController {

    private val basePath = Paths.get("/tmp/")

    @PostMapping
    fun upload(@RequestPart("fileJson") fileJson: String, @RequestPart("file") filePartMono: Mono<FilePart>): Mono<Void> {
        val fileUpload = Gson().fromJson(fileJson, FileUpload::class.java)
        println(fileUpload.remoteJid)
        return filePartMono
            .doOnNext { fileUpload.filePath = it.filename() }
            .map { it.transferTo(basePath.resolve(it.filename())) }
            .flatMap { sendMediaMessage(fileUpload)}
            .onErrorResume{Mono.error(ResponseStatusException(HttpStatus.BAD_GATEWAY, "Pode ser que o container node esteja fora do ar."))}
    }
}