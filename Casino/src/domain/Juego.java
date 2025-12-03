package domain;

import java.time.LocalDateTime;

public abstract class Juego {
    private long id;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private boolean activo;

    public Juego() {}

    public Juego(long id, String nombre, LocalDateTime fechaCreacion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
    }

  

    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	@Override
    public String toString() {
        return "Juego{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", activo=" + activo +
                '}';
    }
}
