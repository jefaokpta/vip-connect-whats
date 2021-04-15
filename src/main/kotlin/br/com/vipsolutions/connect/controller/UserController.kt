package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.User
import br.com.vipsolutions.connect.model.dto.UserDTO
import br.com.vipsolutions.connect.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-05
 */
@RestController
@RequestMapping("/api/users")
class UserController(private val userRepository: UserRepository) {

    @GetMapping // LIMITE 20
    fun getAll() = userRepository.findAll()
        .filter{!it.deleted}
        .map(::UserDTO)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = userRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")))
        .map(::UserDTO)

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    fun save(@RequestBody userDTO: UserDTO) = userRepository.save(User(userDTO))
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}

    @PutMapping
    fun update(@RequestBody userDTO: UserDTO) = userRepository.findById(userDTO.id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")))
        .flatMap { userRepository.save(User(it, userDTO)) }
        .onErrorResume{error -> Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, error.message))}
        .flatMap{Mono.empty<Void>()}

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = userRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")))
        .flatMap{userRepository.save(it.apply { deleted = true })}
        .flatMap{Mono.empty<Void>()}

}