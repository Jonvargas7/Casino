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
        setProperty("a", "resources/imagenes/A.png");
        setProperty("2", "resources/imagenes/2png.png");
        setProperty("3", "resources/imagenes/3.png");
        setProperty("4", "resources/imagenes/4.png");
        setProperty("5", "resources/imagenes/5.png");
        setProperty("6", "resources/imagenes/6.png");
        setProperty("7", "resources/imagenes/7.png");
        setProperty("8", "resources/imagenes/8.png");
        setProperty("9", "resources/imagenes/9.png");
        setProperty("10", "resources/imagenes/10.png");
        setProperty("j", "resources/imagenes/j.png");
        setProperty("q", "resources/imagenes/Q.png");
        setProperty("k", "resources/imagenes/K.png");
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
