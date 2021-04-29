package br.com.vipsolutions.connect.model

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-29
 */
class CompanyInfo(
    val id: Long,
    val company: Int,
    val instance: Int,
    val isActive: Boolean = false,
    val isStopped: Boolean = false,
    val message: String
) {
    constructor(company: Company, message: String): this(
        company.id,
        company.company,
        company.instance,
        company.isActive,
        company.isStopped,
        message
    )
}