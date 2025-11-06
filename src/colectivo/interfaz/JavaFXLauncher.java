package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;
import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXLauncher extends Application {
    @Override
    public void start(Stage primaryStage) {
        CoordinadorApp app = new CoordinadorApp();
        app.inicializarAplicacion();

        InterfazJavaFX ui = new InterfazJavaFX();
        ui.init(app);
        ui.mostrar(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
}
