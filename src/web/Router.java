package web;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import service.*;
import model.*;
import persistence.*;

public class Router {
    private final AuthService authService;
    private final MarketService marketService;
    private final TradingService tradingService;
    private final PortfolioService portfolioService;
    private final SessionManager sessionManager;
    private final DataRepository repository;
    private final Map<String, User> users;

    public Router(AuthService authService, MarketService marketService, TradingService tradingService, PortfolioService portfolioService, SessionManager sessionManager, DataRepository repository, Map<String, User> users) {
        this.authService = authService;
        this.marketService = marketService;
        this.tradingService = tradingService;
        this.portfolioService = portfolioService;
        this.sessionManager = sessionManager;
        this.repository = repository;
        this.users = users;
    }

    public void route(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String sessionId = getSessionId(exchange);
        String username = sessionManager.getUsername(sessionId);

        try {
            if ("/register".equals(path)) {
                handleRegister(exchange, method);
            } else if ("/login".equals(path)) {
                handleLogin(exchange, method);
            } else if ("/logout".equals(path)) {
                handleLogout(exchange, sessionId);
            } else if ("/market".equals(path)) {
                if (username == null) { redirect(exchange, "/login"); return; }
                handleMarket(exchange, username);
            } else if ("/stock".equals(path)) {
                if (username == null) { redirect(exchange, "/login"); return; }
                handleStockDetail(exchange, username);
            } else if ("/trade".equals(path)) {
                if (username == null) { redirect(exchange, "/login"); return; }
                handleTrade(exchange, method, username);
            } else if ("/portfolio".equals(path)) {
                if (username == null) { redirect(exchange, "/login"); return; }
                handlePortfolio(exchange, username);
            } else if ("/history".equals(path)) {
                if (username == null) { redirect(exchange, "/login"); return; }
                handleHistory(exchange, username);
            } else if ("/leaderboard".equals(path)) {
                if (username == null) { redirect(exchange, "/login"); return; }
                handleLeaderboard(exchange, username);
            } else if ("/settings".equals(path)) {
                if (username == null) { redirect(exchange, "/login"); return; }
                handleSettings(exchange, method, username);
            } else {
                redirect(exchange, "/market");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private String getSessionId(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return null;
        for (String cookie : cookieHeader.split(";")) {
            String[] pair = cookie.trim().split("=");
            if (pair.length == 2 && "sessionid".equals(pair[0])) return pair[1];
        }
        return null;
    }

    private void redirect(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Location", path);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String response = HtmlRenderer.layout("Error", "<div class=\"card\"><h2 style=\"color: var(--danger)\">Error " + code + "</h2><p>" + HtmlRenderer.escape(message) + "</p><a href=\"/login\" class=\"btn btn-primary\">Back to Login</a></div>", null, "light");
        sendResponse(exchange, code, response);
    }

    private void handleRegister(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            String content = "<div style=\"display: flex; justify-content: center; margin-top: 4rem\"><div class=\"card\" style=\"width: 400px; padding: 2rem\"><h2 style=\"text-align: center; margin-bottom: 1.5rem\">Create Account</h2><form method=\"POST\"><div style=\"margin-bottom: 15px\"><label style=\"display: block; font-size: 0.85rem; margin-bottom: 5px; color: var(--text-muted)\">Username</label><input type=\"text\" name=\"username\" required style=\"width: 100%; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px; box-sizing: border-box\"></div><div style=\"margin-bottom: 20px\"><label style=\"display: block; font-size: 0.85rem; margin-bottom: 5px; color: var(--text-muted)\">Password</label><input type=\"password\" name=\"password\" required style=\"width: 100%; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px; box-sizing: border-box\"></div><button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%\">Register</button></form><p style=\"text-align: center; margin-top: 1.5rem; font-size: 0.9rem\">Already have an account? <a href=\"/login\" style=\"color: var(--groww-green); text-decoration: none; font-weight: 600\">Login here</a></p></div></div>";
            sendResponse(exchange, 200, HtmlRenderer.layout("Register", content, null, "light"));
        } else if ("POST".equals(method)) {
            Map<String, String> params = HtmlRenderer.parseFormData(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String user = params.get("username");
            String pass = params.get("password");
            try {
                authService.register(user, pass);
                redirect(exchange, "/login?msg=Registered+successfully");
            } catch (Exception e) {
                sendResponse(exchange, 200, HtmlRenderer.layout("Register", "<div class=\"card\" style=\"width: 400px; margin: 2rem auto\"><h2 style=\"color: var(--danger)\">Registration Failed</h2><p>" + HtmlRenderer.escape(e.getMessage()) + "</p><a href=\"/register\" class=\"btn btn-primary\">Try again</a></div>", "Error: " + e.getMessage(), "light"));
            }
        }
    }

    private void handleLogin(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            String content = "<div style=\"display: flex; justify-content: center; margin-top: 4rem\"><div class=\"card\" style=\"width: 400px; padding: 2rem\"><h2 style=\"text-align: center; margin-bottom: 1.5rem\">Welcome Back</h2><form method=\"POST\"><div style=\"margin-bottom: 15px\"><label style=\"display: block; font-size: 0.85rem; margin-bottom: 5px; color: var(--text-muted)\">Username</label><input type=\"text\" name=\"username\" required style=\"width: 100%; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px; box-sizing: border-box\"></div><div style=\"margin-bottom: 20px\"><label style=\"display: block; font-size: 0.85rem; margin-bottom: 5px; color: var(--text-muted)\">Password</label><input type=\"password\" name=\"password\" required style=\"width: 100%; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px; box-sizing: border-box\"></div><button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%\">Login</button></form><p style=\"text-align: center; margin-top: 1.5rem; font-size: 0.9rem\">Don't have an account? <a href=\"/register\" style=\"color: var(--groww-green); text-decoration: none; font-weight: 600\">Register here</a></p></div></div>";
            sendResponse(exchange, 200, HtmlRenderer.layout("Login", content, null, "light"));
        } else if ("POST".equals(method)) {
            Map<String, String> params = HtmlRenderer.parseFormData(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String user = params.get("username");
            String pass = params.get("password");
            try {
                User u = authService.login(user, pass);
                String sid = sessionManager.createSession(u.getUsername());
                exchange.getResponseHeaders().add("Set-Cookie", "sessionid=" + sid + "; HttpOnly; Path=/");
                redirect(exchange, "/market");
            } catch (Exception e) {
                sendResponse(exchange, 200, HtmlRenderer.layout("Login", "<div class=\"card\" style=\"width: 400px; margin: 2rem auto\"><h2 style=\"color: var(--danger)\">Login Failed</h2><p>" + HtmlRenderer.escape(e.getMessage()) + "</p><a href=\"/login\" class=\"btn btn-primary\">Try again</a></div>", "Error: " + e.getMessage(), "light"));
            }
        }
    }

    private void handleLogout(HttpExchange exchange, String sessionId) throws IOException {
        if (sessionId != null) sessionManager.removeSession(sessionId);
        exchange.getResponseHeaders().add("Set-Cookie", "sessionid=; Max-Age=0; Path=/");
        redirect(exchange, "/login");
    }

    private void handleMarket(HttpExchange exchange, String username) throws IOException {
        User user = users.get(username);
        String theme = user != null ? user.getTheme() : "light";
        StringBuilder content = new StringBuilder("<div class=\"card\"><div class=\"card-header\"><h2>Explore Stocks</h2><span class=\"badge\">Live Market</span></div><div class=\"card-body\">");
        content.append("<p style=\"color: var(--text-muted); margin-bottom: 1.5rem\">Discover and invest in the top companies. Prices update every 3 seconds.</p>");
        content.append("<meta http-equiv=\"refresh\" content=\"3\">");
        content.append("<table><thead><tr><th>Symbol</th><th>Company</th><th>Sector</th><th>Price</th><th>Change</th><th>%</th><th>Trend</th></tr></thead><tbody>");
        for (Stock s : marketService.getAllStocks()) {
            double price = s.getCurrentPrice();
            List<Double> history = s.getPriceHistory();
            double prev = history.size() > 1 ? history.get(history.size() - 2) : price;
            double diff = price - prev;
            double pct = (diff / prev) * 100;
            String colorClass = diff >= 0 ? "up" : "down";
            String sign = diff >= 0 ? "+" : "";
            content.append("<tr>")
                    .append("<td><a href=\"/stock?symbol=" + s.getSymbol() + "\" style=\"color: var(--text-dark); font-weight: 600; text-decoration: none; font-size: 1rem\">" + s.getSymbol() + "</a></td>")
                    .append("<td>").append(s.getCompanyName()).append("</td>")
                    .append("<td><span class=\"badge\">").append(s.getSector()).append("</span></td>")
                    .append("<td style=\"font-weight: 600\">$").append(String.format("%.2f", price)).append("</td>")
                    .append("<td class=\"").append(colorClass).append("\">").append(sign).append(String.format("%.2f", diff)).append("</td>")
                    .append("<td class=\"").append(colorClass).append("\">").append(sign).append(String.format("%.2f", pct)).append("%%</td>")
                    .append("<td>").append(SvgChartBuilder.buildSparkline(history)).append("</td>")
                    .append("</tr>");
        }
        content.append("</tbody></table></div></div>");
        sendResponse(exchange, 200, HtmlRenderer.layout("Market", content.toString(), null, theme));
    }

    private void handleStockDetail(HttpExchange exchange, String username) throws IOException {
        User user = users.get(username);
        String theme = user != null ? user.getTheme() : "light";
        Map<String, String> params = HtmlRenderer.parseFormData(exchange.getRequestURI().getQuery());
        String symbol = params.get("symbol");
        if (symbol == null) { sendError(exchange, 400, "Missing symbol parameter"); return; }
        Stock stock = marketService.getStock(symbol);
        if (stock == null) { sendError(exchange, 404, "Stock not found"); return; }
        List<Double> history = stock.getPriceHistory();
        StringBuilder content = new StringBuilder("<div class=\"card\">");
        content.append("<div class=\"card-header\"><h2>").append(stock.getCompanyName()).append(" <span class=\"badge\">").append(stock.getSymbol()).append("</span></h2><a href=\"/market\" style=\"color: var(--text-muted); text-decoration: none; font-size: 0.8rem\">&larr; Back to Market</a></div>");
        content.append("<div class=\"card-body\">");
        content.append("<p style=\"color: var(--text-muted); margin-bottom: 2rem\">Sector: ").append(stock.getSector()).append(" | Current Price: <b style=\"color: var(--text-dark); font-size: 1.25rem\">$").append(String.format("%.2f", stock.getCurrentPrice())).append("</b></p>");
        content.append("<div style=\"margin-bottom: 2rem\">").append(SvgChartBuilder.buildPerformanceChart(history)).append("</div>");
        content.append("<div class=\"card\" style=\"background: #fbfbfb; border: 1px dashed var(--border-color); padding: 1.5rem\">");
        content.append("<h3 style=\"margin-bottom: 1rem\">Trade " + stock.getSymbol() + "</h3>");
        content.append("<form action=\"/trade\" method=\"POST\">");
        content.append("<input type=\"hidden\" name=\"symbol\" value=\"").append(symbol).append("\">");
        content.append("<div style=\"margin-bottom: 15px\">");
        content.append("<label style=\"display: block; font-size: 0.85rem; margin-bottom: 5px; color: var(--text-muted)\">Quantity to Trade</label>");
        content.append("<input type=\"number\" name=\"quantity\" required style=\"width: 150px; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px; font-size: 1rem;\">");
        content.append("</div>");
        content.append("<div style=\"display: flex; gap: 12px\">");
        content.append("<button type=\"submit\" name=\"action\" value=\"buy\" class=\"btn btn-primary\">Buy</button>");
        content.append("<button type=\"submit\" name=\"action\" value=\"sell\" class=\"btn btn-danger\">Sell</button>");
        content.append("</div></form></div></div></div>");
        sendResponse(exchange, 200, HtmlRenderer.layout("Stock Detail", content.toString(), null, theme));
    }

    private void handleTrade(HttpExchange exchange, String method, String username) throws IOException {
        Map<String, String> params = HtmlRenderer.parseFormData(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        String symbol = params.get("symbol");
        String action = params.get("action");
        String qtyStr = params.get("quantity");
        try {
            int qty = Integer.parseInt(qtyStr);
            if ("buy".equals(action)) {
                tradingService.buyStock(username, symbol, qty);
            } else if ("sell".equals(action)) {
                tradingService.sellStock(username, symbol, qty);
            } else {
                throw new Exception("Invalid action");
            }
            redirect(exchange, "/portfolio?msg=Trade+successful");
        } catch (Exception e) {
            sendResponse(exchange, 200, HtmlRenderer.layout("Trade", "<div class=\"card\" style=\"width: 400px; margin: 2rem auto\"><h2 style=\"color: var(--danger)\">Trade Failed</h2><p>" + HtmlRenderer.escape(e.getMessage()) + "</p><a href=\"/market\" class=\"btn btn-primary\">Back to Market</a></div>", "Error: " + e.getMessage(), "light"));
        }
    }

    private void handlePortfolio(HttpExchange exchange, String username) throws IOException {
        User user = users.get(username);
        String theme = user != null ? user.getTheme() : "light";
        if (user == null) { sendError(exchange, 404, "User not found"); return; }
        double totalValue = portfolioService.calculateTotalValue(username);
        Map<String, Map<String, Holding>> allHoldings = repository.loadHoldings();
        Map<String, Holding> userHoldings = allHoldings.getOrDefault(username, new HashMap<>());
        StringBuilder content = new StringBuilder("<div class=\"stat-grid\" style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem; margin-bottom: 2rem\">");
        content.append("<div class=\"stat-card\"><div class=\"stat-label\">Current Balance</div><div class=\"stat-value\">$").append(String.format("%.2f", user.getBalance())).append("</div></div>");
        content.append("<div class=\"stat-card\"><div class=\"stat-label\">Total Investment Value</div><div class=\"stat-value\">$").append(String.format("%.2f", totalValue)).append("</div></div>");
        content.append("<div class=\"stat-card\"><div class=\"stat-label\">Net Worth</div><div class=\"stat-value\">$").append(String.format("%.2f", totalValue)).append("</div></div>");
        content.append("</div>");
        content.append("<div class=\"card\"><div class=\"card-header\"><h2>Growth Analytics</h2></div><div class=\"card-body\">");
        List<PortfolioSnapshot> snapshots = portfolioService.getSnapshots(username);
        List<Double> values = snapshots.stream().map(PortfolioSnapshot::getTotalValue).collect(java.util.stream.Collectors.toList());
        content.append("<div style=\"margin: 20px 0\">").append(SvgChartBuilder.buildPerformanceChart(values)).append("</div></div></div>");
        content.append("<div class=\"card\"><div class=\"card-header\"><h2>My Holdings</h2></div><div class=\"card-body\">");
        content.append("<table><thead><tr><th>Symbol</th><th>Quantity</th><th>Avg. Cost</th><th>Current Price</th><th>Market Value</th><th>P/L</th></tr></thead><tbody>");
        for (Holding h : userHoldings.values()) {
            Stock s = marketService.getStock(h.getSymbol());
            double currentPrice = s != null ? s.getCurrentPrice() : 0;
            double marketValue = h.getQuantity() * currentPrice;
            double pl = marketValue - (h.getQuantity() * h.getAverageCost());
            String colorClass = pl >= 0 ? "up" : "down";
            content.append("<tr>")
                    .append("<td style=\"font-weight: 600\">").append(h.getSymbol()).append("</td>")
                    .append("<td>").append(h.getQuantity()).append("</td>")
                    .append("<td>$").append(String.format("%.2f", h.getAverageCost())).append("</td>")
                    .append("<td>$").append(String.format("%.2f", currentPrice)).append("</td>")
                    .append("<td>$").append(String.format("%.2f", marketValue)).append("</td>")
                    .append("<td class=\"").append(colorClass).append("\">").append(String.format("%.2f", pl)).append("</td>")
                    .append("</tr>");
        }
        content.append("</tbody></table></div></div>");
        sendResponse(exchange, 200, HtmlRenderer.layout("Portfolio", content.toString(), null, theme));
    }

    private void handleHistory(HttpExchange exchange, String username) throws IOException {
        User user = users.get(username);
        String theme = user != null ? user.getTheme() : "light";
        List<Transaction> allTx = repository.loadTransactions();
        List<Transaction> userTx = allTx.stream().filter(t -> t.getUsername().equals(username)).collect(java.util.stream.Collectors.toList());
        StringBuilder content = new StringBuilder("<div class=\"card\"><div class=\"card-header\"><h2>Transaction History</h2></div><div class=\"card-body\">");
        content.append("<table><thead><tr><th>Time</th><th>Type</th><th>Symbol</th><th>Qty</th><th>Price</th><th>Total</th><th>P/L</th></tr></thead><tbody>");
        for (Transaction t : userTx) {
            content.append("<tr>")
                    .append("<td>").append(t.getTimestamp()).append("</td>")
                    .append("<td><span class=\"badge\" style=\"background: ").append(t.getType().equals("BUY") ? "#dcfce7" : "#fee2e2").append("; color: ").append(t.getType().equals("BUY") ? "#166534" : "#991b1b").append("\">").append(t.getType()).append("</span></td>")
                    .append("<td>").append(t.getSymbol()).append("</td>")
                    .append("<td>").append(t.getQuantity()).append("</td>")
                    .append("<td>$").append(String.format("%.2f", t.getPrice())).append("</td>")
                    .append("<td>$").append(String.format("%.2f", t.getTotal())).append("</td>")
                    .append("<td>$").append(String.format("%.2f", t.getRealizedPL())).append("</td>")
                    .append("</tr>");
        }
        content.append("</tbody></table></div></div>");
        sendResponse(exchange, 200, HtmlRenderer.layout("History", content.toString(), null, theme));
    }

    private void handleLeaderboard(HttpExchange exchange, String username) throws IOException {
        User user = users.get(username);
        String theme = user != null ? user.getTheme() : "light";
        List<Map.Entry<String, Double>> leaders = portfolioService.getLeaderboard();
        StringBuilder content = new StringBuilder("<div class=\"card\"><div class=\"card-header\"><h2>Global Leaderboard</h2></div><div class=\"card-body\">");
        content.append("<table><thead><tr><th>Rank</th><th>Investor</th><th>Total Net Worth</th></tr></thead><tbody>");
        int rank = 1;
        for (Map.Entry<String, Double> entry : leaders) {
            content.append("<tr>")
                    .append("<td style=\"font-weight: 600\">#").append(rank++).append("</td>")
                    .append("<td>").append(entry.getKey()).append("</td>")
                    .append("<td style=\"font-weight: 700; color: var(--groww-green)\">$").append(String.format("%.2f", entry.getValue())).append("</td>")
                    .append("</tr>");
        }
        content.append("</tbody></table></div></div>");
        sendResponse(exchange, 200, HtmlRenderer.layout("Leaderboard", content.toString(), null, theme));
    }

    private void handleSettings(HttpExchange exchange, String method, String username) throws IOException {
        User user = users.get(username);
        String theme = user != null ? user.getTheme() : "light";

        if ("POST".equals(method)) {
            Map<String, String> params = HtmlRenderer.parseFormData(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String action = params.get("action");
            if ("toggleTheme".equals(action)) {
                user.setTheme(user.getTheme().equalsIgnoreCase("light") ? "dark" : "light");
                repository.saveUsers(users);
            } else if ("updateUpi".equals(action)) {
                String upi = params.get("upiId");
                if (upi != null && upi.contains("@")) {
                    user.setUpiId(upi);
                    repository.saveUsers(users);
                }
            }
            redirect(exchange, "/settings?msg=Settings+updated");
            return;
        }

        StringBuilder content = new StringBuilder("<div class=\"card\"><div class=\"card-header\"><h2>User Settings</h2></div><div class=\"card-body\">");

        // Theme Section
        content.append("<div style=\"margin-bottom: 2rem\">");
        content.append("<h3 style=\"margin-bottom: 1rem\">Appearance</h3>");
        content.append("<div style=\"display: flex; align-items: center; gap: 15px\">");
        content.append("<span>Current Theme: <b>").append(user.getTheme().toUpperCase()).append("</b></span>");
        content.append("<form action=\"/settings\" method=\"POST\" style=\"display: inline\">");
        content.append("<input type=\"hidden\" name=\"action\" value=\"toggleTheme\">");
        content.append("<button type=\"submit\" class=\"btn btn-outline\">Toggle Theme</button>");
        content.append("</form></div></div>");

        // UPI Section
        content.append("<div style=\"margin-bottom: 1rem\">");
        content.append("<h3 style=\"margin-bottom: 1rem\">Payment Method (UPI)</h3>");
        content.append("<form action=\"/settings\" method=\"POST\" style=\"display: flex; gap: 10px\">");
        content.append("<input type=\"text\" name=\"upiId\" value=\"").append(user.getUpiId()).append("\" placeholder=\"username@bank\" required style=\"flex: 1; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px\">");
        content.append("<input type=\"hidden\" name=\"action\" value=\"updateUpi\">");
        content.append("<button type=\"submit\" class=\"btn btn-primary\">Save UPI</button>");
        content.append("</form></div>");

        content.append("</div>");
        sendResponse(exchange, 200, HtmlRenderer.layout("Settings", content.toString(), null, theme));
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        if (response == null) response = "";
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
            os.flush();
        }
    }
}
