package colectivo.servicios;

import colectivo.dao.FrecuenciaDAO;
import colectivo.modelo.Frecuencia;

import java.util.*;
import java.util.stream.Collectors;

public class FrecuenciaServiceImpl implements FrecuenciaService {
    private final FrecuenciaDAO dao;

    private List<Frecuencia> cache;
    private Map<String, List<Frecuencia>> porLinea; 

    public FrecuenciaServiceImpl(FrecuenciaDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    private void ensureLoaded() {
        if (cache == null) {
            cache = Collections.unmodifiableList(dao.buscarTodos());
            
            porLinea = cache.stream().collect(Collectors.groupingBy(Frecuencia::getIdLinea));
        }
    }

    @Override
    public List<Frecuencia> listar() {
        ensureLoaded();
        return cache;
    }

    @Override
    public List<Frecuencia> porLinea(String idLinea) {
        ensureLoaded();
        return porLinea.getOrDefault(idLinea, List.of());
    }

    @Override
    public OptionalInt primerServicioMinutos(String idLinea) {
        ensureLoaded();
        return porLinea(idLinea).stream().mapToInt(Frecuencia::getMinutos).min();
    }

    @Override
    public OptionalInt ultimoServicioMinutos(String idLinea) {
        ensureLoaded();
        return porLinea(idLinea).stream().mapToInt(Frecuencia::getMinutos).max();
    }
}