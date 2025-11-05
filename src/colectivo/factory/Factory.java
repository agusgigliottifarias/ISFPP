package colectivo.factory;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory / Service Locator simple con cache.
 * Soporta:
 *  - getInstancia("CLAVE")           -> usado por el Test
 *  - getInstancia("CLAVE", Tipo.class) -> cómodo para tu código
 */
public final class Factory {

    private static final ConcurrentHashMap<String, Object> INSTANCIAS = new ConcurrentHashMap<>();
    private static final ResourceBundle RB = ResourceBundle.getBundle("factory");

    private Factory() { }

    /** Versión requerida por el test: devuelve Object. */
    public static Object getInstancia(String clave) {
        return INSTANCIAS.computeIfAbsent(clave, Factory::crearInstancia);
    }

    /** Versión tipada (opcional para tu app). */
    @SuppressWarnings("unchecked")
    public static <T> T getInstancia(String clave, Class<T> type) {
        Object o = getInstancia(clave);
        if (!type.isInstance(o)) {
            throw new IllegalStateException(
                "La instancia para '" + clave + "' no es del tipo esperado: " +
                (o == null ? "null" : o.getClass().getName()) + " vs " + type.getName()
            );
        }
        return (T) o;
    }

    /** Crea la instancia a partir de factory.properties */
    private static Object crearInstancia(String clave) {
        try {
            String className = RB.getString(clave).trim();
            Class<?> clazz = Class.forName(className);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (MissingResourceException e) {
            throw new IllegalStateException("No se encontró la clave '" + clave + "' en factory.properties.", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Clase no encontrada para la clave '" + clave + "'.", e);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo instanciar la clase para '" + clave + "'.", e);
        }
    }
}
