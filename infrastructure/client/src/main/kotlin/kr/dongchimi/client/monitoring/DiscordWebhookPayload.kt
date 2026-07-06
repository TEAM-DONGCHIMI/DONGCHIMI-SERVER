package kr.dongchimi.client.monitoring

/**
 * Discord Webhook 요청 페이로드. 필드명은 Discord API 스펙을 그대로 따른다.
 * (임베드 제약: description ≤ 4096자, field value ≤ 1024자)
 */
data class DiscordWebhookPayload(
    val embeds: List<DiscordEmbed>,
)

data class DiscordEmbed(
    val title: String,
    val description: String,
    val color: Int,
    val fields: List<DiscordField>,
    val timestamp: String,
)

data class DiscordField(
    val name: String,
    val value: String,
    val inline: Boolean,
)
