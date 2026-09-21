package service;

import model.*;
import persistence.DataRepository;
import java.util.*;
import java.util.stream.Collectors;

public class PortfolioService {
    private final MarketService marketService;
    private final DataRepository repository;
    private final Map<String, User> users;
    private final Map<String, Map<String, Holding>> holdings;
    private final List<PortfolioSnapshot> snapshots;

    public PortfolioService(MarketService marketService, DataRepository repository, Map<String, User> users, Map<String, Map<String, Holding>> holdings, List<PortfolioSnapshot> snapshots) {
        this.marketService = marketService;
        this.repository = repository;
        this.users = users;
        this.holdings = holdings;
        this.snapshots = snapshots;
    }

    public synchronized double calculateTotalValue(String username) {
        User user = users.get(username);
        if (user == null) return 0.0;

        double total = user.getBalance();
        Map<String, Holding> userHoldings = holdings.get(username);
        if (userHoldings != null) {
            for (Holding h : userHoldings.values()) {
                Stock s = marketService.getStock(h.getSymbol());
                if (s != null) {
                    total += h.getQuantity() * s.getCurrentPrice();
                }
            }
        }
        return total;
    }

    public synchronized void takeSnapshot(String username) {
        double value = calculateTotalValue(username);
        snapshots.add(new PortfolioSnapshot(username, value));
        repository.saveSnapshots(snapshots);
    }

    public List<PortfolioSnapshot> getSnapshots(String username) {
        return snapshots.stream()
                .filter(s -> s.getUsername().equals(username))
                .collect(Collectors.toList());
    }

    public List<Map.Entry<String, Double>> getLeaderboard() {
        Map<String, Double> values = new HashMap<>();
        for (String username : users.keySet()) {
            values.put(username, calculateTotalValue(username));
        }
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(values.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return sorted;
    }
}
