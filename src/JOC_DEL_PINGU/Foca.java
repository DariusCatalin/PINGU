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
        
        // 1. Comprobar si está penalizada
        if (this.estaPenalizado()) {
            this.decrementarPenalizacion();
            if (ge != null) {
                ge.registrar("La foca " + this.getNombre() + " esta distraida comiendo un pez. Pierde su turno.");
            }
        } else {
            // 2. Tirar el dado (1-6 estandar)
            int tirada = (int)(Math.random() * 6) + 1;
            this.avanzarCasillas(tirada);
            
            if (ge != null) {
                ge.registrar("La CPU (" + this.getNombre() + ") tira el dado, saca un " + tirada + " y avanza a la casilla " + this.getPosicion() + ".");
            }
            
            // 3. Comprobar si ha caído encima de algún pingüino
            interactuarConJugadores(p);
        }
    }
    
    public void interactuarConJugadores(Partida p) {
        for (Jugador j : p.getJugadores()) {
            // Si es un pingüino y están en la misma casilla
            if (j instanceof Pinguino && j.getPosicion() == this.getPosicion()) {
                
                // Buscar si el pingüino tiene un pez
                Item pez = null;
                boolean encontrado = false;
                java.util.List<Item> items = j.getInventario().getLista();
                for (int i = 0; i < items.size() && !encontrado; i++) {
                    Item item = items.get(i);
                    if (item.getNombre().equalsIgnoreCase("Pez")) {
                        pez = item;
                        encontrado = true;
                    }
                }
                
                // CONDICIÓN A: TIENE UN PEZ (Soborno)
                if (pez != null) {
                    j.getInventario().getLista().remove(pez);
                    this.aplicarPenalizacion();
                    this.aplicarPenalizacion(); // Bloqueada 2 turnos
                    
                    if (p.getGestorEventos() != null) {
                        p.getGestorEventos().registrar(j.getNombre() + " le lanza un pez a la Foca. ¡La Foca queda bloqueada 2 turnos!");
                    }
                    
                // CONDICIÓN B: NO TIENE PEZ (Castigo)
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
        boolean encontrado = false;
        for (int i = j.getPosicion() - 1; i >= 0 && !encontrado; i--) {
            Casilla c = t.getCasilla(i);
            if (c instanceof Agujero) {
                j.moverPosicion(i);
                if (ge != null) {
                    ge.registrar(j.getNombre() + " es enviado al agujero anterior por la Foca.");
                }
                encontrado = true;
            }
        }
        if (!encontrado) {
            j.moverPosicion(0);
            if (ge != null) {
                ge.registrar(j.getNombre() + " es enviado al inicio por la Foca (primer agujero).");
            }
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