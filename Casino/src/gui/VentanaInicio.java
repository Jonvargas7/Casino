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

    private JPanel pPrincipal = new JPanel(new GridLayout(2, 2, 20, 20));
    private JPanel barraSuperior = new JPanel(new BorderLayout());
    private JPanel pBotonesSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
    private JPanel pTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));

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

        // Barra superior verde
        barraSuperior.setBackground(new Color(34, 85, 55)); // verde elegante
        barraSuperior.setPreferredSize(new Dimension(900, 70));

        JLabel titulo = new JLabel("Casino");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JButton bJuegos = new JButton("🎮 Juegos");
        bJuegos.setBackground(Color.WHITE);
        bJuegos.setFont(new Font("SansSerif", Font.BOLD, 14));

        pTitulo.setBackground(new Color(34, 85, 55));
        pBotonesSuperior.setBackground(new Color(34, 85, 55));

        pTitulo.add(bJuegos);
        pTitulo.add(Box.createHorizontalStrut(10));
        pTitulo.add(titulo);

        bLogin.setBackground(Color.WHITE);
        bSignUp.setBackground(Color.WHITE);
        pBotonesSuperior.add(bLogin);
        pBotonesSuperior.add(bSignUp);

        barraSuperior.add(pTitulo, BorderLayout.WEST);
        barraSuperior.add(pBotonesSuperior, BorderLayout.EAST);
        add(barraSuperior, BorderLayout.NORTH);

        // Panel principal
        pPrincipal.setBackground(Color.WHITE);
        pPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        configurarBoton(bJuego1, propiedades.getProperty("blackJack"), "BlackJack");
        configurarBoton(bJuego2, propiedades.getProperty("highlow"), "High-Low");
        configurarBoton(bJuego3, propiedades.getProperty("ruleta"), "Ruleta");

        pPrincipal.add(crearPanelJuego(bJuego1));
        pPrincipal.add(crearPanelJuego(bJuego2));
        pPrincipal.add(crearPanelJuego(bJuego3));

        add(pPrincipal, BorderLayout.CENTER);

        // Acciones de los botones
        bJuego1.addActionListener(e -> abrirVentana("BlackJack"));
        bJuego2.addActionListener(e -> abrirVentana("High-Low"));
        bJuego3.addActionListener(e -> abrirVentana("Ruleta"));

        bLogin.addActionListener(e -> JOptionPane.showMessageDialog(this, "Ventana de Login (pendiente)"));
        bSignUp.addActionListener(e -> new VentanaRegistro());

        setVisible(true);
    }

    private void configurarBoton(JButton boton, String ruta, String tooltip) {
        boton.setBorder(null);
        boton.setBackground(Color.WHITE);
        boton.setToolTipText(tooltip);
        if (ruta != null ) {
            ImageIcon icono = new ImageIcon(ruta);
            Image img = icono.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
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
