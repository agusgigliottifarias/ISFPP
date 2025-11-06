package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle; // <-- IMPORTAR

public class MainController {

    @FXML private BorderPane root;
    @FXML private MenuItem menuSalir;

    private CoordinadorApp coordinador;
    private ResourceBundle bundle; 

    private ConsultaController consultaController;
    private ResultadoController resultadoController;
    private Parent consultaPane;
    private Parent resultadoPane;

    public void setCoordinador(CoordinadorApp coordinador) {
        this.coordinador = coordinador;
        this.bundle = coordinador.getConfig().getBundle(); 

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
            if (url == null) throw new IllegalStateException("No se encontró ConsultaView.fxml");
            
            FXMLLoader loader = new FXMLLoader(url, this.bundle); 
            
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
            
            FXMLLoader loader = new FXMLLoader(url, this.bundle);
            
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

    public ResultadoController getResultadoController() { return resultadoController; }
    public ConsultaController getConsultaController() { return consultaController; }

    @FXML
    private void onSalir() {
        javafx.application.Platform.exit();
    }
}