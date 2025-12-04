package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class VentanaRuleta extends JFrame {
    private double saldo = 1000.0;

    private final JLabel lblTitulo = new JLabel("Ruleta", SwingConstants.CENTER);
    private final JLabel lblSaldo = new JLabel();
    private final JTextField txtApuesta = new JTextField("10", 8);
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Rojo/Negro","Par/Impar","Número"});
    private final JComboBox<String> cmbValor = new JComboBox<>(new String[]{"Rojo","Negro"});
    private final JTextField txtNumero = new JTextField("17", 4);
    private final JButton btnGirar = new JButton("Girar");
    private final JLabel lblSalida = new JLabel("Sale: -", SwingConstants.CENTER);
    private final JLabel lblResultado = new JLabel("Listo", SwingConstants.CENTER);

    private final Random rnd = new Random();

    public VentanaRuleta() {
        setTitle("Ruleta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(10,10,0,10));
        top.add(lblTitulo, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(3,1,8,8));
        centro.setBorder(new EmptyBorder(0,20,0,20));
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
        actualizarSaldo();
        derecha.add(lblSaldo);
        add(derecha, BorderLayout.EAST);

        cmbTipo.addActionListener(e -> actualizarInputs());
        btnGirar.addActionListener(e -> girar());

        actualizarInputs();
        setVisible(true);
    }

    private void actualizarSaldo() {
        lblSaldo.setText("Saldo: " + String.format("%.2f", saldo));
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
            if (a<=0 || a>saldo) return -1;
            return a;
        } catch(Exception e){ return -1; }
    }

    private boolean esRojo(int n){
        int[] rojos={1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36};
        for(int r: rojos) if (r==n) return true;
        return false;
    }

    private void girar() {
        double a = leerApuesta();
        if (a<=0) { JOptionPane.showMessageDialog(this, "Apuesta inválida"); return; }
        int salido = rnd.nextInt(37);
        lblSalida.setText("Sale: " + salido);
        String tipo = cmbTipo.getSelectedItem().toString();
        boolean acierto = false;
        double pago = 0;

        if (tipo.equals("Rojo/Negro")) {
            String v = cmbValor.getSelectedItem().toString();
            boolean rojo = esRojo(salido);
            acierto = (v.equals("Rojo") && rojo) || (v.equals("Negro") && !rojo && salido!=0);
            pago = acierto ? a : -a;
        } else if (tipo.equals("Par/Impar")) {
            String v = cmbValor.getSelectedItem().toString();
            acierto = (salido!=0) && ((salido%2==0 && v.equals("Par")) || (salido%2==1 && v.equals("Impar")));
            pago = acierto ? a : -a;
        } else {
            int elegido;
            try { elegido = Integer.parseInt(txtNumero.getText().trim()); }
            catch(Exception ex){ JOptionPane.showMessageDialog(this, "Número inválido"); return; }
            if (elegido<0 || elegido>36) { JOptionPane.showMessageDialog(this, "Número 0-36"); return; }
            acierto = (elegido == salido);
            pago = acierto ? a*35 : -a;
        }

        saldo += pago;
        actualizarSaldo();
        lblResultado.setText((acierto?"Acierto":"Fallo")+" ("+String.format("%+.2f", pago)+")");
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(VentanaRuleta::new); }
}
