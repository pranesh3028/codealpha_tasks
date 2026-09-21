package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a stock in the simulation.
 */
public class Stock {
    private final String symbol;
    private final String companyName;
    private final String sector;
    private double currentPrice;
    private final List<Double> priceHistory = Collections.synchronizedList(new ArrayList<>());

    public Stock(String symbol, String companyName, String sector, double startingPrice) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.sector = sector;
        this.currentPrice = startingPrice;
        this.priceHistory.add(startingPrice);
    }

    public String getSymbol() { return symbol; }
    public String getCompanyName() { return companyName; }
    public String getSector() { return sector; }

    public synchronized double getCurrentPrice() { return currentPrice; }

    public synchronized void setCurrentPrice(double price) {
        this.currentPrice = Math.max(1.0, price);
        priceHistory.add(this.currentPrice);
        if (priceHistory.size() > 200) {
            priceHistory.remove(0);
        }
    }

    public List<Double> getPriceHistory() {
        return new ArrayList<>(priceHistory);
    }
}
