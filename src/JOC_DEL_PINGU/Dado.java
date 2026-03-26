package JOC_DEL_PINGU;

import java.util.Random;


public class Dado extends Item {
    
    // ==================== ATRIBUTOS ====================
    private int max;
    private int min;
    
    // ==================== CONSTRUCTOR ====================
    public Dado(String nombre, int cantidad, int max, int min) {
        super(nombre, cantidad);
        this.max = max;
        this.min = min;
    }
    
    // ==================== MÉTODOS ====================
    
  
    public int tirar(Random r) {
        if (r == null) {
            r = new Random();
        }
        int resultado = r.nextInt((this.max - this.min) + 1) + this.min;
        System.out.println("Dado " + getNombre() + " tirada: " + resultado + 
                          " (" + min + "-" + max + ")");
        return resultado;
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public int getMax() {
        return max;
    }
    
    public void setMax(int max) {
        this.max = max;
    }
    
    public int getMin() {
        return min;
    }
    
    public void setMin(int min) {
        this.min = min;
    }
}