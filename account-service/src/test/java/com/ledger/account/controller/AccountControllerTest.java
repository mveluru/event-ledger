package com.ledger.account.controller;

import com.ledger.account.entity.AccountEntity;
import com.ledger.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService mockService;

    @InjectMocks
    private AccountController controller;

    @Test
    void apply_success() {
        AccountEntity account = new AccountEntity();
        account.setAccountId("acct1");
        account.setBalance(150.0);

        when(mockService.apply("acct1", 50.0)).thenReturn(account);

        Map<String, Object> req = new HashMap<>();
        req.put("id", "acct1");
        req.put("amount", 50.0);

        AccountEntity result = controller.apply(req);

        assertEquals("acct1", result.getAccountId());
        assertEquals(150.0, result.getBalance());
        verify(mockService, times(1)).apply("acct1", 50.0);
    }

    @Test
    void get_success() {
        AccountEntity account = new AccountEntity();
        account.setAccountId("acct2");
        account.setBalance(99.9);

        when(mockService.get("acct2")).thenReturn(account);

        AccountEntity result = controller.get("acct2");

        assertEquals("acct2", result.getAccountId());
        assertEquals(99.9, result.getBalance());
    }
}
