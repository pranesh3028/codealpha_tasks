package Main;

import model.*;
import service.*;
import persistence.*;
import exception.*;
import java.util.*;

public class TestTradingSystem {
    public static void main(String[] args) {
        System.out.println("Running Trading System Tests...");
        try {
            testPersistenceAndTrading();
            System.out.println("All tests passed!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testPersistenceAndTrading() throws Exception {
        // Setup
        FileDataRepository repo = new FileDataRepository("test_data");
        MarketService market = new MarketService();
        Map<String, User> users = new HashMap<>();
        Map<String, Map<String, Holding>> holdings = new HashMap<>();
        List<Transaction> txs = new ArrayList<>();

        AuthService auth = new AuthService(repo, users);
        TradingService trading = new TradingService(market, repo, users, holdings, txs);

        // Test Registration
        User user = auth.register("testuser", "password123");
        System.out.println("Registered user: " + user.getUsername());

        // Test Buy
        Stock s = market.getStock("AAPL");
        double price = s.getCurrentPrice();
        trading.buyStock("testuser", "AAPL", 10);
        System.out.println("Bought 10 AAPL at " + price);

        // Test Sell
        trading.sellStock("testuser", "AAPL", 5);
        System.out.println("Sold 5 AAPL");

        // Test Persistence
        repo.saveUsers(users);
        repo.saveHoldings(holdings);
        repo.saveTransactions(txs);

        Map<String, User> loadedUsers = repo.loadUsers();
        if (!loadedUsers.containsKey("testuser")) throw new Exception("User not persisted");
        System.out.println("User persistence verified");

        Map<String, Map<String, Holding>> loadedHoldings = repo.loadHoldings();
        if (!loadedHoldings.get("testuser").containsKey("AAPL")) throw new Exception("Holdings not persisted");
        System.out.println("Holdings persistence verified");
    }
}
