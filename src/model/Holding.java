package model;

import java.io.Serializable;

/**
 * Represents a user's holding of a specific stock.
 */
public class Holding implements Serializable {
    private final String symbol;
    private int quantity;
    private double averageCost;

    public Holding(String symbol, int quantity, double averageCost) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageCost = averageCost;
    }

    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }

    public synchronized void addShares(int qty, double price) {
        double totalCost = (this.quantity * this.averageCost) + (qty * price);
        this.quantity += qty;
        this.averageCost = totalCost / this.quantity;
    }

    public synchronized void removeShares(int qty) {
        this.quantity -= qty;
    }

    public double getAverageCost() { return averageCost; }
}
