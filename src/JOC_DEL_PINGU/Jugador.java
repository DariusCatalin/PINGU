package JOC_DEL_PINGU;

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
 
 //Penalización
 public void aplicarPenalizacion(int turnos) {
     this.turnosPenalizados += turnos;
     System.out.println(this.nombre + " ha sido penalizado " + turnos + " turnos.");
 }
 
 public boolean estaPenalizado() {
     return turnosPenalizados > 0;
 }
 
 public void gestionarTurnoPerdido() {
     if (this.turnosPenalizados > 0) {
         this.turnosPenalizados--;
         System.out.println(this.nombre + " pierde el turno. Le quedan " + this.turnosPenalizados + " turnos de castigo.");
     }
 }

 // --- Getters y Setters

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
