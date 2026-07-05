package kr.dongchimi.db.owner

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.owner.Owner
import kr.dongchimi.db.common.BaseSoftDeleteEntity

@Entity
@Table(name = "owners")
class OwnerJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    val id: Long = 0,
    @Column(nullable = false)
    val email: String,
    @Column(nullable = false)
    val password: String,
) : BaseSoftDeleteEntity() {
    constructor(owner: Owner) : this(
        id = owner.id,
        email = owner.email,
        password = owner.password,
    )

    fun toDomain(): Owner =
        Owner(
            id = id,
            email = email,
            password = password,
        )
}
