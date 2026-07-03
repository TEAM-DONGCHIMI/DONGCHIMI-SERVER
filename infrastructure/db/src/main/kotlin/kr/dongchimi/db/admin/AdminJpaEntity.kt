package kr.dongchimi.db.admin

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.admin.Admin

@Entity
@Table(name = "admin")
class AdminJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    val id: Long = 0,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val email: String,
    @Column(nullable = false)
    val password: String,
) {
    constructor(admin: Admin) : this(
        id = admin.id,
        name = admin.name,
        email = admin.email,
        password = admin.password,
    )

    fun toDomain(): Admin =
        Admin(
            id = id,
            name = name,
            email = email,
            password = password,
        )
}
