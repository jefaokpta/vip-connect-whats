package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-05
 */
interface UserRepository: ReactiveCrudRepository<User, Long> {
}