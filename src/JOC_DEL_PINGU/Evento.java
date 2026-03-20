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
		
		//GENERAR NÚMERO RANDOM PARA LA PROBABILIDAD
		
		int i = (int) (Math.random() * 20) + 1;
		
		//DADO LENTO
		if (i <= 7) {
			System.out.println("¡" + j.getNombre() + eventos[0]);
			j.getInventario().getLista().add(new Dado("Dado Lento", 1, 3, 1));
			
		//BOLA DE NIEVE
		} else if (i >= 8 && i <= 12) {
			int cantidadBolas = (int) (Math.random() * 3) + 1;
			System.out.println(j.getNombre() + eventos[1] + " Cantidad conseguida: " + cantidadBolas);
			j.getInventario().getLista().add(new BolaDeNieve("Bola de nieve", cantidadBolas));
			
		//PEZ
		} else if (i >= 13 && i <= 15) {
			System.out.println("¡" + j.getNombre() + eventos[2]);
			j.getInventario().getLista().add(new Pez("Pez", 1));
			
		//DADO RÁPIDO	
		} else if (i == 16 || i == 17) {
			System.out.println("¡" + j.getNombre() + eventos[3]);
			j.getInventario().getLista().add(new Dado("Dado Rápido", 1, 6, 4));
			
		//PERDER TURNO
		} else if (i == 18 || i == 19) {
			System.out.println(j.getNombre() + eventos[4]);
			
			j.aplicarPenalizacion(); 
			
		//PERDER OBJETO
		} else {
			System.out.println(j.getNombre() + eventos[5]);
			j.perderObjetoAleatorio();
		}
		
	}
}