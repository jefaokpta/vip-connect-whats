package br.com.vipsolutions.connect.model

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 04/04/22
 */
class ContactDAO(
    val id: Long,
    val name: String,
    var whatsapp: String,
    var controlNumber: Long,
) {
    val imgUrl: String = ""
    var instanceId: Int = 0
}