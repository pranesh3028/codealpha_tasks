package web;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    private final HttpServer server;
    private final Router router;

    public WebServer(int port, Router router) throws IOException {
        this.router = router;
        // Bind to 0.0.0.0 to ensure it listens on all local network interfaces
        this.server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port), 0);
        this.server.createContext("/", exchange -> {
            try {
                router.route(exchange);
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
            }
        });
        // Use a thread pool instead of null to prevent request blocking/dropping
        this.server.setExecutor(Executors.newFixedThreadPool(10));
    }

    public void start() {
        System.out.println("Server started on port " + server.getAddress().getPort());
        System.out.println("Try: http://127.0.0.1:" + server.getAddress().getPort() + "/login");
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
