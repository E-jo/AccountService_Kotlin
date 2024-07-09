package account.repositories

import account.models.entities.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : PagingAndSortingRepository<User, Int> {
    fun findAll(): List<User>
    fun findAllByOrderByIdAsc(): List<User>
    fun findByEmailIgnoreCase(email: String): Optional<User>
    fun save(user: User): User
    fun delete(user: User)
    @Query("DELETE FROM user_table")
    fun deleteAll()
}
