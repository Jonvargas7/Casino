package gestor;

import domain.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; 

public class Database {

    // Constantes de la Base de Datos
    public static final String DB_NAME = "CasinoDB.db";
    public static final String URL = "jdbc:sqlite:" + DB_NAME;
    // Público para ser usado en VentanaGestionUsuarios y VentanaRegistro
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
            stmt.execute(sqlUsuario);

            // 2. Tabla Jugador (HIJA)
            String sqlJugador = "CREATE TABLE IF NOT EXISTS Jugador (" +
                                "id INTEGER PRIMARY KEY," +
                                "saldo REAL NOT NULL," +
                                "numeroDePartidas INTEGER NOT NULL," +
                                "totalGanado REAL NOT NULL," +
                                "nivel INTEGER NOT NULL," +
                                "FOREIGN KEY(id) REFERENCES Usuario(id) ON DELETE CASCADE)";
            stmt.execute(sqlJugador);

            // 3. Tabla Empleado (HIJA de Usuario, incluye Admin)
            String sqlEmpleado = "CREATE TABLE IF NOT EXISTS Empleado (" +
                                 "id INTEGER PRIMARY KEY," +
                                 "puesto TEXT NOT NULL," +
                                 "activo BOOLEAN NOT NULL," +
                                 "fechaInicio TEXT NOT NULL," +
                                 "FOREIGN KEY(id) REFERENCES Usuario(id) ON DELETE CASCADE)";
            stmt.execute(sqlEmpleado);

            // 4. Tabla Juego (PADRE)
            String sqlJuego = "CREATE TABLE IF NOT EXISTS Juego (" +
                              "id INTEGER PRIMARY KEY," + 
                              "nombre TEXT UNIQUE NOT NULL," +
                              "fechaCreacion TEXT NOT NULL," +
                              "activo BOOLEAN NOT NULL)";
            stmt.execute(sqlJuego);

            // 5. Tabla Blackjack (HIJA de Juego)
            String sqlBlackjack = "CREATE TABLE IF NOT EXISTS Blackjack (" +
                                  "id INTEGER PRIMARY KEY," +
                                  "mazos INTEGER NOT NULL," +
                                  "apuestaMin REAL NOT NULL," +
                                  "apuestaMax REAL NOT NULL," +
                                  "FOREIGN KEY(id) REFERENCES Juego(id) ON DELETE CASCADE)";
            stmt.execute(sqlBlackjack);

            // 6. Tabla HighLow (HIJA de Juego)
            String sqlHighLow = "CREATE TABLE IF NOT EXISTS HighLow (" +
                                "id INTEGER PRIMARY KEY," +
                                "mazos INTEGER NOT NULL," +
                                "apuestaMin REAL NOT NULL," +
                                "apuestaMax REAL NOT NULL," +
                                "FOREIGN KEY(id) REFERENCES Juego(id) ON DELETE CASCADE)";
            stmt.execute(sqlHighLow);

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }


    // =========================================================
    // MÉTODOS DE GESTIÓN DE USUARIO (CRUD y Login)
    // =========================================================

    /**
     * Registra un nuevo usuario (Jugador, Empleado o Administrador) en la BD.
     * @param usuario El objeto Usuario a registrar.
     * @throws SQLException Si el email ya existe o hay un error de DB.
     */
    public void registrar(Usuario usuario) throws SQLException {
        Connection conn = connect();
        conn.setAutoCommit(false); // Iniciar transacción

        try {
            // 1. Inserción en tabla Usuario (PADRE)
            String sqlUsuario = "INSERT INTO Usuario (nombre, email, password, fechaRegistro, rol) VALUES (?, ?, ?, ?, ?)";
            
            // Usar Statement.RETURN_GENERATED_KEYS para obtener el ID
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, usuario.getNombre());
                pstmt.setString(2, usuario.getEmail());
                pstmt.setString(3, usuario.getPassword());
                pstmt.setString(4, usuario.getFechaRegistro().format(FORMATTER));
                pstmt.setString(5, usuario.getRol().toString());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long nuevoId = rs.getLong(1);
                        usuario.setId(nuevoId); // Asignar el ID generado al objeto
                        
                        // 2. Inserción en tabla específica (HIJA)
                        if (usuario instanceof Jugador) {
                            registrarJugadorEspecifico(conn, (Jugador) usuario);
                        } else if (usuario instanceof Empleado) {
                            registrarEmpleadoEspecifico(conn, (Empleado) usuario);
                        }
                    }
                }
            }
            conn.commit(); // Confirmar transacción
        } catch (SQLException e) {
            conn.rollback(); // Deshacer en caso de error
            throw new SQLException("Error al registrar el usuario: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    // --- Métodos Auxiliares de Registro ---

    private void registrarJugadorEspecifico(Connection conn, Jugador jugador) throws SQLException {
        String sql = "INSERT INTO Jugador (id, saldo, numeroDePartidas, totalGanado, nivel) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, jugador.getId());
            pstmt.setDouble(2, jugador.getSaldo());
            pstmt.setInt(3, jugador.getNumeroDePartidas());
            pstmt.setDouble(4, jugador.getTotalGanado());
            pstmt.setInt(5, jugador.getNivel());
            pstmt.executeUpdate();
        }
    }

    private void registrarEmpleadoEspecifico(Connection conn, Empleado empleado) throws SQLException {
        String sql = "INSERT INTO Empleado (id, puesto, activo, fechaInicio) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, empleado.getId());
            pstmt.setString(2, empleado.getPuesto());
            pstmt.setBoolean(3, empleado.isActivo());
            pstmt.setString(4, empleado.getFechaInicio().format(FORMATTER));
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Intenta autenticar a un usuario.
     * @param email Email del usuario.
     * @param password Contraseña.
     * @param rol Rol esperado (Jugador, Empleado, etc.).
     * @return Objeto Usuario completo si las credenciales son válidas y el rol coincide, o null en caso contrario.
     */
    public Usuario login(String email, String password, RolUsuario rol) {
        // En un sistema real, la contraseña no se almacenaría en texto plano.
        String sql = "SELECT id FROM Usuario WHERE email = ? AND password = ? AND rol = ?";
        long id = -1;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            pstmt.setString(3, rol.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getLong("id");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en el login: " + e.getMessage());
            return null;
        }

        if (id != -1) {
            return obtenerUsuarioPorId(id);
        }
        return null;
    }

    /**
     * Obtiene una lista de todos los usuarios registrados, cargando sus datos específicos.
     * @return Lista de objetos Usuario.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id FROM Usuario";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                long id = rs.getLong("id");
                // Llama al método que carga el objeto completo por ID
                Usuario u = obtenerUsuarioPorId(id);
                if (u != null) {
                    usuarios.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los usuarios: " + e.getMessage());
        }
        return usuarios;
    }
    
    /**
     * Obtiene un usuario (Jugador, Empleado, Administrador) por su ID.
     * **ESTE ES EL MÉTODO CLAVE para la VentanaGestionUsuarios.**
     * @param id El ID del usuario.
     * @return El objeto Usuario completo o null si no se encuentra.
     */
    public Usuario obtenerUsuarioPorId(long id) {
        String sqlUsuario = "SELECT nombre, email, password, fechaRegistro, rol FROM Usuario WHERE id = ?";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlUsuario)) {
            
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Datos base de la tabla Usuario
                    String nombre = rs.getString("nombre");
                    String email = rs.getString("email");
                    String password = rs.getString("password");
                    LocalDateTime fechaRegistro = LocalDateTime.parse(rs.getString("fechaRegistro"), FORMATTER);
                    String rolStr = rs.getString("rol");
                    
                    RolUsuario rol = RolUsuario.valueOf(rolStr.toUpperCase());
                    
                    // Cargar datos específicos según el rol
                    if (rol == RolUsuario.JUGADOR) {
                        return obtenerJugadorEspecifico(conn, id, nombre, email, password, fechaRegistro);
                    } else { // EMPLEADO o ADMINISTRADOR
                        return obtenerEmpleadoEspecifico(conn, id, nombre, email, password, fechaRegistro, rol);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Método auxiliar para cargar los datos de Jugador.
     */
    private Jugador obtenerJugadorEspecifico(Connection conn, long id, String nombre, String email, String password, LocalDateTime fechaRegistro) throws SQLException {
        String sqlJugador = "SELECT saldo, numeroDePartidas, totalGanado, nivel FROM Jugador WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sqlJugador)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double saldo = rs.getDouble("saldo");
                    int numeroDePartidas = rs.getInt("numeroDePartidas");
                    double totalGanado = rs.getDouble("totalGanado");
                    int nivel = rs.getInt("nivel");
                    
                    return new Jugador(id, nombre, email, password, fechaRegistro, 
                                       saldo, numeroDePartidas, totalGanado, nivel);
                }
            }
        }
        return null; 
    }


    /**
     * Método auxiliar para cargar los datos de Empleado o Administrador.
     * Se ajusta para usar la firma de constructor correcta.
     */
    private Empleado obtenerEmpleadoEspecifico(Connection conn, long id, String nombre, String email, String password, LocalDateTime fechaRegistro, RolUsuario rol) throws SQLException {
        String sqlEmpleado = "SELECT puesto, activo, fechaInicio FROM Empleado WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sqlEmpleado)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String puesto = rs.getString("puesto");
                    // Nota: Los datos se cargan en el orden: puesto, activo, fechaInicio
                    boolean activo = rs.getBoolean("activo");
                    LocalDateTime fechaInicio = LocalDateTime.parse(rs.getString("fechaInicio"), FORMATTER);
                    
                    if (rol == RolUsuario.ADMINISTRADOR) {
                        return new Administrador(id, nombre, email, password, fechaRegistro, 
                                                puesto, fechaInicio, activo); 
                    } else {
                        return new Empleado(id, nombre, email, password, fechaRegistro, 
                                            puesto, fechaInicio, activo);
                    }
                }
            }
        }
        return null; 
    }
    
    // =========================================================
    // WRAPPER DE GUARDADO (NUEVO MÉTODO)
    // =========================================================
    /**
     * Guarda un usuario. Si tiene ID > 0, lo actualiza. Si es nuevo (ID <= 0), lo registra.
     * Este método es usado por VentanaFormularioUsuario.java.
     * @param usuario El objeto Usuario a guardar.
     * @throws Exception Si ocurre un error al registrar/actualizar.
     */
    public void guardarUsuario(Usuario usuario) throws SQLException {
        if (usuario.getId() > 0) {
            // Edición: Llama al método existente de actualización.
            actualizarUsuario(usuario);
        } else {
            // Añadir: Llama al método existente de registro.
            registrar(usuario);
        }
    }


    /**
     * Actualiza la información de un usuario (base y específica).
     * Nota: No permite cambiar el email ni la fecha de registro en esta versión.
     * @param usuario El objeto Usuario con los datos actualizados.
     */
    public void actualizarUsuario(Usuario usuario) throws SQLException {
        Connection conn = connect();
        conn.setAutoCommit(false); 

        try {
            // 1. Actualizar tabla Usuario (PADRE)
            String sqlUsuario = "UPDATE Usuario SET nombre = ?, password = ?, rol = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUsuario)) {
                pstmt.setString(1, usuario.getNombre());
                pstmt.setString(2, usuario.getPassword());
                pstmt.setString(3, usuario.getRol().toString());
                pstmt.setLong(4, usuario.getId());
                pstmt.executeUpdate();
            }

            // 2. Actualizar tabla específica (HIJA)
            if (usuario instanceof Jugador) {
                actualizarJugadorEspecifico(conn, (Jugador) usuario);
            } else if (usuario instanceof Empleado) {
                actualizarEmpleadoEspecifico(conn, (Empleado) usuario);
            }
            conn.commit(); 
        } catch (SQLException e) {
            conn.rollback(); 
            throw new SQLException("Error al actualizar el usuario: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void actualizarJugadorEspecifico(Connection conn, Jugador jugador) throws SQLException {
        String sql = "UPDATE Jugador SET saldo = ?, numeroDePartidas = ?, totalGanado = ?, nivel = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, jugador.getSaldo());
            pstmt.setInt(2, jugador.getNumeroDePartidas());
            pstmt.setDouble(3, jugador.getTotalGanado());
            pstmt.setInt(4, jugador.getNivel());
            pstmt.setLong(5, jugador.getId());
            pstmt.executeUpdate();
        }
    }
    
    private void actualizarEmpleadoEspecifico(Connection conn, Empleado empleado) throws SQLException {
        String sql = "UPDATE Empleado SET puesto = ?, activo = ?, fechaInicio = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empleado.getPuesto());
            pstmt.setBoolean(2, empleado.isActivo());
            pstmt.setString(3, empleado.getFechaInicio().format(FORMATTER));
            pstmt.setLong(4, empleado.getId());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Actualiza el saldo de un jugador y opcionalmente sus estadísticas.
     * @param jugador El objeto Jugador.
     * @param cambioSaldo La cantidad a sumar o restar.
     * @param actualizarEstadisticas Si es true, incrementa el número de partidas.
     */
    public void actualizarSaldoJugador(Jugador jugador, double cambioSaldo, boolean actualizarEstadisticas) throws SQLException {
        // En una aplicación real, esta lógica sería más compleja y se registrarían movimientos.
        double nuevoSaldo = jugador.getSaldo() + cambioSaldo;
        if (nuevoSaldo < 0) {
            throw new SQLException("El saldo no puede ser negativo.");
        }
        jugador.setSaldo(nuevoSaldo);

        // Si es una partida ganada, actualizar total ganado (simplificado)
        if (cambioSaldo > 0) {
            jugador.setTotalGanado(jugador.getTotalGanado() + cambioSaldo);
        }

        if (actualizarEstadisticas) {
            jugador.setNumeroDePartidas(jugador.getNumeroDePartidas() + 1);
        }

        // Simplemente llamamos al método de actualización genérico para persistir los cambios
        actualizarJugadorEspecifico(connect(), jugador); 
    }

    // Método para simplificar la actualización de saldo sin cambiar estadísticas
    public void actualizarSaldoJugador(Jugador jugador, double cambioSaldo) throws SQLException {
        actualizarSaldoJugador(jugador, cambioSaldo, false);
    }
    
    /**
     * Elimina un usuario por su ID. Las claves foráneas se encargarán de eliminar 
     * el registro de las tablas hijas (Jugador/Empleado).
     * @param id ID del usuario a eliminar.
     * @throws SQLException Si ocurre un error de DB.
     */
    public void eliminarUsuario(long id) throws SQLException {
        String sql = "DELETE FROM Usuario WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se encontró ningún usuario con ID: " + id);
            }
        }
    }


    // =========================================================
    // MÉTODOS DE GESTIÓN DE JUEGO (CRUD)
    // =========================================================
    
    /**
     * Registra un nuevo juego.
     * @param juego El objeto Juego a registrar.
     * @throws SQLException Si el nombre del juego ya existe o hay un error de DB.
     */
    public void registrarJuego(Juego juego) throws SQLException {
        Connection conn = connect();
        conn.setAutoCommit(false);

        try {
            // 1. Inserción en tabla Juego (PADRE)
            String sqlJuego = "INSERT INTO Juego (nombre, fechaCreacion, activo) VALUES (?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlJuego, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, juego.getNombre());
                pstmt.setString(2, juego.getFechaCreacion().format(FORMATTER));
                pstmt.setBoolean(3, juego.isActivo());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long nuevoId = rs.getLong(1);
                        juego.setId(nuevoId); 

                        // 2. Inserción en tabla específica (HIJA)
                        if (juego instanceof Blackjack) {
                            registrarBlackjackEspecifico(conn, (Blackjack) juego);
                        } else if (juego instanceof HighLow) {
                            registrarHighLowEspecifico(conn, (HighLow) juego);
                        }
                    }
                }
            }
            conn.commit(); 
        } catch (SQLException e) {
            conn.rollback(); 
            throw new SQLException("Error al registrar el juego: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    private void registrarBlackjackEspecifico(Connection conn, Blackjack blackjack) throws SQLException {
        String sql = "INSERT INTO Blackjack (id, mazos, apuestaMin, apuestaMax) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, blackjack.getId());
            pstmt.setInt(2, blackjack.getMazos());
            pstmt.setDouble(3, blackjack.getApuestaMin());
            pstmt.setDouble(4, blackjack.getApuestaMax());
            pstmt.executeUpdate();
        }
    }

    private void registrarHighLowEspecifico(Connection conn, HighLow highLow) throws SQLException {
        String sql = "INSERT INTO HighLow (id, mazos, apuestaMin, apuestaMax) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, highLow.getId());
            pstmt.setInt(2, highLow.getMazos());
            pstmt.setDouble(3, highLow.getApuestaMin());
            pstmt.setDouble(4, highLow.getApuestaMax());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Obtiene todos los juegos marcados como activos.
     * @return Lista de objetos Juego.
     */
    public List<Juego> obtenerJuegosActivos() {
        List<Juego> juegos = new ArrayList<>();
        String sqlJuego = "SELECT id, nombre, fechaCreacion FROM Juego WHERE activo = TRUE";
        
        try (Connection conn = connect();
             PreparedStatement pstmtJuego = conn.prepareStatement(sqlJuego);
             ResultSet rsJuego = pstmtJuego.executeQuery()) {
            
            while (rsJuego.next()) {
                long id = rsJuego.getLong("id");
                String nombre = rsJuego.getString("nombre");
                LocalDateTime fechaCreacion = LocalDateTime.parse(rsJuego.getString("fechaCreacion"), FORMATTER);
                
                // Asumimos que activo es TRUE por el WHERE clause
                boolean activo = true; 
                
                // 1. Intentar cargar como Blackjack
                String sqlBlackjack = "SELECT mazos, apuestaMin, apuestaMax FROM Blackjack WHERE id = ?";
                try (PreparedStatement pstmtBj = conn.prepareStatement(sqlBlackjack)) {
                    pstmtBj.setLong(1, id);
                    try (ResultSet rsBj = pstmtBj.executeQuery()) {
                        if (rsBj.next()) {
                            int mazos = rsBj.getInt("mazos");
                            double apuestaMin = rsBj.getDouble("apuestaMin");
                            double apuestaMax = rsBj.getDouble("apuestaMax");
                            juegos.add(new Blackjack(id, nombre, fechaCreacion, activo, mazos, apuestaMin, apuestaMax));
                            continue; // Si es Blackjack, pasa al siguiente ID
                        }
                    }
                }
                
                // 2. Intentar cargar como HighLow
                String sqlHighLow = "SELECT mazos, apuestaMin, apuestaMax FROM HighLow WHERE id = ?";
                try (PreparedStatement pstmtHl = conn.prepareStatement(sqlHighLow)) {
                    pstmtHl.setLong(1, id);
                    try (ResultSet rsHl = pstmtHl.executeQuery()) {
                        if (rsHl.next()) {
                            int mazos = rsHl.getInt("mazos");
                            double apuestaMin = rsHl.getDouble("apuestaMin");
                            double apuestaMax = rsHl.getDouble("apuestaMax");
                            juegos.add(new HighLow(id, nombre, fechaCreacion, activo, mazos, apuestaMin, apuestaMax));
                            // No hay más tipos de juego en esta versión, así que no se necesita 'continue'
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