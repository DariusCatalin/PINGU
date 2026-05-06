package JOC_DEL_PINGU;

public class GestorTablero {
    
    public GestorTablero() {
        // ESTA CLASE NO NECESITA INICIALIZACIÓN, ACTÚA COMO SERVICIO DE LÓGICA DE TABLERO
    }
    
    public void ejecutarCasilla(Partida partida, Jugador j, Casilla c) {
        if (partida == null || j == null || c == null) {
            System.out.println("Error: Parametros nulos a ejecutarCasilla()");
            return;
        }
        
        System.out.println("Ejecutando la casilla para el jugador: " + j.getNombre());
        c.realizarAccion(partida, j);
    }
    
    public void comprobarFinTurno(Partida partida) {
        if (partida == null) {
            return;
        }
        
        GestorEventos ge = partida.getGestorEventos();
        int meta = 49;
        
        // CALCULAMOS LA POSICIÓN DE LA META REAL SEGÚN EL TAMAÑO DEL TABLERO ACTUAL
        if (partida.getTablero() != null && !partida.getTablero().getCasillas().isEmpty()) {
            meta = partida.getTablero().getCasillas().size() - 1;
        }
        
        // RECORREMOS TODOS LOS JUGADORES PARA VER SI ALGUNO HA LLEGADO A LA META
        for (Jugador j : partida.getJugadores()) {
            if (j.getPosicion() >= meta) {
                partida.setGanador(j);
                partida.setFinalizada(true);
                
                if (ge != null) {
                    ge.registrar("********************************");
                    ge.registrar("¡FIN DE LA PARTIDA!");
                    ge.registrar("EL GANADOR ES: " + j.getNombre());
                    ge.registrar("********************************");
                }
                
                System.out.println("¡PARTIDA FINALITZADA! Guanyador: " + j.getNombre());
                break;
            }
        }
    }
}