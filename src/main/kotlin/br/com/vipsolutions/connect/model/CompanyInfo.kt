package br.com.vipsolutions.connect.model

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-29
 */
class CompanyInfo(
    val id: Long,
    val controlNumber: Long,
    val instance: Int,
    val message: String,
    val isActive: Boolean = false,
    val isStopped: Boolean = false
) {
    constructor(company: Company, message: String): this(
        company.id,
        company.controlNumber,
        company.instance,
        message,
        company.isActive,
        company.isStopped
    )
}