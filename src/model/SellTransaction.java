package model;

/**
 * A sell transaction.
 */
public class SellTransaction extends Transaction {
    private final double realizedPL;

    public SellTransaction(String username, String symbol, int quantity, double price, double costBasis) {
        super(username, symbol, quantity, price);
        this.realizedPL = (price - costBasis) * quantity;
    }

    @Override
    public String getType() { return "SELL"; }

    @Override
    public double getRealizedPL() { return realizedPL; }
}
