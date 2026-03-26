package JOC_DEL_PINGU;

public abstract class Item {
    
    // ==================== ATRIBUTOS ====================
    private String nombre;
    private int cantidad;
    
    // ==================== CONSTRUCTOR ====================
    public Item(String nombre, int cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    
    // ==================== MÉTODO TO STRING ====================
    
    @Override
    public String toString() {
        return nombre + " (Cantidad: " + cantidad + ")";
    }
}