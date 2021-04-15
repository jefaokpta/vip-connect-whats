package br.com.vipsolutions.connect.model

import br.com.vipsolutions.connect.model.dto.UserDTO
import br.com.vipsolutions.connect.model.util.UserProfile
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * @author Jefferson Alves Reis (jefaokpta) < jefaokpta@hotmail.com >
 * Date: 2021-04-05
 */
@Table("users")
data class User(
    @Id
    val id: Long,
    val fullName: String,
    val profile: UserProfile,
    val email: String,
    val companyId: Int,
    var passwordHash: String,
    var deleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor(db: User, update: UserDTO) : this(
        db.id,
        update.fullName,
        update.profile,
        update.email,
        db.companyId,
        db.passwordHash,
        db.deleted,
        db.createdAt
    ){
        if(update.passwordHash.isNotBlank()) passwordHash = update.passwordHash
    }
    constructor(userDTO: UserDTO): this(
        userDTO.id,
        userDTO.fullName,
        userDTO.profile,
        userDTO.email,
        userDTO.companyId,
        userDTO.passwordHash
    )

}