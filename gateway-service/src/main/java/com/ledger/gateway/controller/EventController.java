
package com.ledger.gateway.controller;

import com.ledger.gateway.entity.EventEntity;
import com.ledger.gateway.service.EventService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service){
        this.service = service;
    }

    @PostMapping
    public String publish(@RequestBody EventEntity event){
        return service.ingest(event);
    }

    @GetMapping
    public List<EventEntity> get(@RequestParam String accountId){
        return service.getEvents(accountId);
    }
}
