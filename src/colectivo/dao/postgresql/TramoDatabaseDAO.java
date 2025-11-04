/*
package colectivo.dao.postgresql;

import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;
import colectivo.conexion.BDConexion;
import colectivo.config.Config;

import java.sql.*;
import java.util.*;

public class TramoDatabaseDAO implements TramoDAO {
    private final BDConexion cx;
    private final String SCHEMA = Config.getInstance().get("db.schema", "public");

    public TramoDatabaseDAO(BDConexion cx) { this.cx = cx; }

    @Override
    public Map<String, Tramo> buscarTodos() {
    	String sql = """
    		    SELECT 
    		      COALESCE(lp1.linea, '?') AS id_linea,
    		      t.inicio   AS id_parada_origen,
    		      t.fin      AS id_parada_destino,
    		      t.tiempo   AS duracion
    		    FROM tramo t
    		    LEFT JOIN linea_parada lp1 ON lp1.parada = t.inicio
    		    LEFT JOIN linea_parada lp2 ON lp2.parada = t.fin
    		                               AND lp2.linea = lp1.linea
    		                               AND lp2.secuencia = lp1.secuencia + 1
    		""";


        Map<String, Tramo> map = new LinkedHashMap<>();
        try (Connection c = cx.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String idLinea = rs.getString("id_linea");      // "?" si no hay par consecutivo
                String origen  = rs.getString("id_parada_origen");
                String destino = rs.getString("id_parada_destino");
                int duracion   = rs.getInt("duracion");         // segundos

                Tramo tr = new Tramo(idLinea, origen, destino, duracion);
                map.put(origen + "-" + destino, tr);            // clave esperada en tu DAO
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
*/