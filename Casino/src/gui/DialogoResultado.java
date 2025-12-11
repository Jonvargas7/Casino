package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DialogoResultado extends JDialog {

    
    public DialogoResultado(JFrame parent, String mensaje, Color color, ActionListener callback) {
        super(parent, "Resultado de la Ronda", true); 
        
      
        JLabel lblMensaje = new JLabel(mensaje, SwingConstants.CENTER);
        
        lblMensaje.setFont(new Font("Arial", Font.BOLD, 22)); 
        lblMensaje.setForeground(color);
        lblMensaje.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnCerrar = new JButton("Continuar");
        btnCerrar.setFont(new Font("Arial", Font.PLAIN, 16));
        
    
        setLayout(new BorderLayout());
        add(lblMensaje, BorderLayout.CENTER);
        
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlSur.add(btnCerrar);
        add(pnlSur, BorderLayout.SOUTH);
        
        
        btnCerrar.addActionListener(e -> {
            dispose();
            if (callback != null) {
                callback.actionPerformed(e);
            }
        });

       
        setSize(600, 200); 
        setLocationRelativeTo(parent);
    }
}