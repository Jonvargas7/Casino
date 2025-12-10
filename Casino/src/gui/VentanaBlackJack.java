package gui;

import io.Propiedades;
import domain.Jugador; 
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.DoubleConsumer; 

public class VentanaBlackJack extends JFrame {

    private Jugador jugador; 
    private double saldo; 
    private double apuesta = 0.0;

    
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

    private final JPanel panelCartasJugador = new JPanel(new FlowLayout());
    private final JPanel panelCartasBanca = new JPanel(new FlowLayout());

    
    private final List<Integer> baraja = new ArrayList<>();
    private final Random rnd = new Random();
    private final List<Integer> manoJ = new ArrayList<>();
    private final List<Integer> manoB = new ArrayList<>();

    private final Propiedades props = new Propiedades();

    
    public VentanaBlackJack(Jugador jugador) {
        this.jugador = jugador;
        this.saldo = jugador.getSaldo(); 

        props.cargar();

        setTitle("BlackJack - Jugador: " + jugador.getNombre()); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10, 10, 0, 10));
        top.add(lblTitulo, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        
        JPanel pCartas = new JPanel(new GridLayout(2, 1));
        pCartas.setBorder(BorderFactory.createTitledBorder("Mesa de Juego"));
        
        JPanel pBanca = new JPanel(new BorderLayout());
        pBanca.setBorder(BorderFactory.createTitledBorder("Banca"));
        pBanca.add(panelCartasBanca, BorderLayout.CENTER);
        pBanca.add(lblValorB, BorderLayout.SOUTH);

        JPanel pJugador = new JPanel(new BorderLayout());
        pJugador.setBorder(BorderFactory.createTitledBorder("Jugador"));
        pJugador.add(panelCartasJugador, BorderLayout.CENTER);
        pJugador.add(lblValorJ, BorderLayout.SOUTH);

        pCartas.add(pBanca);
        pCartas.add(pJugador);
        add(pCartas, BorderLayout.CENTER);

        
        JPanel pControles = new JPanel(new BorderLayout());
        pControles.setBorder(new EmptyBorder(0, 10, 10, 10));

        
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 16));
        pControles.add(lblEstado, BorderLayout.NORTH);
        
        
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        
        actualizarSaldoUI(); 
        
        panelInfo.add(new JLabel("Apuesta (€):"));
        panelInfo.add(txtApuesta);
        panelInfo.add(lblSaldo); 
        panelInfo.add(btnDepositar);

        
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panelAcciones.add(btnNueva);
        panelAcciones.add(btnPedir);
        panelAcciones.add(btnPlantarse);
        panelAcciones.add(btnDoblar);
        
        JPanel pBottom = new JPanel(new GridLayout(2, 1));
        pBottom.add(panelInfo);
        pBottom.add(panelAcciones);
        
        pControles.add(pBottom, BorderLayout.CENTER);
        add(pControles, BorderLayout.SOUTH);

        
        btnNueva.addActionListener(e -> nuevaMano());
        btnPedir.addActionListener(e -> pedir());
        btnPlantarse.addActionListener(e -> plantarse());
        btnDoblar.addActionListener(e -> doblar());
        btnDepositar.addActionListener(e -> depositar());

        
        habilitarBotonesJuego(false);
        btnNueva.setEnabled(true);
        
        setVisible(true);
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

        saldo -= apuesta; 
        actualizarSaldoUI();

        manoJ.clear();
        manoB.clear();
        iniciarBaraja();
        
       
        manoJ.add(robar());
        manoB.add(robar());
        manoJ.add(robar());
        manoB.add(robar());

        
        actualizarVista(false); 

        if (valor(manoJ) == 21) {
            lblEstado.setText("¡BLACKJACK!");
            plantarse(); 
        } else {
            lblEstado.setText("Tu turno. Pedir o Plantarse.");
            habilitarBotonesJuego(true);
            btnDoblar.setEnabled(apuesta * 2 <= saldo); 
        }
    }
    
    
    private void pedir() {
        manoJ.add(robar());
        actualizarVista(false);
        btnDoblar.setEnabled(false); 

        if (valor(manoJ) > 21) {
            lblEstado.setText("¡BUST! Te pasaste de 21.");
            plantarse(); 
        } else if (valor(manoJ) == 21) {
            lblEstado.setText("21. La banca juega.");
            plantarse();
        } else {
            lblEstado.setText("Pide otra carta o plántate.");
        }
    }
    
    
    private void plantarse() {
        habilitarBotonesJuego(false);
        bancaJuega();
        comprobarGanador();
        btnNueva.setEnabled(true);
    }

    
    private void doblar() {
        if (apuesta * 2 > saldo) {
            JOptionPane.showMessageDialog(this, "Saldo insuficiente para doblar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        saldo -= apuesta; 
        apuesta *= 2;
        actualizarSaldoUI();

        manoJ.add(robar());
        actualizarVista(false); 
        
        if (valor(manoJ) > 21) {
            lblEstado.setText("¡BUST! Te pasaste de 21 después de doblar.");
        } else {
            lblEstado.setText("Doblaste con éxito. La banca juega.");
        }
        
        plantarse();
    }
    
    /** Lógica de la banca. */
    private void bancaJuega() {
        
        actualizarVista(true); 

        
        while (valor(manoB) < 17) {
            manoB.add(robar());
            actualizarVista(true); 
            
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }

    
    private void comprobarGanador() {
        int valorJ = valor(manoJ);
        int valorB = valor(manoB);
        double ganancia = 0.0;

        String resultado = "";
        if (valorJ > 21) {
            resultado = "Gana la Banca (Jugador BUST).";
        } else if (valorB > 21) {
            resultado = "Gana el Jugador (Banca BUST).";
            ganancia = apuesta * 2;
        } else if (valorJ == valorB) {
            resultado = "Empate (PUSH). Se devuelve la apuesta.";
            ganancia = apuesta;
        } else if (valorJ > valorB) {
            resultado = "¡Gana el Jugador!";
            
            if (valorJ == 21 && manoJ.size() == 2) {
                 ganancia = apuesta + apuesta * 1.5; 
                 resultado += " (BLACKJACK - Pago 3:2)";
            } else {
                 ganancia = apuesta * 2; 
            }
        } else {
            resultado = "Gana la Banca.";
        }

        saldo += ganancia;
        actualizarSaldoUI();
        lblEstado.setText(resultado);
        
        
        jugador.setSaldo(saldo); 
    }
    
    
    private void depositar() {
        
        DoubleConsumer onSaldoActualizado = cantidad -> {
            saldo += cantidad;
            
            jugador.setSaldo(saldo); 
            actualizarSaldoUI();
            JOptionPane.showMessageDialog(this, String.format("Depósito de %.2f€ realizado.", cantidad), "Depósito Exitoso", JOptionPane.INFORMATION_MESSAGE);
        };
        
        
        new VentanaDeposito(saldo, onSaldoActualizado).setVisible(true);
        
    }
    
    
    private void iniciarBaraja() {
        baraja.clear();
        for (int i = 0; i < 4; i++) { 
            for (int v = 1; v <= 13; v++) { 
                baraja.add(v);
            }
        }
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
        
        while (ases > 0 && suma + 10 <= 21) { 
            suma += 10; 
            ases--; 
        }
        return suma;
    }

    
    private ImageIcon cargarCarta(int v) {
        String key;

        if (v == 1) key = "a";
        else if (v == 11) key = "j";
        else if (v == 12) key = "q";
        else if (v == 13) key = "k";
        else key = String.valueOf(v);

        
        String ruta = props.getProperty(key);
        if (ruta != null && !ruta.isEmpty()) {
             ImageIcon icon = new ImageIcon(ruta);
             
             Image img = icon.getImage().getScaledInstance(70, 100, Image.SCALE_SMOOTH);
             return new ImageIcon(img);
        }
        
        return new ImageIcon(); 
    }
    
    private ImageIcon cargarCartaOculta() {
        
        String ruta = props.getProperty("back"); 
        if (ruta != null && !ruta.isEmpty()) {
             ImageIcon icon = new ImageIcon(ruta);
             Image img = icon.getImage().getScaledInstance(70, 100, Image.SCALE_SMOOTH);
             return new ImageIcon(img);
        }
        return new ImageIcon();
    }

    
    private void actualizarVista(boolean mostrarBanca) {
        panelCartasJugador.removeAll();
        for (int c : manoJ) {
            panelCartasJugador.add(new JLabel(cargarCarta(c)));
        }

        panelCartasBanca.removeAll();
        if (manoB.isEmpty()) {
            lblValorB.setText("Valor: 0");
        } else {
            
            panelCartasBanca.add(new JLabel(cargarCarta(manoB.get(0))));
            
            if (mostrarBanca) {
                
                for (int i = 1; i < manoB.size(); i++) {
                    panelCartasBanca.add(new JLabel(cargarCarta(manoB.get(i))));
                }
                lblValorB.setText("Valor: " + valor(manoB));
            } else {
                
                if (manoB.size() > 1) {
                    panelCartasBanca.add(new JLabel(cargarCartaOculta()));
                }
                lblValorB.setText("Valor: " + valor(Arrays.asList(manoB.get(0))) + " + ?");
            }
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
        btnNueva.setEnabled(!habilitar);
        txtApuesta.setEditable(!habilitar);
        btnDepositar.setEnabled(!habilitar);
    }
}