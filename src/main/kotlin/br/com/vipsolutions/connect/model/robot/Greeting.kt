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
    var id: Long,
    var company: Long,
    val controlNumber: Long,
    val greet: String,
    val btnText: String = "Seu nome é \"$:name\", está correto?",
    val btnFooterText: String?,
    val btnNegative: String?
) {
    constructor(greeting: Greeting, dbGreeting: Greeting): this(
        dbGreeting.id,
        dbGreeting.company,
        dbGreeting.controlNumber,
        greeting.greet,
        greeting.btnText,
        greeting.btnFooterText,
        greeting.btnNegative
    )
}