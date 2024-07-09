package account.services

import account.models.entities.PaymentHistory
import account.repositories.PaymentHistoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class PaymentHistoryService {
    @Autowired
    lateinit var paymentHistoryRepository: PaymentHistoryRepository

    fun findAll(): List<PaymentHistory> =
        paymentHistoryRepository.findAll()
    fun findByEmployeeIgnoreCase(email: String): Optional<PaymentHistory> =
        paymentHistoryRepository.findByEmployeeIgnoreCase(email)
    fun findByEmployeeIgnoreCaseAndPeriod(email: String, period: String): Optional<PaymentHistory> =
        paymentHistoryRepository.findByEmployeeIgnoreCaseAndPeriod(email, period)
    fun findAllByEmployeeIgnoreCaseOrderByPeriodDesc(email: String): Optional<List<PaymentHistory>> =
        paymentHistoryRepository.findAllByEmployeeIgnoreCaseOrderByPeriodDesc(email)
    fun save(paymentHistory: PaymentHistory): PaymentHistory =
        paymentHistoryRepository.save(paymentHistory)
}
