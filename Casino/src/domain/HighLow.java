package domain;

import java.time.LocalDateTime;

public class HighLow extends Juego {
    private int mazos;
    private double apuestaMin;
    private double apuestaMax;

    public HighLow() {}

    public HighLow(long id, String nombre, LocalDateTime fechaCreacion, boolean activo,
                   int mazos, double apuestaMin, double apuestaMax) {
        super(id, nombre, fechaCreacion, activo);
        this.mazos = mazos;
        this.apuestaMin = apuestaMin;
        this.apuestaMax = apuestaMax;
    }

    public int getMazos() { return mazos; }
    public void setMazos(int mazos) { this.mazos = mazos; }

    public double getApuestaMin() { return apuestaMin; }
    public void setApuestaMin(double apuestaMin) { this.apuestaMin = apuestaMin; }

    public double getApuestaMax() { return apuestaMax; }
    public void setApuestaMax(double apuestaMax) { this.apuestaMax = apuestaMax; }

    @Override
    public String toString() {
        return "HighLow{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", mazos=" + mazos +
                ", apuestaMin=" + apuestaMin +
                ", apuestaMax=" + apuestaMax +
                '}';
    }
}
