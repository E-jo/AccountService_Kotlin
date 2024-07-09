package account.models

data class RoleChangeRequest(
    val user: String,
    val role: String,
    val operation: String
)