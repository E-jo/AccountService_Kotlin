package account.services

import account.models.entities.User
import account.models.UserDetailsAdapter
import account.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService : UserDetailsService {
    @Autowired
    lateinit var userRepository: UserRepository

    fun findAll(): List<User> =
        userRepository.findAll()

    fun findAllOrderByIdAsc(): List<User> =
        userRepository.findAllByOrderByIdAsc()

    fun findByEmail(email: String): Optional<User> =
        userRepository.findByEmailIgnoreCase(email)

    fun save(user: User): User =
         userRepository.save(user)

    fun delete(user: User) =
        userRepository.delete(user)

    override fun loadUserByUsername(email: String?): UserDetails {
        if (email.isNullOrEmpty()) {
            throw UsernameNotFoundException("Email is null or empty")
        }

        val userOptional = userRepository.findByEmailIgnoreCase(email)
        val user = userOptional.orElseThrow {
            UsernameNotFoundException("User not found with email: $email")
        }

        println("Found user: ${user.email}")
        return UserDetailsAdapter(user)
    }
}


