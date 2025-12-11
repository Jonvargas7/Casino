package gui;

import io.Propiedades;
import domain.Jugador; 
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.DoubleConsumer; 
import java.io.File; 
import java.awt.event.ActionListener;

public class VentanaBlackJack extends JFrame {

    private Jugador jugador; 
    private double saldo; 
    private double apuesta = 0.0;
    
    private Propiedades props; 

    // Componentes de la UI
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

    // Colores para el diálogo
    private static final Color COLOR_GANANCIA = new Color(0, 150, 0); 
    private static final Color COLOR_PERDIDA = Color.RED;
    private static final Color COLOR_EMPATE = Color.BLUE;
    
    // Lógica del juego
    private final List<Integer> baraja = new ArrayList<>();
    private final List<Integer> manoJ = new ArrayList<>();
    private final List<Integer> manoB = new ArrayList<>();
    private final int MAX_VALOR_CARTA = 13;
    private final int NUM_PALOS = 4;
    private final int MAX_CARTAS_BARAJA = 52;
    

    // Constructor
    public VentanaBlackJack(Jugador j) {
        this.jugador = j;
        this.saldo = j.getSaldo();
        this.props = new Propiedades();
        props.cargar();
        
        initComponents();
        actualizarSaldoUI();
        lblEstado.setText("Listo para jugar. ¡Introduce tu apuesta!");
        
        habilitarBotonesJuego(false);
        btnNueva.setEnabled(true);
        btnDepositar.setEnabled(true);
    }
    
    private void initComponents() {
        setTitle("BlackJack - " + jugador.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Usamos un BorderLayout principal con un poco de margen
        setLayout(new BorderLayout(10, 10)); 
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // ----------------------------------------------------
        // 1. Panel Norte: Título y valor de la Banca
        // ----------------------------------------------------
        JPanel pnlNorte = new JPanel(new BorderLayout());
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        pnlNorte.add(lblTitulo, BorderLayout.NORTH);
        pnlNorte.add(lblValorB, BorderLayout.SOUTH);
        
        // ----------------------------------------------------
        // 2. Panel Central: Cartas (mejor distribución con GridLayout)
        // ----------------------------------------------------
        JPanel pnlCartas = new JPanel(new GridLayout(2, 1, 0, 10)); // 2 filas, 1 columna, 10px vert. gap
        pnlCartas.add(panelCartasBanca);
        pnlCartas.add(panelCartasJugador);

        // ----------------------------------------------------
        // 3. Panel Sur: Controles e Información
        // ----------------------------------------------------
        JPanel pnlSur = new JPanel(new BorderLayout(0, 10)); // Vertical gap de 10px
        
        // Sub-Panel Controles (VALOR, Pedir, Plantarse, Doblar)
        // Usamos un BorderLayout para poner el valor del jugador arriba de los botones
        JPanel pnlControlesWrapper = new JPanel(new BorderLayout(0, 5));
        
        // 💥 MODIFICACIÓN: Mover lblValorJ arriba de los controles 💥
        pnlControlesWrapper.add(lblValorJ, BorderLayout.NORTH); 
        
        JPanel pnlControles = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlControles.add(btnPedir);
        pnlControles.add(btnPlantarse);
        pnlControles.add(btnDoblar);
        
        pnlControlesWrapper.add(pnlControles, BorderLayout.CENTER);
        
        // Sub-Panel Información (Saldo, Apuesta, Estado)
        JPanel pnlInfo = new JPanel(new GridLayout(2, 3, 10, 5));
        pnlInfo.add(lblSaldo);
        pnlInfo.add(new JLabel("Apuesta:"));
        pnlInfo.add(txtApuesta);
        pnlInfo.add(btnDepositar);
        pnlInfo.add(btnNueva);
        pnlInfo.add(lblEstado);
        
        pnlSur.add(pnlControlesWrapper, BorderLayout.NORTH); // Añadir el wrapper
        pnlSur.add(pnlInfo, BorderLayout.CENTER); // La información de saldo va debajo de los controles

        // ----------------------------------------------------
        // 4. Agregar al Frame
        // ----------------------------------------------------
        add(pnlNorte, BorderLayout.NORTH);
        add(pnlCartas, BorderLayout.CENTER);
        add(pnlSur, BorderLayout.SOUTH);

        // Listeners
        btnDepositar.addActionListener(e -> depositar());
        btnNueva.addActionListener(e -> nuevaMano());
        btnPedir.addActionListener(e -> pedirCarta());
        btnPlantarse.addActionListener(e -> plantarse());
        btnDoblar.addActionListener(e -> doblar());
        
        pack();
        setSize(800, 650); 
        setLocationRelativeTo(null);
    }
    
    // ----------------------------------------------------
    // 📢 Método para mostrar el diálogo modal
    // ----------------------------------------------------
    private void mostrarResultadoDialogo(String mensaje, Color color) {
        SwingUtilities.invokeLater(() -> {
            ActionListener callback = e -> {
                btnNueva.setEnabled(true);
                btnDepositar.setEnabled(true);
            };
            
            DialogoResultado dialogo = new DialogoResultado(this, mensaje, color, callback);
            dialogo.setVisible(true); 
        });
    }

    // ====================================================================
    // LÓGICA DE JUEGO (Resto de métodos sin cambio funcional)
    // ====================================================================

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
                Thread.sleep(1500); 

                saldo -= apuesta; 
                manoJ.clear(); manoB.clear(); iniciarBaraja();
                manoJ.add(robar()); manoB.add(robar());
                manoJ.add(robar()); manoB.add(robar());

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
    
    private void depositar() {
        
        DoubleConsumer onSaldoActualizado = cantidad -> {
            
            habilitarBotonesJuego(false);
            btnNueva.setEnabled(false); 
            btnDepositar.setEnabled(false);
            lblEstado.setText("Procesando confirmación de depósito...");
            
            new Thread(() -> {
                try {
                    Thread.sleep(2000); 

                    saldo += cantidad;
                    jugador.setSaldo(saldo); 

                    SwingUtilities.invokeLater(() -> {
                        actualizarSaldoUI(); 
                        lblEstado.setText("Depósito OK.");
                        
                        DialogoResultado dialogo = new DialogoResultado(this, 
                            "✅ Depósito de " + String.format("%.2f € confirmado.", cantidad), 
                            COLOR_GANANCIA, null);
                        dialogo.setVisible(true);

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
        
        new VentanaDeposito(saldo, onSaldoActualizado).setVisible(true);
    }
    
    private void bancaJuega() {
        
        new Thread(() -> {
            
            SwingUtilities.invokeLater(() -> actualizarVista(true));
            
            while (valor(manoB) < 17) {
                try { 
                    Thread.sleep(750); 
                    manoB.add(robar());
                    SwingUtilities.invokeLater(() -> actualizarVista(true)); 

                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return; 
                }
            }
            
            SwingUtilities.invokeLater(() -> {
                comprobarGanador(); 
            });
            
        }).start();
    }
    
    private void plantarse() {
        habilitarBotonesJuego(false);
        lblEstado.setText("Turno de la Banca...");
        bancaJuega(); 
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
            mensaje = "¡GANASTE! BANCA SE PASÓ (+" + String.format("%.2f €", premio) + ")";
            color = COLOR_GANANCIA;
        } else if (vJ > vB) {
            premio = apuesta * 2;
            mensaje = "¡GANASTE! (+" + String.format("%.2f €", premio) + ")";
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
    
    private void doblar() {
        if (apuesta * 2 <= saldo) {
            saldo -= apuesta;
            apuesta *= 2;
            actualizarSaldoUI();
            
            manoJ.add(robar());
            actualizarVista(false);
            
            if (valor(manoJ) > 21) {
                lblEstado.setText("Te has pasado al doblar.");
                comprobarGanador(); 
            } else {
                lblEstado.setText("Te plantas con una carta extra.");
            }
            plantarse(); 
        } else {
            JOptionPane.showMessageDialog(this, "Saldo insuficiente para doblar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void iniciarBaraja() {
        baraja.clear();
        for (int v = 1; v <= MAX_VALOR_CARTA; v++) {
            for (int p = 0; p < NUM_PALOS; p++) {
                baraja.add(v);
            }
        }
        Collections.shuffle(baraja);
    }
    
    private int robar() {
        if (baraja.isEmpty()) {
            iniciarBaraja();
        }
        return baraja.remove(0);
    }
    
    private int valor(List<Integer> mano) {
        int suma = 0;
        int numAses = 0;
        
        for (int carta : mano) {
            int v = carta;
            if (v == 1) { numAses++; suma += 11;
            } else if (v >= 10) { suma += 10;
            } else { suma += v; }
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
            panelCartasJugador.add(new JLabel(cargarCarta(carta)));
        }

        if (manoB.size() > 0) {
            panelCartasBanca.add(new JLabel(cargarCarta(manoB.get(0))));
            if (mostrarBancaCompleta) {
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
    }
    
    private ImageIcon cargarCarta(int v) {
        String key;
        if (v == 1) key = "a";
        else if (v == 11) key = "j";
        else if (v == 12) key = "q";
        else if (v == 13) key = "k";
        else key = String.valueOf(v);
        
        return getScaledIcon(props.getProperty(key));
    }
    
    private ImageIcon cargarCartaOculta() {
        return getScaledIcon(props.getProperty("back")); 
    }
    
    private ImageIcon getScaledIcon(String path) {
        if (path == null || !new File(path).exists()) {
             return new ImageIcon(); 
        }
        
        ImageIcon originalIcon = new ImageIcon(path);
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(100, 140, Image.SCALE_SMOOTH); 
        return new ImageIcon(scaledImage);
    }
}