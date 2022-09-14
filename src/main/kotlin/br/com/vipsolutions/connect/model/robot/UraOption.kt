package br.com.vipsolutions.connect.model.robot

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("uras_options")
class UraOption(
    @Id
    var id: Long,
    var uraId: Long,
    val option: Int,
    val department: String,
    val departmentId: String
) {
}
