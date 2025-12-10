package gui;

import domain.*;
import gestor.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VentanaGestionUsuarios extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("VentanaGestionUsuarios");

    private Database database;
    private Usuario usuarioLogeado;

    private JTabbedPane tabbedPane;
    private JTable tablaJugadores;
    private JTable tablaEmpleados;

    // CONSTRUCTOR CORREGIDO: Acepta JFrame parent, Database y Usuario
    public VentanaGestionUsuarios(JFrame parent, Database database, Usuario usuarioLogeado) {
        super(parent, "Gestión de Usuarios - Casino", true);
        this.database = database;
        this.usuarioLogeado = usuarioLogeado;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(parent);

        initComponents();
        cargarDatos();
        
        setVisible(true);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // --- Pestaña de Jugadores ---
        tablaJugadores = new JTable();
        tablaJugadores.setRowHeight(25);
        tablaJugadores.getTableHeader().setReorderingAllowed(false); 
        JScrollPane scrollJugadores = new JScrollPane(tablaJugadores);
        tabbedPane.addTab("Jugadores (Clientes)", scrollJugadores);

        // --- Pestaña de Empleados (Solo para Administradores) ---
        tablaEmpleados = new JTable();
        tablaEmpleados.setRowHeight(25);
        tablaEmpleados.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollEmpleados = new JScrollPane(tablaEmpleados);

        if (usuarioLogeado instanceof Administrador) {
            tabbedPane.addTab("Empleados", scrollEmpleados);
        }

        getContentPane().setBackground(new Color(40, 44, 52));
        tabbedPane.setBackground(new Color(40, 44, 52));
        tabbedPane.setForeground(Color.WHITE);

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    private void cargarDatos() {
        List<Usuario> usuarios = database.getTodosLosUsuarios();
        
        List<Jugador> jugadores = usuarios.stream()
                .filter(u -> u instanceof Jugador)
                .map(u -> (Jugador) u)
                .collect(Collectors.toList());

        List<Empleado> empleados = usuarios.stream()
                .filter(u -> u instanceof Empleado)
                .map(u -> (Empleado) u)
                .collect(Collectors.toList());

        cargarTablaJugadores(jugadores);

        if (usuarioLogeado instanceof Administrador) {
            cargarTablaEmpleados(empleados);
        }
    }

    private void cargarTablaJugadores(List<Jugador> jugadores) {
        String[] columnas = {
            "ID", "Nombre", "Email", "F. Registro", "Saldo (€)", 
            "Nivel", "Partidas Jug.", "Total Ganado (€)"
        };
        
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 7) return Double.class;
                if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6) return Integer.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Jugador j : jugadores) {
            // Uso de Database.FORMATTER (ahora público)
            String fechaRegStr = j.getFechaRegistro().format(Database.FORMATTER); 
            
            model.addRow(new Object[]{
                j.getId(), j.getNombre(), j.getEmail(), fechaRegStr, j.getSaldo(),
                j.getNivel(), j.getNumeroDePartidas(), j.getTotalGanado()
            });
        }
        
        tablaJugadores.setModel(model);
        tablaJugadores.setAutoCreateRowSorter(true);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tablaJugadores.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); 
        tablaJugadores.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); 
        tablaJugadores.getColumnModel().getColumn(7).setCellRenderer(rightRenderer); 
        
        tablaJugadores.getColumnModel().getColumn(0).setMaxWidth(40); 
    }

    private void cargarTablaEmpleados(List<Empleado> empleados) {
        String[] columnas = {
            "ID", "Nombre", "Email", "Puesto", "Activo", "F. Inicio Puesto", 
            "F. Registro Base"
        };
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Boolean.class; 
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Empleado e : empleados) {
            // Uso de Database.FORMATTER (ahora público)
            String fechaInicioStr = e.getFechaInicio().format(Database.FORMATTER);
            String fechaRegStr = e.getFechaRegistro().format(Database.FORMATTER);
            
            model.addRow(new Object[]{
                e.getId(), e.getNombre(), e.getEmail(), e.getPuesto(), e.isActivo(),
                fechaInicioStr, fechaRegStr
            });
        }

        tablaEmpleados.setModel(model);
        tablaEmpleados.setAutoCreateRowSorter(true);
        
        tablaEmpleados.getColumnModel().getColumn(0).setMaxWidth(40); 
        tablaEmpleados.getColumnModel().getColumn(4).setMaxWidth(60); 
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tablaEmpleados.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); 
    }
}