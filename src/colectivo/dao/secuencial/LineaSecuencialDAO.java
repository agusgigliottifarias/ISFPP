package colectivo.dao.secuencial;

import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.config.ConfiguracionGlobal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;

/** Map<String(id), Linea> — id alfanumérico (ej: L1I); ignora columnas extra. */
public class LineaSecuencialDAO implements LineaDAO {

    private static final int IDX_ID     = 0; // ej: L1I
    private static final int IDX_NOMBRE = 1; // ej: "Linea 1 Ida"

    private volatile boolean loaded = false;
    private Map<String, Linea> cache = Collections.emptyMap();

    @Override
    public Map<String, Linea> buscarTodos() {
        if (loaded) return cache;
        synchronized (this) {
            if (loaded) return cache;

            String ruta = ConfiguracionGlobal.get().require("ruta.lineas");
            char sep    = ConfiguracionGlobal.get().getChar("csv.delimiter", ';');

            List<String> filas;
            try { filas = Files.readAllLines(Path.of(ruta)); }
            catch (IOException e) { throw new IllegalStateException("No pude leer: " + ruta, e); }

            Map<String, Linea> out = new HashMap<>(Math.max(16, filas.size()*2));
            for (String s : filas) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;

                String[] a = t.split("\\" + sep);
                if (a.length <= IDX_NOMBRE) throw new IllegalStateException("Faltan columnas (id;nombre) en: " + t);

                String idStr  = a[IDX_ID].trim();      // L1I/L2V/etc.
                String nombre = a[IDX_NOMBRE].trim();

                Linea l = new Linea(idStr, nombre);
                out.put(idStr, l);

                // Columnas restantes (paradas de la línea) se ignoran a propósito.
            }
            cache = Collections.unmodifiableMap(out);
            loaded = true;
            return cache;
        }
    }
}
