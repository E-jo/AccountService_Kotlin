package account

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class RequestUriFilter : Filter {

    override fun init(filterConfig: FilterConfig) {}

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest = request as HttpServletRequest

        if (httpServletRequest.getAttribute("originalRequestUri") == null) {
            val originalRequestUri = httpServletRequest.requestURI
            println("RequestUriFilter invoked for $originalRequestUri")
            httpServletRequest.setAttribute("originalRequestUri", originalRequestUri)
        }

        println("RequestUriFilter invoked for ${httpServletRequest.requestURI}")
        println("httpServletRequest.requestUri: ${httpServletRequest.requestURI}")
        println("httpServletRequest.requestUrl: ${httpServletRequest.requestURL}")
        println("httpServletRequest.getAttribute(originalRequestUri):" +
                " ${httpServletRequest.getAttribute("originalRequestUri")}")

        chain.doFilter(request, response)
    }

    override fun destroy() {}
}