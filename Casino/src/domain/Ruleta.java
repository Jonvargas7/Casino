package domain;

import java.time.LocalDateTime;

public class Ruleta extends Juego {
    
    private double apuestaMin;
    private double apuestaMax;

    public Ruleta() {}

    public Ruleta(long id, String nombre, LocalDateTime fechaCreacion, boolean activo,
                   double apuestaMin, double apuestaMax) {
        super(id, nombre, fechaCreacion, activo);
        
        this.apuestaMin = apuestaMin;
        this.apuestaMax = apuestaMax;
    }

    
    

    public double getApuestaMin() {
		return apuestaMin;
	}

	public void setApuestaMin(double apuestaMin) {
		this.apuestaMin = apuestaMin;
	}

	public double getApuestaMax() {
		return apuestaMax;
	}

	public void setApuestaMax(double apuestaMax) {
		this.apuestaMax = apuestaMax;
	}

	@Override
    public String toString() {
        return "Ruleta{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", apuestaMin=" + apuestaMin +
                ", apuestaMax=" + apuestaMax +
                '}';
    }
}
