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
        
        
        System.out.println("--- 1. Inicializando Base de Datos ---");
        
        Database db = new Database(); 
        
        
        try { 
            db.inicializarBaseDatos(); 
            System.out.println("Base de datos inicializada o tablas verificadas correctamente.");
            
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al inicializar la base de datos: " + e.getMessage());
            return; 
        }

        
        registrarUsuariosDePrueba(db); // Este método ahora incluye al Empleado
        registrarJuegos(db);
        listarJuegosActivos(db);
        
        
        System.out.println("\n--- 3. Lanzando Ventana de Inicio ---");
        
        SwingUtilities.invokeLater(() -> {
            new VentanaInicio(db).setVisible(true); 
        });
    }
    
    
    /**
     * Registra usuarios de prueba (Admin, Jugador y EMPLEADO) si no existen.
     */
    private static void registrarUsuariosDePrueba(Database db) {
        System.out.println("\n--- 2.1. Registro de Usuarios de Prueba ---");

        // 1. Administrador
        Usuario adminExistente = db.login("admin@casino.com", "admin123", RolUsuario.ADMINISTRADOR.name());
        if (adminExistente == null) {
            Administrador admin = new Administrador(
                0, 
                "Admin Max", 
                "admin@casino.com", 
                "admin123", 
                LocalDateTime.now(), 
                99
            );
            try {
                db.registrar(admin);
                System.out.println("Administrador de prueba registrado: " + admin.getNombre());
            } catch (SQLException e) {
                System.err.println("Error al registrar Admin: " + e.getMessage());
            }
        } else {
            System.out.println("Administrador de prueba ya existe.");
        }
        
        // 2. Jugador
        Usuario jugadorExistente = db.login("player@casino.com", "player123", RolUsuario.JUGADOR.name());
        if (jugadorExistente == null) {
            Jugador jugador = new Jugador(
                0, 
                "Pepe Gambler", 
                "player@casino.com", 
                "player123", 
                LocalDateTime.now(), 
                500.0, // Saldo inicial
                0, 
                0.0, 
                1
            );
            try {
                db.registrar(jugador);
                System.out.println("Jugador de prueba registrado: " + jugador.getNombre());
            } catch (SQLException e) {
                System.err.println("Error al registrar Jugador: " + e.getMessage());
            }
        } else {
            System.out.println("Jugador de prueba ya existe.");
        }
        
        // 3. EMPLEADO (¡Registro Asegurado!)
        Usuario empleadoExistente = db.login("emp@casino.com", "emp123", RolUsuario.EMPLEADO.name());

        if (empleadoExistente == null) { 
            Empleado empleado = new Empleado(
                0, 
                "Crupier Leo", 
                "emp@casino.com", 
                "emp123", 
                LocalDateTime.now(), 
                "Crupier", 
                true, // Activo
                LocalDateTime.now()
            );
            
            try {
                db.registrar(empleado);
                System.out.println("Empleado de prueba registrado: " + empleado.getNombre());
            } catch (SQLException e) {
                System.err.println("Error al registrar Empleado: " + e.getMessage());
            } 
        } else {
            System.out.println("Empleado de prueba ya existe.");
        }
    }
    
    /**
     * Registra juegos de prueba (Blackjack y HighLow) si no existen.
     */
    private static void registrarJuegos(Database db) {
        System.out.println("\n--- 2.2. Registro de Juegos de Prueba ---");
        
        // Blackjack
        Blackjack bj = new Blackjack(
            0, 
            "Blackjack 5 Mazos", 
            java.time.LocalDateTime.now(), 
            true, 
            5, 
            10.0, 
            1000.0
        );
        try {
             db.registrarJuego(bj); 
        } catch (SQLException e) {
            System.err.println("Error al registrar Blackjack: " + e.getMessage());
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
        } catch (SQLException e) {
            System.err.println("Error al registrar High Low: " + e.getMessage());
        }
    }
    
    /**
     * Lista los juegos activos registrados.
     */
    private static void listarJuegosActivos(Database db) {
        System.out.println("\n--- 2.3. Listado de Juegos Activos ---");
        
        List<Juego> juegos = db.obtenerJuegosActivos();
        
        if (juegos.isEmpty()) {
            System.out.println("No se encontraron juegos activos.");
        } else {
            for (Juego juego : juegos) {
                String tipoJuego = juego.getClass().getSimpleName();
                double apuestaMin = 0.0;
                
                if (juego instanceof Blackjack) {
                    apuestaMin = ((Blackjack) juego).getApuestaMin();
                } else if (juego instanceof HighLow) {
                    apuestaMin = ((HighLow) juego).getApuestaMin();
                }
                
                System.out.println(String.format("Juego [%s] - ID: %d, Nombre: %s, Apuesta Mín: %.2f",
                        tipoJuego, juego.getId(), juego.getNombre(), apuestaMin));
            }
        }
    }
}