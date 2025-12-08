package gui;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import io.Propiedades;

// Importar Database, necesario para pasar a la VentanaLogin/Registro
import gestor.Database; 

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
    
    private Database database; 

    private Propiedades propiedades;


    public VentanaInicio(Database database) { 
        this.database = database;
        
        setTitle("Casino");
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
        configurarBotonSuperior(bLogin);
        configurarBotonSuperior(bSignUp);
        pCentroSuperior.add(bLogin);
        pCentroSuperior.add(bSignUp);

        barraSuperior.add(pTitulo, BorderLayout.WEST);
        barraSuperior.add(pCentroSuperior, BorderLayout.CENTER);
        add(barraSuperior, BorderLayout.NORTH);

        pPrincipal.setBackground(new Color(245, 245, 250));
        pPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

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
        
        bSignUp.addActionListener(e -> abrirRegistro());

        
    }
    
    private void abrirLogin() {
        
        new VentanaLogin(database).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        new VentanaLogin(database).setVisible(true);
        logger.info("Se ha abierto la ventana de Login");
    }
    
    private void abrirRegistro() {
        
        new VentanaRegistro(this, database).setVisible(true);
        logger.info("Se ha abierto la ventana de Registro de Jugador");
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(boton, BorderLayout.CENTER);
        return panel;
    }

    private void configurarBotonSuperior(JButton boton) {
        boton.setBackground(new Color(255, 215, 0));
        boton.setForeground(Color.BLACK);
        boton.setFont(new Font("SansSerif", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void abrirBlackJack() {
         JOptionPane.showMessageDialog(this, "Funcionalidad de BlackJack aún no implementada.");
        // Aquí iría new VentanaBlackJack().setVisible(true);
    }

    private void abrirHighLow() {
         JOptionPane.showMessageDialog(this, "Funcionalidad de High-Low aún no implementada.");
        // Aquí iría new VentanaHighLow().setVisible(true);
    }

    private void abrirRuleta() {
         JOptionPane.showMessageDialog(this, "Funcionalidad de Ruleta aún no implementada.");
        // Aquí iría new VentanaRuleta().setVisible(true);
    }
}