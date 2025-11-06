package colectivo.aplicacion;

import colectivo.interfaz.*;
import javafx.application.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point del sistema. 
 * Su responsabilidad es iniciar la aplicación JavaFX.
 */
public class AplicacionPrincipal {

    private static final Logger logger = LogManager.getLogger(AplicacionPrincipal.class);

    public static void main(String[] args) {
        logger.info("🚍 Iniciando aplicación de colectivos...");

        try {
        	Application.launch(colectivo.interfaz.JavaFXLauncher.class, args);

            logger.info("✅ Interfaz JavaFX lanzada exitosamente.");
        } catch (Exception e) {
            logger.fatal("❌ ERROR CRÍTICO: Falló el lanzamiento de JavaFX.", e);
        }
    }
}
