package com.mealmate.ui;

import com.mealmate.dao.AuthDao;
import com.mealmate.dao.StudentDao;
import com.mealmate.model.AppUser;
import com.mealmate.model.Booking;
import com.mealmate.model.Meal;
import com.mealmate.model.Student;
import com.mealmate.service.BookingService;
import com.mealmate.service.MealService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainFrame extends JFrame {

    private static final Color SIDEBAR = new Color(30, 38, 63);
    private static final Color SIDEBAR_MUTED = new Color(184, 193, 212);
    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color CREAM = new Color(255, 248, 240);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(38, 45, 64);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color LINE = new Color(239, 225, 211);

    private final AppUser user;
    private final MealService mealService = new MealService();
    private final BookingService bookingService = new BookingService();
    private final StudentDao studentDao = new StudentDao();
    private final AuthDao authDao = new AuthDao();

    private final JPanel pageHost = new JPanel(new BorderLayout());
    private final JPanel sidebarButtons = new JPanel();
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageSubtitle = new JLabel();
    private final JLabel statusLabel = new JLabel("Ready");
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public MainFrame(AppUser user) {
        super("MealMate | Campus Food");
        this.user = Objects.requireNonNull(user);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1280, 820);
        setLocationRelativeTo(null);

        buildLayout();
        navigate("dashboard");
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CREAM);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createWorkspace(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(235, 0));
        side.setBorder(new EmptyBorder(20, 14, 16, 14));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        brand.setOpaque(false);

        JLabel logo = new JLabel("✦");
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(new Color(255, 177, 115));

        JLabel name = new JLabel("MealMate");
        name.setFont(new Font("SansSerif", Font.BOLD, 23));
        name.setForeground(WHITE);
        brand.add(logo);
        brand.add(name);

        JLabel portal = new JLabel(
            user.isAdmin() ? "ADMINISTRATION" : "STUDENT PORTAL"
        );
        portal.setForeground(SIDEBAR_MUTED);
        portal.setFont(new Font("SansSerif", Font.BOLD, 10));
        portal.setBorder(new EmptyBorder(25, 10, 10, 0));

        sidebarButtons.setOpaque(false);
        sidebarButtons.setLayout(new BoxLayout(sidebarButtons, BoxLayout.Y_AXIS));

        addNavigation("dashboard", "⌂", "Overview");
        addNavigation("meals", "◈", "Explore meals");
        addNavigation("bookings", "▤", "Booking history");

        if (user.isStudent()) {
            addNavigation("account", "⚿", "My Account");
        }

        if (user.isAdmin()) {
            addNavigation("students", "♙", "Student Management");
            addNavigation("manage", "⚙", "Manage meals");
        }

        JPanel menu = new JPanel(new BorderLayout());
        menu.setOpaque(false);
        menu.add(portal, BorderLayout.NORTH);
        menu.add(sidebarButtons, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(brand, BorderLayout.NORTH);
        top.add(menu, BorderLayout.CENTER);

        JPanel userPanel = new JPanel();
        userPanel.setOpaque(false);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(68, 77, 101));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel username = new JLabel(user.username());
        username.setForeground(WHITE);
        username.setFont(new Font("SansSerif", Font.BOLD, 13));
        username.setBorder(new EmptyBorder(14, 8, 4, 8));

        JLabel role = new JLabel(
            user.isAdmin() ? "Administrator" : "Campus student"
        );
        role.setForeground(SIDEBAR_MUTED);
        role.setFont(new Font("SansSerif", Font.PLAIN, 11));
        role.setBorder(new EmptyBorder(0, 8, 14, 8));

        JButton logout = new JButton("↪   Log out");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setForeground(WHITE);
        logout.setBackground(new Color(54, 61, 84));
        logout.setFocusPainted(false);
        logout.setBorderPainted(false);
        logout.setFont(new Font("SansSerif", Font.BOLD, 12));
        logout.setBorder(new EmptyBorder(10, 12, 10, 12));
        logout.addActionListener(event -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        userPanel.add(separator);
        userPanel.add(username);
        userPanel.add(role);
        userPanel.add(logout);

        side.add(top, BorderLayout.NORTH);
        side.add(userPanel, BorderLayout.SOUTH);
        return side;
    }

    private void addNavigation(String key, String icon, String label) {
        JButton button = new JButton(icon + "    " + label);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(13, 13, 13, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(event -> navigate(key));

        navButtons.put(key, button);
        sidebarButtons.add(button);
        sidebarButtons.add(Box.createVerticalStrut(6));
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(CREAM);

        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(WHITE);
        topbar.setBorder(new EmptyBorder(19, 27, 19, 27));

        JPanel headings = new JPanel(new GridLayout(0, 1, 0, 5));
        headings.setOpaque(false);

        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        pageTitle.setForeground(TEXT);
        pageSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pageSubtitle.setForeground(MUTED);

        headings.add(pageTitle);
        headings.add(pageSubtitle);

        JLabel badge = new JLabel(
            user.isAdmin() ? "  ADMIN MODE  " : "  STUDENT MODE  "
        );
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setForeground(ORANGE);
        badge.setBackground(new Color(255, 237, 213));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(8, 7, 8, 7));

        topbar.add(headings, BorderLayout.CENTER);
        topbar.add(badge, BorderLayout.EAST);

        pageHost.setBackground(CREAM);
        pageHost.setBorder(new EmptyBorder(22, 26, 18, 26));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(new EmptyBorder(9, 26, 9, 26));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(MUTED);

        JLabel footerRight = new JLabel("MEALMATE  •  CAMPUS DINING");
        footerRight.setFont(new Font("SansSerif", Font.BOLD, 9));
        footerRight.setForeground(MUTED);

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(footerRight, BorderLayout.EAST);

        workspace.add(topbar, BorderLayout.NORTH);
        workspace.add(pageHost, BorderLayout.CENTER);
        workspace.add(footer, BorderLayout.SOUTH);
        return workspace;
    }

    private void navigate(String page) {
        navButtons.forEach((key, button) -> {
            boolean selected = key.equals(page);
            button.setBackground(selected ? ORANGE : SIDEBAR);
            button.setForeground(selected ? WHITE : SIDEBAR_MUTED);
        });

        JPanel content;

        switch (page) {
            case "meals" -> {
                pageTitle.setText("Explore meals");
                pageSubtitle.setText("Find your next campus meal.");
                content = createMealsPage();
            }
            case "bookings" -> {
                pageTitle.setText(user.isAdmin()
                    ? "Booking management" : "My bookings");
                pageSubtitle.setText("Track orders, receipts and pickup.");
                content = createBookingsPage();
            }
            case "account" -> {
                pageTitle.setText("My Account");
                pageSubtitle.setText("Manage your own login credentials.");
                content = createAccountPage();
            }
            case "students" -> {
                pageTitle.setText("Student Management");
                pageSubtitle.setText("Manage records, logins and wallets.");
                content = createStudentsPage();
            }
            case "manage" -> {
                pageTitle.setText("Manage meals");
                pageSubtitle.setText("Maintain menu items and availability.");
                content = createManageMealsPage();
            }
            default -> {
                pageTitle.setText("Your campus, your meals");
                pageSubtitle.setText("A simpler way to plan cafeteria meals.");
                content = createDashboardPage();
            }
        }

        pageHost.removeAll();
        pageHost.add(content, BorderLayout.CENTER);
        pageHost.revalidate();
        pageHost.repaint();
    }

    // ==================== DASHBOARD ====================

    private JPanel createDashboardPage() {
        JPanel page = verticalPanel();

        JPanel hero = new JPanel(new BorderLayout(15, 10));
        hero.setBackground(SIDEBAR);
        hero.setBorder(new EmptyBorder(24, 25, 24, 25));

        JPanel heroText = new JPanel(new GridLayout(0, 1, 0, 7));
        heroText.setOpaque(false);

        JLabel title = new JLabel(user.isAdmin()
            ? "Welcome back, Admin!" : "Hungry? Let's fix that.");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(WHITE);

        JLabel description = new JLabel(user.isAdmin()
            ? "Keep campus dining running smoothly."
            : "Browse the menu, book ahead and pick up with ease.");
        description.setForeground(new Color(219, 226, 240));

        heroText.add(title);
        heroText.add(description);

        JButton explore = new JButton(
            user.isAdmin() ? "View bookings  →" : "Explore menu  →"
        );
        explore.setForeground(WHITE);
        explore.setBackground(ORANGE);
        explore.setFocusPainted(false);
        explore.setBorderPainted(false);
        explore.addActionListener(event ->
            navigate(user.isAdmin() ? "bookings" : "meals"));

        hero.add(heroText, BorderLayout.CENTER);
        hero.add(explore, BorderLayout.EAST);

        List<Meal> meals = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();

        try {
            for (Meal meal : mealService.allForAdmin()) {
                if (meal.active()) meals.add(meal);
            }
            bookings = user.isAdmin()
                ? bookingService.allBookings()
                : bookingService.studentBookings(requireStudentId());
        } catch (Exception ex) {
            showError(ex);
        }

        long pending = bookings.stream()
            .filter(b -> "BOOKED".equalsIgnoreCase(b.status())).count();
        long collected = bookings.stream()
            .filter(b -> "COLLECTED".equalsIgnoreCase(b.status())).count();

        JPanel metrics = new JPanel(new GridLayout(1, 3, 14, 0));
        metrics.setOpaque(false);
        metrics.add(metric("AVAILABLE MEALS", "" + meals.size(),
            "Currently on the menu", ORANGE));
        metrics.add(metric(user.isAdmin() ? "ALL BOOKINGS" : "MY BOOKINGS",
            "" + bookings.size(), "Recorded in MealMate",
            new Color(124, 58, 237)));
        metrics.add(metric("AWAITING PICKUP", "" + pending,
            collected + " collected", new Color(13, 148, 136)));

        JPanel recent = whiteCard();
        JPanel recentHead = new JPanel(new BorderLayout());
        recentHead.setOpaque(false);
        recentHead.add(sectionTitle(user.isAdmin()
            ? "Recent campus orders" : "Your latest bookings"),
            BorderLayout.WEST);

        JButton viewAll = textButton("View all →");
        viewAll.addActionListener(event -> navigate("bookings"));
        recentHead.add(viewAll, BorderLayout.EAST);

        DefaultTableModel model = model(
            "Booking", "Meal", "Quantity", "Total", "Status");
        JTable table = new JTable(model);
        styleTable(table);

        for (int i = bookings.size() - 1;
                i >= 0 && model.getRowCount() < 5; i--) {
            Booking b = bookings.get(i);
            model.addRow(new Object[]{
                "#" + b.id(), b.mealName(), b.quantity(),
                "₹ " + money(b.totalAmount()), b.status()
            });
        }

        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"—", "No bookings yet", "—", "—", "—"});
        }

        recent.add(recentHead, BorderLayout.NORTH);
        recent.add(new JScrollPane(table), BorderLayout.CENTER);

        page.add(hero);
        page.add(Box.createVerticalStrut(17));
        page.add(metrics);
        page.add(Box.createVerticalStrut(17));
        page.add(recent);
        return page;
    }

    private JPanel metric(String title, String value,
            String subtitle, Color accent) {
        JPanel card = whiteCard();
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(4, 1));

        JPanel labels = new JPanel(new GridLayout(0, 1, 0, 5));
        labels.setOpaque(false);
        labels.setBorder(new EmptyBorder(3, 10, 3, 3));

        JLabel caption = new JLabel(title);
        caption.setFont(new Font("SansSerif", Font.BOLD, 10));
        caption.setForeground(MUTED);

        JLabel number = new JLabel(value);
        number.setFont(new Font("SansSerif", Font.BOLD, 29));
        number.setForeground(TEXT);

        JLabel sub = new JLabel(subtitle);
        sub.setForeground(MUTED);

        labels.add(caption);
        labels.add(number);
        labels.add(sub);
        card.add(stripe, BorderLayout.WEST);
        card.add(labels, BorderLayout.CENTER);
        return card;
    }

    // ==================== MEALS ====================

    private JPanel createMealsPage() {
        JPanel page = verticalPanel();
        JPanel filters = whiteCard();

        JTextField search = new JTextField();
        search.setPreferredSize(new Dimension(160, 34));

        JComboBox<String> category = new JComboBox<>();
        category.addItem("All categories");
        try {
            for (String c : mealService.categories()) category.addItem(c);
        } catch (Exception ex) {
            showError(ex);
        }

        JComboBox<String> foodType = new JComboBox<>(
            new String[]{"All food types", "VEG", "NON_VEG", "EGG", "UNKNOWN"}
        );
        JTextField maxPrice = new JTextField();
        maxPrice.setPreferredSize(new Dimension(65, 34));
        JComboBox<String> sort = new JComboBox<>(new String[]{
            "Recommended", "Name A-Z",
            "Price: Low to high", "Price: High to low"
        });

        JButton apply = UITheme.button("Apply");
        JButton reset = UITheme.secondaryButton("Reset");

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setOpaque(false);
        row.add(new JLabel("Search"));
        row.add(search);
        row.add(category);
        row.add(foodType);
        row.add(new JLabel("Max ₹"));
        row.add(maxPrice);
        row.add(sort);
        row.add(apply);
        row.add(reset);
        filters.add(row, BorderLayout.CENTER);

        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
        grid.setBackground(CREAM);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CREAM);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        Runnable load = () -> {
            grid.removeAll();
            grid.setLayout(new GridLayout(0, 2, 15, 15));

            try {
                String cat = (String) category.getSelectedItem();
                String food = (String) foodType.getSelectedItem();
                String price = maxPrice.getText().trim();

                List<Meal> meals = new ArrayList<>(mealService.search(
                    search.getText().trim(),
                    "All categories".equals(cat) ? null : cat,
                    "All food types".equals(food) ? null : food,
                    price.isEmpty() ? null : price,
                    null
                ));

                if (!user.isAdmin()) meals.removeIf(m -> !m.active());

                String order = (String) sort.getSelectedItem();
                if ("Name A-Z".equals(order)) {
                    meals.sort(Comparator.comparing(
                        Meal::name, String.CASE_INSENSITIVE_ORDER));
                } else if ("Price: Low to high".equals(order)) {
                    meals.sort(Comparator.comparing(Meal::price));
                } else if ("Price: High to low".equals(order)) {
                    meals.sort(Comparator.comparing(Meal::price).reversed());
                }

                for (Meal meal : meals) grid.add(mealCard(meal));
                if (meals.isEmpty()) {
                    grid.add(new JLabel("No meals found. Try changing filters."));
                }
                statusLabel.setText(meals.size() + " meal(s) found.");
            } catch (Exception ex) {
                showError(ex);
            }

            grid.revalidate();
            grid.repaint();
        };

        apply.addActionListener(event -> load.run());
        search.addActionListener(event -> load.run());
        reset.addActionListener(event -> {
            search.setText("");
            category.setSelectedIndex(0);
            foodType.setSelectedIndex(0);
            maxPrice.setText("");
            sort.setSelectedIndex(0);
            load.run();
        });

        page.add(sectionTitle("Find something delicious"));
        page.add(Box.createVerticalStrut(8));
        page.add(filters);
        page.add(Box.createVerticalStrut(12));
        page.add(scroll);
        load.run();
        return page;
    }

    private JPanel mealCard(Meal meal) {
        JPanel card = whiteCard();

        JPanel top = new JPanel(new BorderLayout(8, 5));
        top.setOpaque(false);

        JLabel glyph = new JLabel(meal.likelyNonVegetarian() ? "🍗" : "🍲",
            SwingConstants.CENTER);
        glyph.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 25));
        glyph.setOpaque(true);
        glyph.setBackground(new Color(255, 237, 213));
        glyph.setPreferredSize(new Dimension(48, 48));

        JPanel namePanel = new JPanel(new GridLayout(0, 1, 0, 5));
        namePanel.setOpaque(false);

        JLabel name = new JLabel(meal.name());
        name.setFont(new Font("SansSerif", Font.BOLD, 15));
        name.setForeground(TEXT);

        JLabel category = new JLabel(safe(meal.category()));
        category.setForeground(MUTED);

        namePanel.add(name);
        namePanel.add(category);
        top.add(glyph, BorderLayout.WEST);
        top.add(namePanel, BorderLayout.CENTER);

        JTextArea description = new JTextArea(safe(meal.description()));
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setRows(2);
        description.setOpaque(false);
        description.setForeground(MUTED);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel price = new JLabel("₹ " + money(meal.price()));
        price.setFont(new Font("SansSerif", Font.BOLD, 20));
        price.setForeground(ORANGE);

        JButton book = UITheme.button("Book meal →");
        if (!meal.active() || meal.availableQty() < 1) {
            book.setEnabled(false);
            book.setText("Unavailable");
        }
        book.addActionListener(event -> bookMeal(meal));

        bottom.add(price, BorderLayout.WEST);
        bottom.add(book, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        card.add(description, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private void bookMeal(Meal meal) {
        if (!user.isStudent() || user.studentId() == null) {
            message("Please sign in with a student account to book.");
            return;
        }

        int maxQty = Math.min(100, Math.max(1, meal.availableQty()));
        JSpinner quantity = new JSpinner(
            new SpinnerNumberModel(1, 1, maxQty, 1));
        JTextField date = new JTextField(LocalDate.now().toString());
        JComboBox<String> slot = new JComboBox<>(new String[]{
            "Breakfast", "Lunch", "Evening Snacks", "Dinner"
        });
        JTextArea instructions = new JTextArea(3, 20);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.add(new JLabel("Meal"));
        form.add(new JLabel(meal.name()));
        form.add(new JLabel("Unit price"));
        form.add(new JLabel("₹ " + money(meal.price())));
        form.add(new JLabel("Quantity"));
        form.add(quantity);
        form.add(new JLabel("Booking date (YYYY-MM-DD)"));
        form.add(date);
        form.add(new JLabel("Pickup slot"));
        form.add(slot);
        form.add(new JLabel("Instructions"));
        form.add(new JScrollPane(instructions));

        int result = JOptionPane.showConfirmDialog(
            this, form, "Reserve your meal",
            JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            LocalDate bookingDate = LocalDate.parse(date.getText().trim());
            int qty = (Integer) quantity.getValue();
            BigDecimal total = meal.price().multiply(BigDecimal.valueOf(qty));

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Meal: " + meal.name()
                    + "\nQuantity: " + qty
                    + "\nDate: " + bookingDate
                    + "\nTime slot: " + slot.getSelectedItem()
                    + "\nTotal: ₹ " + money(total)
                    + "\n\nConfirm booking?",
                "Review your order",
                JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            int bookingId = bookingService.create(
                user.studentId(), meal.id(), qty, bookingDate,
                (String) slot.getSelectedItem(),
                instructions.getText().trim());

            statusLabel.setText("Booking #" + bookingId + " created.");
            BookingReceiptDialog.showForBooking(this, bookingId);
            navigate("meals");

        } catch (DateTimeParseException ex) {
            message("Enter the date in YYYY-MM-DD format.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // ==================== MY ACCOUNT ====================

    private JPanel createAccountPage() {
        JPanel page = verticalPanel();

        if (!user.isStudent()) {
            page.add(sectionTitle("Student access required"));
            return page;
        }

        JPanel card = whiteCard();

        JLabel heading = sectionTitle("Your login credentials");
        JLabel currentUsername = new JLabel(
            "Username: " + user.username()
        );
        currentUsername.setFont(new Font("SansSerif", Font.BOLD, 15));
        currentUsername.setForeground(TEXT);

        JTextArea explanation = new JTextArea(
            "You can change your username and password here. "
            + "For your security, the app will ask for your current "
            + "password before saving any changes. After saving, "
            + "you will need to sign in again with the new credentials."
        );
        explanation.setWrapStyleWord(true);
        explanation.setLineWrap(true);
        explanation.setEditable(false);
        explanation.setOpaque(false);
        explanation.setRows(4);
        explanation.setForeground(MUTED);

        JButton change = UITheme.button("Change username / password");
        change.addActionListener(event -> changeOwnCredentials());

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(heading);
        details.add(Box.createVerticalStrut(16));
        details.add(currentUsername);
        details.add(Box.createVerticalStrut(12));
        details.add(explanation);
        details.add(Box.createVerticalStrut(16));
        details.add(change);

        card.add(details, BorderLayout.CENTER);
        page.add(card);
        return page;
    }

    private void changeOwnCredentials() {
        JTextField username = new JTextField(user.username());
        JPasswordField current = new JPasswordField();
        JPasswordField newPassword = new JPasswordField();
        JPasswordField confirm = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.add(new JLabel("Current password"));
        form.add(current);
        form.add(new JLabel("New username"));
        form.add(username);
        form.add(new JLabel("New password"));
        form.add(newPassword);
        form.add(new JLabel("Confirm new password"));
        form.add(confirm);

        int answer = JOptionPane.showConfirmDialog(
            this, form, "Change login credentials",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (answer != JOptionPane.OK_OPTION) return;

        char[] currentChars = current.getPassword();
        char[] newChars = newPassword.getPassword();
        char[] confirmChars = confirm.getPassword();

        try {
            if (!java.util.Arrays.equals(newChars, confirmChars)) {
                message("The new passwords do not match.");
                return;
            }

            String currentPassword = new String(currentChars);
            String newPasswordText = new String(newChars);

            authDao.changeOwnCredentials(
                user.userId(),
                currentPassword,
                username.getText().trim(),
                newPasswordText);

            message(
                "Your credentials have been changed successfully.\n\n"
                + "Please sign in again using your new username "
                + "and password.");

            dispose();
            new LoginFrame().setVisible(true);

        } catch (Exception ex) {
            showError(ex);
        } finally {
            java.util.Arrays.fill(currentChars, '\0');
            java.util.Arrays.fill(newChars, '\0');
            java.util.Arrays.fill(confirmChars, '\0');
            current.setText("");
            newPassword.setText("");
            confirm.setText("");
        }
    }

    // ==================== BOOKINGS ====================

    private JPanel createBookingsPage() {
        JPanel page = verticalPanel();

        String[] columns = user.isAdmin()
            ? new String[]{
                "Booking ID", "Roll No.", "Student", "Meal",
                "Qty", "Total (₹)", "Status", "Booked At", "Cancelled At"
            }
            : new String[]{
                "Booking ID", "Meal", "Qty", "Total (₹)",
                "Status", "Booked At", "Cancelled At"
            };

        DefaultTableModel model = model(columns);
        JTable table = new JTable(model);
        styleTable(table);

        JButton refresh = UITheme.secondaryButton("↻ Refresh");
        JButton receipt = UITheme.secondaryButton("View receipt");
        JButton change = UITheme.secondaryButton("Change quantity");
        JButton cancel = UITheme.dangerButton("Cancel booking");
        JButton collected = UITheme.button("Mark collected");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        actions.setOpaque(false);
        actions.add(refresh);
        actions.add(receipt);
        actions.add(change);
        actions.add(cancel);
        if (user.isAdmin()) actions.add(collected);

        Runnable load = () -> {
            model.setRowCount(0);
            try {
                List<Booking> bookings = user.isAdmin()
                    ? bookingService.allBookings()
                    : bookingService.studentBookings(requireStudentId());

                for (Booking b : bookings) {
                    if (user.isAdmin()) {
                        model.addRow(new Object[]{
                            b.id(), b.rollNo(), b.studentName(), b.mealName(),
                            b.quantity(), money(b.totalAmount()), b.status(),
                            timestamp(b.bookedAt()), timestamp(b.cancelledAt())
                        });
                    } else {
                        model.addRow(new Object[]{
                            b.id(), b.mealName(), b.quantity(),
                            money(b.totalAmount()), b.status(),
                            timestamp(b.bookedAt()), timestamp(b.cancelledAt())
                        });
                    }
                }
                statusLabel.setText(bookings.size() + " booking(s) loaded.");
            } catch (Exception ex) {
                showError(ex);
            }
        };

        refresh.addActionListener(event -> load.run());

        receipt.addActionListener(event -> {
            int row = selectedRow(table);
            if (row < 0) {
                message("Select a booking first.");
                return;
            }
            BookingReceiptDialog.showForBooking(
                this, integerAt(model, row, 0));
        });

        change.addActionListener(event -> {
            int row = selectedRow(table);
            if (row < 0) {
                message("Select a booking first.");
                return;
            }

            int id = integerAt(model, row, 0);
            int statusCol = user.isAdmin() ? 6 : 4;
            if (!"BOOKED".equalsIgnoreCase(stringAt(model, row, statusCol))) {
                message("Only BOOKED orders can be changed.");
                return;
            }

            int qtyCol = user.isAdmin() ? 4 : 2;
            int currentQty = integerAt(model, row, qtyCol);
            JSpinner qty = new JSpinner(
                new SpinnerNumberModel(currentQty, 1, 100, 1));

            int answer = JOptionPane.showConfirmDialog(
                this, qty, "Change quantity", JOptionPane.OK_CANCEL_OPTION);
            if (answer != JOptionPane.OK_OPTION) return;

            try {
                bookingService.changeQuantity(
                    id, user.isAdmin() ? null : requireStudentId(),
                    (Integer) qty.getValue());
                message("Booking updated.");
                load.run();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        cancel.addActionListener(event -> {
            int row = selectedRow(table);
            if (row < 0) {
                message("Select a booking first.");
                return;
            }

            int id = integerAt(model, row, 0);
            int statusCol = user.isAdmin() ? 6 : 4;
            if (!"BOOKED".equalsIgnoreCase(stringAt(model, row, statusCol))) {
                message("Only BOOKED orders can be cancelled.");
                return;
            }

            int answer = JOptionPane.showConfirmDialog(
                this, "Cancel booking #" + id + "?",
                "Confirm cancellation", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;

            try {
                bookingService.cancel(
                    id, user.isAdmin() ? null : requireStudentId());
                message("Booking cancelled.");
                load.run();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        collected.addActionListener(event -> {
            if (!user.isAdmin()) return;

            int row = selectedRow(table);
            if (row < 0) {
                message("Select a booking first.");
                return;
            }

            int id = integerAt(model, row, 0);
            if (!"BOOKED".equalsIgnoreCase(stringAt(model, row, 6))) {
                message("Only BOOKED orders can be marked collected.");
                return;
            }

            int answer = JOptionPane.showConfirmDialog(
                this, "Confirm meal handed over for booking #" + id + "?",
                "Confirm pickup", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;

            try {
                bookingService.markCollected(id);
                message("Booking #" + id + " is now COLLECTED.");
                load.run();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(sectionTitle(
            user.isAdmin() ? "All campus bookings" : "Your booking history"),
            BorderLayout.NORTH);
        heading.add(actions, BorderLayout.SOUTH);

        page.add(heading);
        page.add(Box.createVerticalStrut(12));
        page.add(new JScrollPane(table));
        load.run();
        return page;
    }

    // ==================== STUDENT MANAGEMENT ====================

    private JPanel createStudentsPage() {
        JPanel page = verticalPanel();

        if (!user.isAdmin()) {
            page.add(sectionTitle("Administrator access required"));
            return page;
        }

        DefaultTableModel model = model(
            "Student ID", "Roll Number", "Full Name",
            "Email", "Phone", "Wallet Balance (₹)");

        JTable table = new JTable(model);
        styleTable(table);

        JButton refresh = UITheme.secondaryButton("↻ Refresh");
        JButton edit = UITheme.secondaryButton("Edit profile");
        JButton wallet = UITheme.button("Add wallet funds");
        JButton createLogin = UITheme.button("Create student login");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        actions.setOpaque(false);
        actions.add(refresh);
        actions.add(edit);
        actions.add(wallet);
        actions.add(createLogin);

        Runnable load = () -> {
            model.setRowCount(0);
            try {
                for (Student s : studentDao.findAll()) {
                    model.addRow(new Object[]{
                        s.id(), s.rollNo(), s.fullName(), s.email(),
                        s.phone(), money(s.walletBalance())
                    });
                }
                statusLabel.setText(
                    model.getRowCount() + " student record(s) loaded.");
            } catch (Exception ex) {
                showError(ex);
            }
        };

        refresh.addActionListener(event -> load.run());

        edit.addActionListener(event -> {
            int row = selectedRow(table);
            if (row < 0) {
                message("Select a student first.");
                return;
            }

            int id = integerAt(model, row, 0);
            JTextField name = new JTextField(stringAt(model, row, 2));
            JTextField email = new JTextField(stringAt(model, row, 3));
            JTextField phone = new JTextField(stringAt(model, row, 4));

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
            form.add(new JLabel("Full name")); form.add(name);
            form.add(new JLabel("Email")); form.add(email);
            form.add(new JLabel("Phone")); form.add(phone);

            int answer = JOptionPane.showConfirmDialog(
                this, form, "Edit Student #" + id,
                JOptionPane.OK_CANCEL_OPTION);
            if (answer != JOptionPane.OK_OPTION) return;

            try {
                studentDao.updateProfile(
                    id, name.getText(), email.getText(), phone.getText());
                message("Student profile updated.");
                load.run();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        wallet.addActionListener(event -> {
            int row = selectedRow(table);
            if (row < 0) {
                message("Select a student first.");
                return;
            }

            int id = integerAt(model, row, 0);
            String studentName = stringAt(model, row, 2);
            String value = JOptionPane.showInputDialog(
                this, "Amount to add to " + studentName + "'s wallet (₹):",
                "Wallet top-up", JOptionPane.QUESTION_MESSAGE);
            if (value == null) return;

            try {
                BigDecimal amount = new BigDecimal(value.trim());
                if (amount.signum() <= 0 || amount.scale() > 2) {
                    message("Enter a positive amount with up to two decimals.");
                    return;
                }

                int answer = JOptionPane.showConfirmDialog(
                    this, "Add ₹" + money(amount) + " to " + studentName + "?",
                    "Confirm top-up", JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) return;

                studentDao.creditWallet(id, amount);
                message("Wallet topped up.");
                load.run();
            } catch (NumberFormatException ex) {
                message("Enter a valid amount, such as 100 or 150.50.");
            } catch (Exception ex) {
                showError(ex);
            }
        });

        createLogin.addActionListener(event -> {
            int row = selectedRow(table);
            if (row < 0) {
                message("Select a student first.");
                return;
            }

            int id = integerAt(model, row, 0);
            String studentName = stringAt(model, row, 2);

            JTextField username = new JTextField(stringAt(model, row, 1));
            JPasswordField password = new JPasswordField();
            JPasswordField confirm = new JPasswordField();

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
            form.add(new JLabel("Student")); form.add(new JLabel(studentName));
            form.add(new JLabel("Username")); form.add(username);
            form.add(new JLabel("Temporary password")); form.add(password);
            form.add(new JLabel("Confirm password")); form.add(confirm);

            int answer = JOptionPane.showConfirmDialog(
                this, form, "Create / Reset Student Login",
                JOptionPane.OK_CANCEL_OPTION);
            if (answer != JOptionPane.OK_OPTION) return;

            char[] p1 = password.getPassword();
            char[] p2 = confirm.getPassword();

            try {
                if (!java.util.Arrays.equals(p1, p2)) {
                    message("Passwords do not match.");
                    return;
                }

                authDao.createStudentLogin(
                    id, username.getText().trim(), new String(p1));

                message(
                    "Student credentials saved.\n\nUsername: "
                    + username.getText().trim()
                    + "\nGive the temporary password to the student privately.");
                load.run();
            } catch (Exception ex) {
                showError(ex);
            } finally {
                java.util.Arrays.fill(p1, '\0');
                java.util.Arrays.fill(p2, '\0');
                password.setText("");
                confirm.setText("");
            }
        });

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(sectionTitle("Registered students"), BorderLayout.NORTH);
        heading.add(actions, BorderLayout.SOUTH);

        page.add(heading);
        page.add(Box.createVerticalStrut(12));
        page.add(new JScrollPane(table));
        load.run();
        return page;
    }

    // ==================== MANAGE MEALS ====================

    private JPanel createManageMealsPage() {
        JPanel page = verticalPanel();

        if (!user.isAdmin()) {
            page.add(sectionTitle("Administrator access required"));
            return page;
        }

        DefaultTableModel model = model(
            "Meal ID", "Meal", "Category", "Price (₹)",
            "Stock", "Active", "Description");
        JTable table = new JTable(model);
        styleTable(table);

        JButton add = UITheme.button("+ Add meal");
        JButton deactivate = UITheme.dangerButton("Deactivate selected");
        JButton refresh = UITheme.secondaryButton("↻ Refresh");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        actions.setOpaque(false);
        actions.add(add);
        actions.add(deactivate);
        actions.add(refresh);

        Runnable load = () -> {
            model.setRowCount(0);
            try {
                for (Meal m : mealService.allForAdmin()) {
                    model.addRow(new Object[]{
                        m.id(), m.name(), m.category(), money(m.price()),
                        m.availableQty(), m.active() ? "Yes" : "No",
                        m.description()
                    });
                }
            } catch (Exception ex) {
                showError(ex);
            }
        };

        add.addActionListener(event -> {
            JTextField name = new JTextField();
            JTextField category = new JTextField("Lunch");
            JTextField description = new JTextField();
            JComboBox<String> food = new JComboBox<>(
                new String[]{"VEG", "NON_VEG", "EGG", "UNKNOWN"});
            JTextField price = new JTextField();
            JTextField stock = new JTextField("20");

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
            form.add(new JLabel("Name")); form.add(name);
            form.add(new JLabel("Category")); form.add(category);
            form.add(new JLabel("Description")); form.add(description);
            form.add(new JLabel("Food type")); form.add(food);
            form.add(new JLabel("Price")); form.add(price);
            form.add(new JLabel("Quantity")); form.add(stock);

            int answer = JOptionPane.showConfirmDialog(
                this, form, "Add a menu item",
                JOptionPane.OK_CANCEL_OPTION);
            if (answer != JOptionPane.OK_OPTION) return;

            try {
                mealService.create(
                    name.getText(), category.getText(),
                    (String) food.getSelectedItem(),
                    description.getText(), price.getText(), stock.getText());
                message("Meal added successfully.");
                load.run();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        deactivate.addActionListener(event -> {
            int row = selectedRow(table);
            if (row < 0) {
                message("Select a meal first.");
                return;
            }

            int id = integerAt(model, row, 0);
            int answer = JOptionPane.showConfirmDialog(
                this, "Deactivate this meal?",
                "Confirm", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;

            try {
                mealService.deactivate(id);
                message("Meal deactivated.");
                load.run();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        refresh.addActionListener(event -> load.run());

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(sectionTitle("Menu administration"), BorderLayout.NORTH);
        heading.add(actions, BorderLayout.SOUTH);

        page.add(heading);
        page.add(Box.createVerticalStrut(12));
        page.add(new JScrollPane(table));
        load.run();
        return page;
    }

    // ==================== HELPERS ====================

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CREAM);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel whiteCard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(LINE, 1),
            new EmptyBorder(16, 17, 16, 17)));
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(TEXT);
        return label;
    }

    private JButton textButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setForeground(ORANGE);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleTable(JTable table) {
        UITheme.styleTable(table);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
    }

    private int selectedRow(JTable table) {
        int selected = table.getSelectedRow();
        return selected < 0 ? -1 : table.convertRowIndexToModel(selected);
    }

    private int integerAt(DefaultTableModel model, int row, int col) {
        return Integer.parseInt(model.getValueAt(row, col).toString());
    }

    private String stringAt(DefaultTableModel model, int row, int col) {
        Object value = model.getValueAt(row, col);
        return value == null ? "" : value.toString();
    }

    private int requireStudentId() {
        if (user.studentId() == null || user.studentId() <= 0) {
            throw new IllegalStateException(
                "A valid student account is required.");
        }
        return user.studentId();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String money(BigDecimal amount) {
        return amount == null ? "0.00"
            : amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String timestamp(Timestamp value) {
        return value == null ? "—" : value.toString();
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(
            this, text, "MealMate", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(Exception exception) {
        String text = exception.getMessage();
        if (text == null || text.isBlank()) {
            text = exception.getClass().getSimpleName();
        }
        JOptionPane.showMessageDialog(
            this, text, "MealMate Error", JOptionPane.ERROR_MESSAGE);
    }
}