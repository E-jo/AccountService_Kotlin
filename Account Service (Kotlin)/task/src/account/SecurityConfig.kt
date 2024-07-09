package account

import account.models.entities.SecurityEvent
import account.services.SecurityEventService
import account.services.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime
import java.util.*


@Configuration
@EnableWebSecurity
class SecurityConfig(private val requestUriFilter: RequestUriFilter) {
    @Autowired
    lateinit var restAuthenticationEntryPoint: RestAuthenticationEntryPoint

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var securityEventService: SecurityEventService

    @Bean
    fun encoder(): BCryptPasswordEncoder =
        BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .addFilterBefore(requestUriFilter, BasicAuthenticationFilter::class.java)
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint(restAuthenticationEntryPoint)
                ex.accessDeniedHandler(accessDeniedHandler())
            }
            .userDetailsService(userService)
            .httpBasic()
            .and()
            .authorizeHttpRequests { auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/signup",
                    "/actuator/shutdown").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/empl/payment",
                    "/api/empl/payment/").hasAnyRole("USER", "ACCOUNTANT")
                .requestMatchers(HttpMethod.POST, "/api/acct/payments",
                    "/api/acct/payments/").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.PUT, "/api/acct/payments",
                    "/api/acct/payments/").hasRole("ACCOUNTANT")
                .requestMatchers(HttpMethod.GET, "/api/security/events",
                    "/api/security/events/").hasRole("AUDITOR")
                .requestMatchers(HttpMethod.PUT,"/api/admin/user/role",
                    "/api/admin/user/role/").hasRole("ADMINISTRATOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/admin/user/{email}", "/api/admin/user/",
                    "/api/admin/user").hasRole("ADMINISTRATOR")
                .requestMatchers(HttpMethod.GET, "/api/admin/user",
                    "/api/admin/user/").hasRole("ADMINISTRATOR")
                .anyRequest().authenticated()
            }
            .csrf().disable()
            .build()

    @Bean
    fun configureAuthenticationManager(
        http: HttpSecurity,
        userService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationManager =
        http
            .getSharedObject(AuthenticationManagerBuilder::class.java)
            .userDetailsService(userService)
            .passwordEncoder(passwordEncoder)
            .and()
            .build()

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler {
        val objectMapper = ObjectMapper()

        return AccessDeniedHandler { request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException? ->
            response.status = HttpStatus.FORBIDDEN.value()
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"

            val data: MutableMap<String, Any> = HashMap()
            data["timestamp"] = LocalDateTime.now().toString()
            data["status"] = HttpStatus.FORBIDDEN.value()
            data["error"] = HttpStatus.FORBIDDEN.reasonPhrase
            data["message"] = "${accessDeniedException?.message}!"
            data["path"] = request.requestURI

            val attributes =
                RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes

            val accessDeniedEvent = SecurityEvent(
                date = LocalDateTime.now().toString(),
                action = "ACCESS_DENIED",
                subject = request.userPrincipal.name.lowercase(Locale.getDefault()),
                obj = attributes.request.requestURI,
                path = attributes.request.requestURI
            )

            securityEventService.save(accessDeniedEvent)
            response.writer.println(objectMapper.writeValueAsString(data))
        }
    }
}


