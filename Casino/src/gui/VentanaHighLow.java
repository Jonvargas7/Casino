package gui;

import io.Propiedades;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import domain.Jugador;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VentanaHighLow extends JFrame {
    private double saldo = 1000.0;
    private final Propiedades props = new Propiedades();

    private final JLabel lblTitulo = new JLabel("High–Low", SwingConstants.CENTER);
    private final JLabel lblSaldo = new JLabel();
    private final JTextField txtApuesta = new JTextField("10", 8);
    private final JComboBox<String> cmbDecision = new JComboBox<>(new String[]{"Mayor","Menor"});
    private final JButton btnNueva = new JButton("Nueva ronda");
    private final JButton btnRevelar = new JButton("Revelar");

    // Mantener las etiquetas originales, pero las usaremos para contener la imagen y el texto
    private final JLabel lblCartaActual = new JLabel("Carta actual: -", SwingConstants.CENTER);
    private final JLabel lblCartaNueva = new JLabel("Carta nueva: -", SwingConstants.CENTER);
    private final JLabel lblResultado = new JLabel("Listo", SwingConstants.CENTER);

    private final List<Integer> baraja = new ArrayList<>();
    private final Random rnd = new Random();
    private int cartaActual = -1;

    private static final int CARD_W = 80;
    private static final int CARD_H = 120;
    
    // Colores para el resultado
    private static final Color COLOR_GANANCIA = new Color(0, 150, 0); 
    private static final Color COLOR_PERDIDA = Color.RED;

    public VentanaHighLow(Jugador jugador) {
        props.cargar();

        setTitle("High–Low");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26)); // Fuente más grande
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10,10,0,10));
        top.add(lblTitulo, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(3,1,8,8));
        panelCentro.setBorder(new EmptyBorder(0,20,0,20));
        
        // Configuración de las etiquetas para mostrar texto bajo la imagen
        lblCartaActual.setVerticalTextPosition(SwingConstants.BOTTOM);
        lblCartaActual.setHorizontalTextPosition(SwingConstants.CENTER);
        lblCartaNueva.setVerticalTextPosition(SwingConstants.BOTTOM);
        lblCartaNueva.setHorizontalTextPosition(SwingConstants.CENTER);
        
        lblResultado.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        panelCentro.add(lblCartaActual);
        panelCentro.add(lblCartaNueva);
        panelCentro.add(lblResultado);
        add(panelCentro, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        acciones.add(new JLabel("Apuesta"));
        acciones.add(txtApuesta);
        acciones.add(cmbDecision);
        acciones.add(btnNueva);
        acciones.add(btnRevelar);
        add(acciones, BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        actualizarSaldo();
        derecha.add(lblSaldo);
        add(derecha, BorderLayout.EAST);

        btnNueva.addActionListener(e -> nuevaRonda());
        btnRevelar.addActionListener(e -> revelar());

        prepararBaraja();
        nuevaRonda();
        setVisible(true);
    }
    
    // --- MÉTODOS DE LÓGICA DE JUEGO ---

    private void nuevaRonda() {
        if (baraja.size()<2) prepararBaraja();
        cartaActual = robar();
        
        // 🛠️ Muestra la carta actual como imagen
        setCartaLabel(lblCartaActual, cartaActual, false);
        
        // 🛠️ Muestra la carta nueva oculta
        setCartaLabel(lblCartaNueva, 0, true);
        
        lblResultado.setText("Elige y revela");
        lblResultado.setForeground(Color.BLUE);
        btnNueva.setEnabled(false);
        btnRevelar.setEnabled(true);
    }

    private void revelar() {
        double a = leerApuesta();
        if (a<=0) { JOptionPane.showMessageDialog(this, "Apuesta inválida"); return; }
        
        int nueva = robar();
        
        // 🛠️ Revela la nueva carta como imagen
        setCartaLabel(lblCartaNueva, nueva, false);
        
        String dec = (String) cmbDecision.getSelectedItem();
        boolean mayor = nueva>cartaActual;
        boolean menor = nueva<cartaActual;
        boolean empate = nueva==cartaActual;
        boolean acierto = (dec.equals("Mayor") && mayor) || (dec.equals("Menor") && menor);
        double delta = empate ? 0 : (acierto ? a : -a);
        
        saldo += delta;
        actualizarSaldo();
        
        // 🛠️ Muestra el resultado con colores
        Color colorResultado = (delta >= 0) ? COLOR_GANANCIA : COLOR_PERDIDA;
        lblResultado.setForeground(colorResultado);
        lblResultado.setText((empate?"Empate":(acierto?"Acierto":"Fallo"))+" ("+String.format("%+.2f", delta)+")");
        
        cartaActual = nueva;
        
        // Actualizar la carta actual (se queda en la mesa)
        setCartaLabel(lblCartaActual, cartaActual, false); 
        
        btnNueva.setEnabled(true);
        btnRevelar.setEnabled(false);
    }

   
    public int contarValorRestante(int valorBuscado) {
        return contarValorRecursivo(baraja, valorBuscado, 0);
    }

    
    private int contarValorRecursivo(List<Integer> lista, int valorBuscado, int indice) {
        
        if (indice >= lista.size()) {
            return 0;
        }

        
        int cuentaActual = (lista.get(indice) == valorBuscado) ? 1 : 0;

        
        return cuentaActual + contarValorRecursivo(lista, valorBuscado, indice + 1);
    }

    

    private void setCartaLabel(JLabel label, int valor, boolean oculto) {
        if (oculto) {
            ImageIcon img = cargarImagenReverso();
            label.setIcon(img);
            label.setText("Carta Siguiente");
            return;
        }
        
        ImageIcon img = cargarCarta(valor);
        if (img != null) {
            label.setIcon(img);
            label.setText(cartaTxt(valor)); // Muestra el valor de texto debajo de la imagen
        } else {
            label.setIcon(null);
            label.setText((label==lblCartaActual ? "Carta actual: " : "Carta nueva: ") + cartaTxt(valor));
        }
    }
    
    private ImageIcon cargarImagenReverso() {
        // Usamos la clave 'blackJack' de las propiedades como reverso
        String ruta = props.getProperty("blackJack");
        
        // Intentar cargar como Classpath (más robusto en JAR)
        try {
            String path = ruta.startsWith("/") ? ruta : "/" + ruta;
            URL res = getClass().getResource(path);
            if (res != null) {
                ImageIcon raw = new ImageIcon(res);
                if (raw.getIconWidth() > 0) {
                    Image scaled = raw.getImage().getScaledInstance(CARD_W, CARD_H, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (Exception ignore) {}
        
        return null; // Fallback
    }
    
    private ImageIcon cargarCarta(int valor) {
        String clave;
        if (valor==1) clave="a";
        else if (valor==11) clave="j";
        else if (valor==12) clave="q";
        else if (valor==13) clave="k";
        else clave = String.valueOf(valor);

        String ruta = props.getProperty(clave);
        
        // 1. Intentar como archivo en disco (Tu método original para Eclipse)
        try {
            File f = new File(ruta);
            if (f.exists()) {
                ImageIcon raw = new ImageIcon(ruta);
                if (raw.getIconWidth() > 0) {
                    Image scaled = raw.getImage().getScaledInstance(CARD_W, CARD_H, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (Exception ignore) {}

        // 2. Intentar como recurso en classpath (Para JAR y robustez)
        try {
            String path = ruta.startsWith("/") ? ruta : "/" + ruta;
            URL res = getClass().getResource(path);
            if (res != null) {
                ImageIcon raw = new ImageIcon(res);
                if (raw.getIconWidth() > 0) {
                    Image scaled = raw.getImage().getScaledInstance(CARD_W, CARD_H, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (Exception ignore) {}

        return null;
    }

    // --- MÉTODOS DE LÓGICA DE BARAJA ---

    private void prepararBaraja() {
        baraja.clear();
        for (int v=1; v<=13; v++) for (int s=0; s<4; s++) baraja.add(v);
        Collections.shuffle(baraja, rnd);
    }

    private int robar() {
        if (baraja.isEmpty()) prepararBaraja();
        return baraja.remove(baraja.size()-1);
    }

    private String cartaTxt(int v) {
        if (v==1) return "A";
        if (v==11) return "J";
        if (v==12) return "Q";
        if (v==13) return "K";
        return Integer.toString(v);
    }

    private void actualizarSaldo() {
        lblSaldo.setText("Saldo: " + String.format("%.2f", saldo));
    }

    private double leerApuesta() {
        try {
            double a = Double.parseDouble(txtApuesta.getText().trim());
            if (a<=0 || a>saldo) return -1;
            return a;
        } catch(Exception e){ return -1; }
    }
    
    private void setControles(boolean activo) {
        btnNueva.setEnabled(activo);
        txtApuesta.setEnabled(activo);
        cmbDecision.setEnabled(activo);
    }

}