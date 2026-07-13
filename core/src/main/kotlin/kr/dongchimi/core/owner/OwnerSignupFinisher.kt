package kr.dongchimi.core.owner

import kr.dongchimi.core.market.MarketAppender
import kr.dongchimi.core.market.MarketRegisterCommand
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OwnerSignupFinisher(
    private val ownerAppender: OwnerAppender,
    private val marketAppender: MarketAppender,
) {
    @Transactional
    fun finish(
        pending: PendingOwnerSignup,
        marketCommand: MarketRegisterCommand,
    ): OwnerSignupCompletion {
        val owner = ownerAppender.append(pending.email, pending.encodedPassword)
        val market =
            marketAppender.append(
                ownerId = owner.id,
                info = marketCommand.info,
                location = marketCommand.location,
                businessHours = marketCommand.businessHours,
                phoneNumber = marketCommand.phoneNumber,
                brn = marketCommand.brn,
            )

        return OwnerSignupCompletion(owner, market)
    }
}
