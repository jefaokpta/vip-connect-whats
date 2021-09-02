package br.com.vipsolutions.connect.model.robot

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("uras_options")
class UraOption(
    @Id
    val id: Long,
    val uraId: Long,
    val option: Int,
    val department: String
) {
}
