package domain;

import java.time.LocalDateTime;

public class Empleado extends Usuario {
    private String puesto;
    private Turno turno;
    private boolean activo;
    private LocalDateTime fechaInicio;

    public Empleado() {}

    public Empleado(long id, String nombre, String email, String password, LocalDateTime fechaRegistro,
                    String puesto, Turno turno, boolean activo, LocalDateTime fechaInicio) {
        super(id, nombre, email, password, fechaRegistro, RolUsuario.EMPLEADO);
        this.puesto = puesto;
        this.turno = turno;
        this.activo = activo;
        this.fechaInicio = fechaInicio;
    }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    @Override
    public String toString() {
        return "Empleado{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", puesto='" + puesto + '\'' +
                ", turno=" + turno +
                ", activo=" + activo +
                ", fechaInicio=" + fechaInicio +
                '}';
    }
}
