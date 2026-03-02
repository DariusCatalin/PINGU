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
		for(Casilla c : casillas) {
			
			//Aquí se aplicarán métodos
		}
		
		System.out.println("Se ha actualizado el Tablero y sus datos");
	}

}
