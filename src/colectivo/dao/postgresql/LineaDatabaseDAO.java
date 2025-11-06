package colectivo.dao.postgresql;

import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.config.BDConexion; // Importar tu clase

import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LineaDatabaseDAO implements LineaDAO {
    
    private static final Logger logger = LogManager.getLogger(LineaDatabaseDAO.class);
    
    private volatile boolean loaded = false;
    private Map<String, Linea> cache = Collections.emptyMap();

    @Override
    public Map<String, Linea> buscarTodos() {
        if (loaded) return cache;
        synchronized (this) {
            if (loaded) return cache;
            
            logger.info("Cargando Líneas desde la Base de Datos...");
            
            String sql = "SELECT codigo, nombre FROM linea ORDER BY codigo";
            Map<String, Linea> map = new LinkedHashMap<>();

            try {
                Connection c = BDConexion.getConnection();
                
                try (PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        Linea l = new Linea(
                            rs.getString("codigo"),
                            rs.getString("nombre")
                        );
                        map.put(l.getId(), l);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error al consultar la tabla 'linea'", e);
                throw new RuntimeException("Error en LineaDatabaseDAO", e);
            }
            
            logger.info("Carga de Líneas desde DB completa. {} registros encontrados.", map.size());
            this.cache = Collections.unmodifiableMap(map);
            this.loaded = true;
            return this.cache;
        }
    }
}