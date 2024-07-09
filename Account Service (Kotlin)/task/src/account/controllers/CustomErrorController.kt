package account.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Controller
class CustomErrorController : ErrorController {

    @GetMapping("/error")
    fun handleError(request: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        println("CustomErrorController:")
        request.attributeNames.toList().forEach {
            println(it)
        }
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

        val errorResponse = mapOf(
            "timestamp" to timestamp,
            "status" to request.getAttribute("status"),
            "error" to request.getAttribute("error"),
            "message" to request.getAttribute("message"),
            "path" to request.getAttribute("path")
        )

        return ResponseEntity(errorResponse, HttpStatus.valueOf(request.getAttribute("status").toString().toInt()))
    }

    fun getErrorPath(): String {
        return "/error"
    }
}