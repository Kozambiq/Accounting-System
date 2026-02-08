package com.raven.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.InputStream;

/**
 * Centralized table UI styling used by CoA, Trial Balance, and Financial Reports.
 * Update this class once to change table appearance across all tabs.
 */
public final class StandardTableStyle {

    private static Font workSansRegular;
    private static Font workSansBold;

    public static final int ROW_HEIGHT = 40;
    public static final Color TABLE_BG = new Color(0xcdc6c6);
    public static final Color HEADER_BG = new Color(0x19A64A);
    public static final Color HEADER_FG = Color.WHITE;
    public static final Color GRID_COLOR = new Color(0xE0E0E0);
    public static final Color TEXT_COLOR = new Color(0x2F, 0x2F, 0x2F);
    public static final Color EVEN_ROW = Color.WHITE;
    public static final Color ODD_ROW = new Color(0xF2, 0xF2, 0xF2);

    private StandardTableStyle() {}

    /**
     * Creates a JTable that uses the standard rounded-row painting and is ready for
     * applyStandardTableStyle. Use this instead of {@code new JTable(model)} for consistent look.
     */
    public static JTable createStandardTable(TableModel model) {
        return new StandardStyledTable(model);
    }

    /**
     * Applies the shared table styling: row height, grid, colors, font, header, default cell renderer.
     * Call after creating the table (with createStandardTable or any JTable).
     */
    public static void applyStandardTableStyle(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(ROW_HEIGHT);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFocusable(false);
        table.setBackground(TABLE_BG);
        table.setOpaque(true);
        table.setGridColor(GRID_COLOR);
        table.setFont(getWorkSansRegular(14f));
        table.setDefaultRenderer(Object.class, new StandardTableRenderer());

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setFont(getWorkSansBold(14f));
        header.setReorderingAllowed(false);
        Dimension headerSize = header.getPreferredSize();
        headerSize.height += 8;
        header.setPreferredSize(headerSize);
    }

    /**
     * Styles a JScrollPane that wraps a standard table: border, viewport background, no horizontal scroll.
     */
    public static void styleScrollPaneForTable(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(TABLE_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public static Font getWorkSansRegular(float size) {
        if (workSansRegular != null) {
            return workSansRegular.deriveFont(Font.PLAIN, size);
        }
        try (InputStream stream = StandardTableStyle.class.getResourceAsStream("/fonts/Work_Sans/static/WorkSans-Regular.ttf")) {
            if (stream != null) {
                workSansRegular = Font.createFont(Font.TRUETYPE_FONT, stream);
                return workSansRegular.deriveFont(size);
            }
        } catch (Exception ignored) { }
        return new Font("SansSerif", Font.PLAIN, (int) size);
    }

    public static Font getWorkSansBold(float size) {
        if (workSansBold != null) {
            return workSansBold.deriveFont(Font.BOLD, size);
        }
        try (InputStream stream = StandardTableStyle.class.getResourceAsStream("/fonts/Work_Sans/static/WorkSans-Bold.ttf")) {
            if (stream != null) {
                workSansBold = Font.createFont(Font.TRUETYPE_FONT, stream);
                return workSansBold.deriveFont(Font.BOLD, size);
            }
        } catch (Exception ignored) { }
        return new Font("SansSerif", Font.BOLD, (int) size);
    }

    /** Cell insets used by the default renderer; use in custom renderers for consistency. */
    public static EmptyBorder getStandardCellBorder() {
        return new EmptyBorder(6, 16, 6, 16);
    }

    /**
     * Default cell renderer: transparent background (row color from table painting), WorkSans, standard padding.
     * Text is vertically and horizontally centered in the cell to avoid clipping at edges.
     * Extend this for column-specific behavior (e.g. right-align, highlight totals row).
     */
    public static class StandardTableRenderer extends DefaultTableCellRenderer {
        public StandardTableRenderer() {
            setOpaque(false);
            setBorder(getStandardCellBorder());
            setVerticalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, hasFocus, row, column);
            setForeground(TEXT_COLOR);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(getWorkSansRegular(14f));
            return this;
        }
    }

    /**
     * JTable that paints each row as a rounded rectangle with alternating colors (white / light gray).
     */
    private static class StandardStyledTable extends JTable {
        private static final int ARC = 18;
        private static final int MARGIN_X = 4;
        private static final int MARGIN_Y = 4;

        StandardStyledTable(TableModel model) {
            super(model);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TABLE_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int rowCount = getRowCount();
            int rowHeight = getRowHeight();
            int width = getWidth();

            for (int row = 0; row < rowCount; row++) {
                int y = row * rowHeight;
                Color bg = (row % 2 == 0) ? EVEN_ROW : ODD_ROW;
                g2.setColor(bg);
                g2.fillRoundRect(MARGIN_X, y + MARGIN_Y, width - MARGIN_X * 2,
                        rowHeight - MARGIN_Y * 2, ARC, ARC);
            }
            g2.dispose();

            boolean wasOpaque = isOpaque();
            setOpaque(false);
            super.paintComponent(g);
            setOpaque(wasOpaque);
        }
    }
}
