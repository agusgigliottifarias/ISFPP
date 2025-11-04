package colectivo.interfaz;

import java.time.LocalTime;

public class SeleccionConsulta {
    private final int origenId;
    private final int destinoId;
    private final int diaSemana;
    private final LocalTime hora;

    public SeleccionConsulta(int origenId, int destinoId, int diaSemana, LocalTime hora) {
        this.origenId = origenId;
        this.destinoId = destinoId;
        this.diaSemana = diaSemana;
        this.hora = hora;
    }
    public int getOrigenId() { return origenId; }
    public int getDestinoId() { return destinoId; }
    public int getDiaSemana() { return diaSemana; }
    public LocalTime getHora() { return hora; }
}
