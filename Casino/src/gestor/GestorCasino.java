package gestor;

import domain.*;
import java.util.List;
import java.util.Optional;


public class GestorCasino {
    

    private final UsuarioDAO usuarioDAO;
    private final JuegoDAO juegoDAO;

    public GestorCasino() {
       )
        DBHelper.initializeDatabase();
        
        
        this.usuarioDAO = new UsuarioDAO();
        this.juegoDAO = new JuegoDAO();
    }
    

    
    public void registrarUsuario(Usuario usuario) {
       
        UsuarioDAO.insertar(usuario); 
    }
    
    public Optional<Usuario> autenticarUsuario(String email, String password) {
        
        return UsuarioDAO.autenticar(email, password); 
    }

   
    
    public void registrarJuego(Juego juego) {
        juegoDAO.insertar(juego);
    }
    
    
    public List<Juego> obtenerJuegosActivos() {
       
        return juegoDAO.findAllActivos(); 
    }
    
   
    
    public JuegoDAO getJuegoDAO() {
        return juegoDAO;
    }

    public UsuarioDAO getUsuarioDAO() {
        return usuarioDAO;
    }
}