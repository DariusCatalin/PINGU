package JOC_DEL_PINGU;

public class CasillaFragil extends Casilla {
	
	//CONSTRUCTOR
	
	public CasillaFragil (int posicion) {
		super(posicion);
	}
	
	//REALIZAR ACCIÓN
	public void realizarAccion(Partida p, Jugador j) {
		//VARIABLES
		Inventario invJugador = j.getInventario();
		int cantidadObjetos = 0; 
		
		//BUCLE PARA CONTAR CUÁNTOS OBJETOS TIENE EL JUGADOR
		
		for (Item items : invJugador.getLista()) {
			cantidadObjetos += items.getCantidad();
		}
		
		//CONDICIONES
		
		//VOLVER A LA CASILLA DE INICIO
		if (cantidadObjetos > 5) {
			j.moverPosicion(0);
			if (p.getGestorEventos() != null) {
				p.getGestorEventos().registrar(j.getNombre() + " lleva muchos objetos. ¡Cae al inicio!");
			}
			
		//PERDER UN TURNO	
		} else if (cantidadObjetos > 0 && cantidadObjetos <= 5) {
			j.aplicarPenalizacion(); 
			if (p.getGestorEventos() != null) {
				p.getGestorEventos().registrar(j.getNombre() + " pisa hielo frágil. Pierde un turno.");
			}
		//NADA	
			
		} else {
			if (p.getGestorEventos() != null) {
				p.getGestorEventos().registrar(j.getNombre() + " se salva del hielo frágil (sin objetos).");
			}
		}
	}
}
