package colectivo.negocio;

import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;
import colectivo.modelo.Linea;
import colectivo.modelo.Frecuencia;

import java.time.LocalTime;
import java.util.*;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Calculo {

    private static final Logger logger = LogManager.getLogger(Calculo.class);
    private static final String CLAVE_CAMINANDO = "CAMINANDO";

    
    public List<List<Recorrido>> calcularRecorrido(
            Parada origen,
            Parada destino,
            int diaServicio, 
            LocalTime horaLlegaParada,
            Map<String, Tramo> tramosMap) {
        
        logger.warn("Usando sobrecarga de Calculo (5 argumentos) para TESTS.");

        Map<String, Linea> lineasSimuladas = tramosMap.values().stream()
            .filter(t -> t.getIdLinea() != null)
            .map(Tramo::getIdLinea)
            .distinct()
            .collect(Collectors.toMap(id -> id, id -> new Linea(id, "Línea " + id)));
        
        Map<Integer, Parada> paradasSimuladas = new HashMap<>();
        try {
            paradasSimuladas.put(Integer.parseInt(origen.getId()), origen);
            paradasSimuladas.put(Integer.parseInt(destino.getId()), destino);
            tramosMap.values().forEach(t -> {
                paradasSimuladas.putIfAbsent(Integer.parseInt(t.getIdParadaOrigen()), new Parada(t.getIdParadaOrigen(), "Parada " + t.getIdParadaOrigen()));
                paradasSimuladas.putIfAbsent(Integer.parseInt(t.getIdParadaDestino()), new Parada(t.getIdParadaDestino(), "Parada " + t.getIdParadaDestino()));
            });
        } catch (NumberFormatException e) {
            logger.error("Error al parsear IDs de paradas en modo TEST.", e);
        }

        return calcularRecorrido(
            origen, destino, diaServicio, horaLlegaParada, 
            tramosMap, new ArrayList<>(), lineasSimuladas, paradasSimuladas
        );
    }
    

    public List<List<Recorrido>> calcularRecorrido(
            Parada origen, Parada destino, int diaServicio, LocalTime horaLlegaParada,
            Map<String, Tramo> tramosMap, List<Frecuencia> frecuencias,
            Map<String, Linea> lineasMap, Map<Integer, Parada> paradasMap) { 

        logger.info("Iniciando cálculo: Origen={}, Destino={}", origen.getId(), destino.getId());
        
        Map<String, List<Tramo>> tramosPorTipo = agruparTramosPorTipo(tramosMap);
        Map<String, List<Frecuencia>> frecuenciasPorLinea = agruparFrecuenciasPorLinea(frecuencias);
        
        List<List<Recorrido>> resultados = new ArrayList<>();

        buscarRutaOptima(origen, destino, diaServicio, horaLlegaParada,
                         tramosPorTipo, lineasMap, frecuenciasPorLinea, 
                         paradasMap, resultados);
        
        if (resultados.isEmpty()) {
            logger.warn("Cálculo finalizado: No se encontraron recorridos.");
        } else {
            logger.info("Cálculo finalizado: Se encontraron {} alternativas.", resultados.size());
        }
        
        return resultados.stream()
            .filter(r -> r != null && !r.isEmpty())
            .collect(Collectors.toList());
    }
    
    
    private void buscarRutaOptima(Parada origen, Parada destino, int diaServicio, LocalTime horaLlegaParada,
                                  Map<String, List<Tramo>> tramosPorTipo, 
                                  Map<String, Linea> lineasMap, 
                                  Map<String, List<Frecuencia>> frecuenciasPorLinea,
                                  Map<Integer, Parada> paradasMap,
                                  List<List<Recorrido>> resultados) {
        
        String origenId = String.valueOf(origen.getId());
        String destinoId = String.valueOf(destino.getId());

        logger.debug("--- Fase 1: Buscando Rutas Directas ---");
        for (Map.Entry<String, List<Tramo>> entry : tramosPorTipo.entrySet()) {
            String idLinea = entry.getKey();
            if (idLinea.equals(CLAVE_CAMINANDO)) continue;

            Linea linea = lineasMap.get(idLinea);
            List<Tramo> tramosDeLaLinea = entry.getValue();
            List<Tramo> caminoDirecto = encontrarCaminoBFS(origenId, destinoId, tramosDeLaLinea);

            if (!caminoDirecto.isEmpty()) {
                logger.info("Ruta Directa encontrada en línea {}", idLinea);
                Recorrido r = construirRecorrido(caminoDirecto, linea, diaServicio, horaLlegaParada, 
                                                 frecuenciasPorLinea, paradasMap);
                if (r != null) resultados.add(List.of(r));
            }
        }

        logger.debug("--- Fase 2: Buscando Rutas con Conexión Vehicular ---");
        for (Map.Entry<String, List<Tramo>> entryL1 : tramosPorTipo.entrySet()) {
            String idLinea1 = entryL1.getKey();
            if (idLinea1.equals(CLAVE_CAMINANDO)) continue;
            
            Linea linea1 = lineasMap.get(idLinea1);
            List<Tramo> tramosL1 = entryL1.getValue();

            for (Map.Entry<String, List<Tramo>> entryL2 : tramosPorTipo.entrySet()) {
                String idLinea2 = entryL2.getKey();
                if (idLinea2.equals(CLAVE_CAMINANDO) || idLinea1.equals(idLinea2)) continue;
                
                Linea linea2 = lineasMap.get(idLinea2);
                List<Tramo> tramosL2 = entryL2.getValue();

                Set<String> paradasConexion = obtenerParadasAlcanzables(tramosL1);
                paradasConexion.retainAll(obtenerParadasOrigen(tramosL2)); 

                for (String idParadaConexion : paradasConexion) {
                    if (idParadaConexion.equals(origenId) || idParadaConexion.equals(destinoId)) continue;

                    List<Tramo> camino1 = encontrarCaminoBFS(origenId, idParadaConexion, tramosL1);
                    List<Tramo> camino2 = encontrarCaminoBFS(idParadaConexion, destinoId, tramosL2);

                    if (!camino1.isEmpty() && !camino2.isEmpty()) {
                        logger.info("Conexión Vehicular encontrada: A->C (Línea {}) y C->B (Línea {})", idLinea1, idLinea2);
                        Recorrido r1 = construirRecorrido(camino1, linea1, diaServicio, horaLlegaParada, frecuenciasPorLinea, paradasMap);
                        if (r1 == null) continue;
                        LocalTime horaLlegadaConexion = r1.getHoraSalida().plusMinutes(r1.getDuracion());
                        Recorrido r2 = construirRecorrido(camino2, linea2, diaServicio, horaLlegadaConexion, frecuenciasPorLinea, paradasMap);
                        if (r2 == null) continue;
                        resultados.add(List.of(r1, r2));
                    }
                }
            }
        }
        
        logger.debug("--- Fase 3: Buscando Rutas con Conexión Peatonal ---");
        List<Tramo> tramosCaminando = tramosPorTipo.getOrDefault(CLAVE_CAMINANDO, List.of());
        
        for (Map.Entry<String, List<Tramo>> entryL1 : tramosPorTipo.entrySet()) {
            String idLinea1 = entryL1.getKey();
            if (idLinea1.equals(CLAVE_CAMINANDO)) continue;
            
            Linea linea1 = lineasMap.get(idLinea1);
            List<Tramo> tramosL1 = entryL1.getValue();

            for (Map.Entry<String, List<Tramo>> entryL2 : tramosPorTipo.entrySet()) {
                String idLinea2 = entryL2.getKey();
                if (idLinea2.equals(CLAVE_CAMINANDO) || idLinea1.equals(idLinea2)) continue;

                Linea linea2 = lineasMap.get(idLinea2);
                List<Tramo> tramosL2 = entryL2.getValue();

                Set<String> paradasL1 = obtenerParadasAlcanzables(tramosL1);
                Set<String> paradasL2 = obtenerParadasOrigen(tramosL2);

                for (Tramo tramoCaminando : tramosCaminando) {
                    String idParadaC = tramoCaminando.getIdParadaOrigen();
                    String idParadaD = tramoCaminando.getIdParadaDestino();

                    if (paradasL1.contains(idParadaC) && paradasL2.contains(idParadaD)) {
                        
                        List<Tramo> camino1 = encontrarCaminoBFS(origenId, idParadaC, tramosL1);
                        if (camino1.isEmpty()) continue; 

                        List<Tramo> camino2 = encontrarCaminoBFS(idParadaD, destinoId, tramosL2);
                        if (camino2.isEmpty()) continue; 

                        logger.info("Conexión Peatonal encontrada: A->C (L1), C({})->D({}) (Walk), D->B (L2)", idParadaC, idParadaD);

                        Recorrido r1 = construirRecorrido(camino1, linea1, diaServicio, horaLlegaParada, frecuenciasPorLinea, paradasMap);
                        if (r1 == null) continue;
                        LocalTime horaLlegadaC = r1.getHoraSalida().plusMinutes(r1.getDuracion());

                        Recorrido rCaminando = construirRecorrido(List.of(tramoCaminando), null, diaServicio, horaLlegadaC, frecuenciasPorLinea, paradasMap);
                        if (rCaminando == null) continue;
                        LocalTime horaLlegadaD = horaLlegadaC.plusMinutes(rCaminando.getDuracion());

                        Recorrido r2 = construirRecorrido(camino2, linea2, diaServicio, horaLlegadaD, frecuenciasPorLinea, paradasMap);
                        if (r2 == null) continue;

                        resultados.add(List.of(r1, rCaminando, r2));
                    }
                }
            }
        }
        
    }

    private Recorrido construirRecorrido(List<Tramo> camino, Linea linea, int diaServicio, 
                                         LocalTime horaLlegaParada, 
                                         Map<String, List<Frecuencia>> frecuenciasPorLinea, 
                                         Map<Integer, Parada> paradasMap) {
        
        if (camino == null || camino.isEmpty()) {
            logger.error("Error al construir recorrido: el camino está vacío.");
            return null;
        }

        int duracionTotal = camino.stream().mapToInt(Tramo::getDuracion).sum();
        
        LocalTime horaSalida;
        if (linea != null) { 
            horaSalida = calcularProximaSalida(linea.getId(), diaServicio, horaLlegaParada, frecuenciasPorLinea);
        } else { 
            horaSalida = horaLlegaParada; 
        }

        List<Parada> paradasDelCamino = new ArrayList<>();
        try {
            String idPrimeraParada = camino.get(0).getIdParadaOrigen();
            Parada pOrigen = paradasMap.get(Integer.parseInt(idPrimeraParada));
            if (pOrigen == null) pOrigen = new Parada(idPrimeraParada, "Parada " + idPrimeraParada);
            paradasDelCamino.add(pOrigen);

            for (Tramo t : camino) {
                String idParadaDestino = t.getIdParadaDestino();
                Parada pDestino = paradasMap.get(Integer.parseInt(idParadaDestino));
                if (pDestino == null) pDestino = new Parada(idParadaDestino, "Parada " + idParadaDestino);
                paradasDelCamino.add(pDestino);
            }
        } catch (Exception e) {
            logger.error("Error crítico al reconstruir paradas del camino.", e);
            return null;
        }

        return new Recorrido(linea, paradasDelCamino, horaSalida, duracionTotal);
    }

    private List<Tramo> encontrarCaminoBFS(String origenId, String destinoId, List<Tramo> tramosDeLaLinea) {
        
        Map<String, List<Tramo>> adj = new HashMap<>();
        for (Tramo t : tramosDeLaLinea) {
            adj.computeIfAbsent(t.getIdParadaOrigen(), k -> new ArrayList<>()).add(t);
        }
        if (adj.isEmpty()) return Collections.emptyList();

        Queue<String> cola = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        Map<String, Tramo> tramoPadre = new HashMap<>(); 

        cola.offer(origenId);
        visitados.add(origenId);
        tramoPadre.put(origenId, null);

        while (!cola.isEmpty()) {
            String paradaActualId = cola.poll();

            if (paradaActualId.equals(destinoId)) {
                return reconstruirCamino(origenId, destinoId, tramoPadre);
            }

            if (adj.containsKey(paradaActualId)) {
                for (Tramo tramoSaliente : adj.get(paradaActualId)) {
                    String paradaVecinaId = tramoSaliente.getIdParadaDestino();
                    if (!visitados.contains(paradaVecinaId)) {
                        visitados.add(paradaVecinaId);
                        cola.offer(paradaVecinaId);
                        tramoPadre.put(paradaVecinaId, tramoSaliente); 
                    }
                }
            } 
        }
        
        return Collections.emptyList();
    }

    private List<Tramo> reconstruirCamino(String origenId, String destinoId, Map<String, Tramo> tramoPadre) {
        LinkedList<Tramo> camino = new LinkedList<>();
        String paradaActual = destinoId;
        
        while (tramoPadre.get(paradaActual) != null) {
            Tramo t = tramoPadre.get(paradaActual);
            camino.addFirst(t);
            paradaActual = t.getIdParadaOrigen(); 
            if (camino.size() > 1000) {
                logger.error("Posible bucle infinito en reconstrucción de camino.");
                return Collections.emptyList();
            }
        }
        return camino;
    }
    
    private Map<String, List<Tramo>> agruparTramosPorTipo(Map<String, Tramo> tramosMap) {
        return tramosMap.values().stream()
            .collect(Collectors.groupingBy(
                t -> t.getIdLinea() == null ? CLAVE_CAMINANDO : t.getIdLinea()
            ));
    }
    
    private Map<String, List<Frecuencia>> agruparFrecuenciasPorLinea(List<Frecuencia> frecuencias) {
        return frecuencias.stream().collect(Collectors.groupingBy(Frecuencia::getIdLinea));
    }
    
    private Set<String> obtenerParadasAlcanzables(List<Tramo> tramos) {
        return tramos.stream().map(Tramo::getIdParadaDestino).collect(Collectors.toSet());
    }
    
    private Set<String> obtenerParadasOrigen(List<Tramo> tramos) {
        return tramos.stream().map(Tramo::getIdParadaOrigen).collect(Collectors.toSet());
    }

    private LocalTime calcularProximaSalida(
            String idLinea, 
            int diaServicio, 
            LocalTime horaLlega, 
            Map<String, List<Frecuencia>> frecuenciasPorLinea) {
        
        List<Frecuencia> frecuenciasValidas = frecuenciasPorLinea.getOrDefault(idLinea, List.of()).stream()
            .filter(f -> f.getDiaServicio() == diaServicio)
            .sorted(Comparator.comparingInt(Frecuencia::getMinutos))
            .toList();
        
        if (frecuenciasValidas.isEmpty()) {
             logger.warn("No hay frecuencias para Línea {} en día {}", idLinea, diaServicio);
             return horaLlega.plus(24, ChronoUnit.HOURS); 
        }
        
        int minutosLlegada = (int) ChronoUnit.MINUTES.between(LocalTime.MIDNIGHT, horaLlega);
        
        for (Frecuencia f : frecuenciasValidas) {
             int minutosSalida = f.getMinutos(); 
             if (minutosSalida >= minutosLlegada) {
                 return LocalTime.of(0, 0).plusMinutes(minutosSalida);
             }
        }
        
        Frecuencia primerServicio = frecuenciasValidas.get(0);
        LocalTime horaPrimerServicio = LocalTime.of(0, 0).plusMinutes(primerServicio.getMinutos());
        return horaPrimerServicio; 
    }
}