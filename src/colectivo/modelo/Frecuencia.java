package colectivo.modelo;

import java.util.Objects;

/**
 * Entidad Frecuencia: id, idLinea (String), diaServicio (ej: 1=Semana), minutos (intervalo o tiempo fijo).
 */
public class Frecuencia {
    private final int id;
    private final String idLinea; // Usado para referenciar la Linea real (ej: L1I)
    private final int diaServicio; // 1=Día de semana, 2=Sábado, 3=Domingo/Feriado, etc.
    private final int minutos;     // Duración en minutos (si es intervalo) o minutos totales desde 00:00 (si es horario fijo)

    public Frecuencia(int id, String idLinea, int diaServicio, int minutos) {
        this.id = id;
        this.idLinea = idLinea;
        this.diaServicio = diaServicio;
        this.minutos = minutos;
    }

    public int getId() { return id; }
    public String getIdLinea() { return idLinea; }
    public int getDiaServicio() { return diaServicio; }
    public int getMinutos() { return minutos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Frecuencia)) return false;
        Frecuencia that = (Frecuencia) o;
        return id == that.id; // igualdad por id
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("Frecuencia{id=%d, linea=%s, dia=%d, minutos=%d}", id, idLinea, diaServicio, minutos);
    }
}/*package colectivo.modelo;

import java.util.Objects;

/** Entidad Frecuencia: id, lineaId, minutos (intervalo). */
/*public class Frecuencia {
    private final int id;
    private final int lineaId;
    private final int minutos;

    public Frecuencia(int id, int lineaId, int minutos) {
        this.id = id;
        this.lineaId = lineaId;
        this.minutos = minutos;
    }

    public int getId() { return id; }
    public int getLineaId() { return lineaId; }
    public int getMinutos() { return minutos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Frecuencia)) return false;
        Frecuencia that = (Frecuencia) o;
        return id == that.id; // igualdad por id
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Frecuencia{id=" + id + ", lineaId=" + lineaId + ", minutos=" + minutos + '}';
    }
}*/
