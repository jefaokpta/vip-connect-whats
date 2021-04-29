package br.com.vipsolutions.connect.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-27
 */
@Table("companies")
data class Company(
    @Id
    val id: Long,
    val company: Int,
    val instance: Int,
    var isActive: Boolean = false,
    var isStopped: Boolean = false
) {
}