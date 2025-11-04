package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;

public interface Interfaz {
    /** Inyecta el orquestador ya inicializado (Config -> DAOs -> Datos -> Calculo). */
    void init(CoordinadorApp coordinador);

    /** Muestra la UI. No debe lanzar lógica de negocio ni tocar DAOs. */
    void mostrar();

    /** Cierra la UI y libera recursos. */
    void cerrar();
}
