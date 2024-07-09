package account.models

data class LockChangeRequest(
    val user: String,
    val operation: String
)