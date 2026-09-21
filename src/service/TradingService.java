package service;

import model.*;
import exception.*;
import persistence.DataRepository;
import java.util.*;
import java.util.concurrent.*;

public class TradingService {
    private final MarketService marketService;
    private final DataRepository repository;
    private final Map<String, User> users;
    private final Map<String, Map<String, Holding>> holdings;
    private final List<Transaction> transactions;

    public TradingService(MarketService marketService, DataRepository repository, Map<String, User> users, Map<String, Map<String, Holding>> holdings, List<Transaction> transactions) {
        this.marketService = marketService;
        this.repository = repository;
        this.users = users;
        this.holdings = holdings;
        this.transactions = transactions;
    }

    public synchronized void buyStock(String username, String symbol, int quantity) throws TradingException {
        if (quantity <= 0) throw new InvalidOrderException("Quantity must be positive");

        Stock stock = marketService.getStock(symbol);
        if (stock == null) throw new InvalidOrderException("Invalid stock symbol");

        User user = users.get(username);
        if (user == null) throw new TradingException("User not found");

        double price = stock.getCurrentPrice();
        double totalCost = price * quantity;

        if (user.getBalance() < totalCost) {
            throw new InsufficientFundsException("Insufficient cash to buy " + quantity + " shares of " + symbol);
        }

        user.withdraw(totalCost);

        Map<String, Holding> userHoldings = holdings.computeIfAbsent(username, k -> new HashMap<>());
        Holding holding = userHoldings.computeIfAbsent(symbol, s -> new Holding(symbol, 0, 0));
        holding.addShares(quantity, price);

        transactions.add(new BuyTransaction(username, symbol, quantity, price));
        save();
    }

    public synchronized void sellStock(String username, String symbol, int quantity) throws TradingException {
        if (quantity <= 0) throw new InvalidOrderException("Quantity must be positive");

        Stock stock = marketService.getStock(symbol);
        if (stock == null) throw new InvalidOrderException("Invalid stock symbol");

        User user = users.get(username);
        if (user == null) throw new TradingException("User not found");

        Map<String, Holding> userHoldings = holdings.get(username);
        if (userHoldings == null || !userHoldings.containsKey(symbol)) {
            throw new InsufficientSharesException("No holdings of " + symbol);
        }

        Holding holding = userHoldings.get(symbol);
        if (holding.getQuantity() < quantity) {
            throw new InsufficientSharesException("Insufficient shares of " + symbol);
        }

        double price = stock.getCurrentPrice();
        double totalGain = price * quantity;
        double costBasis = holding.getAverageCost();

        user.deposit(totalGain);
        holding.removeShares(quantity);

        if (holding.getQuantity() == 0) {
            userHoldings.remove(symbol);
        }

        transactions.add(new SellTransaction(username, symbol, quantity, price, costBasis));
        save();
    }

    private void save() {
        repository.saveUsers(users);
        repository.saveHoldings(holdings);
        repository.saveTransactions(transactions);
    }
}
