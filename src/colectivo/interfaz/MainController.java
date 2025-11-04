package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;

/** Controla MainView.fxml: carga Consulta/Resultado y conmuta el centro. */
public class MainController {

    @FXML private BorderPane root;          // fx:id="root" en MainView.fxml
    @FXML private MenuItem menuSalir;       // opcional si tenés un menú con salir

    private CoordinadorApp coordinador;

    private ConsultaController consultaController;
    private ResultadoController resultadoController;
    private Parent consultaPane;
    private Parent resultadoPane;

    public void setCoordinador(CoordinadorApp coordinador) {
        this.coordinador = coordinador;

        cargarConsulta();
        cargarResultado();

        if (consultaController != null)  {
            consultaController.setCoordinador(coordinador);
            consultaController.setMainController(this);
        }
        if (resultadoController != null) {
            resultadoController.setCoordinador(coordinador);
            resultadoController.setMainController(this);
        }

        if (menuSalir != null) {
            menuSalir.setOnAction(e -> javafx.application.Platform.exit());
        }

        mostrarConsulta();
    }

    private void cargarConsulta() {
        try {
            URL url = getClass().getResource("/colectivo/interfaz/ConsultaView.fxml");
            System.out.println("hola");
            if (url == null) throw new IllegalStateException("No se encontró ConsultaView.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            this.consultaPane = loader.load();
            this.consultaController = loader.getController();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar ConsultaView.fxml", e);
        }
    }

    private void cargarResultado() {
        try {
            URL url = getClass().getResource("/colectivo/interfaz/ResultadoView.fxml");
            if (url == null) throw new IllegalStateException("No se encontró ResultadoView.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            this.resultadoPane = loader.load();
            this.resultadoController = loader.getController();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar ResultadoView.fxml", e);
        }
    }

    public void mostrarConsulta() {
        if (root != null && consultaPane != null) root.setCenter(consultaPane);
    }

    public void mostrarResultado() {
        if (root != null && resultadoPane != null) root.setCenter(resultadoPane);
    }

    // === Getters para que ConsultaController pueda pedir el ResultadoController ===
    public ResultadoController getResultadoController() {
        return resultadoController;
    }

    public ConsultaController getConsultaController() {
        return consultaController;
    }

    @FXML
    private void onSalir() { // por si lo llamás desde onAction="#onSalir" en el FXML
        javafx.application.Platform.exit();
    }
}
