package account.controllers

import account.data
import account.models.PasswordChangeRequest
import account.models.PasswordChangeResponse
import account.models.UserDTO
import account.models.entities.SecurityEvent
import account.models.entities.User
import account.models.errors.GenericError
import account.models.errors.PasswordChangeError
import account.models.errors.SignupError
import account.services.GroupService
import account.services.SecurityEventService
import account.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class AuthController {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var groupService: GroupService

    @Autowired
    lateinit var securityEventService: SecurityEventService

    @Autowired
    lateinit var encoder: BCryptPasswordEncoder

    @PostMapping("/api/auth/signup", produces = ["application/json"])
    fun signup(@RequestBody user: User): ResponseEntity<*> {
        println("POST request to /api/auth/signup received")
        println(user)
        val requestStatus = userValidationStatus(user)
        if (requestStatus != "valid") {
            return ResponseEntity(
                SignupError(message = requestStatus),
                HttpStatus.BAD_REQUEST
            )
        }

        // user.email being null would have already returned
        val existingUserOptional = userService.findByEmail(user.email!!)

        if (existingUserOptional.isPresent) {
            return ResponseEntity(
                SignupError(message = "User exist!"),
                HttpStatus.BAD_REQUEST
            )
        }

        if (data.breachedPasswords.contains(user.password)) {
            return ResponseEntity(
                SignupError(message = "The password is in the hacker's database!"),
                HttpStatus.BAD_REQUEST
            )
        }

        // check if it is first user signed up, if so grant admin role
        if (userService.findAll().isEmpty()) {
            user.roles = mutableSetOf("ROLE_ADMINISTRATOR")
            val adminGroup = groupService
                .findByRole("ROLE_ADMINISTRATOR")
                .get()
            adminGroup.members.add(user.email!!)
            groupService.save(adminGroup)
        } else {
            user.roles = mutableSetOf("ROLE_USER")
            val userGroup = groupService
                .findByRole("ROLE_USER")
                .get()
            userGroup.members.add(user.email!!)
            groupService.save(userGroup)
        }

        user.email = user.email!!.lowercase()
        user.accountNonLocked = true
        user.password = encoder.encode(user.password)
        userService.save(user)

        // log security event
        val userCreatedEvent = SecurityEvent(
            date = LocalDateTime.now().toString(),
            action = "CREATE_USER",
            subject = "Anonymous",
            obj = user.email!!,
            path = "/api/auth/signup"
        )
        securityEventService.save(userCreatedEvent)

        val response = UserDTO(
            user.id!!,
            user.name!!,
            user.lastname!!,
            user.email!!,
            user.roles!!
        )
        return ResponseEntity(response, HttpStatus.OK)
    }

    @PostMapping("api/auth/changepass", produces = ["application/json"])
    fun changePassword(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody passwordChangeRequest: PasswordChangeRequest
    ): ResponseEntity<*> {
        println("Request: $passwordChangeRequest")
        println("Current password: ${userDetails.password}")

        println("Looking up details for ${userDetails.username}")
        val userOptional = userService.findByEmail(userDetails.username)

        if (userOptional.isEmpty) {
            return ResponseEntity(GenericError(
                status = 404,
                error = "Bad Request",
                message = "Could not find user!",
                path = "/api/auth/changepass"
            ), HttpStatus.NOT_FOUND)
        }

        val user = userOptional.get()
        val newPassword = passwordChangeRequest.newPassword
        val requestStatus = passwordValidationStatus(newPassword)
        println(requestStatus)

        if (requestStatus != "valid") {
            return ResponseEntity(PasswordChangeError(
                message = requestStatus
            ), HttpStatus.BAD_REQUEST
            )
        }

        try {
            println("Matching passwords")
            if (encoder.matches(newPassword, userDetails.password)) {
                return ResponseEntity(
                    PasswordChangeError(
                        message = "The passwords must be different!"
                    ), HttpStatus.BAD_REQUEST
                )
            }
        } catch (ex: Exception) {
            println("Error matching passwords")
            println("${ex.message}")
        }
        println("No password match")

        userService.save(
            user.copy(password = encoder.encode(newPassword))
        )
        println("Password updated")

        val passwordChangedEvent = SecurityEvent(
            date = LocalDateTime.now().toString(),
            action = "CHANGE_PASSWORD",
            subject = user.email!!,
            obj = user.email!!,
            path = "/api/auth/signup"
        )
        securityEventService.save(passwordChangedEvent)

        val response = PasswordChangeResponse(
            // this lowercase is for the test... "ignorecase" for uniqueness
            // should not necessarily mean "store them all lowercase"...
            // you can preserve the casing and still look for a match ignoring case
            email = userDetails.username.lowercase(),
            status = "The password has been updated successfully"
        )
        println(response)
        return ResponseEntity(response, HttpStatus.OK)
    }

    private fun passwordValidationStatus(newPassword: String): String {
        if (data.breachedPasswords.contains(newPassword)) {
            return "The password is in the hacker's database!"
        } else {
            println("Did not find $newPassword in list of breached passwords")
        }
        if (newPassword.length < 12) {
            return "Password length must be 12 chars minimum!"
        }
        println("valid password")
        return "valid"
    }

    fun validEmail(email: String): Boolean {
        return email.endsWith("@acme.com")
    }

    fun userValidationStatus(user: User): String {
        val userName: String? = user.name
        val lastName: String? = user.lastname
        val email = user.email
        val password = user.password

        when {
            userName == null -> return "Name field is absent!"
            lastName == null -> return "Last name field is absent!"
            email == null -> return "Email field is absent!"
            password == null -> return "Password field is absent!"
        }

        when {
            userName.isNullOrEmpty() -> return "Name field is empty!"
            lastName.isNullOrEmpty() -> return "Last name field is empty!"
            email.isNullOrEmpty() -> return "Email field is empty!"
            password.isNullOrEmpty() -> return "Password field is empty!"
        }

        if (!validEmail(email!!)) {
            return "Wrong email!"
        }

        return "valid"
    }
}


