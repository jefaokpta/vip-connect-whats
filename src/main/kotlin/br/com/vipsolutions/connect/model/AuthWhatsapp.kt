package br.com.vipsolutions.connect.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-06-17
 */
@Table
class AuthWhatsapp(
    @Id
    var id: Long = 0,
    val companyId: Long,
    val clientID: String,
    val serverToken: String,
    val clientToken: String,
    val encKey: String,
    val macKey: String
) {
}