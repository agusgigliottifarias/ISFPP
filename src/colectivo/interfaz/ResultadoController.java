package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList; 
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit; // <-- IMPORTANTE
import java.util.List;
import java.util.stream.Collectors;

public class ResultadoController {

    @FXML private TableView<Recorrido> tblSegmentos;
    @FXML private TableColumn<Recorrido, String> colLinea;
    @FXML private TableColumn<Recorrido, String> colParadas;
    @FXML private TableColumn<Recorrido, String> colSalida;
    @FXML private TableColumn<Recorrido, String> colDur;
    @FXML private TableColumn<Recorrido, String> colHoraLlegada; 
    @FXML private TableColumn<Recorrido, String> colEspera; 
    
    private MainController mainController;
    private CoordinadorApp coordinador;
    private LocalTime horaConsulta;
    
    private ObservableList<Recorrido> segmentosLista = FXCollections.observableArrayList();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCoordinador(CoordinadorApp coordinador) {
        this.coordinador = coordinador;
    }

    @FXML
    private void initialize() {
        
        // Columna Tipo
        if (colLinea != null) {
            colLinea.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    if (c.getValue().getLinea() == null) {
                        return "Caminando";
                    }
                    if (segmentosLista.size() > 1) {
                        return "Conexión (" + c.getValue().getLinea().getId() + ")";
                    } else {
                        return "Directo (" + c.getValue().getLinea().getId() + ")"; 
                    }
                }
            ));
        }

        // Columna Paradas
        if (colParadas != null) {
            colParadas.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    List<Parada> paradas = c.getValue().getParadas();
                    if (paradas == null || paradas.isEmpty()) return "";
                    return paradas.stream()
                            .map(Parada::toString) 
                            .collect(Collectors.joining(" → "));
                }
            ));
        }

        // Columna Salida
        if (colSalida != null) {
            colSalida.setCellValueFactory(c -> Bindings.createStringBinding(
                    () -> c.getValue().getHoraSalida() == null ? "-" : c.getValue().getHoraSalida().toString()
            ));
        }
        
        // Columna Duración
        if (colDur != null) {
            colDur.setCellValueFactory(c -> Bindings.createStringBinding(
                    () -> String.valueOf(c.getValue().getDuracion())
            ));
        }

        // Columna Hora Llegada
        if (colHoraLlegada != null) {
            colHoraLlegada.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    LocalTime salida = c.getValue().getHoraSalida();
                    int duracion = c.getValue().getDuracion();
                    if (salida == null) return "-";
                    // Calcula HORA LLEGADA = HORA SALIDA + DURACIÓN
                    return salida.plusMinutes(duracion).toString();
                }
            ));
        }
        
        // --- LÓGICA DE ESPERA CORREGIDA (Opción 10) ---
        if (colEspera != null) {
            colEspera.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    LocalTime salida = c.getValue().getHoraSalida();
                    if (salida == null || this.horaConsulta == null) return "-";
                    
                    Recorrido primerSegmento = segmentosLista.get(0);
                    // Solo calcular la espera para el primer segmento del viaje
                    if (c.getValue() == primerSegmento) {
                        
                        // Calcula la espera base (Salida - Consulta)
                        long espera = ChronoUnit.MINUTES.between(this.horaConsulta, salida);
                        
                        // --- INICIO DE CORRECCIÓN ---
                        // Si la espera es negativa, es del día siguiente.
                        if (espera < 0) {
                            // Sumar 24 horas (1440 minutos)
                            espera = espera + 1440; 
                        }
                        // --- FIN DE CORRECCIÓN ---
                        
                        return String.valueOf(espera);
                    } else {
                        // Los transbordos no tienen espera inicial
                        return "-"; 
                    }
                }
            ));
        }
        // --- FIN DE LÓGICA CORREGIDA ---
        
        tblSegmentos.setItems(segmentosLista);
    }

    public void setResultados(List<Recorrido> segmentos, LocalTime horaConsulta) {
        this.horaConsulta = horaConsulta;
        this.segmentosLista.setAll(segmentos); 
    }

    @FXML
    private void onVolver() {
        if (mainController != null) {
            mainController.mostrarConsulta();
        }
    }
}