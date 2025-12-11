package domain;

import java.time.LocalDateTime;

public class Administrador extends Empleado {

    // Constructor vacío
    public Administrador() {
        super();
    }
    
    /**
     * Constructor requerido por Database.java. 
     * Llama al constructor protegido de Empleado, pero establece el RolUsuario a ADMINISTRADOR.
     */
    public Administrador(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, 
                         String puesto, LocalDateTime fechaInicio, boolean activo) {
        // Llama al constructor protegido de la clase Empleado (super), 
        // y pasa RolUsuario.ADMINISTRADOR
        super(id, nombre, email, password, fechaRegistro, RolUsuario.ADMINISTRADOR, puesto, fechaInicio, activo);
    }
    
    // No necesita getters/setters extra si solo hereda los campos de Empleado

    @Override
    public String toString() {
        return String.format("ADMINISTRADOR: %s (Puesto: %s)", 
            super.toString(), getPuesto());
    }
}