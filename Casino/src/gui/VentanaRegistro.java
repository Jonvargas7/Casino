package gui;

import domain.Jugador;
import gestor.Database;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class VentanaRegistro extends JDialog {
    
    private static final Logger logger = Logger.getLogger("VentanaRegistroCasino");
    private Database database;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField initialBalanceField;
    private JButton registerButton;
    private JButton cancelButton;

    public VentanaRegistro(JFrame parent, Database database) {
        super(parent, "Registro de Nuevo Jugador", true);
        this.database = database;
        
        setLayout(new BorderLayout(10, 10));
        JPanel fieldPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        
        fieldPanel.add(new JLabel("Nombre Completo:"));
        nameField = new JTextField(20);
        fieldPanel.add(nameField);

        fieldPanel.add(new JLabel("Email (único):"));
        emailField = new JTextField(20);
        fieldPanel.add(emailField);

        fieldPanel.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField(20);
        fieldPanel.add(passwordField);

        fieldPanel.add(new JLabel("Saldo Inicial (€):"));
        initialBalanceField = new JTextField("100.00", 20);
        fieldPanel.add(initialBalanceField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        registerButton = new JButton("Registrarse");
        cancelButton = new JButton("Cancelar");

        buttonPanel.add(cancelButton);
        buttonPanel.add(registerButton);

        registerButton.addActionListener(e -> attemptRegistration());
        cancelButton.addActionListener(e -> dispose());
        
        add(fieldPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void attemptRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        @SuppressWarnings("deprecation")
        String password = passwordField.getText();
        
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos deben ser completados.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        double initialBalance;
        try {
            initialBalance = Double.parseDouble(initialBalanceField.getText().replace(',', '.'));
            if (initialBalance < 0) {
                 JOptionPane.showMessageDialog(this, "El saldo inicial no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El saldo debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Jugador nuevoJugador = new Jugador(
            0,
            name,
            email,
            password,
            LocalDateTime.now(),
            initialBalance,
            0,
            0.0,
            1
        );

        try {
            database.registrarUsuario(nuevoJugador);
            logger.info("Registro exitoso para: " + email);
            JOptionPane.showMessageDialog(this,
                "¡Registro exitoso! Ya puedes iniciar sesión.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            logger.severe("Error al registrar: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error al registrar: " + ex.getMessage() + "\nVerifique que el email no esté ya en uso.",
                "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }
}