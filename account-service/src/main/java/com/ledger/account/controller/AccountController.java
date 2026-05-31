
package com.ledger.account.controller;

import com.ledger.account.entity.AccountEntity;
import com.ledger.account.service.AccountService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service){
        this.service = service;
    }

    @PostMapping("/transactions")
    public AccountEntity apply(@RequestBody Map<String,Object> req){
        return service.apply(
            req.get("id").toString(),
            Double.parseDouble(req.get("amount").toString())
        );
    }

    @GetMapping("/{id}")
    public AccountEntity get(@PathVariable String id){
        return service.get(id);
    }
}
