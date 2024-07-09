package account

import account.models.entities.Group
import account.repositories.GroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DataLoader(
    @Autowired var groupRepository: GroupRepository,
) {

    init {
        createRoles()
    }

    private fun createRoles() {
        try {
            groupRepository.save(Group(role = "ROLE_ADMINISTRATOR"))
            groupRepository.save(Group(role = "ROLE_USER"))
            groupRepository.save(Group(role = "ROLE_ACCOUNTANT"))
            groupRepository.save(Group(role = "ROLE_AUDITOR"))
            println("Role groups created")
        } catch (_: Exception) {
        }
    }
}

