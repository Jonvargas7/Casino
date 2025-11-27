package gestor;

import domain.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JuegoDAO {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    
    public void insertar(Juego juego) {
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false); 

            long generatedId = insertBaseJuego(conn, juego);
            juego.setId(generatedId); 

            insertSubclase(conn, juego);
            
            conn.commit(); 
            System.out.println("Juego '" + juego.getNombre() + "' insertado exitosamente con ID: " + generatedId);

        } catch (SQLException e) {
            System.err.println("ERROR al insertar juego: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
        } finally {
            close(conn);
        }
    }

    
    public Optional<Juego> findById(long id) {
        String sql = "SELECT * FROM JUEGO WHERE id_juego = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildJuego(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR al buscar juego por ID: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    
    public List<Juego> findAllActivos() {
        
        String sql = "SELECT * FROM JUEGO WHERE activo = 1"; 
        List<Juego> juegos = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBHelper.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) { 
                
                while (rs.next()) {
                    
                    juegos.add(buildJuego(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR al buscar todos los juegos activos: " + e.getMessage());
        } finally {
            close(conn);
        }
        return juegos;
    }

    private long insertBaseJuego(Connection conn, Juego juego) throws SQLException {
        String sql = "INSERT INTO JUEGO (nombre, fecha_creacion, activo) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, juego.getNombre());
            pstmt.setString(2, juego.getFechaCreacion().format(FORMATTER));
            pstmt.setInt(3, juego.isActivo() ? 1 : 0);
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Fallo al obtener ID generado para el juego.");
    }

    private void insertSubclase(Connection conn, Juego juego) throws SQLException {
        long id = juego.getId();
        PreparedStatement pstmt = null;

        try {
            if (juego instanceof Blackjack) { 
                Blackjack bj = (Blackjack) juego;
                String sql = "INSERT INTO BLACKJACK (id_juego, mazos, apuesta_min, apuesta_max) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, id);
                pstmt.setInt(2, bj.getMazos());
                pstmt.setDouble(3, bj.getApuestaMin());
                pstmt.setDouble(4, bj.getApuestaMax());
                
            } else if (juego instanceof HighLow) { 
                HighLow hl = (HighLow) juego;
                String sql = "INSERT INTO HIGHLOW (id_juego, mazos, apuesta_min, apuesta_max) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, id);
                pstmt.setInt(2, hl.getMazos());
                pstmt.setDouble(3, hl.getApuestaMin());
                pstmt.setDouble(4, hl.getApuestaMax());
                
            } 
            if (pstmt != null) {
                pstmt.executeUpdate();
            } else {
                throw new SQLException("Tipo de juego no soportado para inserción: " + juego.getClass().getSimpleName());
            }
        } finally {
            if (pstmt != null) pstmt.close();
        }
    }
    
    private Juego buildJuego(Connection conn, ResultSet rsBase) throws SQLException {
        long id = rsBase.getLong("id_juego");
        String nombre = rsBase.getString("nombre");
        LocalDateTime fechaCreacion = LocalDateTime.parse(rsBase.getString("fecha_creacion"), FORMATTER);
        boolean activo = rsBase.getInt("activo") == 1;

        if (checkExists(conn, "BLACKJACK", id)) {
            return buildBlackjack(conn, id, nombre, fechaCreacion, activo);
        } else if (checkExists(conn, "HIGHLOW", id)) { 
            return buildHighLow(conn, id, nombre, fechaCreacion, activo);
        } 
        
        throw new SQLException("Tipo de Juego desconocido para ID: " + id);
    }
    
    private boolean checkExists(Connection conn, String tableName, long id) throws SQLException {
        String sql = "SELECT 1 FROM " + tableName + " WHERE id_juego = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    private Blackjack buildBlackjack(Connection conn, long id, String nombre, LocalDateTime fechaCreacion, boolean activo) throws SQLException {
        String sql = "SELECT mazos, apuesta_min, apuesta_max FROM BLACKJACK WHERE id_juego = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Blackjack(id, nombre, fechaCreacion, activo, 
                                         rs.getInt("mazos"), rs.getDouble("apuesta_min"), rs.getDouble("apuesta_max"));
                }
            }
        }
        throw new SQLException("Detalles de Blackjack no encontrados para ID: " + id);
    }

    private HighLow buildHighLow(Connection conn, long id, String nombre, LocalDateTime fechaCreacion, boolean activo) throws SQLException {
        String sql = "SELECT mazos, apuesta_min, apuesta_max FROM HIGHLOW WHERE id_juego = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new HighLow(id, nombre, fechaCreacion, activo, 
                                       rs.getInt("mazos"), rs.getDouble("apuesta_min"), rs.getDouble("apuesta_max"));
                }
            }
        }
        throw new SQLException("Detalles de HighLow no encontrados para ID: " + id);
    }
    
    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}