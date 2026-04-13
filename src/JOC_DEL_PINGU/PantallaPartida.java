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
    
    /**
     * Llamado por PantallaPrincipal o GestorBBDD al cargar una partida existente.
     */
    public void setPartidaCargada(Partida pCargada) {
        this.partida = pCargada;
        this.partida.setGestorEventos(gestorUI);
        this.jugadoresConfig = pCargada.getJugadores();
        
        ImageView[] fichas = {P1, P2, P3, P4};
        for (ImageView iv : fichas) {
            if (iv != null) iv.setVisible(false);
        }

        for (int i = 0; i < jugadoresConfig.size() && i < fichas.length; i++) {
            Jugador j = jugadoresConfig.get(i);
            ImageView ficha = fichas[i];
            if (ficha != null) {
                asignarImagenAFicha(ficha, obtenerRutaPersonaje(j.getColor()));
                ficha.setVisible(true);
                actualizarPosicionVisual(j, ficha);
            }
        }

        for (Jugador j : jugadoresConfig) {
            if (j instanceof Pinguino) {
                actualizarTextosInventario(j);
                break;
            }
        }
        
        actualizarTextosTurno();
        mostrarTiposDeCasillasEnTablero(partida.getTablero());
        gestorUI.registrar("¡Partida Restaurada desde Base de Datos con " + jugadoresConfig.size() + " jugadores!");
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
            }
        }
        
        inicializarColas();

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
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("");
        dialog.setTitle("Guardar Partida");
        dialog.setHeaderText("Guardar el estado actual en la BBDD Oracle");
        dialog.setContentText("Introduce el ID numérico de tu partida:");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(idStr -> {
            try {
                int idPartida = Integer.parseInt(idStr);
                GestorBBDD gestor = new GestorBBDD();
                gestor.iniciarConexionGUI();
                boolean extio = gestor.guardarBBDD(partida, idPartida);
                gestor.cerrarConexion();
                
                if (extio) {
                    gestorUI.registrar("✅ Partida " + idPartida + " guardada en BBDD.");
                } else {
                    gestorUI.registrar("❌ Error al guardar en BBDD.");
                }
            } catch (NumberFormatException e) {
                gestorUI.registrar("❌ El ID introducido no es válido. Debe ser número.");
            }
        });
    }

    @FXML
    private void handleSaveAndQuit(ActionEvent event) {
        System.out.println("Guardando antes de salir...");
        handleSaveGame(event);
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
            avanzarTurno();
            dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
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
                avanzarTurno();
                dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
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
        dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
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
            int finalBounce = 49 - (nuevaPos - 49);
            encolarCaminoPasoAPaso(j, posInicial, 49);
            encolarCaminoPasoAPaso(j, 49, finalBounce);
            nuevaPos = finalBounce;
            gestorUI.registrar("¡" + j.getNombre() + " ha sacado un " + tirada + " y se ha pasado! Rebota hasta la " + nuevaPos);
        } else {
            encolarCaminoPasoAPaso(j, posInicial, nuevaPos);
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
            // Navegar a la pantalla de victoria tras un breve retardo para que el log se vea
            javafx.application.Platform.runLater(() -> irAPantallaVictoria(j));
            return;
        }
        
        // Ejecuta la casilla (Oso, Trineo, Evento...)
        Casilla casillaActual = partida.getTablero().getCasillas().get(j.getPosicion());
        int posAntesCasilla = j.getPosicion();
        casillaActual.realizarAccion(partida, j);
        if (posAntesCasilla != j.getPosicion()) {
            encolarCaminoPasoAPaso(j, posAntesCasilla, j.getPosicion());
        }
        
        // Comprobar colisiones
        verificarColisionesLocal(j);
    }

    /** Carga la pantalla de victoria y le pasa el nombre y color del ganador. */
    private void irAPantallaVictoria(Jugador ganador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaVictoria.fxml"));
            Parent root = loader.load();

            // Pasar datos del ganador al controlador de victoria
            PantallaVictoria controlador = loader.getController();
            controlador.setGanador(ganador.getNombre(), ganador.getColor());

            Scene scene = new Scene(root);

            // Obtener la ventana actual
            Stage stage = null;
            if (tablero != null && tablero.getScene() != null && tablero.getScene().getWindow() != null) {
                stage = (Stage) tablero.getScene().getWindow();
            }

            if (stage != null) {
                stage.setScene(scene);
                stage.setTitle("El Juego del Pingüino - ¡Victoria!");
                stage.show();
                final Stage finalStage = stage;
                javafx.application.Platform.runLater(() -> finalStage.setMaximized(true));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar PantallaVictoria: " + e.getMessage());
            e.printStackTrace();
        }
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
                        encolarCaminoPasoAPaso(pinguino, pinguino.getPosicion(), posHole);
                        pinguino.moverPosicion(posHole);
                        gestorUI.registrar("¡La Foca da un coletazo a " + pinguino.getNombre()
                            + "! Es enviado al agujero anterior (casilla " + posHole + ").");
                    }
                } else if (actual instanceof Pinguino && otro instanceof Pinguino) {
                    // GUERRA DE BOLAS (PvP)
                    int bolasA = actual.contarBolas();
                    int bolasO = otro.contarBolas();
                    actual.vaciarBolas();
                    otro.vaciarBolas();
                    
                    if (bolasA > bolasO) {
                        int diff = bolasA - bolasO;
                        int dest = Math.max(0, otro.getPosicion() - diff);
                        encolarCaminoPasoAPaso(otro, otro.getPosicion(), dest);
                        otro.moverPosicion(dest);
                        gestorUI.registrar("¡Guerra de bolas! " + actual.getNombre() + " gana a " + otro.getNombre() + " por " + diff + " bolas.");
                    } else if (bolasO > bolasA) {
                        int diff = bolasO - bolasA;
                        int dest = Math.max(0, actual.getPosicion() - diff);
                        encolarCaminoPasoAPaso(actual, actual.getPosicion(), dest);
                        actual.moverPosicion(dest);
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

    private void procesarTurnosCPU_Async() {
        if (partida.isFinalizada()) return;
        Jugador actual = partida.getJugadores().get(partida.getIndiceJugadorActual());
        if (actual instanceof Foca) {
            // Lógica
            if (actual.estaPenalizado()) {
                actual.decrementarPenalizacion();
                gestorUI.registrar("La foca " + actual.getNombre() + " está entretenida comiendo. Pierde su turno.");
            } else {
                int tirada = (int)(Math.random() * 6) + 1;
                moverJugadorYAccion(actual, tirada, "tirada CPU");
            }
            
            avanzarTurno();
            
            // Animación y callback a la siguiente CPU
            dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
        } else {
            // Turno humano: controles habilitados
            setUIInteractuable(true);
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

            // LA CASILLA 49 ES LA META (IGLÚ). Como ya viene dibujada en el fondo,
            // no queremos poner una placa de hielo normal encima, dejando ver el fondo.
            if (i == 49) {
                continue;
            }

            String rutaImg = getRutaCasilla(casilla, i, t.getCasillas().size());

            if (rutaImg != null) {
                try {
                    var resource = getClass().getResourceAsStream(rutaImg);
                    if (resource != null) {
                        ImageView imgView = new ImageView(new Image(resource));
                        imgView.setFitWidth(110);
                        imgView.setFitHeight(80);
                        imgView.setPreserveRatio(true);
                        imgView.setUserData(TAG_CASILLA_TEXT);

                        int columna = i % COLUMNS;
                        int fila = 9 - (i / COLUMNS);

                        GridPane.setRowIndex(imgView, fila);
                        GridPane.setColumnIndex(imgView, columna);
                        GridPane.setValignment(imgView, javafx.geometry.VPos.CENTER);
                        GridPane.setHalignment(imgView, javafx.geometry.HPos.CENTER);

                        tablero.getChildren().add(0, imgView);
                    }
                } catch (Exception e) {
                    System.err.println("Error cargando imagen casilla " + i + ": " + e.getMessage());
                }
            }
        }
        
        // Z-Index fix: Ensure players are always drawn on top of the board cells
        if (P1 != null) P1.toFront();
        if (P2 != null) P2.toFront();
        if (P3 != null) P3.toFront();
        if (P4 != null) P4.toFront();
    }

    /** Devuelve la ruta del recurso imagen para cada tipo de casilla. */
    private String getRutaCasilla(Casilla c, int index, int total) {

        if (c instanceof Agujero)     return "/resources/Casilla_Agujero.png";
        if (c instanceof Oso)         return "/resources/Casilla_Oso.png";
        if (c instanceof Trineo)      return "/resources/Casilla_Trineo.png";
        if (c instanceof Evento)      return "/resources/Casilla_Interrogante.png";
        if (c instanceof CasillaFragil) return "/resources/Casilla_Normal.png";
        
        return "/resources/Casilla_Normal.png";
    }

    // ==========================================
    // SISTEMA DE ANIMACIÓN Y AGRUPACIÓN
    // ==========================================
    private java.util.Map<Jugador, java.util.Queue<Integer>> colasAnimacion = new java.util.HashMap<>();
    private java.util.Map<Jugador, Integer> posVisual = new java.util.HashMap<>();
    private boolean animando = false;

    private void inicializarColas() {
        for (Jugador j : partida.getJugadores()) {
            colasAnimacion.put(j, new java.util.LinkedList<>());
            posVisual.put(j, j.getPosicion());
        }
    }

    private void encolarCaminoPasoAPaso(Jugador j, int desde, int hasta) {
        java.util.Queue<Integer> q = colasAnimacion.get((Jugador)j);
        if (q == null) return;

        if (desde < hasta) {
            for (int i = desde + 1; i <= hasta; i++) q.add(i);
        } else if (desde > hasta) {
            for (int i = desde - 1; i >= hasta; i--) q.add(i);
        }
    }

    private void setUIInteractuable(boolean interactuable) {
        if (dado != null) dado.setDisable(!interactuable);
        if (rapido != null) rapido.setDisable(!interactuable);
        if (lento != null) lento.setDisable(!interactuable);
        if (peces != null) peces.setDisable(!interactuable);
        if (nieve != null) nieve.setDisable(!interactuable);
    }

    private void dispararAnimadorVisual(Runnable onFinished) {
        if (animando) return;
        animando = true;
        setUIInteractuable(false);

        javafx.animation.Timeline tl = new javafx.animation.Timeline();
        tl.setCycleCount(javafx.animation.Animation.INDEFINITE);
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(javafx.util.Duration.millis(350), e -> {
            boolean qVacia = true;
            for (Jugador j : partida.getJugadores()) {
                java.util.Queue<Integer> q = colasAnimacion.get(j);
                if (q != null && !q.isEmpty()) {
                    qVacia = false;
                    int next = q.poll();
                    posVisual.put(j, next);
                    
                    // Colocar en el grid según el índice visual
                    ImageView ficha = getFichaVisual(j);
                    if (ficha != null) {
                        int pos = next;
                        if (pos > 49) pos = 49;
                        int col = pos % 5;
                        int fil = 9 - (pos / 5);
                        GridPane.setColumnIndex(ficha, col);
                        GridPane.setRowIndex(ficha, fil);
                    }
                }
            }

            refrescarDistribucionVisual();

            if (qVacia) {
                tl.stop();
                animando = false;
                setUIInteractuable(true);
                if (onFinished != null) javafx.application.Platform.runLater(onFinished);
            }
        });
        tl.getKeyFrames().add(kf);
        tl.play();
    }

    private void refrescarDistribucionVisual() {
        // Encontrar jugadores por celda visual
        java.util.Map<Integer, java.util.List<Jugador>> celdaJugadores = new java.util.HashMap<>();
        for (Jugador j : partida.getJugadores()) {
            int pos = posVisual.getOrDefault(j, j.getPosicion());
            celdaJugadores.computeIfAbsent(pos, k -> new java.util.ArrayList<>()).add(j);
        }

        // Aplicar offset a jugadores compartiendo celda
        for (java.util.List<Jugador> lista : celdaJugadores.values()) {
            int total = lista.size();
            for (int i = 0; i < total; i++) {
                ImageView ficha = getFichaVisual(lista.get(i));
                if (ficha == null) continue;
                
                if (total == 1) {
                    ficha.setTranslateX(0);
                    ficha.setScaleX(1.0);
                    ficha.setScaleY(1.0);
                } else if (total == 2) {
                    ficha.setScaleX(0.85);
                    ficha.setScaleY(0.85);
                    ficha.setTranslateX(i == 0 ? -15 : 15);
                } else if (total == 3) {
                    ficha.setScaleX(0.70);
                    ficha.setScaleY(0.70);
                    if (i == 0) ficha.setTranslateX(-20);
                    else if (i == 1) ficha.setTranslateX(0);
                    else ficha.setTranslateX(20);
                } else {
                    ficha.setScaleX(0.60);
                    ficha.setScaleY(0.60);
                    if (i == 0) ficha.setTranslateX(-22);
                    else if (i == 1) ficha.setTranslateX(-7);
                    else if (i == 2) ficha.setTranslateX(7);
                    else ficha.setTranslateX(22);
                }
            }
        }
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