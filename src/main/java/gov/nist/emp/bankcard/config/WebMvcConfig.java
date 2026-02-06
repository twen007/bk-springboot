package gov.nist.emp.bankcard.config;

import gov.nist.emp.bankcard.interceptor.UsageLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UsageLoggingInterceptor usageLoggingInterceptor;

    public WebMvcConfig(UsageLoggingInterceptor usageLoggingInterceptor) {
        this.usageLoggingInterceptor = usageLoggingInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(usageLoggingInterceptor)
                .addPathPatterns("/api/org-data/divisions/active");
    }
}
