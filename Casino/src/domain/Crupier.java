package domain;

import java.time.LocalDateTime;

public class Crupier {
    private long id;
    private String nombre;
    private String juegoAsignado;
    private boolean activo;
    private LocalDateTime fechaAlta;

    public Crupier() {}

    public Crupier(long id, String nombre, String juegoAsignado, boolean activo, LocalDateTime fechaAlta) {
        this.id = id;
        this.nombre = nombre;
        this.juegoAsignado = juegoAsignado;
        this.activo = activo;
        this.fechaAlta = fechaAlta;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getJuegoAsignado() { return juegoAsignado; }
    public void setJuegoAsignado(String juegoAsignado) { this.juegoAsignado = juegoAsignado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    @Override
    public String toString() {
        return "Crupier{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", juegoAsignado='" + juegoAsignado + '\'' +
                ", activo=" + activo +
                ", fechaAlta=" + fechaAlta +
                '}';
    }
}
