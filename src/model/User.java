package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a user of the trading platform.
 */
public class User implements Serializable {
    private final String username;
    private final String passwordHash;
    private final String salt;
    private double balance;
    private String theme = "light";
    private String upiId = "";

    public User(String username, String passwordHash, String salt, double initialBalance) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.balance = initialBalance;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }

    public synchronized double getBalance() { return balance; }

    public synchronized void setBalance(double balance) { this.balance = balance; }

    public synchronized void deposit(double amount) { this.balance += amount; }
    public synchronized void withdraw(double amount) { this.balance -= amount; }

    public synchronized String getTheme() { return theme; }
    public synchronized void setTheme(String theme) { this.theme = theme; }

    public synchronized String getUpiId() { return upiId; }
    public synchronized void setUpiId(String upiId) { this.upiId = upiId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
