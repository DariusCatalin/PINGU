package JOC_DEL_PINGU;

import java.util.Random;


public abstract class Jugador {
    
    // ==================== ATRIBUTS ====================
    private int posicion;
    private String nombre;
    private String color;
    private Inventario inventario;
    private int turnosPenalizados;
    
    // Constants per límits d'inventari
    private static final int MAX_DADOS = 3;
    private static final int MAX_PECES = 2;
    private static final int MAX_BOLAS_NIEVE = 6;
    
    // ==================== CONSTRUCTOR ====================
    public Jugador(int posicion, String nombre, String color) {
        this.posicion = posicion;
        this.nombre = nombre;
        this.color = color;
        this.inventario = new Inventario();
        this.turnosPenalizados = 0;
    }
    
    // ==================== MÈTODES DE MOVIMENT ====================
    
    public void moverPosicion(int p) {
        // Assegurem que la posició no sigui negativa
        this.posicion = Math.max(0, p);
    }
    
    public void avanzarCasillas(int pasos) {
        this.posicion += pasos;
    }
    
    // ==================== GESTIÓ DE PENALITZACIONS ====================
    
   
    public void aplicarPenalizacion() {
        this.turnosPenalizados++;
        System.out.println(nombre + " ha estat penalitzat. Torns perduts: " + turnosPenalizados);
    }
    
 
    public boolean estaPenalizado() {
        return this.turnosPenalizados > 0;
    }
    
    
    public void decrementarPenalizacion() {
        if (this.turnosPenalizados > 0) {
            this.turnosPenalizados--;
        }
    }
    
    // ==================== GESTIÓ DE BOLES DE NEU ====================
    
    
    public int contarBolas() {
        int count = 0;
        for (Item item : inventario.getLista()) {
            if (item != null && item.getNombre().toLowerCase().contains("bola")) {
                count++;
            }
        }
        return count;
    }
    
   
    public void vaciarBolas() {
        inventario.getLista().removeIf(item -> 
            item != null && item.getNombre().toLowerCase().contains("bola")
        );
    }
    
    // ==================== GESTIÓ D'INVENTARI ====================
    
   
    public void perderMitadInventario() {
        int total = inventario.getLista().size();
        if (total == 0) {
            System.out.println(nombre + " no té objectes per perdre.");
            return;
        }
        
        int aPerder = total / 2;
        Random rand = new Random();
        
        for (int i = 0; i < aPerder; i++) {
            if (inventario.getLista().isEmpty()) {
                break;
            }
            int index = rand.nextInt(inventario.getLista().size());
            Item eliminado = inventario.getLista().remove(index);
            System.out.println(nombre + " perd: " + (eliminado != null ? eliminado.getNombre() : "objecte"));
        }
    }
    
    
    public void perderObjetoAleatorio() {
        if (this.inventario.getLista().size() > 0) {
            Random rand = new Random();
            int indiceAleatorio = rand.nextInt(this.inventario.getLista().size());
            Item eliminado = this.inventario.getLista().remove(indiceAleatorio);
            System.out.println(nombre + " perd: " + (eliminado != null ? eliminado.getNombre() : "objecte"));
        } else {
            System.out.println(nombre + " no té objectes per perdre.");
        }
    }
    
    // ==================== VALIDACIÓ D'INVENTARI ====================
    
  
    public boolean puedeAgregarItem(Item item) {
        if (item == null) {
            return false;
        }
        
        String nombre = item.getNombre().toLowerCase();
        
        if (nombre.contains("dado")) {
            return contarTipoItem("dado") < MAX_DADOS;
        } else if (nombre.contains("pez") || nombre.contains("peix")) {
            return contarTipoItem("pez") < MAX_PECES;
        } else if (nombre.contains("bola")) {
            return contarBolas() < MAX_BOLAS_NIEVE;
        }
        
        return true; // Altres objectes sense límit
    }
    
  
    private int contarTipoItem(String tipo) {
        int count = 0;
        for (Item item : inventario.getLista()) {
            if (item != null && item.getNombre().toLowerCase().contains(tipo)) {
                count++;
            }
        }
        return count;
    }
    
    // ==================== GETTERS I SETTERS ====================
    
    public int getPosicion() {
        return posicion;
    }
    
    public void setPosicion(int posicion) {
        this.posicion = Math.max(0, posicion);
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public Inventario getInventario() {
        return inventario;
    }
    
    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }
    
    public int getTurnosPenalizados() {
        return turnosPenalizados;
    }
    
    public void setTurnosPenalizados(int turnosPenalizados) {
        this.turnosPenalizados = turnosPenalizados;
    }
}