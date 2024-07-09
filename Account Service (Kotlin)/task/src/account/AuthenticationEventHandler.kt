package account

import account.models.entities.SecurityEvent
import account.services.SecurityEventService
import account.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime
import java.util.*

@Component
class AuthenticationEventHandler {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var securityEventService: SecurityEventService

    @EventListener
    @Throws(LockedException::class)
    fun onLocked(event: AuthenticationFailureLockedEvent) {
        println("Event listener for AuthenticationFailureLockedEvent invoked")
        //throw LockedException("User account is locked")
    }

    @EventListener
    fun onFailure(event: AuthenticationFailureBadCredentialsEvent) {
        val attributes =
            RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes

        val loginFailedEvent = SecurityEvent(
            date = LocalDateTime.now().toString(),
            action = "LOGIN_FAILED",
            subject = event.authentication.name.lowercase(Locale.getDefault()),
            obj = attributes.request.requestURI,
            path = attributes.request.requestURI
        )

        securityEventService.save(loginFailedEvent)

        val attemptedLoginUserDetails = userService.loadUserByUsername(
            event.authentication.name.lowercase(Locale.getDefault())
        )

        val attemptedLoginOptional = userService.findByEmail(attemptedLoginUserDetails.username)
        val attemptedLogin = attemptedLoginOptional.get()

        attemptedLogin.failedLogins++
        userService.save(attemptedLogin)
        println("${attemptedLogin.email}: failed login #${attemptedLogin.failedLogins}")

        if (attemptedLogin.failedLogins > 4 && attemptedLogin.accountNonLocked) {
            val userRoles = attemptedLoginUserDetails.authorities
            for (role in userRoles) {
                if (role.authority.equals("ROLE_ADMINISTRATOR", ignoreCase = true)) {
                    // can't let the admin get locked out
                    attemptedLogin.failedLogins = 0
                    userService.save(attemptedLogin)
                    return
                }
            }
            println("Locking ${attemptedLogin.email}: failed login #${attemptedLogin.failedLogins} " +
                    "and accountNonLocked =  ${attemptedLogin.accountNonLocked}")

            attemptedLogin.accountNonLocked = false
            userService.save(attemptedLogin)
            val bruteForceEvent = SecurityEvent(
                date = LocalDateTime.now().toString(),
                action = "BRUTE_FORCE",
                subject = event.authentication.name.lowercase(Locale.getDefault()),
                obj = attributes.request.requestURI,
                path = attributes.request.requestURI
            )

            securityEventService.save(bruteForceEvent)

            val accountLockedEvent = SecurityEvent(
                date = LocalDateTime.now().toString(),
                action = "LOCK_USER",
                subject = event.authentication.name.lowercase(Locale.getDefault()),
                obj = "Lock user " + event.authentication.name.lowercase(Locale.getDefault()),
                path = attributes.request.requestURI
            )

            securityEventService.save(accountLockedEvent)
        }
    }

    @EventListener
    fun onAuthenticationSuccess(event: AuthenticationSuccessEvent) {
        val attemptedLoginUserDetails = userService.loadUserByUsername(
            event.authentication.name.lowercase(Locale.getDefault())
        )

        val attemptedLoginOptional = userService.findByEmail(attemptedLoginUserDetails.username)
        val attemptedLogin = attemptedLoginOptional.get()

        attemptedLogin.failedLogins = 0
        userService.save(attemptedLogin)
    }

}

