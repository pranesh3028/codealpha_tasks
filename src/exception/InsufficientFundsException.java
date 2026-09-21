package exception;

public class InsufficientFundsException extends TradingException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
