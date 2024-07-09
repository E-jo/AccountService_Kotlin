package account.repositories

import account.models.entities.Group
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GroupRepository : PagingAndSortingRepository<Group, Int> {
    fun findAll(): List<Group>
    fun findByRole(role: String): Optional<Group>
    fun save(group: Group): Group
    fun delete(group: Group)
    fun deleteAll()
}