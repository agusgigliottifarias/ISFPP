package colectivo.dao.secuencial;

import colectivo.config.ConfiguracionGlobal;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;

/** Lee paradas desde TXT (secuencial) una sola vez, cache inmutable. */
public class ParadaSecuencialDAO implements ParadaDAO {

    private volatile boolean loaded = false;
    private Map<Integer, Parada> cache = Collections.emptyMap();

    @Override
    public Map<Integer, Parada> buscarTodos() {
        if (loaded) return cache;
        synchronized (this) {
            if (loaded) return cache;

            String ruta = ConfiguracionGlobal.get().require("ruta.paradas");
            char sep    = ConfiguracionGlobal.get().getChar("csv.delimiter", ';');

            List<String> lineas;
            try { lineas = Files.readAllLines(Path.of(ruta)); }
            catch (IOException e) {
                throw new IllegalStateException("No pude leer archivo de paradas: " + ruta, e);
            }

            Map<Integer, Parada> out = new LinkedHashMap<>();
            for (String s : lineas) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;

                String[] a = t.split("\\" + sep);
                if (a.length < 2) {
                    throw new IllegalStateException("Fila inválida (faltan columnas) en PARADAS: " + t);
                }

                final int id;
                try {
                    id = Integer.parseInt(a[0].trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalStateException("ID de parada no numérico: " + a[0], ex);
                }

                String nombre = a[1].trim();
                double lat = 0.0, lon = 0.0;
                if (a.length >= 4) {
                    try {
                        lat = Double.parseDouble(a[2].trim());
                        lon = Double.parseDouble(a[3].trim());
                    } catch (NumberFormatException ignored) { /* opcional */ }
                }


                Parada p = new Parada(String.valueOf(id), nombre, lat, lon);
                out.put(id, p); 
            }

            cache = Collections.unmodifiableMap(out);
            loaded = true;
            return cache;
        }
    }
}
