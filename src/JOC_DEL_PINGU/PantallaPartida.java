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
    private int idPartidaActual = -1; // ID de la partida en BBDD (-1 = no guardada aún)
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
    /** Menú de escape: ContextMenu que se abre/cierra con la tecla Escape */
    private javafx.scene.control.ContextMenu ctxMenuEscape;
 
    // Gestor de eventos simple para mostrar textos en la pantalla
    private GestorEventos gestorUI = new GestorEventos() {
        @Override
        public void registrar(String mensaje) {
            super.registrar(mensaje);
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
     * Asigna el ID de la partida cargada desde BBDD.
     * Esto es necesario para que cuando se vuelva a guardar (manualmente o al ganar)
     * se ACTUALICE la misma partida en lugar de crear una nueva con otro ID.
     */
    public void setIdPartidaCargada(int idPartida) {
        this.idPartidaActual = idPartida;
        if (gestorUI != null) {
            gestorUI.registrar("📂 Partida cargada con ID " + idPartida);
        }
    }
    
    /**
     * Llamado por PantallaPrincipal o GestorBBDD al cargar una partida existente.
     */
    public void setPartidaCargada(Partida pCargada) {
        this.partida = pCargada;
        this.partida.setGestorEventos(gestorUI);
        this.jugadoresConfig = pCargada.getJugadores();
 
        // CRÍTICO: inicializar las colas de animación para los jugadores cargados.
        // Sin esto, colasAnimacion y posVisual están vacíos y el juego se congela.
        inicializarColas();
 
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
 
        // 3. Configurar escucha de tecla Escape para el menú de opciones
        configurarEscapeMenu();
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
 
        // Asignar imagen según color, posicionar en casilla 0 y mostrar la ficha de cada jugador
        for (int i = 0; i < jugadoresConfig.size() && i < fichas.length; i++) {
            Jugador j = jugadoresConfig.get(i);
            ImageView ficha = fichas[i];
            if (ficha != null) {
                asignarImagenAFicha(ficha, obtenerRutaPersonaje(j.getColor()));
                ficha.setVisible(true);
                actualizarPosicionVisual(j, ficha); // ← coloca la ficha en la fila/col correcta desde el inicio
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
        GestorBBDD gestor = new GestorBBDD();
        gestor.iniciarConexionGUI();

        if (gestor.getConexion() == null) {
            gestorUI.registrar("❌ No hay conexión a la BBDD.");
            return;
        }

        // Si ya tenemos un ID asignado a esta partida, sobrescribir.
        // Si no, generar uno nuevo con la sequence Oracle.
        int idAUsar;
        boolean esActualizacion = (this.idPartidaActual > 0);

        if (esActualizacion) {
            idAUsar = this.idPartidaActual;
            boolean exito = gestor.guardarBBDD(partida, idAUsar);
            gestor.cerrarConexion();
            if (exito) {
                gestorUI.registrar("✅ Partida " + idAUsar + " actualizada en BBDD.");
            } else {
                gestorUI.registrar("❌ Error al actualizar en BBDD.");
            }
        } else {
            // Primera vez que guardamos: generar ID automático
            idAUsar = gestor.guardarPartidaAuto(partida);
            gestor.cerrarConexion();
            if (idAUsar > 0) {
                this.idPartidaActual = idAUsar;
                gestorUI.registrar("✅ Partida creada con ID " + idAUsar + " (asignado automáticamente).");

                // Mostrar al usuario el ID que se le ha asignado
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Partida guardada");
                alert.setHeaderText("ID asignado: " + idAUsar);
                alert.setContentText("Apunta este ID si quieres cargar la partida más tarde.\n" +
                                     "También puedes verlo en la pantalla 'Cargar Partida'.");
                alert.showAndWait();
            } else {
                gestorUI.registrar("❌ Error al guardar en BBDD.");
            }
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaPrincipal.fxml"));
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
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.show();
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
 
        if (!(actual instanceof Pinguino)) {
            avanzarTurno();
            dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
            return;
        }
 
        if (actual.estaPenalizado()) {
            actual.decrementarPenalizacion();
            gestorUI.registrar("¡" + actual.getNombre() + " está penalizado y pierde este turno!");
            actualizarTextosInventario(actual);
            avanzarTurno();
            dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
            return;
        }
 
        // 3. Popup si tiene objetos usables antes de tirar
        if (tieneObjetosUsables(actual)) {
            mostrarPopupObjetos(actual);
        } else {
            ejecutarTiradaNormal(actual);
        }
    }
 
    /** Ejecuta una tirada normal de dado (1-6) y avanza el turno. */
    private void ejecutarTiradaNormal(Jugador actual) {
        int tirada = (int)(Math.random() * 6) + 1;

        // Si el jugador tiene animación de dado registrada, mostrarla antes de mover
        String[] config = obtenerConfigAnimacion(actual.getColor());
        if (config != null) {
            final int tiradaFinal = tirada;
            setUIInteractuable(false);
            mostrarAnimacionTurno(actual, tiradaFinal,
                config[0],                                         // ruta imagen dado
                javafx.scene.paint.Color.web(config[1]),           // color texto resultado
                () -> {
                    moverJugadorYAccion(actual, tiradaFinal, "tirada normal");
                    if (dadoResultText != null) dadoResultText.setText("Has sacado un: " + tiradaFinal);
                    actualizarTextosInventario(actual);
                    avanzarTurno();
                    dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
                });
        } else {
            moverJugadorYAccion(actual, tirada, "tirada normal");
            // 4. Resultado del dado visible: texto grande y claro
            if (dadoResultText != null) dadoResultText.setText("Has sacado un: " + tirada);
            actualizarTextosInventario(actual);
            avanzarTurno();
            dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
        }
    }
 
    /** Comprueba si el jugador tiene objetos que puede usar activamente (dados/bolas). */
    private boolean tieneObjetosUsables(Jugador j) {
        if (!(j instanceof Pinguino)) return false;
        for (Item item : j.getInventario().getLista()) {
            String n = item.getNombre().toLowerCase();
            if (n.contains("rapido") || n.contains("rápido") ||
                n.contains("lento")  || n.contains("bola")) return true;
        }
        return false;
    }
 
    /**
     * 3. Popup que aparece antes de tirar el dado si el jugador tiene objetos disponibles.
     * Muestra un botón por objeto usable y un botón "No, tirar dado normal".
     */
    private void mostrarPopupObjetos(Jugador actual) {
        int nRapidos = contarItemPorNombre(actual, "rapido", "rápido");
        int nLentos  = contarItemPorNombre(actual, "lento");
        int nBolas   = contarBolas(actual);
 
        javafx.scene.control.Alert popup = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.NONE);
        popup.setTitle("¿Usar objeto?");
        popup.setHeaderText("Turno de " + actual.getNombre());
        popup.setContentText("¿Quieres usar algún objeto antes de tirar el dado?");
 
        java.util.List<javafx.scene.control.ButtonType> bts = new java.util.ArrayList<>();
        if (nRapidos > 0) bts.add(new javafx.scene.control.ButtonType(
            "🎲 Dado Rápido (" + nRapidos + ")"));
        if (nLentos > 0)  bts.add(new javafx.scene.control.ButtonType(
            "🐢 Dado Lento  (" + nLentos  + ")"));
        if (nBolas > 0)   bts.add(new javafx.scene.control.ButtonType(
            "❄️ Bola de Nieve (" + nBolas + ")"));
        bts.add(new javafx.scene.control.ButtonType("No, tirar dado normal",
            javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE));
        popup.getButtonTypes().setAll(bts);
 
        popup.showAndWait().ifPresent(btn -> {
            String t = btn.getText();
            if (t.contains("Rápido") || t.contains("Rapido")) {
                usarDadoEspecial("Dado Rápido", 5, 10);
            } else if (t.contains("Lento")) {
                usarDadoEspecial("Dado Lento", 1, 3);
            } else if (t.contains("Nieve") || t.contains("Bola")) {
                usarBolaNieve(null);
            } else {
                ejecutarTiradaNormal(actual);
            }
        });
    }
 
    /** Cuenta items cuyo nombre contenga alguna de las palabras clave dadas. */
    private int contarItemPorNombre(Jugador j, String... claves) {
        int n = 0;
        for (Item item : j.getInventario().getLista()) {
            String nombre = item.getNombre().toLowerCase();
            for (String clave : claves) { if (nombre.contains(clave)) { n++; break; } }
        }
        return n;
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

                // Si el jugador tiene animación de dado registrada, mostrarla antes de mover
                String[] config = obtenerConfigAnimacion(actual.getColor());
                if (config != null) {
                    final int tiradaFinal = tirada;
                    setUIInteractuable(false);
                    mostrarAnimacionTurno(actual, tiradaFinal,
                        config[0],
                        javafx.scene.paint.Color.web(config[1]),
                        () -> {
                            moverJugadorYAccion(actual, tiradaFinal, nombreDado);
                            if (dadoResultText != null) dadoResultText.setText("Has sacado un: " + tiradaFinal + " (" + nombreDado + ")");
                            actualizarTextosInventario(actual);
                            avanzarTurno();
                            dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
                        });
                } else {
                    moverJugadorYAccion(actual, tirada, nombreDado);
                    if (dadoResultText != null) dadoResultText.setText("Has sacado un: " + tirada + " (" + nombreDado + ")");
                    actualizarTextosInventario(actual);
                    avanzarTurno();
                    dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
                }
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
            encolarSaltoDirecto(objetivo, objetivo.getPosicion());
        } else if (bolasObj > bolasActual) {
            int diff = bolasObj - bolasActual;
            actual.moverPosicion(Math.max(0, actual.getPosicion() - diff));
            gestorUI.registrar("¡GUERRA DE BOLAS! " + objetivo.getNombre() + " contraataca a " + actual.getNombre()
                + " y gana por " + diff + " bolas. El perdedor retrocede " + diff + " casillas.");
            encolarSaltoDirecto(actual, actual.getPosicion());
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
            partida.setGanador(j); // ⭐ Guardar el ganador en el objeto Partida
            gestorUI.registrar("¡" + j.getNombre() + " HA LLEGADO A LA META Y GANA LA PARTIDA!");

            // ⭐ NUEVO: Notificar a Oracle (dispara el trigger 'incrementar_wins')
            notificarFinPartidaBBDD(j);

            // Navegar a la pantalla de victoria tras un breve retardo para que el log se vea
            javafx.application.Platform.runLater(() -> irAPantallaVictoria(j));
            return;
        }
        
        // Ejecuta la casilla (Oso, Trineo, Evento...)
        Casilla casillaActual = partida.getTablero().getCasillas().get(j.getPosicion());
        int posAntesCasilla = j.getPosicion();

        casillaActual.realizarAccion(partida, j);

        if (casillaActual instanceof Evento) {
            String msg = partida.getGestorEventos().getUltimoMensaje();
            ultimoEventoVisual.put(j, msg);
            encolarAnimacionEvento(j);
        }
        if (posAntesCasilla != j.getPosicion()) {
            if (casillaActual instanceof Oso && j.getPosicion() == 0) {
                encolarAnimacionOso(j);
            } else if (casillaActual instanceof Agujero) {
                encolarAnimacionAgujero(j, j.getPosicion());
            } else if (casillaActual instanceof Trineo) {
                encolarAnimacionTrineo(j, j.getPosicion());
            }
            // Salto directo para efectos de casilla (Oso, Trineo, etc.)
            encolarSaltoDirecto(j, j.getPosicion());
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
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.show();
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
                        pinguino.moverPosicion(posHole);
                        encolarSaltoDirecto(pinguino, posHole);
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
                        otro.moverPosicion(dest);
                        encolarSaltoDirecto(otro, dest);
                        gestorUI.registrar("¡Guerra de bolas! " + actual.getNombre() + " gana a " + otro.getNombre() + " por " + diff + " bolas.");
                    } else if (bolasO > bolasA) {
                        int diff = bolasO - bolasA;
                        int dest = Math.max(0, actual.getPosicion() - diff);
                        actual.moverPosicion(dest);
                        encolarSaltoDirecto(actual, dest);
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
            // Lógica CPU
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
            // 5. Turno humano: actualizar texto de turno + habilitar controles
            actualizarTextosTurno();
            setUIInteractuable(true);
        }
    }
 
    /**
     * 1. Configura el listener global de teclado: Escape abre/cierra el menú de opciones
     * (implementado como ContextMenu anclado a la ventana).
     */
    private void configurarEscapeMenu() {
        ctxMenuEscape = new javafx.scene.control.ContextMenu();
 
        javafx.scene.control.MenuItem miGuardar = new javafx.scene.control.MenuItem("Guardar partida");
        miGuardar.setOnAction(this::handleSaveGame);
        javafx.scene.control.MenuItem miGuardarSalir = new javafx.scene.control.MenuItem("Guardar y salir");
        miGuardarSalir.setOnAction(this::handleSaveAndQuit);
        javafx.scene.control.SeparatorMenuItem sep = new javafx.scene.control.SeparatorMenuItem();
        javafx.scene.control.MenuItem miSalir = new javafx.scene.control.MenuItem("Salir sin guardar");
        miSalir.setOnAction(this::handleQuitWithoutSaving);
        ctxMenuEscape.getItems().addAll(miGuardar, miGuardarSalir, sep, miSalir);
 
        // Adjuntar listener cuando la escena esté disponible
        tablero.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        if (ctxMenuEscape.isShowing()) {
                            ctxMenuEscape.hide();
                        } else {
                            ctxMenuEscape.show(newScene.getWindow(), 0, 32);
                        }
                    }
                });
            }
        });
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
 
    // ======================================    // int[]{destinoPos, tipo}: tipo 0 = saltito normal, tipo 1 = salto directo (Oso, Foca, etc.)
    private java.util.Map<Jugador, java.util.Queue<int[]>> colasAnimacion = new java.util.HashMap<>();
    private java.util.Map<Jugador, Integer> posVisual = new java.util.HashMap<>();
    private java.util.Map<Jugador, String> ultimoEventoVisual = new java.util.HashMap<>();
    private boolean animando = false;

    private void inicializarColas() {
        colasAnimacion.clear();
        for (Jugador j : partida.getJugadores()) {
            colasAnimacion.put(j, new java.util.LinkedList<>());
            posVisual.put(j, j.getPosicion());
        }
    }

    /** Coloca inmediatamente la ficha visual en la celda correspondiente a la posición del jugador. */
    private void actualizarPosicionVisual(Jugador j, ImageView ficha) {
        if (ficha == null) return;
        int pos = j.getPosicion();
        if (pos > 49) pos = 49;
        if (pos < 0)  pos = 0;
        int col = pos % 5;
        int fil = 9 - (pos / 5);
        GridPane.setColumnIndex(ficha, col);
        GridPane.setRowIndex(ficha, fil);
        posVisual.put(j, pos);
    }

    /** Encola cada casilla intermedia (saltito) entre 'desde' y 'hasta'. */
    private void encolarCaminoPasoAPaso(Jugador j, int desde, int hasta) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        int step = (desde < hasta) ? 1 : -1;
        for (int pos = desde + step; pos != hasta + step; pos += step) q.add(new int[]{pos, 0});
    }

    /** Encola un salto directo (sin pasar por casillas intermedias). Para Oso, Foca, bolas. */
    private void encolarSaltoDirecto(Jugador j, int hasta) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{hasta, 1});
    }

    /** Encola la animación de ataque de Oso. Tipo 2. */
    private void encolarAnimacionOso(Jugador j) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{j.getPosicion(), 2}); // Destino no importa
    }

    /** Encola la animación del regalo. Tipo 3. */
    private void encolarAnimacionEvento(Jugador j) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{j.getPosicion(), 3}); // Destino no importa
    }

    /** Encola la animación del Agujero. Tipo 4. */
    private void encolarAnimacionAgujero(Jugador j, int destino) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{destino, 4}); 
    }

    /** Encola la animación del Trineo. Tipo 5. */
    private void encolarAnimacionTrineo(Jugador j, int destino) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{destino, 5}); 
    }
 
    private void setUIInteractuable(boolean interactuable) {
        if (dado   != null) dado.setDisable(!interactuable);
        if (rapido != null) rapido.setDisable(!interactuable);
        if (lento  != null) lento.setDisable(!interactuable);
        if (peces  != null) peces.setDisable(!interactuable);
        if (nieve  != null) nieve.setDisable(!interactuable);
    }
 
    /**
     * 1. Animacion fluida casilla a casilla con TranslateTransition (300ms EASE_BOTH por paso).
     * Construye la lista de pasos y los encadena via setOnFinished uno tras otro.
     * El boton "Tirar dado" permanece deshabilitado durante toda la animacion.
     */
    private void dispararAnimadorVisual(Runnable onFinished) {
        if (animando) return;
        animando = true;
        setUIInteractuable(false);
 
        // Lista plana de pasos: int[]{playerIdx, posOrigen, posDestino, animType}
        java.util.List<int[]> listaPasos = new java.util.ArrayList<>();
        for (Jugador j : partida.getJugadores()) {
            java.util.Queue<int[]> q = colasAnimacion.get(j);
            if (q == null || q.isEmpty()) continue;
            int idx  = partida.getJugadores().indexOf(j);
            int prev = posVisual.getOrDefault(j, j.getPosicion());
            for (int[] entry : q) {
                int dest = entry[0], tipo = entry[1];
                listaPasos.add(new int[]{idx, prev, dest, tipo});
                prev = dest;
            }
            q.clear();
        }
 
        if (listaPasos.isEmpty()) {
            animando = false;
            setUIInteractuable(true);
            if (onFinished != null) javafx.application.Platform.runLater(onFinished);
            return;
        }
        ejecutarPasoSuave(listaPasos, 0, onFinished);
    }

    /** Ejecuta recursivamente cada paso con la animación que corresponda. */
    private void ejecutarPasoSuave(java.util.List<int[]> pasos, int idx, Runnable onFinished) {
        if (idx >= pasos.size()) {
            animando = false;
            setUIInteractuable(true);
            refrescarDistribucionVisual();
            if (onFinished != null) javafx.application.Platform.runLater(onFinished);
            return;
        }
        int[] paso    = pasos.get(idx);
        int playerIdx = paso[0], desde = paso[1], hasta = paso[2], tipo = paso[3];
        if (playerIdx < 0 || playerIdx >= partida.getJugadores().size()) {
            ejecutarPasoSuave(pasos, idx + 1, onFinished);
            return;
        }
        Jugador   j   = partida.getJugadores().get(playerIdx);
        ImageView fic = getFichaVisual(j);
        if (fic == null) {
            posVisual.put(j, hasta);
            ejecutarPasoSuave(pasos, idx + 1, onFinished);
            return;
        }
        Runnable next = () -> ejecutarPasoSuave(pasos, idx + 1, onFinished);
        if (tipo == 1) {
            animarSaltoDirecto(fic, j, hasta, next);
        } else if (tipo == 2) {
            mostrarAnimacionOso(next);
        } else if (tipo == 3) {
            mostrarAnimacionEvento(j, next);
        } else if (tipo == 4) {
            mostrarAnimacionAgujero(j, hasta, next);
        } else if (tipo == 5) {
            mostrarAnimacionTrineo(j, hasta, next);
        } else {
            animarConSaltito(fic, j, desde, hasta, next);
        }
    }

    /**
     * Saltito: arco parabólico de una celda a la siguiente (300ms, 3 keyframes).
     * El personaje sube ~25px a mitad del trayecto y baja en el destino.
     */
    private void animarConSaltito(ImageView ficha, Jugador j, int desde, int hasta, Runnable onDone) {
        double cellW = tablero.getWidth()  / 5.0;
        double cellH = tablero.getHeight() / 10.0;
        int colA = desde % 5, filA = 9 - (desde / 5);
        int colB = hasta % 5, filB = 9 - (hasta / 5);
        double dx = (colB - colA) * cellW;
        double dy = (filB - filA) * cellH;
        double extraX = 0, extraY = 0;
        if (hasta == 49 && tablero.getScene() != null) {
            extraX = computeIglooOffsetX(colB, cellW);
            extraY = computeIglooOffsetY(filB, cellH);
        }
        final double fExtraX = extraX, fExtraY = extraY;
        double fromX = ficha.getTranslateX();
        double fromY = ficha.getTranslateY();
        double toX   = dx + fExtraX;
        double toY   = dy + fExtraY;
        double midX  = (fromX + toX) / 2.0;
        double midY  = (fromY + toY) / 2.0 - 25.0; // arco hacia arriba
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                new javafx.animation.KeyValue(ficha.translateXProperty(), fromX),
                new javafx.animation.KeyValue(ficha.translateYProperty(), fromY)
            ),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(150),
                new javafx.animation.KeyValue(ficha.translateXProperty(), midX, javafx.animation.Interpolator.EASE_BOTH),
                new javafx.animation.KeyValue(ficha.translateYProperty(), midY, javafx.animation.Interpolator.EASE_OUT)
            ),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                new javafx.animation.KeyValue(ficha.translateXProperty(), toX, javafx.animation.Interpolator.EASE_BOTH),
                new javafx.animation.KeyValue(ficha.translateYProperty(), toY, javafx.animation.Interpolator.EASE_IN)
            )
        );
        tl.setOnFinished(e -> {
            GridPane.setColumnIndex(ficha, colB);
            GridPane.setRowIndex(ficha, filB);
            ficha.setTranslateX(fExtraX);
            ficha.setTranslateY(fExtraY);
            posVisual.put(j, hasta);
            onDone.run();
        });
        tl.play();
    }

    /**
     * Salto directo: se encoge, se teletransporta a la celda destino y reaparece.
     * Para efectos de Oso, Foca, bolas de nieve (sin pasar casilla a casilla).
     */
    private void animarSaltoDirecto(ImageView ficha, Jugador j, int hasta, Runnable onDone) {
        int colB = hasta % 5, filB = 9 - (hasta / 5);
        double extraX = 0, extraY = 0;
        if (hasta == 49 && tablero.getScene() != null) {
            double cellW = tablero.getWidth()  / 5.0;
            double cellH = tablero.getHeight() / 10.0;
            extraX = computeIglooOffsetX(colB, cellW);
            extraY = computeIglooOffsetY(filB, cellH);
        }
        final double fExtraX = extraX, fExtraY = extraY;
        // Fase 1: encoger y desvanecer (200ms)
        javafx.animation.ScaleTransition shrink = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(200), ficha);
        shrink.setToX(0.1); shrink.setToY(0.1);
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(200), ficha);
        fadeOut.setToValue(0.0);
        javafx.animation.ParallelTransition phase1 = new javafx.animation.ParallelTransition(shrink, fadeOut);
        phase1.setOnFinished(e -> {
            // Snap a destino
            GridPane.setColumnIndex(ficha, colB);
            GridPane.setRowIndex(ficha, filB);
            ficha.setTranslateX(fExtraX);
            ficha.setTranslateY(fExtraY);
            posVisual.put(j, hasta);
            // Fase 2: aparecer y crecer (200ms)
            javafx.animation.ScaleTransition grow = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(200), ficha);
            grow.setFromX(0.1); grow.setFromY(0.1);
            grow.setToX(1.0);   grow.setToY(1.0);
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), ficha);
            fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);
            javafx.animation.ParallelTransition phase2 = new javafx.animation.ParallelTransition(grow, fadeIn);
            phase2.setOnFinished(e2 -> onDone.run());
            phase2.play();
        });
        phase1.play();
    }

    /** Muestra una animación a pantalla completa de ataque de oso. */
    private void mostrarAnimacionOso(Runnable onFinish) {
        javafx.scene.Scene escena = tablero.getScene();
        if (escena == null) {
            if (onFinish != null) onFinish.run();
            return;
        }
        double W = escena.getWidth();
        double H = escena.getHeight();
        javafx.scene.layout.Pane rootPane = (javafx.scene.layout.Pane) escena.getRoot();

        javafx.scene.layout.Pane overlayPane = new javafx.scene.layout.Pane();
        overlayPane.setStyle("-fx-background-color: black;");
        overlayPane.setManaged(false);
        overlayPane.resize(W, H);
        overlayPane.setOpacity(0);
        rootPane.getChildren().add(overlayPane);

        javafx.scene.image.ImageView osoView = new javafx.scene.image.ImageView();
        var recurso = getClass().getResourceAsStream("/resources/ataque_oso.png");
        if (recurso != null) {
            osoView.setImage(new Image(recurso));
        }
        double OSO_SIZE = 400;
        osoView.setFitWidth(OSO_SIZE);
        osoView.setFitHeight(OSO_SIZE);
        osoView.setPreserveRatio(true);
        osoView.setOpacity(0);
        osoView.setScaleX(0.5);
        osoView.setScaleY(0.5);
        osoView.setManaged(false);
        osoView.setLayoutX(W / 2 - OSO_SIZE / 2);
        osoView.setLayoutY(H / 2 - OSO_SIZE / 2 - 50);
        rootPane.getChildren().add(osoView);

        javafx.scene.text.Text lblMensaje = new javafx.scene.text.Text("Te ha atacado un oso 😱\nVuelves al principio");
        lblMensaje.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 40));
        lblMensaje.setFill(javafx.scene.paint.Color.web("#FF4444"));
        lblMensaje.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblMensaje.setWrappingWidth(W);
        lblMensaje.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));
        lblMensaje.setOpacity(0);
        lblMensaje.setManaged(false);
        lblMensaje.setLayoutX(0);
        lblMensaje.setLayoutY(H / 2 + OSO_SIZE / 2 + 20);
        rootPane.getChildren().add(lblMensaje);

        javafx.animation.FadeTransition ftOver = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOver.setToValue(0.8);

        javafx.animation.FadeTransition ftOso = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), osoView);
        ftOso.setToValue(1);
        javafx.animation.ScaleTransition stOso = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), osoView);
        stOso.setToX(1.2);
        stOso.setToY(1.2);

        javafx.animation.FadeTransition ftText = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
        ftText.setToValue(1);

        javafx.animation.ParallelTransition ptIn = new javafx.animation.ParallelTransition(ftOver, ftOso, stOso, ftText);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2000));

        javafx.animation.FadeTransition ftOverOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOverOut.setToValue(0);
        javafx.animation.FadeTransition ftOsoOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), osoView);
        ftOsoOut.setToValue(0);
        javafx.animation.FadeTransition ftTextOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
        ftTextOut.setToValue(0);

        javafx.animation.ParallelTransition ptOut = new javafx.animation.ParallelTransition(ftOverOut, ftOsoOut, ftTextOut);

        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(ptIn, pause, ptOut);
        seq.setOnFinished(e -> {
            rootPane.getChildren().removeAll(overlayPane, osoView, lblMensaje);
            if (onFinish != null) onFinish.run();
        });
        seq.play();
    }

    /** Muestra la animación inicial del regalo temblando y luego el objeto obtenido. */
    private void mostrarAnimacionEvento(Jugador j, Runnable onFinish) {
        javafx.scene.Scene escena = tablero.getScene();
        if (escena == null) {
            if (onFinish != null) onFinish.run();
            return;
        }
        double W = escena.getWidth();
        double H = escena.getHeight();
        javafx.scene.layout.Pane rootPane = (javafx.scene.layout.Pane) escena.getRoot();

        javafx.scene.layout.Pane overlayPane = new javafx.scene.layout.Pane();
        overlayPane.setStyle("-fx-background-color: black;");
        overlayPane.setManaged(false);
        overlayPane.resize(W, H);
        overlayPane.setOpacity(0);
        rootPane.getChildren().add(overlayPane);

        javafx.scene.image.ImageView regaloView = new javafx.scene.image.ImageView();
        var recurso = getClass().getResourceAsStream("/resources/objeto_random.png");
        if (recurso != null) {
            regaloView.setImage(new Image(recurso));
        }
        double REGALO_SIZE = 300;
        regaloView.setFitWidth(REGALO_SIZE);
        regaloView.setFitHeight(REGALO_SIZE);
        regaloView.setPreserveRatio(true);
        regaloView.setOpacity(0);
        regaloView.setScaleX(0.5);
        regaloView.setScaleY(0.5);
        regaloView.setManaged(false);
        regaloView.setLayoutX(W / 2 - REGALO_SIZE / 2);
        regaloView.setLayoutY(H / 2 - REGALO_SIZE / 2);
        rootPane.getChildren().add(regaloView);

        // Fase 1: Entrar (FadeIn + Scale)
        javafx.animation.FadeTransition ftOver = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOver.setToValue(0.8);

        javafx.animation.FadeTransition ftRegalo = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), regaloView);
        ftRegalo.setToValue(1);
        javafx.animation.ScaleTransition stRegalo = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), regaloView);
        stRegalo.setToX(1.0);
        stRegalo.setToY(1.0);

        javafx.animation.ParallelTransition ptIn = new javafx.animation.ParallelTransition(ftOver, ftRegalo, stRegalo);

        // Fase 2: Tiembla (Rotate) ~900ms
        javafx.animation.RotateTransition rt1 = new javafx.animation.RotateTransition(javafx.util.Duration.millis(50), regaloView);
        rt1.setByAngle(15);
        
        javafx.animation.RotateTransition rtLoop = new javafx.animation.RotateTransition(javafx.util.Duration.millis(100), regaloView);
        rtLoop.setFromAngle(15);
        rtLoop.setToAngle(-15);
        rtLoop.setCycleCount(8);
        rtLoop.setAutoReverse(true);

        javafx.animation.RotateTransition rtEnd = new javafx.animation.RotateTransition(javafx.util.Duration.millis(50), regaloView);
        rtEnd.setToAngle(0);

        javafx.animation.SequentialTransition rtShake = new javafx.animation.SequentialTransition(rt1, rtLoop, rtEnd);

        // Fase 3: Salir Regalo (FadeOut)
        javafx.animation.FadeTransition ftRegaloOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), regaloView);
        ftRegaloOut.setToValue(0);

        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(ptIn, rtShake, ftRegaloOut);
        seq.setOnFinished(e -> {
            rootPane.getChildren().remove(regaloView);
            
            String msg = ultimoEventoVisual.getOrDefault(j, "");
            String itemPath = null;
            String itemNombre = "";
            String msgLower = msg.toLowerCase();
            
            if (msgLower.contains("lento")) {
                itemPath = "/resources/dado_lento.png";
                itemNombre = "un Dado Lento";
            } else if (msgLower.contains("rápido") || msgLower.contains("rapido")) {
                itemPath = "/resources/dado_rapido.png";
                itemNombre = "un Dado Rápido";
            } else if ((msgLower.contains("bola") || msgLower.contains("nieve")) && !msgLower.contains("moto")) {
                itemPath = "/resources/bola_nieve.png";
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s+bolas? de nieve").matcher(msgLower);
                if (m.find()) {
                    String qty = m.group(1);
                    itemNombre = qty + (qty.equals("1") ? " Bola de Nieve" : " Bolas de Nieve");
                } else {
                    itemNombre = "Bolas de Nieve";
                }
            } else if (msgLower.contains("pez")) {
                itemPath = "/resources/pez.png";
                itemNombre = "un Pez";
            }
            
            if (itemPath != null) {
                // Phase 4: Show Item
                javafx.scene.image.ImageView itemView = new javafx.scene.image.ImageView();
                var rec = getClass().getResourceAsStream(itemPath);
                if (rec != null) itemView.setImage(new Image(rec));
                itemView.setFitWidth(REGALO_SIZE);
                itemView.setFitHeight(REGALO_SIZE);
                itemView.setPreserveRatio(true);
                itemView.setOpacity(0);
                itemView.setScaleX(0.5);
                itemView.setScaleY(0.5);
                itemView.setLayoutX(W / 2 - REGALO_SIZE / 2);
                itemView.setLayoutY(H / 2 - REGALO_SIZE / 2);
                rootPane.getChildren().add(itemView);
                
                javafx.scene.text.Text lblMensaje = new javafx.scene.text.Text(j.getNombre() + " ha obtenido\n" + itemNombre);
                lblMensaje.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 40));
                lblMensaje.setFill(javafx.scene.paint.Color.WHITE); // blanco
                lblMensaje.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                lblMensaje.setWrappingWidth(W);
                lblMensaje.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));
                lblMensaje.setOpacity(0);
                lblMensaje.setLayoutX(0);
                lblMensaje.setLayoutY(H / 2 + REGALO_SIZE / 2 + 20);
                rootPane.getChildren().add(lblMensaje);

                javafx.animation.FadeTransition ftItem = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), itemView);
                ftItem.setToValue(1);
                javafx.animation.ScaleTransition stItem = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), itemView);
                stItem.setToX(1.0); stItem.setToY(1.0);
                
                javafx.animation.FadeTransition ftText = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
                ftText.setToValue(1);
                
                javafx.animation.ParallelTransition ptItemIn = new javafx.animation.ParallelTransition(ftItem, stItem, ftText);
                
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                
                javafx.animation.FadeTransition ftItemOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), itemView);
                ftItemOut.setToValue(0);
                javafx.animation.FadeTransition ftTextOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
                ftTextOut.setToValue(0);
                javafx.animation.FadeTransition ftOverOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
                ftOverOut.setToValue(0);
                
                javafx.animation.ParallelTransition ptItemOut = new javafx.animation.ParallelTransition(ftItemOut, ftTextOut, ftOverOut);
                
                javafx.animation.SequentialTransition seqItem = new javafx.animation.SequentialTransition(ptItemIn, pause, ptItemOut);
                seqItem.setOnFinished(e2 -> {
                    rootPane.getChildren().removeAll(overlayPane, itemView, lblMensaje);
                    if (onFinish != null) onFinish.run();
                });
                seqItem.play();
            } else {
                // Not an item event (e.g. moto, lose turn). Show the text from the event directly
                javafx.scene.text.Text lblMensaje = new javafx.scene.text.Text(msg);
                lblMensaje.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 30));
                lblMensaje.setFill(javafx.scene.paint.Color.WHITE);
                lblMensaje.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                lblMensaje.setWrappingWidth(W);
                lblMensaje.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));
                lblMensaje.setOpacity(0);
                lblMensaje.setLayoutX(0);
                lblMensaje.setLayoutY(H / 2);
                rootPane.getChildren().add(lblMensaje);
                
                javafx.animation.FadeTransition ftText = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
                ftText.setToValue(1);
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2000));
                javafx.animation.FadeTransition ftTextOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
                ftTextOut.setToValue(0);
                javafx.animation.FadeTransition ftOverOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
                ftOverOut.setToValue(0);
                
                javafx.animation.ParallelTransition ptOutFinal = new javafx.animation.ParallelTransition(ftTextOut, ftOverOut);
                javafx.animation.SequentialTransition seqItem = new javafx.animation.SequentialTransition(ftText, pause, ptOutFinal);
                seqItem.setOnFinished(e2 -> {
                    rootPane.getChildren().removeAll(overlayPane, lblMensaje);
                    if (onFinish != null) onFinish.run();
                });
                seqItem.play();
            }
        });
        seq.play();
    }

    /** Muestra la animación a pantalla completa del agujero. */
    private void mostrarAnimacionAgujero(Jugador j, int destino, Runnable onFinish) {
        javafx.scene.Scene escena = tablero.getScene();
        if (escena == null) {
            if (onFinish != null) onFinish.run();
            return;
        }
        double W = escena.getWidth();
        double H = escena.getHeight();
        javafx.scene.layout.Pane rootPane = (javafx.scene.layout.Pane) escena.getRoot();

        javafx.scene.layout.Pane overlayPane = new javafx.scene.layout.Pane();
        overlayPane.setStyle("-fx-background-color: black;");
        overlayPane.setManaged(false);
        overlayPane.resize(W, H);
        overlayPane.setOpacity(0);
        rootPane.getChildren().add(overlayPane);

        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        var recurso = getClass().getResourceAsStream("/resources/agujero_ani.png");
        if (recurso != null) {
            imgView.setImage(new Image(recurso));
        }
        double IMG_SIZE = 400;
        imgView.setFitWidth(IMG_SIZE);
        imgView.setFitHeight(IMG_SIZE);
        imgView.setPreserveRatio(true);
        imgView.setOpacity(0);
        imgView.setScaleX(0.5);
        imgView.setScaleY(0.5);
        imgView.setManaged(false);
        imgView.setLayoutX(W / 2 - IMG_SIZE / 2);
        imgView.setLayoutY(H / 2 - IMG_SIZE / 2 - 50);
        rootPane.getChildren().add(imgView);

        javafx.scene.text.Text lblMensaje = new javafx.scene.text.Text("¡" + j.getNombre() + " se ha caído!\ncaes a la casilla " + destino);
        lblMensaje.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 40));
        lblMensaje.setFill(javafx.scene.paint.Color.WHITE);
        lblMensaje.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblMensaje.setWrappingWidth(W);
        lblMensaje.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));
        lblMensaje.setOpacity(0);
        lblMensaje.setManaged(false);
        lblMensaje.setLayoutX(0);
        lblMensaje.setLayoutY(H / 2 + IMG_SIZE / 2 + 20);
        rootPane.getChildren().add(lblMensaje);

        javafx.animation.FadeTransition ftOver = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOver.setToValue(0.8);

        javafx.animation.FadeTransition ftImg = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), imgView);
        ftImg.setToValue(1);
        javafx.animation.ScaleTransition stImg = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), imgView);
        stImg.setToX(1.2);
        stImg.setToY(1.2);

        javafx.animation.FadeTransition ftText = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
        ftText.setToValue(1);

        javafx.animation.ParallelTransition ptIn = new javafx.animation.ParallelTransition(ftOver, ftImg, stImg, ftText);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2000));

        javafx.animation.FadeTransition ftOverOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOverOut.setToValue(0);
        javafx.animation.FadeTransition ftImgOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), imgView);
        ftImgOut.setToValue(0);
        javafx.animation.FadeTransition ftTextOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
        ftTextOut.setToValue(0);

        javafx.animation.ParallelTransition ptOut = new javafx.animation.ParallelTransition(ftOverOut, ftImgOut, ftTextOut);

        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(ptIn, pause, ptOut);
        seq.setOnFinished(e -> {
            rootPane.getChildren().removeAll(overlayPane, imgView, lblMensaje);
            if (onFinish != null) onFinish.run();
        });
        seq.play();
    }

    /** Muestra la animación a pantalla completa del trineo. */
    private void mostrarAnimacionTrineo(Jugador j, int destino, Runnable onFinish) {
        javafx.scene.Scene escena = tablero.getScene();
        if (escena == null) {
            if (onFinish != null) onFinish.run();
            return;
        }
        double W = escena.getWidth();
        double H = escena.getHeight();
        javafx.scene.layout.Pane rootPane = (javafx.scene.layout.Pane) escena.getRoot();

        javafx.scene.layout.Pane overlayPane = new javafx.scene.layout.Pane();
        overlayPane.setStyle("-fx-background-color: black;");
        overlayPane.setManaged(false);
        overlayPane.resize(W, H);
        overlayPane.setOpacity(0);
        rootPane.getChildren().add(overlayPane);

        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        var recurso = getClass().getResourceAsStream("/resources/trineo_ani.png");
        if (recurso != null) {
            imgView.setImage(new Image(recurso));
        }
        double IMG_SIZE = 400;
        imgView.setFitWidth(IMG_SIZE);
        imgView.setFitHeight(IMG_SIZE);
        imgView.setPreserveRatio(true);
        imgView.setOpacity(0);
        imgView.setScaleX(0.5);
        imgView.setScaleY(0.5);
        imgView.setManaged(false);
        imgView.setLayoutX(W / 2 - IMG_SIZE / 2);
        imgView.setLayoutY(H / 2 - IMG_SIZE / 2 - 50);
        rootPane.getChildren().add(imgView);

        javafx.scene.text.Text lblMensaje = new javafx.scene.text.Text("¡" + j.getNombre() + " ha montado en trineo!\navanzas a la casilla " + destino);
        lblMensaje.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 40));
        lblMensaje.setFill(javafx.scene.paint.Color.WHITE);
        lblMensaje.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblMensaje.setWrappingWidth(W);
        lblMensaje.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));
        lblMensaje.setOpacity(0);
        lblMensaje.setManaged(false);
        lblMensaje.setLayoutX(0);
        lblMensaje.setLayoutY(H / 2 + IMG_SIZE / 2 + 20);
        rootPane.getChildren().add(lblMensaje);

        javafx.animation.FadeTransition ftOver = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOver.setToValue(0.8);

        javafx.animation.FadeTransition ftImg = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), imgView);
        ftImg.setToValue(1);
        javafx.animation.ScaleTransition stImg = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), imgView);
        stImg.setToX(1.2);
        stImg.setToY(1.2);

        javafx.animation.FadeTransition ftText = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
        ftText.setToValue(1);

        javafx.animation.ParallelTransition ptIn = new javafx.animation.ParallelTransition(ftOver, ftImg, stImg, ftText);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2000));

        javafx.animation.FadeTransition ftOverOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOverOut.setToValue(0);
        javafx.animation.FadeTransition ftImgOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), imgView);
        ftImgOut.setToValue(0);
        javafx.animation.FadeTransition ftTextOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
        ftTextOut.setToValue(0);

        javafx.animation.ParallelTransition ptOut = new javafx.animation.ParallelTransition(ftOverOut, ftImgOut, ftTextOut);

        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(ptIn, pause, ptOut);
        seq.setOnFinished(e -> {
            rootPane.getChildren().removeAll(overlayPane, imgView, lblMensaje);
            if (onFinish != null) onFinish.run();
        });
        seq.play();
    }
 
    /** Offset X en px para que la ficha en casilla 49 quede encima del iglu (~85.5% del ancho). */
    private double computeIglooOffsetX(int colB, double cellW) {
        double iglooSceneX = tablero.getScene().getWidth() * 0.855;
        javafx.geometry.Point2D p = tablero.localToScene((colB + 0.5) * cellW, 0);
        return iglooSceneX - p.getX();
    }
 
    /** Offset Y en px para que la ficha en casilla 49 quede encima del iglu (~13% del alto). */
    private double computeIglooOffsetY(int filB, double cellH) {
        double iglooSceneY = tablero.getScene().getHeight() * 0.13;
        javafx.geometry.Point2D p = tablero.localToScene(0, (filB + 0.5) * cellH);
        return iglooSceneY - p.getY();
    }
 
    /**
     * 2. Varios personajes en la misma casilla:
     * Distribuye en fila horizontal los personajes que comparten celda.
     * Para jugador solo: solo resetea escala; NO toca translateX para preservar
     * el offset del iglu si el jugador esta en casilla 49.
     */
    private void refrescarDistribucionVisual() {
        java.util.Map<Integer, java.util.List<Jugador>> celdaJugadores = new java.util.HashMap<>();
        for (Jugador j : partida.getJugadores()) {
            int pos = posVisual.getOrDefault(j, j.getPosicion());
            celdaJugadores.computeIfAbsent(pos, k -> new java.util.ArrayList<>()).add(j);
        }
        for (java.util.List<Jugador> lista : celdaJugadores.values()) {
            int total = lista.size();
            for (int i = 0; i < total; i++) {
                ImageView ficha = getFichaVisual(lista.get(i));
                if (ficha == null) continue;
                if (total == 1) {
                    // Solo escala; translate lo gestiona la animacion (iglu offset)
                    ficha.setScaleX(1.0); ficha.setScaleY(1.0);
                } else if (total == 2) {
                    ficha.setScaleX(0.85); ficha.setScaleY(0.85);
                    ficha.setTranslateX(i == 0 ? -15 : 15);
                } else if (total == 3) {
                    ficha.setScaleX(0.70); ficha.setScaleY(0.70);
                    ficha.setTranslateX(i == 0 ? -20 : (i == 1 ? 0 : 20));
                } else {
                    ficha.setScaleX(0.60); ficha.setScaleY(0.60);
                    int[] offsets = {-22, -7, 7, 22};
                    ficha.setTranslateX(i < offsets.length ? offsets[i] : 0);
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

    // ==========================================
    // ANIMACIÓN DE TURNO – GENÉRICA POR COLOR
    // ==========================================

    /**
     * Devuelve la configuración de animación para el color dado:
     *   [0] = ruta de la imagen del dado  (/resources/dado_XXX.png)
     *   [1] = color HEX del texto de resultado
     * Devuelve null si ese color no tiene animación configurada.
     * Para añadir un nuevo color, basta con añadir una línea aquí.
     */
    private String[] obtenerConfigAnimacion(String color) {
        if (color == null) return null;
        switch (color.toLowerCase()) {
            case "amarillo": return new String[]{"/resources/dado_amarillo.png", "#FFD700"};
            case "azul":     return new String[]{"/resources/dado_azul.png",     "#00BFFF"};
            case "rojo":     return new String[]{"/resources/dado_rojo.png",      "#FF4444"};
            case "verde":    return new String[]{"/resources/dado_verde.png",     "#00FF7F"};
            default:         return null;
        }
    }

    /**
     * Muestra la secuencia animada de turno (overlay + dado girando + resultado).
     * Reutilizable para cualquier color de jugador.
     *
     * @param actual        Jugador cuyo turno se anima.
     * @param resultadoDado Valor del dado (1-6) ya calculado.
     * @param rutaDado      Ruta del recurso imagen del dado (p.ej. /resources/dado_azul.png).
     * @param colorTexto    Color del texto de resultado (API Java, inmune a CSS).
     * @param onFinish      Callback ejecutado al terminar: aquí va el movimiento del personaje.
     */
    private void mostrarAnimacionTurno(Jugador actual, int resultadoDado,
                                       String rutaDado,
                                       javafx.scene.paint.Color colorTexto,
                                       Runnable onFinish) {
        // --- Obtener dimensiones de la escena ---
        javafx.scene.Scene escena = tablero.getScene();
        if (escena == null) {
            // Fallback: ejecutar directamente si la escena no está disponible
            if (onFinish != null) onFinish.run();
            return;
        }
        double W = escena.getWidth();
        double H = escena.getHeight();
        javafx.scene.layout.Pane rootPane = (javafx.scene.layout.Pane) escena.getRoot();

    
        final double DADO_SIZE   = 380;
        final double TEXTO_H     = 50;       // altura estimada del label "Turno de X"
        final double GAP         = 20;       // espacio entre texto y dado
        final double GRUPO_H     = TEXTO_H + GAP + DADO_SIZE;  // 450px
        final double GRUPO_TOP   = H / 2 - GRUPO_H / 2;       // Y inicio del bloque

        // --- 1. Overlay semitransparente (solo fondo oscuro) ---
        // El dado, lblTurno y lblResultado NO son hijos del overlay para evitar
        // que hereden su opacidad y se vean transparentes.
        javafx.scene.layout.Pane overlayPane = new javafx.scene.layout.Pane();
        overlayPane.setStyle("-fx-background-color: black;");
        overlayPane.setManaged(false);
        overlayPane.setLayoutX(0);
        overlayPane.setLayoutY(0);
        overlayPane.resize(W, H);
        overlayPane.setOpacity(0);
        overlayPane.setMouseTransparent(false); // bloquea clics del usuario
        rootPane.getChildren().add(overlayPane);

        // --- 2. Text "Turno de X" ---
        // Text.setFill() es API Java directa, inmune al CSS .label { -fx-text-fill: white }.
        javafx.scene.text.Text lblTurno = new javafx.scene.text.Text("Turno de " + actual.getNombre());
        lblTurno.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 40));
        lblTurno.setFill(javafx.scene.paint.Color.WHITE);
        lblTurno.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblTurno.setWrappingWidth(W);
        lblTurno.setEffect(new javafx.scene.effect.DropShadow(14, javafx.scene.paint.Color.BLACK));
        lblTurno.setOpacity(0);
        lblTurno.setManaged(false);
        lblTurno.setLayoutX(0);
        lblTurno.setLayoutY(GRUPO_TOP + 42);
        rootPane.getChildren().add(lblTurno);

        // --- 3. ImageView del dado (imagen según rutaDado recibida) ---
        javafx.scene.image.ImageView dadoView = new javafx.scene.image.ImageView();
        var recurso = getClass().getResourceAsStream(rutaDado);  // imagen según color del jugador
        if (recurso != null) {
            dadoView.setImage(new Image(recurso));
        } else {
            System.err.println("[AnimAmarillo] No se encontró dado_amarillo.png");
        }
        dadoView.setFitWidth(DADO_SIZE);
        dadoView.setFitHeight(DADO_SIZE);
        dadoView.setPreserveRatio(true);
        dadoView.setOpacity(0);
        dadoView.setScaleX(0.5);
        dadoView.setScaleY(0.5);
        dadoView.setManaged(false);
        dadoView.setLayoutX(W / 2 - DADO_SIZE / 2);          // centrado horizontalmente
        dadoView.setLayoutY(GRUPO_TOP + TEXTO_H + GAP);       // justo debajo del texto
        rootPane.getChildren().add(dadoView);

        // --- 4. Text de resultado ("X ha sacado un N") ---
        // setFill(colorTexto) garantiza el color del jugador sin interferencia de CSS.
        javafx.scene.text.Text lblResultado = new javafx.scene.text.Text(
            actual.getNombre() + " ha sacado un " + resultadoDado);
        lblResultado.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 52));
        lblResultado.setFill(colorTexto);   // color del jugador, API Java pura
        lblResultado.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblResultado.setWrappingWidth(W);
        lblResultado.setEffect(new javafx.scene.effect.DropShadow(18, javafx.scene.paint.Color.BLACK));
        lblResultado.setOpacity(0);
        lblResultado.setManaged(false);
        lblResultado.setLayoutX(0);
        lblResultado.setLayoutY(H / 2 + 26);
        rootPane.getChildren().add(lblResultado); // último hijo → z-index más alto

        // ==============================================
        // Construcción de la SequentialTransition
        // ==============================================

        // Paso 1 (300ms): overlay oscurece + dado crece + lblTurno aparecen juntos
        javafx.animation.FadeTransition fadeInOverlay = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), overlayPane);
        fadeInOverlay.setFromValue(0);
        fadeInOverlay.setToValue(0.6);

        javafx.animation.FadeTransition fadeInDado = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), dadoView);
        fadeInDado.setFromValue(0);
        fadeInDado.setToValue(1);

        javafx.animation.ScaleTransition zoomDado = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(300), dadoView);
        zoomDado.setFromX(0.5); zoomDado.setFromY(0.5);
        zoomDado.setToX(1.0);   zoomDado.setToY(1.0);

        javafx.animation.FadeTransition fadeInTurno = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), lblTurno);
        fadeInTurno.setFromValue(0);
        fadeInTurno.setToValue(1);

        // Overlay + (dado zoom + fade + texto turno) todos a la vez
        javafx.animation.ParallelTransition paso1 = new javafx.animation.ParallelTransition(
            fadeInOverlay, fadeInDado, zoomDado, fadeInTurno);

        // Paso 2 (2000ms): dado gira 3 vueltas mientras "Turno de X" permanece visible
        javafx.animation.RotateTransition girarDado = new javafx.animation.RotateTransition(
            javafx.util.Duration.millis(2000), dadoView);
        girarDado.setFromAngle(0);
        girarDado.setToAngle(360 * 3);
        girarDado.setInterpolator(javafx.animation.Interpolator.LINEAR);

        // Paso 3 (400ms): dado y lblTurno desaparecen juntos
        javafx.animation.FadeTransition fadeOutDado = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(400), dadoView);
        fadeOutDado.setFromValue(1);
        fadeOutDado.setToValue(0);

        javafx.animation.FadeTransition fadeOutTurno = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(400), lblTurno);
        fadeOutTurno.setFromValue(1);
        fadeOutTurno.setToValue(0);

        javafx.animation.ParallelTransition paso3 =
            new javafx.animation.ParallelTransition(fadeOutDado, fadeOutTurno);

        // Paso 4 (300ms): mensaje de resultado aparece en el centro
        javafx.animation.FadeTransition fadeInLabel = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), lblResultado);
        fadeInLabel.setFromValue(0);
        fadeInLabel.setToValue(1);

        // Paso 5 (1500ms): pausa para que el jugador lea el resultado
        javafx.animation.PauseTransition pausaResultado =
            new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));

        // Paso 6 (400ms): resultado y overlay desaparecen juntos
        javafx.animation.FadeTransition fadeOutLabel = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(400), lblResultado);
        fadeOutLabel.setFromValue(1);
        fadeOutLabel.setToValue(0);

        javafx.animation.FadeTransition fadeOutOverlay = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(400), overlayPane);
        fadeOutOverlay.setFromValue(0.6);
        fadeOutOverlay.setToValue(0);

        javafx.animation.ParallelTransition paso6 =
            new javafx.animation.ParallelTransition(fadeOutLabel, fadeOutOverlay);

        // --- Secuencia completa encadenada ---
        javafx.animation.SequentialTransition secuenciaCompleta =
            new javafx.animation.SequentialTransition(
                paso1,           // overlay + dado + "Turno de X" aparecen
                girarDado,       // dado gira 3 vueltas
                paso3,           // dado + "Turno de X" desaparecen
                fadeInLabel,     // resultado aparece en el centro
                pausaResultado,  // pausa 1.5s
                paso6            // resultado + overlay desaparecen
            );

        secuenciaCompleta.setOnFinished(e -> {
            // Eliminar todos los nodos del grafo de la escena
            rootPane.getChildren().removeAll(overlayPane, lblTurno, dadoView, lblResultado);
            // Ejecutar el movimiento del personaje amarillo
            if (onFinish != null) onFinish.run();
        });

        secuenciaCompleta.play();
    }

    /**
     * Notifica a Oracle que la partida ha finalizado.
     * 1. Si la partida no se había guardado antes, le asigna ID automático.
     * 2. Guarda el estado FINAL de la partida (con el ganador en su posición de victoria).
     * 3. Asigna el ganador en BBDD (dispara el trigger 'incrementar_wins').
     */
    private void notificarFinPartidaBBDD(Jugador ganador) {
        try {
            GestorBBDD gestor = new GestorBBDD();
            gestor.iniciarConexionGUI();

            if (gestor.getConexion() == null) {
                gestorUI.registrar("⚠️ Sin conexión a BBDD, no se actualizan estadísticas.");
                return;
            }

            // PASO 1: Si la partida no se había guardado nunca, asignar ID automático
            if (idPartidaActual <= 0) {
                int nuevoId = gestor.obtenerSiguienteIdPartida();
                if (nuevoId > 0) {
                    this.idPartidaActual = nuevoId;
                    gestorUI.registrar("ℹ️ Partida nueva creada con ID " + nuevoId);
                } else {
                    gestorUI.registrar("⚠️ No se pudo generar ID, no se guardará la partida.");
                    gestor.cerrarConexion();
                    return;
                }
            }

            // PASO 2: Guardar el ESTADO FINAL de la partida (con el ganador en la meta)
            boolean exitoGuardado = gestor.guardarBBDD(partida, idPartidaActual);
            if (exitoGuardado) {
                gestorUI.registrar("💾 Estado final de la partida guardado.");
            } else {
                gestorUI.registrar("⚠️ No se pudo guardar el estado final.");
            }

            // PASO 3: Buscar id del ganador en tabla JUGADOR
            int idGanador = gestor.obtenerIdJugador(ganador.getNombre());
            if (idGanador != -1) {
                // PASO 4: Construir lista de TODOS los participantes (no solo el ganador)
                java.util.List<String> participantes = new java.util.ArrayList<>();
                if (partida != null && partida.getJugadores() != null) {
                    for (Jugador jug : partida.getJugadores()) {
                        // Solo añadir Pinguinos (no Focas/CPU, que no son usuarios reales)
                        if (jug instanceof Pinguino) {
                            participantes.add(jug.getNombre());
                        }
                    }
                }

                // PASO 5: Asignar ganador y actualizar num_partidas a todos
                gestor.finalizarPartida(idPartidaActual, idGanador, participantes);
                gestorUI.registrar("✅ Estadísticas actualizadas para " + participantes.size() 
                                 + " jugadores. Ganador: " + ganador.getNombre());
            } else {
                gestorUI.registrar("⚠️ El ganador no está registrado en JUGADOR.");
            }

            gestor.cerrarConexion();
        } catch (Exception e) {
            gestorUI.registrar("❌ Error al actualizar estadísticas: " + e.getMessage());
        }
    }
}
