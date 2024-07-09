package account.services

import account.models.entities.SecurityEvent
import account.repositories.SecurityEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SecurityEventService {
    @Autowired
    lateinit var securityEventRepository: SecurityEventRepository

    fun findAll(): List<SecurityEvent> = securityEventRepository.findAll()
    fun findAllByOrderByIdAsc(): List<SecurityEvent> =
        securityEventRepository.findAllByOrderByIdAsc()
    fun save(securityEvent: SecurityEvent): SecurityEvent =
        securityEventRepository.save(securityEvent)
    fun delete(securityEvent: SecurityEvent) =
        securityEventRepository.delete(securityEvent)
}