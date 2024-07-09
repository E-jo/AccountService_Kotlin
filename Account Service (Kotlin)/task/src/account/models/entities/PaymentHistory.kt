package account.models.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "payment_history")
data class PaymentHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    val id: Int? = null,
    val employee: String,
    val period: String,
    val salary: Long
)