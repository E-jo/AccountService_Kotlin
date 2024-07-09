package account.models.errors

import java.time.LocalDateTime

data class GenericError(
    val timestamp: String = LocalDateTime.now().toString(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)