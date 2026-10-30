
package com.mealmate.ui;

import com.mealmate.model.AppUser;
import com.mealmate.service.AuthService;
import com.mealmate.util.AppLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginFrame() {
        super("MealMate | Sign In");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(540, 590);
        setMinimumSize(new Dimension(460, 520));
        setLocationRelativeTo(null);

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(UITheme.NAVY);
        banner.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel brand = new JLabel(
            "<html><div style='font-size:30px;"
            + "font-weight:bold;color:white'>MealMate</div>"
            + "<div style='font-size:12px;color:#cbd5e1;"
            + "letter-spacing:2px'>CAMPUS MEAL BOOKING</div></html>"
        );

        banner.add(brand, BorderLayout.WEST);
        root.add(banner, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.BACKGROUND);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER),
            new EmptyBorder(26, 28, 24, 28)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 0, 8, 0);

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.setForeground(UITheme.NAVY);
        card.add(heading, g);

        g.gridy++;
        g.insets = new Insets(0, 0, 20, 0);
        card.add(
            UITheme.subtitle("Sign in to access your meal portal."),
            g
        );

        usernameField = new JTextField(22);
        passwordField = new JPasswordField(22);

        g.gridwidth = 1;
        g.gridy++;
        g.gridx = 0;
        g.weightx = 0;
        g.insets = new Insets(7, 0, 7, 12);
        card.add(new JLabel("Username"), g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(7, 0, 7, 0);
        card.add(usernameField, g);

        g.gridy++;
        g.gridx = 0;
        g.weightx = 0;
        g.insets = new Insets(7, 0, 7, 12);
        card.add(new JLabel("Password"), g);

        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(7, 0, 7, 0);
        card.add(passwordField, g);

        loginButton = UITheme.button("Sign In");
        loginButton.setPreferredSize(new Dimension(100, 42));

        JButton registerButton =
            new JButton("Create student account");
        registerButton.setFocusPainted(false);

        g.gridx = 0;
        g.gridy++;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(20, 0, 8, 0);
        card.add(loginButton, g);

        g.gridy++;
        g.insets = new Insets(0, 0, 14, 0);
        card.add(registerButton, g);

        JLabel adminHint = new JLabel(
            "Demo admin: admin / admin123"
        );

        JLabel studentHint = new JLabel(
            "Demo student: MB2026001 / student123"
        );

        adminHint.setForeground(UITheme.MUTED);
        studentHint.setForeground(UITheme.MUTED);

        adminHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        studentHint.setFont(new Font("SansSerif", Font.PLAIN, 11));

        g.gridy++;
        g.insets = new Insets(3, 0, 3, 0);
        card.add(adminHint, g);

        g.gridy++;
        g.insets = new Insets(3, 0, 0, 0);
        card.add(studentHint, g);

        center.add(card);
        root.add(center, BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "Java Swing  •  JDBC  •  MySQL",
            SwingConstants.CENTER
        );

        footer.setForeground(UITheme.MUTED);
        footer.setBorder(new EmptyBorder(12, 8, 12, 8));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);

        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());
        getRootPane().setDefaultButton(loginButton);

        registerButton.addActionListener(e -> {
            RegisterDialog dialog =
                new RegisterDialog(this, authService);
            dialog.setVisible(true);
        });
    }

    private void performLogin() {
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        try {
            String username = usernameField.getText().trim();
            char[] passwordChars = passwordField.getPassword();

            if (passwordChars.length == 0) {
                throw new IllegalArgumentException(
                    "Enter your password."
                );
            }

            String password = new String(passwordChars);
            java.util.Arrays.fill(passwordChars, '\0');

            AppUser user = authService.login(username, password);

            AppLogger.info("Login successful for " + user.username());

            MainFrame mainFrame = new MainFrame(user);
            mainFrame.setVisible(true);
            dispose();

        } catch (Exception ex) {
            AppLogger.warning("Login attempt failed.");

            JOptionPane.showMessageDialog(
                this,
                friendlyMessage(ex),
                "Sign-in failed",
                JOptionPane.ERROR_MESSAGE
            );

        } finally {
            loginButton.setEnabled(true);
            loginButton.setText("Sign In");
        }
    }

    private String friendlyMessage(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return ex.getMessage();
        }

        AppLogger.error("Sign-in error", ex);

        return "Unable to sign in. Check your MySQL connection "
            + "and try again.";
    }
}
