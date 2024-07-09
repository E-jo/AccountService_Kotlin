package account.models

import com.fasterxml.jackson.annotation.JsonProperty

data class PasswordChangeRequest(
    @JsonProperty("new_password")
    val newPassword: String
)