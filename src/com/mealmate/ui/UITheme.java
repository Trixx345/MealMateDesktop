
package com.mealmate.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UITheme {

    public static final Color NAVY = new Color(39, 48, 83);
    public static final Color ORANGE = new Color(234, 88, 12);
    public static final Color CORAL = new Color(249, 115, 102);
    public static final Color TEAL = new Color(13, 148, 136);
    public static final Color YELLOW = new Color(250, 204, 21);
    public static final Color PURPLE = new Color(124, 58, 237);

    public static final Color BACKGROUND = new Color(255, 248, 240);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT = new Color(38, 45, 64);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(244, 226, 211);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color DANGER = new Color(220, 38, 38);

    public static final Font BODY =
        new Font("SansSerif", Font.PLAIN, 13);

    public static final Font BODY_BOLD =
        new Font("SansSerif", Font.BOLD, 13);

    public static final Font HEADING =
        new Font("SansSerif", Font.BOLD, 25);

    public static final Font SMALL =
        new Font("SansSerif", Font.PLAIN, 11);

    private UITheme() {
    }

    public static JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(22, 24, 20, 24));
        return panel;
    }

    public static JPanel surface() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new LineBorder(BORDER, 1, true));
        return panel;
    }

    public static JPanel card() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 17, 16, 17)
        ));
        return panel;
    }

    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(ORANGE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(Cursor.getPredefinedCursor(
            Cursor.HAND_CURSOR
        ));
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BODY_BOLD);
        button.setForeground(NAVY);
        button.setBackground(new Color(255, 237, 213));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(Cursor.getPredefinedCursor(
            Cursor.HAND_CURSOR
        ));
        return button;
    }

    public static JButton dangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(DANGER);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(Cursor.getPredefinedCursor(
            Cursor.HAND_CURSOR
        ));
        return button;
    }

    public static JButton navButton(
            String text, boolean selected) {

        JButton button = new JButton(text);
        button.setFont(BODY_BOLD);
        button.setForeground(selected ? Color.WHITE : NAVY);
        button.setBackground(
            selected ? ORANGE : new Color(255, 248, 240)
        );
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(11, 15, 11, 15));
        button.setCursor(Cursor.getPredefinedCursor(
            Cursor.HAND_CURSOR
        ));
        return button;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADING);
        label.setForeground(NAVY);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel header(
            String heading, String description) {

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(title(heading), BorderLayout.NORTH);
        panel.add(subtitle(description), BorderLayout.CENTER);
        return panel;
    }

    public static JLabel badge(
            String text, Color background, Color foreground) {

        JLabel label = new JLabel("  " + text + "  ");
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(foreground);
        label.setBackground(background);
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(5, 3, 5, 3));
        return label;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(34);
        table.setGridColor(new Color(245, 235, 226));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        table.setSelectionBackground(new Color(255, 237, 213));
        table.setSelectionForeground(NAVY);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(BODY_BOLD);
        header.setBackground(new Color(255, 237, 213));
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 39));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer =
            new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(
                        JTable source,
                        Object value,
                        boolean selected,
                        boolean hasFocus,
                        int row,
                        int column) {

                    Component component =
                        super.getTableCellRendererComponent(
                            source,
                            value,
                            selected,
                            hasFocus,
                            row,
                            column
                        );

                    if (selected) {
                        component.setBackground(
                            new Color(255, 237, 213)
                        );
                        component.setForeground(NAVY);
                    } else {
                        component.setBackground(
                            row % 2 == 0
                                ? Color.WHITE
                                : new Color(255, 251, 246)
                        );
                        component.setForeground(TEXT);
                    }

                    setBorder(new EmptyBorder(4, 9, 4, 9));
                    return component;
                }
            };

        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);
        table.setDefaultRenderer(
            java.math.BigDecimal.class, renderer
        );
        table.setDefaultRenderer(Integer.class, renderer);
    }

    public static JPanel metricCard(
            String caption,
            String value,
            Color accent) {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(17, 19, 17, 19)
        ));

        JLabel heading = new JLabel(caption);
        heading.setFont(SMALL);
        heading.setForeground(MUTED);

        JLabel number = new JLabel(value);
        number.setFont(new Font("SansSerif", Font.BOLD, 28));
        number.setForeground(accent);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(number, BorderLayout.CENTER);

        return panel;
    }
}
