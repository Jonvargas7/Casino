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
import java.util.function.DoubleConsumer;

public class VentanaBlackJack extends JFrame {

    private Jugador jugador;
    private double saldo;
    private double apuesta = 0.0;
    private Propiedades props;

    private static final int CARD_W = 100;
    private static final int CARD_H = 140;

    private final JLabel lblTitulo = new JLabel("BlackJack", SwingConstants.CENTER);
    private final JLabel lblSaldo = new JLabel();
    private final JTextField txtApuesta = new JTextField("10", 8);
    private final JButton btnDepositar = new JButton("Depositar");
    private final JButton btnNueva = new JButton("Nueva mano");
    private final JButton btnPedir = new JButton("Pedir");
    private final JButton btnPlantarse = new JButton("Plantarse");
    private final JButton btnDoblar = new JButton("Doblar");

    private final JLabel lblValorJ = new JLabel("Valor: 0", SwingConstants.CENTER);
    private final JLabel lblValorB = new JLabel("Valor: 0", SwingConstants.CENTER);
    private final JLabel lblEstado = new JLabel("Listo", SwingConstants.CENTER);

    private final JPanel panelCartasJugador = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private final JPanel panelCartasBanca = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

    private static final Color COLOR_GANANCIA = new Color(0, 150, 0);
    private static final Color COLOR_PERDIDA = Color.RED;
    private static final Color COLOR_EMPATE = Color.BLUE;

    private final List<Integer> baraja = new ArrayList<>();
    private final List<Integer> manoJ = new ArrayList<>();
    private final List<Integer> manoB = new ArrayList<>();
    private final int MAX_VALOR_CARTA = 13;
    private final int NUM_PALOS = 4;
    private final Random random = new Random();

    public VentanaBlackJack(Jugador j) {
        this.jugador = j;
        this.saldo = j.getSaldo();
        this.props = new Propiedades();
        props.cargar();
        System.out.println("=== DEBUG IMAGENES CARTAS ===");
        System.out.println("Working dir: " + System.getProperty("user.dir"));
        String[] claves = {"a","2","3","4","5","6","7","8","9","10","j","q","k","back"};
        for (String c : claves) {
            String ruta = props.getProperty(c);
            System.out.println(c + " -> " + ruta);
            if (ruta != null ) {
                java.io.File f = new java.io.File(ruta);
                System.out.println("   File absolute: " + f.getAbsolutePath());
                System.out.println("   file.exists(): " + f.exists());
                java.net.URL r = getClass().getResource(ruta.startsWith("/") ? ruta : "/" + ruta);
                System.out.println("   getResource -> " + (r==null? "NULL": r.toString()));
            } else {
                System.out.println("   propiedad ausente");
            }
        }
        System.out.println("================================");

        if (props.getProperty("back") == null) {
            props.setProperty("back", props.getProperty("blackJack"));
        }
        initComponents();
        actualizarSaldoUI();
        lblEstado.setText("Listo para jugar. ¡Introduce tu apuesta!");
        habilitarBotonesJuego(false);
        btnNueva.setEnabled(true);
        btnDepositar.setEnabled(true);
    }

    private void initComponents() {
        setTitle("BlackJack - " + jugador.getNombre());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel pnlNorte = new JPanel(new BorderLayout());
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        pnlNorte.add(lblTitulo, BorderLayout.NORTH);
        lblValorB.setHorizontalAlignment(SwingConstants.CENTER);
        pnlNorte.add(lblValorB, BorderLayout.SOUTH);
        JPanel pnlCartas = new JPanel(new GridLayout(2, 1, 0, 10));
        pnlCartas.add(panelCartasBanca);
        pnlCartas.add(panelCartasJugador);
        JPanel pnlSur = new JPanel(new BorderLayout(0, 10));
        JPanel pnlControlesWrapper = new JPanel(new BorderLayout(0, 5));
        lblValorJ.setHorizontalAlignment(SwingConstants.CENTER);
        pnlControlesWrapper.add(lblValorJ, BorderLayout.NORTH);
        JPanel pnlControles = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlControles.add(btnPedir);
        pnlControles.add(btnPlantarse);
        pnlControles.add(btnDoblar);
        pnlControlesWrapper.add(pnlControles, BorderLayout.CENTER);
        JPanel pnlInfo = new JPanel(new GridLayout(2, 3, 10, 5));
        pnlInfo.add(lblSaldo);
        pnlInfo.add(new JLabel("Apuesta:"));
        pnlInfo.add(txtApuesta);
        pnlInfo.add(btnDepositar);
        pnlInfo.add(btnNueva);
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        pnlInfo.add(lblEstado);
        pnlSur.add(pnlControlesWrapper, BorderLayout.NORTH);
        pnlSur.add(pnlInfo, BorderLayout.CENTER);
        add(pnlNorte, BorderLayout.NORTH);
        add(pnlCartas, BorderLayout.CENTER);
        add(pnlSur, BorderLayout.SOUTH);
        btnDepositar.addActionListener(e -> depositar());
        btnNueva.addActionListener(e -> nuevaMano());
        btnPedir.addActionListener(e -> pedirCarta());
        btnPlantarse.addActionListener(e -> plantarse());
        btnDoblar.addActionListener(e -> doblar());
        pack();
        setSize(800, 650);
        setLocationRelativeTo(null);
    }

    private void nuevaMano() {
        try {
            apuesta = Double.parseDouble(txtApuesta.getText().trim());
            if (apuesta <= 0) {
                JOptionPane.showMessageDialog(this, "La apuesta debe ser mayor que 0.", "Error de Apuesta", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (apuesta > saldo) {
                JOptionPane.showMessageDialog(this, "No tienes suficiente saldo para esta apuesta.", "Error de Saldo", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Apuesta inválida. Introduce un número.", "Error de Apuesta", JOptionPane.ERROR_MESSAGE);
            return;
        }
        habilitarBotonesJuego(false);
        btnNueva.setEnabled(false);
        btnDepositar.setEnabled(false);
        lblEstado.setText("Barajando y repartiendo...");
        new Thread(() -> {
            try {
                Thread.sleep(600);
                saldo -= apuesta;
                manoJ.clear();
                manoB.clear();
                iniciarBaraja();
                manoJ.add(robar());
                manoB.add(robar());
                manoJ.add(robar());
                manoB.add(robar());
                SwingUtilities.invokeLater(() -> {
                    actualizarSaldoUI();
                    actualizarVista(false);
                    if (valor(manoJ) == 21) {
                        lblEstado.setText("¡BLACKJACK!");
                        comprobarGanador();
                    } else {
                        lblEstado.setText("Tu turno. Pedir o Plantarse.");
                        habilitarBotonesJuego(true);
                        btnDoblar.setEnabled(apuesta * 2 <= saldo);
                        btnDepositar.setEnabled(true);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void pedirCarta() {
        if (valor(manoJ) < 21) {
            manoJ.add(robar());
            actualizarVista(false);
            btnDoblar.setEnabled(false);
            if (valor(manoJ) > 21) {
                lblEstado.setText("¡Te has pasado!");
                comprobarGanador();
            }
        }
    }

    private void plantarse() {
        habilitarBotonesJuego(false);
        lblEstado.setText("Turno de la Banca...");
        bancaJuega();
    }

    private void doblar() {
        if (apuesta * 2 <= saldo) {
            saldo -= apuesta;
            apuesta *= 2;
            actualizarSaldoUI();
            manoJ.add(robar());
            actualizarVista(false);
            habilitarBotonesJuego(false);
            if (valor(manoJ) > 21) {
                lblEstado.setText("Te has pasado al doblar.");
                comprobarGanador();
            } else {
                lblEstado.setText("Te plantas con una carta extra.");
                bancaJuega();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Saldo insuficiente para doblar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bancaJuega() {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> actualizarVista(true));
            while (valor(manoB) < 17) {
                try {
                    Thread.sleep(700);
                    manoB.add(robar());
                    SwingUtilities.invokeLater(() -> actualizarVista(true));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            SwingUtilities.invokeLater(this::comprobarGanador);
        }).start();
    }

    private void comprobarGanador() {
        int vJ = valor(manoJ);
        int vB = valor(manoB);
        double premio = 0;
        String mensaje;
        Color color;
        if (vJ > 21) {
            mensaje = "¡TE HAS PASADO! GANÓ LA BANCA";
            color = COLOR_PERDIDA;
        } else if (vB > 21) {
            premio = apuesta * 2;
            mensaje = "¡GANASTE! BANCA SE PASÓ (+" + String.format("%.2f €", apuesta) + ")";
            color = COLOR_GANANCIA;
        } else if (vJ > vB) {
            premio = apuesta * 2;
            mensaje = "¡GANASTE! (+" + String.format("%.2f €", apuesta) + ")";
            color = COLOR_GANANCIA;
        } else if (vB > vJ) {
            mensaje = "GANÓ LA BANCA";
            color = COLOR_PERDIDA;
        } else {
            premio = apuesta;
            mensaje = "EMPATE. Se devuelve la apuesta.";
            color = COLOR_EMPATE;
        }
        saldo += premio;
        jugador.setSaldo(saldo);
        actualizarSaldoUI();
        manoJ.clear();
        manoB.clear();
        apuesta = 0;
        mostrarResultadoDialogo(mensaje, color);
        lblEstado.setText("Ronda finalizada.");
    }

    private void depositar() {
        DoubleConsumer onSaldoActualizado = cantidad -> {
            habilitarBotonesJuego(false);
            btnNueva.setEnabled(false);
            btnDepositar.setEnabled(false);
            lblEstado.setText("Procesando confirmación de depósito...");
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    saldo += cantidad;
                    jugador.setSaldo(saldo);
                    SwingUtilities.invokeLater(() -> {
                        actualizarSaldoUI();
                        lblEstado.setText("Depósito OK.");
                        JOptionPane.showMessageDialog(this, String.format("Depósito de %.2f€ realizado.", cantidad), "Depósito Exitoso", JOptionPane.INFORMATION_MESSAGE);
                        if (manoJ.isEmpty()) {
                            btnNueva.setEnabled(true);
                        } else {
                            habilitarBotonesJuego(true);
                        }
                        btnDepositar.setEnabled(true);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        };
        String entrada = JOptionPane.showInputDialog(this, "Cantidad a depositar:");
        try {
            double deposito = Double.parseDouble(entrada);
            if (deposito > 0) onSaldoActualizado.accept(deposito);
        } catch (Exception ignored) {}
    }

    private void iniciarBaraja() {
        baraja.clear();
        for (int v = 1; v <= MAX_VALOR_CARTA; v++) {
            for (int p = 0; p < NUM_PALOS; p++) {
                baraja.add(v);
            }
        }
        Collections.shuffle(baraja, random);
    }

    private int robar() {
        if (baraja.isEmpty()) iniciarBaraja();
        return baraja.remove(0);
    }

    private int valor(List<Integer> mano) {
        int suma = 0;
        int numAses = 0;
        for (int carta : mano) {
            int v = carta;
            if (v == 1) {
                numAses++;
                suma += 11;
            } else if (v >= 10) {
                suma += 10;
            } else {
                suma += v;
            }
        }
        while (suma > 21 && numAses > 0) {
            suma -= 10;
            numAses--;
        }
        return suma;
    }

    private void actualizarVista(boolean mostrarBancaCompleta) {
        panelCartasJugador.removeAll();
        panelCartasBanca.removeAll();
        for (int carta : manoJ) {
            JLabel l = new JLabel(cargarCarta(carta));
            l.setPreferredSize(new Dimension(CARD_W, CARD_H));
            panelCartasJugador.add(l);
        }
        if (manoB.size() > 0) {
            JLabel primera = new JLabel(cargarCarta(manoB.get(0)));
            primera.setPreferredSize(new Dimension(CARD_W, CARD_H));
            panelCartasBanca.add(primera);
            if (mostrarBancaCompleta) {
                for (int i = 1; i < manoB.size(); i++) {
                    JLabel l = new JLabel(cargarCarta(manoB.get(i)));
                    l.setPreferredSize(new Dimension(CARD_W, CARD_H));
                    panelCartasBanca.add(l);
                }
                lblValorB.setText("Valor: " + valor(manoB));
            } else {
                if (manoB.size() > 1) {
                    JLabel oculto = new JLabel(cargarCartaOculta());
                    oculto.setPreferredSize(new Dimension(CARD_W, CARD_H));
                    panelCartasBanca.add(oculto);
                }
                lblValorB.setText("Valor: " + valor(Arrays.asList(manoB.get(0))) + " + ?");
            }
        } else {
            lblValorB.setText("Valor: 0");
        }
        lblValorJ.setText("Valor: " + valor(manoJ));
        panelCartasJugador.revalidate();
        panelCartasJugador.repaint();
        panelCartasBanca.revalidate();
        panelCartasBanca.repaint();
        this.revalidate();
        this.repaint();
    }

    private void actualizarSaldoUI() {
        lblSaldo.setText("Saldo: " + String.format("%.2f €", saldo));
        if (saldo <= 0) {
            habilitarBotonesJuego(false);
            btnNueva.setEnabled(false);
            lblEstado.setText("Sin saldo. Por favor, deposita.");
        }
    }

    private void habilitarBotonesJuego(boolean habilitar) {
        btnPedir.setEnabled(habilitar);
        btnPlantarse.setEnabled(habilitar);
        btnDoblar.setEnabled(habilitar);
    }

    private ImageIcon cargarCarta(int v) {
        String key;
        if (v >= 2 && v <= 10) key = String.valueOf(v);
        else if (v == 1) key = "a";
        else if (v == 11) key = "j";
        else if (v == 12) key = "q";
        else if (v == 13) key = "k";
        else return placeholderIcon("?");
        String path = props.getProperty(key);
        if (path == null ) return placeholderIcon(key);
        ImageIcon scaled = getScaledIcon(path);
        if (scaled == null) return placeholderIcon(key);
        return scaled;
    }

    private ImageIcon cargarCartaOculta() {
        String p = props.getProperty("back");
        if (p == null ) return placeholderIcon("X");
        ImageIcon scaled = getScaledIcon(p);
        if (scaled == null) return placeholderIcon("X");
        return scaled;
    }

    private ImageIcon getScaledIcon(String path) {
        try {
            URL res = getClass().getResource(path.startsWith("/") ? path : "/" + path);
            BufferedImage bi = null;
            if (res != null) {
                bi = ImageIO.read(res);
            } else {
                File f = new File(path);
                if (f.exists()) bi = ImageIO.read(f);
            }
            if (bi == null) return null;
            double ratio = Math.min((double) CARD_W / bi.getWidth(), (double) CARD_H / bi.getHeight());
            int w = (int) Math.round(bi.getWidth() * ratio);
            int h = (int) Math.round(bi.getHeight() * ratio);
            Image scaled = bi.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            BufferedImage out = new BufferedImage(CARD_W, CARD_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
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

    private ImageIcon placeholderIcon(String text) {
        BufferedImage bi = new BufferedImage(CARD_W, CARD_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(new Color(230,230,230));
        g.fillRect(0,0,CARD_W,CARD_H);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0,0,CARD_W-1,CARD_H-1);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int tx = (CARD_W - fm.stringWidth(text)) / 2;
        int ty = (CARD_H - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
        g.dispose();
        return new ImageIcon(bi);
    }

    private void mostrarResultadoDialogo(String mensaje, Color color) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, mensaje, "Resultado de la Ronda", JOptionPane.INFORMATION_MESSAGE);
            btnNueva.setEnabled(true);
            btnDepositar.setEnabled(true);
        });
    }
}
