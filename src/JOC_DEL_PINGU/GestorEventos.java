package JOC_DEL_PINGU;

import java.util.ArrayList;

/**
 * GestorEventos acumula los mensajes del juego para mostrarlos en la UI.
 * Actúa como un log centralizado: todos los gestores reciben una referencia
 * y añaden mensajes con registrar(). La pantalla los lee con getLog().
 */
public class GestorEventos {

    private static final int MAX_LINEAS = 8; // Cuántas líneas se muestran en la tarjeta de eventos

    private final ArrayList<String> log;
    private int turnoActual = 0;

    public GestorEventos() {
        this.log = new ArrayList<>();
    }

    /** Añade un mensaje al log (y lo sigue mostrando por consola para debug). */
    public void registrar(String mensaje) {
        String mensajeConTurno = turnoActual + "\t" + mensaje;
        System.out.println("[EVENTO] " + mensajeConTurno);
        log.add(mensajeConTurno);
        // Mantenemos solo las últimas MAX_LINEAS entradas
        if (log.size() > MAX_LINEAS) {
            log.remove(0);
        }
    }

    /**
     * Devuelve los últimos eventos concatenados con saltos de línea,
     * listos para poner en un javafx.scene.text.Text.
     */
    public String getLog() {
        StringBuilder sb = new StringBuilder();
        for (String linea : log) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(linea);
        }
        return sb.toString();
    }

    /** Vacía el log (útil al iniciar una nueva partida). */
    public void limpiar() {
        log.clear();
    }

    public void setTurnoActual(int turno) {
        this.turnoActual = turno;
    }
}
