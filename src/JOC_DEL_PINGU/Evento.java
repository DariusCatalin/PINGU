package JOC_DEL_PINGU;
//SUBCLASE EVENTO
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
				" ha tenido mucha suerte! ¡Hasobtenido un dado rápido!",
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
		//BOLA DE NIEVE
		} else if (i >= 8 && i <= 12) {
			int cantidadBolas = (int) (Math.random() * 3) + 1;
			System.out.println(j.getNombre() + eventos[1] + " Cantidad conseguida: " + cantidadBolas);
		//PEZ
		} else if (i >= 13 && i <= 15) {
			System.out.println("¡" + j.getNombre() + eventos[2]);
		//DADO RÁPIDO	
		} else if (i == 16 || i == 17) {
			System.out.println("¡" + j.getNombre() + eventos[3]);
		//PERDER TURNO
		} else if (i == 18 || i == 19) {
			System.out.println(j.getNombre() + eventos[4]);
			j.penalizarTurno();
		//PERDER OBJETO
		} else {
			System.out.println(j.getNombre() + eventos[5]);
			j.perderObjeto();
		}
		
	}
}
