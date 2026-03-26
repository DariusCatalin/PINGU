package JOC_DEL_PINGU;

import java.util.Random;

public class Foca extends Jugador {
    
    private boolean soborno;
    private int turnosBloqueado;
    
    public Foca(int posicion, String nombre, String color) {
        super(posicion, nombre, color);
        this.soborno = false;
        this.turnosBloqueado = 0;
    }
    
    public void jugarTurnoCPU(Partida p) {
        GestorEventos ge = p.getGestorEventos();
        
        // 1. Comprovar si esta penalitzada
        if (this.estaPenalizado()) {
            this.decrementarPenalizacion();
            if (ge != null) {
                ge.registrar("La foca " + this.getNombre() + " esta distraida comiendo un pez. Pierde su turno.");
            }
            return;
        }
        
        // 2. Tirar el dado (1-6 estandar)
        int tirada = (int)(Math.random() * 6) + 1;
        this.avanzarCasillas(tirada);
        
        if (ge != null) {
            ge.registrar("La CPU (" + this.getNombre() + ") tira el dado, saca un " + tirada + " y avanza a la casilla " + this.getPosicion() + ".");
        }
        
        // 3. Comprovar si ha caigut damunt d'algun pinguino
        interactuarConJugadores(p);
    }
    
    public void interactuarConJugadores(Partida p) {
        for (Jugador j : p.getJugadores()) {
            // Si es un pinguino i estan a la mateixa casella
            if (j instanceof Pinguino && j.getPosicion() == this.getPosicion()) {
                
                // Buscar si el pinguino te un peix
                Item pez = null;
                for (Item item : j.getInventario().getLista()) {
                    if (item.getNombre().equalsIgnoreCase("Pez")) {
                        pez = item;
                        break;
                    }
                }
                
                // CONDICIO A: TE UN PEIX (Suborn)
                if (pez != null) {
                    j.getInventario().getLista().remove(pez);
                    this.aplicarPenalizacion();
                    this.aplicarPenalizacion(); // Bloquejada 2 torns
                    
                    if (p.getGestorEventos() != null) {
                        p.getGestorEventos().registrar(j.getNombre() + " le lanza un pez a la Foca. ¡La Foca queda bloqueada 2 turnos!");
                    }
                    
                // CONDICIO B: NO TE PEIX (Castig)
                } else {
                    if (p.getGestorEventos() != null) {
                        p.getGestorEventos().registrar("¡La Foca " + this.getNombre() + " golpea a " + j.getNombre() + " con la cola por no darle comida!");
                    }
                    enviarAlAgujeroAnterior(j, p.getTablero(), p.getGestorEventos());
                }
            }
        }
    }
    
    private void enviarAlAgujeroAnterior(Jugador j, Tablero t, GestorEventos ge) {
        for (int i = j.getPosicion() - 1; i >= 0; i--) {
            Casilla c = t.getCasilla(i);
            if (c instanceof Agujero) {
                j.moverPosicion(i);
                if (ge != null) {
                    ge.registrar(j.getNombre() + " es enviado al agujero anterior por la Foca.");
                }
                return;
            }
        }
        j.moverPosicion(0);
        if (ge != null) {
            ge.registrar(j.getNombre() + " es enviado al inicio por la Foca (primer agujero).");
        }
    }
    
    public boolean isSoborno() {
        return soborno;
    }
    
    public void setSoborno(boolean soborno) {
        this.soborno = soborno;
    }
    
    public int getTurnosBloqueado() {
        return turnosBloqueado;
    }
    
    public void setTurnosBloqueado(int turnos) {
        this.turnosBloqueado = turnos;
    }
}