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
			j.moverPosicion(1);
			System.out.println(j.getNombre() + " lleva muchos objetos encima, "
					+ "por lo que el suelo no ha podido aguantar y ha "
					+ "caído hasta la casilla de inicio");
		//PERDER UN TURNO	
		} else if (cantidadObjetos > 0 && cantidadObjetos <= 5) {
			j.aplicarPenalizacion();
			System.out.println("Por suerte, el suelo no se ha derrumbado, pero... " 
					+ j.getNombre() + " pierde el próximo turno.");
		//NADA	
		} else {
			System.out.println("¡" + j.getNombre() +" se ha salvado! Menos mal que no llevaba objetos encima...");
		}
	}
}
