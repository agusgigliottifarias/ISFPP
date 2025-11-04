package colectivo.servicios;

import colectivo.modelo.Linea;
import java.util.Collection;
import java.util.Map;

public interface LineaService {
    Map<String, Linea> getLineasMap();
    Collection<Linea> listar();
    Linea get(String id);
    boolean existe(String id);
}
