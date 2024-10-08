package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.repository.GroupRepository
import br.com.vipsolutions.connect.service.GroupService
import org.springframework.web.bind.annotation.*

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 06/06/22
 */
@RestController
@RequestMapping("/api/groups")
class GroupController(private val groupRepository: GroupRepository, private val groupService: GroupService) {
/*
    private val NOT_FOUND_MESSAGE = "Grupo não encontrado."

    @GetMapping
    fun getAllOnlyGroups() = groupRepository.findAll()

    @GetMapping("/{id}")
    fun getGroupWithContactList(@PathVariable id: Long) = groupService.getGroupWithContactList(id)
        .map(::GroupDAO)
        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE)))

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    fun new(@RequestBody group: Group) = groupService.newGroup(group)
        .onErrorResume { Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, handleMessageError(it.localizedMessage))) }

    @PutMapping
    fun update(@RequestBody group: Group) = groupService.updateGroup(group)
        .onErrorResume { Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, handleMessageError(it.localizedMessage))) }
        .switchIfEmpty { Mono.error { ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE) } }
        .then()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = groupService.deleteGroup(id)

    private fun handleMessageError(errorMessage: String): String{
        if (errorMessage.contains("groups_control_number_fk")){
            return "Aparentemente ControlNumber Invalido."
        } else if (errorMessage.contains("Duplicate entry")){
            return "Este Nome já existe, escolha outro."
        }
        return errorMessage
    }
    */
}