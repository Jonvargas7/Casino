package domain;

import java.time.LocalDateTime;

public abstract class Usuario {
    private long id;
    private String nombre;
    private String email;
    private String password;
    private LocalDateTime fechaRegistro;
    private RolUsuario rol;

    public Usuario() {}

    /**
     * Constructor para CARGAR desde la base de datos (con ID).
     */
    public Usuario(long id, String nombre, String email, String password, LocalDateTime fechaRegistro, RolUsuario rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.fechaRegistro = fechaRegistro;
        this.rol = rol;
    }

    /**
     * Constructor para CREAR un nuevo usuario (SIN ID).
     * El ID se asignará automáticamente por la base de datos.
     */
    public Usuario(String nombre, String email, String password, LocalDateTime fechaRegistro, RolUsuario rol) {
        // this.id se deja a 0/default y se asigna más tarde por setId(nuevoId)
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.fechaRegistro = fechaRegistro;
        this.rol = rol;
    }

    
    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDateTime getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(LocalDateTime fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public RolUsuario getRol() {
		return rol;
	}

	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}
// ... (resto de getters y setters se mantienen)
// ...
}