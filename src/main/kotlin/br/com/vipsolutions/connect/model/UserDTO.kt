package br.com.vipsolutions.connect.model

import br.com.vipsolutions.connect.model.util.UserProfile

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-08
 */
class UserDTO(
    val id: Long,
    val fullName: String,
    val profile: UserProfile,
    val email: String,
    val companyId: Int,
    val passwordHash: String,
) {
    constructor(user: User): this(
        user.id,
        user.fullName,
        user.profile,
        user.email,
        user.companyId,
        ""
    )
}