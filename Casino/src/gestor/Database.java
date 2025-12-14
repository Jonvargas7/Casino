package gestor;

import domain.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; 

public class Database {

    public static final String DB_NAME = "CasinoDB.db";
    public static final String URL = "jdbc:sqlite:" + DB_NAME;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public void inicializarBaseDatos() { 
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            String sqlUsuario = "CREATE TABLE IF NOT EXISTS Usuario (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," + 
                                "nombre TEXT NOT NULL," +
                                "email TEXT UNIQUE NOT NULL," +
                                "password TEXT NOT NULL," +
                                "fechaRegistro TEXT NOT NULL," +
                                "rol TEXT NOT NULL)";
            stmt.execute(sqlUsuario);

            String sqlJugador = "CREATE TABLE IF NOT EXISTS Jugador (" +
                                "id INTEGER UNIQUE NOT NULL," +
                                "saldo REAL NOT NULL," +
                                "numeroDePartidas INTEGER NOT NULL," +
                                "totalGanado REAL NOT NULL," +
                                "nivel INTEGER NOT NULL," +
                                "FOREIGN KEY(id) REFERENCES Usuario(id) ON DELETE CASCADE)"; 

            stmt.execute(sqlJugador);

            String sqlEmpleado = "CREATE TABLE IF NOT EXISTS Empleado (" +
                                 "id INTEGER UNIQUE NOT NULL," +
                                 "puesto TEXT NOT NULL," +
                                 "fechaInicio TEXT NOT NULL," +
                                 "activo BOOLEAN NOT NULL," +
                                 "FOREIGN KEY(id) REFERENCES Usuario(id) ON DELETE CASCADE)"; 
            stmt.execute(sqlEmpleado);
            
            String sqlJuego = "CREATE TABLE IF NOT EXISTS Juego (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT," + 
                              "nombre TEXT UNIQUE NOT NULL," +
                              "fechaCreacion TEXT NOT NULL," +
                              "activo BOOLEAN NOT NULL)";
            stmt.execute(sqlJuego);

            String sqlBlackjack = "CREATE TABLE IF NOT EXISTS Blackjack (" +
                                  "id INTEGER UNIQUE NOT NULL," +
                                  "mazos INTEGER NOT NULL," +
                                  "apuestaMin REAL NOT NULL," +
                                  "apuestaMax REAL NOT NULL," +
                                  "FOREIGN KEY(id) REFERENCES Juego(id) ON DELETE CASCADE)";
            stmt.execute(sqlBlackjack);
            
            String sqlHighLow = "CREATE TABLE IF NOT EXISTS HighLow (" +
                                "id INTEGER UNIQUE NOT NULL," +
                                "mazos INTEGER NOT NULL," +
                                "apuestaMin REAL NOT NULL," +
                                "apuestaMax REAL NOT NULL," +
                                "FOREIGN KEY(id) REFERENCES Juego(id) ON DELETE CASCADE)";
            stmt.execute(sqlHighLow);

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
    
    public Usuario loginUsuario(String email, String password, RolUsuario rol) {
        String sql = "SELECT id FROM Usuario WHERE email = ? AND password = ? AND rol = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            pstmt.setString(3, rol.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    return obtenerUsuarioPorId(id);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return null;
    }
    
    public boolean existeUsuario(String email) {
        String sql = "SELECT id FROM Usuario WHERE email = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar la existencia del usuario: " + e.getMessage());
            return true; 
        }
    }

    public boolean registrarUsuario(Usuario usuario) {
        if (existeUsuario(usuario.getEmail())) {
            System.err.println("Error al registrar el usuario: El email ya existe: " + usuario.getEmail());
            return false;
        }

        String sqlUsuario = "INSERT INTO Usuario (nombre, email, password, fechaRegistro, rol) VALUES (?, ?, ?, ?, ?)";
        long idGenerado = -1;
        Connection conn = null;

        try {
            conn = connect();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmt = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, usuario.getNombre());
                pstmt.setString(2, usuario.getEmail());
                pstmt.setString(3, usuario.getPassword()); 
                pstmt.setString(4, usuario.getFechaRegistro().format(FORMATTER));
                pstmt.setString(5, usuario.getRol().toString());

                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getLong(1);
                        usuario.setId(idGenerado); 
                    }
                }
            }

            boolean subtipoRegistrado = false;
            
            if (idGenerado > 0) {
                if (usuario instanceof Jugador) {
                    subtipoRegistrado = registrarJugador((Jugador) usuario, conn);
                } else if (usuario instanceof Administrador) {
                    subtipoRegistrado = registrarAdministrador((Administrador) usuario, conn);
                } else if (usuario instanceof Empleado) {
                    subtipoRegistrado = registrarEmpleado((Empleado) usuario, conn);
                }
            } else {
                 System.err.println("Error crítico: No se pudo obtener el ID autogenerado de la tabla Usuario.");
            }

            if (subtipoRegistrado) {
                conn.commit(); 
                return true;
            } else {
                conn.rollback(); 
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error al registrar el usuario en la tabla base o subtipo.");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Mensaje de Error DB: " + e.getMessage());
            try {
                if (conn != null) {
                    System.err.println("Haciendo Rollback de la transacción.");
                    conn.rollback();
                }
            } catch (SQLException rollbackErr) {
                System.err.println("Error crítico al hacer rollback: " + rollbackErr.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    private boolean registrarJugador(Jugador jugador, Connection conn) throws SQLException {
        String sqlJugador = "INSERT INTO Jugador (id, saldo, numeroDePartidas, totalGanado, nivel) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlJugador)) {
            pstmt.setLong(1, jugador.getId());
            pstmt.setDouble(2, jugador.getSaldo());
            pstmt.setInt(3, jugador.getNumeroDePartidas());
            pstmt.setDouble(4, jugador.getTotalGanado());
            pstmt.setInt(5, jugador.getNivel());
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }
    
    private boolean registrarAdministrador(Administrador admin, Connection conn) throws SQLException {
        return registrarEmpleado(admin, conn);
    }
    
    private boolean registrarEmpleado(Empleado empleado, Connection conn) throws SQLException {
        String sqlEmpleado = "INSERT INTO Empleado (id, puesto, fechaInicio, activo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlEmpleado)) {
            pstmt.setLong(1, empleado.getId());
            pstmt.setString(2, empleado.getPuesto());
            pstmt.setString(3, empleado.getFechaInicio().format(FORMATTER));
            pstmt.setBoolean(4, empleado.isActivo());
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }
    
    public List<Usuario> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nombre, email, password, fechaRegistro, rol FROM Usuario ORDER BY id";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String nombre = rs.getString("nombre");
                String email = rs.getString("email");
                String password = rs.getString("password");
                LocalDateTime fechaRegistro = LocalDateTime.parse(rs.getString("fechaRegistro"), FORMATTER);
                RolUsuario rol = RolUsuario.valueOf(rs.getString("rol"));

                Usuario usuario = null;
                switch (rol) {
                    case JUGADOR:
                        usuario = obtenerDetallesJugador(id, nombre, email, password, fechaRegistro);
                        break;
                    case EMPLEADO:
                        usuario = obtenerDetallesEmpleado(id, nombre, email, password, fechaRegistro, rol);
                        break;
                    case ADMINISTRADOR:
                        usuario = obtenerDetallesEmpleado(id, nombre, email, password, fechaRegistro, rol);
                        break;
                    default:
                        break;
                }
                
                if (usuario != null) {
                    usuarios.add(usuario);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todos los usuarios: " + e.getMessage());
        }
        return usuarios;
    }
    
    private Jugador obtenerDetallesJugador(long id, String nombre, String email, String password, LocalDateTime fechaRegistro) throws SQLException {
        String sql = "SELECT saldo, numeroDePartidas, totalGanado, nivel FROM Jugador WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double saldo = rs.getDouble("saldo");
                    int numeroDePartidas = rs.getInt("numeroDePartidas");
                    double totalGanado = rs.getDouble("totalGanado");
                    int nivel = rs.getInt("nivel");
                    return new Jugador(id, nombre, email, password, fechaRegistro, saldo, numeroDePartidas, totalGanado, nivel);
                }
            }
        }
        return null;
    }
    
    private Empleado obtenerDetallesEmpleado(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, RolUsuario rol) throws SQLException {
        String sql = "SELECT puesto, fechaInicio, activo FROM Empleado WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String puesto = rs.getString("puesto");
                    LocalDateTime fechaInicio = LocalDateTime.parse(rs.getString("fechaInicio"), FORMATTER);
                    boolean activo = rs.getBoolean("activo");
                    
                    if (rol == RolUsuario.ADMINISTRADOR) {
                        return new Administrador(id, nombre, email, password, fechaRegistro, puesto, fechaInicio, activo);
                    } else {
                        return new Empleado(id, nombre, email, password, fechaRegistro, puesto, fechaInicio, activo);
                    }
                }
            }
        }
        return null;
    }

    public Usuario obtenerUsuarioPorId(long id) {
        String sql = "SELECT nombre, email, password, fechaRegistro, rol FROM Usuario WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String email = rs.getString("email");
                    String password = rs.getString("password");
                    LocalDateTime fechaRegistro = LocalDateTime.parse(rs.getString("fechaRegistro"), FORMATTER);
                    RolUsuario rol = RolUsuario.valueOf(rs.getString("rol"));
                    
                    switch (rol) {
                        case JUGADOR:
                            return obtenerDetallesJugador(id, nombre, email, password, fechaRegistro);
                        case EMPLEADO:
                        case ADMINISTRADOR:
                            return obtenerDetallesEmpleado(id, nombre, email, password, fechaRegistro, rol);
                        default:
                            return new Usuario(id, nombre, email, password, fechaRegistro, rol) {};
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por ID: " + e.getMessage());
        }
        return null;
    }
    
    public boolean eliminarUsuario(long id) {
        String sqlUsuario = "DELETE FROM Usuario WHERE id = ?";
        String sqlJugador = "DELETE FROM Jugador WHERE id = ?";
        String sqlEmpleado = "DELETE FROM Empleado WHERE id = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtJ = conn.prepareStatement(sqlJugador)) {
                pstmtJ.setLong(1, id);
                pstmtJ.executeUpdate();
            }
            try (PreparedStatement pstmtE = conn.prepareStatement(sqlEmpleado)) {
                pstmtE.setLong(1, id);
                pstmtE.executeUpdate();
            }
            
            try (PreparedStatement pstmtU = conn.prepareStatement(sqlUsuario)) {
                pstmtU.setLong(1, id);
                int filasAfectadas = pstmtU.executeUpdate();

                if (filasAfectadas > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false; 
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al intentar eliminar el usuario con ID: " + id);
            System.err.println("Mensaje de Error DB: " + e.getMessage()); 
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackErr) {
                System.err.println("Error al hacer rollback: " + rollbackErr.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    public Optional<Juego> registrarJuego(Juego juego) {
        String sqlJuego = "INSERT INTO Juego (nombre, fechaCreacion, activo) VALUES (?, ?, ?)";
        long idGenerado = -1;
        Connection conn = null;
        
        try {
            conn = connect();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmt = conn.prepareStatement(sqlJuego, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, juego.getNombre());
                pstmt.setString(2, juego.getFechaCreacion().format(FORMATTER));
                pstmt.setBoolean(3, juego.isActivo());
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getLong(1);
                        juego.setId(idGenerado);
                    }
                }
            }

            boolean subtipoRegistrado = false;
            
            if (idGenerado > 0) {
                if (juego instanceof Blackjack) {
                    subtipoRegistrado = registrarBlackjack((Blackjack) juego, conn);
                } else if (juego instanceof HighLow) {
                    subtipoRegistrado = registrarHighLow((HighLow) juego, conn);
                }
            } else {
                System.err.println("Error crítico: No se pudo obtener el ID autogenerado de la tabla Juego.");
            }

            if (subtipoRegistrado) {
                conn.commit(); 
                return Optional.of(juego);
            } else {
                conn.rollback(); 
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            System.err.println("Error al registrar el juego: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackErr) {
                System.err.println("Error al hacer rollback: " + rollbackErr.getMessage());
            }
            return Optional.empty();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    private boolean registrarBlackjack(Blackjack blackjack, Connection conn) throws SQLException {
        String sqlBlackjack = "INSERT INTO Blackjack (id, mazos, apuestaMin, apuestaMax) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlBlackjack)) {
            pstmt.setLong(1, blackjack.getId());
            pstmt.setInt(2, blackjack.getMazos());
            pstmt.setDouble(3, blackjack.getApuestaMin());
            pstmt.setDouble(4, blackjack.getApuestaMax());
            return pstmt.executeUpdate() > 0;
        }
    }
    
    private boolean registrarHighLow(HighLow highLow, Connection conn) throws SQLException {
        String sqlHighLow = "INSERT INTO HighLow (id, mazos, apuestaMin, apuestaMax) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlHighLow)) {
            pstmt.setLong(1, highLow.getId());
            pstmt.setInt(2, highLow.getMazos());
            pstmt.setDouble(3, highLow.getApuestaMin());
            pstmt.setDouble(4, highLow.getApuestaMax());
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Juego> obtenerJuegosActivos() {
        List<Juego> juegos = new ArrayList<>();
        String sql = "SELECT id, nombre, fechaCreacion, activo FROM Juego WHERE activo = 1 ORDER BY id";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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
            System.err.println("Error al obtener juegos activos: " + e.getMessage());
        }
        return juegos;
    }
}