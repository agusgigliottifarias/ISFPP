package colectivo.modelo;

import java.util.Objects;

/**
 * Entidad Frecuencia: id, idLinea (String), diaServicio (ej: 1=Semana), minutos (intervalo o tiempo fijo).
 */
public class Frecuencia {
    private final int id;
    private final String idLinea; 
    private final int diaServicio; 
    private final int minutos;     
    
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
        return id == that.id; 
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("Frecuencia{id=%d, linea=%s, dia=%d, minutos=%d}", id, idLinea, diaServicio, minutos);
    }
}