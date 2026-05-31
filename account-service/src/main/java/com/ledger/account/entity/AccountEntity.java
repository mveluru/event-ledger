
package com.ledger.account.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private String accountId;

    private double balance;

    @Version
    private Long version;

    public String getAccountId(){ return accountId; }
    public void setAccountId(String id){ this.accountId = id; }

    public double getBalance(){ return balance; }
    public void setBalance(double b){ this.balance = b; }
}
