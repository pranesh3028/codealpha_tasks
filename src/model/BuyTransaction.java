package model;

/**
 * A buy transaction.
 */
public class BuyTransaction extends Transaction {
    public BuyTransaction(String username, String symbol, int quantity, double price) {
        super(username, symbol, quantity, price);
    }

    @Override
    public String getType() { return "BUY"; }

    @Override
    public double getRealizedPL() { return 0.0; }
}
