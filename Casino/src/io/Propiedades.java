package io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Propiedades extends Properties {

    private static final long serialVersionUID = 1L;

    public Propiedades() {
        super();
    }

    /**
     * Guarda las propiedades principales del casino en el archivo conf/config.properties
     */
    public void guardar() {
        // Imágenes principales del casino
        setProperty("favicon", "resources/imagenes/blackjack.png");
        setProperty("blackJack", "resources/imagenes/blackjack.png");
        setProperty("highlow", "resources/imagenes/highLow.png");
        setProperty("ruleta", "resources/imagenes/ruleta.png");

        try (FileOutputStream output = new FileOutputStream("conf/config.properties")) {
            store(output, "Configuración del Casino Virtual");
            System.out.println("Archivo config.properties actualizado correctamente.");
        } catch (IOException io) {
            System.err.println("Error al guardar las propiedades: " + io.getMessage());
        }
    }

    
    public void cargar() {
        try (FileInputStream input = new FileInputStream("conf/config.properties")) {
            load(input);
        } catch (IOException ex) {
            System.err.println("No se pudo cargar config.properties. Se usará configuración por defecto.");
            guardar(); // si no existe, lo crea con valores por defecto
        }
    }
}
