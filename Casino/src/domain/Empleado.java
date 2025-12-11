package domain;

import java.time.LocalDateTime;

public class Empleado extends Usuario {
    private String puesto;
    private LocalDateTime fechaInicio;
    private boolean activo;

    // Constructor vacío
    public Empleado() {
        super();
    }
    
    /**
     * Constructor PROTEGIDO para uso interno y para que Administrador pueda llamar a super()
     * pasando su propio rol.
     */
    protected Empleado(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, 
                       RolUsuario rol, String puesto, LocalDateTime fechaInicio, boolean activo) {
        super(id, nombre, email, password, fechaRegistro, rol);
        this.puesto = puesto;
        this.fechaInicio = fechaInicio;
        this.activo = activo;
    }

    /**
     * Constructor PÚBLICO requerido por Database.java para crear un Empleado normal.
     * Este constructor fija el rol a EMPLEADO.
     */
    public Empleado(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, 
                    String puesto, LocalDateTime fechaInicio, boolean activo) {
        // Llama al constructor protegido, fijando el rol a EMPLEADO
        this(id, nombre, email, password, fechaRegistro, RolUsuario.EMPLEADO, puesto, fechaInicio, activo);
    }
    
    // Getters y Setters
    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    @Override
    public String toString() {
        return String.format("%s (Puesto: %s, Activo: %b)", 
            super.toString(), puesto, activo);
    }
}