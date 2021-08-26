package br.com.vipsolutions.connect.model.robot

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 25/08/21
 */
class Ura(
    val id: Long,
    val company: Long,
    val greeting: String,
    val answers: List<UraAnswer>
) {
}