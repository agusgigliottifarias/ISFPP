package colectivo.servicios;

import colectivo.modelo.Frecuencia;
import java.util.List;
import java.util.OptionalInt;

public interface FrecuenciaService {
    List<Frecuencia> listar();

    List<Frecuencia> porLinea(String idLinea); 
    
    OptionalInt primerServicioMinutos(String idLinea); 
    
    OptionalInt ultimoServicioMinutos(String idLinea);
}
