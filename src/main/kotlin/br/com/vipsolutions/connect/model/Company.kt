package br.com.vipsolutions.connect.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-06
 */
@Table("companies")
data class Company(
    @Id
    val id: Long,
    val fullName: String,
    val fantasyName: String,
    val cnpj: String,
    var deleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor(db: Company, update: Company): this(
        db.id,
        update.fullName,
        update.fantasyName,
        update.cnpj,
        db.deleted,
        db.createdAt
    )
}