package colectivo.interfaz;

import colectivo.aplicacion.CoordinadorApp;

public interface Interfaz {
    void init(CoordinadorApp coordinador);

    void mostrar();

    void cerrar();
}
