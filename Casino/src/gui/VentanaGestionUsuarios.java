package gui;

import domain.*;
import gestor.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VentanaGestionUsuarios extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("VentanaGestionUsuarios");
    
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Database database;
    private Usuario usuarioLogeado;

    private JTabbedPane tabbedPane;
    private JTable tablaJugadores;
    private JTable tablaEmpleados;

   
    private final JButton btnAnadir = new JButton("➕ Añadir Nuevo Usuario");
    private final JButton btnEditar = new JButton("✏️ Editar Seleccionado");
    private final JButton btnEliminar = new JButton("🗑️ Eliminar Seleccionado");


    public VentanaGestionUsuarios(JFrame parent, Database database, Usuario usuarioLogeado) {
        super(parent, "Gestión de Usuarios - Casino", true);
        this.database = database;
        this.usuarioLogeado = usuarioLogeado;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(parent);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        
        tablaJugadores = new JTable();
        tablaJugadores.setRowHeight(25);
        tablaJugadores.getTableHeader().setReorderingAllowed(false);
        tabbedPane.addTab("Jugadores", new JScrollPane(tablaJugadores));
        
        
        tablaEmpleados = new JTable();
        tablaEmpleados.setRowHeight(25);
        tablaEmpleados.getTableHeader().setReorderingAllowed(false);
        
     
        if (usuarioLogeado instanceof Administrador) {
            tabbedPane.addTab("Empleados y Administradores", new JScrollPane(tablaEmpleados));
        } else if (usuarioLogeado instanceof Empleado) {
            logger.info("Empleado logeado (" + usuarioLogeado.getEmail() + ") no tiene permiso para ver la pestaña de gestión de Empleados/Admin.");
        }
        // -----------------------------------------------------------------------

        add(tabbedPane, BorderLayout.CENTER);
        
      
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        panelBotones.add(btnAnadir);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);

        add(panelBotones, BorderLayout.SOUTH);
        
        btnAnadir.addActionListener(e -> abrirVentanaAnadir());
        btnEditar.addActionListener(e -> abrirVentanaEditar());
        btnEliminar.addActionListener(e -> intentarEliminar());
    }

   
    private void cargarDatos() {
        try {
            List<Usuario> todosUsuarios = database.obtenerTodosLosUsuarios();
            
          
            List<Jugador> jugadores = todosUsuarios.stream()
                    .filter(u -> u instanceof Jugador)
                    .map(u -> (Jugador) u)
                    .collect(Collectors.toList());
            cargarTablaJugadores(jugadores);

           
            if (usuarioLogeado instanceof Administrador) {
                // Filtramos a todos los Empleados (que incluye Administrador)
                List<Empleado> empleados = todosUsuarios.stream()
                        .filter(u -> u instanceof Empleado)
                        .map(u -> (Empleado) u)
                        .collect(Collectors.toList());
                cargarTablaEmpleados(empleados);
            }
            
        } catch (Exception e) {
            logger.severe("Error al cargar datos de la base de datos: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al cargar los datos de usuario: " + e.getMessage(), 
                "Error de Carga", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void cargarTablaJugadores(List<Jugador> jugadores) {
        String[] columnas = {"ID", "Nombre", "Email", "Saldo", "Partidas", "Ganado", "Nivel", "Registro"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Long.class;
                if (columnIndex == 3 || columnIndex == 5) return Double.class;
                if (columnIndex == 4 || columnIndex == 6) return Integer.class;
                return String.class;
            }
        };

        for (Jugador j : jugadores) {
            model.addRow(new Object[]{
                j.getId(),
                j.getNombre(),
                j.getEmail(),
                j.getSaldo(),
                j.getNumeroDePartidas(),
                j.getTotalGanado(),
                j.getNivel(),
                j.getFechaRegistro().format(DISPLAY_FORMATTER)
            });
        }
        
        tablaJugadores.setModel(model);
        tablaJugadores.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaJugadores.getColumnModel().getColumn(3).setPreferredWidth(70);
    }

   
    private void cargarTablaEmpleados(List<Empleado> empleados) {
        String[] columnas = {"ID", "Nombre", "Email", "Rol", "Puesto", "Fecha Inicio", "Activo"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Long.class;
                if (columnIndex == 6) return Boolean.class;
                return String.class;
            }
        };

        for (Empleado e : empleados) {
           
            String rolDisplay = (e instanceof Administrador) ? "ADMINISTRADOR" : "EMPLEADO";
            
            model.addRow(new Object[]{
                e.getId(),
                e.getNombre(),
                e.getEmail(),
                rolDisplay, 
                e.getPuesto(),
                e.getFechaInicio().format(DISPLAY_FORMATTER),
                e.isActivo()
            });
        }
        
        tablaEmpleados.setModel(model);
        tablaEmpleados.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaEmpleados.getColumnModel().getColumn(3).setPreferredWidth(120); 
        tablaEmpleados.getColumnModel().getColumn(6).setPreferredWidth(60); 
    }
    
    
    public void cargarDatosDesdeFormulario() {
        cargarDatos();
    }

    
  
    private void abrirVentanaAnadir() {
        
        String rolForzado = null; 
        boolean tienePermiso = false;

        if (usuarioLogeado instanceof Administrador) {
            tienePermiso = true; 
        } else if (usuarioLogeado instanceof Empleado) {
            rolForzado = "JUGADOR"; // Empleado solo puede crear Jugadores
            tienePermiso = true; 
        } else { 
            logger.warning("Usuario sin permisos (" + usuarioLogeado.getClass().getSimpleName() + ") intentó usar Añadir.");
        }
        
        if (tienePermiso) {
            
            VentanaFormularioUsuario ventana = new VentanaFormularioUsuario(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                this, 
                database, 
                usuarioLogeado, 
                (Usuario) null, 
                rolForzado
            );
            ventana.setVisible(true);
        } else {
             JOptionPane.showMessageDialog(this, 
                "Permiso Denegado: Su rol no le permite añadir nuevos usuarios.", 
                "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirVentanaEditar() {
        Usuario seleccionado = obtenerUsuarioSeleccionado();
        
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario para editar.", "Error de Selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
      
        boolean esAutoEdicion = seleccionado.getId() == usuarioLogeado.getId();
        boolean tienePermiso = false;

        if (usuarioLogeado instanceof Administrador) {
            tienePermiso = true;
        } else if (usuarioLogeado instanceof Empleado) {
            // Empleado puede editar Jugadores o sus propios datos
            if (seleccionado instanceof Jugador || esAutoEdicion) {
                 tienePermiso = true;
            }
        } else { // Jugador logeado
            // Jugador solo puede auto-editarse
            if (esAutoEdicion) {
                tienePermiso = true;
            }
        }
        
        if (tienePermiso) {
            // Abrir la nueva ventana de formulario para Editar
            VentanaFormularioUsuario ventana = new VentanaFormularioUsuario(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                this, 
                database, 
                usuarioLogeado, 
                seleccionado, 
                null 
            );
            ventana.setVisible(true);
        } else {
             JOptionPane.showMessageDialog(this, 
                "Permiso Denegado: Solo puede editar Jugadores o sus propios datos.", 
                "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void intentarEliminar() {
        Usuario seleccionado = obtenerUsuarioSeleccionado();
        
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario de la tabla para eliminar.", "Error de Selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // No se puede eliminar a uno mismo
        if (seleccionado.getId() == usuarioLogeado.getId()) {
             JOptionPane.showMessageDialog(this, 
                "Permiso Denegado: No puede eliminarse a sí mismo.", 
                "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
             return;
        }
        
        
        boolean tienePermiso = false;
        String rolSeleccionado = seleccionado.getRol().toString();

        if (usuarioLogeado instanceof Administrador) {
            if (seleccionado instanceof Administrador) {
                JOptionPane.showMessageDialog(this, 
                    "Permiso Denegado: Un Administrador no puede eliminar a otro Administrador.", 
                    "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                return;
            }
            tienePermiso = true;
            
        } else if (usuarioLogeado instanceof Empleado) {
            // Empleado solo puede eliminar Jugadores
            if (seleccionado instanceof Jugador) {
                tienePermiso = true;
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Permiso Denegado: Un Empleado solo puede eliminar Jugadores.", 
                    "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else { // Jugador
            JOptionPane.showMessageDialog(this, 
                "Permiso Denegado: Un Jugador no tiene permisos para eliminar usuarios.", 
                "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tienePermiso) {
            int confirmacion = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar a '" + seleccionado.getNombre() + "' (" + rolSeleccionado + ")?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    database.eliminarUsuario(seleccionado.getId()); 
                    cargarDatos(); 
                    JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error al intentar eliminar: " + e.getMessage(), 
                        "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    
    private Usuario obtenerUsuarioSeleccionado() {
        
        JTable tablaActiva;
        try {
            tablaActiva = (JTable) ((JScrollPane) tabbedPane.getSelectedComponent()).getViewport().getView();
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
        
        int filaSeleccionada = tablaActiva.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            return null; 
        }

        
        Object idObjeto = tablaActiva.getValueAt(filaSeleccionada, 0);
        long id = -1;
        
        if (idObjeto instanceof Long) {
            id = (Long) idObjeto;
        } else if (idObjeto instanceof Integer) {
            id = ((Integer) idObjeto).longValue();
        } else if (idObjeto instanceof String) {
            try {
                id = Long.parseLong((String) idObjeto);
            } catch (NumberFormatException e) {
                logger.severe("Error al parsear ID: " + idObjeto);
                return null;
            }
        } else {
             logger.warning("Tipo de ID desconocido en la tabla: " + idObjeto.getClass().getName());
             return null;
        }

        // Recuperar el usuario completo de la base de datos
        try {
            Usuario u = database.obtenerUsuarioPorId(id);
            return u;
        } catch (Exception e) {
            logger.severe("Error al obtener usuario por ID: " + e.getMessage());
        }
        
        return null;
    }
}