package colectivo.servicios;

import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;
import colectivo.servicios.TramoService;

import java.util.Map;
import java.util.Objects;

public class TramoServiceImpl implements TramoService {
    private final TramoDAO dao;
    private Map<String, Tramo> cache; 

    public TramoServiceImpl(TramoDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    private void ensureLoaded() {
        if (cache == null) cache = dao.buscarTodos();
    }

    @Override
    public Map<String, Tramo> getTramosMap() {
        ensureLoaded();
        return cache;
    }

    @Override
    public Tramo get(String origenId, String destinoId) {
        ensureLoaded();
        return cache.get(key(origenId, destinoId));
    }

    @Override
    public Tramo get(int origenId, int destinoId) {
        return get(Integer.toString(origenId), Integer.toString(destinoId));
    }

    @Override
    public boolean existe(String origenId, String destinoId) {
        ensureLoaded();
        return cache.containsKey(key(origenId, destinoId));
    }

    @Override
    public boolean existe(int origenId, int destinoId) {
        return existe(Integer.toString(origenId), Integer.toString(destinoId));
    }

    private static String key(String o, String d) { return o.trim() + "-" + d.trim(); }
}
