package com.ledger.account;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class AccountApplication {
  public static void main(String[] args){
    SpringApplication.run(AccountApplication.class,args);
  }

  @Bean
  public Filter securityAndTracingFilter() {
    return new Filter() {
      private static final String INTERNAL_TOKEN = "internal-service-token-99";

      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
              throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 1. Service-to-Service Security Check
        String path = req.getRequestURI();
        if (path.contains("/accounts")) {
           String token = req.getHeader("X-Internal-Service-Token");
           if (!INTERNAL_TOKEN.equals(token)) {
             res.setStatus(HttpServletResponse.SC_FORBIDDEN);
             res.getWriter().write("Forbidden: Invalid Service Token");
             return;
           }
        }

        // 2. Tracing
        String traceId = req.getHeader("X-Trace-Id");
        if (traceId != null) {
          MDC.put("traceId", traceId);
        }

        try {
          chain.doFilter(request, response);
        } finally {
          MDC.clear();
        }
      }
    };
  }
}
