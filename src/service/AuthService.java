package service;

import model.*;
import exception.*;
import persistence.DataRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final DataRepository repository;
    private final Map<String, User> users;

    public AuthService(DataRepository repository, Map<String, User> users) {
        this.repository = repository;
        this.users = users;
    }

    public synchronized User register(String username, String password) throws TradingException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new TradingException("Username and password cannot be empty");
        }
        if (users.containsKey(username)) {
            throw new TradingException("Username already exists");
        }

        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        User user = new User(username, hash, salt, 100000.0);
        users.put(username, user);
        repository.saveUsers(users);
        return user;
    }

    public synchronized User login(String username, String password) throws TradingException {
        User user = users.get(username);
        if (user == null || !user.getPasswordHash().equals(hashPassword(password, user.getSalt()))) {
            throw new TradingException("Invalid username or password");
        }
        return user;
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
