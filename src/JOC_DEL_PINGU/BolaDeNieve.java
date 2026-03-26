package JOC_DEL_PINGU;


public class BolaDeNieve extends Item {
    
    // ==================== CONSTRUCTOR ====================
    public BolaDeNieve(String nombre, int cantidad) {
        super(nombre, cantidad);
    }
    
    // ==================== MÉTODOS ESPECÍFICOS ====================
    
   
    public boolean esUsable() {
        return true;
    }
}
