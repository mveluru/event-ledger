
package com.ledger.gateway.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_account_time", columnList = "accountId,eventTimestamp")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = "eventId")
})
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventId;

    private String accountId;
    private String type;
    private double amount;

    private Instant eventTimestamp;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }
}
