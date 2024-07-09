package account.models.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity(name = "user_table")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,
    val name: String?,
    val lastname: String?,
    var email: String?,
    var password: String?,
    @ElementCollection(fetch = FetchType.EAGER)
    var roles: MutableSet<String>?,
    @JsonIgnore
    var failedLogins: Int = 0,
    @JsonIgnore
    var accountNonLocked: Boolean = true
)
