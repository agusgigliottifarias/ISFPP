// En colectivo.servicios.FrecuenciaService.java
package colectivo.servicios;
import colectivo.modelo.Frecuencia;
import java.util.List;
import java.util.OptionalInt;

public interface FrecuenciaService {
    List<Frecuencia> listar();

    // ➡️ CAMBIO: Usar String idLinea, no int
    List<Frecuencia> porLinea(String idLinea); 
    
    // ➡️ CAMBIO: Usar String idLinea, no int
    OptionalInt primerServicioMinutos(String idLinea); 
    
    // ➡️ CAMBIO: Usar String idLinea, no int
    OptionalInt ultimoServicioMinutos(String idLinea);
}/*package colectivo.servicios;

import colectivo.modelo.Frecuencia;

import java.util.List;
import java.util.OptionalInt;

public interface FrecuenciaService {
    List<Frecuencia> listar();
    List<Frecuencia> porLinea(int lineaIdInterno);
    OptionalInt primerServicioMinutos(int lineaIdInterno);
    OptionalInt ultimoServicioMinutos(int lineaIdInterno);
}*/
