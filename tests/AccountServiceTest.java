package com.ledger.account;

import com.ledger.account.entity.AccountEntity;
import com.ledger.account.repo.AccountRepository;
import com.ledger.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository repo;

    @InjectMocks
    private AccountService service;

    @Test
    void apply_credit_increasesBalance() {
        AccountEntity acc = new AccountEntity();
        acc.setAccountId("acc-123");
        acc.setBalance(100.0);

        when(repo.findById("acc-123")).thenReturn(Optional.of(acc));
        when(repo.save(any(AccountEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        AccountEntity result = service.apply("acc-123", 50.0);

        assertEquals(150.0, result.getBalance());
    }

    @Test
    void apply_debit_decreasesBalance() {
        AccountEntity acc = new AccountEntity();
        acc.setAccountId("acc-123");
        acc.setBalance(100.0);

        when(repo.findById("acc-123")).thenReturn(Optional.of(acc));
        when(repo.save(any(AccountEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // Negative amount passed by Gateway for DEBIT
        AccountEntity result = service.apply("acc-123", -30.0);

        assertEquals(70.0, result.getBalance());
    }
}
