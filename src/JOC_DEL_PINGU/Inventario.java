package JOC_DEL_PINGU;

import java.util.ArrayList;


public class Inventario {
    
    // ==================== ATRIBUTS ====================
    private ArrayList<Item> lista;
    
    // Límits segons enunciat (Nivell BÁSIC/INTERMIG)
    private static final int MAX_DADOS = 3;
    private static final int MAX_PECES = 2;
    private static final int MAX_BOLAS_NIEVE = 6;
    
    // ==================== CONSTRUCTOR ====================
  
    public Inventario() {
        this.lista = new ArrayList<Item>();
    }
    
    // ==================== GESTIÓ D'ITEMS ====================
    
 
    public boolean agregarItem(Item item) {
        if (item == null) {
            System.out.println("Error: El item no pot ser null");
            return false;
        }
        
        // Verificar límits segons el tipus d'item
        if (!puedeAgregarItem(item)) {
            System.out.println("Error: Límit de " + item.getNombre() + " assolit");
            return false;
        }
        
        lista.add(item);
        System.out.println("Item afegit: " + item.getNombre() + 
                          " (Total a l'inventari: " + lista.size() + ")");
        return true;
    }
 
    public boolean puedeAgregarItem(Item item) {
        String nombre = item.getNombre().toLowerCase();
        
        if (nombre.contains("dado")) {
            return contarTipoItem("dado") < MAX_DADOS;
        } else if (nombre.contains("pez") || nombre.contains("peix")) {
            return contarTipoItem("pez") < MAX_PECES;
        } else if (nombre.contains("bola")) {
            return contarTipoItem("bola") < MAX_BOLAS_NIEVE;
        }
        
        return true; // Altres objectes sense límit
    }
    
   
    public boolean eliminarItem(Item item) {
        if (item == null) {
            return false;
        }
        boolean eliminat = lista.remove(item);
        if (eliminat) {
            System.out.println("Item eliminat: " + item.getNombre());
        }
        return eliminat;
    }
    
   
    public Item eliminarItemPorIndice(int indice) {
        if (indice >= 0 && indice < lista.size()) {
            Item eliminat = lista.remove(indice);
            System.out.println("Item eliminat: " + eliminat.getNombre());
            return eliminat;
        }
        return null;
    }
    
  
    private int contarTipoItem(String tipo) {
        int count = 0;
        for (Item item : lista) {
            if (item != null && item.getNombre().toLowerCase().contains(tipo)) {
                count++;
            }
        }
        return count;
    }
    
   
    public int tamañoInventario() {
        return lista.size();
    }
    
   
    public boolean estaVacio() {
        return lista.isEmpty();
    }
    
    
    public void vaciarInventario() {
        lista.clear();
        System.out.println("Inventari buidat completament");
    }
    
    // ==================== GETTERS I SETTERS ====================
    
    public ArrayList<Item> getLista() {
        return lista;
    }
    
    public void setLista(ArrayList<Item> lista) {
        this.lista = lista;
    }
    
    // ==================== MÈTODES ESPECÍFICS PER NIVELL INTERMIG ====================
    
   
    public boolean tieneMasDe5Objetos() {
        return lista.size() > 5;
    }
    
    
    public boolean tiene5ObjetosOMenos() {
        return lista.size() <= 5;
    }
  
    public boolean noTieneObjetos() {
        return lista.isEmpty();
    }
}