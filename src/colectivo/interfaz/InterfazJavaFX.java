package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle; // <-- IMPORTAR

public class InterfazJavaFX {

    private CoordinadorApp app;

    public void init(CoordinadorApp app) {
        this.app = app;
    }

    public void mostrar() {
        runFx(() -> {
            try {
                URL url = getClass().getResource("/colectivo/interfaz/MainView.fxml");
                if (url == null) throw new IllegalStateException("No se encontró MainView.fxml");
                
                // --- LÍNEAS MODIFICADAS ---
                // Obtener el bundle desde la configuración
                ResourceBundle bundle = app.getConfig().getBundle(); 
                
                // Inyectar el bundle en el FXMLLoader
                FXMLLoader loader = new FXMLLoader(url, bundle); 
                // --- FIN DE MODIFICACIÓN ---

                Scene scene = new Scene(loader.load());
                MainController controller = loader.getController();
                controller.setCoordinador(app); // Le pasamos el coordinador (que tiene el bundle)

                Stage stage = new Stage();
                // El título ahora vendrá del FXML (o lo seteamos desde el bundle)
                stage.setTitle(bundle.getString("app.window.title")); 
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                throw new IllegalStateException("Error al inicializar la interfaz JavaFX", e);
            }
        });
    }

    void runFx(Runnable r) {
        if (javafx.application.Platform.isFxApplicationThread()) r.run();
        else javafx.application.Platform.runLater(r);
    }
}