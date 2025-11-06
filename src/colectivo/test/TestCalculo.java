package colectivo.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import colectivo.modelo.Recorrido;
import colectivo.negocio.Calculo;

public class TestCalculo {

    private Calculo calculo;
    private Parada p1, p2, p3, p4, p5;
    private Map<String, Tramo> tramos;

    @BeforeEach
    void setUp() {

        calculo = new Calculo();

        p1 = new Parada("1", "Terminal");
        p2 = new Parada("2", "Centro");
        p3 = new Parada("3", "Hospital");
        p4 = new Parada("4", "Universidad");
        p5 = new Parada("5", "Destino");

        tramos = new HashMap<>();

        tramos.put("A-1-2", new Tramo("A", "1", "2", 5));
        tramos.put("A-2-3", new Tramo("A", "2", "3", 5));

        tramos.put("B-3-4", new Tramo("B", "3", "4", 7));

        tramos.put("W-2-4", new Tramo(null, "2", "4", 10));
    }

    @Test
    void testRutaDirecta() {
        List<List<Recorrido>> resultados = calculo.calcularRecorrido(p1, p3, 1, LocalTime.of(8, 0), tramos);
        assertNotNull(resultados, "La lista de resultados no debe ser null");
        assertFalse(resultados.isEmpty(), "Debe existir al menos una alternativa de recorrido");

        boolean existeDirecta = resultados.stream()
            .anyMatch(lista -> lista.size() == 1 && lista.get(0).getLinea() != null
                              && lista.get(0).getParadas().size() >= 2);
        assertTrue(existeDirecta, "Debe existir una ruta directa 1->3 en la misma línea");

        Optional<Recorrido> posible = resultados.stream()
            .filter(lista -> lista.size() == 1)
            .map(lista -> lista.get(0))
            .findFirst();
        assertTrue(posible.isPresent(), "Debe encontrarse un recorrido directo para validar duración");
        assertEquals(10, posible.get().getDuracion(), "La duración total de 1->3 debe ser 10 minutos");
    }

    @Test
    void testRutaConConexionVehicular() {
        List<List<Recorrido>> resultados = calculo.calcularRecorrido(p1, p4, 1, LocalTime.of(8, 0), tramos);

        boolean hayConexionVehicular = resultados.stream()
            .anyMatch(lista -> lista.size() == 2
                    && lista.get(0).getLinea() != null
                    && lista.get(1).getLinea() != null);
        assertTrue(hayConexionVehicular, "Debe existir al menos una alternativa con conexión vehicular (dos recorridos)");
    }

    @Test
    void testRutaConConexionPeatonal() {
        tramos.remove("B-3-4");

        tramos.put("B-4-5", new Tramo("B", "4", "5", 8)); // Línea B desde 4 hasta 5

        List<List<Recorrido>> resultados = calculo.calcularRecorrido(p1, p5, 1, LocalTime.of(8, 0), tramos);

        boolean hayConexionPeatonal = resultados.stream()
            .anyMatch(lista -> lista.size() == 3
                    && lista.get(0).getLinea() != null
                    && lista.get(1).getLinea() == null   
                    && lista.get(2).getLinea() != null);
        assertTrue(hayConexionPeatonal, "Debe existir una alternativa que incluya un tramo caminando entre líneas");
    }

    @Test
    void testSinRuta() {
        tramos.clear();
        tramos.put("A-1-2", new Tramo("A", "1", "2", 5));

        List<List<Recorrido>> resultados = calculo.calcularRecorrido(p1, p4, 1, LocalTime.of(8, 0), tramos);
        assertNotNull(resultados, "La lista no debe ser null");
        assertTrue(resultados.isEmpty(), "No debe existir recorrido posible hasta la parada 4");
    }
}
