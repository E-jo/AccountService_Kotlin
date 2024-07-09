package account.controllers

import account.data
import account.models.Payment
import account.models.PaymentHistoryDTO
import account.models.StatusResponse
import account.models.entities.PaymentHistory
import account.models.errors.GenericError
import account.models.errors.PaymentError
import account.models.errors.PaymentHistoryError
import account.services.PaymentHistoryService
import account.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime


@RestController
class BusinessController {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var paymentHistoryService: PaymentHistoryService

    @PostMapping("/api/acct/payments")
    @Transactional
    fun uploadPayments(
        @RequestBody payments: List<Payment> ,
        //@AuthenticationPrincipal userMakingPayments: UserDetails
    ): ResponseEntity<*> {
        payments.forEach {
            if (userService.findByEmail(it.employee).isEmpty) {
                return ResponseEntity(PaymentError(
                    message = "Couldn't find employee ${it.employee}"
                ),
                    HttpStatus.BAD_REQUEST
                )
            }
            if (it.salary < 0) {
                return ResponseEntity(PaymentError(
                    message = "Cannot apply negative salary"
                ),
                    HttpStatus.BAD_REQUEST
                )
            }
            if (paymentHistoryService.findByEmployeeIgnoreCaseAndPeriod(
                    it.employee,
                    it.period
                ).isPresent) {
                return ResponseEntity(PaymentError(
                    message = "Cannot apply duplicate payment"
                ),
                    HttpStatus.BAD_REQUEST
                )
            }
            val period: String = it.period
            val monthNumber = period.substring(0, 2).toInt()
            if (monthNumber > 12 || !period.matches("[0-9]{2}-[0-9]{4}".toRegex())) {
                return ResponseEntity(PaymentError(
                    message = "Invalid period of payment"
                ),
                    HttpStatus.BAD_REQUEST
                )
            }
            val newPaymentHistory = PaymentHistory(
                employee = it.employee,
                period = it.period,
                salary = it.salary
            )
            paymentHistoryService.save(newPaymentHistory)
            println("Payment successful")
        }
        return ResponseEntity(StatusResponse(
            status = "Added successfully!"
        ),
            HttpStatus.OK
        )
    }

    @PutMapping("/api/acct/payments")
    fun adjustPayment(
        //@AuthenticationPrincipal requestingUser: UserDetails,
        @RequestBody payment: Payment
    ): ResponseEntity<*> {
        val record = paymentHistoryService
            .findByEmployeeIgnoreCaseAndPeriod(
                payment.employee,
                payment.period
            )
        if (record.isEmpty) {
            return ResponseEntity(PaymentError(
                message = "Payment record not found"
            ),
            HttpStatus.BAD_REQUEST
            )
        }
        if (payment.salary < 0) {
            return ResponseEntity(PaymentError(
                message = "Cannot apply a negative salary"
            ),
                HttpStatus.BAD_REQUEST
            )
        }
        val monthNumber = payment.period.substring(0, 2)
        val monthNumberInt = monthNumber.toInt()
        if (monthNumberInt > 12 ||
            !payment.period.matches(Regex("[0-9]{2}-[0-9]{4}"))) {
            return ResponseEntity(PaymentHistoryError(
                message = "Invalid period of payment"
            ),
                HttpStatus.BAD_REQUEST
            )
        }
        val recordToUpdate = record.get()
        paymentHistoryService.save(
            recordToUpdate.copy(
                salary = payment.salary
            )
        )
        return ResponseEntity(StatusResponse(
            status = "Updated successfully!"
        ),
            HttpStatus.OK
        )
    }

    @GetMapping("/api/empl/payment", "/api/empl/payment/")
    fun getEmployeePaymentRecord(
        @AuthenticationPrincipal employee: UserDetails,
        @RequestParam(required = false) period: String?
    ): ResponseEntity<*> {
        val userOptional = userService.findByEmail(employee.username)
        if (userOptional.isEmpty) {
            return ResponseEntity(
                PaymentHistoryError(
                    message = "Employee record not found"
                ),
                HttpStatus.NOT_FOUND)
        }
        val user = userOptional.get()

        // check for period parameter, if present do single period history
        period?.let {
            println("Period: $period")
            val monthNumber = period.substring(0, 2)
            val monthNumberInt = monthNumber.toInt()
            if (monthNumberInt > 12 ||
                !period.matches(Regex("[0-9]{2}-[0-9]{4}"))) {
                return ResponseEntity(PaymentHistoryError(
                    message = "Invalid period of payment"
                ),
                    HttpStatus.BAD_REQUEST
                )
            }
            val employeeEmail = employee.username
            val record = paymentHistoryService
                .findByEmployeeIgnoreCaseAndPeriod(
                    employeeEmail,
                    period
                )
            if (record.isEmpty) {
                return ResponseEntity(PaymentHistoryError(
                    message = "Payment record not found"
                ),
                    HttpStatus.BAD_REQUEST
                )
            }

            val paymentHistory = record.get()
            val month = data.monthMap[monthNumber]
            val year = period.substring(3)
            val formattedPeriod = "$month-$year"

            val salaryInCents = paymentHistory.salary
            val dollars = salaryInCents / 100
            val cents = salaryInCents % 100

            val salary = "$dollars dollar(s) $cents cent(s)"

            return ResponseEntity(PaymentHistoryDTO(
                name = user.name!!,
                lastname = user.lastname!!,
                period = formattedPeriod,
                salary = salary
            ),
                HttpStatus.OK
            )
        }

        // no parameter, return full payment history
        val completePaymentHistoryOptional = paymentHistoryService.
            findAllByEmployeeIgnoreCaseOrderByPeriodDesc(user.email!!)

        if (completePaymentHistoryOptional.isEmpty) {
            return ResponseEntity(PaymentHistoryError(
                message = "Payment record not found"
            ),
                HttpStatus.BAD_REQUEST
            )
        }

        val completePaymentHistory = completePaymentHistoryOptional.get()
        val completePaymentHistoryDTO = mutableListOf<PaymentHistoryDTO>()
        completePaymentHistory.forEach {
            val monthNumber = it.period.substring(0, 2)
            val month = data.monthMap[monthNumber]
            val year = it.period.substring(3)
            val formattedPeriod = "$month-$year"

            val salaryInCents = it.salary
            val dollars = salaryInCents / 100
            val cents = salaryInCents % 100

            val salary = "$dollars dollar(s) $cents cent(s)"

            completePaymentHistoryDTO.add(
                PaymentHistoryDTO(
                name = user.name!!,
                lastname = user.lastname!!,
                period = formattedPeriod,
                salary = salary
            )
            )
        }

        return ResponseEntity(
            completePaymentHistoryDTO,
            HttpStatus.OK
        )
    }

}


