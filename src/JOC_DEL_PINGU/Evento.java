package JOC_DEL_PINGU;

public class Evento extends Casilla {
	
	//ATRIBUTOS
	
	private String[] eventos;
	
	//CONSTRUCTOR
	
	public Evento(int posicion) {
		super(posicion);
		this.eventos = new String[] {
				" ha obtenido un dado lento!", 
				" se ha encontrado el objeto bola de nieve!",
				" ha pescado un pez!",
				" ha tenido mucha suerte! ¡Has obtenido un dado rápido!",
				" pierde un turno.",
				" acaba de perder un objeto."
		};
	}
	
	//FUNCIÓN REALIZAR ACCIÓN SI ES EVENTO
	
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		GestorEventos ge = p.getGestorEventos();
		
		//GENERAR NÚMERO RANDOM PARA LA PROBABILIDAD
		int i = (int) (Math.random() * 20) + 1;
		
		// 1-7: DADO LENTO (Obtener dado especial 1-3)
		if (i <= 7) {
			if (ge != null) ge.registrar("¡" + j.getNombre() + " encuentra un dado lento!");
			j.getInventario().getLista().add(new Dado("Dado Lento", 1, 3, 1));
			
		// 8-12: BOLA DE NIEVE (1-3 bolas)
		} else if (i <= 12) {
			int cantidadBolas = (int) (Math.random() * 3) + 1;
			if (ge != null) ge.registrar(j.getNombre() + " recoge " + cantidadBolas + " bolas de nieve.");
			for(int n=0; n<cantidadBolas; n++) {
                j.getInventario().getLista().add(new BolaDeNieve("Bola de nieve", 1));
            }
			
		// 13-15: PEZ (Obtener un pez)
		} else if (i <= 15) {
			if (ge != null) ge.registrar("¡" + j.getNombre() + " ha pescado un pez!");
			j.getInventario().getLista().add(new Pez("Pez", 1));
			
		// 16-17: DADO RÁPIDO (Avanza 5-10 casillas, prob baja)
		} else if (i <= 17) {
			if (ge != null) ge.registrar("¡" + j.getNombre() + " tiene mucha suerte! ¡DADO RÁPIDO!");
			j.getInventario().getLista().add(new Dado("Dado Rápido", 5, 10, 1)); // Según PDF: avança 5-10 caselles
			
		// 18-19: PERDER TURNO
		} else if (i <= 19) {
			if (ge != null) ge.registrar(j.getNombre() + " pierde un turno por el mal tiempo.");
			j.aplicarPenalizacion(); 
			
		// 20: PERDER OBJETO ALEATORIO
		} else {
			if (ge != null) ge.registrar(j.getNombre() + " ha perdido un objeto por el camino.");
			j.perderObjetoAleatorio();
		}
	}
}