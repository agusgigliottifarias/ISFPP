package colectivo.dao.postgresql;

import colectivo.dao.FrecuenciaDAO;
import colectivo.modelo.Frecuencia;
import colectivo.config.BDConexion; // Importar tu clase

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FrecuenciaDatabaseDAO implements FrecuenciaDAO {
    
    private static final Logger logger = LogManager.getLogger(FrecuenciaDatabaseDAO.class);
    
    private volatile boolean loaded = false;
    private List<Frecuencia> cache = Collections.emptyList();

    @Override
    public List<Frecuencia> buscarTodos() {
        if (loaded) return cache;
        synchronized (this) {
            if (loaded) return cache;

            logger.info("Cargando Frecuencias desde la Base de Datos...");
            
            String sql = "SELECT linea, diasemana, hora FROM linea_frecuencia ORDER BY linea, diasemana, hora";
            List<Frecuencia> list = new ArrayList<>();
            int autoId = 1;

            try {
                Connection c = BDConexion.getConnection();
                
                try (PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        String linea = rs.getString("linea");
                        int dia = rs.getInt("diasemana");
                        Time horaSql = rs.getTime("hora");
                        
                        LocalTime localTime = horaSql.toLocalTime();
                        int minutos = localTime.getHour() * 60 + localTime.getMinute();

                        list.add(new Frecuencia(autoId++, linea, dia, minutos));
                    }
                }
            } catch (SQLException e) {
                logger.error("Error al consultar la tabla 'linea_frecuencia'", e);
                throw new RuntimeException("Error en FrecuenciaDatabaseDAO", e);
            }
            
            logger.info("Carga de Frecuencias desde DB completa. {} registros encontrados.", list.size());
            this.cache = Collections.unmodifiableList(list);
            this.loaded = true;
            return this.cache;
        }
    }
}