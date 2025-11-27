package gestor;

import domain.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UsuarioDAO {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    

    public static void insertar(Usuario usuario) {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false); 

            long generatedId = insertarBaseUsuario(conn, usuario);
            usuario.setId(generatedId); 

            insertarSubclase(conn, usuario);
            
            conn.commit(); 
        } catch (SQLException e) {
            
        } finally {
            close(conn);
        }
    }
    
    private static long insertarBaseUsuario(Connection conn, Usuario usuario) throws SQLException {
        
        String sql = "INSERT INTO USUARIO (nombre, email, password, fecha_registro, id_rol) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getPassword()); 
            pstmt.setString(4, usuario.getFechaRegistro().format(FORMATTER));
            pstmt.setInt(5, usuario.getRol().ordinal() + 1); // ROL_USUARIO IDs empiezan en 1
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Fallo al obtener ID generado.");
    }
    
    private static void insertarSubclase(Connection conn, Usuario usuario) throws SQLException {
        long id = usuario.getId();
        PreparedStatement pstmt = null;

        try {
            if (usuario instanceof Jugador) {
                Jugador j = (Jugador) usuario;
                
                String sql = "INSERT INTO JUGADOR (id_usuario, saldo, partidas_jugadas, total_apostado, nivel) VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, id);
                pstmt.setDouble(2, j.getSaldo());
                pstmt.setInt(3, j.getNumeroDePartidas());
                pstmt.setDouble(4, j.getTotalGanado());
                pstmt.setInt(5, j.getNivel());
                
            } else if (usuario instanceof Empleado) {
                Empleado e = (Empleado) usuario;
                
                String sql = "INSERT INTO EMPLEADO (id_usuario, puesto, activo, fecha_contratacion) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, id);
                pstmt.setString(2, e.getPuesto());
                
                pstmt.setInt(3, e.isActivo() ? 1 : 0);
                pstmt.setString(4, e.getFechaInicio().format(FORMATTER));
                
            } else if (usuario instanceof Administrador) {
                Administrador a = (Administrador) usuario;
               
                String sql = "INSERT INTO ADMINISTRADOR (id_usuario, nivel_acceso) VALUES (?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, id);
                pstmt.setInt(2, a.getNivelAcceso());
            }

            if (pstmt != null) {
                pstmt.executeUpdate();
            } else {
                throw new SQLException("Tipo de usuario no soportado para inserción: " + usuario.getClass().getSimpleName());
            }
        } finally {
            if (pstmt != null) pstmt.close();
        }
    }
    
    public static Optional<Usuario> autenticar(String email, String password) {
        
        String sql = "SELECT * FROM USUARIO WHERE email = ? AND password = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildUsuario(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
        }
        return Optional.empty();
    }

    private static Usuario buildUsuario(Connection conn, ResultSet rsBase) throws SQLException {
        long id = rsBase.getLong("id_usuario");
        String nombre = rsBase.getString("nombre");
        String email = rsBase.getString("email");
        String password = rsBase.getString("password");
        LocalDateTime fechaRegistro = LocalDateTime.parse(rsBase.getString("fecha_registro"), FORMATTER);
        RolUsuario rol = RolUsuario.values()[rsBase.getInt("id_rol") - 1]; 

        switch (rol) {
            case JUGADOR:
                return buildJugador(conn, id, nombre, email, password, fechaRegistro);
            case EMPLEADO:
                return buildEmpleado(conn, id, nombre, email, password, fechaRegistro);
            case ADMINISTRADOR:
                return buildAdministrador(conn, id, nombre, email, password, fechaRegistro);
            default:
                throw new SQLException("Rol de usuario desconocido.");
        }
    }
    
    private static Empleado buildEmpleado(Connection conn, long id, String nombre, String email, String password, LocalDateTime fechaRegistro) throws SQLException {
        
        String sql = "SELECT puesto, activo, fecha_contratacion FROM EMPLEADO WHERE id_usuario = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String puesto = rs.getString("puesto");
                    
                    boolean activo = rs.getInt("activo") == 1;
                    LocalDateTime fechaInicio = LocalDateTime.parse(rs.getString("fecha_contratacion"), FORMATTER);
                    
                    return new Empleado(id, nombre, email, password, fechaRegistro, 
                                        puesto, activo, fechaInicio); 
                }
            }
        }
        throw new SQLException("Detalles de Empleado no encontrados para ID: " + id);
    }
    
    private static Jugador buildJugador(Connection conn, long id, String nombre, String email, String password, LocalDateTime fechaRegistro) throws SQLException {
        
        String sql = "SELECT saldo, partidas_jugadas, total_apostado, nivel FROM JUGADOR WHERE id_usuario = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    
                    return new Jugador(id, nombre, email, password, fechaRegistro,
                                       rs.getDouble("saldo"), rs.getInt("partidas_jugadas"), 
                                       rs.getDouble("total_apostado"), rs.getInt("nivel"));
                }
            }
        }
        throw new SQLException("Detalles de Jugador no encontrados para ID: " + id);
    }
    
    private static Administrador buildAdministrador(Connection conn, long id, String nombre, String email, String password, LocalDateTime fechaRegistro) throws SQLException {
        
        String sql = "SELECT nivel_acceso FROM ADMINISTRADOR WHERE id_usuario = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                   
                    return new Administrador(id, nombre, email, password, fechaRegistro, rs.getInt("nivel_acceso"));
                }
            }
        }
        throw new SQLException("Detalles de Administrador no encontrados para ID: " + id);
    }

    private static void close(Connection conn) {
        
    }
}