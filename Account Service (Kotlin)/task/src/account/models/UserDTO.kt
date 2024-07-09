package account.models

/*
data class UserDTO(
    val id: Int,
    val name: String,
    val lastname: String,
    val email: String,
    val roles: MutableSet<String>
)

 */
data class UserDTO(
    val id: Int,
    val name: String,
    val lastname: String,
    val email: String,
    private val _roles: MutableSet<String>
) {
    val roles: List<String> = _roles.sorted()
}