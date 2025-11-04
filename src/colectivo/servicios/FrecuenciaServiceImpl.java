package colectivo.servicios;

import colectivo.dao.FrecuenciaDAO;
import colectivo.modelo.Frecuencia;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Frecuencia: Intermediario delgado entre CoordinadorApp y FrecuenciaDAO.
 * Implementa cache en memoria por Código de Línea (String).
 */
public class FrecuenciaServiceImpl implements FrecuenciaService {
    private final FrecuenciaDAO dao;

    private List<Frecuencia> cache;
    // ➡️ CAMBIO CLAVE: Usa String (código de línea) como clave para agrupar.
    private Map<String, List<Frecuencia>> porLinea; 

    public FrecuenciaServiceImpl(FrecuenciaDAO dao) {
        // Se inyecta la dependencia DAO (Factory Pattern)
        this.dao = Objects.requireNonNull(dao);
    }

    /** Carga los datos y los agrupa en memoria la primera vez. */
    private void ensureLoaded() {
        if (cache == null) {
            // El DAO solo carga una vez; esta es la cache del Service.
            cache = Collections.unmodifiableList(dao.buscarTodos());
            
            // ➡️ Agrupación por getIdLinea() que ahora devuelve String
            porLinea = cache.stream().collect(Collectors.groupingBy(Frecuencia::getIdLinea));
        }
    }

    @Override
    public List<Frecuencia> listar() {
        ensureLoaded();
        return cache;
    }

    // ➡️ Método porLinea: Acepta String idLinea
    @Override
    public List<Frecuencia> porLinea(String idLinea) {
        ensureLoaded();
        return porLinea.getOrDefault(idLinea, List.of());
    }

    // ➡️ Método primerServicioMinutos: Acepta String idLinea
    @Override
    public OptionalInt primerServicioMinutos(String idLinea) {
        ensureLoaded();
        // Filtra por línea y busca el menor valor en minutos
        return porLinea(idLinea).stream().mapToInt(Frecuencia::getMinutos).min();
    }

    // ➡️ Método ultimoServicioMinutos: Acepta String idLinea
    @Override
    public OptionalInt ultimoServicioMinutos(String idLinea) {
        ensureLoaded();
        // Filtra por línea y busca el mayor valor en minutos
        return porLinea(idLinea).stream().mapToInt(Frecuencia::getMinutos).max();
    }
}