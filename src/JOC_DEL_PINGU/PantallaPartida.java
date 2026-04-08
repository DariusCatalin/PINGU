package JOC_DEL_PINGU;

import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;

public class PantallaPartida {

    // --- INTERFAZ (FXML) ---
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    @FXML private Button dado;
    @FXML private javafx.scene.control.Label lblDadoTitulo;
    @FXML private Button rapido;
    @FXML private Button lento;
    @FXML private Button peces;
    @FXML private Button nieve;

    @FXML private Text dadoResultText;
    @FXML private Text rapido_t;
    @FXML private Text lento_t;
    @FXML private Text peces_t;
    @FXML private Text nieve_t;
    @FXML private javafx.scene.control.TextArea eventos;

    @FXML private GridPane tablero;
    @FXML private Circle P1; // Pingüino Jugador
    @FXML private Circle P2; // Foca CPU
    @FXML private Circle P3;
    @FXML private Circle P4;

    // --- LÓGICA DEL JUEGO (EL CEREBRO) ---
    private Partida partida;
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
    
    // Gestor de eventos simple para mostrar textos en la pantalla
    private GestorEventos gestorUI = new GestorEventos() {
        @Override
        public void registrar(String mensaje) {
            System.out.println(mensaje);
            if (eventos != null) {
                eventos.appendText(mensaje + "\n");
                // Auto-scroll the TextArea to bottom
                eventos.setScrollTop(Double.MAX_VALUE);
            }
        }
    };

    // Jugadores pre-configurados (recibidos desde PantallaConfig)
    private java.util.ArrayList<Jugador> jugadoresConfig = null;

    /**
     * Llamado por PantallaConfig antes de mostrar la pantalla,
     * para pasar la lista de jugadores configurados.
     */
    public void setJugadores(java.util.ArrayList<Jugador> jugadores) {
        this.jugadoresConfig = jugadores;
        // Si initialize() ya se ejecutó, re-lanzamos la configuración
        if (partida != null) {
            configurarPartida();
        }
    }

    @FXML
    private void initialize() {
        System.out.println("Cargando Pantalla Partida...");
        cargarSkins();

        // 1. INICIALIZAMOS LA PARTIDA
        partida = new Partida();
        partida.setGestorEventos(gestorUI);

        // 2. Si llegaron jugadores desde PantallaConfig, los usamos.
        //    Si no, creamos uno por defecto (modo fallback / reinicio).
        if (jugadoresConfig != null) {
            configurarPartida();
        } else {
            // Fallback: 1 Pingüino + 1 Foca CPU por defecto
            Pinguino jugador1 = new Pinguino(0, "Pingu", "Azul");
            Foca cpuFoca = new Foca(0, "Focabrón", "Rojo");
            partida.getJugadores().add(jugador1);
            partida.getJugadores().add(cpuFoca);

            actualizarPosicionVisual(jugador1, P1);
            actualizarPosicionVisual(cpuFoca, P2);
            actualizarTextosInventario(jugador1);
            actualizarTextosTurno();
            mostrarTiposDeCasillasEnTablero(partida.getTablero());
            gestorUI.registrar("¡Partida iniciada! Tu turno, " + jugador1.getNombre());
        }
    }

    /** Configura la partida usando los jugadores recibidos de PantallaConfig. */
    private void configurarPartida() {
        partida.getJugadores().clear();
        for (Jugador j : jugadoresConfig) {
            partida.getJugadores().add(j);
        }

        // Fichas visuales disponibles en el FXML
        Circle[] fichas = {P1, P2, P3, P4};

        // Ocultar todas primero
        for (Circle c : fichas) {
            if (c != null) c.setVisible(false);
        }

        // Asignar y mostrar las fichas para cada jugador
        for (int i = 0; i < jugadoresConfig.size() && i < fichas.length; i++) {
            Jugador j = jugadoresConfig.get(i);
            Circle ficha = fichas[i];
            if (ficha != null) {
                ficha.setVisible(true);
                actualizarPosicionVisual(j, ficha);
            }
        }

        // Actualizar inventario del primer pingüino
        for (Jugador j : jugadoresConfig) {
            if (j instanceof Pinguino) {
                actualizarTextosInventario(j);
                break;
            }
        }
        
        actualizarTextosTurno();

        mostrarTiposDeCasillasEnTablero(partida.getTablero());
        gestorUI.registrar("¡Partida iniciada con " + jugadoresConfig.size() + " jugadores!");
    }

    // ==========================================
    // MENÚ ARCHIVO (EVENTOS)
    // ==========================================
    @FXML
    private void handleNewGame(ActionEvent event) {
        gestorUI.registrar("Reiniciando partida...");
        initialize(); // Volvemos a empezar
    }

    @FXML
    private void handleSaveGame(ActionEvent event) {
        gestorUI.registrar("Guardando partida... (Función no implementada aún)");
        // TODO: Conectar con GestorBBDD
    }

    @FXML
    private void handleLoadGame(ActionEvent event) {
        gestorUI.registrar("Cargando partida... (Función no implementada aún)");
        // TODO: Conectar con GestorBBDD
    }

    @FXML
    private void handleQuitGame(ActionEvent event) {
        System.out.println("Saliendo del juego...");
        System.exit(0);
    }

    // ==========================================
    // ACCIÓN PRINCIPAL: TIRAR EL DADO NORMAL
    // ==========================================
    @FXML
    private void handleDado(ActionEvent event) {
        if (partida.isFinalizada()) return;

        Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
        
        if (actual instanceof Pinguino) {
            if (actual.estaPenalizado()) {
                actual.decrementarPenalizacion();
                gestorUI.registrar("¡" + actual.getNombre() + " está penalizado y pierde este turno!");
            } else {
                int tirada = (int)(Math.random() * 6) + 1;
                moverJugadorYAccion(actual, tirada, "tirada normal");
                if (dadoResultText != null) dadoResultText.setText("Has sacado un: " + tirada);
            }
            
            actualizarTextosInventario(actual);
            actualizarPosicionVisual(actual, getFichaVisual(actual)); 
            avanzarTurno();

            procesarTurnosCPU();
        }
    }

    // ==========================================
    // USO DE OBJ ETOS DEL INVENTARIO
    // ==========================================
    @FXML
    private void handleRapido(ActionEvent event) {
        usarDadoEspecial("Dado Rápido", 4, 6); 
    }
    
    @FXML
    private void handleLento(ActionEvent event) {
        usarDadoEspecial("Dado Lento", 1, 3); 
    }
    
    @FXML
    private void handlePeces(ActionEvent event) {
        usarPez(event);
    }
    
    @FXML
    private void handleNieve(ActionEvent event) {
        usarBolaNieve(event);
    }

    private void usarDadoEspecial(String nombreDado, int min, int max) {
        if (partida.isFinalizada()) return;
        Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());

        if (actual instanceof Pinguino) {
            if (consumirObjeto(actual, nombreDado)) {
                int tirada = (int)(Math.random() * (max - min + 1)) + min;
                moverJugadorYAccion(actual, tirada, nombreDado);
                
                if (dadoResultText != null) dadoResultText.setText("Has sacado un: " + tirada + " (" + nombreDado + ")");
                
                actualizarTextosInventario(actual);
                actualizarPosicionVisual(actual, getFichaVisual(actual));
                avanzarTurno();

                procesarTurnosCPU();
            } else {
                gestorUI.registrar("¡No tienes ningún " + nombreDado + " en la mochila!");
            }
        }
    }

    @FXML
    private void usarPez(ActionEvent event) {
        Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
        if (actual instanceof Pinguino) {
            if (tieneObjeto(actual, "Pez")) {
                gestorUI.registrar("¡Tienes un Pez listo! Te protegerá de la Foca.");
            } else {
                gestorUI.registrar("No tienes Peces. ¡Huye de la Foca!");
            }
        }
    }

    @FXML
    private void usarBolaNieve(ActionEvent event) {
        Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
        if (actual instanceof Pinguino) {
            int cantidad = contarBolas(actual);
            gestorUI.registrar("Tienes " + cantidad + " Bolas de Nieve listas para la guerra.");
        }
    }

    // ==========================================
    // LÓGICA INTERNA DE TURNOS Y CASILLAS
    // ==========================================

    private void jugarTurnoCPU_IA(Foca foca, Circle fichaVisual) {
        if (foca.estaPenalizado()) {
            foca.decrementarPenalizacion();
            gestorUI.registrar("La foca " + foca.getNombre() + " está entretenida comiendo. Pierde su turno.");
        } else {
            int tirada = (int)(Math.random() * 6) + 1;
            moverJugadorYAccion(foca, tirada, "tirada CPU");
        }
        
        actualizarPosicionVisual(foca, fichaVisual);
        avanzarTurno(); // Devuelve el turno al jugador
    }

    private void moverJugadorYAccion(Jugador j, int tirada, String contexto) {
        int posInicial = j.getPosicion();
        int nuevaPos = j.getPosicion() + tirada;

        // REGLA: META EXACTA (49)
        if (nuevaPos > 49) {
            nuevaPos = 49 - (nuevaPos - 49);
            gestorUI.registrar("¡" + j.getNombre() + " ha sacado un " + tirada + " y se ha pasado! Rebota hasta la " + nuevaPos);
        } else {
            gestorUI.registrar(j.getNombre() + " (" + contexto + ") saca un " + tirada + " y avanza a la casilla " + nuevaPos);
        }

        // REGLA FOCA: Pasar por encima
        if (j instanceof Foca) {
            int maxRecorrido = Math.min(49, posInicial + tirada);
            for (Jugador p : partida.getJugadores()) {
                if (!(p instanceof Foca) && p.getPosicion() > posInicial && p.getPosicion() <= maxRecorrido) {
                    if (p.getPosicion() != nuevaPos) { // La posición final se procesa en el choque
                        p.perderMitadInventario();
                        gestorUI.registrar("¡La Foca le roba la mitad del equipaje a " + p.getNombre() + " al pasar por encima!");
                    }
                }
            }
        }

        j.moverPosicion(nuevaPos);
        ejecutarLogicaCasilla(j);
    }

    private void ejecutarLogicaCasilla(Jugador j) {
        if (j.getPosicion() >= 49) {
            j.moverPosicion(49);
            partida.setFinalizada(true);
            gestorUI.registrar("¡" + j.getNombre() + " HA LLEGADO A LA META Y GANA LA PARTIDA!");
            return;
        }
        
        // Ejecuta la casilla (Oso, Trineo, Evento...)
        Casilla casillaActual = partida.getTablero().getCasillas().get(j.getPosicion());
        casillaActual.realizarAccion(partida, j);
        
        // Comprobar colisiones
        verificarColisionesLocal(j);
    }

    private void verificarColisionesLocal(Jugador actual) {
        for (Jugador otro : partida.getJugadores()) {
            if (otro != actual && actual.getPosicion() == otro.getPosicion() && actual.getPosicion() > 0) {
                
                // Si alguien choca contra la Foca
                if (actual instanceof Foca || otro instanceof Foca) {
                    Jugador pinguino = (actual instanceof Pinguino) ? actual : otro;
                    Foca foca = (actual instanceof Foca) ? (Foca) actual : (Foca) otro;
                    
                    if (consumirObjeto(pinguino, "Pez")) {
                        gestorUI.registrar("¡" + pinguino.getNombre() + " le lanza un Pez a la Foca para salvarse!");
                        foca.aplicarPenalizacion(); 
                    } else {
                        // Castigo: mitad de objetos y retroceder al agujero anterior
                        pinguino.perderMitadInventario();
                        int posHole = buscarAgujeroAnterior(pinguino.getPosicion(), partida.getTablero());
                        pinguino.moverPosicion(posHole);
                        gestorUI.registrar("¡La foca atrapa a " + pinguino.getNombre() + "! Pierde la mitad de objetos y vuelve al agujero en " + posHole);
                    }
                } else if (actual instanceof Pinguino && otro instanceof Pinguino) {
                    // GUERRA DE BOLAS (PvP)
                    int bolasA = actual.contarBolas();
                    int bolasO = otro.contarBolas();
                    actual.vaciarBolas();
                    otro.vaciarBolas();
                    
                    if (bolasA > bolasO) {
                        int diff = bolasA - bolasO;
                        otro.moverPosicion(Math.max(0, otro.getPosicion() - diff));
                        gestorUI.registrar("¡Guerra de bolas! " + actual.getNombre() + " gana a " + otro.getNombre() + " por " + diff + " bolas.");
                    } else if (bolasO > bolasA) {
                        int diff = bolasO - bolasA;
                        actual.moverPosicion(Math.max(0, actual.getPosicion() - diff));
                        gestorUI.registrar("¡Guerra de bolas! " + otro.getNombre() + " gana a " + actual.getNombre() + " por " + diff + " bolas.");
                    } else {
                        gestorUI.registrar("¡Guerra de bolas EMPATE entre " + actual.getNombre() + " y " + otro.getNombre() + "! Gastan todo pero nadie retrocede.");
                    }
                }
            }
        }
    }

    private int buscarAgujeroAnterior(int pos, Tablero t) {
        for (int i = pos - 1; i >= 0; i--) {
            if (t.getCasillas().get(i) instanceof Agujero) return i;
        }
        return 0;
    }

    private void avanzarTurno() {
        int turnoActual = partida.getIndiceJugadorActual(); 
        int siguienteTurno = (turnoActual + 1) % partida.getJugadores().size(); 
        partida.setJugadorActual(siguienteTurno);
    }

    private void mostrarTiposDeCasillasEnTablero(Tablero t) {
        if (tablero == null) return;
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);
            String tipo = getNombreAmigable(casilla, i, t.getCasillas().size());
            
            if (!tipo.isEmpty()) {
                Text texto = new Text(tipo);
                texto.setUserData(TAG_CASILLA_TEXT);
                
                // Estilo más pequeño para que no moleste
                texto.setStyle("-fx-font-size: 10px; -fx-fill: #555555; -fx-font-weight: bold;");

                int columna = i % COLUMNS;
                int fila = 9 - (i / COLUMNS);

                GridPane.setRowIndex(texto, fila);
                GridPane.setColumnIndex(texto, columna);
                
                // Alineación al fondo de la casilla
                GridPane.setValignment(texto, javafx.geometry.VPos.BOTTOM);
                GridPane.setHalignment(texto, javafx.geometry.HPos.CENTER);
                
                tablero.getChildren().add(texto);
            }
        }
    }

    private String getNombreAmigable(Casilla c, int index, int total) {
        if (index == 0) return "SALIDA";
        if (index == total - 1) return "META";
        if (c instanceof Agujero) return "Agujero";
        if (c instanceof Trineo) return "Trineo";
        if (c instanceof Evento) return "EVENTO";
        if (c instanceof Oso) return "OSO";
        if (c instanceof CasillaFragil) return "Fragil";
        return "";
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y VISUALES
    // ==========================================

    private void procesarTurnosCPU() {
        if (partida.isFinalizada()) return;
        Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
        
        while (actual instanceof Foca && !partida.isFinalizada()) {
            jugarTurnoCPU_IA((Foca) actual, getFichaVisual(actual));
            actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
        }
        
        if (actual instanceof Pinguino && !partida.isFinalizada()) {
            actualizarTextosInventario(actual);
            actualizarTextosTurno();
        }
    }

    private void actualizarTextosTurno() {
        if (partida == null || partida.isFinalizada()) return;
        Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
        
        if (lblDadoTitulo != null) {
            lblDadoTitulo.setText("Dado Jugador: " + actual.getNombre());
        }
        if (dado != null) {
            dado.setText("Tirar Dado (" + actual.getNombre() + ")");
        }
    }

    private Circle getFichaVisual(Jugador j) {
        int index = partida.getJugadores().indexOf(j);
        switch (index) {
            case 0: return P1;
            case 1: return P2;
            case 2: return P3;
            case 3: return P4;
            default: return P1;
        }
    }

    private void actualizarPosicionVisual(Jugador j, Circle ficha) {
        if (ficha == null) return;
        int pos = j.getPosicion();
        if (pos > 49) pos = 49;
        
        // Tablero de 5 columnas (índices 0-4) y 10 filas (indices 0-9)
        int columna = pos % 5;
        int fila = 9 - (pos / 5);
        
        GridPane.setColumnIndex(ficha, columna);
        GridPane.setRowIndex(ficha, fila);
    }

    private boolean consumirObjeto(Jugador j, String nombreObjeto) {
        ArrayList<Item> inv = j.getInventario().getLista();
        for (int i = 0; i < inv.size(); i++) {
            Item item = inv.get(i);
            if (item.getNombre().equalsIgnoreCase(nombreObjeto) || item.getNombre().contains(nombreObjeto)) {
                inv.remove(i);
                actualizarTextosInventario(j);
                return true;
            }
        }
        return false;
    }

    private boolean tieneObjeto(Jugador j, String nombreObjeto) {
        for (Item item : j.getInventario().getLista()) {
            if (item.getNombre().equalsIgnoreCase(nombreObjeto) || item.getNombre().contains(nombreObjeto)) {
                return true;
            }
        }
        return false;
    }

    private int contarBolas(Jugador j) {
        int count = 0;
        for (Item item : j.getInventario().getLista()) {
            if (item.getNombre().toLowerCase().contains("bola")) count++;
        }
        return count;
    }

    private void actualizarTextosInventario(Jugador j) {
        int dadosRapidos = 0, dadosLentos = 0, peces = 0, bolas = 0;
        
        for (Item item : j.getInventario().getLista()) {
            String nombre = item.getNombre().toLowerCase();
            if (nombre.contains("rápido") || nombre.contains("rapido")) dadosRapidos++;
            else if (nombre.contains("lento")) dadosLentos++;
            else if (nombre.contains("pez")) peces++;
            else if (nombre.contains("bola")) bolas++;
        }
        
        if (rapido_t != null) rapido_t.setText("Dado Rápido: " + dadosRapidos);
        if (lento_t != null) lento_t.setText("Dado Lento: " + dadosLentos);
        if (peces_t != null) peces_t.setText("Peces: " + peces);
        if (nieve_t != null) nieve_t.setText("Bolas: " + bolas);
    }

    private void cargarSkins() {
        try {
            String pathPingu = "/resources/pinguino_azul.png";
            String pathRojo = "/resources/foca_roja.png";
            asignarImagenAFicha(P1, pathPingu);
            asignarImagenAFicha(P2, pathRojo);
        } catch (Exception e) {
            System.err.println("Aviso: No se han podido cargar todas las skins.");
        }
    }

    private void asignarImagenAFicha(Circle ficha, String path) {
        if (ficha == null) return;
        try {
            var resource = getClass().getResourceAsStream(path);
            if (resource == null) {
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                resource = getClass().getResourceAsStream("/" + fileName);
            }
            if (resource != null) {
                ficha.setFill(new ImagePattern(new Image(resource)));
                ficha.setStroke(null); 
            }
        } catch (Exception e) {
            System.err.println("Error aplicando skin: " + e.getMessage());
        }
    }
}