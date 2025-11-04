package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import colectivo.modelo.Recorrido;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class ResultadoController {

    @FXML private TableView<Recorrido> tblSegmentos;
    @FXML private TableColumn<Recorrido, String> colLinea;
    @FXML private TableColumn<Recorrido, String> colSalida;
    @FXML private TableColumn<Recorrido, String> colDur;

    private MainController mainController;
    private CoordinadorApp coordinador;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCoordinador(CoordinadorApp coordinador) {
        this.coordinador = coordinador;
    }

    @FXML
    private void initialize() {
        if (colLinea != null) {
            colLinea.setCellValueFactory(c -> javafx.beans.binding.Bindings.createStringBinding(
                    () -> c.getValue().getLinea() == null ? "A PIE" : c.getValue().getLinea().getId()
            ));
        }
        if (colSalida != null) {
            colSalida.setCellValueFactory(c -> javafx.beans.binding.Bindings.createStringBinding(
                    () -> c.getValue().getHoraSalida() == null ? "-" : c.getValue().getHoraSalida().toString()
            ));
        }
        if (colDur != null) {
            colDur.setCellValueFactory(c -> javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.valueOf(c.getValue().getDuracion())
            ));
        }
    }

    public void setResultados(List<Recorrido> segmentos) {
        if (tblSegmentos != null) {
            tblSegmentos.setItems(FXCollections.observableArrayList(segmentos));
        }
    }

    @FXML
    private void onVolver() {
        if (mainController != null) {
            mainController.mostrarConsulta();
        }
    }
}
