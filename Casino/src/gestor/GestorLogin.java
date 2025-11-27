package gestor;

import domain.Usuario;
import java.util.Optional;


public class GestorLogin {


    private Usuario usuarioActual;

    public GestorLogin() {
        
        DBHelper.initializeDatabase();
        this.usuarioActual = null;
    }

 
    public boolean login(String email, String password) {
        Optional<Usuario> usuarioOpt = UsuarioDAO.autenticar(email, password);
        
        if (usuarioOpt.isPresent()) {
            this.usuarioActual = usuarioOpt.get();
            System.out.println("✅ Login exitoso: " + usuarioActual.getRol() + " - " + usuarioActual.getNombre());
            return true;
        } else {
            System.err.println("❌ Login fallido: Credenciales incorrectas para el email: " + email);
            return false;
        }
    }

 
    public void logout() {
        if (usuarioActual != null) {
            System.out.println("Sesión cerrada para el usuario: " + usuarioActual.getNombre());
            this.usuarioActual = null;
        } else {
            System.out.println("No hay sesión activa para cerrar.");
        }
    }

    public Optional<Usuario> getUsuarioActual() {
        return Optional.ofNullable(usuarioActual);
    }

    public boolean isSesionActiva() {
        return this.usuarioActual != null;
    }
    
}