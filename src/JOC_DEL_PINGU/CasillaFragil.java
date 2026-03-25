package JOC_DEL_PINGU;

public class CasillaFragil extends Casilla {
	
	public CasillaFragil (int posicion) {
		super(posicion);
	}
	
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		Inventario invJugador = j.getInventario();
		
		// CAMBIO CLAVE: Simplemente contamos el tamaño del ArrayList
		int cantidadObjetos = invJugador.getLista().size(); 
		
		// VOLVER A LA CASILLA DE INICIO (Más de 5 objetos)
		
		if (cantidadObjetos > 5) {
			j.moverPosicion(0);
			if (p.getGestorEventos() != null) {
				p.getGestorEventos().registrar(j.getNombre() + " pesa demasiado. ¡Rompe el hielo y cae al inicio!");
			}
			
		// PERDER UN TURNO (Entre 1 y 5 objetos)
			
		} else if (cantidadObjetos > 0 && cantidadObjetos <= 5) {
			j.aplicarPenalizacion(); 
			if (p.getGestorEventos() != null) {
				p.getGestorEventos().registrar(j.getNombre() + " pisa hielo frágil con cuidado. Pierde un turno.");
			}
		}
		// Si tiene 0 objetos, no pasa nada y el juego sigue normal.
	}
}
