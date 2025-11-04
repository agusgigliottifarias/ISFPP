/*package colectivo.dao.postgresql;

import colectivo.dao.LineaDAO;
import colectivo.modelo.Linea;
import colectivo.conexion.BDConexion;
import colectivo.config.Config;

import java.sql.*;
import java.util.*;

public class LineaDatabaseDAO implements LineaDAO {
    private final BDConexion cx;
    private final String SCHEMA = Config.getInstance().get("db.schema", "public");

    public LineaDatabaseDAO(BDConexion cx) { this.cx = cx; }

    @Override
    public Map<String, Linea> buscarTodos() {
        // codigo; nombre  (TXT/DB)
    	String sql = "SELECT codigo, nombre FROM linea ORDER BY codigo";

        Map<String, Linea> map = new LinkedHashMap<>();
        try (Connection c = cx.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Linea l = new Linea(
                    rs.getString("codigo"),
                    rs.getString("nombre")
                );
                map.put(l.getId(), l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
*/