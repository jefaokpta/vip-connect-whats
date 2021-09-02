package br.com.vipsolutions.connect.model.robot

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 27/08/21
 */
@Table("greetings")
class Greeting(
    @Id
    val id: Long,
    val company: Long,
    val controlNumber: Long,
    val greet: String,
    val btnText: String,
    val btnFooterText: String?,
    val btnNegative: String
) {
}