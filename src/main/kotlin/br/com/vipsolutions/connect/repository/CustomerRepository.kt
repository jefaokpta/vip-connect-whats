package br.com.vipsolutions.connect.repository

import br.com.vipsolutions.connect.model.Customer
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-07
 */
interface CustomerRepository: ReactiveCrudRepository<Customer, Long> {
}