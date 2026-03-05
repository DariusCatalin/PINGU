package JOC_DEL_PINGU;

import java.util.Random; 

public abstract class Jugador {
	
    // Atributos
    private int posicion;
    private String nombre;
    private String color;
    private Inventario inventario; 
    private int turnosPenalizados;

    // Constructor
    public Jugador(int posicion, String nombre, String color) {
        this.posicion = posicion;
        this.nombre = nombre;
        this.color = color;
        this.inventario = new Inventario();
        this.turnosPenalizados = 0;
    }

    public void moverPosicion(int p) {
        this.posicion = p;
    }
    
    public void avanzarCasillas(int p) {
    	this.posicion += p;
    }
    //PENALIZACIÓN
    
    public void aplicarPenalizacion() {
        this.turnosPenalizados++;
    }
 
    // Este método comprueba si está castigado Y resta un turno del castigo
    public boolean estaPenalizado() {
        if (this.turnosPenalizados > 0) {
            this.turnosPenalizados--;
            return true; 
        }
        return false; 
    }

    //PERDER OBJETO
    
    public void perderObjetoAleatorio() {
        //Accedemos a la lista del inventario
        if (this.inventario.getLista().size() > 0) {
            
            //Elegimos un número al azar entre 0 y el tamaño de la lista
            Random rand = new Random();
            int indiceAleatorio = rand.nextInt(this.inventario.getLista().size());
            
            //Obtenemos el objeto (opcional, por si quieres imprimir su nombre)

            //Borramos el objeto de la lista
            this.inventario.getLista().remove(indiceAleatorio);
            
        } else {
            System.out.println(this.nombre + " no tiene objetos para perder.");
        }
    }

    // --- Getters y Setters ---

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Inventario getInventario() {
        return inventario;
    }
    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }
 
    public int getTurnosPenalizados() { return turnosPenalizados; }
    public void setTurnosPenalizados(int turnosPenalizados) { this.turnosPenalizados = turnosPenalizados; }
}
