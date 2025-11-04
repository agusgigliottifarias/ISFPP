package colectivo.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

import java.util.Properties;

public final class ConfiguracionGlobal {

    private static final ConfiguracionGlobal INSTANCE = new ConfiguracionGlobal();
    private final Properties props = new Properties();

    private ConfiguracionGlobal() {
        Path ruta = Paths.get("config", "application.properties"); // <-- SOLO acá
        try (InputStream in = Files.newInputStream(ruta)) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                "No pude cargar " + ruta.toString() + ". Creá ese archivo y poné las claves.", e);
        }
    }

    public static ConfiguracionGlobal get() { return INSTANCE; }

    // --- accesos mínimos ---
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
