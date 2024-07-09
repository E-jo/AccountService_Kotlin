package account.services

import account.models.entities.Group
import account.repositories.GroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupService {
    @Autowired
    lateinit var groupRepository: GroupRepository

    fun findAll(): List<Group> = groupRepository.findAll()
    fun findByRole(role: String): Optional<Group> = groupRepository.findByRole(role)
    fun save(group: Group): Group = groupRepository.save(group)
    fun delete(group: Group) = groupRepository.delete(group)
}
