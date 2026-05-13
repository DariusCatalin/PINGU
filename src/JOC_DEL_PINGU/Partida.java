package JOC_DEL_PINGU;

/**
 * ============================================================
 * CLASE: Partida
 * ============================================================
 * Representa el estado completo de una partida en curso.
 * Es el objeto central que se serializa/deserializa al guardar
 * y cargar desde Oracle.
 *
 * RESPONSABILIDAD:
 *   - Mantener la lista de jugadores y el tablero.
 *   - Controlar el turno actual y el contador de rondas.
 *   - Gestionar el estado de victoria (finalizada, ganador).
 *   - Activar/desactivar el modo guerra de bolas de nieve.
 *
 * ATRIBUTOS CLAVE:
 *   tablero          → El tablero de 50 casillas.
 *   jugadores        → Lista de Jugador (Pinguino + Foca).
 *   jugadorActual    → Índice del jugador que tiene el turno.
 *   turnos           → Contador de rondas completas.
 *   finalizada       → true cuando alguien llega a la meta.
 *   ganador          → Referencia al jugador que ganó.
 *   modoGuerraActivado → Activa la guerra PvP automática.
 *   gestorEventos    → Log de mensajes del juego.
 *
 * MÉTODOS PRINCIPALES:
 *   agregarJugador(j)      → Añade jugador si hay hueco (max 4).
 *   puedeIniciarPartida()  → True si hay ≥ 2 jugadores.
 *   pasarTurno()           → Avanza al siguiente jugador, salta
 *                            penalizados, previene bucle infinito.
 *   verificarColisiones(j) → Si modoGuerra activo, comprueba
 *                            si dos jugadores están en la misma
 *                            casilla y resuelve la guerra.
 *   verificarFinPartida()  → True si algún jugador está en pos ≥ 49.
 * ============================================================
 */

import java.util.ArrayList;


public class Partida {
    
    // ==================== ATRIBUTS ====================
    private Tablero tablero;
    private ArrayList<Jugador> jugadores;
    private int turnos;
    private int jugadorActual;
    private boolean finalizada;
    private Jugador ganador;
    private int maxJugadores; // Per limitar jugadors
    private boolean modoGuerraActivado; // Només per nivell IMPOSSIBLE
    private GestorEventos gestorEventos;
    
    // ==================== CONSTRUCTOR ====================
    public Partida() {
        this.tablero = new Tablero();
        this.jugadores = new ArrayList<Jugador>();
        this.turnos = 0;
        this.jugadorActual = 0;
        this.finalizada = false;
        this.ganador = null;
        this.maxJugadores = 4; // Màxim 4 jugadors (ampliable per CPU)
        this.modoGuerraActivado = false; // Desactivat per defecte 
    }
    
    // ==================== MÈTODES DE GESTIÓ DE JUGADORS ====================
    
   
    public boolean agregarJugador(Jugador jugador) {
        if (jugadores.size() >= maxJugadores) {
            System.out.println("Màxim de jugadors assolit (" + maxJugadores + ")");
            return false;
        }
        if (jugador == null) {
            System.out.println("El jugador no pot ser null");
            return false;
        }
        jugadores.add(jugador);
        System.out.println("Jugador " + jugador.getNombre() + " afegit. Total: " + jugadores.size());
        return true;
    }
    
 
    public boolean puedeIniciarPartida() {
        return jugadores.size() >= 2;
    }
    
    // ==================== MÈTODES DE TORN ====================
    
 
    public void pasarTurno() {
        int intentos = 0;
        int totalJugadores = jugadores.size();
        boolean halt = false;
        
        do {
            jugadorActual++;
            intentos++;
            
            // Si hem donat la volta completa, incrementem el comptador de torns
            
            if (jugadorActual >= totalJugadores) {
                jugadorActual = 0;
                turnos++;
            }
            
            // Verifiquem si el jugador actual està penalitzat
            
            Jugador jActual = jugadores.get(jugadorActual);
            if (jActual.estaPenalizado()) {
                jActual.decrementarPenalizacion();
                System.out.println(jActual.getNombre() + " perd aquest torn per penalització.");
            }
            
            // Prevenció de bucle infinit (seguretat)
            
            if (intentos > totalJugadores * 2) {
                System.out.println("Error: Tots els jugadors estan penalitzats indefinidament.");
                halt = true;
            }
            
        } while (!halt && jugadores.get(jugadorActual).estaPenalizado());
    }
    
    // ==================== MÈTODES DE COL·LISIÓ ====================
    
   
    public void verificarColisiones(Jugador jugadorMovido) {
        if (modoGuerraActivado) {
            // No hi ha guerres a la sortida (0) ni a la meta (49+)
            if (jugadorMovido.getPosicion() != 0 && jugadorMovido.getPosicion() < 49) {
                for (Jugador otro : jugadores) {
                    if (otro != jugadorMovido && otro.getPosicion() == jugadorMovido.getPosicion()) {
                        resolverGuerra(jugadorMovido, otro);
                    }
                }
            }
        }
    }
    
   
    private void resolverGuerra(Jugador j1, Jugador j2) {
        System.out.println("¡GUERRA! " + j1.getNombre() + " vs " + j2.getNombre());

        int bolasJ1 = j1.contarBolas();
        int bolasJ2 = j2.contarBolas();

        // Tots dos gasten les seves boles
        
        j1.vaciarBolas();
        j2.vaciarBolas();

        if (bolasJ1 > bolasJ2) {
            int diferencia = bolasJ1 - bolasJ2;
            j2.moverPosicion(Math.max(0, j2.getPosicion() - diferencia));
            System.out.println(j1.getNombre() + " guanya. " + j2.getNombre() + 
                             " retrocedeix " + diferencia + " caselles.");
        } else if (bolasJ2 > bolasJ1) {
            int diferencia = bolasJ2 - bolasJ1;
            j1.moverPosicion(Math.max(0, j1.getPosicion() - diferencia));
            System.out.println(j2.getNombre() + " guanya. " + j1.getNombre() + 
                             " retrocedeix " + diferencia + " caselles.");
        } else {
            System.out.println("Empat! Cap jugador retrocedeix.");
        }
    }
    
    // ==================== MÈTODES DE FINALITZACIÓ ====================
    
   
    public boolean verificarFinPartida() {
        for (Jugador j : jugadores) {
            if (j.getPosicion() >= 49) {
                finalizada = true;
                ganador = j;
                System.out.println("PARTIDA FINALITZADA! Guanyador: " + j.getNombre());
                return true;
            }
        }
        return false;
    }
    
    // ==================== GETTERS I SETTERS ====================
    
    public Tablero getTablero() {
        return tablero;
    }
    
    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }
    
    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }
    
    public void setJugadores(ArrayList<Jugador> jugadores) {
        this.jugadores = jugadores;
    }
    
    public int getTurnos() {
        return turnos;
    }
    
    public void setTurnos(int turnos) {
        this.turnos = turnos;
    }
    
    public boolean isFinalizada() {
        return finalizada;
    }
    
    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }
    
    public Jugador getGanador() {
        return ganador;
    }
    
    public void setGanador(Jugador ganador) {
        this.ganador = ganador;
    }
    
    public Jugador getJugadorActual() {
        if (jugadores.isEmpty()) {
            return null;
        }
        return this.jugadores.get(this.jugadorActual);
    }
    
    public int getIndiceJugadorActual() {
        return this.jugadorActual;
    }
    
    public void setJugadorActual(int indice) {
        this.jugadorActual = indice;
    }
    
    public GestorEventos getGestorEventos() {
        return gestorEventos;
    }
    
    public void setGestorEventos(GestorEventos gestorEventos) {
        this.gestorEventos = gestorEventos;
    }
    
    public void setModoGuerraActivado(boolean activado) {
        this.modoGuerraActivado = activado;
    }
    
    public boolean isModoGuerraActivado() {
        return modoGuerraActivado;
    }
    
    public int getMaxJugadores() {
        return maxJugadores;
    }
    
    public void setMaxJugadores(int max) {
        this.maxJugadores = max;
    }
}