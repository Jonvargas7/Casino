package gui;

import io.Propiedades;
import domain.Jugador; 
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.DoubleConsumer; 
import java.io.File; // Necesario para cargar las imágenes de Propiedades

public class VentanaBlackJack extends JFrame {

    private Jugador jugador; 
    private double saldo; 
    private double apuesta = 0.0;
    
    // Propiedades para la carga de imágenes
    private Propiedades props; 

    // Componentes de la UI (final si se inicializan en la declaración o constructor)
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
        
        // Habilitar botones iniciales
        habilitarBotonesJuego(false);
        btnNueva.setEnabled(true);
        btnDepositar.setEnabled(true);
    }
    
    private void initComponents() {
        setTitle("BlackJack - " + jugador.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Configuración de paneles
        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.add(lblTitulo, BorderLayout.NORTH);
        pnlNorte.add(lblValorB, BorderLayout.SOUTH);
        pnlNorte.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel pnlCentro = new JPanel(new GridLayout(2, 1));
        pnlCentro.add(panelCartasBanca);
        pnlCentro.add(panelCartasJugador);

        JPanel pnlSur = new JPanel(new BorderLayout());
        
        JPanel pnlControles = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlControles.add(btnPedir);
        pnlControles.add(btnPlantarse);
        pnlControles.add(btnDoblar);

        JPanel pnlInfo = new JPanel(new GridLayout(2, 3, 10, 5));
        pnlInfo.add(lblSaldo);
        pnlInfo.add(new JLabel("Apuesta:"));
        pnlInfo.add(txtApuesta);
        pnlInfo.add(btnDepositar);
        pnlInfo.add(btnNueva);
        pnlInfo.add(lblEstado);
        
        pnlSur.add(pnlControles, BorderLayout.NORTH);
        pnlSur.add(pnlInfo, BorderLayout.CENTER);
        pnlSur.add(lblValorJ, BorderLayout.SOUTH);
        pnlSur.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        add(pnlNorte, BorderLayout.NORTH);
        add(pnlCentro, BorderLayout.CENTER);
        add(pnlSur, BorderLayout.SOUTH);

        // Listeners
        btnDepositar.addActionListener(e -> depositar());
        btnNueva.addActionListener(e -> nuevaMano());
        btnPedir.addActionListener(e -> pedirCarta());
        btnPlantarse.addActionListener(e -> plantarse());
        btnDoblar.addActionListener(e -> doblar());
        
        pack();
        setSize(500, 650);
        setLocationRelativeTo(null);
    }
    
    
    // ====================================================================
    // 📢 IMPLEMENTACIÓN DE HILOS (THREADS) EN ACCIONES LENTAS
    // ====================================================================

    /**
     * Hilo para la acción de Nueva Mano. Simula una operación de barajado o reparto que toma tiempo.
     */
    private void nuevaMano() {
        // *** Lógica de validación (sigue en el EDT para feedback inmediato) ***
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

        // 1. Deshabilitar UI antes de empezar el trabajo concurrente (EDT)
        habilitarBotonesJuego(false);
        btnNueva.setEnabled(false);
        btnDepositar.setEnabled(false);
        lblEstado.setText("Barajando y repartiendo...");
        
        // 2. Crear y ejecutar el hilo para el reparto (NEW -> RUNNABLE)
        new Thread(() -> {
            try {
                // Simulación de una operación larga (TIMED_WAITING)
                Thread.sleep(1500); 

                // 3. Lógica del juego (en el Hilo Secundario)
                saldo -= apuesta; 
                
                manoJ.clear();
                manoB.clear();
                iniciarBaraja();
                
                manoJ.add(robar());
                manoB.add(robar());
                manoJ.add(robar());
                manoB.add(robar());

                // 4. Actualizar la interfaz (DEBE HACERSE EN EL EDT)
                SwingUtilities.invokeLater(() -> {
                    actualizarSaldoUI(); 
                    actualizarVista(false); 

                    if (valor(manoJ) == 21) {
                        lblEstado.setText("¡BLACKJACK!");
                        plantarse(); // plantarse() llamará a la lógica de la banca (también en hilo)
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
    
    /**
     * Hilo para la acción de Depositar. Simula la confirmación de la transacción.
     */
    private void depositar() {
        
        DoubleConsumer onSaldoActualizado = cantidad -> {
            
            // 1. Deshabilitar UI para simular espera de confirmación (EDT)
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
                        actualizarSaldoUI(); // UI update
                        lblEstado.setText("✅ Depósito de " + String.format("%.2f € confirmado.", cantidad));
                        
                        
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
        
        // Abre la ventana de depósito
        new VentanaDeposito(saldo, onSaldoActualizado).setVisible(true);
    }
    
    /**
     * Hilo para la lógica de la Banca. Simula la pausa entre cartas robadas.
     */
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
            
            // 3. Al finalizar el turno de la banca, ejecutar la comprobación de ganador (EDT)
            SwingUtilities.invokeLater(() -> {
                comprobarGanador(); // Comprueba y actualiza el estado final
                btnNueva.setEnabled(true);
                btnDepositar.setEnabled(true);
            });
            
        }).start();
    }
    
    /** Llama al juego de la banca en un hilo. */
    private void plantarse() {
        habilitarBotonesJuego(false);
        lblEstado.setText("Turno de la Banca...");
        bancaJuega(); // Inicia el hilo de juego de la Banca
    }


    
    private void pedirCarta() {
        if (valor(manoJ) < 21) {
            manoJ.add(robar());
            actualizarVista(false);
            btnDoblar.setEnabled(false); // No se puede doblar después de pedir
            if (valor(manoJ) > 21) {
                lblEstado.setText("¡Te has pasado!");
                plantarse(); // Llama a plantarse, que llama a bancaJuega en un hilo
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
            } else {
                lblEstado.setText("Te plantas con una carta.");
            }
            plantarse(); // Llama a plantarse, que llama a bancaJuega en un hilo
        } else {
            JOptionPane.showMessageDialog(this, "Saldo insuficiente para doblar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void comprobarGanador() {
        int vJ = valor(manoJ);
        int vB = valor(manoB);
        double premio = 0;
        
        if (vJ > 21) {
            lblEstado.setText("Ganó la Banca (Te pasaste)");
        } else if (vB > 21) {
            premio = apuesta * 2;
            lblEstado.setText("¡Ganaste! (Banca se pasó) +" + String.format("%.2f€", premio));
        } else if (vJ > vB) {
            premio = apuesta * 2;
            lblEstado.setText("¡Ganaste! +" + String.format("%.2f€", premio));
        } else if (vB > vJ) {
            lblEstado.setText("Ganó la Banca");
        } else {
            premio = apuesta;
            lblEstado.setText("Empate. Se devuelve la apuesta.");
        }
        
        saldo += premio;
        jugador.setSaldo(saldo);
        actualizarSaldoUI();
        manoJ.clear(); // Limpiar manos para el siguiente juego
        manoB.clear();
        apuesta = 0;
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
            if (v == 1) { // As
                numAses++;
                suma += 11;
            } else if (v >= 10) { // Figuras (10, J, Q, K)
                suma += 10;
            } else {
                suma += v;
            }
        }
        
        // Ajustar Ases si la suma es mayor a 21
        while (suma > 21 && numAses > 0) {
            suma -= 10;
            numAses--;
        }
        return suma;
    }

    private void actualizarVista(boolean mostrarBancaCompleta) {
        // Limpiar paneles
        panelCartasJugador.removeAll();
        panelCartasBanca.removeAll();

        // Cartas del Jugador
        for (int carta : manoJ) {
            panelCartasJugador.add(new JLabel(cargarCarta(carta)));
        }

        // Cartas de la Banca
        if (manoB.size() > 0) {
            // Primera carta de la Banca (siempre visible)
            panelCartasBanca.add(new JLabel(cargarCarta(manoB.get(0))));

            if (mostrarBancaCompleta) {
                // Si mostramos la banca completa (al plantarse)
                for (int i = 1; i < manoB.size(); i++) {
                    panelCartasBanca.add(new JLabel(cargarCarta(manoB.get(i))));
                }
                lblValorB.setText("Valor: " + valor(manoB));
            } else {
                // Carta oculta (al inicio)
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
        btnDoblar.setEnabled(habilitar); // La lógica de saldo lo ajustará
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
        Image scaledImage = originalImage.getScaledInstance(70, 100, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
    
    
}