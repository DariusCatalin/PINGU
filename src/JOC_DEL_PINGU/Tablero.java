package JOC_DEL_PINGU;
import java.util.ArrayList;
public class Tablero {
	
	private ArrayList<Casilla> casillas;
	
	public Tablero() {
		this.casillas = new ArrayList<>();
	}

	public ArrayList<Casilla> getCasillas() {
		return casillas;
	}

	public void setCasillas(ArrayList<Casilla> casillas) {
		this.casillas = casillas;
	}
	
	public void actualizarTablero() {
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
