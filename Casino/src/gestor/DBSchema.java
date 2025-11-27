package gestor;

import domain.RolUsuario;

public class DBSchema {


    private static final String CREATE_TABLE_USUARIO =
        "CREATE TABLE IF NOT EXISTS USUARIO (" +
        "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
        "nombre TEXT NOT NULL," +
        "email TEXT NOT NULL UNIQUE," +
        "password TEXT NOT NULL," +
        "fecha_registro TEXT NOT NULL," +
        "id_rol INTEGER NOT NULL," +
        "FOREIGN KEY(id_rol) REFERENCES ROL_USUARIO(id_rol)" +
        ")";

    private static final String CREATE_TABLE_JUEGO =
        "CREATE TABLE IF NOT EXISTS JUEGO (" +
        "id_juego INTEGER PRIMARY KEY AUTOINCREMENT," +
        "nombre TEXT NOT NULL UNIQUE," +
        "fecha_creacion TEXT NOT NULL," +
        "activo INTEGER NOT NULL DEFAULT 1" + // 1=activo, 0=inactivo
        ")";
    

    private static final String CREATE_TABLE_ROL_USUARIO =
        "CREATE TABLE IF NOT EXISTS ROL_USUARIO (" +
        "id_rol INTEGER PRIMARY KEY," +
        "nombre_rol TEXT NOT NULL UNIQUE" +
        ")";
    

    private static final String CREATE_TABLE_JUGADOR =
        "CREATE TABLE IF NOT EXISTS JUGADOR (" +
        "id_usuario INTEGER PRIMARY KEY," +
        "saldo REAL NOT NULL," +
        "partidas_jugadas INTEGER NOT NULL," +
        "total_apostado REAL NOT NULL," +
        "nivel INTEGER NOT NULL DEFAULT 1," +
        "FOREIGN KEY(id_usuario) REFERENCES USUARIO(id_usuario)" +
        ")";

    private static final String CREATE_TABLE_EMPLEADO =
        "CREATE TABLE IF NOT EXISTS EMPLEADO (" +
        "id_usuario INTEGER PRIMARY KEY," +
        "puesto TEXT NOT NULL," +
        "activo INTEGER NOT NULL DEFAULT 1," + 
        "fecha_contratacion TEXT NOT NULL," +
        "FOREIGN KEY(id_usuario) REFERENCES USUARIO(id_usuario)" +
        ")";
        
    private static final String CREATE_TABLE_ADMINISTRADOR =
        "CREATE TABLE IF NOT EXISTS ADMINISTRADOR (" +
        "id_usuario INTEGER PRIMARY KEY," +
        "nivel_acceso INTEGER NOT NULL DEFAULT 1," +
        "FOREIGN KEY(id_usuario) REFERENCES USUARIO(id_usuario)" +
        ")";


    private static final String CREATE_TABLE_BLACKJACK =
        "CREATE TABLE IF NOT EXISTS BLACKJACK (" +
        "id_juego INTEGER PRIMARY KEY," +
        "mazos INTEGER NOT NULL," +
        "apuesta_min REAL NOT NULL," +
        "apuesta_max REAL NOT NULL," +
        "FOREIGN KEY(id_juego) REFERENCES JUEGO(id_juego)" +
        ")";
        
    private static final String CREATE_TABLE_HIGHLOW =
        "CREATE TABLE IF NOT EXISTS HIGHLOW (" +
        "id_juego INTEGER PRIMARY KEY," +
        "mazos INTEGER NOT NULL," +
        "apuesta_min REAL NOT NULL," +
        "apuesta_max REAL NOT NULL," +
        "FOREIGN KEY(id_juego) REFERENCES JUEGO(id_juego)" +
        ")";


    private static final String CREATE_TABLE_PARTIDA =
        "CREATE TABLE IF NOT EXISTS PARTIDA (" +
        "id_partida INTEGER PRIMARY KEY AUTOINCREMENT," +
        "id_jugador INTEGER NOT NULL," +
        "id_juego INTEGER NOT NULL," +
        "fecha_inicio TEXT NOT NULL," +
        "fecha_fin TEXT," +
        "resultado TEXT," +
        "apuesta REAL NOT NULL," +
        "ganancia REAL," +
        "FOREIGN KEY(id_jugador) REFERENCES JUGADOR(id_usuario)," +
        "FOREIGN KEY(id_juego) REFERENCES JUEGO(id_juego)" +
        ")";


    public static final String[] CREATE_TABLE_SQL = {
        CREATE_TABLE_ROL_USUARIO,
        CREATE_TABLE_USUARIO,
        CREATE_TABLE_JUGADOR,
        CREATE_TABLE_EMPLEADO,
        CREATE_TABLE_ADMINISTRADOR,
        CREATE_TABLE_JUEGO,
        CREATE_TABLE_BLACKJACK,
        CREATE_TABLE_HIGHLOW, 
        CREATE_TABLE_PARTIDA 
    };


    public static final String INSERT_ROL_USUARIO_SQL =
        "INSERT INTO ROL_USUARIO (id_rol, nombre_rol) VALUES (?, ?)";
}