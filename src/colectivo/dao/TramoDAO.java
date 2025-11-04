package colectivo.dao;

import colectivo.modelo.Tramo;
import java.util.Map;

public interface TramoDAO {
    /** * Clave: String único por tramo.
     * Implementación sugerida: "idLinea-origenId-destinoId" (ej: "L1R-16-17").
     */
    Map<String, Tramo> buscarTodos();
}