package gestor;

import domain.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Database {

    
    public static final String DB_NAME = "CasinoDB.db";
    public static final String URL = "jdbc:sqlite:" + DB_NAME;
    // CORRECCIÓN: Cambiado a public para ser visible en otras clases
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Establece la conexión con la base de datos SQLite.
     * @return Objeto Connection.
     * @throws SQLException Si ocurre un error de conexión.
     */
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    
    /**
     * Inicializa la base de datos creando todas las tablas si no existen.
     */
    public void inicializarBaseDatos() { 
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // 1. Tabla Usuario (PADRE)
            String sqlUsuario = "CREATE TABLE IF NOT EXISTS Usuario (" +
                                "id INTEGER PRIMARY KEY," + 
                                "nombre TEXT NOT NULL," +
                                "email TEXT UNIQUE NOT NULL," +
                                "password TEXT NOT NULL," +
                                "fechaRegistro TEXT NOT NULL," +
                                "rol TEXT NOT NULL)"; 

            // 2. Tabla Administrador (HIJA)
            String sqlAdministrador = "CREATE TABLE IF NOT EXISTS Administrador (" +
                                      "id INTEGER PRIMARY KEY," +
                                      "nivelAcceso INTEGER NOT NULL," +
                                      "FOREIGN KEY(id) REFERENCES Usuario(id) ON DELETE CASCADE)";

            // 3. Tabla Jugador (HIJA)
            String sqlJugador = "CREATE TABLE IF NOT EXISTS Jugador (" +
                                "id INTEGER PRIMARY KEY," +
                                "saldo REAL NOT NULL," +
                                "numeroDePartidas INTEGER NOT NULL," +
                                "totalGanado REAL NOT NULL," +
                                "nivel INTEGER NOT NULL," +
                                "FOREIGN KEY(id) REFERENCES Usuario(id) ON DELETE CASCADE)";

            // 4. Tabla Empleado (HIJA)
            String sqlEmpleado = "CREATE TABLE IF NOT EXISTS Empleado (" +
                                  "id INTEGER PRIMARY KEY," + 
                                  "puesto TEXT NOT NULL," +
                                  "activo BOOLEAN NOT NULL CHECK (activo IN (0, 1))," + 
                                  "fechaInicio TEXT NOT NULL," +
                                  "FOREIGN KEY (id) REFERENCES Usuario(id) ON DELETE CASCADE)";


            // 5. Tabla Juego (PADRE)
            String sqlJuego = "CREATE TABLE IF NOT EXISTS Juego (" +
                              "id INTEGER PRIMARY KEY," +
                              "nombre TEXT UNIQUE NOT NULL," +
                              "fechaCreacion TEXT NOT NULL," +
                              "activo INTEGER NOT NULL)";

            // 6. Tabla Blackjack (HIJA)
            String sqlBlackjack = "CREATE TABLE IF NOT EXISTS Blackjack (" +
                                  "id INTEGER PRIMARY KEY," +
                                  "mazos INTEGER NOT NULL," +
                                  "apuestaMin REAL NOT NULL," +
                                  "apuestaMax REAL NOT NULL," +
                                  "FOREIGN KEY(id) REFERENCES Juego(id) ON DELETE CASCADE)";

            // 7. Tabla HighLow (HIJA)
            String sqlHighLow = "CREATE TABLE IF NOT EXISTS HighLow (" +
                                "id INTEGER PRIMARY KEY," +
                                "mazos INTEGER NOT NULL," +
                                "apuestaMin REAL NOT NULL," +
                                "apuestaMax REAL NOT NULL," +
                                "FOREIGN KEY(id) REFERENCES Juego(id) ON DELETE CASCADE)";
            
            
            stmt.executeUpdate(sqlUsuario);
            stmt.executeUpdate(sqlAdministrador);
            stmt.executeUpdate(sqlJugador);
            stmt.executeUpdate(sqlEmpleado); 
            stmt.executeUpdate(sqlJuego);
            stmt.executeUpdate(sqlBlackjack);
            stmt.executeUpdate(sqlHighLow);
            
            System.out.println("Tablas de la base de datos verificadas o creadas correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
    
    
    /**
     * Registra un usuario en la tabla principal y en la tabla hija correspondiente (Administrador, Jugador, Empleado).
     */
    public void registrar(Usuario usuario) throws SQLException {
        
        
        String sqlUsuario = "INSERT INTO Usuario(nombre, email, password, fechaRegistro, rol) VALUES(?, ?, ?, ?, ?)";
        long idUsuario = -1;

        try (Connection conn = connect();
             PreparedStatement pstmtUsuario = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false); 

            pstmtUsuario.setString(1, usuario.getNombre());
            pstmtUsuario.setString(2, usuario.getEmail());
            pstmtUsuario.setString(3, usuario.getPassword());
            pstmtUsuario.setString(4, usuario.getFechaRegistro().format(FORMATTER));
            pstmtUsuario.setString(5, usuario.getRol().name());
            pstmtUsuario.executeUpdate();

            
            try (ResultSet rs = pstmtUsuario.getGeneratedKeys()) {
                if (rs.next()) {
                    idUsuario = rs.getLong(1);
                    usuario.setId(idUsuario); 
                }
            }
            
            
            if (usuario instanceof Administrador) {
                Administrador admin = (Administrador) usuario;
                String sqlAdmin = "INSERT INTO Administrador(id, nivelAcceso) VALUES(?, ?)";
                try (PreparedStatement pstmtAdmin = conn.prepareStatement(sqlAdmin)) {
                    pstmtAdmin.setLong(1, idUsuario);
                    pstmtAdmin.setInt(2, admin.getNivelAcceso());
                    pstmtAdmin.executeUpdate();
                }
            } else if (usuario instanceof Jugador) {
                Jugador jugador = (Jugador) usuario;
                String sqlJugador = "INSERT INTO Jugador(id, saldo, numeroDePartidas, totalGanado, nivel) VALUES(?, ?, ?, ?, ?)";
                try (PreparedStatement pstmtJugador = conn.prepareStatement(sqlJugador)) {
                    pstmtJugador.setLong(1, idUsuario);
                    pstmtJugador.setDouble(2, jugador.getSaldo());
                    pstmtJugador.setInt(3, jugador.getNumeroDePartidas());
                    pstmtJugador.setDouble(4, jugador.getTotalGanado());
                    pstmtJugador.setInt(5, jugador.getNivel());
                    pstmtJugador.executeUpdate();
                }
            } else if (usuario instanceof Empleado) { 
                Empleado empleado = (Empleado) usuario;
                String sqlEmpleado = "INSERT INTO Empleado(id, puesto, activo, fechaInicio) VALUES(?, ?, ?, ?)";
                try (PreparedStatement pstmtEmpleado = conn.prepareStatement(sqlEmpleado)) {
                    pstmtEmpleado.setLong(1, idUsuario);
                    pstmtEmpleado.setString(2, empleado.getPuesto());
                    pstmtEmpleado.setBoolean(3, empleado.isActivo());
                    pstmtEmpleado.setString(4, empleado.getFechaInicio().format(FORMATTER));
                    pstmtEmpleado.executeUpdate();
                }
            }

            
            conn.commit(); 
            System.out.println(usuario.getClass().getSimpleName() + " registrado con éxito. ID: " + idUsuario);

        } catch (SQLException e) {
            
            try (Connection conn = connect()) {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                // Manejo de error si el rollback falla
            }
            throw new SQLException("Error al registrar usuario: " + e.getMessage());
        }
    }

    
    /**
     * Intenta autenticar a un usuario con un rol específico y carga los datos de la tabla hija.
     */
    public Usuario login(String email, String password, String rolString) {
        
        String sql = "SELECT id, nombre, fechaRegistro, rol FROM Usuario WHERE email = ? AND password = ? AND rol = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            pstmt.setString(3, rolString);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String nombre = rs.getString("nombre");
                    String userPassword = password; 
                    LocalDateTime fechaRegistro = LocalDateTime.parse(rs.getString("fechaRegistro"), FORMATTER);
                    RolUsuario rol = RolUsuario.valueOf(rs.getString("rol"));
                    
                    // Cargar datos específicos del hijo
                    if (rol == RolUsuario.ADMINISTRADOR) {
                        String sqlAdmin = "SELECT nivelAcceso FROM Administrador WHERE id = ?";
                        try (PreparedStatement pstmtAdmin = conn.prepareStatement(sqlAdmin)) {
                            pstmtAdmin.setLong(1, id);
                            try (ResultSet rsAdmin = pstmtAdmin.executeQuery()) {
                                if (rsAdmin.next()) {
                                    int nivelAcceso = rsAdmin.getInt("nivelAcceso");
                                    return new Administrador(id, nombre, email, userPassword, fechaRegistro, nivelAcceso);
                                }
                            }
                        }
                    } else if (rol == RolUsuario.JUGADOR) {
                        String sqlJugador = "SELECT saldo, numeroDePartidas, totalGanado, nivel FROM Jugador WHERE id = ?";
                         try (PreparedStatement pstmtJugador = conn.prepareStatement(sqlJugador)) {
                            pstmtJugador.setLong(1, id);
                            try (ResultSet rsJugador = pstmtJugador.executeQuery()) {
                                if (rsJugador.next()) {
                                    double saldo = rsJugador.getDouble("saldo");
                                    int numPartidas = rsJugador.getInt("numeroDePartidas");
                                    double totalGanado = rsJugador.getDouble("totalGanado");
                                    int nivel = rsJugador.getInt("nivel");
                                    return new Jugador(id, nombre, email, userPassword, fechaRegistro, saldo, numPartidas, totalGanado, nivel);
                                }
                            }
                        }
                    } else if (rol == RolUsuario.EMPLEADO) { 
                        String sqlEmpleado = "SELECT puesto, activo, fechaInicio FROM Empleado WHERE id = ?";
                        try (PreparedStatement pstmtEmpleado = conn.prepareStatement(sqlEmpleado)) {
                            pstmtEmpleado.setLong(1, id);
                            try (ResultSet rsEmpleado = pstmtEmpleado.executeQuery()) {
                                if (rsEmpleado.next()) {
                                    String puesto = rsEmpleado.getString("puesto");
                                    boolean activo = rsEmpleado.getBoolean("activo");
                                    LocalDateTime fechaInicio = LocalDateTime.parse(rsEmpleado.getString("fechaInicio"), FORMATTER);
                                    
                                    // Usar el constructor completo de Empleado
                                    return new Empleado(id, nombre, email, userPassword, fechaRegistro, puesto, activo, fechaInicio);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de base de datos durante el login: " + e.getMessage());
        }
        return null; 
    }
    
    /**
     * Obtiene una lista de todos los usuarios registrados en la base de datos (Admin, Jugador, Empleado).
     */
    public List<Usuario> getTodosLosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nombre, email, password, fechaRegistro, rol FROM Usuario";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String nombre = rs.getString("nombre");
                String email = rs.getString("email");
                String password = rs.getString("password");
                LocalDateTime fechaReg = LocalDateTime.parse(rs.getString("fechaRegistro"), FORMATTER);
                RolUsuario rolEnum = RolUsuario.valueOf(rs.getString("rol"));
                
                if (rolEnum == RolUsuario.JUGADOR) {
                    usuarios.add(getJugadorById(id, nombre, email, password, fechaReg));
                } else if (rolEnum == RolUsuario.EMPLEADO) {
                    usuarios.add(getEmpleadoById(id, nombre, email, password, fechaReg));
                } else if (rolEnum == RolUsuario.ADMINISTRADOR) {
                    usuarios.add(getAdministradorById(id, nombre, email, password, fechaReg));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en Database.getTodosLosUsuarios: " + e.getMessage());
        }
        return usuarios;
    }

    // Métodos auxiliares para obtener subclases (Implementación asumida)
    private Jugador getJugadorById(long id, String nombre, String email, String password, LocalDateTime fechaReg) throws SQLException {
        // [Implementación para leer de la tabla Jugador]
        return new Jugador(id, nombre, email, password, fechaReg, 0.0, 0, 0.0, 0); // Placeholder
    }
    private Empleado getEmpleadoById(long id, String nombre, String email, String password, LocalDateTime fechaReg) throws SQLException {
        // [Implementación para leer de la tabla Empleado]
        return new Empleado(id, nombre, email, password, fechaReg, "Cajero", true, LocalDateTime.now()); // Placeholder
    }
    private Administrador getAdministradorById(long id, String nombre, String email, String password, LocalDateTime fechaReg) throws SQLException {
        // [Implementación para leer de la tabla Administrador]
        return new Administrador(id, nombre, email, password, fechaReg, 1); // Placeholder
    }


    
    public boolean existeJuegoPorNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM Juego WHERE nombre = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia del juego: " + e.getMessage());
        }
        return false;
    }

    
    public void registrarJuego(Juego juego) throws SQLException {
        if (existeJuegoPorNombre(juego.getNombre())) {
            
            System.out.println("El juego '" + juego.getNombre() + "' ya existe. No se registrará de nuevo.");
            return;
        }

        String sqlJuego = "INSERT INTO Juego(nombre, fechaCreacion, activo) VALUES(?, ?, ?)";
        long idJuego = -1;

        try (Connection conn = connect();
             PreparedStatement pstmtJuego = conn.prepareStatement(sqlJuego, Statement.RETURN_GENERATED_KEYS)) {
            
            conn.setAutoCommit(false); 

            pstmtJuego.setString(1, juego.getNombre());
            pstmtJuego.setString(2, juego.getFechaCreacion().format(FORMATTER));
            pstmtJuego.setBoolean(3, juego.isActivo());
            pstmtJuego.executeUpdate();

            
            try (ResultSet rs = pstmtJuego.getGeneratedKeys()) {
                if (rs.next()) {
                    idJuego = rs.getLong(1);
                    juego.setId(idJuego); 
                }
            }

            
            if (juego instanceof Blackjack) {
                Blackjack bj = (Blackjack) juego;
                String sqlBj = "INSERT INTO Blackjack(id, mazos, apuestaMin, apuestaMax) VALUES(?, ?, ?, ?)";
                try (PreparedStatement pstmtBj = conn.prepareStatement(sqlBj)) {
                    pstmtBj.setLong(1, idJuego);
                    pstmtBj.setInt(2, bj.getMazos());
                    pstmtBj.setDouble(3, bj.getApuestaMin());
                    pstmtBj.setDouble(4, bj.getApuestaMax());
                    pstmtBj.executeUpdate();
                }
            } else if (juego instanceof HighLow) {
                HighLow hl = (HighLow) juego;
                String sqlHl = "INSERT INTO HighLow(id, mazos, apuestaMin, apuestaMax) VALUES(?, ?, ?, ?)";
                try (PreparedStatement pstmtHl = conn.prepareStatement(sqlHl)) {
                    pstmtHl.setLong(1, idJuego);
                    pstmtHl.setInt(2, hl.getMazos());
                    pstmtHl.setDouble(3, hl.getApuestaMin());
                    pstmtHl.setDouble(4, hl.getApuestaMax());
                    pstmtHl.executeUpdate();
                }
            }
            
            conn.commit();
            System.out.println("Juego '" + juego.getNombre() + "' registrado con éxito. ID: " + idJuego);

        } catch (SQLException e) {
            try (Connection conn = connect()) {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                // Manejo de error si el rollback falla
            }
            throw new SQLException("Error al registrar juego: " + e.getMessage());
        }
    }
    

    
    public List<Juego> obtenerJuegosActivos() {
        List<Juego> juegos = new ArrayList<>();
        String sql = "SELECT id, nombre, fechaCreacion, activo FROM Juego WHERE activo = 1";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String nombre = rs.getString("nombre");
                LocalDateTime fechaCreacion = LocalDateTime.parse(rs.getString("fechaCreacion"), FORMATTER);
                boolean activo = rs.getBoolean("activo");
                
                
                
                
                String sqlBlackjack = "SELECT mazos, apuestaMin, apuestaMax FROM Blackjack WHERE id = ?";
                try (PreparedStatement pstmtBj = conn.prepareStatement(sqlBlackjack)) {
                    pstmtBj.setLong(1, id);
                    try (ResultSet rsBj = pstmtBj.executeQuery()) {
                        if (rsBj.next()) {
                            int mazos = rsBj.getInt("mazos");
                            double apuestaMin = rsBj.getDouble("apuestaMin");
                            double apuestaMax = rsBj.getDouble("apuestaMax");
                            juegos.add(new Blackjack(id, nombre, fechaCreacion, activo, mazos, apuestaMin, apuestaMax));
                            continue; 
                        }
                    }
                }
                
                
                String sqlHighLow = "SELECT mazos, apuestaMin, apuestaMax FROM HighLow WHERE id = ?";
                try (PreparedStatement pstmtHl = conn.prepareStatement(sqlHighLow)) {
                    pstmtHl.setLong(1, id);
                    try (ResultSet rsHl = pstmtHl.executeQuery()) {
                        if (rsHl.next()) {
                            int mazos = rsHl.getInt("mazos");
                            double apuestaMin = rsHl.getDouble("apuestaMin");
                            double apuestaMax = rsHl.getDouble("apuestaMax");
                            juegos.add(new HighLow(id, nombre, fechaCreacion, activo, mazos, apuestaMin, apuestaMax));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener juegos activos: " + e.getMessage());
        }
        return juegos;
    }
}