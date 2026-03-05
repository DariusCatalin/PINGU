package JOC_DEL_PINGU;
import java.util.ArrayList;
public class Tablero { // Se Crea la clase Tablero
	
	private ArrayList<Casilla> casillas; // Creamos ArrayList para guardar las casillas
	
	public Tablero() { // Constructor que llama al ArrayList creado
		this.casillas = new ArrayList<>();
	}
	
	//GETTERS/SETTERS

	public ArrayList<Casilla> getCasillas() { 
		return casillas;
	}

	public void setCasillas(ArrayList<Casilla> casillas) {
		this.casillas = casillas;
	}
	
	public void actualizarTablero() { // método que actualiza el tablero.
		System.out.println("Actualizando Tablero ... ");
		
		if(casillas.isEmpty()) {
			System.out.println("El tablero no tiene casillas !!");
		} else {
			
			for(Casilla c : casillas) {
				System.out.println("[Casilla " + c.getPosicion() + "] -> Evento: " + c.getClass().getSimpleName());
			}
			
		}
		
	}

}
