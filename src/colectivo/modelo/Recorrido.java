package colectivo.modelo;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Representa un trayecto de una línea entre varias paradas.
 * Puede representar un recorrido de colectivo o caminando (linea == null).
 */
public class Recorrido {

    private Linea linea;                
    private List<Parada> paradas;
    private LocalTime horaSalida;
    private int duracion;               

    public Recorrido(Linea linea, List<Parada> paradas, LocalTime horaSalida, int duracion) {
        this.linea = linea;
        this.paradas = paradas;
        this.horaSalida = horaSalida;
        this.duracion = duracion;
    }

    public Linea getLinea() {
        return linea;
    }

    public List<Parada> getParadas() {
        return paradas;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public int getDuracion() {
        return duracion;
    }

    @Override
    public String toString() {
        return (linea != null ? linea.getNombre() : "Caminando") + " " + paradas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recorrido)) return false;
        Recorrido that = (Recorrido) o;
        return duracion == that.duracion &&
               Objects.equals(linea, that.linea) &&
               Objects.equals(paradas, that.paradas) &&
               Objects.equals(horaSalida, that.horaSalida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linea, paradas, horaSalida, duracion);
    }
}
