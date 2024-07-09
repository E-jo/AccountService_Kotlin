package account.models.errors

import java.time.LocalDateTime

data class SignupError(
    val timestamp: String = LocalDateTime.now().toString(),
    val status: Int = 400,
    val error: String = "Bad Request",
    val message: String,
    val path: String = "/api/auth/signup"
)