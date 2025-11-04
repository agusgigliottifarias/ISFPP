package colectivo.aplicacion;

import colectivo.factory.Factory;
import colectivo.dao.*;
import colectivo.config.ConfiguracionGlobal;
import colectivo.modelo.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DiagnosticoMain {

    private static int fails = 0;

    public static void main(String[] args) {
        titulo("DIAGNÓSTICO ISFPP");

        // 1) factory.properties presente y claves
        checkFactory();

        // 2) Configuración y rutas de archivos
        checkConfigYArchivos();

        // 3) DAOs por Factory y carga básica
        checkDAOs();

        // 4) JavaFX + FXML + controllers + handlers comunes
        checkJavaFX();

        // 5) Coordinador + cálculo “dummy” (sin UI)
        checkCoordinadorCalculo();

        linea();
        if (fails == 0) ok("TODO OK ✅ — la app debería levantar. Si todavía falla, es un bug de wiring puntual (ver stack).");
        else err("FALLAS: " + fails + " ❌ — arreglá lo marcado arriba y volvemos a probar.");
    }

    /* ====================== BLOQUES ====================== */

    private static void checkFactory() {
        subtitulo("factory.properties");
        try {
            ResourceBundle rb = ResourceBundle.getBundle("factory");
            ok("factory.properties cargado.");
            checkClave(rb, "PARADA");
            checkClave(rb, "LINEA");
            checkClave(rb, "TRAMO");
            checkClave(rb, "FRECUENCIA");
        } catch (Exception e) {
            fail("No se pudo cargar factory.properties. Debe estar en el classpath como 'factory.properties'. " + root(e));
        }
    }

    private static void checkClave(ResourceBundle rb, String key) {
        try {
            String clazz = rb.getString(key);
            ok("Clave " + key + " -> " + clazz);
        } catch (Exception e) {
            fail("Falta clave '" + key + "' en factory.properties.");
        }
    }

    private static void checkConfigYArchivos() {
        subtitulo("application.properties + archivos TXT");
        try {
            ConfiguracionGlobal cfg = ConfiguracionGlobal.get();
            ok("Configuración cargada. Delimiter='" + cfg.getChar("csv.delimiter", ';') + "'");

            String pParadas = cfg.require("ruta.paradas");
            String pLineas  = cfg.require("ruta.lineas");
            String pTramos  = cfg.require("ruta.tramos");
            String pFreq    = cfg.require("ruta.frecuencias");

            checkFile("paradas", pParadas);
            checkFile("lineas",  pLineas);
            checkFile("tramos",  pTramos);
            checkFile("frecuencias", pFreq);

        } catch (Exception e) {
            fail("Config/rutas: " + root(e));
        }
    }

    private static void checkDAOs() {
        subtitulo("DAOs vía Factory");
        try {
            ParadaDAO pDao = Factory.getInstancia("PARADA", ParadaDAO.class);
            LineaDAO  lDao = Factory.getInstancia("LINEA",  LineaDAO.class);
            TramoDAO  tDao = Factory.getInstancia("TRAMO",  TramoDAO.class);
            FrecuenciaDAO fDao = Factory.getInstancia("FRECUENCIA", FrecuenciaDAO.class);
            ok("Instanciados DAOs.");

            Map<Integer, Parada> paradas = pDao.buscarTodos();
            Map<String, Linea> lineas    = lDao.buscarTodos();
            Map<String, Tramo> tramos    = tDao.buscarTodos();
            List<Frecuencia> frec       = fDao.buscarTodos();

            if (paradas==null||lineas==null||tramos==null||frec==null) {
                fail("Algún DAO devolvió null (todos deben devolver colecciones vacías o con datos, nunca null).");
            } else {
                ok("DAO datos: paradas=" + paradas.size() + ", lineas=" + lineas.size() + ", tramos=" + tramos.size() + ", frecuencias=" + frec.size());
            }
        } catch (Exception e) {
            fail("DAOs/Factory: " + root(e));
        }
    }

    private static void checkJavaFX() {
        subtitulo("JavaFX + FXML + Controllers");

        // Inicializamos el toolkit sin usar JFXPanel (evitamos módulos internos)
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        javafx.application.Platform.startup(() -> {
            try {
                // MainView
                checkFXML("/colectivo/interfaz/MainView.fxml",
                          "colectivo.interfaz.MainController",
                          new String[]{"onSalir"}); // si tenés ese handler

                // ConsultaView
                checkFXML("/colectivo/interfaz/ConsultaView.fxml",
                          "colectivo.interfaz.ConsultaController",
                          new String[]{"onCalcular"}); // este te fallaba antes

                // ResultadoView
                checkFXML("/colectivo/interfaz/ResultadoView.fxml",
                          "colectivo.interfaz.ResultadoController",
                          new String[]{}); // sin handlers obligatorios por ahora

            } catch (Exception e) {
                fail("JavaFX/FXML: " + root(e));
            } finally {
                latch.countDown();
            }
        });

        try { latch.await(); } catch (InterruptedException ignored) {}
    }


    private static void checkCoordinadorCalculo() {
        subtitulo("CoordinadorApp + cálculo base (sin UI)");
        try {
            CoordinadorApp app = new CoordinadorApp();
            app.inicializarAplicacion();
            ok("CoordinadorApp inicializado.");

            // smoke test de cálculo (usa datos cargados)
            Map<Integer, Parada> paradas = app.getParadas();
            if (paradas.size() >= 2) {
                int anyA = paradas.keySet().iterator().next();
                int anyB = paradas.keySet().stream().filter(x -> x != anyA).findFirst().orElse(anyA);
                app.calcularRecorrido(anyA, anyB, 1, LocalTime.of(10, 0));
                ok("CalcularRecorrido ejecutó (smoke).");
            } else {
                fail("Hay menos de 2 paradas cargadas; revisá los TXT de paradas.");
            }
        } catch (Exception e) {
            fail("Coordinador/cálculo: " + root(e));
        }
    }

    /* ====================== helpers ====================== */

    private static void checkFile(String etiqueta, String ruta) {
        File f = new File(ruta);
        if (f.exists() && f.isFile() && f.length() > 0) {
            ok("TXT " + etiqueta + " OK: " + ruta + " (" + f.length() + " bytes)");
        } else {
            fail("TXT " + etiqueta + " NO ENCONTRADO o vacío: " + ruta);
        }
    }

    private static void checkFXML(String path, String controllerFQCN, String[] handlers) {
        URL url = resource(path);
        if (url == null) {
            fail("FXML NO ENCONTRADO: " + path + " (copialo al classpath: /bin o /classes)");
            return;
        }
        ok("FXML existe: " + path);

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller == null) {
                fail("El FXML no creó controller. ¿Tiene fx:controller=\"" + controllerFQCN + "\"?");
                return;
            }
            if (!controller.getClass().getName().equals(controllerFQCN)) {
                fail("Controller inesperado: " + controller.getClass().getName() + " (se esperaba " + controllerFQCN + ")");
            } else {
                ok("Controller OK: " + controllerFQCN);
            }
            // Handlers típicos
            for (String h : handlers) {
                try {
                    Method m = findHandler(controller.getClass(), h);
                    if (m == null) {
                        fail("Handler faltante en controller: " + h + "(...)");
                    } else {
                        ok("Handler OK: " + h + " en " + controllerFQCN);
                    }
                } catch (Exception ex) {
                    fail("Error chequeando handler " + h + ": " + root(ex));
                }
            }
        } catch (Exception e) {
            fail("Al cargar " + path + ": " + root(e));
        }
    }

    private static Method findHandler(Class<?> clazz, String name) {
        // acepta firma sin params o con ActionEvent
        try { return clazz.getDeclaredMethod(name); }
        catch (NoSuchMethodException ignored) { }
        try { return clazz.getDeclaredMethod(name, javafx.event.ActionEvent.class); }
        catch (NoSuchMethodException ignored) { }
        return null;
    }

    private static URL resource(String path) {
        // prueba como recurso y como stream para mensaje más claro
        URL u = DiagnosticoMain.class.getResource(path);
        if (u == null) {
            try (InputStream is = DiagnosticoMain.class.getResourceAsStream(path)) {
                if (is != null) return DiagnosticoMain.class.getResource(path);
            } catch (Exception ignored) {}
        }
        return u;
    }

    private static String root(Throwable t) {
        Throwable x = t;
        while (x.getCause() != null) x = x.getCause();
        return x.getClass().getSimpleName() + ": " + String.valueOf(x.getMessage());
    }

    /* ====================== output ====================== */

    private static void titulo(String s)   { System.out.println("\n==== " + s + " ====\n"); }
    private static void subtitulo(String s){ System.out.println("-- " + s + " --"); }
    private static void linea()            { System.out.println("--------------------------------------------"); }
    private static void ok(String s)       { System.out.println("✔ " + s); }
    private static void fail(String s)     { System.out.println("✖ " + s); fails++; }
    private static void err(String s)      { System.out.println("⛔ " + s); }
}
