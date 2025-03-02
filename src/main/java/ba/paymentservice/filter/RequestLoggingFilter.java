package ba.paymentservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        String endpoint = requestURI;

        if (queryString != null && !queryString.isEmpty()) {
            endpoint += "?" + queryString;
        }

        String message = String.format("Incoming request: %s %s; from IP: %s", method, endpoint, clientIp);

        logger.info(message);

        filterChain.doFilter(request, response);
    }
}
