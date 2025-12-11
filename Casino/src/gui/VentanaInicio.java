package gui;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import io.Propiedades;

// Importar Database y todas las clases de dominio
import gestor.Database; 
import domain.Usuario; 
import domain.Jugador; 
import domain.Administrador; // Importación necesaria para el chequeo de rol
import domain.Empleado;      // Importación necesaria para el chequeo de rol

public class VentanaInicio extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("VentanaInicioCasino");

    private JPanel pPrincipal = new JPanel(new GridLayout(2, 2, 25, 25));
    private JPanel barraSuperior = new JPanel(new BorderLayout());
    private JPanel pCentroSuperior = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
    private JPanel pTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 10));

    private JButton bJuego1 = new JButton("");
    private JButton bJuego2 = new JButton("");
    private JButton bJuego3 = new JButton("");
    private JButton bLogin = new JButton("Login");
    private JButton bSignUp = new JButton("Sign up"); 
    // AÑADIDO: Botón de gestión
    private JButton bGestionUsuarios;
    
    private Database database; 
    private Usuario usuarioLogeado = null; 

    private Propiedades propiedades;


    public VentanaInicio(Database database) { 
        this.database = database;
        
        setTitle("Casino Royale"); // Corregido el título
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

    
        propiedades = new Propiedades();
        propiedades.cargar();

        String fav = propiedades.getProperty("favicon");
        if (fav != null) {
            Image img = new ImageIcon(fav).getImage();
            setIconImage(img);
        }

        Color azulOscuro = new Color(20, 30, 70);
        barraSuperior.setBackground(azulOscuro);
        barraSuperior.setPreferredSize(new Dimension(900, 80));

        JLabel titulo = new JLabel("Casino Royale");
        titulo.setForeground(new Color(255, 215, 0));
        titulo.setFont(new Font("Serif", Font.BOLD, 26));

        pTitulo.setBackground(azulOscuro);
        pTitulo.add(titulo);

        pCentroSuperior.setBackground(azulOscuro);
        
        // Inicialización del botón de gestión (oculto inicialmente)
        bGestionUsuarios = new JButton("Gestión Usuarios");
        configurarBotonSuperior(bGestionUsuarios);
        bGestionUsuarios.addActionListener(e -> abrirGestionUsuarios());
        
        configurarBotonSuperior(bLogin);
        configurarBotonSuperior(bSignUp);
        // Los botones se añadirán en actualizarEstadoLogin()

        barraSuperior.add(pTitulo, BorderLayout.WEST);
        barraSuperior.add(pCentroSuperior, BorderLayout.EAST); // Usamos EAST para la zona de botones de usuario
        add(barraSuperior, BorderLayout.NORTH);

        pPrincipal.setBackground(new Color(245, 245, 250));
        pPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        // MANTENIENDO LA LÓGICA DE CARGA DE IMÁGENES DEL USUARIO
        configurarBotonJuego(bJuego1, propiedades.getProperty("blackJack"), "BlackJack");
        configurarBotonJuego(bJuego2, propiedades.getProperty("highlow"), "High-Low");
        configurarBotonJuego(bJuego3, propiedades.getProperty("ruleta"), "Ruleta");

        pPrincipal.add(crearPanelJuego(bJuego1));
        pPrincipal.add(crearPanelJuego(bJuego2));
        pPrincipal.add(crearPanelJuego(bJuego3));
        pPrincipal.add(new JLabel(""));

        add(pPrincipal, BorderLayout.CENTER);

        bJuego1.addActionListener(e -> abrirBlackJack());
        bJuego2.addActionListener(e -> abrirHighLow());
        bJuego3.addActionListener(e -> abrirRuleta());

       
        bLogin.addActionListener(e -> abrirLogin());
        
        bSignUp.addActionListener(e -> abrirRegistroOLogout());
        
        actualizarEstadoLogin();
    }
    
    // CORREGIDO/MEJORADO: Maneja todos los botones superiores según el estado
    private void actualizarEstadoLogin() {
        pCentroSuperior.removeAll(); // Limpiamos la barra superior derecha

        if (usuarioLogeado != null) {
            String rol = usuarioLogeado.getClass().getSimpleName();
            
            // Botón de info de usuario
            JLabel lblUsuario = new JLabel(usuarioLogeado.getNombre() + " (" + rol + ")");
            lblUsuario.setForeground(Color.WHITE);
            pCentroSuperior.add(lblUsuario);

            // CRÍTICO: Mostrar botón de gestión si es Admin o Empleado
            if (usuarioLogeado instanceof Administrador || usuarioLogeado instanceof Empleado) {
                pCentroSuperior.add(bGestionUsuarios);
            }
            
            // Botón de Logout
            bSignUp.setText("Logout");
            bSignUp.setActionCommand("Logout");
            pCentroSuperior.add(bSignUp);
            
        } else {
            bLogin.setText("Login");
            bSignUp.setText("Sign up");
            bSignUp.setActionCommand("SignUp");
            pCentroSuperior.add(bLogin);
            pCentroSuperior.add(bSignUp);
        }
        
        pCentroSuperior.revalidate();
        pCentroSuperior.repaint();
    }
    
    private void abrirLogin() {
        VentanaLogin login = new VentanaLogin(database, this::onLoginSuccess);
        login.setVisible(true);
        logger.info("Se ha abierto la ventana de Login");
    }
    
    // CRÍTICO: Callback que se ejecuta tras el login exitoso
    private void onLoginSuccess(Usuario usuario) {
        this.usuarioLogeado = usuario;
        actualizarEstadoLogin();
        logger.info("Usuario logeado: " + usuario.getEmail());
        
        // CRÍTICO: Comprobar el rol y abrir la gestión si procede
        if (usuarioLogeado instanceof Administrador || usuarioLogeado instanceof Empleado) {
            abrirGestionUsuarios();
        }
    }
    
    private void abrirGestionUsuarios() {
        if (usuarioLogeado instanceof Administrador || usuarioLogeado instanceof Empleado) {
            // 1. Crear la instancia en una variable
            VentanaGestionUsuarios ventanaGestion = new VentanaGestionUsuarios(this, database, usuarioLogeado); 
            
            // 2. HACERLA VISIBLE (Esta es la corrección clave)
            ventanaGestion.setVisible(true); // <--- AÑADIR ESTA LÍNEA
            
            logger.info("Abriendo Gestión de Usuarios para: " + usuarioLogeado.getEmail());
        } else {
            // Esto no debería pasar si la lógica de `actualizarEstadoLogin` es correcta
            JOptionPane.showMessageDialog(this, "Tu rol no tiene permiso para acceder a la gestión de usuarios.", "Acceso denegado", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirRegistroOLogout() {
        if ("Logout".equals(bSignUp.getActionCommand())) {
            // Lógica de Logout
            usuarioLogeado = null;
            actualizarEstadoLogin();
            JOptionPane.showMessageDialog(this, "Sesión cerrada.", "Logout", JOptionPane.INFORMATION_MESSAGE);
            logger.info("Sesión cerrada.");
        } else {
            // Lógica de Registro
            new VentanaRegistro(this, database).setVisible(true);
            logger.info("Se ha abierto la ventana de Registro de Jugador");
        }
    }


  
    private void configurarBotonJuego(JButton boton, String ruta, String tooltip) {
        
        if (ruta != null) {
            
            try {
                ImageIcon icon = new ImageIcon(ruta);
                Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                boton.setIcon(new ImageIcon(img));
            } catch (Exception e) {
               
                boton.setText(tooltip);
            }
        } else {
             boton.setText(tooltip);
        }
        boton.setToolTipText(tooltip);
        boton.setPreferredSize(new Dimension(250, 250));
        boton.setBackground(new Color(240, 240, 250));
        boton.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
    }

    private JPanel crearPanelJuego(JButton boton) {
        // MANTENEMOS TU LÓGICA EXACTA
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(boton, BorderLayout.CENTER);
        return panel;
    }

    private void configurarBotonSuperior(JButton boton) {
        // MANTENEMOS TU LÓGICA EXACTA
        boton.setBackground(new Color(255, 215, 0));
        boton.setForeground(Color.BLACK);
        boton.setFont(new Font("SansSerif", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void abrirBlackJack() {
        
        if (usuarioLogeado instanceof Jugador) {
            
            Jugador jugador = (Jugador) usuarioLogeado;
            // Se asume la existencia de VentanaBlackJack
            new VentanaBlackJack(jugador).setVisible(true); 
            logger.info("Abriendo BlackJack para: " + jugador.getEmail());
            
        } else if (usuarioLogeado != null) {
             
             JOptionPane.showMessageDialog(this, "Debes iniciar sesión como Jugador para acceder a este juego.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        } else {
            // No logeado
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para acceder a BlackJack.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void abrirHighLow() {
        
        if (usuarioLogeado instanceof Jugador) {
            
            Jugador jugador = (Jugador) usuarioLogeado;
            // Se asume la existencia de VentanaHighLow
            new VentanaHighLow(jugador).setVisible(true); 
            logger.info("Abriendo High-Low para: " + jugador.getEmail());
            
        } else if (usuarioLogeado != null) {
             
             JOptionPane.showMessageDialog(this, "Debes iniciar sesión como Jugador para acceder a este juego.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        } else {
            // No logeado
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para acceder a High-Low.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void abrirRuleta() {
        
        if (usuarioLogeado instanceof Jugador) {
            
            Jugador jugador = (Jugador) usuarioLogeado;
            // Se asume la existencia de VentanaRuleta
            new VentanaRuleta(jugador).setVisible(true); 
            logger.info("Abriendo Ruleta para: " + jugador.getEmail());
            
        } else if (usuarioLogeado != null) {
             
             JOptionPane.showMessageDialog(this, "Debes iniciar sesión como Jugador para acceder a este juego.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        } else {
            // No logeado
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para acceder a Ruleta.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        }
    }
}