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

/**
 * CoordinadorApp (versión simple, alineada a consigna)
 * - Orquesta: Config -> DAOs (Factory) -> Carga única -> Calculo
 * - Agnóstico de UI
 * - Sin System.out; errores claros con IllegalStateException
 */
public class CoordinadorApp {

    // 1) Config
    private ConfiguracionGlobal config;

    // 2) DAOs (por Factory)
    private ParadaDAO       paradaDAO;
    private LineaDAO        lineaDAO;
    private TramoDAO        tramoDAO;
    private FrecuenciaDAO   frecuenciaDAO;

    // 3) Datos en memoria (cache inmutable)
    private Map<Integer, Parada> paradas;     // id -> Parada
    private Map<String, Linea>   lineas;      // "L1I" -> Linea
    private Map<String, Tramo>   tramos;      // "origen-destino" -> Tramo
    private List<Frecuencia>     frecuencias;

    // 4) Negocio
    private Calculo calculo;

    /* ===================== Ciclo de vida ===================== */

    /** Llamar una sola vez al inicio. */
    public void inicializarAplicacion() {
        inicializarConfiguracion();
        inicializarAccesoDatos();    // DAOs via Factory
        cargarDatosUnaVez();         // cache inmutable
        inicializarCalculoConDatos();
    }

    private void inicializarConfiguracion() {
        this.config = ConfiguracionGlobal.get(); // singleton existente
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

        this.paradas     = Collections.unmodifiableMap(new LinkedHashMap<>(p));
        this.lineas      = Collections.unmodifiableMap(new LinkedHashMap<>(l));
        this.tramos      = Collections.unmodifiableMap(new LinkedHashMap<>(t));
        this.frecuencias = Collections.unmodifiableList(new ArrayList<>(f));
    }

    private void inicializarCalculoConDatos() {
        this.calculo = new Calculo(); // el test también lo instancia así; acá queda listo para UI
    }

    /* ===================== Facade para UI/otras capas ===================== */

    /** Delegá el cálculo usando los datos ya cargados (sin volver a tocar DAOs). */
    public List<List<Recorrido>> calcularRecorrido(int idParadaOrigen,
                                                   int idParadaDestino,
                                                   int diaSemana,
                                                   LocalTime horaLlegaParada) {
        Parada origen  = paradas.get(idParadaOrigen);
        Parada destino = paradas.get(idParadaDestino);
        if (origen == null || destino == null) return List.of();
        
        // ➡️ Pasamos TODAS las colecciones necesarias al motor de cálculo.
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

    /* ===================== Getters (solo lectura) ===================== */
    public ConfiguracionGlobal getConfig()      { return config; }
    public Map<Integer, Parada> getParadas()    { return paradas; }
    public Map<String, Linea>   getLineas()     { return lineas; }
    public Map<String, Tramo>   getTramos()     { return tramos; }
    public List<Frecuencia>     getFrecuencias() { return frecuencias; }
    public Calculo              getCalculo()    { return calculo; }
}