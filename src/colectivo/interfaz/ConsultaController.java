package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;
import javafx.application.Platform; // <<<<<<<< CAMBIO 1: Nueva Importación para hilos
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; 
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL; 
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConsultaController implements Initializable {

    @FXML private ComboBox<Parada> cbOrigen;
    @FXML private ComboBox<Parada> cbDestino;
    @FXML private ChoiceBox<String> cbDiaSemana;
    @FXML private ComboBox<Integer> cbHora;
    @FXML private ComboBox<Integer> cbMinuto;
    @FXML private Button btnCalcular;
    @FXML private TableView<Recorrido> tblPreview;
    @FXML private TableColumn<Recorrido, String> colLinea;
    @FXML private TableColumn<Recorrido, String> colHoraSalida;
    @FXML private TableColumn<Recorrido, String> colDuracion;

    private CoordinadorApp coordinador;
    private MainController mainController;
    private ResourceBundle bundle; 

    public void setCoordinador(CoordinadorApp coordinador) {
        this.coordinador = coordinador;
        cargarParadas();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.bundle = resources; 
        
        cbDiaSemana.setItems(FXCollections.observableArrayList(
                bundle.getString("dia.lunes") + "(1)", 
                bundle.getString("dia.martes") + "(2)", 
                bundle.getString("dia.miercoles") + "(3)", 
                bundle.getString("dia.jueves") + "(4)", 
                bundle.getString("dia.viernes") + "(5)", 
                bundle.getString("dia.sabado") + "(6)", 
                bundle.getString("dia.domingo") + "(7)"
        ));
        
        cbDiaSemana.getSelectionModel().select(0);
        
        StringConverter<Integer> twoDigits = new StringConverter<>() {
            @Override public String toString(Integer v) { return v == null ? "" : String.format("%02d", v); }
            @Override public Integer fromString(String s) {
                try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
            }
        };

        ObservableList<Integer> horas = FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 23).boxed().collect(Collectors.toList()));
        cbHora.setItems(horas);
        cbHora.setConverter(twoDigits);
        cbHora.getSelectionModel().select(Integer.valueOf(8));

        ObservableList<Integer> minutos = FXCollections.observableArrayList(
                IntStream.rangeClosed(0, 59).boxed().collect(Collectors.toList()));
        cbMinuto.setItems(minutos);
        cbMinuto.setConverter(twoDigits);
        cbMinuto.getSelectionModel().select(Integer.valueOf(0));

        if (colLinea != null) {
            colLinea.setCellValueFactory(c -> javafx.beans.binding.Bindings.createStringBinding(
                    () -> c.getValue().getLinea() == null ? "A PIE" : c.getValue().getLinea().getId()
            ));
        }
        if (colHoraSalida != null) {
            colHoraSalida.setCellValueFactory(c -> javafx.beans.binding.Bindings.createStringBinding(
                    () -> c.getValue().getHoraSalida() == null ? "-" : c.getValue().getHoraSalida().toString()
            ));
        }
        if (colDuracion != null) {
            colDuracion.setCellValueFactory(c -> javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.valueOf(c.getValue().getDuracion())
            ));
        }

        btnCalcular.setOnAction(e -> onCalcular());
    }
    
    private void cargarParadas() {
        if (coordinador == null) return;
        Map<Integer, Parada> mapa = coordinador.getParadas();
        ObservableList<Parada> items = FXCollections.observableArrayList(
                mapa.values().stream()
                        .sorted(Comparator.comparing(Parada::getId))
                        .collect(Collectors.toList())
        );
        cbOrigen.setItems(items);
        cbDestino.setItems(items);
        if (!items.isEmpty()) {
            cbOrigen.getSelectionModel().select(0);
            cbDestino.getSelectionModel().select(Math.min(1, items.size() - 1));
        }
    }

    private int parseDiaSemana() {
        int idx = cbDiaSemana.getSelectionModel().getSelectedIndex();
        return (idx >= 0 ? idx + 1 : 1);
    }

    private static int idAsInt(Parada p) {
        try {
            return Integer.parseInt(p.getId().trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("El id de la parada no es numérico: " + p.getId());
        }
    }

    private void onCalcular() {
        Parada origen = cbOrigen.getValue();
        Parada destino = cbDestino.getValue();
        if (origen == null || destino == null) {
            new Alert(Alert.AlertType.WARNING, bundle.getString("validacion.faltan.campos")).showAndWait();
            return;
        }

        Integer hSel = cbHora.getValue();
        Integer mSel = cbMinuto.getValue();
        if (hSel == null || mSel == null) {
            new Alert(Alert.AlertType.WARNING, bundle.getString("validacion.faltan.campos")).showAndWait();
            return;
        }
        
        LocalTime hora = LocalTime.of(hSel, mSel);
        int diaSemana = parseDiaSemana();

        new Thread(() -> {
            List<List<Recorrido>> alternativas =
                    coordinador.calcularRecorrido(idAsInt(origen), idAsInt(destino), diaSemana, hora);

            Platform.runLater(() -> {
                
                if (alternativas == null || alternativas.isEmpty()) {
                    Map<String, Tramo> tramos = coordinador.getTramos();
                    String origenId = origen.getId();
                    var salientes = tramos.values().stream()
                            .filter(t -> t.getIdParadaOrigen().equals(origenId) && t.getIdLinea() != null) 
                            .collect(Collectors.toList());
                    String lineas = salientes.stream().map(Tramo::getIdLinea).distinct().sorted()
                            .collect(Collectors.joining(", "));

                    new Alert(Alert.AlertType.INFORMATION,
                            bundle.getString("sin.resultados.msg") + "\n\n" +
                            "Paradas cargadas: " + (coordinador.getParadas()!=null?coordinador.getParadas().size():"-") +
                            " | Tramos cargados: " + (tramos!=null?tramos.size():"-") + "\n" +
                            "Desde la parada de origen hay " + salientes.size() + " tramos salientes.\n" +
                            (lineas.isEmpty() ? "No hay líneas saliendo del origen."
                                            : "Líneas que salen del origen: " + lineas)
                    ).showAndWait();
                    return;
                }

                if (tblPreview != null) {
                    tblPreview.getItems().setAll(alternativas.get(0));
                }

                if (mainController != null && mainController.getResultadoController() != null) {
                    mainController.getResultadoController().setResultados(alternativas.get(0), hora); 
                    mainController.mostrarResultado();
                }
            });
        }).start();
    }
}