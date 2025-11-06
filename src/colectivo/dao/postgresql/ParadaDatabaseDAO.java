package colectivo.dao.postgresql;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import colectivo.config.BDConexion; // Importar tu clase de conexión

import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParadaDatabaseDAO implements ParadaDAO {
    
    private static final Logger logger = LogManager.getLogger(ParadaDatabaseDAO.class);
    
    private volatile boolean loaded = false;
    private Map<Integer, Parada> cache = Collections.emptyMap();

    @Override
    public Map<Integer, Parada> buscarTodos() {
        if (loaded) return cache;
        synchronized (this) {
            if (loaded) return cache;
            
            logger.info("Cargando Paradas desde la Base de Datos...");
            
            String sql = "SELECT codigo, direccion, latitud, longitud FROM parada ORDER BY codigo";
            Map<Integer, Parada> map = new LinkedHashMap<>();

            try {
                Connection c = BDConexion.getConnection(); 
                
                try (PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        int id = rs.getInt("codigo");
                        String nombre = rs.getString("direccion");
                        double lat = rs.getDouble("latitud");
                        double lon = rs.getDouble("longitud");

                        Parada p = new Parada(String.valueOf(id), nombre, lat, lon);
                        map.put(id, p); 
                    }
                }
            } catch (SQLException e) {
                logger.error("Error al consultar la tabla 'parada'", e);
                throw new RuntimeException("Error en ParadaDatabaseDAO", e);
            }
            
            logger.info("Carga de Paradas desde DB completa. {} registros encontrados.", map.size());
            this.cache = Collections.unmodifiableMap(map);
            this.loaded = true;
            return this.cache;
        }
    }
}