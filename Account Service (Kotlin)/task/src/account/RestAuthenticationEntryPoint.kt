package account

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {

    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        println(authException)
        println(authException.javaClass)

        val message = when (authException) {
            is LockedException -> "User account is locked"
            else -> authException.message
        }

        val originalRequestUri = request.getAttribute("originalRequestUri") as? String ?: request.requestURI

        val data: MutableMap<String, Any> = HashMap()
        data["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        data["status"] = HttpServletResponse.SC_UNAUTHORIZED
        data["error"] = "Unauthorized"
        data["message"] = "User account is locked"
        data["path"] = originalRequestUri

        response.outputStream.println(ObjectMapper().writeValueAsString(data))
    }
}