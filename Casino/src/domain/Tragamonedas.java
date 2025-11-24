package domain;

import java.time.LocalDateTime;

public class Tragamonedas extends Juego {
    private int rodillos;
    private double apuestaMin;
    private double apuestaMax;

    public Tragamonedas() {}

    public Tragamonedas(long id, String nombre, LocalDateTime fechaCreacion, boolean activo,
                        int rodillos, double apuestaMin, double apuestaMax) {
        super(id, nombre, fechaCreacion, activo);
        this.rodillos = rodillos;
        this.apuestaMin = apuestaMin;
        this.apuestaMax = apuestaMax;
    }

    public int getRodillos() { return rodillos; }
    public void setRodillos(int rodillos) { this.rodillos = rodillos; }

    public double getApuestaMin() { return apuestaMin; }
    public void setApuestaMin(double apuestaMin) { this.apuestaMin = apuestaMin; }

    public double getApuestaMax() { return apuestaMax; }
    public void setApuestaMax(double apuestaMax) { this.apuestaMax = apuestaMax; }

    @Override
    public String toString() {
        return "Tragamonedas{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", rodillos=" + rodillos +
                ", apuestaMin=" + apuestaMin +
                ", apuestaMax=" + apuestaMax +
                '}';
    }
}
