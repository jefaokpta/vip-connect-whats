package br.com.vipsolutions.connect.model.dto

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 04/04/22
 */
data class ContactDTO(
    val name: String,
    var whatsapp: String,
    var controlNumber: Long,
) {
    var instanceId: Int = 0
}