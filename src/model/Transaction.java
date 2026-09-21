package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract base class for a transaction.
 */
public abstract class Transaction implements Serializable {
    private final LocalDateTime timestamp;
    private final String username;
    private final String symbol;
    private final int quantity;
    private final double price;

    public Transaction(String username, String symbol, int quantity, double price) {
        this.timestamp = LocalDateTime.now();
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotal() { return quantity * price; }

    public abstract String getType();
    public abstract double getRealizedPL();
}
