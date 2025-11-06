package colectivo.dao.secuencial;

import colectivo.config.ConfiguracionGlobal;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementación secuencial de TramoDAO 
 * Versión Limpia con Log4j.
 */
public class TramoSecuencialDAO implements TramoDAO {

    private static final Logger logger = LogManager.getLogger(TramoSecuencialDAO.class);

    private volatile boolean loaded = false;
    private Map<String, Tramo> cache = Collections.emptyMap();

    private static final String TIPO_COLECTIVO = "1";
    private static final String TIPO_CAMINANDO = "2";

    @Override
    public Map<String, Tramo> buscarTodos() {
        if (loaded) return cache;
        
        synchronized (this) {
            if (loaded) return cache;
            
            logger.info("Iniciando carga de datos de tramos (DAO)...");
            char sep = ConfiguracionGlobal.get().getChar("csv.delimiter", ';');

            Map<String, Integer> mapaDuraciones = new HashMap<>();
            List<Tramo> tramosCaminando = new ArrayList<>();
            String rutaTramosDatos = ConfiguracionGlobal.get().require("ruta.tramos");
            List<String> filasTramos;
            
            try {
                filasTramos = Files.readAllLines(Path.of(rutaTramosDatos));
            } catch (IOException e) {
                logger.error("No pude leer archivo de datos de tramos: " + rutaTramosDatos, e);
                throw new IllegalStateException("No pude leer archivo de datos de tramos", e);
            }

            for (String s : filasTramos) {
                String t = s.trim();
                if (t.isEmpty() || t.startsWith("#") || t.equals("}")) continue;

                String[] a = t.split("\\" + sep);
                if (a.length < 3) continue; 

                String origenId = a[0].trim();
                String destinoId = a[1].trim();
                String claveOrigenDestino = origenId + "-" + destinoId;
                
                int minutos;
                try {
                    int segundos = Integer.parseInt(a[2].trim());
                    minutos = Math.max(1, (int) Math.round(segundos / 60.0));
                } catch (NumberFormatException ex) {
                    logger.warn("Duración inválida para {}. Usando 1 min.", claveOrigenDestino);
                    minutos = 1;
                }
                
                mapaDuraciones.put(claveOrigenDestino, minutos);

                if (a.length > 3 && a[3].trim().equals(TIPO_CAMINANDO)) {
                    Tramo tramoCaminando = new Tramo(null, origenId, destinoId, minutos);
                    tramosCaminando.add(tramoCaminando);
                }
            }
            
            logger.info("Paso 1 (Tramos) completado. {} duraciones y {} tramos caminando cargados.",
                           mapaDuraciones.size(), tramosCaminando.size());

            Map<String, Tramo> out = new LinkedHashMap<>();
            String rutaLineasDef = ConfiguracionGlobal.get().require("ruta.lineas");
            List<String> filasLineas;
            
            try {
                filasLineas = Files.readAllLines(Path.of(rutaLineasDef));
            } catch (IOException e) {
                logger.error("No pude leer archivo de definición de líneas: " + rutaLineasDef, e);
                throw new IllegalStateException("No pude leer archivo de definición de líneas", e);
            }

            for (String s : filasLineas) {
                String t = s.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;

                String[] a = t.split("\\" + sep);
                if (a.length < 4) continue;

                String idLinea = a[0].trim();
                
                for (int i = 2; i < a.length - 1; i++) {
                    String origenId = a[i].trim();
                    String destinoId = a[i+1].trim();
                    if (origenId.isEmpty() || destinoId.isEmpty()) continue;

                    String claveOrigenDestino = origenId + "-" + destinoId;
                    Integer duracion = mapaDuraciones.get(claveOrigenDestino);
                    
                    if (duracion == null) {
                        logger.warn("No se encontró duración para el tramo {} de la línea {}. Usando 1 min.", 
                                       claveOrigenDestino, idLinea);
                        duracion = 1;
                    }

                    Tramo tramo = new Tramo(idLinea, origenId, destinoId, duracion);
                    String claveUnica = idLinea + "-" + claveOrigenDestino;
                    out.put(claveUnica, tramo);
                }
            }
            
            logger.info("Paso 2 (Líneas) completado. {} tramos de colectivo construidos.", out.size());

            for (Tramo tramoCaminando : tramosCaminando) {
                String claveUnica = "CAMINANDO-" + tramoCaminando.getIdParadaOrigen() + "-" + tramoCaminando.getIdParadaDestino();
                out.put(claveUnica, tramoCaminando);
            }

            logger.info("Total tramos (Colectivo + Caminando) en DAO: {}", out.size());
            
            cache = Collections.unmodifiableMap(out);
            loaded = true;
            return cache;
        }
    }
} 