package com.smartpos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {}

class DatabaseConnection {
    // IMPORTANT: Update these with your local MySQL credentials
    private static final String URL = "jdbc:mysql://localhost:3306/smartpos?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "P@ssw0rd"; // CHANGE THIS

    private static Connection connection = null;

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Register JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Open a connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Ensure mysql-connector-j is on the classpath.", e);
            }
        }
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        return connection;
    }

    public static boolean checkConnection() {
        try (Connection testConn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return testConn != null && !testConn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

class ProductDao {
    public List<Product> getAllProducts() {
        return getProductsByCategory(null);
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String sql = (category == null || category.equalsIgnoreCase("All"))
                ? "SELECT * FROM products"
                : "SELECT * FROM products WHERE category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (category != null && !category.equalsIgnoreCase("All")) {
                stmt.setString(1, category);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (Exception e) {
            System.err.println("[ProductDao] Error loading products by category '" + category + "': " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    public Product getProductByBarcodeOrId(String query) {
        String sql = "SELECT * FROM products WHERE barcode = ? OR id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, query);
            try {
                stmt.setInt(2, Integer.parseInt(query));
            } catch (NumberFormatException e) {
                stmt.setInt(2, -1);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }
        } catch (Exception e) {
            System.err.println("[ProductDao] Error looking up product by barcode/id '" + query + "': " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (Exception e) {
            System.err.println("[ProductDao] Error searching products for '" + query + "': " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (name, category, price, stock_qty, barcode, image_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStockQty());
            stmt.setString(5, product.getBarcode());
            stmt.setString(6, product.getImagePath());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[ProductDao] Error adding product '" + product.getName() + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Update an existing product's name, category, price, stock and barcode. */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name=?, category=?, price=?, stock_qty=?, barcode=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStockQty());
            stmt.setString(5, product.getBarcode());
            stmt.setInt(6, product.getId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[ProductDao] Error updating product id=" + product.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Fast stock-only update. */
    public boolean updateProductStock(int productId, int newStock) {
        String sql = "UPDATE products SET stock_qty=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[ProductDao] Error updating stock for id=" + productId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getBigDecimal("price"),
                rs.getInt("stock_qty"),
                rs.getString("barcode"),
                rs.getString("image_path"));
    }
}

class TransactionDao {
    /**
     * Executes a checkout transaction.
     * Deducts stock and inserts records into transactions and transaction_items tables.
     * Uses a single database transaction (setAutoCommit(false)).
     */
    public boolean processCheckout(Transaction transaction, List<TransactionItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into transactions table
            String insertTxSql = "INSERT INTO transactions (terminal_id, employee_id, item_total, gst_amount, discount, final_amount, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int generatedTransactionId = -1;
            try (PreparedStatement txStmt = conn.prepareStatement(insertTxSql, Statement.RETURN_GENERATED_KEYS)) {
                txStmt.setString(1, transaction.getTerminalId());
                txStmt.setInt(2, transaction.getEmployeeId());
                txStmt.setBigDecimal(3, transaction.getItemTotal());
                txStmt.setBigDecimal(4, transaction.getGstAmount());
                txStmt.setBigDecimal(5, transaction.getDiscount());
                txStmt.setBigDecimal(6, transaction.getFinalAmount());
                txStmt.setString(7, transaction.getPaymentMethod());

                txStmt.executeUpdate();

                try (ResultSet rs = txStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedTransactionId = rs.getInt(1);
                        transaction.setId(generatedTransactionId);
                    } else {
                        throw new SQLException("Failed to retrieve transaction ID.");
                    }
                }
            }

            // 2. Insert into transaction_items and deduct stock
            String insertItemSql = "INSERT INTO transaction_items (transaction_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
            String updateStockSql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";

            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql);
                    PreparedStatement stockStmt = conn.prepareStatement(updateStockSql)) {

                for (TransactionItem item : items) {
                    // Insert item
                    itemStmt.setInt(1, generatedTransactionId);
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getUnitPrice());
                    itemStmt.setBigDecimal(5, item.getLineTotal());
                    itemStmt.addBatch();

                    // Deduct stock
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProductId());
                    stockStmt.setInt(3, item.getQuantity()); // Ensure enough stock exists
                    stockStmt.addBatch();
                }

                itemStmt.executeBatch();

                // Check stock update results to ensure no negative stock
                int[] stockUpdateResults = stockStmt.executeBatch();
                for (int res : stockUpdateResults) {
                    if (res == 0) {
                        throw new SQLException("Insufficient stock for one or more items.");
                    }
                }
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Returns the most recent transactions, newest first. */
    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getString("terminal_id"),
                        rs.getInt("employee_id"),
                        rs.getBigDecimal("item_total"),
                        rs.getBigDecimal("gst_amount"),
                        rs.getBigDecimal("discount"),
                        rs.getBigDecimal("final_amount"),
                        rs.getString("payment_method"),
                        rs.getTimestamp("created_at")));
            }
        } catch (Exception e) {
            System.err.println("[TransactionDao] Error loading transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /** Returns total sales amount and count for today. */
    public Object[] getTodaySummary() {
        String sql = "SELECT COUNT(*) as cnt, COALESCE(SUM(final_amount),0) as total FROM transactions WHERE DATE(created_at)=CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Object[]{ rs.getInt("cnt"), rs.getBigDecimal("total") };
            }
        } catch (Exception e) {
            System.err.println("[TransactionDao] Error getting today summary: " + e.getMessage());
        }
        return new Object[]{ 0, java.math.BigDecimal.ZERO };
    }
}

class UserDao {
    public User authenticate(String employeeId, String password, String role) {
        String sql = "SELECT * FROM users WHERE employee_id = ? AND password_hash = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            stmt.setString(2, password); // Note: in real app this should be hashed
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("employee_id"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getFirstCashier() {
        String sql = "SELECT * FROM users WHERE role = 'CASHIER' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("employee_id"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
