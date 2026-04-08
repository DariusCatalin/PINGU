package JOC_DEL_PINGU;

import java.util.Random;
import java.util.ArrayList;
import java.util.Scanner;

public class GestorPartida {
    
    // ==================== ATRIBUTS ====================
    private Partida partida;
    private GestorTablero gestorTablero;
    private GestorJugador gestorJugador;
    private GestorBBDD gestorBBDD;
    private Random random;
    private GestorEventos gestorEventos;
    private int idPartidaActual; // ID únic per guardar/carregar
    
    // ==================== CONSTRUCTOR ====================
    
    
    public GestorPartida() {
        this.random = new Random();
        this.gestorTablero = new GestorTablero();
        this.gestorJugador = new GestorJugador();
        this.gestorBBDD = new GestorBBDD();
        this.gestorEventos = new GestorEventos();
        this.idPartidaActual = 1; // Per defecte, ID 1
    }
    
    // ==================== INICI DE PARTIDA ====================
    
    
    public void nuevaPartida(ArrayList<Jugador> jugadores, Tablero tablero) {
        // Verifiquem que hi ha prou jugadors (Nivell INTERMIG: mínim 2)
        if (jugadores == null || jugadores.size() < 2) {
            System.out.println("Error: Es necessiten mínim 2 jugadors per començar.");
            return;
        }
        
        // Creem la instància del model Partida
        this.partida = new Partida();
        
        // Assignem el taulell i la llista de jugadors
        this.partida.setTablero(tablero);
        this.partida.setJugadores(jugadores);
        
        // Inicialitzem els valors per defecte
        this.partida.setTurnos(0);
        this.partida.setJugadorActual(0);
        this.partida.setFinalizada(false);
        this.partida.setGestorEventos(this.gestorEventos);
        
        // Assignem posició inicial a cada jugador
        for (Jugador j : jugadores) {
            j.setPosicion(0);
            if (j instanceof Pinguino) {
                // Assegurem que cada pingüí tingui inventari
                if (j.getInventario() == null) {
                    ((Pinguino) j).setInventario(new Inventario());
                }
            }
        }
        
        // Generem un ID únic per aquesta partida
        this.idPartidaActual = (int) (System.currentTimeMillis() % 10000);
        
        // Actualitzem l'estat del taulell
        actualizarEstadoTablero();
        
        System.out.println("Partida creada. Número de Jugadors: " + jugadores.size());
        System.out.println("ID de Partida: " + this.idPartidaActual);
    }
    
    // ==================== GESTIÓ DE DADOS ====================
    
   
    public int tirarDado(Jugador j, Dado dadoOpcional) {
        Dado dadoAUsar;
        
        // Si usem un dado de l'inventari o no
        if (dadoOpcional != null) {
            dadoAUsar = dadoOpcional;
            System.out.println(j.getNombre() + " ha usat un dado especial: " + dadoAUsar.getNombre());
            // Consumim el dado (l'eliminem de l'inventari)
            j.getInventario().eliminarItem(dadoOpcional);
        } else {
            dadoAUsar = new Dado("Dado Estàndard", 1, 6, 1);
            System.out.println(j.getNombre() + " usa un dado normal.");
        }
        
        // Obtenim el número del dado i movem la posició del jugador
        int resultado = dadoAUsar.tirar(this.random);
        gestorEventos.registrar(j.getNombre() + " avança " + resultado + " caselles.");
        this.gestorJugador.jugadorSeMueve(j, resultado, this.partida.getTablero());
        
        return resultado;
    }
    
    // ==================== GESTIÓ DE TORNS ====================
    
   
    public void ejecutarTurnoCompleto() {
        // Obtenim el jugador actual
        Jugador actual = partida.getJugadorActual();
        
        if (actual == null) {
            System.out.println("Error: No hi ha jugador actual.");
            return;
        }
        
        System.out.println("Torn de " + actual.getNombre());
        
        // Processem el seu torn
        procesarTurnoJugador(actual, null);
        
        // Verifiquem si hi ha guanyador
        verificarFinPartida();
        
        // Notifiquem fi de torn (penalitzacions, reduir torns càstigs, etc.)
        this.gestorJugador.jugadorFinalizaTurno(actual);
        
        // Passem al següent torn si no s'ha acabat la partida
        if (!this.partida.isFinalizada()) {
            siguienteTurno();
        } else {
            System.out.println("¡El joc ha terminat! El guanyador és " + actual.getNombre());
        }
    }
    
  
    public int procesarTurnoJugador(Jugador j, Dado dadoOpcional) {
        // Comprovar penalització
        if (j.estaPenalizado()) {
            gestorEventos.registrar(j.getNombre() + 
                                  " no es pot moure aquest torn per penalització.");
            j.decrementarPenalizacion(); // Reduïm la penalització
            return 0;
        }
        
        // 1. Tirem el dado (centralitzat)
        int resultadoDado = tirarDado(j, dadoOpcional);
        
        // 2. Executem l'acció de la casella (amb bucle per efectes encadenats)
        int posActual;
        do {
            posActual = j.getPosicion();
            Casilla casillaActual = this.partida.getTablero().getCasillas().get(posActual);
            this.gestorTablero.ejecutarCasilla(this.partida, j, casillaActual);
            
            // Comproven fi de partida en cada pas de l'encadenament
            verificarFinPartida();
        } while (j.getPosicion() != posActual && !this.partida.isFinalizada());
        
        // 3. Actualitzem la interfície log
        actualizarEstadoTablero();
        
        return resultadoDado;
    }
    
   
    public void siguienteTurno() {
        int numTurnoActual = this.partida.getIndiceJugadorActual();
        ArrayList<Jugador> jugadores = this.partida.getJugadores();
        int total = jugadores.size();
        
        // Simplement passem al següent índex (cíclic)
        int siguienteIndice = (numTurnoActual + 1) % total;
        
        // Verifiquem que el següent jugador no estigui penalitzat
        int intentos = 0;
        while (jugadores.get(siguienteIndice).estaPenalizado() && intentos < total) {
            jugadores.get(siguienteIndice).decrementarPenalizacion();
            siguienteIndice = (siguienteIndice + 1) % total;
            intentos++;
        }
        
        this.partida.setJugadorActual(siguienteIndice);
        this.gestorEventos.setTurnoActual(this.partida.getTurnos() + 1);
        
        System.out.println("Següent torn: " + jugadores.get(siguienteIndice).getNombre());
    }
    
    // ==================== VERIFICACIÓ DE FI DE PARTIDA ====================
   
    public void verificarFinPartida() {
        for (Jugador j : this.partida.getJugadores()) {
            if (j.getPosicion() >= 49) {
                this.partida.setFinalizada(true);
                this.partida.setGanador(j);
                gestorEventos.registrar("¡PARTIDA FINALITZADA! Guanyador: " + j.getNombre());
                System.out.println("¡PARTIDA FINALITZADA! Guanyador: " + j.getNombre());
                return;
            }
        }
    }
    
    // ==================== GUARDAR I CARREGAR PARTIDA  ====================
    
  
    public void guardarPartida() {
        if (this.partida != null) {
            System.out.println("Preparant tot per guardar la partida...");
            
            // Inicialitzem la connexió si no existeix
            if (gestorBBDD.getConexion() == null) {
                System.out.println("Iniciant connexió amb la BBDD...");
                Scanner scan = new Scanner(System.in);
                gestorBBDD.iniciarConexion(scan);
            }
            
            // Guardem amb l'ID de partida
            boolean guardat = gestorBBDD.guardarBBDD(this.partida, this.idPartidaActual);
            
            if (guardat) {
                System.out.println("✅ Partida guardada correctament!");
                System.out.println("   ID de Partida: " + this.idPartidaActual);
            } else {
                System.out.println("❌ Error al guardar la partida.");
            }
        } else {
            System.out.println("No es pot guardar, no hi ha cap partida en curs.");
        }
    }
    
   
    public void cargarPartida(int idPartida) {
        System.out.println("Buscant la partida amb ID " + idPartida + " a Oracle...");
        
        // Inicialitzem la connexió si no existeix
        if (gestorBBDD.getConexion() == null) {
            System.out.println("Iniciant connexió amb la BBDD...");
            Scanner scan = new Scanner(System.in);
            gestorBBDD.iniciarConexion(scan);
        }
        
        // Carreguem la partida (retorna Partida, no Tablero!)
        Partida partidaCargada = gestorBBDD.cargarBBDD(idPartida);
        
        if (partidaCargada != null) {
            System.out.println("Dades recuperades amb èxit de la base de dades.");
            
            // Assignem la partida carregada
            this.partida = partidaCargada;
            this.idPartidaActual = idPartida;
            
            // Assignem el gestor d'esdeveniments
            this.partida.setGestorEventos(this.gestorEventos);
            
            // Actualitzem l'estat
            actualizarEstadoTablero();
            
            System.out.println("✅ Partida carregada correctament!");
            System.out.println("   Jugadors: " + this.partida.getJugadores().size());
            System.out.println("   Torn actual: " + this.partida.getIndiceJugadorActual());
        } else {
            System.out.println("❌ Fallada al carregar. Comprova si l'ID " + idPartida + " existeix.");
        }
    }
    
    // ==================== UTILITATS ====================
  
    public void actualizarEstadoTablero() {
        ArrayList<Jugador> jugadores = this.partida.getJugadores();
        System.out.println("==== ESTAT DEL TAULER ====");
        for (Jugador j : jugadores) {
            System.out.println("[SINCRONITZAT] " + j.getNombre() + 
                             " en casilla " + j.getPosicion());
        }
        System.out.println("==========================");
    }
    
    // ==================== MÈTODES DE FOCA ====================
    
   
    public void ejecutarTurnoFocas() {
        // Aquest mètode és per al Nivell IMPOSSIBLE (CPU Foca)
        // Per al Nivell INTERMIG, es pot deixar buit o no implementar
        System.out.println("[Nivell IMPOSSIBLE] Torn de les Foces...");
    }
    
    // ==================== GETTERS I SETTERS ====================
    
    public Partida getPartida() {
        return this.partida;
    }
    
    public void setPartida(Partida partida) {
        this.partida = partida;
    }
    
    public GestorEventos getGestorEventos() {
        return this.gestorEventos;
    }
    
    public void setGestorEventos(GestorEventos gestorEventos) {
        this.gestorEventos = gestorEventos;
    }
    
    public int getIdPartidaActual() {
        return this.idPartidaActual;
    }
    
    public void setIdPartidaActual(int id) {
        this.idPartidaActual = id;
    }
    
    public GestorBBDD getGestorBBDD() {
        return this.gestorBBDD;
    }
}