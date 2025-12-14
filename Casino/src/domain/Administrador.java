package domain;

import java.time.LocalDateTime;

public class Administrador extends Empleado {

    public Administrador() {
        super();
    }
   
    public Administrador(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, 
                         String puesto, LocalDateTime fechaInicio, boolean activo) {
        super(id, nombre, email, password, fechaRegistro, RolUsuario.ADMINISTRADOR, puesto, fechaInicio, activo);
    }
    
    
    public Administrador(String nombre, String email, String password, LocalDateTime fechaRegistro, 
                         String puesto, LocalDateTime fechaInicio, boolean activo) {
        super(nombre, email, password, fechaRegistro, RolUsuario.ADMINISTRADOR, puesto, fechaInicio, activo);
    }
    
    
}