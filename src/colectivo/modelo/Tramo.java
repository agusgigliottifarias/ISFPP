package colectivo.modelo;

import java.util.Objects;

/**
 * Tramo: segmento entre dos paradas perteneciente a una línea.
 * ID compuesto: <idLinea>-<idParadaOrigen>-<idParadaDestino>
 */
public class Tramo {
    private String idLinea;
    private String idParadaOrigen;
    private String idParadaDestino;
    private int duracion; // en minutos

    public Tramo(String idLinea, String idParadaOrigen, String idParadaDestino, int duracion) {
        this.idLinea = idLinea;
        this.idParadaOrigen = idParadaOrigen;
        this.idParadaDestino = idParadaDestino;
        this.duracion = duracion;
    }

    // ID compuesto útil para map keys internas
    public String getId() {
        return (idLinea == null ? "null" : idLinea) + "-" +
               (idParadaOrigen == null ? "null" : idParadaOrigen) + "-" +
               (idParadaDestino == null ? "null" : idParadaDestino);
    }

    /** Clave esperada por la consigna/DAO: origen-destino */
    public String getClaveOrigenDestino() {
        return (idParadaOrigen == null ? "null" : idParadaOrigen) + "-" +
               (idParadaDestino == null ? "null" : idParadaDestino);
    }

    public String getIdLinea() { return idLinea; }
    public String getIdParadaOrigen() { return idParadaOrigen; }
    public String getIdParadaDestino() { return idParadaDestino; }
    public int getDuracion() { return duracion; }

    public void setIdLinea(String idLinea) { this.idLinea = idLinea; }
    public void setIdParadaOrigen(String idParadaOrigen) { this.idParadaOrigen = idParadaOrigen; }
    public void setIdParadaDestino(String idParadaDestino) { this.idParadaDestino = idParadaDestino; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    @Override
    public String toString() {
        return String.format("Tramo[%s: %s→%s, %dmin]", idLinea, idParadaOrigen, idParadaDestino, duracion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tramo)) return false;
        Tramo tramo = (Tramo) o;
        return Objects.equals(getId(), tramo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
