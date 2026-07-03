package kr.dongchimi.db.user

import kr.dongchimi.core.user.User
import kr.dongchimi.core.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {
    override fun findById(id: Long): User? = userJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(user: User): User = userJpaRepository.save(UserJpaEntity(user)).toDomain()
}
