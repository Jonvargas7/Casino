package main; 

import domain.*;
import gestor.Database; 
import gui.VentanaInicio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.SwingUtilities; 


public class MainApp {

    public static void main(String[] args) {
        
        // 1. Inicialización de la Base de Datos
        System.out.println("--- 1. Inicializando Base de Datos ---");
        
        Database db = new Database(); 
        
        
        try { 
            db.inicializarBaseDatos(); 
            System.out.println("Base de datos inicializada o tablas verificadas correctamente.");
            
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al inicializar la base de datos: " + e.getMessage());
            return; 
        }

        // 2. Registro de Datos de Prueba
        registrarUsuariosDePrueba(db); 
        registrarJuegos(db);
        listarJuegosActivos(db);
        
        
        // 3. Lanzamiento de la Interfaz Gráfica
        System.out.println("\n--- 3. Lanzando Ventana de Inicio ---");
        
        SwingUtilities.invokeLater(() -> {
            new VentanaInicio(db).setVisible(true); 
        });
    }
    
    
    /**
     * Registra usuarios de prueba (Admin, Empleado y Jugador) si no existen.
     */
    private static void registrarUsuariosDePrueba(Database db) {
        System.out.println("\n--- 2.1. Registrando Usuarios de Prueba (si no existen) ---");
        LocalDateTime now = LocalDateTime.now();
        
        // La restricción UNIQUE del email hace que falle si ya existen.
        // Se usa una comprobación más robusta para el error.
        
        // 1. ADMINISTRADOR: admin@casino.com, admin123, Acceso Total
        try {
            Administrador admin = new Administrador(0, "Admin Casino", "admin@casino.com", "admin123", 
                now, "Gerente General", now, true);
            db.registrar(admin); 
            System.out.println("-> Administrador admin@casino.com registrado.");
        } catch (SQLException e) {
            // CORRECCIÓN: Comprueba tanto el código 19 como el mensaje de error.
            boolean isUniqueConstraintError = (e.getErrorCode() == 19) || 
                                              e.getMessage().contains("UNIQUE constraint failed");
            
            if (isUniqueConstraintError) {
                 System.out.println("-> Administrador admin@casino.com ya existe.");
            } else {
                 System.err.println("-> Error al registrar Admin: " + e.getMessage());
            }
        }

        // 2. EMPLEADO: emp@casino.com, emp123, Acceso Limitado (Crupier)
        try {
            Empleado emp = new Empleado(0, "Empleado Crupier", "emp@casino.com", "emp123", 
                now, "Crupier de Mesa", now, true);
            db.registrar(emp);
            System.out.println("-> Empleado emp@casino.com registrado.");
        } catch (SQLException e) {
            // CORRECCIÓN: Comprueba tanto el código 19 como el mensaje de error.
            boolean isUniqueConstraintError = (e.getErrorCode() == 19) || 
                                              e.getMessage().contains("UNIQUE constraint failed");
                                              
            if (isUniqueConstraintError) {
                 System.out.println("-> Empleado emp@casino.com ya existe.");
            } else {
                 System.err.println("-> Error al registrar Empleado: " + e.getMessage());
            }
        }

        // 3. JUGADOR: player@casino.com, player123, Acceso Juego (Saldo inicial 500.0)
        try {
            Jugador player = new Jugador(0, "Jugador Demo", "player@casino.com", "player123", 
                now, 500.0, 0, 0.0, 1);
            db.registrar(player);
            System.out.println("-> Jugador player@casino.com registrado (Saldo: 500.0).");
        } catch (SQLException e) {
            // CORRECCIÓN: Comprueba tanto el código 19 como el mensaje de error.
            boolean isUniqueConstraintError = (e.getErrorCode() == 19) || 
                                              e.getMessage().contains("UNIQUE constraint failed");
                                              
            if (isUniqueConstraintError) {
                 System.out.println("-> Jugador player@casino.com ya existe.");
            } else {
                 System.err.println("-> Error al registrar Jugador: " + e.getMessage());
            }
        }
    }
    
    /**
     * Registra los juegos iniciales si no existen.
     */
    private static void registrarJuegos(Database db) {
        System.out.println("\n--- 2.2. Registrando Juegos de Prueba (si no existen) ---");
        
        // Blackjack
        Blackjack bj = new Blackjack(
            0, 
            "Blackjack Clásico", 
            java.time.LocalDateTime.now(), 
            true, // activo
            6,    // 6 mazos
            10.0, // apuesta mínima
            1000.0 // apuesta máxima
        );
        try {
             db.registrarJuego(bj); 
             System.out.println("-> Blackjack Clásico registrado.");
        } catch (SQLException e) {
            System.out.println("-> Blackjack Clásico ya existe o error: " + e.getMessage());
        }
        
        // High Low
        HighLow hl = new HighLow(
            0, 
            "High Low Clásico", 
            java.time.LocalDateTime.now(), 
            true, 
            1, 
            5.0, 
            500.0
        );
        try {
             db.registrarJuego(hl); 
             System.out.println("-> High Low Clásico registrado.");
        } catch (SQLException e) {
            System.out.println("-> High Low Clásico ya existe o error: " + e.getMessage());
        }
    }
    
    /**
     * Lista los juegos activos registrados.
     */
    private static void listarJuegosActivos(Database db) {
        System.out.println("\n--- 2.3. Listado de Juegos Activos ---\n");
        
        List<Juego> juegos = db.obtenerJuegosActivos();
        
        if (juegos.isEmpty()) {
            System.out.println("No se encontraron juegos activos.");
        } else {
            System.out.printf("| %-4s | %-20s | %-10s | %-10s | %-10s |\n", "ID", "Nombre", "Tipo", "Apuesta Min", "Mazos");
            System.out.println("|------|----------------------|------------|------------|------------|");
            for (Juego juego : juegos) {
                String tipoJuego = juego.getClass().getSimpleName();
                double apuestaMin = 0.0;
                int mazos = 0;
                
                if (juego instanceof Blackjack) {
                    Blackjack bj = (Blackjack) juego;
                    apuestaMin = bj.getApuestaMin();
                    mazos = bj.getMazos();
                } else if (juego instanceof HighLow) {
                    HighLow hl = (HighLow) juego;
                    apuestaMin = hl.getApuestaMin();
                    mazos = hl.getMazos();
                }
                
                System.out.printf("| %-4d | %-20s | %-10s | %-10.2f | %-10d |\n", 
                    juego.getId(), 
                    juego.getNombre(), 
                    tipoJuego, 
                    apuestaMin, 
                    mazos
                );
            }
        }
        System.out.println("--------------------------------------------------------------------");
    }
}