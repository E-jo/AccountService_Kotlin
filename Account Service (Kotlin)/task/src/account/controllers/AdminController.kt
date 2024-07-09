package account.controllers

import account.models.*
import account.models.entities.SecurityEvent
import account.models.errors.GenericError
import account.services.GroupService
import account.services.SecurityEventService
import account.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
class AdminController {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var groupService: GroupService

    @Autowired
    lateinit var securityEventService: SecurityEventService

    @PutMapping("/api/admin/user/role", "/api/admin/user/role/")
    fun changeRoles(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody changeRequest: RoleChangeRequest
    ): ResponseEntity<*> {
        val userOptional = userService.findByEmail(changeRequest.user)
        if (userOptional.isEmpty) {
            return ResponseEntity(
                GenericError(
                    status = 404,
                    error = "Not Found",
                    message = "User not found!",
                    path = "/api/admin/user/role"
                ),
                HttpStatus.NOT_FOUND
            )
        }

        val allRoles = groupService.findAll()
        println("Found the following role groups:")
        allRoles.forEach {
            println(it.role)
        }
        val roleToChange = "ROLE_${changeRequest.role}"
        println("Searching for $roleToChange")
        val roleOptional = groupService.findByRole(roleToChange)
        if (roleOptional.isEmpty) {
            return ResponseEntity(
                GenericError(
                    status = 404,
                    error = "Not Found",
                    message = "Role not found!",
                    path = "/api/admin/user/role"
                ),
                HttpStatus.NOT_FOUND
            )
        }

        val roleGroupToChange = roleOptional.get()
        val userToChange = userOptional.get()
        val userToChangeRoles = userToChange.roles

        when (changeRequest.operation) {
            "GRANT" -> {
                when (roleToChange) {
                    "ROLE_ADMINISTRATOR" -> {
                        if (userToChangeRoles!!.contains("ROLE_USER")
                            || userToChangeRoles.contains("ROLE_ACCOUNTANT")) {
                            return ResponseEntity(
                                GenericError(
                                    status = 400,
                                    error = "Bad Request",
                                    message = "The user cannot combine administrative and business roles!",
                                    path = "/api/admin/user/role"
                                ),
                                HttpStatus.BAD_REQUEST
                            )
                        }
                        roleGroupToChange.members.add(userToChange.email!!)
                        groupService.save(roleGroupToChange)
                        userToChange.roles!!.add(roleToChange)
                        userService.save(userToChange)
                        val roleGrantedEvent = SecurityEvent(
                            date = LocalDateTime.now().toString(),
                            action = "GRANT_ROLE",
                            subject = user.username,
                            obj = "Grant role ${changeRequest.role} to ${userToChange.email}",
                            path = "/api/admin/user/role"
                        )
                        securityEventService.save(roleGrantedEvent)
                    }
                    "ROLE_USER", "ROLE_ACCOUNTANT", "ROLE_AUDITOR" -> {
                        if (userToChangeRoles!!.contains("ROLE_ADMINISTRATOR")) {
                            return ResponseEntity(
                                GenericError(
                                    status = 400,
                                    error = "Bad Request",
                                    message = "The user cannot combine administrative and business roles!",
                                    path = "/api/admin/user/role"
                                ),
                                HttpStatus.BAD_REQUEST
                            )
                        }
                        roleGroupToChange.members.add(userToChange.email!!)
                        groupService.save(roleGroupToChange)
                        userToChange.roles!!.add(roleToChange)
                        userService.save(userToChange)
                        val roleGrantedEvent = SecurityEvent(
                            date = LocalDateTime.now().toString(),
                            action = "GRANT_ROLE",
                            subject = user.username,
                            obj = "Grant role ${changeRequest.role} to ${userToChange.email}",
                            path = "/api/admin/user/role"
                        )
                        securityEventService.save(roleGrantedEvent)
                    }
                }
            }
            "REMOVE" -> {
                when (roleToChange) {
                    "ROLE_ADMINISTRATOR" -> {
                        return ResponseEntity(
                            GenericError(
                                status = 400,
                                error = "Bad Request",
                                message = "Can't remove ADMINISTRATOR role!",
                                path = "/api/admin/user/role"
                            ),
                            HttpStatus.BAD_REQUEST
                        )
                    }
                    "ROLE_USER", "ROLE_ACCOUNTANT", "ROLE_AUDITOR" -> {
                        if (userToChangeRoles != null) {
                            if (!userToChangeRoles.contains(roleToChange)) {
                                return ResponseEntity(
                                    GenericError(
                                        status = 400,
                                        error = "Bad Request",
                                        message = "The user does not have a role!",
                                        path = "/api/admin/user/role"
                                    ),
                                    HttpStatus.BAD_REQUEST
                                )
                            }
                            if (userToChangeRoles.size <= 1)
                                return ResponseEntity(
                                    GenericError(
                                        status = 400,
                                        error = "Bad Request",
                                        message = "The user must have at least one role!",
                                        path = "/api/admin/user/role"
                                    ),
                                    HttpStatus.BAD_REQUEST
                                )
                        }

                        roleGroupToChange.members.remove(userToChange.email!!)
                        groupService.save(roleGroupToChange)
                        userToChange.roles!!.remove(roleToChange)
                        userService.save(userToChange)

                        val roleRemovedEvent = SecurityEvent(
                            date = LocalDateTime.now().toString(),
                            action = "REMOVE_ROLE",
                            subject = user.username,
                            obj = "Remove role ${changeRequest.role} from ${userToChange.email}",
                            path = "/api/admin/user/role"
                        )
                        securityEventService.save(roleRemovedEvent)
                    }
                }
            }
        }
        return ResponseEntity(
            UserDTO(
            userToChange.id!!,
            userToChange.name!!,
            userToChange.lastname!!,
            userToChange.email!!.lowercase(),
            userToChange.roles!!
        ),
            HttpStatus.OK
        )
    }

    @PutMapping("/api/admin/user/access/", "/api/admin/user/access")
    fun changeUserLock(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody lockChangeRequest: LockChangeRequest
    ): ResponseEntity<*> {
        val userOptional = userService.findByEmail(lockChangeRequest.user)
        if (userOptional.isEmpty) {
            return ResponseEntity(
                GenericError(
                    status = 404,
                    error = "Not Found",
                    message = "User not found!",
                    path = "/api/admin/user/access"
                ),
                HttpStatus.NOT_FOUND
            )
        }
        val userToChange = userOptional.get()

        when (lockChangeRequest.operation.uppercase()) {
            "LOCK" -> {
                val userToChangeRoles = userToChange.roles
                if (userToChangeRoles!!.contains("ROLE_ADMINISTRATOR")) {
                    return ResponseEntity(
                        GenericError(
                            status = 400,
                            error = "Bad Request",
                            message = "Can't lock the ADMINISTRATOR!",
                            path = "/api/admin/user/access"
                        ),
                        HttpStatus.BAD_REQUEST
                    )
                }
                if (!userToChange.accountNonLocked) {
                    return ResponseEntity(
                        GenericError(
                            status = 400,
                            error = "Bad Request",
                            message = "User ${userToChange.email} already locked!",
                            path = "/api/admin/user/access"
                        ),
                        HttpStatus.BAD_REQUEST
                    )
                }
                userToChange.accountNonLocked = false
                val userLockedEvent = SecurityEvent(
                    date = LocalDateTime.now().toString(),
                    action = "LOCK_USER",
                    subject = user.username,
                    obj = "Lock user ${userToChange.email!!}",
                    path = "/api/admin/user/access"
                )
                securityEventService.save(userLockedEvent)
            }
            "UNLOCK" -> {
                if (userToChange.accountNonLocked) {
                    return ResponseEntity(
                        GenericError(
                            status = 400,
                            error = "Bad Request",
                            message = "User ${userToChange.email} already unlocked!",
                            path = "/api/admin/user/access"
                        ),
                        HttpStatus.BAD_REQUEST
                    )
                }
                userToChange.accountNonLocked = true
                userToChange.failedLogins = 0
                val userUnlockedEvent = SecurityEvent(
                    date = LocalDateTime.now().toString(),
                    action = "UNLOCK_USER",
                    subject = user.username,
                    obj = "Unlock user ${userToChange.email!!}",
                    path = "/api/admin/user/access"
                )
                securityEventService.save(userUnlockedEvent)
            }
        }

        userService.save(userToChange)
        val action = if (lockChangeRequest.operation == "LOCK") "locked" else "unlocked"

        return ResponseEntity(StatusResponse(
            status = "User ${userToChange.email} $action!"
        ),
            HttpStatus.OK
        )
    }

    @GetMapping("/api/admin/user/", "/api/admin/user")
    fun listUsers(
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<*> {
        val allUsers = userService.findAllOrderByIdAsc()
        val allUserDTO = mutableListOf<UserDTO>()
        allUsers.forEach {
            allUserDTO.add(
                UserDTO(
                it.id!!,
                it.name!!,
                it.lastname!!,
                it.email!!.lowercase(),
                it.roles!!
            )
            )
        }
        return ResponseEntity(allUserDTO, HttpStatus.OK)
    }

    @DeleteMapping("/api/admin/user/{email}", "/api/admin/user/", "/api/admin/user")
    fun deleteUser(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable(required = false) email: String?
    ): ResponseEntity<*> {
        if (email == null) {
            return ResponseEntity(
                GenericError(
                    status = 400,
                    error = "Bad Request",
                    message = "No user entered!",
                    path = "/api/admin/user/"
                ),
                HttpStatus.BAD_REQUEST
            )
        }
        val userOptional = userService.findByEmail(email)
        if (userOptional.isEmpty) {
            return ResponseEntity(
                GenericError(
                    status = 404,
                    error = "Not Found",
                    message = "User not found!",
                    path = "/api/admin/user/$email"
                ),
                HttpStatus.NOT_FOUND
            )
        }
        val userToDelete = userOptional.get()
        val userToDeleteRoles = userToDelete.roles
        if (userToDeleteRoles!!.contains("ROLE_ADMINISTRATOR")) {
            return ResponseEntity(
                GenericError(
                    status = 400,
                    error = "Bad Request",
                    message = "Can't remove ADMINISTRATOR role!",
                    path = "/api/admin/user/$email"
                ),
                HttpStatus.BAD_REQUEST)
        }
        userService.delete(userToDelete)

        val userDeletedEvent = SecurityEvent(
            date = LocalDateTime.now().toString(),
            action = "DELETE_USER",
            subject = user.username,
            obj = userToDelete.email!!,
            path = "/api/admin/user/${userToDelete.email}"
        )
        securityEventService.save(userDeletedEvent)

        return ResponseEntity(
            UserDeletedMessage(
            userToDelete.email!!.lowercase(),
            "Deleted successfully!"
        ),
            HttpStatus.OK
        )
    }
}