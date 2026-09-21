package service;

import model.Stock;
import java.util.*;
import java.util.concurrent.*;

public class MarketService {
    private final Map<String, Stock> stocks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();

    public MarketService() {
        seedStocks();
        startPriceSimulation();
    }

    private void seedStocks() {
        stocks.put("AAPL", new Stock("AAPL", "Apple Inc.", "Tech", 150.0));
        stocks.put("MSFT", new Stock("MSFT", "Microsoft Corp.", "Tech", 280.0));
        stocks.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", "Tech", 2500.0));
        stocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", "Consumer", 3300.0));
        stocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", "Auto", 700.0));
        stocks.put("META", new Stock("META", "Meta Platforms Inc.", "Tech", 200.0));
        stocks.put("NFLX", new Stock("NFLX", "Netflix Inc.", "Entertainment", 400.0));
        stocks.put("NVDA", new Stock("NVDA", "Nvidia Corp.", "Tech", 220.0));
        stocks.put("PYPL", new Stock("PYPL", "Fintech", "Fintech", 120.0));
        stocks.put("BABA", new Stock("BABA", "Alibaba Group", "Consumer", 110.0));
    }

    private void startPriceSimulation() {
        scheduler.scheduleAtFixedRate(() -> {
            for (Stock stock : stocks.values()) {
                double current = stock.getCurrentPrice();
                double change = current * (0.02 * (random.nextDouble() * 2 - 1)); // +/- 2%
                stock.setCurrentPrice(current + change);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    public Collection<Stock> getAllStocks() {
        return stocks.values();
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
