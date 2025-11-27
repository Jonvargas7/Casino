package gestor;

import java.sql.*;
import domain.*;
import java.time.LocalDateTime;

public class DBHelper {
    
    private static final String DB_URL = "jdbc:sqlite:CasinoDB.db";

    public static Connection getConnection() throws SQLException {
        
        try {
            Class.forName("org.sqlite.JDBC"); 
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC de SQLite no encontrado.");
            throw new SQLException(e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            
            for (String sql : DBSchema.CREATE_TABLE_SQL) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
            
            loadInitialData(conn);
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }

    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + tableName + " LIMIT 1")) {
            return !rs.next();
        }
    }

    
    private static void loadInitialData(Connection conn) throws SQLException {
        
        
        if (isTableEmpty(conn, "ROL_USUARIO")) {
            try (PreparedStatement pstmt = conn.prepareStatement(DBSchema.INSERT_ROL_USUARIO_SQL)) {
                int id = 1;
                for (RolUsuario rol : RolUsuario.values()) { 
                    pstmt.setInt(1, id++);
                    pstmt.setString(2, rol.name());
                    pstmt.addBatch(); 
                }
                pstmt.executeBatch();
            }
        }
        
        
        if (!UsuarioDAO.autenticar("admin@casino.com", "admin123").isPresent()) { 
             UsuarioDAO.insertar(new Administrador(
                0, "Admin Boss", "admin@casino.com", "admin123", LocalDateTime.now(), 1
            ));
        }
    }
}