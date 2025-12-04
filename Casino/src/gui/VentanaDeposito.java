package gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.DoubleConsumer;

public class VentanaDeposito extends JFrame {
    private final JTextField txtCantidad = new JTextField(10);
    private final JButton btnDepositar = new JButton("Depositar");
    private final JButton btnCancelar = new JButton("Cancelar");

    public VentanaDeposito(double saldoActual, DoubleConsumer onSaldoActualizado) {
        setTitle("Depósito");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(320, 160);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0;c.gridy=0; p.add(new JLabel("Saldo actual"), c);
        c.gridx=1;c.gridy=0; p.add(new JLabel(String.format("%.2f", saldoActual)), c);
        c.gridx=0;c.gridy=1; p.add(new JLabel("Cantidad"), c);
        c.gridx=1;c.gridy=1; p.add(txtCantidad, c);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.add(btnCancelar);
        acciones.add(btnDepositar);
        c.gridx=0;c.gridy=2;c.gridwidth=2; p.add(acciones,c);

        setContentPane(p);

        btnCancelar.addActionListener(e -> dispose());
        btnDepositar.addActionListener(e -> {
            try {
                double x = Double.parseDouble(txtCantidad.getText().trim());
                if (x <= 0) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida");
                    return;
                }
                onSaldoActualizado.accept(saldoActual + x);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cantidad inválida");
            }
        });

        setVisible(true);
    }
}
