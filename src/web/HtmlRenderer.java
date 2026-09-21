package web;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HtmlRenderer {
    public static String escape(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    public static String layout(String title, String content, String flashMessage) {
        // Default to light theme if not specified
        return layout(title, content, flashMessage, "light");
    }

    public static String layout(String title, String content, String flashMessage, String theme) {
        String themeClass = (theme != null && theme.equalsIgnoreCase("dark")) ? "dark-theme" : "light-theme";

        return """
        <!DOCTYPE html>
        <html lang=\"en\">
        <head>
            <meta charset=\"UTF-8\">
            <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
            <title>%s - StockSim</title>
            <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">
            <style>
                :root {
                    --groww-green: #00d09c;
                    --groww-green-dark: #00b386;
                    --danger: #eb5757;
                }
                .light-theme {
                    --text-dark: #44475b;
                    --text-muted: #7c7e8c;
                    --bg-light: #ffffff;
                    --bg-gray: #fbfbfb;
                    --border-color: #ebebeb;
                }
                .dark-theme {
                    --text-dark: #e2e8f0;
                    --text-muted: #94a3b8;
                    --bg-light: #1e293b;
                    --bg-gray: #0f172a;
                    --border-color: #334155;
                }
                body {
                    font-family: 'Inter', sans-serif;
                    margin: 0;
                    background: var(--bg-gray);
                    color: var(--text-dark);
                    -webkit-font-smoothing: antialiased;
                    transition: background 0.3s, color 0.3s;
                }
                nav {
                    background: var(--bg-light);
                    border-bottom: 1px solid var(--border-color);
                    padding: 0 2rem;
                    height: 64px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    position: sticky;
                    top: 0;
                    z-index: 100;
                }
                .nav-brand {
                    font-size: 1.5rem;
                    font-weight: 700;
                    color: var(--groww-green);
                    text-decoration: none;
                    display: flex;
                    align-items: center;
                }
                .nav-links { display: flex; gap: 30px; align-items: center; }
                .nav-links a {
                    color: var(--text-dark);
                    text-decoration: none;
                    font-size: 0.9rem;
                    font-weight: 500;
                    transition: color 0.2s;
                }
                .nav-links a:hover { color: var(--groww-green); }
                .container { max-width: 1200px; margin: 0 auto; padding: 2rem 1.5rem; }

                .card {
                    background: var(--bg-light);
                    border-radius: 12px;
                    border: 1px solid var(--border-color);
                    margin-bottom: 1.5rem;
                    overflow: hidden;
                }
                .card-header {
                    padding: 1.5rem;
                    border-bottom: 1px solid var(--border-color);
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .card-body { padding: 1.5rem; }

                h1, h2, h3 { color: var(--text-dark); font-weight: 600; margin: 0; }

                table { width: 100%%; border-collapse: collapse; }
                th {
                    text-align: left;
                    padding: 12px 16px;
                    font-size: 0.8rem;
                    color: var(--text-muted);
                    border-bottom: 1px solid var(--border-color);
                    font-weight: 500;
                }
                td { padding: 16px; border-bottom: 1px solid var(--border-color); font-size: 0.9rem; }

                .btn {
                    padding: 10px 20px;
                    border-radius: 8px;
                    border: none;
                    cursor: pointer;
                    font-weight: 600;
                    font-size: 0.9rem;
                    transition: all 0.2s;
                }
                .btn-primary { background: var(--groww-green); color: white; }
                .btn-primary:hover { background: var(--groww-green-dark); }
                .btn-outline { background: white; border: 1px solid var(--groww-green); color: var(--groww-green); }
                .btn-outline:hover { background: #f0fffb; }
                .btn-danger { background: white; border: 1px solid var(--danger); color: var(--danger); }
                .btn-danger:hover { background: #fff5f5; }

                .up { color: var(--groww-green); font-weight: 600; }
                .down { color: var(--danger); font-weight: 600; }

                .stat-card {
                    background: var(--bg-light);
                    padding: 1.5rem;
                    border-radius: 12px;
                    border: 1px solid var(--border-color);
                    text-align: center;
                }
                .stat-label { color: var(--text-muted); font-size: 0.85rem; margin-bottom: 8px; }
                .stat-value { font-size: 1.75rem; font-weight: 700; color: var(--text-dark); }

                .flash { padding: 1rem; border-radius: 8px; margin-bottom: 1.5rem; font-size: 0.9rem; text-align: center; }
                .flash-success { background: #e6fcf5; color: #0ca678; border: 1px solid #c3fae8; }
                .flash-error { background: #fff5f5; color: #fa5252; border: 1px solid #ffe3e3; }

                .badge {
                    padding: 4px 8px;
                    border-radius: 4px;
                    font-size: 0.75rem;
                    background: var(--border-color);
                    color: var(--text-muted);
                }
            </style>
        </head>
        <body class=\"%s\">
            <nav>
                <a href=\"/market\" class=\"nav-brand\">GrowwSim</a>
                <div class=\"nav-links\">
                    <a href=\"/market\">Explore</a>
                    <a href=\"/portfolio\">Investments</a>
                    <a href=\"/leaderboard\">Rankings</a>
                    <a href=\"/settings\">Settings</a>
                    <a href=\"/logout\">Logout</a>
                </div>
            </nav>
            <div class=\"container\">
                %s
                <div class=\"content\">%s</div>
            </div>
        </body>
        </html>
        """.formatted(title, flashMessage != null ? String.format("<div class=\"flash %s\">%s</div>", flashMessage.contains("Error") ? "flash-error" : "flash-success", escape(flashMessage)) : "", content, themeClass);
    }

    public static Map<String, String> parseFormData(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}
