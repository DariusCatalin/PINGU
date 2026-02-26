package JOC_DEL_PINGU;

public class Foca extends Jugador {
	//Atributos probios de foca
	private boolean soborno;
	
	//Constructor
	public Foca(int posicion, String nombre, String color, boolean soborno) {
		//Avisamos constructor de Jugador
		super(posicion, nombre, color);
		this.soborno = soborno;
	}
	
	public void aplastarJugador(Pinguino p) {
		System.out.println(this.getNombre() + " está intentando aplastar a " + p.getNombre());
		//Restar vida al pingüino, moverlo al inicio, etc.
	}
	
	public void golpearJugador(Pinguino p) {
		System.out.println(this.getNombre() + " ha golpeado a" + p.getNombre());
	}
	
	public void esSoborno(int cantidad) {
		if (cantidad > 0) {
			this.soborno = true;
			System.out.println("La foca ha sido sobornada");
		} else {this.soborno = false;	}
	}
	
}
