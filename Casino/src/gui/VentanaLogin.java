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

public class VentanaLogin extends JFrame {
    
    private Database database;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JButton loginButton;
    private JButton registerButton; 

    public VentanaLogin(Database database) {
        this.database = database;
        setTitle("Casino Login");
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10)); 

       
        JPanel fieldPanel = new JPanel(new GridLayout(3, 2, 10, 10)); 

        
        fieldPanel.add(new JLabel("Email:"));
        emailField = new JTextField(20);
        fieldPanel.add(emailField);

        
        fieldPanel.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField(20);
        fieldPanel.add(passwordField);
        
        
        fieldPanel.add(new JLabel("Rol:"));
        String[] roles = {"ADMINISTRADOR", "EMPLEADO", "JUGADOR"};
        rolComboBox = new JComboBox<>(roles);
        fieldPanel.add(rolComboBox);
        
       
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0)); 
        
        loginButton = new JButton("Iniciar Sesión");
        registerButton = new JButton("Registrarse (Jugador)"); 
        registerButton.setEnabled(false); 

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        
        add(fieldPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> openRegisterDialog());
        
        
        rolComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                boolean isPlayer = "JUGADOR".equals(rolComboBox.getSelectedItem());
                registerButton.setEnabled(isPlayer);
            }
        });

        pack(); 
        setLocationRelativeTo(null); 
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
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
            dispose(); 
            
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas o Rol no coincide.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openRegisterDialog() {
        
        new VentanaRegistro(this, database).setVisible(true);
    }
}