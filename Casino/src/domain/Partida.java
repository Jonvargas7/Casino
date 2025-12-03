package domain;

import java.time.LocalDateTime;

public class Partida {
    private long id;
    private long idUsuario;
    private String juego;
    private LocalDateTime fecha;
    private String resultado;
    private double ganancia;

    public Partida() {}

    public Partida(long id, long idUsuario, String juego, LocalDateTime fecha, String resultado, double ganancia) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.juego = juego;
        this.fecha = fecha;
        this.resultado = resultado;
        this.ganancia = ganancia;
    }

    
    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getJuego() {
		return juego;
	}

	public void setJuego(String juego) {
		this.juego = juego;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public double getGanancia() {
		return ganancia;
	}

	public void setGanancia(double ganancia) {
		this.ganancia = ganancia;
	}

	@Override
    public String toString() {
        return "Partida{" +
                "id=" + id +
                ", idUsuario=" + idUsuario +
                ", juego='" + juego + '\'' +
                ", fecha=" + fecha +
                ", resultado='" + resultado + '\'' +
                ", ganancia=" + ganancia +
                '}';
    }
}
