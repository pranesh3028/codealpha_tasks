package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A snapshot of the total portfolio value at a given time.
 */
public class PortfolioSnapshot implements Serializable {
    private final LocalDateTime timestamp;
    private final String username;
    private final double totalValue;

    public PortfolioSnapshot(String username, double totalValue) {
        this.timestamp = LocalDateTime.now();
        this.username = username;
        this.totalValue = totalValue;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public double getTotalValue() { return totalValue; }
}
