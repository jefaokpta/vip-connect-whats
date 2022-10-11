package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.client.sendMediaMessage
import br.com.vipsolutions.connect.model.FileUpload
import br.com.vipsolutions.connect.service.GroupService
import br.com.vipsolutions.connect.util.EnvironmentVarCenter
import com.google.gson.Gson
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.nio.file.Paths

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 17/08/21
 */
@RestController
@RequestMapping("/api/upload")
class FileUploadController(private val groupService: GroupService) {

    private val basePath = Paths.get(EnvironmentVarCenter.environmentVar.uploadedFileFolder!!)

    @CrossOrigin
    @PostMapping
    fun upload(@RequestPart("fileJson") fileJson: String, @RequestPart("file") filePartMono: Mono<FilePart>): Mono<Void> {
        val fileUpload = Gson().fromJson(fileJson, FileUpload::class.java)
        println(fileJson)
        return filePartMono
            .doOnNext { fileUpload.filePath = it.filename() }
            .delayUntil { it.transferTo(basePath.resolve(it.filename())) }
            .flatMap { sendMediaMessage(fileUpload) }
            .onErrorResume { Mono.error(ResponseStatusException(HttpStatus.BAD_GATEWAY, "${it.message} - Pode ser que o container node esteja fora do ar.")) }
    }

    @CrossOrigin
    @PostMapping("/list")
    fun mediaGroupMessage(@RequestPart("fileJson") fileJson: String, @RequestPart("file") filePartMono: Mono<FilePart>): Mono<Void> {
        println(fileJson)
        val fileUpload = Gson().fromJson(fileJson, FileUpload::class.java)
        if(fileUpload.messageGroupId == null) {
            return Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "O id do grupo não pode ser nulo"))
        }
        return filePartMono
            .doOnNext { fileUpload.filePath = it.filename() }
            .delayUntil { it.transferTo(basePath.resolve(it.filename())) }
            .flatMap { groupService.sendGroupMessage(fileUpload) }
            .then()
            .onErrorResume { Mono.error(ResponseStatusException(HttpStatus.BAD_GATEWAY, "${it.message} - Pode ser que o container node esteja fora do ar.")) }
    }

}