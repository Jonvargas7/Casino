package gui; 

import domain.Administrador;
import domain.Empleado;
import domain.Jugador;
import domain.Usuario;

import gestor.Database; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer; 

public class VentanaLogin extends JFrame {
    
    private Database database;
    private Consumer<Usuario> onLoginSuccess; 

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JButton loginButton;
    private JButton registerButton; 

    // Constructor que acepta el callback
    public VentanaLogin(Database database, Consumer<Usuario> onLoginSuccess) {
        this.database = database;
        this.onLoginSuccess = onLoginSuccess; 
        setTitle("Casino Login");
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10)); 

        JPanel fieldPanel = new JPanel(new GridLayout(3, 2, 10, 10)); 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        fieldPanel.add(new JLabel("Email:"));
        emailField = new JTextField(20);
        fieldPanel.add(emailField);

        fieldPanel.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField(20);
        fieldPanel.add(passwordField);

        fieldPanel.add(new JLabel("Rol:"));
        rolComboBox = new JComboBox<>(new String[]{"JUGADOR", "EMPLEADO", "ADMINISTRADOR"});
        fieldPanel.add(rolComboBox);

        loginButton = new JButton("Login");
        registerButton = new JButton("Sign up (Jugador)");

        loginButton.addActionListener(e -> realizarLogin());
        registerButton.addActionListener(e -> openRegisterDialog());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        add(fieldPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void realizarLogin() {
        String email = emailField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        String rol = (String) rolComboBox.getSelectedItem();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, introduce email y contraseña.", "Error de Login", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario usuario = database.login(email, password, rol);

        if (usuario != null) {
            String mensaje = String.format("Bienvenido %s! Rol: %s", usuario.getNombre(), rol);
            
            if (usuario instanceof Jugador) {
                mensaje += String.format(" (Acceso de Juego - Saldo: %.2f)", ((Jugador)usuario).getSaldo()); 
            } else if (usuario instanceof Administrador) {
                mensaje += " (Acceso Total)";
            } else if (usuario instanceof Empleado) {
                mensaje += " (Acceso de Crupier/Empleado)";
            }
            
            JOptionPane.showMessageDialog(this, mensaje, "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);
            
           
            if (onLoginSuccess != null) {
                // CORRECCIÓN: Notifica a VentanaInicio con el usuario logeado
                onLoginSuccess.accept(usuario); 
            }
            
            // *** BLOQUE DE CÓDIGO INCORRECTO ELIMINADO ***
            // (Ya no se intenta abrir VentanaGestionUsuarios aquí)

            dispose(); // Cierra la ventana de Login
            
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas o Rol no coincide.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openRegisterDialog() {
        // Se asume que VentanaRegistro existe
        new VentanaRegistro(this, database).setVisible(true);
    }
}