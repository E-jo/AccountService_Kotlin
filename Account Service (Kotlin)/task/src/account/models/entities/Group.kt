package account.models.entities

import jakarta.persistence.*

@Entity(name = "group_table")
data class Group(
    @Id
    val role: String,
    @ElementCollection(fetch = FetchType.EAGER)
    val members: MutableSet<String> = mutableSetOf()
)