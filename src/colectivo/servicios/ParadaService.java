package colectivo.servicios;

import colectivo.modelo.Parada;
import java.util.Collection;
import java.util.Map;

public interface ParadaService {
    Map<Integer, Parada> getParadasMap();
    Collection<Parada> listar();
    Parada get(int id);
    boolean existe(int id);
}
