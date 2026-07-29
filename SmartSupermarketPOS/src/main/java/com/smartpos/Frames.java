package com.smartpos;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.math.RoundingMode;
import java.sql.Timestamp;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.image.BufferedImage;

public class Frames {
}

class AdminDashboardFrame extends JFrame {

    private ProductDao productDao;
    private TransactionDao transactionDao;

    private DefaultTableModel productTableModel;
    private JTable productTable;

    private DefaultTableModel txModel;
    private JLabel todayTxLabel;
    private JLabel todayAmtLabel;
    private JComboBox<String> limitBox;

    public AdminDashboardFrame(User user) {
        this.productDao = new ProductDao();
        this.transactionDao = new TransactionDao();

        setTitle("Admin Dashboard - " + user.getName());
        setIconImage(createAppIcon());
        Dimension adminScreen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(adminScreen.width, adminScreen.height);
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(AppColors.BG_BASE);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setForeground(AppColors.TEXT_PRIMARY);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topBar.add(titleLabel, BorderLayout.WEST);

        GradientButton logoutBtn = new GradientButton("Logout");
        logoutBtn.setColors(AppColors.ACCENT_DANGER, new Color(192, 57, 43));
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        topBar.add(logoutBtn, BorderLayout.EAST);

        mainPanel.add(topBar, BorderLayout.NORTH);

        JPanel contentArea = new JPanel(new CardLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JPanel manageProductsPanel = createProductManagementPanel();
        JPanel salesReportsPanel = createReportsPanel();

        contentArea.add(manageProductsPanel, "PRODUCTS");
        contentArea.add(salesReportsPanel, "REPORTS");

        JPanel tabArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        tabArea.setOpaque(false);
        tabArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        GradientButton productsTab = new GradientButton("Manage Products");
        productsTab.setColors(AppColors.ACCENT_1, AppColors.ACCENT_2);

        GradientButton reportsTab = new GradientButton("Sales Reports");
        reportsTab.setColors(AppColors.BG_SURFACE_ALT, AppColors.BG_SURFACE_ALT);

        CardLayout cl = (CardLayout) contentArea.getLayout();

        productsTab.addActionListener(e -> {
            productsTab.setColors(AppColors.ACCENT_1, AppColors.ACCENT_2);
            reportsTab.setColors(AppColors.BG_SURFACE_ALT, AppColors.BG_SURFACE_ALT);
            cl.show(contentArea, "PRODUCTS");
        });

        reportsTab.addActionListener(e -> {
            reportsTab.setColors(AppColors.ACCENT_1, AppColors.ACCENT_2);
            productsTab.setColors(AppColors.BG_SURFACE_ALT, AppColors.BG_SURFACE_ALT);
            cl.show(contentArea, "REPORTS");
        });

        tabArea.add(productsTab);
        tabArea.add(reportsTab);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(tabArea, BorderLayout.NORTH);
        centerWrapper.add(contentArea, BorderLayout.CENTER);

        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private Image createAppIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppColors.ACCENT_1);
        g2.fillRoundRect(4, 4, 56, 56, 16, 16);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("🛒", 16, 32 + fm.getAscent() / 2 - 2);
        g2.dispose();
        return img;
    }

    private JPanel createProductManagementPanel() {
        RoundedPanel panel = new RoundedPanel(15, AppColors.BG_SURFACE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = { "ID", "Name", "Category", "Price", "Stock", "Barcode" };
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        productTable = new JTable(productTableModel);
        productTable.setBackground(AppColors.BG_SURFACE_ALT);
        productTable.setForeground(AppColors.TEXT_PRIMARY);
        productTable.setFillsViewportHeight(true);
        productTable.setRowHeight(30);
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));
        productTable.getTableHeader().setBackground(AppColors.BG_BASE);
        productTable.getTableHeader().setForeground(AppColors.TEXT_SECONDARY);
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        productTable.setDefaultRenderer(Object.class, new AppTableRenderer());

        loadProductsIntoTable();

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.getViewport().setBackground(AppColors.BG_SURFACE_ALT);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 15));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel rowBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rowBtnPanel.setOpaque(false);

        GradientButton updateStockBtn = new GradientButton("Update Stock");
        updateStockBtn.setColors(AppColors.ACCENT_SUCCESS, new Color(39, 174, 96));
        updateStockBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row < 0) {
                AppDialog.showMessageDialog(this, "Select a product first.");
                return;
            }
            int id = (int) productTableModel.getValueAt(row, 0);
            String name = (String) productTableModel.getValueAt(row, 1);
            int current = (int) productTableModel.getValueAt(row, 4);
            String input = AppDialog.showInputDialog(this,
                    "New stock quantity for \"" + name + "\":", current);
            if (input == null || input.trim().isEmpty())
                return;
            try {
                int newStock = Integer.parseInt(input.trim());
                if (productDao.updateProductStock(id, newStock)) {
                    AppDialog.showMessageDialog(this, "Stock updated to " + newStock + "!");
                    loadProductsIntoTable();
                } else {
                    AppDialog.showMessageDialog(this, "Update failed.", "Error", AppDialog.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                AppDialog.showMessageDialog(this, "Enter a valid number.", "Error", AppDialog.ERROR_MESSAGE);
            }
        });

        GradientButton editProductBtn = new GradientButton("Edit Product");
        editProductBtn.setColors(AppColors.ACCENT_INFO, new Color(41, 128, 185));
        editProductBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row < 0) {
                AppDialog.showMessageDialog(this, "Select a product first.");
                return;
            }
            int id = (int) productTableModel.getValueAt(row, 0);
            String name = (String) productTableModel.getValueAt(row, 1);
            String cat = (String) productTableModel.getValueAt(row, 2);
            String priceStr = productTableModel.getValueAt(row, 3).toString();
            int stock = (int) productTableModel.getValueAt(row, 4);
            String barcode = (String) productTableModel.getValueAt(row, 5);

            JTextField nf = new JTextField(name);
            JTextField cf = new JTextField(cat);
            JTextField pf = new JTextField(priceStr);
            JTextField sf = new JTextField(String.valueOf(stock));
            JTextField bf = new JTextField(barcode);
            Object[] msg = { "Name:", nf, "Category:", cf, "Price:", pf, "Stock:", sf, "Barcode:", bf };
            int res = AppDialog.showConfirmDialog(this, msg, "Edit Product #" + id, AppDialog.OK_CANCEL_OPTION);
            if (res == AppDialog.OK_OPTION) {
                try {
                    Product updated = new Product(id, nf.getText(), cf.getText(),
                            new java.math.BigDecimal(pf.getText()), Integer.parseInt(sf.getText()),
                            bf.getText(), "");
                    if (productDao.updateProduct(updated)) {
                        AppDialog.showMessageDialog(this, "Product updated!");
                        loadProductsIntoTable();
                    } else {
                        AppDialog.showMessageDialog(this, "Update failed.", "Error", AppDialog.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    AppDialog.showMessageDialog(this, "Invalid input.", "Error", AppDialog.ERROR_MESSAGE);
                }
            }
        });

        GradientButton refreshBtn = new GradientButton("Refresh");
        refreshBtn.setColors(AppColors.BG_SURFACE_ALT, AppColors.BG_SURFACE_ALT);
        refreshBtn.addActionListener(e -> loadProductsIntoTable());

        rowBtnPanel.add(updateStockBtn);
        rowBtnPanel.add(editProductBtn);
        rowBtnPanel.add(refreshBtn);
        bottomPanel.add(rowBtnPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 5, 10, 5));
        formPanel.setOpaque(false);

        JTextField nameField = createField("Name");
        JTextField catField = createField("Category");
        JTextField priceField = createField("Price");
        JTextField stockField = createField("Stock");
        JTextField barcodeField = createField("Barcode");

        GradientButton addBtn = new GradientButton("Add Product");
        addBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String cat = catField.getText().trim();
                if (name.isEmpty() || cat.isEmpty())
                    throw new Exception("Name/Category required");
                java.math.BigDecimal price = new java.math.BigDecimal(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                String barcode = barcodeField.getText().trim();

                Product p = new Product(0, name, cat, price, stock, barcode, "");
                if (productDao.addProduct(p)) {
                    AppDialog.showMessageDialog(this, "Product Added!");
                    loadProductsIntoTable();
                    nameField.setText("");
                    catField.setText("");
                    priceField.setText("");
                    stockField.setText("");
                    barcodeField.setText("");
                } else {
                    AppDialog.showMessageDialog(this, "Failed to add product");
                }
            } catch (Exception ex) {
                AppDialog.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        });

        formPanel.add(createFormLabel("Name"));
        formPanel.add(createFormLabel("Category"));
        formPanel.add(createFormLabel("Price (\u20B9)"));
        formPanel.add(createFormLabel("Stock"));
        formPanel.add(createFormLabel("Barcode"));

        formPanel.add(nameField);
        formPanel.add(catField);
        formPanel.add(priceField);
        formPanel.add(stockField);
        formPanel.add(barcodeField);

        JPanel formRow = new JPanel(new BorderLayout(15, 0));
        formRow.setOpaque(false);
        formRow.add(formPanel, BorderLayout.CENTER);

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        btnWrapper.add(addBtn, BorderLayout.CENTER);
        formRow.add(btnWrapper, BorderLayout.EAST);

        bottomPanel.add(formRow, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    private void loadProductsIntoTable() {
        productTableModel.setRowCount(0);
        List<Product> products = productDao.getAllProducts();
        for (Product p : products) {
            productTableModel.addRow(new Object[] {
                    p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStockQty(), p.getBarcode()
            });
        }
    }

    private JPanel createReportsPanel() {
        RoundedPanel panel = new RoundedPanel(15, AppColors.BG_SURFACE);
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        RoundedPanel summaryBar = new RoundedPanel(10, AppColors.BG_BASE);
        summaryBar.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        summaryBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        todayTxLabel = new JLabel("Today's Sales: loading...");
        todayAmtLabel = new JLabel();
        todayTxLabel.setForeground(AppColors.TEXT_PRIMARY);
        todayTxLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        todayAmtLabel.setForeground(AppColors.ACCENT_SUCCESS);
        todayAmtLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryBar.add(todayTxLabel);
        summaryBar.add(todayAmtLabel);
        panel.add(summaryBar, BorderLayout.NORTH);

        String[] cols = { "Tx ID", "Date & Time", "Terminal", "Subtotal", "GST", "Final Amount", "Payment" };
        txModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable txTable = new JTable(txModel);
        txTable.setBackground(AppColors.BG_SURFACE_ALT);
        txTable.setForeground(AppColors.TEXT_PRIMARY);
        txTable.setFillsViewportHeight(true);
        txTable.setRowHeight(30);
        txTable.setShowGrid(false);
        txTable.setIntercellSpacing(new Dimension(0, 0));
        txTable.getTableHeader().setBackground(AppColors.BG_BASE);
        txTable.getTableHeader().setForeground(AppColors.TEXT_SECONDARY);
        txTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        txTable.setDefaultRenderer(Object.class, new AppTableRenderer());

        JScrollPane scrollPane = new JScrollPane(txTable);
        scrollPane.getViewport().setBackground(AppColors.BG_SURFACE_ALT);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel ctrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        ctrlPanel.setOpaque(false);

        JLabel limitLabel = new JLabel("Show last:");
        limitLabel.setForeground(AppColors.TEXT_SECONDARY);
        limitLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        String[] limits = { "50", "100", "200", "500" };
        limitBox = new JComboBox<>(limits);
        limitBox.setBackground(AppColors.BG_SURFACE_ALT);
        limitBox.setForeground(AppColors.TEXT_PRIMARY);

        GradientButton refreshBtn = new GradientButton("Refresh");
        refreshBtn.setColors(AppColors.BG_SURFACE_ALT, AppColors.BG_SURFACE_ALT);

        refreshBtn.addActionListener(e -> loadReport());
        ctrlPanel.add(limitLabel);
        ctrlPanel.add(limitBox);
        ctrlPanel.add(refreshBtn);
        panel.add(ctrlPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::loadReport);

        return panel;
    }

    private void loadReport() {
        if (limitBox == null)
            return;
        int limit = Integer.parseInt((String) limitBox.getSelectedItem());
        List<Transaction> txList = transactionDao.getRecentTransactions(limit);
        txModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        for (Transaction tx : txList) {
            txModel.addRow(new Object[] {
                    tx.getId(),
                    sdf.format(tx.getCreatedAt()),
                    tx.getTerminalId(),
                    String.format("\u20B9 %.2f", tx.getItemTotal()),
                    String.format("\u20B9 %.2f", tx.getGstAmount()),
                    String.format("\u20B9 %.2f", tx.getFinalAmount()),
                    tx.getPaymentMethod()
            });
        }
        Object[] summary = transactionDao.getTodaySummary();
        int cnt = (int) summary[0];
        java.math.BigDecimal total = (java.math.BigDecimal) summary[1];
        todayTxLabel.setText("Today: " + cnt + " transaction" + (cnt == 1 ? "" : "s"));
        todayAmtLabel.setText("  Total: \u20B9 " + total.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    private JTextField createField(String toolTip) {
        JTextField field = new JTextField();
        field.setToolTipText(toolTip);
        field.setBackground(AppColors.BG_BASE);
        field.setForeground(AppColors.TEXT_PRIMARY);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return field;
    }
}

class LoginFrame extends JFrame {

    private JLabel statusLabel;
    private JPanel statusPill;
    private JTextField idField;
    private JPasswordField passField;
    private ToggleSwitch roleSwitch;
    private UserDao userDao;
    private RoundedPanel loginCard;
    private JPanel centerWrapper;
    private float cardOpacity = 0f;
    private int cardYOffset = 20;

    private int headerYOffset = -30;
    private int visibleBadgeCount = 0;
    private List<JPanel> badgePanels = new ArrayList<>();

    public LoginFrame() {
        userDao = new UserDao();
        roleSwitch = new ToggleSwitch();

        setIconImage(createAppIcon());

        setTitle("Smart Supermarket POS & Self-Checkout v1.0");
        setUndecorated(true);
        Dimension loginScreen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(loginScreen.width, loginScreen.height);
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            private ImageIcon bgIcon;
            {
                try {
                    java.net.URL imgUrl = getClass().getResource("/images/bg.gif");
                    if (imgUrl != null) {
                        bgIcon = new ImageIcon(imgUrl);
                        bgIcon.setImageObserver(this);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image: " + e.getMessage());
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                if (bgIcon != null && bgIcon.getImage() != null) {
                    UIComponents.paintCoverBackground(g2, bgIcon.getImage(), getWidth(), getHeight(), this);
                } else {
                    g2.setColor(new Color(0x0B, 0x0D, 0x12));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // 50% Dark Overlay (RGBA 0,0,0,120)
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setOpaque(false);

        // ── TOP SECTION: STATUS BAR & HEADER ─────────────────────────────────
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        // Top Status Bar (Top Right Badges)
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        statusBar.setOpaque(false);
        statusBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 25));

        statusPill = createBadge("🟢 Database Connected");
        statusLabel = (JLabel) statusPill.getComponent(0);
        statusLabel.setText("Checking Connection...");

        JPanel inventoryBadge = createBadge("📦 Inventory Ready");
        JPanel paymentBadge = createBadge("💳 Payment System Ready");
        JPanel printerBadge = createBadge("🖨 Printer Ready");

        badgePanels.add(statusPill);
        badgePanels.add(inventoryBadge);
        badgePanels.add(paymentBadge);
        badgePanels.add(printerBadge);

        for (JPanel b : badgePanels) {
            b.setVisible(false);
            statusBar.add(b);
        }

        topContainer.add(statusBar, BorderLayout.NORTH);

        // Header Panel (Center)
        JPanel headerPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(0, headerYOffset);
                super.paint(g2);
                g2.dispose();
            }
        };
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        JLabel logoIcon = new JLabel("🛒");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoIcon.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel("SMART SUPERMARKET");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("POS & SELF-CHECKOUT SYSTEM");
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel("Scan \u2022 Pay \u2022 Go");
        taglineLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));
        taglineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logoIcon);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        headerPanel.add(taglineLabel);

        topContainer.add(headerPanel, BorderLayout.CENTER);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // ── MAIN SECTION: TWO LARGE CARDS ───────────────────────────────────
        centerWrapper = new JPanel(new GridBagLayout()) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardOpacity));
                super.paint(g2);
                g2.dispose();
            }
        };
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(cardYOffset, 0, 0, 0));

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0)); // 50px spacing between cards
        cardsPanel.setOpaque(false);

        // CARD 1: Customer Self Checkout
        RoundedPanel card1 = new RoundedPanel(25, new Color(25, 30, 40, 180));
        card1.setPreferredSize(new Dimension(380, 420));
        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        card1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));

        JLabel cartIcon = new JLabel("🛒");
        cartIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        cartIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        cartIcon.setForeground(Color.WHITE);

        JLabel c1Title = new JLabel("START SHOPPING");
        c1Title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        c1Title.setForeground(Color.WHITE);
        c1Title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel c1Subtitle = new JLabel("Customer Self Checkout");
        c1Subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c1Subtitle.setForeground(new Color(0xD1, 0xD5, 0xDB));
        c1Subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel c1Desc = new JLabel(
                "<html><center>Browse products, scan items,<br>review cart and make payment.</center></html>");
        c1Desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c1Desc.setForeground(new Color(0xD1, 0xD5, 0xDB));
        c1Desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        c1Desc.setHorizontalAlignment(SwingConstants.CENTER);

        GradientButton startShoppingBtn = new GradientButton("START SHOPPING");
        startShoppingBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startShoppingBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        startShoppingBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startShoppingBtn.addActionListener(e -> {
            roleSwitch.setSelected(false);
            performLogin();
        });

        card1.add(cartIcon);
        card1.add(Box.createRigidArea(new Dimension(0, 10)));
        card1.add(c1Title);
        card1.add(Box.createRigidArea(new Dimension(0, 4)));
        card1.add(c1Subtitle);
        card1.add(Box.createRigidArea(new Dimension(0, 15)));
        card1.add(c1Desc);
        card1.add(Box.createVerticalGlue());
        card1.add(startShoppingBtn);

        // CARD 2: Employee Login
        loginCard = new RoundedPanel(25, new Color(25, 30, 40, 180));
        loginCard.setPreferredSize(new Dimension(380, 420));
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));

        JLabel empIcon = new JLabel("👨\u200D💼");
        empIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        empIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel c2Title = new JLabel("EMPLOYEE LOGIN");
        c2Title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        c2Title.setForeground(Color.WHITE);
        c2Title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel c2Subtitle = new JLabel("Cashier & Administrator");
        c2Subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c2Subtitle.setForeground(new Color(0xD1, 0xD5, 0xDB));
        c2Subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false);

        idField = new PlaceholderTextField("Employee ID");
        idField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        passField = new PlaceholderPasswordField("Password");
        passField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JButton eyeBtn = new JButton("👁");
        eyeBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        eyeBtn.setForeground(new Color(0xD1, 0xD5, 0xDB));
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeBtn.addActionListener(e -> {
            if (passField.getEchoChar() == '\0') {
                passField.setEchoChar(((PlaceholderPasswordField) passField).getDefaultEchoChar());
                eyeBtn.setForeground(new Color(0xD1, 0xD5, 0xDB));
            } else {
                passField.setEchoChar('\0');
                eyeBtn.setForeground(new Color(0x63, 0x66, 0xF1));
            }
        });

        JPanel innerPassPanel = new JPanel(new BorderLayout());
        innerPassPanel.setBackground(AppColors.BG_SURFACE_ALT);
        innerPassPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 75, 90), 1));
        innerPassPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        innerPassPanel.add(passField, BorderLayout.CENTER);
        innerPassPanel.add(eyeBtn, BorderLayout.EAST);

        inputPanel.add(idField);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(innerPassPanel);

        GradientButton loginBtn = new GradientButton("LOGIN");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            roleSwitch.setSelected(true);
            performLogin();
        });

        loginCard.add(empIcon);
        loginCard.add(Box.createRigidArea(new Dimension(0, 10)));
        loginCard.add(c2Title);
        loginCard.add(Box.createRigidArea(new Dimension(0, 4)));
        loginCard.add(c2Subtitle);
        loginCard.add(Box.createRigidArea(new Dimension(0, 15)));
        loginCard.add(inputPanel);
        loginCard.add(Box.createVerticalGlue());
        loginCard.add(loginBtn);

        cardsPanel.add(card1);
        cardsPanel.add(loginCard);

        centerWrapper.add(cardsPanel);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        // ── BOTTOM SECTION: FEATURE CARDS & FOOTER ──────────────────────────
        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.setOpaque(false);
        southContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        // Four Feature Cards
        JPanel featureCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        featureCardsPanel.setOpaque(false);

        featureCardsPanel.add(createFeatureCard("✔", "Live Inventory", "Real-time stock updates"));
        featureCardsPanel.add(createFeatureCard("✔", "Secure Payments", "Cash \u2022 Card \u2022 UPI"));
        featureCardsPanel.add(createFeatureCard("✔", "Instant Receipt", "Print after payment"));

        southContainer.add(featureCardsPanel, BorderLayout.NORTH);

        // Footer Bar
        JPanel footerBar = new JPanel(new BorderLayout());
        footerBar.setOpaque(false);
        footerBar.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));

        JLabel vLabel = new JLabel("Version 1.0");
        vLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        vLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));

        JLabel copyLabel = new JLabel("\u00A9 Smart Supermarket POS");
        copyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        copyLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));
        copyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel techLabel = new JLabel("Created By\n Om D Nathwani");
        techLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        techLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));

        footerBar.add(vLabel, BorderLayout.WEST);
        footerBar.add(copyLabel, BorderLayout.CENTER);
        footerBar.add(techLabel, BorderLayout.EAST);

        southContainer.add(footerBar, BorderLayout.SOUTH);
        mainPanel.add(southContainer, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        checkDatabaseConnection();

        // ── SWING TIMER ANIMATIONS ───────────────────────────────────────────
        Timer animationTimer = new Timer(20, null);
        animationTimer.addActionListener(e -> {
            boolean updated = false;

            if (headerYOffset < 0) {
                headerYOffset += 2;
                if (headerYOffset > 0)
                    headerYOffset = 0;
                updated = true;
            }

            if (cardOpacity < 1.0f) {
                cardOpacity += 0.05f;
                if (cardOpacity > 1.0f)
                    cardOpacity = 1.0f;
                updated = true;
            }

            if (cardYOffset > 0) {
                cardYOffset -= 2;
                if (cardYOffset < 0)
                    cardYOffset = 0;
                updated = true;
            }

            centerWrapper.setBorder(BorderFactory.createEmptyBorder(Math.max(0, cardYOffset), 0, 0, 0));

            if (updated) {
                mainPanel.repaint();
            } else {
                animationTimer.stop();
            }
        });

        // Staggered Badges appearance via Swing Timer
        Timer badgeTimer = new Timer(150, e -> {
            if (visibleBadgeCount < badgePanels.size()) {
                badgePanels.get(visibleBadgeCount).setVisible(true);
                visibleBadgeCount++;
                mainPanel.revalidate();
                mainPanel.repaint();
            } else {
                ((Timer) e.getSource()).stop();
            }
        });

        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            animationTimer.start();
            badgeTimer.start();
        });
    }

    private JPanel createBadge(String text) {
        RoundedPanel badge = new RoundedPanel(16, new Color(25, 30, 40, 180));
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(0x22, 0xC5, 0x5E));
        badge.add(label);

        return badge;
    }

    private JPanel createFeatureCard(String iconStr, String titleText, String subtitleText) {
        RoundedPanel pill = new RoundedPanel(18, new Color(25, 30, 40, 180));
        pill.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 25), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 14)));

        // Circular Green Icon Component
        JPanel greenCircle = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x22, 0xC5, 0x5E));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        greenCircle.setPreferredSize(new Dimension(24, 24));
        greenCircle.setOpaque(false);

        JLabel chk = new JLabel(iconStr);
        chk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chk.setForeground(Color.WHITE);
        greenCircle.add(chk);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel tLbl = new JLabel(titleText);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tLbl.setForeground(Color.WHITE);

        JLabel sLbl = new JLabel(subtitleText);
        sLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sLbl.setForeground(new Color(0xD1, 0xD5, 0xDB));

        textGroup.add(tLbl);
        textGroup.add(sLbl);

        pill.add(greenCircle);
        pill.add(textGroup);

        return pill;
    }

    private Image createAppIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppColors.ACCENT_1);
        g2.fillRoundRect(4, 4, 56, 56, 16, 16);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("🛒", 16, 32 + fm.getAscent() / 2 - 2);
        g2.dispose();
        return img;
    }

    private void checkDatabaseConnection() {
        new Thread(() -> {
            boolean isConnected = DatabaseConnection.checkConnection();
            SwingUtilities.invokeLater(() -> {
                if (isConnected) {
                    statusLabel.setText("🟢 Database Connected");
                    statusLabel.setForeground(new Color(0x22, 0xC5, 0x5E));
                } else {
                    statusLabel.setText("🔴 Connection Failed");
                    statusLabel.setForeground(AppColors.ACCENT_DANGER);
                }
            });
        }).start();
    }

    private void performLogin() {
        boolean isAdmin = roleSwitch.isSelected();

        if (!isAdmin) {
            User kiosk = userDao.getFirstCashier();
            if (kiosk == null) {
                kiosk = new User(1, "OM", "", "CASHIER", "Self Checkout");
            }
            transitionToFrame(new PosFrame(kiosk));
            return;
        }

        String empId = idField.getText().trim();
        String password = new String(passField.getPassword());

        if (empId.isEmpty() || password.isEmpty()) {
            AppDialog.showMessageDialog(this, "Please enter Employee ID and Password", "Error",
                    AppDialog.ERROR_MESSAGE);
            return;
        }

        User user = userDao.authenticate(empId, password, "ADMIN");
        if (user != null) {
            transitionToFrame(new AdminDashboardFrame(user));
        } else {
            AppDialog.showMessageDialog(this, "Invalid Admin credentials", "Login Failed", AppDialog.ERROR_MESSAGE);
        }
    }

    private void transitionToFrame(JFrame targetFrame) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Timer fadeOut = new Timer(15, null);
        fadeOut.addActionListener(e -> {
            try {
                float opacity = getOpacity() - 0.04f;

                if (opacity <= 0.05f) {
                    fadeOut.stop();

                    // 1. Start at 5% opacity so the OS respects the window
                    targetFrame.setOpacity(0.05f);
                    targetFrame.setVisible(true);

                    // 2. Force the OS to keep focus on our Java app
                    targetFrame.toFront();
                    targetFrame.requestFocus();

                    // 3. Hide the old frame so it doesn't block, but wait to dispose it
                    setVisible(false);

                    Timer fadeIn = new Timer(15, ev -> {
                        float newOpacity = targetFrame.getOpacity() + 0.04f;
                        if (newOpacity >= 1.0f) {
                            targetFrame.setOpacity(1.0f);
                            ((Timer) ev.getSource()).stop();

                            // 4. Safely dispose of the old frame ONLY when the new one is fully loaded
                            dispose();
                        } else {
                            targetFrame.setOpacity(newOpacity);
                        }
                    });
                    fadeIn.start();

                } else {
                    setOpacity(opacity);
                }
            } catch (Exception ex) {
                fadeOut.stop();
                targetFrame.setOpacity(1.0f);
                targetFrame.setVisible(true);
                dispose();
            }
        });
        fadeOut.start();
    }
}

class PosFrame extends JFrame {

    private User currentUser;
    private ProductDao productDao;
    private TransactionDao transactionDao;

    private JPanel catalogPanel;
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JTextField barcodeField;

    private JLabel itemTotalLabel;
    private JLabel gstLabel;
    private JLabel finalAmountLabel;
    private JPanel orderSummaryListPanel;

    private List<TransactionItem> cartItems = new ArrayList<>();
    private final String terminalId = "TERM-02";
    private String selectedPaymentMethod = "CASH";

    private CardLayout cartCenterCardLayout;
    private JPanel cartCenterPanel;
    private JLayeredPane layeredPane;
    private JPanel toastPanel;
    private String currentSelectedCategory = "All";

    public PosFrame(User user) {
        this.currentUser = user;
        this.productDao = new ProductDao();
        this.transactionDao = new TransactionDao();

        setTitle("Self-Checkout Terminal - " + user.getName());
        setIconImage(createAppIcon());
        setUndecorated(true);
        Dimension posScreen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(posScreen.width, posScreen.height);
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        layeredPane = getLayeredPane();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(0x11, 0x18, 0x27)); // Premium Dark #111827

        mainPanel.add(createTopBar(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 20, 0)); // 20px padding between panels
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        contentPanel.add(createCatalogPanel());
        contentPanel.add(createCartPanel());
        contentPanel.add(createCheckoutPanel());

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel bottomStatusBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 6));
        bottomStatusBar.setBackground(new Color(0x11, 0x18, 0x27));
        bottomStatusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x37, 0x41, 0x51)));

        JLabel b1 = new JLabel("🟢 Database Connected");
        JLabel b2 = new JLabel("📦 Inventory Synced");
        JLabel b3 = new JLabel("🖨 Printer Ready");
        JLabel b4 = new JLabel("💳 Payment Gateway Active");

        for (JLabel b : new JLabel[] { b1, b2, b3, b4 }) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setForeground(new Color(0xD1, 0xD5, 0xDB));
            bottomStatusBar.add(b);
        }

        mainPanel.add(bottomStatusBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        loadProducts("All");
    }

    private Image createAppIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0x63, 0x66, 0xF1));
        g2.fillRoundRect(4, 4, 56, 56, 16, 16);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("🛒", 16, 32 + fm.getAscent() / 2 - 2);
        g2.dispose();
        return img;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0x1F, 0x29, 0x37));
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x37, 0x41, 0x51)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        // Left Section: Back Button
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        GradientButton backBtn = new GradientButton("← Back");
        backBtn.setColors(new Color(0x37, 0x41, 0x51), new Color(0x1F, 0x29, 0x37));
        backBtn.setHoverColors(new Color(0xEF, 0x44, 0x44), new Color(0xDC, 0x26, 0x26));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setRadius(12);
        backBtn.setPreferredSize(new Dimension(100, 38));
        backBtn.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });
        leftPanel.add(backBtn);

        topBar.add(leftPanel, BorderLayout.WEST);

        // Center Section: Header Title, Subtitle, and Cashier Info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("🛒 Smart Supermarket POS");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Self Checkout Terminal  |  Cashier: " + currentUser.getName());
        subtitleLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        centerPanel.add(subtitleLabel);

        topBar.add(centerPanel, BorderLayout.CENTER);

        // Right Section: Date & Time + Status
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        JLabel clockLabel = new JLabel();
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        Timer timer = new Timer(1000, e -> {
            clockLabel.setText(new SimpleDateFormat("dd MMM yyyy  HH:mm:ss").format(new Date()));
        });
        timer.start();

        JLabel statusBadge = new JLabel("🟢 Connected");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusBadge.setForeground(new Color(0x22, 0xC5, 0x5E));

        rightPanel.add(clockLabel);
        rightPanel.add(statusBadge);

        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createCatalogPanel() {
        RoundedPanel panel = new RoundedPanel(18, new Color(0x1F, 0x29, 0x37));
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Search Bar & Scan Barcode row
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);

        PlaceholderTextField searchField = new PlaceholderTextField("🔍 Search products...");
        searchField.setBackground(new Color(0x23, 0x2B, 0x3A));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        searchField.setPreferredSize(new Dimension(200, 42));

        barcodeField = searchField; // Re-using barcodeField for search functionality without breaking variable
                                    // names

        searchRow.add(searchField, BorderLayout.CENTER);

        topSection.add(searchRow);
        topSection.add(Box.createRigidArea(new Dimension(0, 12)));

        // Category Pills row
        JPanel categories = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        categories.setOpaque(false);
        String[] cats = { "All", "Groceries", "Snacks", "Dairy", "Beverages" };
        String[] catLabels = { "🛒 All", "🥬 Grocery", "🍫 Snacks", "🥛 Dairy", "🥤 Drinks" };

        java.util.List<GradientButton> catBtns = new java.util.ArrayList<>();

        for (int i = 0; i < cats.length; i++) {
            final String catName = cats[i];
            GradientButton btn = new GradientButton(catLabels[i]);
            btn.setRadius(12);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

            Runnable updateStyles = () -> {
                for (int j = 0; j < catBtns.size(); j++) {
                    GradientButton b = catBtns.get(j);
                    if (cats[j].equalsIgnoreCase(currentSelectedCategory)) {
                        b.setColors(new Color(0x63, 0x66, 0xF1), new Color(0x4F, 0x46, 0xE5));
                    } else {
                        b.setColors(new Color(0x23, 0x2B, 0x3A), new Color(0x23, 0x2B, 0x3A));
                    }
                }
            };

            btn.addActionListener(e -> {
                currentSelectedCategory = catName;
                updateStyles.run();
                loadProducts(catName);
            });

            catBtns.add(btn);
            categories.add(btn);

            if (catName.equals("All")) {
                btn.setColors(new Color(0x63, 0x66, 0xF1), new Color(0x4F, 0x46, 0xE5));
            } else {
                btn.setColors(new Color(0x23, 0x2B, 0x3A), new Color(0x23, 0x2B, 0x3A));
            }
        }

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String query = searchField.getText().trim().toLowerCase();
                if (query.isEmpty() || query.startsWith("🔍")) {
                    loadProducts(currentSelectedCategory);
                } else {
                    filterProductsByQuery(query);
                }
            }
        });

        topSection.add(categories);
        panel.add(topSection, BorderLayout.NORTH);

        catalogPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        catalogPanel.setOpaque(false);
        catalogPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        JScrollPane scrollPane = new JScrollPane(catalogPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(0x63, 0x66, 0xF1, 180);
                this.trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
                    return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 6, 6);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            }
        });
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void filterProductsByQuery(String query) {
        catalogPanel.removeAll();
        try {
            List<Product> products = productDao.getAllProducts();
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getName().toLowerCase().contains(query) ||
                        p.getCategory().toLowerCase().contains(query) ||
                        (p.getBarcode() != null && p.getBarcode().contains(query))) {
                    filtered.add(p);
                }
            }
            if (filtered.isEmpty()) {
                JLabel emptyLabel = new JLabel("<html><center>No products match \"" + query + "\"</center></html>");
                emptyLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));
                emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                catalogPanel.setLayout(new BorderLayout());
                catalogPanel.add(emptyLabel, BorderLayout.CENTER);
            } else {
                catalogPanel.setLayout(new GridLayout(0, 3, 12, 12));
                for (Product p : filtered) {
                    catalogPanel.add(createProductCard(p));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        catalogPanel.revalidate();
        catalogPanel.repaint();
    }

    private void loadProducts(String category) {
        catalogPanel.removeAll();
        try {
            List<Product> products = productDao.getProductsByCategory(category);
            if (products.isEmpty()) {
                JLabel emptyLabel = new JLabel("<html><center>No products found in " + category + ".</center></html>");
                emptyLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));
                emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                catalogPanel.setLayout(new BorderLayout());
                catalogPanel.add(emptyLabel, BorderLayout.CENTER);
            } else {
                catalogPanel.setLayout(new GridLayout(0, 3, 12, 12));
                for (Product p : products) {
                    catalogPanel.add(createProductCard(p));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errLabel = new JLabel("<html><center>&#9888; DB Error:<br>" + e.getMessage() + "</center></html>");
            errLabel.setForeground(AppColors.ACCENT_DANGER);
            errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            errLabel.setHorizontalAlignment(SwingConstants.CENTER);
            catalogPanel.setLayout(new BorderLayout());
            catalogPanel.add(errLabel, BorderLayout.CENTER);
        }
        catalogPanel.revalidate();
        catalogPanel.repaint();
    }

    private JPanel createProductCard(Product product) {
        RoundedPanel card = new RoundedPanel(15, new Color(0x23, 0x2B, 0x3A));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        card.setPreferredSize(new Dimension(140, 280));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(0x2A, 0x34, 0x47));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x63, 0x66, 0xF1), 1),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)));
                card.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(0x23, 0x2B, 0x3A));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)));
                card.repaint();
            }
        });

        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                java.net.URL imgUrl = getClass().getResource(product.getImagePath());
                if (imgUrl != null) {
                    ImageIcon icon = new ImageIcon(imgUrl);
                    Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                    imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    card.add(imageLabel);
                    card.add(Box.createRigidArea(new Dimension(0, 8)));
                }
            } catch (Exception e) {
            }
        }

        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel catLabel = new JLabel(product.getCategory());
        catLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));
        catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        catLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("\u20B9 " + product.getPrice());
        priceLabel.setForeground(new Color(0x22, 0xC5, 0x5E));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stockLabel = new JLabel(
                product.getStockQty() > 0 ? "In Stock: " + product.getStockQty() : "Out of Stock");
        stockLabel.setForeground(product.getStockQty() > 0 ? new Color(0x22, 0xC5, 0x5E) : AppColors.ACCENT_DANGER);
        stockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel actionPanel = new JPanel(new CardLayout());
        actionPanel.setOpaque(false);
        actionPanel.setMaximumSize(new Dimension(130, 34));
        actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GradientButton addBtn = new GradientButton("＋ Add");
        addBtn.setColors(new Color(0x63, 0x66, 0xF1), new Color(0x4F, 0x46, 0xE5));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setRadius(10);
        addBtn.setEnabled(product.getStockQty() > 0);

        // 1. Change FlowLayout to a 1-row, 3-column GridLayout
        JPanel stepperPanel = new JPanel(new GridLayout(1, 3, 0, 0));
        stepperPanel.setOpaque(false);

        JButton minusBtn = new JButton("-");
        minusBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        minusBtn.setForeground(Color.WHITE);
        minusBtn.setContentAreaFilled(false);
        minusBtn.setFocusPainted(false);
        minusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 2. Remove default margins so the text fits nicely
        minusBtn.setMargin(new Insets(0, 0, 0, 0));

        JLabel qtyLabel = new JLabel("0");
        qtyLabel.setForeground(Color.WHITE);
        qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // The preferred size isn't strictly needed anymore with GridLayout, but it's
        // fine to keep
        qtyLabel.setPreferredSize(new Dimension(22, 22));
        qtyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton plusBtn = new JButton("+");
        plusBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        plusBtn.setForeground(Color.WHITE);
        plusBtn.setContentAreaFilled(false);
        plusBtn.setFocusPainted(false);
        plusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 3. Remove default margins here as well
        plusBtn.setMargin(new Insets(0, 0, 0, 0));

        stepperPanel.add(minusBtn);
        stepperPanel.add(qtyLabel);
        stepperPanel.add(plusBtn);

        actionPanel.add(addBtn, "ADD");
        actionPanel.add(stepperPanel, "STEPPER");

        CardLayout cl = (CardLayout) actionPanel.getLayout();

        addBtn.addActionListener(e -> {
            addToCart(product);
            product.setStockQty(product.getStockQty() - 1);
            stockLabel.setText(product.getStockQty() > 0 ? "In Stock: " + product.getStockQty() : "Out of Stock");
            if (product.getStockQty() <= 0)
                stockLabel.setForeground(AppColors.ACCENT_DANGER);

            qtyLabel.setText("1");
            cl.show(actionPanel, "STEPPER");
        });

        plusBtn.addActionListener(e -> {
            if (product.getStockQty() > 0) {
                addToCart(product);
                product.setStockQty(product.getStockQty() - 1);
                stockLabel.setText(product.getStockQty() > 0 ? "In Stock: " + product.getStockQty() : "Out of Stock");
                if (product.getStockQty() <= 0)
                    stockLabel.setForeground(AppColors.ACCENT_DANGER);

                int qty = Integer.parseInt(qtyLabel.getText());
                qtyLabel.setText(String.valueOf(qty + 1));
            }
        });

        minusBtn.addActionListener(e -> {
            int qty = Integer.parseInt(qtyLabel.getText());
            if (qty > 1) {
                removeFromCart(product);
                product.setStockQty(product.getStockQty() + 1);
                stockLabel.setText("In Stock: " + product.getStockQty());
                stockLabel.setForeground(new Color(0x22, 0xC5, 0x5E));
                qtyLabel.setText(String.valueOf(qty - 1));
            } else {
                removeFromCart(product);
                product.setStockQty(product.getStockQty() + 1);
                stockLabel.setText("In Stock: " + product.getStockQty());
                stockLabel.setForeground(new Color(0x22, 0xC5, 0x5E));
                cl.show(actionPanel, "ADD");
            }
        });

        int inCart = 0;
        for (TransactionItem item : cartItems) {
            if (item.getProductId() == product.getId())
                inCart = item.getQuantity();
        }
        if (inCart > 0) {
            qtyLabel.setText(String.valueOf(inCart));
            cl.show(actionPanel, "STEPPER");
        } else {
            cl.show(actionPanel, "ADD");
        }

        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(catLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(priceLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(stockLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(actionPanel);

        return card;
    }

    private JPanel createCartPanel() {
        RoundedPanel panel = new RoundedPanel(18, new Color(0x1F, 0x29, 0x37));
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("🛒 Interactive Cart");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        cartCenterCardLayout = new CardLayout();
        cartCenterPanel = new JPanel(cartCenterCardLayout);
        cartCenterPanel.setOpaque(false);

        // EMPTY CART ILLUSTRATION
        RoundedPanel emptyPanel = new RoundedPanel(15, new Color(0x23, 0x2B, 0x3A));
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel emptyIcon = new JLabel("🛒");
        emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 54));
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyTitle = new JLabel("Your Cart is Empty");
        emptyTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        emptyTitle.setForeground(Color.WHITE);
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptySub = new JLabel(
                "<html><center>Add products from the left panel<br>to begin checkout.</center></html>");
        emptySub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptySub.setForeground(new Color(0xD1, 0xD5, 0xDB));
        emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(Box.createVerticalGlue());
        emptyPanel.add(emptyIcon);
        emptyPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        emptyPanel.add(emptyTitle);
        emptyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        emptyPanel.add(emptySub);
        emptyPanel.add(Box.createVerticalGlue());

        // CART TABLE VIEW
        String[] columns = { "Name", "Qty", "Price", "Total" };
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setBackground(new Color(0x1F, 0x29, 0x37));
        cartTable.setForeground(Color.WHITE);
        cartTable.setFillsViewportHeight(true);
        cartTable.setRowHeight(40); // 40px Row Height
        cartTable.setShowGrid(false);
        cartTable.setIntercellSpacing(new Dimension(0, 0));

        cartTable.getTableHeader().setPreferredSize(new Dimension(0, 38)); // 38px Header Height
        cartTable.getTableHeader().setBackground(new Color(0x11, 0x18, 0x27));
        cartTable.getTableHeader().setForeground(Color.WHITE);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        cartTable.setDefaultRenderer(Object.class, (TableCellRenderer) new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (isSelected) {
                    c.setBackground(new Color(0x63, 0x66, 0xF1, 120));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(0x1F, 0x29, 0x37) : new Color(0x26, 0x32, 0x44));
                    c.setForeground(Color.WHITE);
                }

                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    if (column == 1) {
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    } else if (column >= 2) {
                        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.getViewport().setBackground(new Color(0x1F, 0x29, 0x37));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(0x63, 0x66, 0xF1, 180);
                this.trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });

        cartCenterPanel.add(emptyPanel, "EMPTY");
        cartCenterPanel.add(scrollPane, "TABLE");

        panel.add(cartCenterPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        GradientButton deleteBtn = new GradientButton("Delete Item");
        deleteBtn.setColors(new Color(0x37, 0x41, 0x51), new Color(0x23, 0x2B, 0x3A));
        deleteBtn.setHoverColors(new Color(0xEF, 0x44, 0x44), new Color(0xDC, 0x26, 0x26));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.setRadius(10);
        deleteBtn.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow >= 0) {
                cartItems.remove(selectedRow);
                updateCartUI();
                loadProducts(currentSelectedCategory);
            } else {
                AppDialog.showMessageDialog(PosFrame.this, "Select an item to delete.");
            }
        });

        GradientButton clearBtn = new GradientButton("Clear Cart");
        clearBtn.setColors(new Color(0x37, 0x41, 0x51), new Color(0x23, 0x2B, 0x3A));
        clearBtn.setHoverColors(new Color(0xEF, 0x44, 0x44), new Color(0xDC, 0x26, 0x26));
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearBtn.setRadius(10);
        clearBtn.addActionListener(e -> {
            if (cartItems.isEmpty())
                return;
            int confirm = AppDialog.showConfirmDialog(PosFrame.this,
                    "Clear all items from cart?", "Confirm", AppDialog.YES_NO_OPTION);
            if (confirm == AppDialog.YES_OPTION) {
                cartItems.clear();
                updateCartUI();
                loadProducts(currentSelectedCategory);
            }
        });

        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showToast(String message) {
        if (toastPanel != null && toastPanel.getParent() != null) {
            layeredPane.remove(toastPanel);
        }

        RoundedPanel toast = new RoundedPanel(16, new Color(0x1F, 0x29, 0x37));
        toast.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        toast.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x22, 0xC5, 0x5E), 2),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));

        JLabel iconLbl = new JLabel("✓");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        iconLbl.setForeground(new Color(0x22, 0xC5, 0x5E));

        JLabel msgLbl = new JLabel(message);
        msgLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        msgLbl.setForeground(Color.WHITE);

        toast.add(iconLbl);
        toast.add(msgLbl);

        toastPanel = toast;
        toastPanel.setSize(toast.getPreferredSize());

        int x = getWidth() - toastPanel.getWidth() - 35;
        int y = getHeight() - toastPanel.getHeight() - 75;
        toastPanel.setLocation(x, y);

        layeredPane.add(toastPanel, JLayeredPane.POPUP_LAYER);
        layeredPane.repaint();

        Timer toastTimer = new Timer(2000, e -> {
            if (toastPanel != null && toastPanel.getParent() != null) {
                layeredPane.remove(toastPanel);
                layeredPane.repaint();
            }
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private void addToCart(Product product) {
        boolean found = false;
        for (TransactionItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                item.setLineTotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                found = true;
                break;
            }
        }
        if (!found) {
            TransactionItem newItem = new TransactionItem(0, 0, product.getId(), 1, product.getPrice(),
                    product.getPrice());
            newItem.setProductName(product.getName());
            cartItems.add(newItem);
        }
        updateCartUI();
        showToast("✓ " + product.getName() + " added to cart");
    }

    private void removeFromCart(Product product) {
        for (int i = 0; i < cartItems.size(); i++) {
            TransactionItem item = cartItems.get(i);
            if (item.getProductId() == product.getId()) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    item.setLineTotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                } else {
                    cartItems.remove(i);
                }
                break;
            }
        }
        updateCartUI();
    }

    private void updateCartUI() {
        cartTableModel.setRowCount(0);
        for (TransactionItem item : cartItems) {
            cartTableModel.addRow(new Object[] {
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal()
            });
        }

        if (cartCenterCardLayout != null && cartCenterPanel != null) {
            if (cartItems.isEmpty()) {
                cartCenterCardLayout.show(cartCenterPanel, "EMPTY");
            } else {
                cartCenterCardLayout.show(cartCenterPanel, "TABLE");
            }
        }

        updateTotals();
    }

    private void updateTotals() {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (TransactionItem item : cartItems) {
            subTotal = subTotal.add(item.getLineTotal());
        }

        BigDecimal gst = subTotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = subTotal.add(gst).setScale(2, RoundingMode.HALF_UP);

        if (itemTotalLabel != null)
            itemTotalLabel.setText("\u20B9 " + subTotal);
        if (gstLabel != null)
            gstLabel.setText("\u20B9 " + gst);
        if (finalAmountLabel != null)
            finalAmountLabel.setText("\u20B9 " + finalTotal);

        if (orderSummaryListPanel != null) {
            orderSummaryListPanel.removeAll();
            for (TransactionItem item : cartItems) {
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                row.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

                String name = item.getProductName();
                if (name.length() > 16)
                    name = name.substring(0, 14) + "...";

                JLabel n = new JLabel(item.getQuantity() + "x " + name);
                n.setForeground(Color.WHITE);
                n.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                JLabel p = new JLabel("\u20B9 " + item.getLineTotal());
                p.setForeground(Color.WHITE);
                p.setFont(new Font("Segoe UI", Font.BOLD, 13));

                row.add(n, BorderLayout.WEST);
                row.add(p, BorderLayout.EAST);
                orderSummaryListPanel.add(row);
            }
            orderSummaryListPanel.revalidate();
            orderSummaryListPanel.repaint();
        }
    }

    private JPanel createCheckoutPanel() {
        RoundedPanel panel = new RoundedPanel(18, new Color(0x1F, 0x29, 0x37));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel title = new JLabel("Order Summary");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        orderSummaryListPanel = new JPanel();
        orderSummaryListPanel.setLayout(new BoxLayout(orderSummaryListPanel, BoxLayout.Y_AXIS));
        orderSummaryListPanel.setOpaque(false);

        JScrollPane orderScroll = new JScrollPane(orderSummaryListPanel);
        orderScroll.setOpaque(false);
        orderScroll.getViewport().setOpaque(false);
        orderScroll.setBorder(BorderFactory.createEmptyBorder());
        orderScroll.setPreferredSize(new Dimension(250, 130));

        orderScroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        orderScroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 255, 255, 40);
                this.trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });

        panel.add(orderScroll);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        itemTotalLabel = new JLabel("\u20B9 0.00");
        gstLabel = new JLabel("\u20B9 0.00");
        JLabel discountLabel = new JLabel("\u20B9 0.00");

        panel.add(createSummaryRow("Items:", itemTotalLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(createSummaryRow("GST (18%):", gstLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(createSummaryRow("Discount:", discountLabel));
        panel.add(Box.createRigidArea(new Dimension(0, 14)));

        // LARGE GREEN TOTAL BOX
        RoundedPanel finalCard = new RoundedPanel(15, new Color(0x22, 0xC5, 0x5E, 35));
        finalCard.setLayout(new BorderLayout());
        finalCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x22, 0xC5, 0x5E), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        finalCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JLabel finalDesc = new JLabel("TOTAL");
        finalDesc.setForeground(new Color(0x22, 0xC5, 0x5E));
        finalDesc.setFont(new Font("Segoe UI", Font.BOLD, 16));

        finalAmountLabel = new JLabel("\u20B9 0.00");
        finalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        finalAmountLabel.setForeground(new Color(0x22, 0xC5, 0x5E));

        finalCard.add(finalDesc, BorderLayout.WEST);
        finalCard.add(finalAmountLabel, BorderLayout.EAST);

        panel.add(finalCard);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel payMethodLabel = new JLabel("Select Payment Method");
        payMethodLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        payMethodLabel.setForeground(new Color(0xD1, 0xD5, 0xDB));
        payMethodLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(payMethodLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel payMethods = new JPanel(new GridLayout(1, 3, 10, 0));
        payMethods.setOpaque(false);

        RoundedPanel cashCard = createPaymentCard("CASH", "💵");
        RoundedPanel upiCard = createPaymentCard("UPI", "📱");
        RoundedPanel cardCard = createPaymentCard("CARD", "💳");

        Runnable[] resetAll = new Runnable[1];
        resetAll[0] = () -> {
            unselectPaymentCard(cashCard);
            unselectPaymentCard(upiCard);
            unselectPaymentCard(cardCard);
        };

        selectPaymentCard(cashCard);

        cashCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedPaymentMethod = "CASH";
                resetAll[0].run();
                selectPaymentCard(cashCard);
            }
        });
        upiCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedPaymentMethod = "UPI";
                resetAll[0].run();
                selectPaymentCard(upiCard);
            }
        });
        cardCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedPaymentMethod = "CARD";
                resetAll[0].run();
                selectPaymentCard(cardCard);
            }
        });

        payMethods.add(cashCard);
        payMethods.add(upiCard);
        payMethods.add(cardCard);

        payMethods.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        panel.add(payMethods);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        panel.add(Box.createVerticalGlue());

        // PAY & PRINT RECEIPT BUTTON (Large Green Gradient)
        GradientButton payBtn = new GradientButton("PAY & PRINT RECEIPT");
        payBtn.setColors(new Color(0x22, 0xC5, 0x5E), new Color(0x16, 0xA3, 0x4A));
        payBtn.setHoverColors(new Color(0x16, 0xA3, 0x4A), new Color(0x15, 0x80, 0x3D));
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        payBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        payBtn.setRadius(14);
        payBtn.addActionListener(e -> {
            if (cartItems.isEmpty()) {
                AppDialog.showMessageDialog(this, "Cart is empty!");
                return;
            }
            payBtn.setText("PROCESSING...");
            payBtn.setEnabled(false);

            Timer t = new Timer(600, ev -> {
                processPayment();
                payBtn.setText("PAY & PRINT RECEIPT");
                payBtn.setEnabled(true);
            });
            t.setRepeats(false);
            t.start();
        });
        panel.add(payBtn);

        return panel;
    }

    private JLabel createProgressStep(String text, boolean active) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(active ? new Color(0x63, 0x66, 0xF1) : new Color(0xD1, 0xD5, 0xDB));
        return l;
    }

    private JPanel createSummaryRow(String label, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel l = new JLabel(label);
        l.setForeground(new Color(0xD1, 0xD5, 0xDB));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        p.add(l, BorderLayout.WEST);
        p.add(valueLabel, BorderLayout.EAST);
        return p;
    }

    private RoundedPanel createPaymentCard(String name, String emoji) {
        RoundedPanel p = new RoundedPanel(12, new Color(0x23, 0x2B, 0x3A));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)));

        JLabel icon = new JLabel(emoji);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text = new JLabel(name);
        text.setFont(new Font("Segoe UI", Font.BOLD, 12));
        text.setForeground(new Color(0xD1, 0xD5, 0xDB));
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(icon);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(text);

        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return p;
    }

    private void selectPaymentCard(RoundedPanel p) {
        p.setBackground(new Color(0x63, 0x66, 0xF1, 40));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x22, 0xC5, 0x5E), 2),
                BorderFactory.createEmptyBorder(9, 0, 9, 0)));
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel)
                c.setForeground(Color.WHITE);
        }
    }

    private void unselectPaymentCard(RoundedPanel p) {
        p.setBackground(new Color(0x23, 0x2B, 0x3A));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)));
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(new Color(0xD1, 0xD5, 0xDB));
            }
        }
    }

    private void processPayment() {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (TransactionItem item : cartItems) {
            subTotal = subTotal.add(item.getLineTotal());
        }
        BigDecimal gst = subTotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = subTotal.add(gst).setScale(2, RoundingMode.HALF_UP);

        Transaction tx = new Transaction(0, terminalId, currentUser.getId(), subTotal, gst, BigDecimal.ZERO, finalTotal,
                selectedPaymentMethod, new Timestamp(System.currentTimeMillis()));

        List<TransactionItem> receiptItems = new ArrayList<>(cartItems);

        boolean success = transactionDao.processCheckout(tx, cartItems);

        if (success) {
            cartItems.clear();
            updateCartUI();
            loadProducts(currentSelectedCategory);
            generateReceipt(tx, receiptItems);
        } else {
            AppDialog.showMessageDialog(this, "Payment failed. Please try again.", "Error", AppDialog.ERROR_MESSAGE);
        }
    }

    private void generateReceipt(Transaction tx, List<TransactionItem> items) {
        JDialog dialog = new JDialog(this, "Receipt", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        RoundedPanel rp = new RoundedPanel(20, new Color(0x1F, 0x29, 0x37));
        rp.setLayout(new BoxLayout(rp, BoxLayout.Y_AXIS));
        rp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x37, 0x41, 0x51), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        JLabel header = new JLabel("Payment Successful");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(new Color(0x22, 0xC5, 0x5E));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        rp.add(header);

        JLabel check = new JLabel("✅");
        check.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        check.setAlignmentX(Component.CENTER_ALIGNMENT);
        rp.add(check);
        rp.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        detailsPanel.setOpaque(false);
        detailsPanel.add(new JLabel("<html><font color='#9AA0AC'>Date:</font> <font color='white'>"
                + new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(tx.getCreatedAt()) + "</font></html>"));
        detailsPanel.add(new JLabel(
                "<html><font color='#9AA0AC'>Txn ID:</font> <font color='white'>" + tx.getId() + "</font></html>"));
        detailsPanel.add(new JLabel("<html><font color='#9AA0AC'>Terminal:</font> <font color='white'>"
                + tx.getTerminalId() + "</font></html>"));
        detailsPanel.add(new JLabel("<html><font color='#9AA0AC'>Payment:</font> <font color='white'>"
                + tx.getPaymentMethod() + "</font></html>"));
        rp.add(detailsPanel);
        rp.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (TransactionItem item : items) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

            String name = item.getProductName();
            if (name.length() > 16)
                name = name.substring(0, 14) + "...";

            JLabel n = new JLabel(item.getQuantity() + "x " + name);
            n.setForeground(Color.WHITE);
            n.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JLabel p = new JLabel("\u20B9 " + item.getLineTotal());
            p.setForeground(Color.WHITE);
            p.setFont(new Font("Segoe UI", Font.BOLD, 12));

            row.add(n, BorderLayout.WEST);
            row.add(p, BorderLayout.EAST);
            listPanel.add(row);
        }
        rp.add(listPanel);
        rp.add(Box.createRigidArea(new Dimension(0, 15)));

        rp.add(new JSeparator());
        rp.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel finalPanel = new JPanel(new BorderLayout());
        finalPanel.setOpaque(false);
        finalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel fL = new JLabel("TOTAL PAID:");
        fL.setForeground(new Color(0xD1, 0xD5, 0xDB));
        fL.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel fV = new JLabel("\u20B9 " + tx.getFinalAmount());
        fV.setForeground(new Color(0x22, 0xC5, 0x5E));
        fV.setFont(new Font("Segoe UI", Font.BOLD, 18));
        finalPanel.add(fL, BorderLayout.WEST);
        finalPanel.add(fV, BorderLayout.EAST);
        rp.add(finalPanel);
        rp.add(Box.createRigidArea(new Dimension(0, 25)));

        GradientButton closeBtn = new GradientButton("CLOSE");
        closeBtn.setColors(new Color(0x63, 0x66, 0xF1), new Color(0x4F, 0x46, 0xE5));
        closeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());
        rp.add(closeBtn);

        dialog.add(rp);
        dialog.pack();
        dialog.setSize(new Dimension(320, dialog.getHeight() + 20));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
