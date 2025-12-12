package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import domain.Jugador;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;



public class VentanaRuleta extends JFrame {

    private static final long serialVersionUID = 1L;
    
    
    private double saldo = 1000.0; 

    private final JLabel lblTitulo = new JLabel("Ruleta", SwingConstants.CENTER);
    private final JLabel lblSaldo = new JLabel();
    private final JTextField txtApuesta = new JTextField("10", 8);
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Rojo/Negro", "Par/Impar", "Número"});
    private final JComboBox<String> cmbValor = new JComboBox<>(new String[]{"Rojo", "Negro"});
    private final JTextField txtNumero = new JTextField("17", 4);
    private final JButton btnGirar = new JButton("Girar");
    
   
    private final JLabel lblSalida = new JLabel("Sale: -", SwingConstants.CENTER);
    private final JLabel lblResultado = new JLabel("Listo para apostar", SwingConstants.CENTER); 

    private final Random rnd = new Random();
    
   
    private static final Color COLOR_GANANCIA = new Color(0, 150, 0); 
    private static final Color COLOR_PERDIDA = Color.RED;
    private static final Color COLOR_CERO = new Color(0, 100, 0); // 

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
        
        
        JPanel centro = new JPanel(new GridLayout(2, 1, 8, 8));
        centro.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        lblSalida.setFont(new Font("SansSerif", Font.BOLD, 32)); 
        lblSalida.setForeground(Color.DARK_GRAY);
        
        lblResultado.setFont(new Font("SansSerif", Font.BOLD, 18)); 
        lblResultado.setForeground(Color.BLUE);
        
        centro.add(lblSalida);
        centro.add(lblResultado);
        add(centro, BorderLayout.CENTER);

       
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

       
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        lblSaldo.setFont(new Font("SansSerif", Font.BOLD, 14));
        derecha.add(new JLabel("SALDO:"));
        derecha.add(lblSaldo);
        derecha.setBorder(new EmptyBorder(10, 0, 0, 10));
        add(derecha, BorderLayout.EAST);

      
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
    
    
    private String toHtmlColor(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

   
    private void girar() {
        double a = leerApuesta();
        if (a <= 0) { JOptionPane.showMessageDialog(this, "Apuesta inválida o saldo insuficiente", "Error de Apuesta", JOptionPane.ERROR_MESSAGE); return; }
        
       
        btnGirar.setEnabled(false);
        txtApuesta.setEnabled(false);
        lblResultado.setText("GIRANDO...");
        lblResultado.setForeground(Color.ORANGE.darker());
        lblSalida.setText("Sale: ?");
        lblSalida.setForeground(Color.DARK_GRAY);

       
        Timer timer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop(); 
                ejecutarLogicaGiro(a); 
                btnGirar.setEnabled(true);
                txtApuesta.setEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
   
    private void ejecutarLogicaGiro(double a) {
        int salido = rnd.nextInt(37); 
        
        
        Color colorSalida = COLOR_CERO;
        if (salido != 0) {
            colorSalida = esRojo(salido) ? Color.RED : Color.BLACK;
        }

       
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
            } else { 
                int elegido = Integer.parseInt(txtNumero.getText().trim());
                if (elegido < 0 || elegido > 36) throw new IllegalArgumentException("Número fuera de rango 0-36");
                acierto = (elegido == salido);
                pago = acierto ? a * 35 : -a; 
            }
        } catch (Exception ex) {
            lblResultado.setText("ERROR: Configuración de apuesta inválida.");
            lblResultado.setForeground(COLOR_PERDIDA);
            pago = -a; 
        }

      
        saldo += pago;

       
        String textoSalida = "<html>SALE: <span style='color: " + (colorSalida == Color.BLACK ? "white" : "black") + 
                             "; background-color: " + toHtmlColor(colorSalida) + 
                             "; border: 1px solid gray; padding: 4px; border-radius: 4px;'><b>" + salido + "</b></span></html>";
        
        lblSalida.setText(textoSalida);
        
        
        Color colorResultado = (pago >= 0) ? COLOR_GANANCIA : COLOR_PERDIDA;
        lblResultado.setForeground(colorResultado);
        lblResultado.setText((pago >= 0 ? "¡ACIERTO!" : "FALLO") + " (" + String.format("%+.2f €", pago) + ")");
        
        actualizarSaldo();
        
       
    }

   
}