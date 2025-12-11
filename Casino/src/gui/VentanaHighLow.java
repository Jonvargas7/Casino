package gui;

import io.Propiedades;
import domain.Jugador;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class VentanaHighLow extends JFrame {
    private double saldo = 1000.0;
    private final Propiedades props = new Propiedades();

    private final JLabel lblTitulo = new JLabel("High–Low", SwingConstants.CENTER);
    private final JLabel lblSaldo = new JLabel();
    private final JTextField txtApuesta = new JTextField("10", 8);
    private final JComboBox<String> cmbDecision = new JComboBox<>(new String[]{"Mayor","Menor"});
    private final JButton btnNueva = new JButton("Nueva ronda");
    private final JButton btnRevelar = new JButton("Revelar");

    private final JLabel lblCartaActual = new JLabel("Carta actual: -", SwingConstants.CENTER);
    private final JLabel lblCartaNueva = new JLabel("Carta nueva: -", SwingConstants.CENTER);
    private final JLabel lblResultado = new JLabel("Listo", SwingConstants.CENTER);

    private final List<Integer> baraja = new ArrayList<>();
    private final Random rnd = new Random();
    private int cartaActual = -1;

    private static final int CARD_W = 80;
    private static final int CARD_H = 120;
    private static final Color COLOR_GANANCIA = new Color(0, 150, 0);
    private static final Color COLOR_PERDIDA = Color.RED;

    public VentanaHighLow(Jugador jugador) {
        props.cargar();
        setTitle("High–Low");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10,10,0,10));
        top.add(lblTitulo, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        JPanel panelCentro = new JPanel(new GridLayout(3,1,8,8));
        panelCentro.setBorder(new EmptyBorder(0,20,0,20));
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

    private void nuevaRonda() {
        if (baraja.size()<2) prepararBaraja();
        cartaActual = robar();
        setCartaLabel(lblCartaActual, cartaActual, false);
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
        setCartaLabel(lblCartaNueva, nueva, false);
        String dec = (String) cmbDecision.getSelectedItem();
        boolean mayor = nueva>cartaActual;
        boolean menor = nueva<cartaActual;
        boolean empate = nueva==cartaActual;
        boolean acierto = (dec.equals("Mayor") && mayor) || (dec.equals("Menor") && menor);
        double delta = empate ? 0 : (acierto ? a : -a);
        saldo += delta;
        actualizarSaldo();
        Color colorResultado = (delta >= 0) ? COLOR_GANANCIA : COLOR_PERDIDA;
        lblResultado.setForeground(colorResultado);
        lblResultado.setText((empate?"Empate":(acierto?"Acierto":"Fallo"))+" ("+String.format("%+.2f", delta)+")");
        cartaActual = nueva;
        setCartaLabel(lblCartaActual, cartaActual, false);
        btnNueva.setEnabled(true);
        btnRevelar.setEnabled(false);
    }

    public int contarValorRestante(int valorBuscado) {
        return contarValorRecursivo(baraja, valorBuscado, 0);
    }

    private int contarValorRecursivo(List<Integer> lista, int valorBuscado, int indice) {
        if (indice >= lista.size()) return 0;
        int cuentaActual = (lista.get(indice) == valorBuscado) ? 1 : 0;
        return cuentaActual + contarValorRecursivo(lista, valorBuscado, indice + 1);
    }

    private void setCartaLabel(JLabel label, int valor, boolean oculto) {
        label.setPreferredSize(new Dimension(CARD_W, CARD_H));
        if (oculto) {
            ImageIcon img = cargarImagenReverso();
            if (img != null) {
                label.setIcon(img);
                label.setText("");
            } else {
                label.setIcon(null);
                label.setText("Carta Siguiente");
            }
            return;
        }
        ImageIcon img = cargarCarta(valor);
        if (img != null) {
            label.setIcon(img);
            label.setText(cartaTxt(valor));
        } else {
            label.setIcon(null);
            label.setText((label==lblCartaActual ? "Carta actual: " : "Carta nueva: ") + cartaTxt(valor));
        }
    }

    private ImageIcon cargarImagenReverso() {
        String ruta = props.getProperty("back");
        if (ruta == null ) ruta = props.getProperty("blackJack");
        ImageIcon scaled = getScaledIcon(ruta);
        return scaled;
    }

    private ImageIcon cargarCarta(int valor) {
        String clave;
        if (valor==1) clave="a";
        else if (valor==11) clave="j";
        else if (valor==12) clave="q";
        else if (valor==13) clave="k";
        else clave = String.valueOf(valor);
        String ruta = props.getProperty(clave);
        if (ruta != null) {
            ImageIcon scaled = getScaledIcon(ruta);
            if (scaled != null) return scaled;
        }
        String rutaAlt = tryCommonPaths(clave);
        return getScaledIcon(rutaAlt);
    }

    private String tryCommonPaths(String clave) {
        String base = "resources/imagenes/";
        List<String> candidates = new ArrayList<>();
        candidates.add(base + clave + ".png");
        candidates.add(base + clave.toUpperCase() + ".png");
        if (clave.length()==1) {
            candidates.add(base + clave.toLowerCase() + ".png");
        }
        for (String p : candidates) {
            File f = new File(p);
            if (f.exists()) return p;
            URL res = getClass().getResource("/" + p);
            if (res != null) return p;
        }
        return base + clave + ".png";
    }

    private ImageIcon getScaledIcon(String path) {
        if (path == null ) return null;
        try {
            URL res = getClass().getResource(path.startsWith("/") ? path : "/" + path);
            BufferedImage bi = null;
            if (res != null) bi = ImageIO.read(res);
            else {
                File f = new File(path);
                if (f.exists()) bi = ImageIO.read(f);
            }
            if (bi == null) return null;
            double ratio = Math.min((double) CARD_W / bi.getWidth(), (double) CARD_H / bi.getHeight());
            int w = Math.max(1, (int) Math.round(bi.getWidth() * ratio));
            int h = Math.max(1, (int) Math.round(bi.getHeight() * ratio));
            Image scaled = bi.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            BufferedImage out = new BufferedImage(CARD_W, CARD_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(new Color(0,0,0,0));
            g.fillRect(0,0,CARD_W,CARD_H);
            int x = (CARD_W - w) / 2;
            int y = (CARD_H - h) / 2;
            g.drawImage(scaled, x, y, null);
            g.dispose();
            return new ImageIcon(out);
        } catch (IOException e) {
            return null;
        }
    }

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
