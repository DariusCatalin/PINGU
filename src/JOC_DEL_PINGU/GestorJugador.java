package JOC_DEL_PINGU;

public class GestorJugador {

    //Un jugador usa un objeto de su inventario
    public void jugadorUsaItem(String nombreItem) {
        System.out.println("El jugador intenta usar el item: " + nombreItem);
    }

    //Mover al jugador por el tablero
    public void jugadorSeMueve(Jugador j, int pasos, Tablero t) {
        System.out.println(j.getNombre() + " se mueve " + pasos + " pasos.");
    }

    //Acabar el turno
    public void jugadorFinalizaTurno(Jugador j) {
        System.out.println(j.getNombre() + " ha finalizado su turno.");
    }

    //Cae en una casilla de evento (Ojo: en el diagrama pone 'pigüinoEvento' sin la 'n', lo corrijo a pinguino)
    public void pinguinoEvento(Pinguino p) {
        System.out.println("¡Ha ocurrido un evento para el pingüino " + p.getNombre() + "!");
    }

    //Dos pingüinos caen en la misma casilla y se pelean
    public void pinguinoGuerra(Pinguino p1, Pinguino p2) {
        System.out.println("¡Guerra! " + p1.getNombre() + " se enfrenta a " + p2.getNombre());
    }

    //Un pingüino se encuentra con la Foca
    public void focaInteractua(Pinguino p, Foca f) {
        System.out.println("La foca interactúa con el pingüino " + p.getNombre());
    }

}
