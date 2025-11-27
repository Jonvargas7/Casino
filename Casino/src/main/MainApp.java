package main; 

import domain.*;
import gestor.GestorCasino;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MainApp {

    public static void main(String[] args) {
        // 1. Inicializar el Gestor Casino
        System.out.println("--- Inicializando Gestor Casino y Base de Datos ---");
        GestorCasino gestor = new GestorCasino(); 

        // 2. Registrar un Jugador de Prueba
        registrarJugadorDePrueba(gestor);
        
        // 3. Registrar los Juegos (Blackjack y HighLow)
        registrarJuegos(gestor);

        // 4. Listar y verificar Juegos Activos 
        listarJuegosActivos(gestor);
    }
    
    
    private static void registrarJugadorDePrueba(GestorCasino gestor) {
        System.out.println("\n--- 1. Registro y Autenticación ---");
        
        Optional<Usuario> admin = gestor.autenticarUsuario("admin@casino.com", "admin123");
        if (admin.isPresent()) {
            System.out.println("Administrador autenticado correctamente: " + admin.get().getNombre());
        }

        
        if (!gestor.autenticarUsuario("player@casino.com", "pass123").isPresent()) { 
            Jugador jugador = new Jugador(
                0, 
                "Prueba Player", 
                "player@casino.com", 
                "pass123", 
                LocalDateTime.now(), 
                1000.0, // Saldo inicial
                0, 
                0.0, 
                1
            );
            gestor.registrarUsuario(jugador);
            System.out.println("Jugador de prueba registrado: " + jugador.getNombre());
        } else {
            System.out.println("Jugador de prueba ya existe.");
        }
    }

    
    private static void registrarJuegos(GestorCasino gestor) {
        System.out.println("\n--- 2. Registro de Juegos (Blackjack y HighLow) ---");
        
        Blackjack bj = new Blackjack(
            0, 
            "Blackjack Pro", 
            LocalDateTime.now(), 
            true, 
            4, 
            10.0, 
            1000.0
        );
        try {
            gestor.registrarJuego(bj);
        } catch (Exception e) {
            System.out.println("Blackjack Pro ya registrado."); 
        }
        
        HighLow hl = new HighLow(
            0, 
            "High Low Clásico", 
            LocalDateTime.now(), 
            true, 
            1, 
            5.0, 
            500.0
        );
         try {
            gestor.registrarJuego(hl);
        } catch (Exception e) {
            System.out.println("High Low Clásico ya registrado."); 
        }
    }
    
    private static void listarJuegosActivos(GestorCasino gestor) {
        System.out.println("\n--- 3. Listado de Juegos Activos ---");
        
        List<Juego> juegos = gestor.obtenerJuegosActivos();
        
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
                    tipoJuego, juego.getId(), juego.getNombre(), apuestaMin
                ));
            }
        }
    }
}