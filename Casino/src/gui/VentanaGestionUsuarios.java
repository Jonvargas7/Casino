package gui;

import domain.*;
import gestor.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VentanaGestionUsuarios extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("VentanaGestionUsuarios");
    // Formato de fecha para mostrar en la tabla, más legible.
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Database database;
    private Usuario usuarioLogeado;

    private JTabbedPane tabbedPane;
    private JTable tablaJugadores;
    private JTable tablaEmpleados;

    // Botones de Gestión
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
        cargarDatos(); // Llama a la carga de datos inicial
        
        // Se hace visible al final del constructor o en el MainApp
        // setVisible(true);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Pestaña de Jugadores
        tablaJugadores = new JTable();
        tablaJugadores.setRowHeight(25);
        tablaJugadores.getTableHeader().setReorderingAllowed(false);
        tabbedPane.addTab("Jugadores", new JScrollPane(tablaJugadores));
        
        // Pestaña de Empleados/Admin
        tablaEmpleados = new JTable();
        tablaEmpleados.setRowHeight(25);
        tablaEmpleados.getTableHeader().setReorderingAllowed(false);
        tabbedPane.addTab("Empleados y Administradores", new JScrollPane(tablaEmpleados));

        // Añadir el panel de pestañas a la ventana
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.add(btnAnadir);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);

        add(panelBotones, BorderLayout.SOUTH);
        
        // Listeners
        btnAnadir.addActionListener(e -> abrirVentanaAnadir());
        btnEditar.addActionListener(e -> abrirVentanaEditar());
        btnEliminar.addActionListener(e -> intentarEliminar());
    }

    /**
     * CORRECCIÓN CLAVE: Obtiene todos los usuarios y los clasifica para las tablas.
     */
    private void cargarDatos() {
        try {
            // Este método DEBE DEVOLVER los objetos Empleado/Administrador completos
            List<Usuario> todosUsuarios = database.obtenerTodosLosUsuarios();
            
            // 1. Filtrar y cargar Jugadores
            List<Jugador> jugadores = todosUsuarios.stream()
                    .filter(u -> u instanceof Jugador)
                    .map(u -> (Jugador) u)
                    .collect(Collectors.toList());
            cargarTablaJugadores(jugadores);

            // 2. Filtrar y cargar Empleados/Administradores
            // Nota: Administrador hereda de Empleado, por lo que este filtro los incluye a ambos.
            List<Empleado> empleados = todosUsuarios.stream()
                    .filter(u -> u instanceof Empleado)
                    .map(u -> (Empleado) u)
                    .collect(Collectors.toList());
            cargarTablaEmpleados(empleados);
            
        } catch (Exception e) {
            logger.severe("Error al cargar datos de la base de datos: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error al cargar los datos de usuario: " + e.getMessage(), 
                "Error de Carga", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Implementación para cargar la tabla de Jugadores.
     * @param jugadores Lista de objetos Jugador.
     */
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

    /**
     * Implementación para cargar la tabla de Empleados y Administradores.
     * @param empleados Lista de objetos Empleado (incluye Administrador).
     */
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
            // Determina el rol de visualización
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
    
    // =========================================================
    // --- MÉTODOS DE GESTIÓN (Añadir, Editar, Eliminar) ---
    // =========================================================

    private void abrirVentanaAnadir() {
        // Lógica de añadir...
        JOptionPane.showMessageDialog(this, "Funcionalidad 'Añadir Usuario' pendiente.", "Pendiente", JOptionPane.INFORMATION_MESSAGE);
    }

    private void abrirVentanaEditar() {
        Usuario seleccionado = obtenerUsuarioSeleccionado();
        
        if (seleccionado != null) {
            // Lógica de edición...
            JOptionPane.showMessageDialog(this, 
                "Funcionalidad 'Editar Usuario' (" + seleccionado.getNombre() + ") pendiente.", 
                "Pendiente", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario.", "Error de Selección", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void intentarEliminar() {
        Usuario seleccionado = obtenerUsuarioSeleccionado();
        
        if (seleccionado != null) {
            int confirmacion = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar al usuario " + seleccionado.getNombre() + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    // Lógica de eliminación...
                    database.eliminarUsuario(seleccionado.getId()); // Asumiendo que el método ya está implementado
                    cargarDatos(); // Refrescar la tabla
                    JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error al intentar eliminar: " + e.getMessage(), 
                        "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario de la tabla para eliminar.", "Error de Selección", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Método auxiliar para obtener el usuario seleccionado en la pestaña activa.
     */
    private Usuario obtenerUsuarioSeleccionado() {
        // Obtener la tabla activa
        JTable tablaActiva;
        try {
            tablaActiva = (JTable) ((JScrollPane) tabbedPane.getSelectedComponent()).getViewport().getView();
        } catch (ClassCastException | NullPointerException e) {
            // En caso de que el componente no sea un JScrollPane o esté mal inicializado
            return null;
        }
        
        int filaSeleccionada = tablaActiva.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            return null; // No hay fila seleccionada
        }

        // Obtener el ID (primera columna)
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