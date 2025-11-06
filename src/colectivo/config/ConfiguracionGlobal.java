package colectivo.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale; 
import java.util.Properties;
import java.util.ResourceBundle; 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConfiguracionGlobal {

    private static final Logger logger = LogManager.getLogger(ConfiguracionGlobal.class);
    private static final ConfiguracionGlobal INSTANCE = new ConfiguracionGlobal(); 
    
    private final Properties props = new Properties();
    private final ResourceBundle bundle; 

    private ConfiguracionGlobal() {
        Path ruta = Paths.get("config", "application.properties"); 
        try (InputStream in = Files.newInputStream(ruta)) {
            props.load(in);
        } catch (IOException e) {
            logger.fatal("No pude cargar " + ruta.toString(), e);
            throw new IllegalStateException("No pude cargar " + ruta.toString(), e);
        }

        try {
            String idiomaConfig = props.getProperty("idioma.actual", "es_ES"); // Default "es_ES"
            
            String[] partes = idiomaConfig.split("_");
            Locale locale;
            if (partes.length == 1) {
                locale = new Locale(partes[0]);
            } else {
                locale = new Locale(partes[0], partes[1]);
            }
            
            this.bundle = ResourceBundle.getBundle("colectivo.interfaz.mensajes", locale);
            logger.info("Internacionalización cargada para el idioma: {}", idiomaConfig);
            
        } catch (Exception e) {
            logger.fatal("No se pudo cargar el ResourceBundle de idiomas.", e);
            throw new IllegalStateException("No se pudo cargar i18n", e);
        }
    }

    public static ConfiguracionGlobal get() { return INSTANCE; }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public String require(String key) {
        String v = props.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Falta clave obligatoria: " + key);
        }
        return v.trim();
    }

    public char getChar(String key, char def) {
        String v = props.getProperty(key);
        return (v == null || v.isBlank()) ? def : v.trim().charAt(0);
    }

    public String get(String key, String def) {
        String v = props.getProperty(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }
}