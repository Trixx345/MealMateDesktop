
package com.mealmate.ui;

import com.mealmate.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class RegisterDialog extends JDialog {

    private final AuthService authService;

    private JTextField rollField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;

    public RegisterDialog(
            Frame owner,
            AuthService authService) {

        super(owner, "Create Student Account", true);
        this.authService = authService;

        setDefaultCloseOperation(
            WindowConstants.DISPOSE_ON_CLOSE
        );
        setSize(520, 490);
        setMinimumSize(new Dimension(460, 450));
        setLocationRelativeTo(owner);

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 14));
        root.setBackground(UITheme.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 22, 20, 22));

        root.add(
            UITheme.header(
                "Create account",
                "Register as a student to start booking meals."
            ),
            BorderLayout.NORTH
        );

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 18, 16, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 7, 7, 7);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        rollField = new JTextField(22);
        nameField = new JTextField(22);
        emailField = new JTextField(22);
        phoneField = new JTextField(22);
        passwordField = new JPasswordField(22);
        confirmField = new JPasswordField(22);

        addRow(form, g, "Roll number", rollField);
        addRow(form, g, "Full name", nameField);
        addRow(form, g, "Email (optional)", emailField);
        addRow(form, g, "Phone (optional)", phoneField);
        addRow(form, g, "Password (minimum 8)", passwordField);
        addRow(form, g, "Confirm password", confirmField);

        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actions.setOpaque(false);

        JButton cancelButton = new JButton("Cancel");
        JButton createButton = UITheme.button("Create Account");

        actions.add(cancelButton);
        actions.add(createButton);

        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);

        cancelButton.addActionListener(e -> dispose());
        createButton.addActionListener(e -> register());
        getRootPane().setDefaultButton(createButton);
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints g,
            String label,
            JComponent field) {

        g.gridx = 0;
        g.weightx = 0;
        panel.add(new JLabel(label), g);

        g.gridx = 1;
        g.weightx = 1;
        panel.add(field, g);

        g.gridy++;
    }

    private void register() {
        char[] passwordChars = passwordField.getPassword();
        char[] confirmChars = confirmField.getPassword();

        try {
            authService.register(
                rollField.getText(),
                nameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                new String(passwordChars),
                new String(confirmChars)
            );

            JOptionPane.showMessageDialog(
                this,
                "Your account has been created.\n"
                + "Sign in using your roll number and password.",
                "Registration successful",
                JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                friendlyMessage(ex),
                "Registration failed",
                JOptionPane.ERROR_MESSAGE
            );

        } finally {
            Arrays.fill(passwordChars, '\0');
            Arrays.fill(confirmChars, '\0');
            passwordField.setText("");
            confirmField.setText("");
        }
    }

    private String friendlyMessage(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return ex.getMessage();
        }

        String message = ex.getMessage();

        if (message != null
                && message.toLowerCase().contains("duplicate")) {
            return "That roll number or username is already registered.";
        }

        return "Registration failed. Check the MySQL connection "
            + "and try again.";
    }
}
