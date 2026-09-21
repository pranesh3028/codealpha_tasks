package persistence;

import model.*;
import java.util.*;

public interface DataRepository {
    void saveUsers(Map<String, User> users);
    Map<String, User> loadUsers();

    void saveHoldings(Map<String, Map<String, Holding>> holdings);
    Map<String, Map<String, Holding>> loadHoldings();

    void saveTransactions(List<Transaction> transactions);
    List<Transaction> loadTransactions();

    void saveSnapshots(List<PortfolioSnapshot> snapshots);
    List<PortfolioSnapshot> loadSnapshots();
}
