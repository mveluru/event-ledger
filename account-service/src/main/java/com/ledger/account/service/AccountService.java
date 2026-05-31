
package com.ledger.account.service;

import com.ledger.account.entity.AccountEntity;
import com.ledger.account.repo.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository repo;

    public AccountService(AccountRepository repo){
        this.repo = repo;
    }

    @Transactional
    public AccountEntity apply(String id, double amt){

        AccountEntity acc = repo.findById(id)
                .orElseGet(() -> {
                    AccountEntity a = new AccountEntity();
                    a.setAccountId(id);
                    a.setBalance(0);
                    return a;
                });

        acc.setBalance(acc.getBalance() + amt);
        return repo.save(acc);
    }

    public AccountEntity get(String id){
        return repo.findById(id).orElse(null);
    }
}
