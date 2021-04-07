package br.com.vipsolutions.connect.model

import br.com.vipsolutions.connect.model.util.CustomerEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-07
 */
@Table("customers")
data class Customer(
    @Id
    val id: Long,
    val companyId: Long,
    val fullName: String,
    val fantasyName: String,
    val entity: CustomerEntity,
    val rg: String,
    val cpf: String,
    val cnpj: String,
    val stateRegistry: String,
    val residentialPhone: String,
    val cellPhone: String,
    val occupation: String,
    val email: String,
    val street: String,
    val number: Int,
    val neighborhood: String,
    val reference: String,
    val city: String,
    val state: String,
    val country: String,
    val zipCode: String,
    var deleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
}