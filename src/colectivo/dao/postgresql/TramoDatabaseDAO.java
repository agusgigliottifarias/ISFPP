package colectivo.dao.postgresql;

import colectivo.dao.TramoDAO;
import colectivo.modelo.Tramo;
import colectivo.config.BDConexion; 

import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TramoDatabaseDAO implements TramoDAO {
    
    private static final Logger logger = LogManager.getLogger(TramoDatabaseDAO.class);

    private volatile boolean loaded = false;
    private Map<String, Tramo> cache = Collections.emptyMap();
    
    private static final int TIPO_COLECTIVO = 1;
    private static final int TIPO_CAMINANDO = 2;

    @Override
    public Map<String, Tramo> buscarTodos() {
        if (loaded) return cache;
        synchronized (this) {
            if (loaded) return cache;
            
            logger.info("Cargando Tramos desde la Base de Datos (Esquema Normalizado)...");
            
            String sql = 
                " (SELECT " +
                "   lp1.linea AS id_linea, " +
                "   CAST(lp1.parada AS VARCHAR) AS id_parada_origen, " +
                "   CAST(lp2.parada AS VARCHAR) AS id_parada_destino, " +
                "   t.tiempo AS duracion_segundos " + 
                " FROM linea_parada lp1" +
                " JOIN linea_parada lp2 ON lp1.linea = lp2.linea AND lp1.secuencia = (lp2.secuencia - 1)" +
                " JOIN tramo t ON t.inicio = lp1.parada AND t.fin = lp2.parada " +
                " WHERE t.tipo = ? )" + 
                
                " UNION ALL " +

                " (SELECT " +
                "   NULL AS id_linea," +
                "   CAST(t.inicio AS VARCHAR) AS id_parada_origen," +
                "   CAST(t.fin AS VARCHAR) AS id_parada_destino," +
                "   t.tiempo AS duracion_segundos " + 
                " FROM tramo t " +
                " WHERE t.tipo = ? )"; 

            Map<String, Tramo> map = new LinkedHashMap<>();
            try {
                Connection c = BDConexion.getConnection();
                
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    
                    ps.setInt(1, TIPO_COLECTIVO);
                    ps.setInt(2, TIPO_CAMINANDO);
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String idLinea = rs.getString("id_linea"); 
                            String origen  = rs.getString("id_parada_origen");
                            String destino = rs.getString("id_parada_destino");
                            
                            int duracionEnSegundos = rs.getInt("duracion_segundos"); 
                            int duracionEnMinutos = duracionEnSegundos / 60; 

                            Tramo tr = new Tramo(idLinea, origen, destino, duracionEnMinutos); 
                            
                            String claveUnica;
                            if (idLinea == null) {
                                claveUnica = "CAMINANDO-" + origen + "-" + destino;
                            } else {
                                claveUnica = idLinea + "-" + origen + "-" + destino;
                            }
                            map.put(claveUnica, tr);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("Error al consultar las tablas 'linea_parada' y 'tramo'", e);
                throw new RuntimeException("Error en TramoDatabaseDAO", e);
            }
            
            logger.info("Carga de Tramos desde DB completa. {} registros encontrados.", map.size());
            this.cache = Collections.unmodifiableMap(map);
            this.loaded = true;
            return this.cache;
        }
    }
}