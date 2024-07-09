package account.models.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "security_event_table")
@JsonPropertyOrder(value = ["id", "date", "action", "subject", "object", "path"])
data class SecurityEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,
    val date: String,
    val action: String,
    val subject: String,
    @JsonProperty("object")
    val obj: String,
    val path: String
)

