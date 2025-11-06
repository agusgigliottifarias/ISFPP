package colectivo.servicios;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import colectivo.servicios.ParadaService;

import java.util.*;

public class ParadaServiceImpl implements ParadaService {
    private final ParadaDAO dao;
    private Map<Integer, Parada> cache;

    public ParadaServiceImpl(ParadaDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    private void ensureLoaded() {
        if (cache == null) cache = dao.buscarTodos();
    }

    @Override
    public Map<Integer, Parada> getParadasMap() {
        ensureLoaded();
        return cache;
    }

    @Override
    public Collection<Parada> listar() {
        ensureLoaded();
        return cache.values();
    }

    @Override
    public Parada get(int id) {
        ensureLoaded();
        return cache.get(id);
    }

    @Override
    public boolean existe(int id) {
        ensureLoaded();
        return cache.containsKey(id);
    }
}
