package br.com.vipsolutions.connect.model.dao

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 04/04/22
 */
class ContactDAO(
    val name: String,
    var whatsapp: String,
    var controlNumber: Long,
) {
    var instanceId: Int = 0
}