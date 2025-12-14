package domain;

import java.time.LocalDateTime;

public class Empleado extends Usuario {
    private String puesto;
    private LocalDateTime fechaInicio;
    private boolean activo;

    public Empleado() {
        super();
    }
 
    protected Empleado(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, 
                       RolUsuario rol, String puesto, LocalDateTime fechaInicio, boolean activo) {
        super(id, nombre, email, password, fechaRegistro, rol);
        this.puesto = puesto;
        this.fechaInicio = fechaInicio;
        this.activo = activo;
    }

  
    protected Empleado(String nombre, String email, String password, LocalDateTime fechaRegistro, 
                       RolUsuario rol, String puesto, LocalDateTime fechaInicio, boolean activo) {
        super(nombre, email, password, fechaRegistro, rol); // Llama a super sin 'id'
        this.puesto = puesto;
        this.fechaInicio = fechaInicio;
        this.activo = activo;
    }

  
    public Empleado(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, 
                    String puesto, LocalDateTime fechaInicio, boolean activo) {
        this(id, nombre, email, password, fechaRegistro, RolUsuario.EMPLEADO, puesto, fechaInicio, activo);
    }
    
  
    public Empleado(String nombre, String email, String password, LocalDateTime fechaRegistro, 
                    String puesto, LocalDateTime fechaInicio, boolean activo) {
        this(nombre, email, password, fechaRegistro, RolUsuario.EMPLEADO, puesto, fechaInicio, activo);
    }

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
    
  
}