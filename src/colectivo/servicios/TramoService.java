package colectivo.servicios;

import colectivo.modelo.Tramo;
import java.util.Map;

public interface TramoService {
    Map<String, Tramo> getTramosMap();
    Tramo get(String origenId, String destinoId);
    Tramo get(int origenId, int destinoId);
    boolean existe(String origenId, String destinoId);
    boolean existe(int origenId, int destinoId);
}
