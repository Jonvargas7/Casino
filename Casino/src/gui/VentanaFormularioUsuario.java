package gui;

import domain.*;
import gestor.Database;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class VentanaFormularioUsuario extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private Database database;
    private Usuario usuarioLogeado;
    private Usuario usuarioAEditar; 
    private String rolPermitidoParaCrear; 
    
    private VentanaGestionUsuarios ventanaGestionPadre; 
    
    private JTextField txtNombre = new JTextField(20);
    private JTextField txtEmail = new JTextField(20);
    private JPasswordField txtPassword = new JPasswordField(20);
    private JComboBox<String> comboRol; 
    
    private JPanel panelEspecifico = new JPanel(new GridBagLayout()); 
    
    private JTextField txtSaldo = new JTextField(20);
    private JTextField txtPartidas = new JTextField(20);
    private JTextField txtTotalGanado = new JTextField(20);
    private JTextField txtNivel = new JTextField(20);
   
    private JTextField txtPuesto = new JTextField(20);
    private JTextField txtFechaInicio = new JTextField(20); 
    private JCheckBox chkActivo = new JCheckBox("Activo");

    private JButton btnGuardar = new JButton("Guardar");

    public VentanaFormularioUsuario(JFrame parentFrame, VentanaGestionUsuarios ventanaGestionPadre,
                                    Database database, Usuario usuarioLogeado, 
                                    Usuario usuarioAEditar, String rolPermitidoParaCrear) {
        super(parentFrame, (usuarioAEditar == null ? "Añadir Nuevo Usuario" : "Editar Usuario: " + usuarioAEditar.getNombre()), true);
        this.ventanaGestionPadre = ventanaGestionPadre;
        this.database = database;
        this.usuarioLogeado = usuarioLogeado;
        this.usuarioAEditar = usuarioAEditar;
        this.rolPermitidoParaCrear = rolPermitidoParaCrear;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setSize(550, 600); 
        setLocationRelativeTo(parentFrame);

        initComponents();
        
        if (usuarioAEditar != null) {
            cargarDatosSiEsEdicion();
            mostrarCamposEspecificos(usuarioAEditar.getRol(), true);
        } else {
            RolUsuario rolInicial = determinarRolInicial();
            mostrarCamposEspecificos(rolInicial, false);
        }
    }
    
    private RolUsuario determinarRolInicial() {
        if (usuarioLogeado instanceof Administrador) {
            return RolUsuario.JUGADOR; 
        }
        if (rolPermitidoParaCrear != null) {
            try {
                return RolUsuario.valueOf(rolPermitidoParaCrear);
            } catch (IllegalArgumentException e) {
                return RolUsuario.JUGADOR; 
            }
        }
        return RolUsuario.JUGADOR; 
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        JPanel panelFormularioBase = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
        panelFormularioBase.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
        panelFormularioBase.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
        panelFormularioBase.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
        panelFormularioBase.add(txtEmail, gbc);
        
        if (usuarioAEditar == null) {
            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panelFormularioBase.add(new JLabel("Contraseña:"), gbc);
            gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panelFormularioBase.add(txtPassword, gbc);
        } else {
            txtPassword.setVisible(false);
        }

        setupRolField(panelFormularioBase, y++);
        
        panelPrincipal.add(panelFormularioBase, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(panelEspecifico);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Detalles Específicos del Rol"));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnGuardar.addActionListener(e -> guardarUsuario());
        panelBoton.add(btnGuardar);
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        add(panelPrincipal, BorderLayout.CENTER);
    }
    
    private void setupRolField(JPanel panel, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        if (usuarioAEditar == null && usuarioLogeado instanceof Administrador) {
            comboRol = new JComboBox<>(new String[]{"JUGADOR", "EMPLEADO", "ADMINISTRADOR"});
            comboRol.addActionListener(e -> {
                try {
                    RolUsuario nuevoRol = RolUsuario.valueOf((String) comboRol.getSelectedItem());
                    mostrarCamposEspecificos(nuevoRol, false); 
                } catch (IllegalArgumentException ex) {
                }
            });
            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panel.add(new JLabel("Rol:"), gbc);
            gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panel.add(comboRol, gbc);
            
        } else {
            RolUsuario rol = (usuarioAEditar != null) ? usuarioAEditar.getRol() : RolUsuario.valueOf(rolPermitidoParaCrear);
            JLabel lblRolFijo = new JLabel(rol.toString());
            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panel.add(new JLabel("Rol Fijo:"), gbc);
            gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panel.add(lblRolFijo, gbc);
        }
    }
    
    private void mostrarCamposEspecificos(RolUsuario rol, boolean esEdicion) {
        panelEspecifico.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        int y = 0;

        if (rol == RolUsuario.JUGADOR) {
            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panelEspecifico.add(new JLabel("Saldo (€):"), gbc);
            gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panelEspecifico.add(txtSaldo, gbc);
            
            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panelEspecifico.add(new JLabel("Partidas Jugadas:"), gbc);
            gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panelEspecifico.add(txtPartidas, gbc);

            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panelEspecifico.add(new JLabel("Total Ganado (€):"), gbc);
            gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panelEspecifico.add(txtTotalGanado, gbc);

            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panelEspecifico.add(new JLabel("Nivel:"), gbc);
            gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panelEspecifico.add(txtNivel, gbc);
            
        } else if (rol == RolUsuario.EMPLEADO || rol == RolUsuario.ADMINISTRADOR) {
            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panelEspecifico.add(new JLabel("Puesto:"), gbc);
            gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panelEspecifico.add(txtPuesto, gbc);

            if (esEdicion) {
                gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
                panelEspecifico.add(new JLabel("Fecha Inicio (ISO):"), gbc);
                gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
                panelEspecifico.add(txtFechaInicio, gbc);
            } else {
                JLabel lblFechaAuto = new JLabel("Establecida Automáticamente al Guardar");
                lblFechaAuto.setFont(lblFechaAuto.getFont().deriveFont(Font.ITALIC));
                gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
                panelEspecifico.add(new JLabel("Fecha Inicio:"), gbc);
                gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
                panelEspecifico.add(lblFechaAuto, gbc);
            }
            
            gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.0;
            panelEspecifico.add(new JLabel("Estado:"), gbc);
            gbc.gridx = 1; gbc.gridy = y++; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0;
            panelEspecifico.add(chkActivo, gbc);
        }
        
        panelEspecifico.revalidate();
        panelEspecifico.repaint();
    }
    
    private void cargarDatosSiEsEdicion() {
        if (usuarioAEditar != null) {
            txtNombre.setText(usuarioAEditar.getNombre());
            txtEmail.setText(usuarioAEditar.getEmail());
            txtEmail.setEditable(false); 
            
            if (usuarioAEditar instanceof Jugador) {
                Jugador j = (Jugador) usuarioAEditar;
                txtSaldo.setText(String.valueOf(j.getSaldo()));
                txtPartidas.setText(String.valueOf(j.getNumeroDePartidas()));
                txtTotalGanado.setText(String.valueOf(j.getTotalGanado()));
                txtNivel.setText(String.valueOf(j.getNivel()));
                
            } else if (usuarioAEditar instanceof Empleado) {
                Empleado e = (Empleado) usuarioAEditar;
                txtPuesto.setText(e.getPuesto());
                txtFechaInicio.setText(e.getFechaInicio().format(Database.FORMATTER));
                chkActivo.setSelected(e.isActivo());
            }
        }
    }

    private void guardarUsuario() {
        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String passwordStr = new String(txtPassword.getPassword());
        
        if (nombre.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Email son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (usuarioAEditar == null && passwordStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La Contraseña es obligatoria para un nuevo usuario.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Usuario usuarioFinal = null;
            RolUsuario rol;
            
            if (usuarioAEditar == null) {
                
                String rolStr = (usuarioLogeado instanceof Administrador) 
                                ? (String) comboRol.getSelectedItem() : rolPermitidoParaCrear;
                rol = RolUsuario.valueOf(rolStr);
                
                LocalDateTime ahora = LocalDateTime.now(); 
                
                if (rol == RolUsuario.JUGADOR) {
                    double saldo = parseDouble(txtSaldo.getText(), "Saldo");
                    int partidas = parseInt(txtPartidas.getText(), "Partidas Jugadas");
                    double totalGanado = parseDouble(txtTotalGanado.getText(), "Total Ganado");
                    int nivel = parseInt(txtNivel.getText(), "Nivel");
                    
                    usuarioFinal = new Jugador(nombre, email, passwordStr, ahora, 
                                               saldo, partidas, totalGanado, nivel);
                } else if (rol == RolUsuario.EMPLEADO || rol == RolUsuario.ADMINISTRADOR) {
                    String puesto = txtPuesto.getText().trim();
                    boolean activo = chkActivo.isSelected();
                    
                    if (rol == RolUsuario.ADMINISTRADOR) {
                        usuarioFinal = new Administrador(nombre, email, passwordStr, ahora, 
                                                         puesto, ahora, activo);
                    } else {
                        usuarioFinal = new Empleado(nombre, email, passwordStr, ahora, 
                                                    puesto, ahora, activo);
                    }
                } else {
                    throw new IllegalArgumentException("Rol no soportado.");
                }
            } else {
                usuarioFinal = usuarioAEditar;
                usuarioFinal.setNombre(nombre);
                rol = usuarioAEditar.getRol();

                if (rol == RolUsuario.JUGADOR) {
                    Jugador j = (Jugador) usuarioFinal;
                    j.setSaldo(parseDouble(txtSaldo.getText(), "Saldo"));
                    j.setNumeroDePartidas(parseInt(txtPartidas.getText(), "Partidas Jugadas"));
                    j.setTotalGanado(parseDouble(txtTotalGanado.getText(), "Total Ganado"));
                    j.setNivel(parseInt(txtNivel.getText(), "Nivel"));
                    
                } else if (rol == RolUsuario.EMPLEADO || rol == RolUsuario.ADMINISTRADOR) {
                    Empleado e = (Empleado) usuarioFinal;
                    e.setPuesto(txtPuesto.getText().trim());
                    e.setFechaInicio(parseLocalDateTime(txtFechaInicio.getText())); 
                    e.setActivo(chkActivo.isSelected());
                }
                
                if (txtPassword.isVisible() && !passwordStr.isEmpty()) {
                    usuarioFinal.setPassword(passwordStr);
                }
            }
            
            if (usuarioFinal != null) {
                database.registrarUsuario(usuarioFinal); 
                JOptionPane.showMessageDialog(this, "Usuario guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                if (ventanaGestionPadre != null) {
                    ventanaGestionPadre.cargarDatosDesdeFormulario(); 
                }
                
                dispose();
            }

        } catch (NumberFormatException | DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error de formato: Asegúrate de que los campos numéricos y la fecha (ISO: YYYY-MM-DDThh:mm:ss) sean correctos. Detalle: " + ex.getMessage(), 
                "Error de Validación de Datos", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al guardar el usuario: " + ex.getMessage(), 
                "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private double parseDouble(String text, String fieldName) throws NumberFormatException {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("El campo '" + fieldName + "' debe ser un número decimal válido.");
        }
    }

    private int parseInt(String text, String fieldName) throws NumberFormatException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("El campo '" + fieldName + "' debe ser un número entero válido.");
        }
    }

    private LocalDateTime parseLocalDateTime(String text) throws DateTimeParseException {
        try {
            return LocalDateTime.parse(text.trim(), Database.FORMATTER);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("El formato de fecha/hora debe ser 'YYYY-MM-DDThh:mm:ss'.", text, e.getErrorIndex());
        }
    }
}