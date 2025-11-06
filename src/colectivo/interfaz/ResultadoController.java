package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList; // Importar ObservableList
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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

        if (colSalida != null) {
            colSalida.setCellValueFactory(c -> Bindings.createStringBinding(
                    () -> c.getValue().getHoraSalida() == null ? "-" : c.getValue().getHoraSalida().toString()
            ));
        }
        
        if (colDur != null) {
            colDur.setCellValueFactory(c -> Bindings.createStringBinding(
                    () -> String.valueOf(c.getValue().getDuracion())
            ));
        }

        if (colHoraLlegada != null) {
            colHoraLlegada.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    LocalTime salida = c.getValue().getHoraSalida();
                    int duracion = c.getValue().getDuracion();
                    if (salida == null) return "-";
                    return salida.plusMinutes(duracion).toString();
                }
            ));
        }
        
        if (colEspera != null) {
            colEspera.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    LocalTime salida = c.getValue().getHoraSalida();
                    if (salida == null || this.horaConsulta == null) return "-";
                    
                    Recorrido primerSegmento = segmentosLista.get(0);
                    if (c.getValue() == primerSegmento) {
                        long espera = ChronoUnit.MINUTES.between(this.horaConsulta, salida);
                        return String.valueOf(espera);
                    } else {
                        return "-"; 
                    }
                }
            ));
        }
        
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