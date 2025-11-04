package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConsultaController {

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

    public void setCoordinador(CoordinadorApp coordinador) {
        this.coordinador = coordinador;
        cargarParadas();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        // Día de semana 1..7
        cbDiaSemana.setItems(FXCollections.observableArrayList(
                "Lunes(1)", "Martes(2)", "Miércoles(3)", "Jueves(4)", "Viernes(5)", "Sábado(6)", "Domingo(7)"
        ));
        cbDiaSemana.getSelectionModel().select(0);

        // Formateador 2 dígitos para ambos ComboBox
        StringConverter<Integer> twoDigits = new StringConverter<>() {
            @Override public String toString(Integer v) { return v == null ? "" : String.format("%02d", v); }
            @Override public Integer fromString(String s) {
                try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
            }
        };

        // Horas 01..23 (sin 00)
        ObservableList<Integer> horas = FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 23).boxed().collect(Collectors.toList()));
        cbHora.setItems(horas);
        cbHora.setConverter(twoDigits);
        cbHora.getSelectionModel().select(Integer.valueOf(8)); // 08

        // Minutos 00..59
        ObservableList<Integer> minutos = FXCollections.observableArrayList(
                IntStream.rangeClosed(0, 59).boxed().collect(Collectors.toList()));
        cbMinuto.setItems(minutos);
        cbMinuto.setConverter(twoDigits);
        cbMinuto.getSelectionModel().select(Integer.valueOf(0)); // 00

        // Preview columns
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
            new Alert(Alert.AlertType.WARNING, "Elegí origen y destino.").showAndWait();
            return;
        }

        Integer hSel = cbHora.getValue();
        Integer mSel = cbMinuto.getValue();
        if (hSel == null || mSel == null) {
            new Alert(Alert.AlertType.WARNING, "Elegí hora y minuto.").showAndWait();
            return;
        }
        
        // Guarda la hora de la consulta
        LocalTime hora = LocalTime.of(hSel, mSel);
        int diaSemana = parseDiaSemana();

        List<List<Recorrido>> alternativas =
                coordinador.calcularRecorrido(idAsInt(origen), idAsInt(destino), diaSemana, hora);

        if (alternativas == null || alternativas.isEmpty()) {
            // (Tu lógica de alerta de "No hay recorridos"...)
            Map<String, Tramo> tramos = coordinador.getTramos();
            String origenId = origen.getId();
            var salientes = tramos.values().stream()
                    .filter(t -> origenId.equals(t.getIdParadaOrigen()))
                    .collect(Collectors.toList());
            String lineas = salientes.stream().map(Tramo::getIdLinea).distinct().sorted()
                    .collect(Collectors.joining(", "));

            new Alert(Alert.AlertType.INFORMATION,
                    "No hay recorridos para esa combinación.\n" +
                    "Paradas cargadas: " + (coordinador.getParadas()!=null?coordinador.getParadas().size():"-") +
                    " | Tramos cargados: " + (tramos!=null?tramos.size():"-") + "\n" +
                    "Desde la parada de origen hay " + salientes.size() + " tramos salientes.\n" +
                    (lineas.isEmpty() ? "No hay líneas saliendo del origen."
                                      : "Líneas que salen del origen: " + lineas) + "\n" +
                    "Probá elegir un destino que comparta alguna de esas líneas.\n" +
                    "Si necesitás conexión a pie, agregá pares en application.properties (walking.pairs)."
            ).showAndWait();
            return;
        }

        // Preview (primera alternativa)
        if (tblPreview != null) {
            tblPreview.getItems().setAll(alternativas.get(0));
        }

        // --- LÍNEA MODIFICADA ---
        if (mainController != null && mainController.getResultadoController() != null) {
            // Pasa la 'hora' de la consulta al controlador de resultados
            mainController.getResultadoController().setResultados(alternativas.get(0), hora); 
            mainController.mostrarResultado();
        }
        // --- FIN DE LÍNEA MODIFICADA ---
    }
}