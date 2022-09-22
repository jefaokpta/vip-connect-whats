package br.com.vipsolutions.connect.model.robot

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 25/08/21
 */
@Table("uras")
data class Ura(
    @Id
    var id: Long,
    var company: Long,
    val controlNumber: Long,
    val initialMessage: String,
    val active: Boolean,
    val vipUraId: Long,
    val agentEmpty: String?,
    val invalidOption: String?,
    val validOption: String?,
    val finalMessage: String?,
) {
    @Transient
    var options: MutableList<UraOption> = mutableListOf()

    constructor(ura: Ura, dbUra: Ura): this(
        dbUra.id,
        dbUra.company,
        dbUra.controlNumber,
        ura.initialMessage,
        ura.active,
        ura.vipUraId,
        ura.agentEmpty,
        ura.invalidOption,
        ura.validOption,
        ura.finalMessage
    ){
        this.options = ura.options
    }
}