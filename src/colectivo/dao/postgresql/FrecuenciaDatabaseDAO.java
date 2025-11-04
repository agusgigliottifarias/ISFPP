/*package colectivo.dao.postgresql;

import colectivo.dao.FrecuenciaDAO;
import colectivo.modelo.Frecuencia;
import colectivo.conexion.BDConexion;
import colectivo.config.Config;

import java.sql.*;
import java.util.*;
import java.lang.reflect.*;

public class FrecuenciaDatabaseDAO implements FrecuenciaDAO {
    private final BDConexion cx;
    private final String SCHEMA = Config.getInstance().get("db.schema", "public");

    public FrecuenciaDatabaseDAO(BDConexion cx) { this.cx = cx; }

    @Override
    public List<Frecuencia> buscarTodos() {
        // Tu tabla real: linea_frecuencia(linea, diasemana, hora)
    	String sql = """
    		    SELECT linea,
    		           diasemana,
    		           to_char(hora, 'HH24:MI') AS hora_txt
    		    FROM linea_frecuencia
    		    ORDER BY linea, diasemana, hora
    		""";


        List<Frecuencia> list = new ArrayList<>();
        try (Connection c = cx.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String linea = rs.getString("linea");
                int    dia   = rs.getInt("diasemana");
                String hora  = rs.getString("hora_txt"); // "HH:mm"

                list.add(constructFrecuencia(linea, dia, hora));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // si usás log4j, reemplazá por logger.error(...)
        }
        return list;
    }

    /**
     * No tocamos tu modelo: nos adaptamos a lo que tengas.
     * - Si existe Frecuencia(String, int, String)  -> usamos ese (3 params).
     * - Si existe Frecuencia(String, String, String, int, int) -> usamos ese (5 params),
     *   mapeando horaInicio=hora, horaFin=hora, intervalo=0, tipoDia=dia.
     * - Si no, intentamos setters comunes (no-args + setters).
     */
/*
    private Frecuencia constructFrecuencia(String linea, int dia, String hora) {
        try {
            Class<Frecuencia> cls = Frecuencia.class;

            // 1) Versión 3 parámetros: (String linea, int diaSemana, String hora)
            try {
                Constructor<Frecuencia> c3 = cls.getConstructor(String.class, int.class, String.class);
                return c3.newInstance(linea, dia, hora);
            } catch (NoSuchMethodException ignored) {}

            // 2) Versión 5 parámetros: (String linea, String horaIni, String horaFin, int intervalo, int tipoDia)
            try {
                Constructor<Frecuencia> c5 = cls.getConstructor(String.class, String.class, String.class, int.class, int.class);
                return c5.newInstance(linea, hora, hora, 0, dia);
            } catch (NoSuchMethodException ignored) {}

            // 3) Fallback: no-args + setters (si existen)
            Frecuencia f = cls.getDeclaredConstructor().newInstance();
            try { cls.getMethod("setIdLinea", String.class).invoke(f, linea); } catch (NoSuchMethodException ignored) {}
            try { cls.getMethod("setTipoDia", int.class).invoke(f, dia); } catch (NoSuchMethodException ignored) {}
            try { cls.getMethod("setHoraInicio", String.class).invoke(f, hora); } catch (NoSuchMethodException ignored) {}
            try { cls.getMethod("setHoraFin", String.class).invoke(f, hora); } catch (NoSuchMethodException ignored) {}
            try { cls.getMethod("setIntervalo", int.class).invoke(f, 0); } catch (NoSuchMethodException ignored) {}
            return f;

        } catch (Exception e) {
            throw new RuntimeException(
                "No pude construir Frecuencia(linea="+linea+", dia="+dia+", hora="+hora+")", e);
        }
    }
}
*/