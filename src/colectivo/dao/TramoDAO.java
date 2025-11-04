package colectivo.dao;

import colectivo.modelo.Tramo;
import java.util.Map;

public interface TramoDAO {
    /** Clave requerida por la cátedra: "origenId-destinoId" (ej: "66-31"). */
    Map<String, Tramo> buscarTodos();
}
