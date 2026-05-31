package com.ledger.gateway.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String traceId = req.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put("traceId", traceId);

        try {
            System.out.println(String.format(
                    "{\"timestamp\":\"%s\",\"level\":\"INFO\",\"service\":\"gateway\",\"traceId\":\"%s\",\"path\":\"%s\"}",
                    java.time.Instant.now(),
                    traceId,
                    req.getRequestURI()
            ));
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
