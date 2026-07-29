package com.smartpos;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

// ── Design-token interface ─────────────────────────────────────────────────────
interface AppColors {
    Color BG_BASE        = new Color(0x0B, 0x0D, 0x12);
    Color BG_SURFACE     = new Color(0x18, 0x1B, 0x23, 216);   // 85 % opacity
    Color BG_SURFACE_ALT = new Color(0x22, 0x25, 0x2E);
    Color ACCENT_1       = new Color(108, 99, 255);             // #6C63FF
    Color ACCENT_2       = new Color(72, 52, 212);              // #4834D4
    Color ACCENT_SUCCESS = new Color(46, 204, 113);             // #2ECC71
    Color ACCENT_INFO    = new Color(41, 128, 185);             // #2980B9
    Color ACCENT_DANGER  = new Color(231, 76, 60);              // #E74C3C
    Color TEXT_PRIMARY   = Color.WHITE;
    Color TEXT_SECONDARY = new Color(0x9A, 0xA0, 0xAC);
    Color BORDER         = new Color(255, 255, 255, 20);        // hairline
    int   RADIUS_CARD    = 18;
    int   RADIUS_INPUT   = 12;
}

// ─────────────────────────────────────────────────────────────────────────────
public class UIComponents {

    /**
     * Paints a background image in "cover" mode — scales uniformly to fill the
     * panel with no gaps, crops overflow, centres the result, then applies a
     * semi-transparent dark overlay so foreground text stays readable.
     */
    public static void paintCoverBackground(Graphics2D g2, Image img,
                                             int panelW, int panelH,
                                             java.awt.image.ImageObserver obs) {
        if (img == null) return;
        int iw = img.getWidth(obs);
        int ih = img.getHeight(obs);
        if (iw <= 0 || ih <= 0) return;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scale = Math.max((double) panelW / iw, (double) panelH / ih);
        int dw = (int) Math.ceil(iw * scale);
        int dh = (int) Math.ceil(ih * scale);
        int dx = (panelW - dw) / 2;
        int dy = (panelH - dh) / 2;

        Shape oldClip = g2.getClip();
        g2.setClip(0, 0, panelW, panelH);
        g2.drawImage(img, dx, dy, dw, dh, obs);
        g2.setClip(oldClip);

        // ~rgba(10,11,15,140) overlay for readability
        g2.setColor(new Color(10, 11, 15, 140));
        g2.fillRect(0, 0, panelW, panelH);
    }
}

// ── Placeholder text field ────────────────────────────────────────────────────
class PlaceholderTextField extends JTextField {
    private final String placeholder;

    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;
        setBackground(AppColors.BG_SURFACE_ALT);
        setForeground(AppColors.TEXT_PRIMARY);
        setCaretColor(AppColors.TEXT_PRIMARY);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 75, 90), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(AppColors.TEXT_SECONDARY);
            g2.setFont(getFont().deriveFont(Font.PLAIN));
            Insets ins = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, ins.left, y);
            g2.dispose();
        }
    }
}

// ── Placeholder password field ────────────────────────────────────────────────
class PlaceholderPasswordField extends JPasswordField {
    private final String placeholder;
    private final char   defaultEcho;

    public PlaceholderPasswordField(String placeholder) {
        this.placeholder = placeholder;
        this.defaultEcho = getEchoChar();
        setBackground(AppColors.BG_SURFACE_ALT);
        setForeground(AppColors.TEXT_PRIMARY);
        setCaretColor(AppColors.TEXT_PRIMARY);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 75, 90), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        setOpaque(true);
    }

    public char getDefaultEchoChar() { return defaultEcho; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getPassword().length == 0 && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(AppColors.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            Insets ins = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, ins.left, y);
            g2.dispose();
        }
    }
}

// ── Gradient Button ───────────────────────────────────────────────────────────
class GradientButton extends JButton {
    private Color color1      = new Color(0x63, 0x66, 0xF1); // #6366F1
    private Color color2      = new Color(0x3B, 0x82, 0xF6); // #3B82F6
    private Color hoverColor1 = new Color(0x43, 0x38, 0xCA); // #4338CA
    private Color hoverColor2 = new Color(0x25, 0x63, 0xEB); // #2563EB

    private boolean isHovered = false;
    private int     radius    = 20;

    public GradientButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setColors(Color c1, Color c2) {
        this.color1 = c1;
        this.color2 = c2;
        repaint();
    }

    public void setHoverColors(Color hc1, Color hc2) {
        this.hoverColor1 = hc1;
        this.hoverColor2 = hc2;
        repaint();
    }

    public void setRadius(int r) { this.radius = r; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width  = getWidth();
        int height = getHeight();

        Color startColor = isHovered ? hoverColor1 : color1;
        Color endColor   = isHovered ? hoverColor2 : color2;

        if (isHovered) {
            // Hover glow effect
            g2.setColor(new Color(0x63, 0x66, 0xF1, 80));
            g2.fillRoundRect(0, 0, width, height, radius + 4, radius + 4);

            // Scale to 102%
            g2.scale(1.015, 1.015);
            g2.translate(-width * 0.0075, -height * 0.0075);
        }

        GradientPaint gp = new GradientPaint(0, 0, startColor, width, height, endColor);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, width, height, radius, radius);

        super.paintComponent(g);
        g2.dispose();
    }
}

// ── Rounded Panel with hairline border ───────────────────────────────────────
class RoundedPanel extends JPanel {
    private Color backgroundColor;
    private int   cornerRadius = 15;

    public RoundedPanel(int radius, Color bgColor) {
        super();
        this.cornerRadius    = radius;
        this.backgroundColor = bgColor;
        setOpaque(false); // Important for custom painting
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width  = getWidth();
        int height = getHeight();

        // Fill with design-system surface colour (supports alpha)
        g2.setColor(backgroundColor != null ? backgroundColor : getBackground());
        g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        // Hairline border — rgba(255,255,255,0.08)
        g2.setColor(AppColors.BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        g2.dispose();
    }
}

// ── Toggle Switch with animated thumb ────────────────────────────────────────
class ToggleSwitch extends JComponent {
    private boolean selected = false;
    private float   thumbPos = 0f;   // 0.0 = left  ·  1.0 = right
    private Timer   animTimer;

    private final Color onColor    = new Color(46, 204, 113); // Green
    private final Color offColor   = new Color(85, 85, 85);   // Gray
    private final Color thumbColor = Color.WHITE;

    private final java.util.List<ActionListener> actionListeners = new java.util.ArrayList<>();

    public ToggleSwitch() {
        setPreferredSize(new Dimension(50, 25));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                animateThumb(selected ? 1f : 0f);
                for (ActionListener listener : actionListeners) {
                    listener.actionPerformed(new ActionEvent(ToggleSwitch.this,
                            ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });
    }

    /** Interpolates the thumb position over ~150 ms. */
    private void animateThumb(float target) {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        animTimer = new Timer(16, null);
        animTimer.addActionListener(e -> {
            float diff = target - thumbPos;
            if (Math.abs(diff) < 0.02f) {
                thumbPos = target;
                animTimer.stop();
            } else {
                thumbPos += diff * 0.20f;
            }
            repaint();
        });
        animTimer.start();
    }

    public void addActionListener(ActionListener listener) { actionListeners.add(listener); }
    public boolean isSelected() { return selected; }

    public void setSelected(boolean s) {
        this.selected = s;
        thumbPos      = s ? 1f : 0f;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width  = getWidth();
        int height = getHeight();

        // Interpolate track colour between off/on as thumb moves
        float t  = thumbPos;
        int   r  = (int) (offColor.getRed()   + t * (onColor.getRed()   - offColor.getRed()));
        int   gv = (int) (offColor.getGreen() + t * (onColor.getGreen() - offColor.getGreen()));
        int   b  = (int) (offColor.getBlue()  + t * (onColor.getBlue()  - offColor.getBlue()));
        g2.setColor(new Color(r, gv, b));
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, height, height));

        // Thumb circle — position interpolated between left and right ends
        int thumbSize = height - 4;
        int maxX      = width - thumbSize - 2;
        int thumbX    = 2 + (int) (thumbPos * maxX);
        g2.setColor(thumbColor);
        g2.fillOval(thumbX, 2, thumbSize, thumbSize);

        g2.dispose();
    }
}

// ── App Table Cell Renderer ───────────────────────────────────────────────────
class AppTableRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, col);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        if (isSelected) {
            lbl.setBackground(new Color(108, 99, 255, 80));
            lbl.setForeground(AppColors.TEXT_PRIMARY);
        } else {
            lbl.setBackground(row % 2 == 0
                    ? AppColors.BG_SURFACE_ALT
                    : new Color(0x18, 0x1B, 0x23));
            lbl.setForeground(AppColors.TEXT_PRIMARY);
        }
        lbl.setOpaque(true);
        return lbl;
    }
}

// ── Scrollable Panel (moved out of ToggleSwitch) ──────────────────────────────
class ScrollablePanel extends JPanel implements Scrollable {
    public ScrollablePanel(LayoutManager layout) { super(layout); }

    @Override
    public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 40; }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 120; }

    @Override
    public boolean getScrollableTracksViewportWidth() { return true; } // <- the actual fix

    @Override
    public boolean getScrollableTracksViewportHeight() { return false; }
}


class AppDialog {
    public static final int OK_OPTION = 0;
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int OK_CANCEL_OPTION = 2;
    public static final int YES_NO_OPTION = 0;
    public static final int ERROR_MESSAGE = 0;

    public static void showMessageDialog(Component parent, Object message) {
        showCustom(parent, message, "Message", false);
    }
    
    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        showCustom(parent, message, title, false);
    }
    
    public static String showInputDialog(Component parent, Object message, Object initialSelectionValue) {
        JTextField input = new JTextField(initialSelectionValue != null ? initialSelectionValue.toString() : "");
        input.setBackground(AppColors.BG_BASE);
        input.setForeground(AppColors.TEXT_PRIMARY);
        input.setCaretColor(Color.WHITE);
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        Object[] content = { message, input };
        int res = showCustom(parent, content, "Input", true);
        if (res == OK_OPTION) return input.getText();
        return null;
    }
    
    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showCustom(parent, message, title, true);
    }
    
    private static int showCustom(Component parent, Object content, String title, boolean isConfirm) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(window, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        
        RoundedPanel rp = new RoundedPanel(15, AppColors.BG_SURFACE);
        rp.setLayout(new BorderLayout(0, 15));
        rp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppColors.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(AppColors.TEXT_PRIMARY);
        rp.add(titleLbl, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        if (content instanceof Object[]) {
            for (Object obj : (Object[]) content) {
                if (obj instanceof String) {
                    JLabel l = new JLabel((String)obj);
                    l.setForeground(AppColors.TEXT_SECONDARY);
                    l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    l.setAlignmentX(Component.LEFT_ALIGNMENT);
                    contentPanel.add(l);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                } else if (obj instanceof Component) {
                    Component c = (Component) obj;
                    ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
                    contentPanel.add(c);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        } else {
            String text = content != null ? content.toString() : "";
            JLabel msgLbl = new JLabel("<html><body style='width: 250px; color: #9AA0AC;'>" + text + "</body></html>");
            msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            contentPanel.add(msgLbl);
        }
        
        rp.add(contentPanel, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        
        final int[] result = { CANCEL_OPTION };
        
        if (isConfirm) {
            GradientButton cancelBtn = new GradientButton("Cancel");
            cancelBtn.setColors(AppColors.BG_SURFACE_ALT, AppColors.BG_SURFACE_ALT);
            cancelBtn.addActionListener(e -> { result[0] = CANCEL_OPTION; dialog.dispose(); });
            
            GradientButton okBtn = new GradientButton("OK");
            okBtn.setColors(AppColors.ACCENT_1, AppColors.ACCENT_2);
            okBtn.addActionListener(e -> { result[0] = OK_OPTION; dialog.dispose(); });
            
            btnPanel.add(cancelBtn);
            btnPanel.add(okBtn);
        } else {
            GradientButton okBtn = new GradientButton("OK");
            okBtn.setColors(AppColors.ACCENT_1, AppColors.ACCENT_2);
            okBtn.addActionListener(e -> { result[0] = OK_OPTION; dialog.dispose(); });
            btnPanel.add(okBtn);
        }
        
        rp.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(rp);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        
        return result[0];
    }
}

