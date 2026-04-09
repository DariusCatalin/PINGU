package JOC_DEL_PINGU;

import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
    @FXML private ImageView P1; // Jugador 1
    @FXML private ImageView P2; // Jugador 2
    @FXML private ImageView P3; // Jugador 3
    @FXML private ImageView P4; // Jugador 4

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
        ImageView[] fichas = {P1, P2, P3, P4};

        // Ocultar todas primero
        for (ImageView iv : fichas) {
            if (iv != null) iv.setVisible(false);
        }

        // Asignar imagen según color y mostrar la ficha de cada jugador
        for (int i = 0; i < jugadoresConfig.size() && i < fichas.length; i++) {
            Jugador j = jugadoresConfig.get(i);
            ImageView ficha = fichas[i];
            if (ficha != null) {
                asignarImagenAFicha(ficha, obtenerRutaPersonaje(j.getColor()));
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
    // MENÚ OPCIONES (EVENTOS)
    // ==========================================

    @FXML
    private void handleSaveGame(ActionEvent event) {
        gestorUI.registrar("Guardando partida... (Función no implementada aún)");
        // TODO: Conectar con GestorBBDD
    }

    @FXML
    private void handleSaveAndQuit(ActionEvent event) {
        gestorUI.registrar("Guardando partida... (Función no implementada aún)");
        // TODO: Conectar con GestorBBDD para guardar primero
        System.out.println("Saliendo al menú tras guardar...");
        volverAlMenu(event);
    }

    @FXML
    private void handleQuitWithoutSaving(ActionEvent event) {
        System.out.println("Saliendo al menú sin guardar...");
        volverAlMenu(event);
    }

    private void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaPrincipal.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch(Exception ignored){}
            Stage stage = null;
            
            // Intento 1: A través del tablero
            if (tablero != null && tablero.getScene() != null && tablero.getScene().getWindow() != null) {
                stage = (Stage) tablero.getScene().getWindow();
            } 
            // Intento 2: A través del evento (MenuItem -> MenuBar -> Stage)
            else if (event.getSource() instanceof MenuItem) {
                MenuItem mItem = (MenuItem) event.getSource();
                if (mItem.getParentPopup() != null && mItem.getParentPopup().getOwnerWindow() != null) {
                    stage = (Stage) mItem.getParentPopup().getOwnerWindow();
                }
            }

            if (stage != null) {
                stage.setScene(scene);
                stage.setTitle("El Juego del Pingüino - Menú Principal");
                stage.show();
                final Stage finalStage = stage; // necesario: stage no es effectively final
                javafx.application.Platform.runLater(() -> finalStage.setMaximized(true));
            } else {
                throw new Exception("No se pudo obtener la ventana principal (Stage).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setHeaderText("Error al volver al menú");
            alert.setContentText(e.toString() + " - " + e.getMessage());
            alert.showAndWait();
        }
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
        usarDadoEspecial("Dado Rápido", 5, 10); // Especificació: avança entre 5 i 10 caselles
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
        if (partida.isFinalizada()) return;
        Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());

        if (!(actual instanceof Pinguino)) return;

        int bolasActual = contarBolas(actual);
        if (bolasActual == 0) {
            gestorUI.registrar("¡No tienes Bolas de Nieve para lanzar!");
            return;
        }

        // Buscar objetivo: el pinguino humano que va más adelante
        Jugador objetivo = null;
        int maxPos = -1;
        for (Jugador j : partida.getJugadores()) {
            if (j != actual && j instanceof Pinguino && j.getPosicion() > maxPos) {
                maxPos = j.getPosicion();
                objetivo = j;
            }
        }

        if (objetivo == null) {
            gestorUI.registrar("No hay otros pingüinos a los que lanzar bolas de nieve.");
            return;
        }

        // ¡GUERRA DE BOLAS DE NIEVE! Ambos gastan todas sus bolas
        int bolasObj = contarBolas(objetivo);
        actual.vaciarBolas();
        objetivo.vaciarBolas();
        actualizarTextosInventario(actual);
        actualizarTextosInventario(objetivo);

        if (bolasActual > bolasObj) {
            int diff = bolasActual - bolasObj;
            objetivo.moverPosicion(Math.max(0, objetivo.getPosicion() - diff));
            gestorUI.registrar("¡GUERRA DE BOLAS! " + actual.getNombre() + " ataca a " + objetivo.getNombre()
                + " y gana por " + diff + " bolas. El perdedor retrocede " + diff + " casillas.");
            actualizarPosicionVisual(objetivo, getFichaVisual(objetivo));
        } else if (bolasObj > bolasActual) {
            int diff = bolasObj - bolasActual;
            actual.moverPosicion(Math.max(0, actual.getPosicion() - diff));
            gestorUI.registrar("¡GUERRA DE BOLAS! " + objetivo.getNombre() + " contraataca a " + actual.getNombre()
                + " y gana por " + diff + " bolas. El perdedor retrocede " + diff + " casillas.");
            actualizarPosicionVisual(actual, getFichaVisual(actual));
        } else {
            gestorUI.registrar("¡EMPATE en la Guerra de Bolas entre " + actual.getNombre()
                + " y " + objetivo.getNombre() + "! Todos pierden sus bolas pero nadie retrocede.");
        }

        // Lanzar bolas ES la acción del turno (reemplaza tirar dado)
        avanzarTurno();
        procesarTurnosCPU();
    }

    // ==========================================
    // LÓGICA INTERNA DE TURNOS Y CASILLAS
    // ==========================================

    private void jugarTurnoCPU_IA(Foca foca, ImageView fichaVisual) {
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
                        // El pingüino le da un pez: la Foca queda bloqueada 2 turnos
                        gestorUI.registrar("¡" + pinguino.getNombre() + " le lanza un Pez a la Foca! ¡La Foca queda bloqueada 2 turnos!");
                        foca.aplicarPenalizacion();
                        foca.aplicarPenalizacion(); // 2 turnos según especificación
                    } else {
                        // Sin pez: coletazo → solo retrocede al agujero anterior (NO pierde objetos)
                        int posHole = buscarAgujeroAnterior(pinguino.getPosicion(), partida.getTablero());
                        pinguino.moverPosicion(posHole);
                        gestorUI.registrar("¡La Foca da un coletazo a " + pinguino.getNombre()
                            + "! Es enviado al agujero anterior (casilla " + posHole + ").");
                        actualizarPosicionVisual(pinguino, getFichaVisual(pinguino));
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
                        actualizarPosicionVisual(otro, getFichaVisual(otro));
                    } else if (bolasO > bolasA) {
                        int diff = bolasO - bolasA;
                        actual.moverPosicion(Math.max(0, actual.getPosicion() - diff));
                        gestorUI.registrar("¡Guerra de bolas! " + otro.getNombre() + " gana a " + actual.getNombre() + " por " + diff + " bolas.");
                        actualizarPosicionVisual(actual, getFichaVisual(actual));
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

    private void procesarTurnosCPU() {
        while (!partida.isFinalizada()) {
            Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
            if (actual instanceof Foca) {
                jugarTurnoCPU_IA((Foca) actual, getFichaVisual(actual));
            } else {
                break;
            }
        }
    }

    private ImageView getFichaVisual(Jugador j) {
        int index = partida.getJugadores().indexOf(j);
        ImageView[] fichas = {P1, P2, P3, P4};
        if (index >= 0 && index < fichas.length) {
            return fichas[index];
        }
        return null;
    }

    private void actualizarTextosTurno() {
        if (partida != null && partida.getJugadores() != null && !partida.getJugadores().isEmpty()) {
            Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
            if (lblDadoTitulo != null) {
                lblDadoTitulo.setText("Turno de: " + actual.getNombre());
            }
        }
    }

    private void mostrarTiposDeCasillasEnTablero(Tablero t) {
        if (tablero == null) return;
        // Eliminar imágenes de casillas anteriores
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);
            String rutaImg = getRutaCasilla(casilla, i, t.getCasillas().size());

            if (rutaImg != null) {
                try {
                    var resource = getClass().getResourceAsStream(rutaImg);
                    if (resource != null) {
                        ImageView imgView = new ImageView(new Image(resource));
                        imgView.setFitWidth(90);
                        imgView.setFitHeight(50);
                        imgView.setPreserveRatio(false);
                        imgView.setUserData(TAG_CASILLA_TEXT);

                        int columna = i % COLUMNS;
                        int fila = 9 - (i / COLUMNS);

                        GridPane.setRowIndex(imgView, fila);
                        GridPane.setColumnIndex(imgView, columna);
                        GridPane.setValignment(imgView, javafx.geometry.VPos.CENTER);
                        GridPane.setHalignment(imgView, javafx.geometry.HPos.CENTER);

                        tablero.getChildren().add(imgView);
                    }
                } catch (Exception e) {
                    System.err.println("Error cargando imagen casilla " + i + ": " + e.getMessage());
                }
            }
        }
    }

    /** Devuelve la ruta del recurso imagen para cada tipo de casilla. */
    private String getRutaCasilla(Casilla c, int index, int total) {
        // Casilla normal (inicio, meta, y casillas sin tipo especial) usan imagen normal
        if (c instanceof Agujero)     return "/resources/Casilla_Agujero.png";
        if (c instanceof Oso)         return "/resources/Casilla_Oso.png";
        if (c instanceof Trineo)      return "/resources/Casilla_Trineo.png";
        if (c instanceof Evento)      return "/resources/Casilla_Interrogante.png";
        if (c instanceof CasillaFragil) return "/resources/Casilla_Normal.png";
        // CasillaNormal (incluye salida y meta)
        return "/resources/Casilla_Normal.png";
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y VISUALES
    // ==========================================

    private void actualizarPosicionVisual(Jugador j, ImageView ficha) {
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

    /** Devuelve la ruta del PNG según el color del jugador. */
    private String obtenerRutaPersonaje(String color) {
        if (color == null) return "/resources/Personaje Amarillo.png";
        switch (color.toLowerCase()) {
            case "amarillo": return "/resources/Personaje Amarillo.png";
            case "rojo":     return "/resources/Personaje Rojo.png";
            case "verde":    return "/resources/Personaje Verde.png";
            case "azul":
            default:         return "/resources/Gemini_Generated_Image_y5ki0gy5ki0gy5ki-fotor-bg-remover-202603161643.png";
        }
    }

    private void cargarSkins() {
        // En el modo fallback (sin PantallaConfig), asignar skins por defecto
        asignarImagenAFicha(P1, obtenerRutaPersonaje("azul"));
        asignarImagenAFicha(P2, obtenerRutaPersonaje("rojo"));
    }

    private void asignarImagenAFicha(ImageView ficha, String path) {
        if (ficha == null) return;
        try {
            var resource = getClass().getResourceAsStream(path);
            if (resource != null) {
                ficha.setImage(new Image(resource));
            } else {
                System.err.println("No se encontró el recurso: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error aplicando skin: " + e.getMessage());
        }
    }
}