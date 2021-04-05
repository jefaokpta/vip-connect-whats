package br.com.vipsolutions.connect.controller

import br.com.vipsolutions.connect.model.User
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

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = userRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")))

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    fun save(@RequestBody user: User) = userRepository.save(user)

    @PutMapping
    fun update(@RequestBody user: User) = userRepository.findById(user.id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")))
        .flatMap { userDb ->
            if(user.passwordHash.isBlank()) user.passwordHash = userDb.passwordHash
            user.updatedAt = LocalDateTime.now()
            userRepository.save(user)
        }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = userRepository.findById(id)
        .switchIfEmpty( Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.")))
        .flatMap { userRepository.deleteById(id) }

}