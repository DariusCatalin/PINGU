package JOC_DEL_PINGU;

public class Evento extends Casilla {
	
	private String[] eventos;
	
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
	
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		GestorEventos ge = p.getGestorEventos();
		
		//GENERAR NÚMERO RANDOM PARA LA PROBABILIDAD (1 a 20)
		
		int i = (int) (Math.random() * 20) + 1;
		
		// 1-7: DADO LENTO (Obtener dado especial 1-3)
		if (i <= 7) {
			if (ge != null) ge.registrar("¡" + j.getNombre() + " encuentra un dado lento!");
			j.getInventario().añadirItem(new Dado("Dado Lento", 1, 3, 1));
			
		// 8-12: BOLA DE NIEVE (1-3 bolas)
			
		} else if (i <= 12) {
			int cantidadBolas = (int) (Math.random() * 3) + 1;
			if (ge != null) ge.registrar(j.getNombre() + " intenta recoger " + cantidadBolas + " bolas de nieve.");
			for(int n=0; n<cantidadBolas; n++) {
				j.getInventario().añadirItem(new BolaDeNieve("Bola de nieve", 1));
			}
			
		// 13-15: PEZ (Obtener un pez)
			
		} else if (i <= 15) {
			if (ge != null) ge.registrar("¡" + j.getNombre() + " ha pescado un pez!");
			j.getInventario().añadirItem(new Pez("Pez", 1));
			
		// 16-17: DADO RÁPIDO 
			
		} else if (i <= 17) {
			if (ge != null) ge.registrar("¡" + j.getNombre() + " ha tenido suerte y encuentra un Dado Rápido!");
			j.getInventario().añadirItem(new Dado("Dado Rápido", 4, 6, 1));
			
		// 18-19: PERDER TURNO
			
		} else if (i <= 19) {
			if (ge != null) ge.registrar("¡Ups! " + j.getNombre() + " resbala y pierde un turno.");
			j.aplicarPenalizacion();
			
		// 20: PERDER OBJETO
			
		} else {
			if (ge != null) ge.registrar("¡Oh no! " + j.getNombre() + " acaba de perder un objeto aleatorio.");
			j.perderObjetoAleatorio();
		}
	}
}