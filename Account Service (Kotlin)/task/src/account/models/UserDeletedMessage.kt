package account.models

data class UserDeletedMessage(
    val user: String,
    val status: String
)