package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.Group
import br.com.vipsolutions.connect.repository.GroupRepository
import br.com.vipsolutions.connect.service.GroupService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 06/06/22
 */
@RestController
@RequestMapping("/api/groups")
class GroupController(private val groupRepository: GroupRepository, private val groupService: GroupService) {

    private val NOT_FOUND_MESSAGE = "Grupo não encontrado."

    @GetMapping
    fun getAllOnlyGroups() = groupRepository.findAll()

    @GetMapping("/{id}")
    fun getGroupWithContactList(@PathVariable id: Long) = groupService.getGroupWithContactList(id)

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    fun new(@RequestBody group: Group) = groupRepository.save(group)
        .onErrorResume { Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, handleMessageError(it.localizedMessage))) }

    @PutMapping
    fun update(@RequestBody group: Group) = groupRepository.findById(group.id)
        .flatMap{groupRepository.save(group)}
        .onErrorResume { Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, handleMessageError(it.localizedMessage))) }
        .switchIfEmpty { Mono.error { ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE) } }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = groupRepository.findById(id)
        .switchIfEmpty { Mono.error { ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE) } }
        .flatMap(groupRepository::delete)

    private fun handleMessageError(errorMessage: String): String{
        if (errorMessage.contains("groups_control_number_fk")){
            return "Aparentemente ControlNumber Invalido."
        } else if (errorMessage.contains("Duplicate entry")){
            return "Este Nome já existe, escolha outro."
        }
        return errorMessage
    }
}