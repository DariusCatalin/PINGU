package JOC_DEL_PINGU;

public class Pez extends Item {
    
    // ==================== CONSTRUCTOR ====================
    public Pez(String nombre, int cantidad) {
        super(nombre, cantidad);
    }
    
    // ==================== MÉTODOS ESPECÍFICOS ====================
    

    public boolean esUsable() {
        return true;
    }
}