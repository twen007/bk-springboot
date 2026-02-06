package gov.nist.emp.bankcard.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UsageLoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(UsageLoggingInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String origin = request.getHeader("Origin");
        if (origin != null && origin.endsWith("emp.nist.gov")) {
            // Example: Extract client ID from Okta/JWT or header (customize as needed)
            String clientId = request.getHeader("X-Client-Id");
            if (clientId == null) {
                clientId = "unknown";
            }
            String path = request.getRequestURI();
            String method = request.getMethod();
            logger.info("API usage: clientId={}, method={}, path={}", clientId, method, path);
        }
        return true;
    }
}
