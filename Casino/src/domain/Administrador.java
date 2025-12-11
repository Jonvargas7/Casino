package domain;

import java.time.LocalDateTime;

public class Administrador extends Empleado {

    // Constructor vacío
    public Administrador() {
        super();
    }
    
    /**
     * Constructor para CARGAR desde la DB (con ID).
     */
    public Administrador(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, 
                         String puesto, LocalDateTime fechaInicio, boolean activo) {
        // Llama al constructor protegido de Empleado (super) con 'id'
        super(id, nombre, email, password, fechaRegistro, RolUsuario.ADMINISTRADOR, puesto, fechaInicio, activo);
    }
    
    /**
     * Constructor para CREAR un nuevo Administrador (SIN ID).
     */
    public Administrador(String nombre, String email, String password, LocalDateTime fechaRegistro, 
                         String puesto, LocalDateTime fechaInicio, boolean activo) {
        // Llama al constructor protegido de Empleado (super) sin 'id'
        super(nombre, email, password, fechaRegistro, RolUsuario.ADMINISTRADOR, puesto, fechaInicio, activo);
    }
    
    // No necesita getters/setters extra si solo hereda los campos de Empleado
// ... (resto de toString)
// ...
}