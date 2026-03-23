package JOC_DEL_PINGU;
//SUBCLASE OSO
public class Oso extends Casilla{	
	public Oso(int posicion) {
		super(posicion);
	}	
	//FUNCIÓN REALIZAR ACCIÓN SI ES OSO
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		GestorEventos ge = p.getGestorEventos();
        
        // Comprobar si tiene pez para sobornar
        Item pez = null;
        if (j.getInventario() != null) {
            for (Item item : j.getInventario().getLista()) {
                if (item.getNombre().equalsIgnoreCase("Pez")) {
                    pez = item;
                    break;
                }
            }
        }

        if (pez != null) {
            j.getInventario().getLista().remove(pez);
            if (ge != null) ge.registrar("¡" + j.getNombre() + " soborna al oso con un pez y se queda en su sitio!");
        } else {
            if (ge != null) ge.registrar("¡" + j.getNombre() + " es atacado por un oso! Vuelve al inicio.");
            j.moverPosicion(0);
        }
	}
}
