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
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
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
        
        // 1. Crear jugadores (Pinguino Humano)
        ArrayList<Jugador> jugadores = new ArrayList<>();
        Pinguino p1 = new Pinguino(0, "Pinguino1", "Azul");
        
        // Inventario P1
        p1.getInventario().getLista().add(new Dado("Dado Normal", 1, 6, 1));
        p1.getInventario().getLista().add(new BolaDeNieve("Bola de nieve", 3));
        
        jugadores.add(p1);

        // 2. Crear las focas (CPU)
        Foca f_roja = new Foca(0, "Foca Roja", "Rojo", false); // Sustituimos el segundo pinguino
        Foca f1 = new Foca(0, "Foca1", "Verde", false);
        Foca f2 = new Foca(0, "Foca2", "Naranja", false);
        
        jugadores.add(f_roja);
        jugadores.add(f1);
        jugadores.add(f2);

        // 3. Crear tablero e iniciar partida
        Tablero tableroModelo = new Tablero();
        gestorPartida.nuevaPartida(jugadores, tableroModelo);

        // 4. Asociar fichas visuales
        fichas.put(p1, P1);
        fichas.put(f_roja, P2);
        fichas.put(f1, P3);
        fichas.put(f2, P4);

        // 5. APLICAR SKINS (IMÁGENES PIXEL-ART)
        aplicarSkins();

        // 6. Posiciones iniciales
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
        
        // Buscamos si tiene un dado en el inventario (Verificando el tipo para evitar errores)
        Dado dadoAUsar = null;
        if (pinguActual.getInventario() != null) {
            for (Item item : pinguActual.getInventario().getLista()) {
                if (item instanceof Dado) {
                    dadoAUsar = (Dado) item;
                    break;
                }
            }
        }
        
        try {
            // 1. Procesamos el turno del Pingüino (Tirar dado + Casilla + Fin Partida)
            int resultado = gestorPartida.procesarTurnoJugador(pinguActual, dadoAUsar);
            dadoResultText.setText("Ha salido: " + resultado);
            
            // 2. Ejecutamos el turno de las focas (Movimiento + Casilla + Fin Partida)
            if (!gestorPartida.getPartida().isFinalizada()) {
                gestorPartida.ejecutarTurnoFocas();
            }

            // 3. ACTUALIZAMOS EL LOG DE EVENTOS EN LA UI
            actualizarEventos();

            // 4. MOVEMOS TODAS LAS FICHAS VISUALMENTE (EN SECUENCIA)
            actualizarTodasLasFichas();

            // 5. BLOQUEO DEFINITIVO SI HA TERMINADO
            partidaCheckGameOver();
        } catch (Exception e) {
            System.err.println("¡CRASH en handleDado!");
            e.printStackTrace();
        }
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

        // Al final de la cola, rehabilitamos los botones SOLO SI LA PARTIDA NO HA TERMINADO
        cola.add(() -> {
            if (!gestorPartida.getPartida().isFinalizada()) {
                dado.setDisable(false);
                rapido.setDisable(false);
                lento.setDisable(false);
                peces.setDisable(false);
                nieve.setDisable(false);
            } else {
                System.out.println("Partida finalizada: botones bloqueados permanentemente.");
            }
        });

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
    private void handleRapido() { System.out.println("Usando Dado Rápido."); }

    @FXML
    private void handleLento() { System.out.println("Usando Dado Lento."); }

    @FXML
    private void handlePeces() { System.out.println("Usando Pez."); }

    @FXML
    private void handleNieve() { 
        System.out.println("Lanzando bolas de nieve."); 
        System.out.println("Throwing snowballs."); 
    }

    public void setGestorPartida(GestorPartida gestorPartida) {
        this.gestorPartida = gestorPartida;
    }

    /** Carga las imágenes de recursos y las aplica a los círculos. */
    private void aplicarSkins() {
        try {
            // Rutas de los recursos (deben estar en src/resources/)
            String pathPingu = "/resources/pinguino.png";
            String pathRojo = "/resources/foca_roja.png";
            String pathVerde = "/resources/foca_verde.png";
            String pathAmarillo = "/resources/foca_amarilla.png";

            // Aplicar skin al Pingüino
            asignarImagenAFicha(P1, pathPingu);
            // Aplicar skin a Foca Roja
            asignarImagenAFicha(P2, pathRojo);
            // Aplicar skin a Foca Verde
            asignarImagenAFicha(P3, pathVerde);
            // Aplicar skin a Foca Amarilla (cuando esté disponible)
            asignarImagenAFicha(P4, pathAmarillo);

        } catch (Exception e) {
            System.err.println("Aviso: No se han podido cargar todas las skins. Usando colores por defecto.");
        }
    }

    private void asignarImagenAFicha(Circle ficha, String path) {
        try {
            var resource = getClass().getResourceAsStream(path);
            if (resource == null) {
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                resource = getClass().getResourceAsStream("/" + fileName);
            }
            if (resource != null) {
                Image img = new Image(resource);
                ficha.setFill(new ImagePattern(img));
                ficha.setStroke(null); 
                System.out.println("Skin cargada: " + path);
            } else {
                System.err.println("Imagen no encontrada: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error al aplicar skin " + path + ": " + e.getMessage());
        }
    }
}