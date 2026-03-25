package JOC_DEL_PINGU;

public class Foca extends Jugador {

    // Constructor
    public Foca(int posicion, String nombre, String color) {
        // Llamamos al constructor padre. Ya no necesitamos el boolean soborno
        // porque usaremos los "turnosPenalizados" de la clase Jugador.
        super(posicion, nombre, color);
    }

    // --- INTELIGENCIA ARTIFICIAL (TURNO DE LA FOCA) ---
    public void jugarTurnoCPU(Partida p) {
        GestorEventos ge = p.getGestorEventos();
        
        // 1. Comprobar si está comiendo pescado (penalizada)
        if (this.estaPenalizado()) {
            if (ge != null) {
                ge.registrar("La foca " + this.getNombre() + " está distraída comiendo un pez. Pierde su turno.");
            }
            return; // Termina su turno sin moverse
        }

        // 2. Tirar el dado (del 1 al 6 estándar)
        int tirada = (int)(Math.random() * 6) + 1;
        this.avanzarCasillas(tirada);
        
        if (ge != null) {
            ge.registrar("La CPU (" + this.getNombre() + ") tira el dado, saca un " + tirada + " y avanza a la casilla " + this.getPosicion() + ".");
        }

        // 3. Comprobar si ha caído encima de algún pingüino
        interactuarConJugadores(p);
    }

    // --- LÓGICA DE INTERACCIÓN CON LOS PINGÜINOS ---
    public void interactuarConJugadores(Partida p) {
        for (Jugador j : p.getJugadores()) {
            
            // Si es un pingüino y están exactamente en la misma casilla
            if (j instanceof Pinguino && j.getPosicion() == this.getPosicion()) {
                
                // Buscar si el pingüino tiene un pez en su inventario
                Item pez = null;
                for (Item item : j.getInventario().getLista()) {
                    if (item.getNombre().equalsIgnoreCase("Pez")) {
                        pez = item;
                        break; // Encontramos un pez, dejamos de buscar
                    }
                }

                // CONDICIÓN A: TIENE UN PEZ (Soborno)
                if (pez != null) {
                    j.getInventario().getLista().remove(pez); // Le quitamos el pez
                    this.aplicarPenalizacion(); // Bloqueada 1 turno
                    this.aplicarPenalizacion(); // Bloqueada 2 turnos
                    
                    if (p.getGestorEventos() != null) {
                        p.getGestorEventos().registrar(j.getNombre() + " le lanza un pez a la Foca. ¡La Foca queda bloqueada 2 turnos!");
                    }
                    
                // CONDICIÓN B: NO TIENE PEZ (Castigo del coletazo)
                } else {
                    if (p.getGestorEventos() != null) {
                        p.getGestorEventos().registrar("¡La Foca " + this.getNombre() + " golpea a " + j.getNombre() + " con la cola por no darle comida!");
                    }
                    enviarAlAgujeroAnterior(j, p.getTablero(), p.getGestorEventos());
                }
            }
        }
    }

    // --- MÉTODO AUXILIAR PARA BUSCAR EL AGUJERO ANTERIOR ---
    private void enviarAlAgujeroAnterior(Jugador j, Tablero t, GestorEventos ge) {
        // Miramos las casillas hacia atrás desde la posición del jugador
        for (int i = j.getPosicion() - 1; i >= 0; i--) {
            Casilla c = t.getCasillas().get(i);
            
            if (c instanceof Agujero) {
                j.moverPosicion(i);
                if (ge != null) {
                    ge.registrar(j.getNombre() + " sale volando hasta el agujero de la casilla " + i + ".");
                }
                return; // Cortamos el método aquí
            }
        }
        
        // Si el bucle termina y no había agujeros detrás, se va a la casilla de Salida (0)
        j.moverPosicion(0);
        if (ge != null) {
            ge.registrar("No había agujeros detrás... ¡" + j.getNombre() + " sale volando hasta el inicio!");
        }
    }
}
