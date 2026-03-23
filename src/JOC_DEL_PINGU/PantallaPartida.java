package JOC_DEL_PINGU; // Adaptado a tu estructura de paquetes

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PantallaPartida {

    // --- Menu items ---
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    // --- Botones (Daus y objetos) ---
    @FXML private Button dado;
    @FXML private Button rapido;
    @FXML private Button lento;
    @FXML private Button peces;
    @FXML private Button nieve;

    // --- Textos ---
    @FXML private Text dadoResultText;
    @FXML private Text rapido_t;
    @FXML private Text lento_t;
    @FXML private Text peces_t;
    @FXML private Text nieve_t;
    @FXML private Text eventos;

    // --- Tablero y Fichas ---
    @FXML private GridPane tablero;
    @FXML private Circle P1;
    @FXML private Circle P2;
    @FXML private Circle P3;
    @FXML private Circle P4;

    private GestorPartida gestorPartida;
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";

    // Mapa para relacionar cada Jugador con su Circle en la UI
    private HashMap<Jugador, Circle> fichas;
    // Guardamos las posiciones previas para poder animar
    private HashMap<Jugador, Integer> posicionesPrevias;

    @FXML
    private void initialize() {
        gestorPartida = new GestorPartida();
        fichas = new HashMap<>();
        posicionesPrevias = new HashMap<>();
        
        // 1. Crear jugadores (Pinguinos)
        ArrayList<Jugador> jugadores = new ArrayList<>();
        Pinguino p1 = new Pinguino(0, "Pinguino1", "Azul");
        Pinguino p2 = new Pinguino(0, "Pinguino2", "Rosa"); // Jugador 2
        
        // Inventario P1
        p1.getInventario().getLista().add(new Dado("Dado Normal", 1, 6, 1));
        p1.getInventario().getLista().add(new BolaDeNieve("Bola de nieve", 3)); // Usar la clase concreta
        
        // Inventario P2
        p2.getInventario().getLista().add(new Dado("Dado Normal", 1, 6, 1));
        p2.getInventario().getLista().add(new BolaDeNieve("Bola de nieve", 1)); // Usar la clase concreta
        
        jugadores.add(p1);
        jugadores.add(p2);

        // 2. Crear las focas
        Foca f1 = new Foca(0, "Foca1", "Verde", false);
        Foca f2 = new Foca(0, "Foca2", "Naranja", false);
        jugadores.add(f1);
        jugadores.add(f2);

        // 3. Crear tablero e iniciar partida
        Tablero tableroModelo = new Tablero();
        gestorPartida.nuevaPartida(jugadores, tableroModelo);

        // 4. Asociar cada jugador con su ficha visual (Circle)
        fichas.put(p1, P1);
        fichas.put(p2, P2);
        fichas.put(f1, P3);
        fichas.put(f2, P4);

        // Posiciones iniciales
        for (Jugador j : jugadores) {
            posicionesPrevias.put(j, 0);
        }

        eventos.setText("¡El juego ha comenzado! Turno de Pinguino1.");
    }

    /** Refresca el Text 'eventos' con el log del GestorEventos. */
    private void actualizarEventos() {
        eventos.setText(gestorPartida.getGestorEventos().getLog());
    }

    private void mostrarTiposDeCasillasEnTablero(Tablero t) {
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);

            if (i > 0 && i < 49) {
                String tipo = casilla.getClass().getSimpleName();
                Text texto = new Text(tipo);
                texto.setUserData(TAG_CASILLA_TEXT);
                texto.getStyleClass().add("cell-type");

                int row = i / COLUMNS;
                int col = i % COLUMNS;

                GridPane.setRowIndex(texto, row);
                GridPane.setColumnIndex(texto, col);
                tablero.getChildren().add(texto);
            }
        }
    }

    // ==========================================
    // ACCIONES DE MENÚ
    // ==========================================
    @FXML
    private void handleNewGame() {
        System.out.println("Nueva partida.");
        // Podrías reiniciar la lista de jugadores y llamar a initialize()
    }

    @FXML
    private void handleSaveGame() {
        gestorPartida.guardarPartida();
    }

    @FXML
    private void handleLoadGame() {
        // Ejemplo genérico, deberías pedir el ID al usuario
        gestorPartida.cargarPartida(1); 
    }

    @FXML
    private void handleQuitGame() {
        System.exit(0);
    }

    // ==========================================
    // ACCIONES DE BOTONES
    // ==========================================
    @FXML
    private void handleDado(ActionEvent event) {
        // Obtenemos el jugador actual desde la partida
        Jugador pinguActual = gestorPartida.getPartida().getJugadorActual();
        
        // Guardamos posiciones previas de TODOS antes de mover
        guardarPosicionesPrevias();
        
        // Buscamos si tiene un dado en el inventario
        Dado dadoAUsar = null;
        if (pinguActual.getInventario() != null && !pinguActual.getInventario().getLista().isEmpty()) {
            dadoAUsar = (Dado) pinguActual.getInventario().getLista().get(0);
        }
        
        // Tiramos el dado del jugador
        int resultado = gestorPartida.tirarDado(pinguActual, dadoAUsar);
        dadoResultText.setText("Ha salido: " + resultado);

        // Ejecutamos el turno de las focas (se mueven automáticamente)
        gestorPartida.ejecutarTurnoFocas();

        // 3. COMPROBAMOS SI ALGUIEN HA GANADO
        gestorPartida.getPartida().getTablero().getCasillas().get(0); // Dummy touch to ensure tablero exists? No.
        gestorPartida.getPartida().getGestorEventos().registrar("Fin de la ronda. Comprobando meta...");
        
        // Llamada clave para disparar la lógica de victoria
        new GestorTablero().comprobarFinTurno(gestorPartida.getPartida());

        // Actualizamos el log de eventos en la UI
        actualizarEventos();

        // Movemos TODAS las fichas visualmente
        actualizarTodasLasFichas();

        // 4. COMPROBAMOS SI LA PARTIDA HA TERMINADO
        partidaCheckGameOver();
    }

    /** Si la partida ha terminado, desactiva los botones de acción. */
    private void partidaCheckGameOver() {
        if (gestorPartida.getPartida().isFinalizada()) {
            dado.setDisable(true);
            rapido.setDisable(true);
            lento.setDisable(true);
            peces.setDisable(true);
            nieve.setDisable(true);
            
            String ganador = gestorPartida.getPartida().getGanador() != null ? 
                             gestorPartida.getPartida().getGanador().getNombre() : "Alguien";
            
            System.out.println("¡FIN DE JUEGO detectado en UI! Ganador: " + ganador);
        }
    }

    /** Guarda las posiciones actuales de todos los jugadores antes de mover. */
    private void guardarPosicionesPrevias() {
        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            posicionesPrevias.put(j, j.getPosicion());
        }
    }

    /** Mueve las fichas UNA POR UNA en secuencia (primero tú, luego las focas). */
    private void actualizarTodasLasFichas() {
        dado.setDisable(true);

        // Creamos una cola con los movimientos pendientes
        java.util.LinkedList<Runnable> cola = new java.util.LinkedList<>();

        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            Circle ficha = fichas.get(j);
            if (ficha == null) continue;

            int posVieja = posicionesPrevias.getOrDefault(j, 0);
            int posNueva = j.getPosicion();

            if (posVieja == posNueva) continue; // No se ha movido

            // Añadimos cada movimiento como una tarea a la cola
            cola.add(() -> moverFicha(ficha, posVieja, posNueva, cola));
        }

        // Al final de la cola, rehabilitamos el botón
        cola.add(() -> dado.setDisable(false));

        // Arrancamos la primera animación
        if (!cola.isEmpty()) {
            cola.poll().run();
        }
    }

    /** Mueve una ficha con animación y al terminar lanza la siguiente de la cola. */
    private void moverFicha(Circle ficha, int oldPosition, int newPosition, java.util.LinkedList<Runnable> cola) {
        int oldRow = oldPosition / COLUMNS;
        int oldCol = oldPosition % COLUMNS;
        int newRow = newPosition / COLUMNS;
        int newCol = newPosition % COLUMNS;

        double cellWidth = tablero.getWidth() / COLUMNS;
        double cellHeight = tablero.getHeight() / 10;

        double dx = (newCol - oldCol) * cellWidth;
        double dy = (newRow - oldRow) * cellHeight;

        TranslateTransition slide = new TranslateTransition(Duration.millis(350), ficha);
        slide.setByX(dx);
        slide.setByY(dy);

        slide.setOnFinished(e -> {
            ficha.setTranslateX(0);
            ficha.setTranslateY(0);
            GridPane.setRowIndex(ficha, newRow);
            GridPane.setColumnIndex(ficha, newCol);

            // Lanzamos la siguiente animación de la cola
            if (!cola.isEmpty()) {
                cola.poll().run();
            }
        });

        slide.play();
    }

    @FXML
    private void handleRapido() { System.out.println("Dado Rápido."); }

    @FXML
    private void handleLento() { System.out.println("Dado Lento."); }

    @FXML
    private void handlePeces() { System.out.println("Usar Pez."); }

    @FXML
    private void handleNieve() { 
        System.out.println("Lanzar Boles de neu."); 
        // En tu turno, puedes tirar bolas de nieve [cite: 206]
    }

    public void setGestorPartida(GestorPartida gestorPartida) {
        this.gestorPartida = gestorPartida;
    }
}