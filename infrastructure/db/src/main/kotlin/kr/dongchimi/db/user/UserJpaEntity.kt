package kr.dongchimi.db.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.user.Gender
import kr.dongchimi.core.user.SocialProvider
import kr.dongchimi.core.user.User
import kr.dongchimi.db.common.BaseSoftDeleteEntity

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    val email: String,
    val name: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val socialProvider: SocialProvider,
    val socialId: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender,
    val age: Int? = null,
) : BaseSoftDeleteEntity() {
    constructor(user: User) : this(
        id = user.id,
        email = user.email,
        name = user.name,
        socialProvider = user.socialProvider,
        socialId = user.socialId,
        gender = user.gender,
        age = user.age,
    )

    fun toDomain(): User =
        User(
            id = id,
            email = email,
            name = name,
            socialProvider = socialProvider,
            socialId = socialId,
            gender = gender,
            age = age,
        )
}
