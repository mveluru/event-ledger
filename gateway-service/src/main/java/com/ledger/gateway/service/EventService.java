package com.ledger.gateway.service;

import com.ledger.gateway.entity.EventEntity;
import com.ledger.gateway.repository.EventRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    private final EventRepository repo;
    private final RestTemplate restTemplate;

    @Value("${account.service.url:http://localhost:8081}")
    private String accountServiceUrl;

    // Shared secret for service-to-service auth
    private static final String SERVICE_TOKEN = "internal-service-token-99";

    public EventService(EventRepository repo, RestTemplate restTemplate){
        this.repo = repo;
        this.restTemplate = restTemplate;
    }

    public String ingest(EventEntity event){
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            return "ERROR_MISSING_EVENT_ID";
        }
        if (event.getAmount() <= 0) {
            return "ERROR_INVALID_AMOUNT";
        }
        if (!"CREDIT".equalsIgnoreCase(event.getType()) && !"DEBIT".equalsIgnoreCase(event.getType())) {
            return "ERROR_INVALID_TYPE";
        }

        if(repo.findByEventId(event.getEventId()).isPresent()){
            return "DUPLICATE_EVENT";
        }

        repo.save(event);
        return callAccountService(event);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "accountServiceFallback")
    private String callAccountService(EventEntity event) {
        String url = accountServiceUrl + "/accounts/transactions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Trace-Id", MDC.get("traceId"));
        headers.set("X-Internal-Service-Token", SERVICE_TOKEN); // Service-to-Service Auth
        
        Map<String, Object> request = new HashMap<>();
        request.put("id", event.getAccountId());
        
        double signedAmount = "DEBIT".equalsIgnoreCase(event.getType()) ? -event.getAmount() : event.getAmount();
        request.put("amount", signedAmount);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            restTemplate.postForEntity(url, entity, Object.class);
            return "EVENT_ACCEPTED";
        } catch (Exception e) {
            throw e;
        }
    }

    public String accountServiceFallback(EventEntity event, Throwable t) {
        return "SERVICE_UNAVAILABLE_FALLBACK";
    }

    public List<EventEntity> getEvents(String accountId){
        return repo.findByAccountIdOrderByEventTimestampAsc(accountId);
    }
}
