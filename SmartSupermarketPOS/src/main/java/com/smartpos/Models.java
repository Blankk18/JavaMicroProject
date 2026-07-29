package com.smartpos;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Models {
}

class Product {
    private int id;
    private String name;
    private String category;
    private BigDecimal price;
    private int stockQty;
    private String barcode;
    private String imagePath;

    public Product(int id, String name, String category, BigDecimal price, int stockQty, String barcode,
            String imagePath) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQty = stockQty;
        this.barcode = barcode;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQty() {
        return stockQty;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}

class Transaction {
    private int id;
    private String terminalId;
    private int employeeId;
    private BigDecimal itemTotal;
    private BigDecimal gstAmount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private Timestamp createdAt;

    public Transaction(int id, String terminalId, int employeeId, BigDecimal itemTotal, BigDecimal gstAmount,
            BigDecimal discount, BigDecimal finalAmount, String paymentMethod, Timestamp createdAt) {
        this.id = id;
        this.terminalId = terminalId;
        this.employeeId = employeeId;
        this.itemTotal = itemTotal;
        this.gstAmount = gstAmount;
        this.discount = discount;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }
}

class TransactionItem {
    private int id;
    private int transactionId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // Additional property not in DB but useful for UI
    private String productName;

    public TransactionItem(int id, int transactionId, int productId, int quantity, BigDecimal unitPrice,
            BigDecimal lineTotal) {
        this.id = id;
        this.transactionId = transactionId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public int getId() {
        return id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}

class User {
    private int id;
    private String employeeId;
    private String passwordHash;
    private String role;
    private String name;

    public User(int id, String employeeId, String passwordHash, String role, String name) {
        this.id = id;
        this.employeeId = employeeId;
        this.passwordHash = passwordHash;
        this.role = role;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }
}
