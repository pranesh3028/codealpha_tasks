# 📈 StockSim - Professional Stock Trading Platform

A high-performance, full-stack simulated stock trading platform built entirely in **Java 17+**. This application demonstrates advanced concepts in Server-Side Rendering (SSR), real-time data simulation, and secure user session management—all without using any external frameworks or libraries.

![Project Banner](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=java)
![UI Style](https://img.shields.io/badge/UI-Groww--Style-green?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

## 🌟 Key Features

### 🚀 Real-Time Market Simulation
- **Live Price Ticks**: A background `ScheduledExecutorService` simulates market volatility with random-walk price updates every 3 seconds.
- **Dynamic Analytics**: Instant calculation of price changes, percentage shifts, and trend analysis.

### 🎨 Modern "Groww-Style" UI
- **Minimalist Design**: A clean, professional interface inspired by modern fintech platforms.
- **Dark & Light Themes**: Fully persistent theme switching implemented via CSS variables and user preferences.
- **SVG Data Visualization**: Custom-built SVG engine to generate:
    - **Sparklines**: Mini-charts for quick trend visualization in the market table.
    - **Performance Graphs**: High-resolution area charts for portfolio growth and stock history.

### 💼 Trading & Portfolio Management
- **Trading Engine**: Thread-safe buy/sell operations with real-time balance and holding updates.
- **Portfolio Tracking**: Detailed breakdown of holdings, average cost basis, and unrealized Profit/Loss (P/L).
- **Global Leaderboard**: Competitive ranking of investors based on total net worth.

### 🛡️ Security & Persistence
- **Secure Auth**: Password security using **Salted SHA-256 hashing**.
- **Session Management**: Cookie-based session tracking for authenticated access.
- **Atomic Persistence**: File-based CSV storage with atomic write operations to prevent data corruption.

## 🛠️ Technical Stack

- **Language**: Java 17 (Standard Edition)
- **Web Server**: `com.sun.net.httpserver.HttpServer`
- **Frontend**: Server-Side Rendered (SSR) HTML5 / CSS3 / SVG
- **Persistence**: CSV File-based I/O
- **Concurrency**: `ConcurrentHashMap`, `ScheduledExecutorService`, `synchronized` blocks

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher installed.

### Running the Application
1. **Clone the repository**:
   ```bash
   git clone https://github.com/pranesh3028/codealpha_tasks.git
   cd codealpha_tasks
   ```

2. **Compile the source code**:
   ```bash
   javac -d out $(find src -name "*.java")
   ```

3. **Run the server**:
   ```bash
   java -cp out Main.Main
   ```

4. **Access the app**:
   Open your browser and go to: `http://127.0.0.1:9000/login`

## 📂 Project Structure

```text
codealpha_tasks/
├── src/
│   ├── Main.java               # Application entry point
│   ├── model/                  # Data entities (User, Stock, Transaction)
│   ├── service/                # Business logic (Trading, Market, Auth)
│   ├── persistence/            # CSV Data handling
│   └── web/                    # SSR Engine, Router, and SVG Builder
└── data/                       # Local CSV storage (created on run)
```
