package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import io.Propiedades;

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

    private Propiedades propiedades;

    public VentanaInicio() {
        setTitle("Casino");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        propiedades = new Propiedades();
        propiedades.cargar();

        setIconImage(new ImageIcon(propiedades.getProperty("favicon")).getImage());

        // 🎨 Barra superior azul marino
        Color azulOscuro = new Color(20, 30, 70);
        barraSuperior.setBackground(azulOscuro);
        barraSuperior.setPreferredSize(new Dimension(900, 80));

        JLabel titulo = new JLabel("Casino Royale");
        titulo.setForeground(new Color(255, 215, 0)); // Dorado
        titulo.setFont(new Font("Serif", Font.BOLD, 26));

        pTitulo.setBackground(azulOscuro);
        pTitulo.add(titulo);

        // 🔘 Botones centrados en la parte superior
        pCentroSuperior.setBackground(azulOscuro);
        configurarBotonSuperior(bLogin);
        configurarBotonSuperior(bSignUp);
        pCentroSuperior.add(bLogin);
        pCentroSuperior.add(bSignUp);

        barraSuperior.add(pTitulo, BorderLayout.WEST);
        barraSuperior.add(pCentroSuperior, BorderLayout.CENTER);
        add(barraSuperior, BorderLayout.NORTH);

        // 🎮 Panel de juegos
        pPrincipal.setBackground(new Color(245, 245, 250));
        pPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        configurarBotonJuego(bJuego1, propiedades.getProperty("blackJack"), "BlackJack");
        configurarBotonJuego(bJuego2, propiedades.getProperty("highlow"), "High-Low");
        configurarBotonJuego(bJuego3, propiedades.getProperty("ruleta"), "Ruleta");

        pPrincipal.add(crearPanelJuego(bJuego1));
        pPrincipal.add(crearPanelJuego(bJuego2));
        pPrincipal.add(crearPanelJuego(bJuego3));
        pPrincipal.add(new JLabel("")); // espacio vacío para equilibrio visual

        add(pPrincipal, BorderLayout.CENTER);

        // 🧠 Acciones de los botones
        bJuego1.addActionListener(e -> abrirVentana("BlackJack"));
        bJuego2.addActionListener(e -> abrirVentana("High-Low"));
        bJuego3.addActionListener(e -> abrirVentana("Ruleta"));

        bLogin.addActionListener(e -> JOptionPane.showMessageDialog(this, "Aquí irá la ventana de Login"));
        bSignUp.addActionListener(e -> new VentanaRegistro());

        setVisible(true);
    }

    private void configurarBotonJuego(JButton boton, String ruta, String tooltip) {
        boton.setBorder(null);
        boton.setBackground(Color.WHITE);
        boton.setToolTipText(tooltip);
        if (ruta != null ) {
            ImageIcon icono = new ImageIcon(ruta);
            Image img = icono.getImage().getScaledInstance(240, 240, Image.SCALE_SMOOTH);
            boton.setIcon(new ImageIcon(img));
        }
    }

    private JPanel crearPanelJuego(JButton boton) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(boton, BorderLayout.CENTER);
        return panel;
    }

    private void configurarBotonSuperior(JButton boton) {
        boton.setBackground(new Color(255, 215, 0)); // Dorado
        boton.setForeground(Color.BLACK);
        boton.setFont(new Font("SansSerif", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void abrirVentana(String nombreJuego) {
        JFrame ventanaJuego = new JFrame(nombreJuego);
        ventanaJuego.setSize(400, 300);
        ventanaJuego.setLocationRelativeTo(this);
        ventanaJuego.add(new JLabel("Aquí irá la lógica de " + nombreJuego, SwingConstants.CENTER), BorderLayout.CENTER);
        ventanaJuego.setVisible(true);
        logger.info("Se ha abierto la ventana de " + nombreJuego);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaInicio::new);
    }
}

