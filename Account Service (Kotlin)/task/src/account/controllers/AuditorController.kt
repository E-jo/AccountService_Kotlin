package account.controllers

import account.services.SecurityEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuditorController {
    @Autowired
    lateinit var securityEventService: SecurityEventService

    @GetMapping("/api/security/events", "/api/security/events/")
    fun getSecurityEventLog(
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<*> = ResponseEntity(
        securityEventService.findAllByOrderByIdAsc(),
        HttpStatus.OK
    )
}