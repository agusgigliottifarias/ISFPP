package colectivo.aplicacion;

import colectivo.config.ConfiguracionGlobal;
import colectivo.factory.Factory;
import colectivo.dao.ParadaDAO;
import colectivo.dao.LineaDAO;
import colectivo.dao.TramoDAO;
import colectivo.dao.FrecuenciaDAO;
import colectivo.modelo.Parada;
import colectivo.modelo.Linea;
import colectivo.modelo.Tramo;
import colectivo.modelo.Frecuencia;
import colectivo.modelo.Recorrido;
import colectivo.negocio.Calculo;

import java.time.LocalTime;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CoordinadorApp (Versión Limpia con Log4j).
 * Orquesta la aplicación y sirve de Facade para la UI.
 */
public class CoordinadorApp {

    private static final Logger logger = LogManager.getLogger(CoordinadorApp.class);

    private ConfiguracionGlobal config;

    private ParadaDAO       paradaDAO;
    private LineaDAO        lineaDAO;
    private TramoDAO        tramoDAO;
    private FrecuenciaDAO   frecuenciaDAO;

    private Map<Integer, Parada> paradas;     
    private Map<String, Linea>   lineas;      
    private Map<String, Tramo>   tramos;      
    private List<Frecuencia>     frecuencias;

    private Calculo calculo;

    public void inicializarAplicacion() {
        logger.info("INICIALIZANDO APLICACIÓN...");
        try {
            logger.info("1. Inicializando Configuración...");
            inicializarConfiguracion();
            
            logger.info("2. Inicializando Acceso a Datos (DAOs)...");
            inicializarAccesoDatos();
            
            logger.info("3. Cargando Datos en Memoria (una sola vez)...");
            cargarDatosUnaVez();
            
            logger.info("4. Inicializando Capa de Negocio (Calculo)...");
            inicializarCalculoConDatos();
            
            logger.info("✅ INICIALIZACIÓN COMPLETA.");

        } catch (Exception e) {
            logger.fatal("Falló la inicialización de CoordinadorApp.", e);
            throw new RuntimeException("Error fatal al iniciar", e);
        }
    }

    private void inicializarConfiguracion() {
        this.config = ConfiguracionGlobal.get();
    }

    private void inicializarAccesoDatos() {
        this.paradaDAO      = Factory.getInstancia("PARADA",   ParadaDAO.class);
        this.lineaDAO       = Factory.getInstancia("LINEA",    LineaDAO.class);
        this.tramoDAO       = Factory.getInstancia("TRAMO",    TramoDAO.class);
        this.frecuenciaDAO  = Factory.getInstancia("FRECUENCIA", FrecuenciaDAO.class);
    }


    private void cargarDatosUnaVez() {
        if (paradaDAO == null || lineaDAO == null || tramoDAO == null || frecuenciaDAO == null) {
            throw new IllegalStateException("DAOs no inicializados. Ejecutá inicializarAccesoDatos() antes.");
        }

        Map<Integer, Parada> p = paradaDAO.buscarTodos();
        Map<String, Linea>   l = lineaDAO.buscarTodos();
        Map<String, Tramo>   t = tramoDAO.buscarTodos();
        List<Frecuencia>     f = frecuenciaDAO.buscarTodos();

        if (p == null || l == null || t == null || f == null) {
            throw new IllegalStateException("Carga inicial retornó null. Verificá implementaciones DAO.");
        }
        
        logger.info("Carga de datos OK -> {} Paradas, {} Lineas, {} Tramos, {} Frecuencias.",
                           p.size(), l.size(), t.size(), f.size());

        this.paradas     = Collections.unmodifiableMap(new LinkedHashMap<>(p));
        this.lineas      = Collections.unmodifiableMap(new LinkedHashMap<>(l));
        this.tramos      = Collections.unmodifiableMap(new LinkedHashMap<>(t));
        this.frecuencias = Collections.unmodifiableList(new ArrayList<>(f));
    }

    private void inicializarCalculoConDatos() {
        this.calculo = new Calculo();
    }


    public List<List<Recorrido>> calcularRecorrido(int idParadaOrigen,
                                                   int idParadaDestino,
                                                   int diaSemana,
                                                   LocalTime horaLlegaParada) {
                                                       
        logger.info("UI solicita recorrido: Origen={} -> Destino={} (Día: {}, Hora: {})",
                    idParadaOrigen, idParadaDestino, diaSemana, horaLlegaParada);

        Parada origen  = paradas.get(idParadaOrigen);
        Parada destino = paradas.get(idParadaDestino);
        
        if (origen == null || destino == null) {
            logger.warn("FALLO: La parada de origen (ID: {}) o destino (ID: {}) es NULL.", idParadaOrigen, idParadaDestino);
            return List.of(); 
        }
        
        logger.info("Paradas encontradas. Delegando a Calculo.java...");
        
        return calculo.calcularRecorrido(
            origen, 
            destino, 
            diaSemana, 
            horaLlegaParada, 
            tramos, 
            frecuencias, 
            lineas, 
            paradas
        );
    }

    public ConfiguracionGlobal getConfig()      { return config; }
    public Map<Integer, Parada> getParadas()    { return paradas; }
    public Map<String, Linea>   getLineas()     { return lineas; }
    public Map<String, Tramo>   getTramos()     { return tramos; }
    public List<Frecuencia>     getFrecuencias() { return frecuencias; }
    public Calculo              getCalculo()    { return calculo; }
}