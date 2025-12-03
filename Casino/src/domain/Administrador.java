package domain;

import java.time.LocalDateTime;

public class Administrador extends Usuario {
    private int nivelAcceso;

    public Administrador() {}

    public Administrador(long id, String nombre, String email, String password, LocalDateTime fechaRegistro,
                         int nivelAcceso) {
        super(id, nombre, email, password, fechaRegistro, RolUsuario.ADMINISTRADOR);
        this.nivelAcceso = nivelAcceso;
    }

    

    public int getNivelAcceso() {
		return nivelAcceso;
	}

	public void setNivelAcceso(int nivelAcceso) {
		this.nivelAcceso = nivelAcceso;
	}

	@Override
    public String toString() {
        return "Administrador{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", nivelAcceso=" + nivelAcceso +
                '}';
    }
}
