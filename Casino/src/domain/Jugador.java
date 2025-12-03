package domain;

import java.time.LocalDateTime;

public class Jugador extends Usuario {
    private double saldo;
    private int numeroDePartidas;
    private double totalGanado;
    private int nivel;

    public Jugador() {}

    public Jugador(long id, String nombre, String email, String password, LocalDateTime fechaRegistro,
                   double saldo, int numeroDePartidas, double totalGanado, int nivel) {
        super(id, nombre, email, password, fechaRegistro, RolUsuario.JUGADOR);
        this.saldo = saldo;
        this.numeroDePartidas = numeroDePartidas;
        this.totalGanado = totalGanado;
        this.nivel = nivel;
    }


    public double getSaldo() {
		return saldo;
	}

	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}

	public int getNumeroDePartidas() {
		return numeroDePartidas;
	}

	public void setNumeroDePartidas(int numeroDePartidas) {
		this.numeroDePartidas = numeroDePartidas;
	}

	public double getTotalGanado() {
		return totalGanado;
	}

	public void setTotalGanado(double totalGanado) {
		this.totalGanado = totalGanado;
	}

	public int getNivel() {
		return nivel;
	}

	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	@Override
    public String toString() {
        return "Jugador{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", saldo=" + saldo +
                ", partidas=" + numeroDePartidas +
                ", totalGanado=" + totalGanado +
                ", nivel=" + nivel +
                '}';
    }
}
