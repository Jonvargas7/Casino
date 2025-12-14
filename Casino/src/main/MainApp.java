package main; 

import domain.*;
import gestor.Database; 
import gui.VentanaInicio;
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

        registrarUsuariosDePrueba(db); 
        registrarJuegos(db);
        listarJuegosActivos(db);
        
        System.out.println("\n--- 3. Lanzando Ventana de Inicio ---");
        
        SwingUtilities.invokeLater(() -> {
            new VentanaInicio(db).setVisible(true); 
        });
    }
    
    private static void registrarUsuariosDePrueba(Database db) {
        System.out.println("\n--- 2.1. Registrando Usuarios de Prueba (si no existen) ---");
        LocalDateTime now = LocalDateTime.now();
        
        try {
            Administrador admin = new Administrador(0, "Admin Casino", "admin@casino.com", "admin123", 
                now, "Gerente General", now, true);
            if (db.registrarUsuario(admin)) {
                System.out.println("-> Administrador admin@casino.com registrado.");
            } else {
                System.out.println("-> Administrador admin@casino.com ya existe.");
            }
        } catch (Exception e) {
            System.err.println("-> Error al registrar Admin: " + e.getMessage());
        }

        try {
            Empleado emp = new Empleado(0, "Empleado Crupier", "emp@casino.com", "emp123", 
                now, "Crupier de Mesa", now, true);
            if (db.registrarUsuario(emp)) {
                System.out.println("-> Empleado emp@casino.com registrado.");
            } else {
                System.out.println("-> Empleado emp@casino.com ya existe.");
            }
        } catch (Exception e) {
            System.err.println("-> Error al registrar Empleado: " + e.getMessage());
        }

        try {
            Jugador player = new Jugador(0, "Jugador Demo", "player@casino.com", "player123", 
                now, 500.0, 0, 0.0, 1);
            if (db.registrarUsuario(player)) {
                System.out.println("-> Jugador player@casino.com registrado (Saldo: 500.0).");
            } else {
                System.out.println("-> Jugador player@casino.com ya existe.");
            }
        } catch (Exception e) {
            System.err.println("-> Error al registrar Jugador: " + e.getMessage());
        }
    }
    
    private static void registrarJuegos(Database db) {
        System.out.println("\n--- 2.2. Registrando Juegos de Prueba (si no existen) ---");
        
        Blackjack bj = new Blackjack(
            0, 
            "Blackjack Clásico", 
            java.time.LocalDateTime.now(), 
            true, 
            6,    
            10.0, 
            1000.0 
        );
        try {
             if (db.registrarJuego(bj).isPresent()) {
                 System.out.println("-> Blackjack Clásico registrado.");
             } else {
                 System.out.println("-> Blackjack Clásico ya existe o error.");
             }
        } catch (Exception e) {
            System.out.println("-> Blackjack Clásico ya existe o error: " + e.getMessage());
        }
        
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
             if (db.registrarJuego(hl).isPresent()) {
                 System.out.println("-> High Low Clásico registrado.");
             } else {
                 System.out.println("-> High Low Clásico ya existe o error.");
             }
        } catch (Exception e) {
            System.out.println("-> High Low Clásico ya existe o error: " + e.getMessage());
        }
    }
    
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