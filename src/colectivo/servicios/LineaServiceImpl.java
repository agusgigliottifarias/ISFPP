package colectivo.servicios;

import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.servicios.LineaService;

import java.util.*;

public class LineaServiceImpl implements LineaService {
    private final LineaDAO dao;
    private Map<String, Linea> cache;

    public LineaServiceImpl(LineaDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    private void ensureLoaded() {
        if (cache == null) cache = dao.buscarTodos();
    }

    @Override
    public Map<String, Linea> getLineasMap() {
        ensureLoaded();
        return cache;
    }

    @Override
    public Collection<Linea> listar() {
        ensureLoaded();
        return cache.values();
    }

    @Override
    public Linea get(String id) {
        ensureLoaded();
        return cache.get(id);
    }

    @Override
    public boolean existe(String id) {
        ensureLoaded();
        return cache.containsKey(id);
    }
}
