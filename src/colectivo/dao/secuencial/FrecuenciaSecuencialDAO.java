package colectivo.dao.secuencial;

import colectivo.dao.FrecuenciaDAO;
import colectivo.modelo.Frecuencia;
import colectivo.config.ConfiguracionGlobal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;

public class FrecuenciaSecuencialDAO implements FrecuenciaDAO {

    private static final int IDX_COD_LINEA = 0;
    private static final int IDX_DIA_SERVICIO = 1;
    private static final int IDX_MINUTOS = 2;

    private volatile boolean loaded = false;
    private List<Frecuencia> cache = Collections.emptyList();

    @Override
    public List<Frecuencia> buscarTodos() {
        if (loaded) return cache;
        
        synchronized (this) {
            if (loaded) return cache;

            String ruta = ConfiguracionGlobal.get().require("ruta.frecuencias");
            char sep  = ConfiguracionGlobal.get().getChar("csv.delimiter", ';');

            List<String> filas;
            try { filas = Files.readAllLines(Path.of(ruta)); }
            catch (IOException e) { 
                throw new IllegalStateException("No pude leer archivo de frecuencias: " + ruta, e); 
            }

            List<Frecuencia> out = new ArrayList<>(filas.size());
            int autoId = 1;

            for (String s : filas) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;

                String[] a = t.split("\\" + sep);
                if (a.length < 3) {
                    throw new IllegalStateException("Faltan columnas (código;día;minutos) en la línea: " + t);
                }

                String codLinea = a[IDX_COD_LINEA].trim();
                if (codLinea.isEmpty()) throw new IllegalStateException("Código de línea vacío en: " + t);

                final int diaServicio;
                try {
                    diaServicio = Integer.parseInt(a[IDX_DIA_SERVICIO].trim());
                    if (diaServicio <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Día de servicio ('" + a[IDX_DIA_SERVICIO] + "') inválido en: " + t, e);
                }

                String minStr = a[IDX_MINUTOS].trim();
                int minutos = parseMinutos(minStr, t);

                out.add(new Frecuencia(autoId++, codLinea, diaServicio, minutos));
            }

            cache = Collections.unmodifiableList(out);
            loaded = true;
            return cache;
        }
    }

    private static int parseMinutos(String v, String lineaOriginal) {
        if (v.contains(":")) {
            String[] partes = v.split(":");
            int h, m;
            
            if (partes.length >= 2) {
                h = Integer.parseInt(partes[0].trim());
                m = Integer.parseInt(partes[1].trim());
            } else {
                 throw new IllegalStateException("Formato de hora inválido en: " + lineaOriginal);
            }
            
            try {
                if (h < 0 || m < 0 || m >= 60) throw new NumberFormatException();
                return h * 60 + m;
            } catch (NumberFormatException ex) {
                throw new IllegalStateException("Hora inválida o minutos fuera de rango en: " + lineaOriginal, ex);
            }
        }
        
        try {
            return Integer.parseInt(v.trim()); 
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Minutos inválidos en: " + lineaOriginal, ex);
        }
    }
}