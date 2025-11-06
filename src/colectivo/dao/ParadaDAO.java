package colectivo.dao;

import colectivo.modelo.Parada;
import java.util.Map;

public interface ParadaDAO {
    /** Clave sugerida: String.valueOf(idParada). */
    Map<Integer, Parada> buscarTodos();
}
