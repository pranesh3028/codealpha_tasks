package exception;

public class InvalidOrderException extends TradingException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
