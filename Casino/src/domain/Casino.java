package domain;

import java.util.ArrayList;
import java.util.List;

public class Casino {
    private String nombre;
    private String version;
    private List<Jugador> jugadores;
    private List<Partida> partidas;

    public Casino() {
        this.jugadores = new ArrayList<>();
        this.partidas = new ArrayList<>();
    }

    public Casino(String nombre, String version, List<Jugador> jugadores, List<Partida> partidas) {
        this.nombre = nombre;
        this.version = version;
        this.jugadores = jugadores;
        this.partidas = partidas;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public List<Jugador> getJugadores() { return jugadores; }
    public void setJugadores(List<Jugador> jugadores) { this.jugadores = jugadores; }

    public List<Partida> getPartidas() { return partidas; }
    public void setPartidas(List<Partida> partidas) { this.partidas = partidas; }

    @Override
    public String toString() {
        return "Casino{" +
                "nombre='" + nombre + '\'' +
                ", version='" + version + '\'' +
                ", jugadores=" + (jugadores != null ? jugadores.size() : 0) +
                ", partidas=" + (partidas != null ? partidas.size() : 0) +
                '}';
    }
}
