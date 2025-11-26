package gui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class VentanaRegistro extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField txtNombre = new JTextField(15);
    private JTextField txtApellidos = new JTextField(15);
    private JTextField txtDNI = new JTextField(15);
    private JTextField txtUsuario = new JTextField(15);
    private JPasswordField txtContrasena = new JPasswordField(15);
    private JTextField txtFecha = new JTextField(15);
    private JComboBox<String> jcbPais = new JComboBox<>(new String[]{
    	    "España", "Italia", "Portugal", "México", "Argentina", 
    	    "Brasil", "Canadá", "Australia", "Suecia", "Noruega"
    	});

    	private JCheckBox chkPrivacidad = new JCheckBox("Confirmo que he leído y acepto las condiciones de privacidad");
    	private JLabel lblMayorEdad = new JLabel("⚠️ Solo disponible para mayores de 18 años");
    	private JButton btnRegistro = new JButton("Crear cuenta");


    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public VentanaRegistro() {
        setTitle("Registro");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 480); 
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(10, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setContentPane(content);

        JLabel titulo = new JLabel("Registro de usuario", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        content.add(titulo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        addRow(form, c, row++, new JLabel("País de residencia:"), jcbPais);
        addRow(form, c, row++, new JLabel("DNI:"), txtDNI);
        addRow(form, c, row++, new JLabel("Nombre:"), txtNombre);
        addRow(form, c, row++, new JLabel("Apellidos:"), txtApellidos);
        addRow(form, c, row++, new JLabel("Fecha de nacimiento (yyyy-MM-dd):"), txtFecha);
        addRow(form, c, row++, new JLabel("Usuario:"), txtUsuario);
        addRow(form, c, row++, new JLabel("Contraseña:"), txtContrasena);

        JPanel bottom = new JPanel(new GridLayout(0,1,6,6));
        lblMayorEdad.setForeground(new Color(170, 0, 0));
        lblMayorEdad.setVisible(false);
        bottom.add(chkPrivacidad);
        bottom.add(lblMayorEdad);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRegistro.setEnabled(false);
        acciones.add(btnRegistro);
        bottom.add(acciones);

        content.add(form, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        txtFecha.setToolTipText("Formato: yyyy-MM-dd (ej: 2000-05-21)");

        DocumentListener refrescar = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizarEstado(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarEstado(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarEstado(); }
        };
        txtNombre.getDocument().addDocumentListener(refrescar);
        txtApellidos.getDocument().addDocumentListener(refrescar);
        txtDNI.getDocument().addDocumentListener(refrescar);
        txtUsuario.getDocument().addDocumentListener(refrescar);
        txtContrasena.getDocument().addDocumentListener(refrescar);
        txtFecha.getDocument().addDocumentListener(refrescar);
        chkPrivacidad.addActionListener(e -> actualizarEstado());

        btnRegistro.addActionListener(e -> {
            if (!esMayorDeEdad()) {
                JOptionPane.showMessageDialog(this, "Debes ser mayor de 18 años.", "Edad insuficiente", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "Registro completado:\n" +
                    "- Nombre: " + txtNombre.getText() + " " + txtApellidos.getText() + "\n" +
                    "- DNI: " + txtDNI.getText() + "\n" +
                    "- Usuario: " + txtUsuario.getText() + "\n" +
                    "- País: " + jcbPais.getSelectedItem(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        setVisible(true);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, JComponent label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        panel.add(label, c);
        c.gridx = 1; c.gridy = row; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    private void actualizarEstado() {
        boolean completos = !txtNombre.getText().isEmpty()
                && !txtApellidos.getText().isEmpty()
                && !txtDNI.getText().isEmpty()
                && !txtUsuario.getText().isEmpty()
                && txtContrasena.getPassword().length > 0
                && !txtFecha.getText().isEmpty();

        boolean mayorEdad = esMayorDeEdad();
        lblMayorEdad.setVisible(!mayorEdad && !txtFecha.getText().isEmpty());

        boolean privacidadOk = chkPrivacidad.isSelected();

        btnRegistro.setEnabled(completos && mayorEdad && privacidadOk);
    }

    private boolean esMayorDeEdad() {
        try {
            LocalDate nacimiento = LocalDate.parse(txtFecha.getText().trim(), FMT);
            return Period.between(nacimiento, LocalDate.now()).getYears() >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaRegistro::new);
    }
}
