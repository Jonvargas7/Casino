package gui; 

import domain.Administrador;
import domain.Empleado;
import domain.Jugador;
import domain.Usuario;
import domain.RolUsuario; // <--- Importación de RolUsuario

import gestor.Database; 

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer; 

public class VentanaLogin extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private Database database;
    private Consumer<Usuario> onLoginSuccess; 

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JButton loginButton;
    private JButton registerButton; 

    public VentanaLogin(Database database, Consumer<Usuario> onLoginSuccess) {
        this.database = database;
        this.onLoginSuccess = onLoginSuccess; 
        setTitle("Casino Login");
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10)); 
        setSize(400, 250);
        setLocationRelativeTo(null); // Centrar

        JPanel fieldPanel = new JPanel(new GridLayout(4, 2, 10, 10)); // 4 filas para rol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        fieldPanel.add(new JLabel("Email:"));
        emailField = new JTextField(20);
        fieldPanel.add(emailField);

        fieldPanel.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField(20);
        fieldPanel.add(passwordField);

        fieldPanel.add(new JLabel("Rol:"));
        // Inicializa el ComboBox con los nombres del enum RolUsuario
        String[] roles = Arrays.stream(RolUsuario.values())
                               .map(RolUsuario::name)
                               .toArray(String[]::new);
        rolComboBox = new JComboBox<>(roles);
        fieldPanel.add(rolComboBox);

        fieldPanel.add(new JLabel("")); // Espacio en blanco
        
        loginButton = new JButton("Login");
        registerButton = new JButton("Registro (Jugador)"); 
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        add(fieldPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> openRegisterDialog());

        setVisible(true);
    }
    
    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String rolSeleccionadoStr = (String) rolComboBox.getSelectedItem(); 
        
        if (rolSeleccionadoStr == null || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe rellenar todos los campos.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // CORRECCIÓN CLAVE: Convertir el String a RolUsuario ENUM
            RolUsuario rol = RolUsuario.valueOf(rolSeleccionadoStr); 
            
            Usuario usuario = database.login(email, password, rol);

            if (usuario != null) {
                String mensaje = String.format("Bienvenido %s! Rol: %s", usuario.getNombre(), rol);
                
                if (usuario instanceof Jugador) {
                    mensaje += String.format(" (Saldo: %.2f €)", ((Jugador)usuario).getSaldo()); 
                } else if (usuario instanceof Administrador) {
                    mensaje += " (Acceso Total)";
                } else if (usuario instanceof Empleado) {
                    mensaje += " (Acceso de Crupier/Empleado)";
                }
                
                JOptionPane.showMessageDialog(this, mensaje, "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);
                
                if (onLoginSuccess != null) {
                    onLoginSuccess.accept(usuario); 
                }
                
                dispose(); 
                
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas o Rol no coincide.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
             // Esto sucede si el String del ComboBox no coincide con un nombre de enum (No debería pasar si el ComboBox se inicializa correctamente)
             JOptionPane.showMessageDialog(this, "Error: Rol de usuario no válido.", "Error Interno", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openRegisterDialog() {
        // Se asume que VentanaRegistro existe
        // Si VentanaRegistro no existe, crearía una clase simple aquí
        // new VentanaRegistro(this, database).setVisible(true);
        JOptionPane.showMessageDialog(this, "Lógica de registro pendiente. Usa: player@casino.com / player123", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}