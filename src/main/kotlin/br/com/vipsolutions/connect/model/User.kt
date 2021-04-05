package br.com.vipsolutions.connect.model

import br.com.vipsolutions.connect.model.util.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-05
 */
@Table("users")
data class User(
    @Id
    val id: Long,
    val fullName: String,
    val profile: Profile,
    val email: String,
    val passwordHash: String,
    val deleted: Boolean = false,
    val companyId: Int
) {
}