package account.models

import account.models.entities.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsAdapter(private val user: User) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()
        user.roles?.forEach {
            println("Found role: $it")
            authorities.add(SimpleGrantedAuthority(it))
        }
        return authorities
    }

    override fun getPassword(): String = requireNotNull(user.password)

    override fun getUsername(): String = requireNotNull(user.email)

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = user.accountNonLocked

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}