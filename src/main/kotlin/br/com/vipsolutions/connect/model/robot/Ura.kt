package br.com.vipsolutions.connect.model.robot

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 25/08/21
 */
@Table("uras")
class Ura(
    @Id
    val id: Long,
    val company: Long,
    val controlNumber: Long,
    val initialMessage: String,
    val agentEmpty: String?,
    val invalidOption: String?,
    val validOption: String?,
    val finalMessage: String?,
) {
    @Transient
    var options: List<UraOption> = mutableListOf()
}