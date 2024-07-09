package account.repositories

import account.models.entities.SecurityEvent
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SecurityEventRepository : PagingAndSortingRepository<SecurityEvent, Int> {
    fun findAll(): List<SecurityEvent>
    fun findAllByOrderByIdAsc(): List<SecurityEvent>
    fun save(securityEvent: SecurityEvent): SecurityEvent
    fun delete(securityEvent: SecurityEvent)
}