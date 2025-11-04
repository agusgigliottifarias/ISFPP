/*package colectivo.dao.postgresql;

import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;
import colectivo.conexion.BDConexion;

import java.sql.*;
import java.util.*;

public class ParadaDatabaseDAO implements ParadaDAO {
    private final BDConexion cx;

    public ParadaDatabaseDAO(BDConexion cx) { this.cx = cx; }

    @Override
    public Map<String, Parada> buscarTodos() {
        // columnas reales: codigo(int4), direccion(varchar), latitud(numeric), longitud(numeric)
        String sql = "SELECT codigo, direccion, latitud, longitud FROM parada ORDER BY codigo";

        Map<String, Parada> map = new LinkedHashMap<>();
        try (Connection c = cx.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("codigo")); // pasa int -> String
                String nombre = rs.getString("direccion");       // en tu modelo es el “nombre”
                double lat = rs.getDouble("latitud");
                double lon = rs.getDouble("longitud");

                Parada p = new Parada(id, nombre, lat, lon);
                map.put(p.getId(), p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
*/