package kr.dongchimi.db.common

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseSoftDeleteEntity : BaseTimeEntity() {

    @Column
    var deletedAt: LocalDateTime? = null
        protected set

    val isDeleted: Boolean
        get() = deletedAt != null

    fun delete() {
        deletedAt = LocalDateTime.now()
    }
}
