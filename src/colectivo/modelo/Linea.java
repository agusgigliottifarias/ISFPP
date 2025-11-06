package colectivo.modelo;

import java.util.Objects;

/**
 * Entidad Linea: identifica una línea de colectivo (id y nombre/numero).
 */
public class Linea {
    private String id;      
    private String nombre;  

    public Linea(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return nombre != null && !nombre.isEmpty() ? nombre : id;
    }

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Linea other = (Linea) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
