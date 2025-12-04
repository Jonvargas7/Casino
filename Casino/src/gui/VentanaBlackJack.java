package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VentanaBlackJack extends JFrame {
    private double saldo = 1000.0;
    private double apuesta = 0.0;

    private final JLabel lblTitulo = new JLabel("BlackJack", SwingConstants.CENTER);
    private final JLabel lblSaldo = new JLabel();
    private final JTextField txtApuesta = new JTextField("10", 8);
    private final JButton btnDepositar = new JButton("Depositar");
    private final JButton btnNueva = new JButton("Nueva mano");
    private final JButton btnPedir = new JButton("Pedir");
    private final JButton btnPlantarse = new JButton("Plantarse");
    private final JButton btnDoblar = new JButton("Doblar");
    private final JLabel lblJugador = new JLabel("Jugador: ", SwingConstants.CENTER);
    private final JLabel lblBanca = new JLabel("Banca: ", SwingConstants.CENTER);
    private final JLabel lblValorJ = new JLabel("Valor: 0", SwingConstants.CENTER);
    private final JLabel lblValorB = new JLabel("Valor: 0", SwingConstants.CENTER);
    private final JLabel lblEstado = new JLabel("Listo", SwingConstants.CENTER);

    private final List<Integer> baraja = new ArrayList<>();
    private final Random rnd = new Random();
    private final java.util.List<Integer> manoJ = new ArrayList<>();
    private final java.util.List<Integer> manoB = new ArrayList<>();

    public VentanaBlackJack() {
        setTitle("BlackJack");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10,10,0,10));
        top.add(lblTitulo, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        lblSaldo.setText("Saldo: " + String.format("%.2f", saldo));
        panelInfo.add(new JLabel("Apuesta"));
        panelInfo.add(txtApuesta);
        panelInfo.add(btnDepositar);
        panelInfo.add(lblSaldo);
        add(panelInfo, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new GridLayout(2,1,10,10));
        JPanel pJ = new JPanel(new BorderLayout());
        pJ.add(lblJugador, BorderLayout.CENTER);
        pJ.add(lblValorJ, BorderLayout.SOUTH);
        JPanel pB = new JPanel(new BorderLayout());
        pB.add(lblBanca, BorderLayout.CENTER);
        pB.add(lblValorB, BorderLayout.SOUTH);
        centro.add(pJ);
        centro.add(pB);
        add(centro, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        acciones.add(btnNueva);
        acciones.add(btnPedir);
        acciones.add(btnPlantarse);
        acciones.add(btnDoblar);
        add(acciones, BorderLayout.WEST);

        add(lblEstado, BorderLayout.EAST);

        setBotonesEnJuego(false);
        btnNueva.setEnabled(true);

        btnNueva.addActionListener(e -> nuevaMano());
        btnPedir.addActionListener(e -> pedir());
        btnPlantarse.addActionListener(e -> plantarse());
        btnDoblar.addActionListener(e -> doblar());
        btnDepositar.addActionListener(e -> abrirDeposito());

        setVisible(true);
    }

    private void abrirDeposito() {
        new VentanaDeposito(saldo, ns -> {
            saldo = ns;
            lblSaldo.setText("Saldo: " + String.format("%.2f", saldo));
        });
    }

    private void nuevaMano() {
        try {
            apuesta = Double.parseDouble(txtApuesta.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Apuesta inválida");
            return;
        }
        if (apuesta <= 0 || apuesta > saldo) {
            JOptionPane.showMessageDialog(this, "Apuesta fuera de rango");
            return;
        }
        prepararBaraja();
        manoJ.clear();
        manoB.clear();
        manoJ.add(robar()); manoB.add(robar()); manoJ.add(robar()); manoB.add(robar());
        actualizarVista();
        lblEstado.setText("Tu turno");
        setBotonesEnJuego(true);
        btnNueva.setEnabled(false);
        if (valor(manoJ) == 21) resolverFinal();
    }

    private void pedir() {
        manoJ.add(robar());
        actualizarVista();
        if (valor(manoJ) > 21) resolverFinal();
    }

    private void plantarse() {
        turnoBanca();
        actualizarVista();
        resolverFinal();
    }

    private void doblar() {
        if (manoJ.size() != 2) {
            JOptionPane.showMessageDialog(this, "Solo con dos cartas");
            return;
        }
        if (apuesta * 2 > saldo) {
            JOptionPane.showMessageDialog(this, "Saldo insuficiente");
            return;
        }
        apuesta *= 2;
        manoJ.add(robar());
        actualizarVista();
        if (valor(manoJ) <= 21) {
            turnoBanca();
            actualizarVista();
        }
        resolverFinal();
    }

    private void resolverFinal() {
        int vj = valor(manoJ);
        int vb = valor(manoB);
        double delta;
        String msg;
        if (vj > 21) { delta = -apuesta; msg = "Te pasaste"; }
        else if (vb > 21) { delta = apuesta; msg = "La banca se pasa"; }
        else if (vj > vb) { delta = apuesta; msg = "Ganas"; }
        else if (vj < vb) { delta = -apuesta; msg = "Pierdes"; }
        else { delta = 0; msg = "Empate"; }
        saldo += delta;
        lblSaldo.setText("Saldo: " + String.format("%.2f", saldo));
        lblEstado.setText(msg + " (" + String.format("%+.2f", delta) + ")");
        setBotonesEnJuego(false);
        btnNueva.setEnabled(true);
    }

    private void setBotonesEnJuego(boolean jugando) {
        btnPedir.setEnabled(jugando);
        btnPlantarse.setEnabled(jugando);
        btnDoblar.setEnabled(jugando);
    }

    private void turnoBanca() {
        while (valor(manoB) < 17) manoB.add(robar());
    }

    private void prepararBaraja() {
        baraja.clear();
        for (int v = 1; v <= 13; v++) for (int s = 0; s < 4; s++) baraja.add(v);
        Collections.shuffle(baraja, rnd);
    }

    private int robar() {
        return baraja.remove(baraja.size() - 1);
    }

    private int valor(List<Integer> mano) {
        int suma = 0, ases = 0;
        for (int v : mano) {
            int val = Math.min(v, 10);
            if (v == 1) ases++;
            suma += val;
        }
        while (ases > 0 && suma + 10 <= 21) { suma += 10; ases--; }
        return suma;
    }

    private String textoMano(List<Integer> mano) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mano.size(); i++) {
            sb.append(cartaStr(mano.get(i)));
            if (i < mano.size() - 1) sb.append("  ");
        }
        return sb.toString();
    }

    private String cartaStr(int v) {
        if (v == 1) return "A";
        if (v == 11) return "J";
        if (v == 12) return "Q";
        if (v == 13) return "K";
        return Integer.toString(v);
    }

    private void actualizarVista() {
        lblJugador.setText("Jugador: " + textoMano(manoJ));
        lblBanca.setText("Banca: " + textoMano(manoB));
        lblValorJ.setText("Valor: " + valor(manoJ));
        lblValorB.setText("Valor: " + valor(manoB));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaBlackJack::new);
    }
}
