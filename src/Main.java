package Main;

import com.sun.net.httpserver.HttpServer;
import persistence.*;
import service.*;
import web.*;
import model.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // 1. Persistence
        DataRepository repository = new FileDataRepository("data");
        Map<String, User> users = repository.loadUsers();
        Map<String, Map<String, Holding>> holdings = repository.loadHoldings();
        List<Transaction> transactions = repository.loadTransactions();
        List<PortfolioSnapshot> snapshots = repository.loadSnapshots();

        // 2. Services
        MarketService marketService = new MarketService();
        AuthService authService = new AuthService(repository, users);
        TradingService tradingService = new TradingService(marketService, repository, users, holdings, transactions);
        PortfolioService portfolioService = new PortfolioService(marketService, repository, users, holdings, snapshots);
        SessionManager sessionManager = new SessionManager();

        // 3. Web Server
        Router router = new Router(authService, marketService, tradingService, portfolioService, sessionManager, repository, users);
        WebServer webServer = new WebServer(9000, router);

        // 4. Snapshot Background Task
        ScheduledExecutorService snapshotExecutor = Executors.newSingleThreadScheduledExecutor();
        snapshotExecutor.scheduleAtFixedRate(() -> {
            for (String username : users.keySet()) {
                portfolioService.takeSnapshot(username);
            }
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            marketService.shutdown();
            snapshotExecutor.shutdown();
            webServer.stop();
        }));

        webServer.start();
    }
}
