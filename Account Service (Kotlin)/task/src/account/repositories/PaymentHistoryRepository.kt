package account.repositories

import account.models.entities.PaymentHistory
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface PaymentHistoryRepository : PagingAndSortingRepository<PaymentHistory, Int> {
    fun findAll(): List<PaymentHistory>
    fun findByEmployeeIgnoreCase(email: String): Optional<PaymentHistory>
    fun findByEmployeeIgnoreCaseAndPeriod(email: String, period: String): Optional<PaymentHistory>
    fun findAllByEmployeeIgnoreCaseOrderByPeriodDesc(email: String): Optional<List<PaymentHistory>>
    fun save(paymentHistory: PaymentHistory): PaymentHistory
}