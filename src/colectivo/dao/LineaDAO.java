package colectivo.dao;

import colectivo.modelo.Linea;
import java.util.Map;

public interface LineaDAO {
    /** Clave sugerida: String.valueOf(idLinea). */
    Map<String, Linea> buscarTodos();
}
