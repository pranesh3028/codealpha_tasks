package persistence;

import model.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FileDataRepository implements DataRepository {
    private final Path dataDir;

    public FileDataRepository(String dir) throws IOException {
        this.dataDir = Paths.get(dir);
        Files.createDirectories(dataDir);
    }

    private void atomicWrite(String fileName, List<String> lines) throws IOException {
        Path filePath = dataDir.resolve(fileName);
        Path tempPath = dataDir.resolve(fileName + ".tmp");
        Files.write(tempPath, lines);
        Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public void saveUsers(Map<String, User> users) {
        try {
            List<String> lines = new ArrayList<>();
            for (User u : users.values()) {
                lines.add(String.join(",", u.getUsername(), u.getPasswordHash(), u.getSalt(), String.valueOf(u.getBalance()), u.getTheme(), u.getUpiId()));
            }
            atomicWrite("users.csv", lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        Path path = dataDir.resolve("users.csv");
        if (!Files.exists(path)) return users;
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    User u = new User(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]));
                    if (parts.length >= 5) u.setTheme(parts[4]);
                    if (parts.length >= 6) u.setUpiId(parts[5]);
                    users.put(u.getUsername(), u);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void saveHoldings(Map<String, Map<String, Holding>> holdings) {
        try {
            List<String> lines = new ArrayList<>();
            holdings.forEach((user, userHoldings) -> {
                userHoldings.forEach((symbol, holding) -> {
                    lines.add(String.join(",", user, symbol, String.valueOf(holding.getQuantity()), String.valueOf(holding.getAverageCost())));
                });
            });
            atomicWrite("holdings.csv", lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Map<String, Holding>> loadHoldings() {
        Map<String, Map<String, Holding>> holdings = new HashMap<>();
        Path path = dataDir.resolve("holdings.csv");
        if (!Files.exists(path)) return holdings;
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String user = parts[0];
                    String symbol = parts[1];
                    int qty = Integer.parseInt(parts[2]);
                    double avgCost = Double.parseDouble(parts[3]);
                    holdings.computeIfAbsent(user, k -> new HashMap<>()).put(symbol, new Holding(symbol, qty, avgCost));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return holdings;
    }

    @Override
    public void saveTransactions(List<Transaction> transactions) {
        try {
            List<String> lines = new ArrayList<>();
            for (Transaction t : transactions) {
                lines.add(String.join(",", t.getType(), t.getUsername(), t.getSymbol(), String.valueOf(t.getQuantity()), String.valueOf(t.getPrice()), t.getTimestamp().toString(), String.valueOf(t.getRealizedPL())));
            }
            atomicWrite("transactions.csv", lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Path path = dataDir.resolve("transactions.csv");
        if (!Files.exists(path)) return transactions;
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 7) {
                    String type = parts[0];
                    String user = parts[1];
                    String symbol = parts[2];
                    int qty = Integer.parseInt(parts[3]);
                    double price = Double.parseDouble(parts[4]);
                    double pl = Double.parseDouble(parts[6]);
                    if ("BUY".equals(type)) {
                        transactions.add(new BuyTransaction(user, symbol, qty, price));
                    } else {
                        transactions.add(new SellTransaction(user, symbol, qty, price, 0)); // cost basis not stored in CSV for simplicity in basic load
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public void saveSnapshots(List<PortfolioSnapshot> snapshots) {
        try {
            List<String> lines = new ArrayList<>();
            for (PortfolioSnapshot s : snapshots) {
                lines.add(String.join(",", s.getUsername(), String.valueOf(s.getTotalValue()), s.getTimestamp().toString()));
            }
            atomicWrite("snapshots.csv", lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PortfolioSnapshot> loadSnapshots() {
        List<PortfolioSnapshot> snapshots = new ArrayList<>();
        Path path = dataDir.resolve("snapshots.csv");
        if (!Files.exists(path)) return snapshots;
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String user = parts[0];
                    double val = Double.parseDouble(parts[1]);
                    // timestamp is parsed but not used for the model in this simple load
                    snapshots.add(new PortfolioSnapshot(user, val));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return snapshots;
    }
}
