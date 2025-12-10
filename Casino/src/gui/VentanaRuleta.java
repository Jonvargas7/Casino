package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import domain.Jugador;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

// Importaciones comentadas (para que el código sea copy-pasteable sin errores de DB)
// import domain.Jugador;
// import gestor.Database;

public class VentanaRuleta extends JFrame {

    private static final long serialVersionUID = 1L;
    
    // --- ATRIBUTOS DE JUEGO Y ESTADO ---
    private double saldo = 1000.0; // Usado para prueba. En producción, vendría del Jugador.

    private final JLabel lblTitulo = new JLabel("Ruleta", SwingConstants.CENTER);
    private final JLabel lblSaldo = new JLabel();
    private final JTextField txtApuesta = new JTextField("10", 8);
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Rojo/Negro", "Par/Impar", "Número"});
    private final JComboBox<String> cmbValor = new JComboBox<>(new String[]{"Rojo", "Negro"});
    private final JTextField txtNumero = new JTextField("17", 4);
    private final JButton btnGirar = new JButton("Girar");
    
    // Mejoras Visuales: Más grande y con color
    private final JLabel lblSalida = new JLabel("Sale: -", SwingConstants.CENTER);
    private final JLabel lblResultado = new JLabel("Listo para apostar", SwingConstants.CENTER); 

    private final Random rnd = new Random();
    
    // Colores de Ruleta
    private static final Color COLOR_GANANCIA = new Color(0, 150, 0); 
    private static final Color COLOR_PERDIDA = Color.RED;
    private static final Color COLOR_CERO = new Color(0, 100, 0); // Verde oscuro para el cero

    public VentanaRuleta(Jugador jugador) {
        setTitle("Ruleta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10, 10, 0, 10));
        top.add(lblTitulo, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        
        // --- PANEL CENTRAL: Resultados (Mejorado) ---
        JPanel centro = new JPanel(new GridLayout(2, 1, 8, 8));
        centro.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        lblSalida.setFont(new Font("SansSerif", Font.BOLD, 32)); // Más grande para el resultado
        lblSalida.setForeground(Color.DARK_GRAY);
        
        lblResultado.setFont(new Font("SansSerif", Font.BOLD, 18)); // Más grande para el mensaje
        lblResultado.setForeground(Color.BLUE);
        
        centro.add(lblSalida);
        centro.add(lblResultado);
        add(centro, BorderLayout.CENTER);

        // --- PANEL SUR: Controles de Apuesta ---
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controles.add(new JLabel("Apuesta"));
        controles.add(txtApuesta);
        controles.add(new JLabel("Tipo"));
        controles.add(cmbTipo);
        controles.add(new JLabel("Valor"));
        controles.add(cmbValor);
        controles.add(txtNumero);
        txtNumero.setVisible(false);
        controles.add(btnGirar);
        add(controles, BorderLayout.SOUTH);

        // --- PANEL DERECHA: Saldo (Mejorado visualmente) ---
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        lblSaldo.setFont(new Font("SansSerif", Font.BOLD, 14));
        derecha.add(new JLabel("SALDO:"));
        derecha.add(lblSaldo);
        derecha.setBorder(new EmptyBorder(10, 0, 0, 10));
        add(derecha, BorderLayout.EAST);

        // --- LISTENERS ---
        cmbTipo.addActionListener(e -> actualizarInputs());
        btnGirar.addActionListener(e -> girar());

        actualizarInputs();
        actualizarSaldo();
        setVisible(true);
    }

    private void actualizarSaldo() {
        lblSaldo.setText(String.format("%.2f €", saldo));
    }

    private void actualizarInputs() {
        String t = cmbTipo.getSelectedItem().toString();
        boolean numero = t.equals("Número");
        cmbValor.setVisible(!numero);
        txtNumero.setVisible(numero);
        if (!numero) {
            cmbValor.removeAllItems();
            if (t.equals("Rojo/Negro")) { cmbValor.addItem("Rojo"); cmbValor.addItem("Negro"); }
            if (t.equals("Par/Impar")) { cmbValor.addItem("Par"); cmbValor.addItem("Impar"); }
        }
        revalidate();
        repaint();
    }

    private double leerApuesta() {
        try {
            double a = Double.parseDouble(txtApuesta.getText().trim());
            if (a <= 0 || a > saldo) return -1;
            return a;
        } catch (Exception e) { return -1; }
    }

    private boolean esRojo(int n) {
        int[] rojos = {1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36};
        for (int r : rojos) if (r == n) return true;
        return false;
    }
    
    // Método auxiliar para obtener el código hexadecimal de un Color de Java (para HTML)
    private String toHtmlColor(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // --- LÓGICA DE GIRO CON RETARDO SIMULADO ---
    private void girar() {
        double a = leerApuesta();
        if (a <= 0) { JOptionPane.showMessageDialog(this, "Apuesta inválida o saldo insuficiente", "Error de Apuesta", JOptionPane.ERROR_MESSAGE); return; }
        
        // 1. Bloquear la GUI e indicar giro
        btnGirar.setEnabled(false);
        txtApuesta.setEnabled(false);
        lblResultado.setText("GIRANDO...");
        lblResultado.setForeground(Color.ORANGE.darker());
        lblSalida.setText("Sale: ?");
        lblSalida.setForeground(Color.DARK_GRAY);

        // 2. Usar un Timer para simular el tiempo de giro (1.5 segundos)
        Timer timer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop(); // Detener el timer
                ejecutarLogicaGiro(a); // Ejecutar la lógica real después del retardo
                btnGirar.setEnabled(true);
                txtApuesta.setEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    // --- LÓGICA CENTRAL DE CÁLCULO DE RESULTADO ---
    private void ejecutarLogicaGiro(double a) {
        int salido = rnd.nextInt(37); // Número de 0 a 36
        
        // 1. Determinar Color (Para la Visualización)
        Color colorSalida = COLOR_CERO; // 0 (Verde)
        if (salido != 0) {
            colorSalida = esRojo(salido) ? Color.RED : Color.BLACK;
        }

        // 2. Cálculo del acierto y pago
        boolean acierto = false;
        double pago = 0;
        String tipo = cmbTipo.getSelectedItem().toString();
        
        try {
            if (tipo.equals("Rojo/Negro")) {
                String v = cmbValor.getSelectedItem().toString();
                boolean rojo = esRojo(salido);
                acierto = (v.equals("Rojo") && rojo && salido != 0) || (v.equals("Negro") && !rojo && salido != 0);
                pago = acierto ? a : -a;
            } else if (tipo.equals("Par/Impar")) {
                String v = cmbValor.getSelectedItem().toString();
                acierto = (salido != 0) && ((salido % 2 == 0 && v.equals("Par")) || (salido % 2 == 1 && v.equals("Impar")));
                pago = acierto ? a : -a;
            } else { // Número (0-36)
                int elegido = Integer.parseInt(txtNumero.getText().trim());
                if (elegido < 0 || elegido > 36) throw new IllegalArgumentException("Número fuera de rango 0-36");
                acierto = (elegido == salido);
                pago = acierto ? a * 35 : -a; // Pago 35:1
            }
        } catch (Exception ex) {
            lblResultado.setText("ERROR: Configuración de apuesta inválida.");
            lblResultado.setForeground(COLOR_PERDIDA);
            pago = -a; // Si hay error de parsing o rango, la apuesta se pierde.
            // Si hay un error, ajustamos el pago para que el saldo refleje la apuesta fallida.
        }

        // 3. Actualizar Saldo
        saldo += pago;

        // 4. Actualizar la Vista (Salida y Resultado)
        
        // Visualización del número que sale (con fondo y color)
        String textoSalida = "<html>SALE: <span style='color: " + (colorSalida == Color.BLACK ? "white" : "black") + 
                             "; background-color: " + toHtmlColor(colorSalida) + 
                             "; border: 1px solid gray; padding: 4px; border-radius: 4px;'><b>" + salido + "</b></span></html>";
        
        lblSalida.setText(textoSalida);
        
        // Visualización del mensaje de Ganancia/Pérdida
        Color colorResultado = (pago >= 0) ? COLOR_GANANCIA : COLOR_PERDIDA;
        lblResultado.setForeground(colorResultado);
        lblResultado.setText((pago >= 0 ? "¡ACIERTO!" : "FALLO") + " (" + String.format("%+.2f €", pago) + ")");
        
        actualizarSaldo();
        
        // Si usáramos DB, aquí iría la persistencia del resultado y saldo.
    }

   
}