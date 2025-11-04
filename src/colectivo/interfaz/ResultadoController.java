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
    
    // --- NUEVO CAMPO ---
    // Guardamos la lista completa para saber si es conexión
    private ObservableList<Recorrido> segmentosLista = FXCollections.observableArrayList();
    // --- FIN DE NUEVO CAMPO ---

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCoordinador(CoordinadorApp coordinador) {
        this.coordinador = coordinador;
    }

    @FXML
    private void initialize() {
        
        // --- LÓGICA MODIFICADA ---
        // Columna Tipo (antes Línea)
        if (colLinea != null) {
            colLinea.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    // Primero, verifica si es "Caminando"
                    if (c.getValue().getLinea() == null) {
                        return "Caminando";
                    }
                    
                    // Segundo, verifica el tamaño de la lista total
                    if (segmentosLista.size() > 1) {
                        // Si hay más de 1 fila, es "Conexión"
                        return "Conexión (" + c.getValue().getLinea().getId() + ")";
                    } else {
                        // Si solo hay 1 fila (y no es caminando), es "Directo"
                        return "Directo (" + c.getValue().getLinea().getId() + ")"; 
                    }
                }
            ));
        }
        // --- FIN DE LÓGICA MODIFICADA ---

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
                    return salida.plusMinutes(duracion).toString();
                }
            ));
        }
        
        // Columna Espera
        if (colEspera != null) {
            colEspera.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> {
                    LocalTime salida = c.getValue().getHoraSalida();
                    if (salida == null || this.horaConsulta == null) return "-";
                    
                    // Lógica de espera: solo aplica al PRIMER segmento del viaje
                    Recorrido primerSegmento = segmentosLista.get(0);
                    if (c.getValue() == primerSegmento) {
                        long espera = ChronoUnit.MINUTES.between(this.horaConsulta, salida);
                        return String.valueOf(espera);
                    } else {
                        // Los segmentos de conexión no tienen "espera" desde la consulta original
                        return "-"; 
                    }
                }
            ));
        }
        
        // Asignar la lista a la tabla
        tblSegmentos.setItems(segmentosLista);
    }

    // --- MÉTODO MODIFICADO ---
    // Ahora actualiza la lista observable 'segmentosLista'
    public void setResultados(List<Recorrido> segmentos, LocalTime horaConsulta) {
        this.horaConsulta = horaConsulta;
        this.segmentosLista.setAll(segmentos); // Actualiza la lista observable
    }
    // --- FIN DE MÉTODO MODIFICADO ---

    @FXML
    private void onVolver() {
        if (mainController != null) {
            mainController.mostrarConsulta();
        }
    }
}