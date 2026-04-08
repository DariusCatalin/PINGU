package JOC_DEL_PINGU;

import java.util.ArrayList;

public class GestorEventos {
    
    // ==================== ATRIBUTS ====================
    private static final int MAX_LINEAS = 8; // Quantes línies es mostren a la tarjeta d'esdeveniments
    private final ArrayList<String> log;
    private int turnoActual = 0;
    
    // ==================== CONSTRUCTOR ====================
   
    public GestorEventos() {
        this.log = new ArrayList<String>();
    }
    
    // ==================== MÈTODES DE LOG ====================
    
   
    public void registrar(String mensaje) {
        String mensajeConTurno = turnoActual + "\t" + mensaje;
        System.out.println("[EVENTE] " + mensajeConTurno);
        log.add(mensajeConTurno);
        
        // Mantenim només les últimes MAX_LINEAS entrades
        if (log.size() > MAX_LINEAS) {
            log.remove(0);
        }
    }
    
   
    public String getLog() {
        StringBuilder sb = new StringBuilder();
        for (String linea : log) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(linea);
        }
        return sb.toString();
    }
    
   
    public void limpiar() {
        log.clear();
        System.out.println("[EVENTE] Log netejat.");
    }
    
   
    public void setTurnoActual(int turno) {
        this.turnoActual = turno;
    }
   
    public int getTurnoActual() {
        return turnoActual;
    }
    
   
    public int getLogSize() {
        return log.size();
    }
}