package colectivo.dao.secuencial;

import colectivo.config.ConfiguracionGlobal;
import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;
import colectivo.factory.Factory; // Importado para demostrar inyección

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Implementación secuencial de TramoDAO.
 * Lee un archivo de texto una sola vez (bandera).
 * Clave del mapa: "origenId-destinoId" (ej: "66-31")
 */
public class TramoSecuencialDAO implements TramoDAO {

    // Bandera para carga única (corrección de la cátedra)
    private volatile boolean loaded = false;
    private Map<String, Tramo> cache = Collections.emptyMap();

    // Si este DAO necesitara otro DAO (ej: ParadaDAO), se inyectaría aquí con Factory:
    // private final ParadaDAO paradaDAO;
    
    // Constructor (inyección manual si fuese necesario)
    public TramoSecuencialDAO() {
        // Ejemplo de uso de Factory si fuera necesario:
        // this.paradaDAO = Factory.getInstancia("PARADA", ParadaDAO.class); 
    }


    @Override
    public Map<String, Tramo> buscarTodos() {
        // 1. Carga única (check 1)
        if (loaded) return cache;
        
        // 2. Carga única (check 2 con sincronización thread-safe)
        synchronized (this) {
            if (loaded) return cache;

            String ruta = ConfiguracionGlobal.get().require("ruta.tramos");
            char sep = ConfiguracionGlobal.get().getChar("csv.delimiter", ';');

            List<String> filas;
            try {
                filas = Files.readAllLines(Path.of(ruta));
            } catch (IOException e) {
                // Usar excepciones específicas y no System.out
                throw new IllegalStateException("No pude leer archivo de tramos: " + ruta, e);
            }

            Map<String, Tramo> out = new LinkedHashMap<>();
            for (String s : filas) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;

                String[] a = t.split("\\" + sep);
                if (a.length < 4) continue;

                // Formato esperado: linea;origen;destino;minutos;id;
                String idLinea = a[0].trim();
                String origenId = a[1].trim();
                String destinoId = a[2].trim();
                int minutos;

                try {
                    minutos = Integer.parseInt(a[3].trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalStateException("Duración inválida en: " + t, ex);
                }

                // Aquí usarías el ParadaDAO si necesitaras objetos Parada en el Tramo
                // Parada origenObj = paradaDAO.buscarPorId(origenId);

                Tramo tramo = new Tramo(idLinea, origenId, destinoId, minutos);
                // Clave requerida por la consigna/DAO: origen-destino
                out.put(origenId + "-" + destinoId, tramo);
            }

            // ELIMINADA línea: System.out.println("DEBUG → Total tramos: " + out.size());

            cache = Collections.unmodifiableMap(out);
            loaded = true; // Establecer bandera (loaded)
            return cache;
        }
    }
}