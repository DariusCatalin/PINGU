package JOC_DEL_PINGU;
 
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;
 
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
 
public class PantallaPartida {
 
    // --- ELEMENTOS DE LA INTERFAZ GRÁFICA VINCULADOS AL FXML ---
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
    @FXML private ImageView P1; // FICHA VISUAL DEL JUGADOR 1
    @FXML private ImageView P2; // FICHA VISUAL DEL JUGADOR 2
    @FXML private ImageView P3; // FICHA VISUAL DEL JUGADOR 3
    @FXML private ImageView P4; // FICHA VISUAL DEL JUGADOR 4
 
    // --- VARIABLES INTERNAS DE LA LÓGICA DEL JUEGO ---
    private Partida partida;
    private int idPartidaActual = -1; // ID DE LA PARTIDA EN BASE DE DATOS (-1 = AÚN NO GUARDADA)
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
    // MENÚ CONTEXTUAL QUE SE ABRE Y CIERRA AL PULSAR LA TECLA ESCAPE
    private javafx.scene.control.ContextMenu ctxMenuEscape;
    // REPRODUCTOR DE AUDIO DE LA FOCA (SUENA EXACTAMENTE 1 SEGUNDO AL INICIO DE SU TURNO)
    private MediaPlayer mediaPlayerFoca;
 
    // GESTOR DE EVENTOS: MUESTRA LOS MENSAJES DEL JUEGO EN EL ÁREA DE TEXTO DE LA UI
    private GestorEventos gestorUI = new GestorEventos() {
        @Override
        public void registrar(String mensaje) {
            super.registrar(mensaje);
            System.out.println(mensaje);
            if (eventos != null) {
                eventos.appendText(mensaje + "\n");
                // DESPLAZA EL ÁREA DE TEXTO HACIA ABAJO PARA SIEMPRE VER EL ÚLTIMO MENSAJE
                eventos.setScrollTop(Double.MAX_VALUE);
            }
        }
    };
 
    // LISTA DE JUGADORES CONFIGURADOS EN LA PANTALLA DE CONFIGURACIÓN
    private java.util.ArrayList<Jugador> jugadoresConfig = null;
 
 
    // RECIBE LA LISTA DE JUGADORES DESDE PANTALLACONFIG ANTES DE MOSTRAR ESTA PANTALLA
    public void setJugadores(java.util.ArrayList<Jugador> jugadores) {
        this.jugadoresConfig = jugadores;
        // SI initialize() YA SE EJECUTÓ ANTES, RECONFIGURAMOS LA PARTIDA CON LOS NUEVOS JUGADORES
        if (partida != null) {
            configurarPartida();
        }
    }
    
    // GUARDA EL ID DE UNA PARTIDA CARGADA DESDE LA BASE DE DATOS
    // MÉTODO PARA ASIGNAR EL ID DE UNA PARTIDA QUE VIENE DE LA BASE DE DATOS
    public void setIdPartidaCargada(int idPartida) {
        this.idPartidaActual = idPartida;
        if (gestorUI != null) {
            gestorUI.registrar("📂 Partida cargada con ID " + idPartida);
        }
    }
    
    // CARGA UNA PARTIDA EXISTENTE (LLAMADO DESDE PANTALLA PRINCIPAL O GESTOR BBDD)
    public void setPartidaCargada(Partida pCargada) {
        this.partida = pCargada;
        this.partida.setGestorEventos(gestorUI);
        this.jugadoresConfig = pCargada.getJugadores();
 
        // OBLIGATORIO: SIN ESTO LAS COLAS DE ANIMACIÓN ESTÁN VACÍAS Y EL JUEGO SE BLOQUEA
        inicializarColas();
 
        ImageView[] fichas = {P1, P2, P3, P4};
        for (ImageView iv : fichas) {
            if (iv != null) iv.setVisible(false);
        }
 
        for (int i = 0; i < jugadoresConfig.size() && i < fichas.length; i++) {
            Jugador j = jugadoresConfig.get(i);
            ImageView ficha = fichas[i];
            if (ficha != null) {
                asignarImagenAFicha(ficha, obtenerRutaPersonaje(j));
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
 
        // PASO 1: CREAR EL OBJETO PARTIDA Y ASIGNARLE EL GESTOR DE MENSAJES
        partida = new Partida();
        partida.setGestorEventos(gestorUI);
 
        // PASO 2: SI HAY JUGADORES DE PANTALLACONFIG LOS USAMOS; SI NO, CREAMOS UNO POR DEFECTO
        if (jugadoresConfig != null) {
            configurarPartida();
        } else {
            // MODO DE EMERGENCIA: 1 PINGÜINO HUMANO + 1 FOCA CPU POR DEFECTO
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
 
        // PASO 3: ACTIVAR EL MENÚ DE ESCAPE (SE ABRE AL PULSAR LA TECLA ESC)
        configurarEscapeMenu();
    }
 
    // CONFIGURA LA PARTIDA CON LOS JUGADORES QUE VIENEN DE LA PANTALLA DE CONFIGURACIÓN
    private void configurarPartida() {
        partida.getJugadores().clear();
        for (Jugador j : jugadoresConfig) {
            partida.getJugadores().add(j);
        }
 
        // ARRAY CON LAS 4 FICHAS VISUALES DISPONIBLES EN EL FXML
        ImageView[] fichas = {P1, P2, P3, P4};
 
        // PRIMERO OCULTAMOS TODAS LAS FICHAS PARA QUE NO SE VEAN LAS QUE NO SE USAN
        for (ImageView iv : fichas) {
            if (iv != null) iv.setVisible(false);
        }
 
        // ASIGNAMOS IMAGEN, POSICIÓN INICIAL Y VISIBILIDAD A CADA JUGADOR
        for (int i = 0; i < jugadoresConfig.size() && i < fichas.length; i++) {
            Jugador j = jugadoresConfig.get(i);
            ImageView ficha = fichas[i];
            if (ficha != null) {
                asignarImagenAFicha(ficha, obtenerRutaPersonaje(j));
                ficha.setVisible(true);
                actualizarPosicionVisual(j, ficha); // COLOCA LA FICHA EN LA CELDA CORRECTA DESDE EL INICIO
            }
        }

        inicializarColas();
 
        // ACTUALIZAMOS EL CONTADOR DEL INVENTARIO DEL PRIMER PINGÜINO HUMANO
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
    // MENÚ DE OPCIONES: GUARDAR, CARGAR Y SALIR
    // ==========================================
 
    @FXML
    private void handleSaveGame(ActionEvent event) {
        GestorBBDD gestor = new GestorBBDD();
        gestor.iniciarConexionGUI();

        if (gestor.getConexion() == null) {
            gestorUI.registrar("❌ No hay conexión a la BBDD.");
            return;
        }

        // SI YA HAY UN ID ASIGNADO A ESTA PARTIDA, LA SOBRESCRIBIMOS EN LA BASE DE DATOS
        // SI NO TIENE ID AÚN, GENERAMOS UNO NUEVO CON LA SEQUENCE DE ORACLE
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
            // PRIMERA VEZ QUE SE GUARDA: GENERAMOS UN ID AUTOMÁTICO CON LA SEQUENCE DE ORACLE
            idAUsar = gestor.guardarPartidaAuto(partida);
            gestor.cerrarConexion();
            if (idAUsar > 0) {
                this.idPartidaActual = idAUsar;
                gestorUI.registrar("✅ Partida creada con ID " + idAUsar + " (asignado automáticamente).");

                // MOSTRAMOS UN DIALOGO INFORMANDO AL JUGADOR DEL ID ASIGNADO
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
            
            // INTENTO 1: OBTENEMOS LA VENTANA A TRAVÉS DEL TABLERO
            if (tablero != null && tablero.getScene() != null && tablero.getScene().getWindow() != null) {
                stage = (Stage) tablero.getScene().getWindow();
            } 
            // INTENTO 2: OBTENEMOS LA VENTANA A TRAVÉS DEL EVENTO (MENUITEM -> MENUBAR -> STAGE)
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
 
        // SI EL JUGADOR TIENE OBJETOS USABLES, LE PREGUNTAMOS SI QUIERE USARLOS ANTES DE TIRAR
        if (tieneObjetosUsables(actual)) {
            mostrarPopupObjetos(actual);
        } else {
            ejecutarTiradaNormal(actual);
        }
    }
 
    // EJECUTA UNA TIRADA NORMAL DE DADO (VALOR 1-6) Y HACE MOVER AL JUGADOR
    private void ejecutarTiradaNormal(Jugador actual) {
        int tirada = (int)(Math.random() * 6) + 1;

        // SI HAY ANIMACIÓN CONFIGURADA PARA ESTE JUGADOR, LA MOSTRAMOS ANTES DE MOVERLO
        String[] config = obtenerConfigAnimacion(actual, "normal");
        if (config != null) {
            final int tiradaFinal = tirada;
            setUIInteractuable(false);
            mostrarAnimacionTurno(actual, tiradaFinal,
                config[0],                                         // RUTA DE LA IMAGEN DEL DADO
                javafx.scene.paint.Color.web(config[1]),           // COLOR DEL TEXTO DEL RESULTADO
                () -> {
                    moverJugadorYAccion(actual, tiradaFinal, "tirada normal");
                    if (dadoResultText != null) dadoResultText.setText("Has sacado un: " + tiradaFinal);
                    actualizarTextosInventario(actual);
                    avanzarTurno();
                    dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
                });
        } else {
            moverJugadorYAccion(actual, tirada, "tirada normal");
            // MOSTRAMOS EL RESULTADO DEL DADO EN EL TEXTO GRANDE DE LA UI
            if (dadoResultText != null) dadoResultText.setText("Has sacado un: " + tirada);
            actualizarTextosInventario(actual);
            avanzarTurno();
            dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
        }
    }
 
    // DEVUELVE TRUE SI EL JUGADOR TIENE DADOS ESPECIALES O BOLAS DE NIEVE EN SU INVENTARIO
    private boolean tieneObjetosUsables(Jugador j) {
        if (!(j instanceof Pinguino)) return false;
        for (Item item : j.getInventario().getLista()) {
            String n = item.getNombre().toLowerCase();
            if (n.contains("rapido") || n.contains("rápido") ||
                n.contains("lento")  || n.contains("bola")) return true;
        }
        return false;
    }
 
    // MUESTRA UN POPUP ANTES DE TIRAR: PERMITE AL JUGADOR USAR UN OBJETO DE SU INVENTARIO
    // CREA UN BOTÓN POR CADA OBJETO USABLE MÁS UN BOTÓN PARA TIRAR EL DADO NORMAL
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
 
    // CUENTA CUÁNTOS ITEMS DEL INVENTARIO CONTIENEN ALGUNA DE LAS PALABRAS CLAVE DADAS
    private int contarItemPorNombre(Jugador j, String... claves) {
        int n = 0;
        for (Item item : j.getInventario().getLista()) {
            String nombre = item.getNombre().toLowerCase();
            for (String clave : claves) { if (nombre.contains(clave)) { n++; break; } }
        }
        return n;
    }
 
    // ==========================================
    // USO DE OBJETOS DEL INVENTARIO (BOTONES UI)
    // ==========================================
    @FXML
    private void handleRapido(ActionEvent event) {
        usarDadoEspecial("Dado Rápido", 5, 10); // AVANZA ENTRE 5 Y 10 CASILLAS
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

                // SI HAY ANIMACIÓN CONFIGURADA PARA ESTE DADO, LA REPRODUCIMOS ANTES DE MOVER
                String[] config = obtenerConfigAnimacion(actual, nombreDado);
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
 
        // BUSCAMOS AL PINGÜINO HUMANO QUE VA MÁS ADELANTE EN EL TABLERO (OBJETIVO DEL ATAQUE)
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
            encolarAnimacionGuerra(actual, actual.getNombre() + ":" + objetivo.getNombre() + ":" + diff);
            encolarSaltoDirecto(objetivo, objetivo.getPosicion());
        } else if (bolasObj > bolasActual) {
            int diff = bolasObj - bolasActual;
            actual.moverPosicion(Math.max(0, actual.getPosicion() - diff));
            gestorUI.registrar("¡GUERRA DE BOLAS! " + objetivo.getNombre() + " contraataca a " + actual.getNombre()
                + " y gana por " + diff + " bolas. El perdedor retrocede " + diff + " casillas.");
            encolarAnimacionGuerra(objetivo, objetivo.getNombre() + ":" + actual.getNombre() + ":" + diff);
            encolarSaltoDirecto(actual, actual.getPosicion());
        } else {
            gestorUI.registrar("¡EMPATE en la Guerra de Bolas entre " + actual.getNombre()
                + " y " + objetivo.getNombre() + "! Todos pierden sus bolas pero nadie retrocede.");
            encolarAnimacionGuerra(actual, "EMPATE:" + actual.getNombre() + " y " + objetivo.getNombre() + ":0");
        }
 
        // LANZAR BOLAS ES LA ACCIÓN DEL TURNO: NO SE TIRA EL DADO ESTE TURNO
        avanzarTurno();
        dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
    }
 
    // ==========================================
    // LÓGICA INTERNA DE TURNOS Y CASILLAS
    // ==========================================
 

 
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
                    if (p.getPosicion() != nuevaPos) { // LA POSICIÓN FINAL SE PROCESA EN EL CHOQUE
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
            partida.setGanador(j); // GUARDAMOS EL GANADOR EN EL OBJETO PARTIDA
            gestorUI.registrar("¡" + j.getNombre() + " HA LLEGADO A LA META Y GANA LA PARTIDA!");

            // NOTIFICAMOS A ORACLE PARA DISPARAR EL TRIGGER 'INCREMENTAR_WINS'
            notificarFinPartidaBBDD(j);

            // NAVEGAMOS A LA PANTALLA DE VICTORIA DESDE EL HILO DE UI
            javafx.application.Platform.runLater(() -> irAPantallaVictoria(j));
            return;
        }
        
        // EJECUTAMOS LA ACCION DE LA CASILLA EN LA QUE CAYÓ EL JUGADOR (OSO, TRINEO, EVENTO...)
        Casilla casillaActual = partida.getTablero().getCasillas().get(j.getPosicion());
        int posAntesCasilla = j.getPosicion();

        casillaActual.realizarAccion(partida, j);

        if (casillaActual instanceof Evento) {
            String msg = partida.getGestorEventos().getUltimoMensaje();
            ultimoEventoVisual.put(j, msg);
            encolarAnimacionEvento(j);
        }
        if (casillaActual instanceof Oso) {
            if (posAntesCasilla == j.getPosicion()) {
                encolarAnimacionSobornoOso(j);
            } else {
                encolarAnimacionOso(j);
                encolarSaltoDirecto(j, j.getPosicion());
            }
        } else if (posAntesCasilla != j.getPosicion()) {
            if (casillaActual instanceof Agujero) {
                encolarAnimacionAgujero(j, j.getPosicion());
            } else if (casillaActual instanceof Trineo) {
                encolarAnimacionTrineo(j, j.getPosicion());
            }
            // SI HUBO MOVIMIENTO EXTRA POR LA CASILLA, ENCOLAMOS UN SALTO DIRECTO VISUAL
            encolarSaltoDirecto(j, j.getPosicion());
        }
        
        // COMPROBAMOS SI ALGÚN JUGADOR ATERRIZÓ EN LA MISMA CASILLA QUE OTRO
        verificarColisionesLocal(j);
    }
 
    // CARGA LA PANTALLA DE VICTORIA Y LE PASA EL NOMBRE Y COLOR DEL GANADOR
    private void irAPantallaVictoria(Jugador ganador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaVictoria.fxml"));
            Parent root = loader.load();
 
            // PASAMOS LOS DATOS DEL GANADOR AL CONTROLADOR DE LA PANTALLA DE VICTORIA
            PantallaVictoria controlador = loader.getController();
            controlador.setGanador(ganador.getNombre(), ganador.getColor(), ganador instanceof Foca);
 
            Scene scene = new Scene(root);
 
            // OBTENEMOS EL STAGE ACTUAL PARA CAMBIAR DE ESCENA
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
                
                // DETECTAMOS SI ALGÚN JUGADOR CHOCÓ CON LA FOCA
                if (actual instanceof Foca || otro instanceof Foca) {
                    Jugador pinguino = (actual instanceof Pinguino) ? actual : otro;
                    Foca foca = (actual instanceof Foca) ? (Foca) actual : (Foca) otro;
 
                    if (consumirObjeto(pinguino, "Pez")) {
                        // EL PINGÜINO USA UN PEZ: LA FOCA QUEDA BLOQUEADA 2 TURNOS
                        gestorUI.registrar("¡" + pinguino.getNombre() + " le lanza un Pez a la Foca! ¡La Foca queda bloqueada 2 turnos!");
                        foca.aplicarPenalizacion();
                        foca.aplicarPenalizacion(); // SE LLAMA DOS VECES PORQUE SON 2 TURNOS DE PENALIZACIÓN
                    } else {
                        // SIN PEZ: LA FOCA DA UN COLETAZO Y EL PINGÜINO RETROCEDE AL AGUJERO ANTERIOR
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
                        encolarAnimacionGuerra(actual, actual.getNombre() + ":" + otro.getNombre() + ":" + diff);
                        encolarSaltoDirecto(otro, dest);
                        gestorUI.registrar("¡Guerra de bolas! " + actual.getNombre() + " gana a " + otro.getNombre() + " por " + diff + " bolas.");
                    } else if (bolasO > bolasA) {
                        int diff = bolasO - bolasA;
                        int dest = Math.max(0, actual.getPosicion() - diff);
                        actual.moverPosicion(dest);
                        encolarAnimacionGuerra(otro, otro.getNombre() + ":" + actual.getNombre() + ":" + diff);
                        encolarSaltoDirecto(actual, dest);
                        gestorUI.registrar("¡Guerra de bolas! " + otro.getNombre() + " gana a " + actual.getNombre() + " por " + diff + " bolas.");
                    } else {
                        encolarAnimacionGuerra(actual, "EMPATE:" + actual.getNombre() + " y " + otro.getNombre() + ":0");
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
            // REPRODUCIMOS EL SONIDO DE LA FOCA DURANTE 1 SEGUNDO AL INICIO DE SU TURNO
            reproducirSonidoFoca();
            // LÓGICA DEL TURNO DE LA FOCA (CPU)
            if (actual.estaPenalizado()) {
                actual.decrementarPenalizacion();
                gestorUI.registrar("La foca " + actual.getNombre() + " está entretenida comiendo. Pierde su turno.");
                avanzarTurno();
                dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
            } else {
                int tirada = (int)(Math.random() * 6) + 1;
                
                String[] config = obtenerConfigAnimacion(actual, "normal");
                if (config != null) {
                    final int tiradaFinal = tirada;
                    setUIInteractuable(false);
                    mostrarAnimacionTurno(actual, tiradaFinal,
                        config[0],
                        javafx.scene.paint.Color.web(config[1]),
                        () -> {
                            moverJugadorYAccion(actual, tiradaFinal, "tirada CPU");
                            avanzarTurno();
                            dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
                        });
                } else {
                    moverJugadorYAccion(actual, tirada, "tirada CPU");
                    avanzarTurno();
                    dispararAnimadorVisual(() -> procesarTurnosCPU_Async());
                }
            }
        } else {
            // ES EL TURNO DEL JUGADOR HUMANO: ACTUALIZAMOS EL INDICADOR Y HABILITAMOS LOS BOTONES
            actualizarTextosTurno();
            setUIInteractuable(true);
        }
    }

    // REPRODUCE EL SONIDO DE LA FOCA EXACTAMENTE 1 SEGUNDO
    // SI YA HABÍA UN SONIDO REPRODUCIÉNDOSE, LO PARA Y LO REINICIA DESDE EL PRINCIPIO
    private void reproducirSonidoFoca() {
        try {
            if (mediaPlayerFoca != null) {
                mediaPlayerFoca.stop();
                mediaPlayerFoca.dispose();
                mediaPlayerFoca = null;
            }
            var url = getClass().getResource("/resources/sonidoFoca.mp3");
            if (url != null) {
                Media media = new Media(url.toExternalForm());
                mediaPlayerFoca = new MediaPlayer(media);
                mediaPlayerFoca.play();
                javafx.animation.PauseTransition pausa =
                    new javafx.animation.PauseTransition(Duration.seconds(1));
                pausa.setOnFinished(e -> {
                    if (mediaPlayerFoca != null) {
                        mediaPlayerFoca.stop();
                        mediaPlayerFoca.dispose();
                        mediaPlayerFoca = null;
                    }
                });
                pausa.play();
            } else {
                System.err.println("No se encontró sonidoFoca.mp3 en /resources/");
            }
        } catch (Throwable e) {
            System.err.println("⚠️ No se pudo reproducir sonido de foca (posible falta de javafx-media): " + e.getMessage());
        }
    }
 
    // REGISTRA EL LISTENER DE TECLADO: ESCAPE ABRE Y CIERRA EL MENÚ DE OPCIONES
    // EL MENÚ SE IMPLEMENTA COMO UN CONTEXTMENU ANCLADO A LA VENTANA
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
 
        // ADJUNTAMOS EL LISTENER AL TABLERO PARA CAPTURAR LA TECLA ESCAPE EN CUALQUIER MOMENTO
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
        // BORRAMOS LAS IMÁGENES DE CASILLAS DEL TURNO ANTERIOR ANTES DE REDIBUJARLAS
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));
 
        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);
 
            // LA CASILLA 49 ES EL IGLU (META): YA VIENE DIBUJADA EN EL FONDO, NO LA SOBREPONEMOS
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
        
        // ASEGURAMOS QUE LAS FICHAS DE LOS JUGADORES SIEMPRE SE DIBUJEN POR ENCIMA DE LAS CASILLAS
        if (P1 != null) P1.toFront();
        if (P2 != null) P2.toFront();
        if (P3 != null) P3.toFront();
        if (P4 != null) P4.toFront();
    }
 
    // DEVUELVE LA RUTA DEL ARCHIVO DE IMAGEN CORRESPONDIENTE A CADA TIPO DE CASILLA
    private String getRutaCasilla(Casilla c, int index, int total) {
 
        if (c instanceof Agujero)     return "/resources/Casilla_Agujero.png";
        if (c instanceof Oso)         return "/resources/Casilla_Oso.png";
        if (c instanceof Trineo)      return "/resources/Casilla_Trineo.png";
        if (c instanceof Evento)      return "/resources/Casilla_Interrogante.png";
        if (c instanceof CasillaFragil) return "/resources/casilla_fragil.png";
        
        return "/resources/Casilla_Normal.png";
    }
 
    // COLA DE PASOS DE ANIMACIÓN POR JUGADOR: int[]{destinoPos, tipo}
    // TIPO 0 = SALTITO NORMAL, TIPO 1 = SALTO DIRECTO (OSO, FOCA, BOLAS, ETC.)
    private java.util.Map<Jugador, java.util.Queue<int[]>> colasAnimacion = new java.util.HashMap<>();
    private java.util.Map<Jugador, Integer> posVisual = new java.util.HashMap<>();
    private java.util.Map<Jugador, String> ultimoEventoVisual = new java.util.HashMap<>();
    private java.util.Map<Jugador, String> ultimoGuerraVisual = new java.util.HashMap<>();
    private boolean animando = false;

    private void inicializarColas() {
        colasAnimacion.clear();
        for (Jugador j : partida.getJugadores()) {
            colasAnimacion.put(j, new java.util.LinkedList<>());
            posVisual.put(j, j.getPosicion());
        }
    }

    // COLOCA INMEDIATAMENTE LA FICHA VISUAL EN LA CELDA CORRESPONDIENTE A LA POSICIÓN DEL JUGADOR
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

    // ENCOLA CADA CASILLA INTERMEDIA (SALTITO) ENTRE 'DESDE' Y 'HASTA'
    private void encolarCaminoPasoAPaso(Jugador j, int desde, int hasta) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        int step = (desde < hasta) ? 1 : -1;
        for (int pos = desde + step; pos != hasta + step; pos += step) q.add(new int[]{pos, 0});
    }

    // ENCOLA UN SALTO DIRECTO SIN PASAR POR CASILLAS INTERMEDIAS (USADO EN OSO, FOCA, BOLAS)
    private void encolarSaltoDirecto(Jugador j, int hasta) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{hasta, 1});
    }

    // ENCOLA LA ANIMACIÓN DE ATAQUE DEL OSO (TIPO 2: OVERLAY ROJO A PANTALLA COMPLETA)
    private void encolarAnimacionOso(Jugador j) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{j.getPosicion(), 2}); // Destino no importa
    }

    // ENCOLA LA ANIMACIÓN DEL REGALO / EVENTO (TIPO 3: CAJA TEMBLANDO CON OBJETO DENTRO)
    /** Encola la animación de soborno a Oso. Tipo 7. */
    private void encolarAnimacionSobornoOso(Jugador j) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{j.getPosicion(), 7});
    }
    // ENCOLA LA ANIMACIÓN DEL AGUJERO (TIPO 4: JUGADOR SE CAE Y APARECE EN LA CASILLA DESTINO)
    private void encolarAnimacionAgujero(Jugador j, int destino) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{destino, 4}); 
    }

    // ENCOLA LA ANIMACIÓN DEL TRINEO (TIPO 5: JUGADOR SUBE AL TRINEO Y AVANZA DE GOLPE)
    private void encolarAnimacionTrineo(Jugador j, int destino) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        q.add(new int[]{destino, 5}); 
    }

    // ENCOLA LA ANIMACIÓN DE GUERRA DE BOLAS (TIPO 6: OVERLAY CON EL RESULTADO DEL DUELO)
    private void encolarAnimacionGuerra(Jugador j, String data) {
        java.util.Queue<int[]> q = colasAnimacion.get(j);
        if (q == null) return;
        ultimoGuerraVisual.put(j, data);
        q.add(new int[]{j.getPosicion(), 6}); // Destino no importa
    }
 
    private void setUIInteractuable(boolean interactuable) {
        if (dado   != null) dado.setDisable(!interactuable);
        if (rapido != null) rapido.setDisable(!interactuable);
        if (lento  != null) lento.setDisable(!interactuable);
        if (peces  != null) peces.setDisable(!interactuable);
        if (nieve  != null) nieve.setDisable(!interactuable);
    }
 
    // ANIMA CASILLA A CASILLA CON UN SALTITO PARABÓLICO (300ms POR PASO)
    // EL BOTÓN DE TIRAR DADO PERMANECE DESHABILITADO DURANTE TODA LA ANIMACIÓN
    private void dispararAnimadorVisual(Runnable onFinished) {
        if (animando) return;
        animando = true;
        setUIInteractuable(false);
 
        // LISTA PLANA DE TODOS LOS PASOS: int[]{playerIdx, posOrigen, posDestino, animType}
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

    // EJECUTA RECURSIVAMENTE CADA PASO CON LA ANIMACIÓN QUE LE CORRESPONDE
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
        } else if (tipo == 6) {
            mostrarAnimacionGuerra(j, next);
        } else if (tipo == 7) {
            mostrarAnimacionSobornoOso(j, next);
        } else {
            animarConSaltito(fic, j, desde, hasta, next);
        }
    }

    // SALTITO: ARCO PARABÓLICO DE UNA CELDA A LA SIGUIENTE (300ms, 3 KEYFRAMES)
    // EL PERSONAJE SUBE ~25px A MITAD DEL TRAYECTO Y BAJA EN EL DESTINO
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

    // SALTO DIRECTO: EL PERSONAJE SE ENCOGE, SE TELETRANSPORTA A LA CELDA DESTINO Y REAPARECE
    // SE USA PARA OSO, FOCA Y BOLAS DE NIEVE (SIN PASAR CASILLA A CASILLA)
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

    // MUESTRA UNA ANIMACIÓN A PANTALLA COMPLETA DEL ATAQUE DEL OSO
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

    // MUESTRA UNA ANIMACIÓN A PANTALLA COMPLETA DEL SOBORNO AL OSO
    private void mostrarAnimacionSobornoOso(Jugador j, Runnable onFinish) {
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
        var recurso = getClass().getResourceAsStream("/resources/soborno_oso.png");
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

        javafx.scene.text.Text lblMensaje = new javafx.scene.text.Text("¡" + j.getNombre() + " ha sobornado al oso con un pez!\nTe quedas en tu sitio.");
        lblMensaje.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 40));
        lblMensaje.setFill(javafx.scene.paint.Color.web("#44FF44"));
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
    // MUESTRA LA ANIMACIÓN DEL REGALO: PRIMERO TIEMBLA Y LUEGO REVELA EL OBJETO OBTENIDO
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

    // MUESTRA LA ANIMACIÓN A PANTALLA COMPLETA DE LA GUERRA DE BOLAS DE NIEVE
    private void mostrarAnimacionGuerra(Jugador j, Runnable onFinish) {
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

        javafx.scene.image.ImageView guerraView = new javafx.scene.image.ImageView();
        var recurso = getClass().getResourceAsStream("/resources/guerra_bolas.png");
        if (recurso != null) {
            guerraView.setImage(new Image(recurso));
        }
        double IMG_SIZE = 400;
        guerraView.setFitWidth(IMG_SIZE);
        guerraView.setFitHeight(IMG_SIZE);
        guerraView.setPreserveRatio(true);
        guerraView.setOpacity(0);
        guerraView.setScaleX(0.5);
        guerraView.setScaleY(0.5);
        guerraView.setManaged(false);
        guerraView.setLayoutX(W / 2 - IMG_SIZE / 2);
        guerraView.setLayoutY(H / 2 - IMG_SIZE / 2);
        rootPane.getChildren().add(guerraView);

        String data = ultimoGuerraVisual.getOrDefault(j, "::0");
        String[] parts = data.split(":");
        String txtGanador = "";
        String txtPerdedor = "";
        if (parts[0].equals("EMPATE")) {
            txtGanador = "¡Empate!";
            txtPerdedor = "Nadie retrocede";
        } else {
            txtGanador = "Ha Ganado " + parts[0];
            txtPerdedor = parts[1] + " retrocede " + parts[2] + " casilla/s";
        }

        javafx.scene.text.Text lblTitulo = new javafx.scene.text.Text("¡Guerra de bolas de nieve!");
        lblTitulo.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 40));
        lblTitulo.setFill(javafx.scene.paint.Color.WHITE);
        lblTitulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblTitulo.setWrappingWidth(W);
        lblTitulo.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));
        lblTitulo.setOpacity(0);
        lblTitulo.setManaged(false);
        lblTitulo.setLayoutX(0);
        lblTitulo.setLayoutY(H / 2 - IMG_SIZE / 2 - 20);
        rootPane.getChildren().add(lblTitulo);

        javafx.scene.text.Text lblMensaje = new javafx.scene.text.Text(txtGanador + "\n" + txtPerdedor);
        lblMensaje.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 40));
        lblMensaje.setFill(javafx.scene.paint.Color.web("#44AAFF"));
        lblMensaje.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblMensaje.setWrappingWidth(W);
        lblMensaje.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.BLACK));
        lblMensaje.setOpacity(0);
        lblMensaje.setManaged(false);
        lblMensaje.setLayoutX(0);
        lblMensaje.setLayoutY(H / 2 + IMG_SIZE / 2 + 60);
        rootPane.getChildren().add(lblMensaje);

        javafx.animation.FadeTransition ftOver = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOver.setToValue(0.8);

        javafx.animation.FadeTransition ftImg = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), guerraView);
        ftImg.setToValue(1);
        javafx.animation.ScaleTransition stImg = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), guerraView);
        stImg.setToX(1.2);
        stImg.setToY(1.2);

        javafx.animation.FadeTransition ftTitulo = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblTitulo);
        ftTitulo.setToValue(1);
        javafx.animation.FadeTransition ftMensaje = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
        ftMensaje.setToValue(1);

        javafx.animation.ParallelTransition ptIn = new javafx.animation.ParallelTransition(ftOver, ftImg, stImg, ftTitulo, ftMensaje);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(2000));

        javafx.animation.FadeTransition ftOverOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlayPane);
        ftOverOut.setToValue(0);
        javafx.animation.FadeTransition ftImgOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), guerraView);
        ftImgOut.setToValue(0);
        javafx.animation.FadeTransition ftTituloOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblTitulo);
        ftTituloOut.setToValue(0);
        javafx.animation.FadeTransition ftMensajeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), lblMensaje);
        ftMensajeOut.setToValue(0);

        javafx.animation.ParallelTransition ptOut = new javafx.animation.ParallelTransition(ftOverOut, ftImgOut, ftTituloOut, ftMensajeOut);

        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(ptIn, pause, ptOut);
        seq.setOnFinished(e -> {
            rootPane.getChildren().removeAll(overlayPane, guerraView, lblTitulo, lblMensaje);
            if (onFinish != null) onFinish.run();
        });
        seq.play();
    }

    // MUESTRA LA ANIMACIÓN A PANTALLA COMPLETA DEL AGUJERO: EL JUGADOR SE CAE
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

    // MUESTRA LA ANIMACIÓN A PANTALLA COMPLETA DEL TRINEO: EL JUGADOR AVANZA RÁPIDO
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
 
    // OFFSET X EN PIXELES PARA QUE LA FICHA EN CASILLA 49 QUEDE ENCIMA DEL IGLU (~85.5% DEL ANCHO)
    private double computeIglooOffsetX(int colB, double cellW) {
        double iglooSceneX = tablero.getScene().getWidth() * 0.855;
        javafx.geometry.Point2D p = tablero.localToScene((colB + 0.5) * cellW, 0);
        return iglooSceneX - p.getX();
    }
 
    // OFFSET Y EN PIXELES PARA QUE LA FICHA EN CASILLA 49 QUEDE ENCIMA DEL IGLU (~13% DEL ALTO)
    private double computeIglooOffsetY(int filB, double cellH) {
        double iglooSceneY = tablero.getScene().getHeight() * 0.13;
        javafx.geometry.Point2D p = tablero.localToScene(0, (filB + 0.5) * cellH);
        return iglooSceneY - p.getY();
    }
 
    // REDISTRIBUYE LAS FICHAS HORIZONTALMENTE CUANDO HAY VARIOS JUGADORES EN LA MISMA CASILLA
    // SI HAY UN SOLO JUGADOR EN LA CELDA, SOLO RESETEA LA ESCALA SIN TOCAR LA POSICIÓN
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
                    // UN SOLO JUGADOR EN LA CELDA: SÓLO RESETEAMOS ESCALA, NO MOVEMOS
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
 
    // DEVUELVE LA RUTA DEL PNG SEGÚN EL TIPO DE JUGADOR (PINGÜINO O FOCA)
    private String obtenerRutaPersonaje(Jugador j) {
        if (j instanceof Foca) return "/resources/foca.png";
        return obtenerRutaPersonaje(j.getColor());
    }

    // DEVUELVE LA RUTA DEL PNG SEGÚN EL COLOR DEL JUGADOR
    private String obtenerRutaPersonaje(String color) {
        if (color == null) return "/resources/Personaje Amarillo.png";
        switch (color.toLowerCase()) {
            case "gris":
            case "foca":     return "/resources/foca.png";
            case "amarillo": return "/resources/Personaje Amarillo.png";
            case "rojo":     return "/resources/Personaje Rojo.png";
            case "verde":    return "/resources/Personaje Verde.png";
            case "azul":
            default:         return "/resources/Gemini_Generated_Image_y5ki0gy5ki0gy5ki-fotor-bg-remover-202603161643.png";
        }
    }
 
    private void cargarSkins() {
        // EN EL MODO SIN CONFIG (FALLBACK), ASIGNAMOS LAS SKINS POR DEFECTO A P1 Y P2
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

    // DEVUELVE [0]=RUTA IMAGEN DEL DADO  [1]=COLOR HEX DEL TEXTO DE RESULTADO
    // DEVUELVE null SI ESE COLOR O TIPO DE DADO NO TIENE ANIMACIÓN CONFIGURADA
    private String[] obtenerConfigAnimacion(Jugador j, String tipoDado) {
        if (tipoDado != null) {
            if (tipoDado.equals("Dado Rápido")) return new String[]{"/resources/dado_rapido.png", "#FF4500"};
            if (tipoDado.equals("Dado Lento"))  return new String[]{"/resources/dado_lento.png",  "#8B4513"};
        }
        
        if (j instanceof Foca) return new String[]{"/resources/dado_foca.png", "#A9A9A9"}; // DADO DE LA FOCA (CPU)

        String color = j.getColor();
        if (color == null) return null;
        switch (color.toLowerCase()) {
            case "amarillo": return new String[]{"/resources/dado_amarillo.png", "#FFD700"};
            case "azul":     return new String[]{"/resources/dado_azul.png",     "#00BFFF"};
            case "rojo":     return new String[]{"/resources/dado_rojo.png",     "#FF4444"};
            case "verde":    return new String[]{"/resources/dado_verde.png",    "#00FF7F"};
            case "gris":     return new String[]{"/resources/dado_foca.png",     "#A9A9A9"};
            default:         return null;
        }
    }

    // MUESTRA LA SECUENCIA COMPLETA DE ANIMACIÓN DE TURNO: OVERLAY + DADO GIRANDO + RESULTADO
    // ES REUTILIZABLE PARA CUALQUIER COLOR DE JUGADOR, RECIBE LA IMAGEN Y COLOR POR PARÁMETRO
    //
    // @param actual        JUGADOR CUYO TURNO SE ANIMA
    // @param resultadoDado VALOR DEL DADO (1-6) YA CALCULADO
    // @param rutaDado      RUTA DE LA IMAGEN DEL DADO (ej. /resources/dado_azul.png)
    // @param colorTexto    COLOR DEL TEXTO DE RESULTADO (API JAVA, INMUNE A CSS)
    // @param onFinish      CALLBACK QUE SE EJECUTA AL TERMINAR: AQUÍ VA EL MOVIMIENTO DEL PERSONAJE
    private void mostrarAnimacionTurno(Jugador actual, int resultadoDado,
                                       String rutaDado,
                                       javafx.scene.paint.Color colorTexto,
                                       Runnable onFinish) {
        // OBTENEMOS LAS DIMENSIONES DE LA ESCENA PARA CENTRAR LOS ELEMENTOS
        javafx.scene.Scene escena = tablero.getScene();
        if (escena == null) {
            // SI LA ESCENA AÚN NO ESTÁ DISPONIBLE, EJECUTAMOS EL CALLBACK DIRECTAMENTE
            if (onFinish != null) onFinish.run();
            return;
        }
        double W = escena.getWidth();
        double H = escena.getHeight();
        javafx.scene.layout.Pane rootPane = (javafx.scene.layout.Pane) escena.getRoot();

    
        final double DADO_SIZE   = 380;
        final double TEXTO_H     = 50;       // ALTURA ESTIMADA DEL LABEL "TURNO DE X"
        final double GAP         = 20;       // ESPACIO ENTRE EL TEXTO Y EL DADO
        final double GRUPO_H     = TEXTO_H + GAP + DADO_SIZE;  // ALTO TOTAL DEL BLOQUE
        final double GRUPO_TOP   = H / 2 - GRUPO_H / 2;       // Y DESDE DONDE EMPIEZA EL BLOQUE

        // OVERLAY 1: FONDO NEGRO SEMITRANSPARENTE (EL DADO Y LOS TEXTOS NO SON HIJOS SUYOS
        // PARA EVITAR QUE HEREDEN SU OPACIDAD Y SE VEAN TRANSPARENTES)
        javafx.scene.layout.Pane overlayPane = new javafx.scene.layout.Pane();
        overlayPane.setStyle("-fx-background-color: black;");
        overlayPane.setManaged(false);
        overlayPane.setLayoutX(0);
        overlayPane.setLayoutY(0);
        overlayPane.resize(W, H);
        overlayPane.setOpacity(0);
        overlayPane.setMouseTransparent(false); // BLOQUEA LOS CLICS DEL USUARIO DURANTE LA ANIMACIÓN
        rootPane.getChildren().add(overlayPane);

        // TEXTO 2: "TURNO DE X" - USAMOS setFill() (API JAVA) PARA QUE EL CSS NO LO SOBREESCRIBA
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

        // IMAGEN 3: DADO DEL COLOR DEL JUGADOR (LA RUTA SE RECIBE COMO PARÁMETRO)
        javafx.scene.image.ImageView dadoView = new javafx.scene.image.ImageView();
        var recurso = getClass().getResourceAsStream(rutaDado);  // IMAGEN DEL DADO SEGÚN EL COLOR DEL JUGADOR
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
        dadoView.setLayoutX(W / 2 - DADO_SIZE / 2);          // CENTRADO HORIZONTALMENTE EN PANTALLA
        dadoView.setLayoutY(GRUPO_TOP + TEXTO_H + GAP);       // JUSTO DEBAJO DEL TEXTO DE TURNO
        rootPane.getChildren().add(dadoView);

        // TEXTO 4: RESULTADO DEL DADO - setFill() GARANTIZA EL COLOR DEL JUGADOR SIN CSS
        javafx.scene.text.Text lblResultado = new javafx.scene.text.Text(
            actual.getNombre() + " ha sacado un " + resultadoDado);
        lblResultado.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 52));
        lblResultado.setFill(colorTexto);   // COLOR DEL JUGADOR, API JAVA DIRECTA
        lblResultado.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblResultado.setWrappingWidth(W);
        lblResultado.setEffect(new javafx.scene.effect.DropShadow(18, javafx.scene.paint.Color.BLACK));
        lblResultado.setOpacity(0);
        lblResultado.setManaged(false);
        lblResultado.setLayoutX(0);
        lblResultado.setLayoutY(H / 2 + 26);
        rootPane.getChildren().add(lblResultado); // ÚLTIMO HIJO = Z-INDEX MÁS ALTO (SIEMPRE AL FRENTE)

        // ==============================================
        // CONSTRUCCIÓN DE LA SEQUENTIAL TRANSITION
        // ==============================================

        // PASO 1 (300ms): OVERLAY SE OSCURECE + DADO CRECE + TEXTO TURNO APARECEN JUNTOS
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

        // OVERLAY + DADO (ZOOM + FADE) + TEXTO TURNO: TODOS A LA VEZ EN PARALELO
        javafx.animation.ParallelTransition paso1 = new javafx.animation.ParallelTransition(
            fadeInOverlay, fadeInDado, zoomDado, fadeInTurno);

        // PASO 2 (2000ms): EL DADO GIRA 3 VUELTAS COMPLETAS MIENTRAS SE VE "TURNO DE X"
        javafx.animation.RotateTransition girarDado = new javafx.animation.RotateTransition(
            javafx.util.Duration.millis(2000), dadoView);
        girarDado.setFromAngle(0);
        girarDado.setToAngle(360 * 3);
        girarDado.setInterpolator(javafx.animation.Interpolator.LINEAR);

        // PASO 3 (400ms): EL DADO Y EL TEXTO DE TURNO DESAPARECEN JUNTOS CON FADE OUT
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

        // PASO 4 (300ms): EL MENSAJE CON EL RESULTADO NUMÉRICO APARECE EN EL CENTRO
        javafx.animation.FadeTransition fadeInLabel = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), lblResultado);
        fadeInLabel.setFromValue(0);
        fadeInLabel.setToValue(1);

        // PASO 5 (1500ms): PAUSA PARA QUE EL JUGADOR PUEDA LEER EL RESULTADO
        javafx.animation.PauseTransition pausaResultado =
            new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));

        // PASO 6 (400ms): EL RESULTADO Y EL OVERLAY DESAPARECEN JUNTOS
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

        // SECUENCIA COMPLETA ENCADENADA EN ORDEN
        javafx.animation.SequentialTransition secuenciaCompleta =
            new javafx.animation.SequentialTransition(
                paso1,           // OVERLAY + DADO + "TURNO DE X" APARECEN
                girarDado,       // DADO GIRA 3 VUELTAS
                paso3,           // DADO + "TURNO DE X" DESAPARECEN
                fadeInLabel,     // RESULTADO APARECE EN EL CENTRO
                pausaResultado,  // PAUSA 1.5 SEGUNDOS
                paso6            // RESULTADO + OVERLAY DESAPARECEN
            );

        secuenciaCompleta.setOnFinished(e -> {
            // ELIMINAMOS TODOS LOS NODOS DEL OVERLAY DEL GRAFO DE ESCENA
            rootPane.getChildren().removeAll(overlayPane, lblTurno, dadoView, lblResultado);
            // EJECUTAMOS EL CALLBACK: AQUÍ ES DONDE SE MUEVE EL PERSONAJE
            if (onFinish != null) onFinish.run();
        });

        secuenciaCompleta.play();
    }

    // NOTIFICA A ORACLE QUE LA PARTIDA HA TERMINADO Y ACTUALIZA LAS ESTADÍSTICAS
    // PASO 1: SI NO TENÍA ID, SE GENERA UNO AUTOMÁTICO
    // PASO 2: GUARDA EL ESTADO FINAL CON EL GANADOR EN LA META
    // PASO 3: DISPARA EL TRIGGER 'INCREMENTAR_WINS' EN ORACLE
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
            
            // PASO 4: Construir lista de TODOS los participantes (no solo el ganador)
            java.util.List<String> participantes = new java.util.ArrayList<>();
            if (partida != null && partida.getJugadores() != null) {
                for (Jugador jug : partida.getJugadores()) {
                    // SOLO AÑADIMOS PINGÜINOS (LAS FOCAS/CPU NO SON USUARIOS REALES)
                    if (jug instanceof Pinguino) {
                        participantes.add(jug.getNombre());
                    }
                }
            }

            // PASO 5: ASIGNAR GANADOR Y ACTUALIZAR NUM_PARTIDAS A TODOS
            gestor.finalizarPartida(idPartidaActual, idGanador, participantes);
            if (idGanador != -1) {
                gestorUI.registrar("✅ Estadísticas actualizadas para " + participantes.size() 
                                 + " jugadores. Ganador: " + ganador.getNombre());
            } else {
                gestorUI.registrar("✅ Estadísticas actualizadas para " + participantes.size() 
                                 + " jugadores. ¡La IA ha ganado!");
            }

            gestor.cerrarConexion();
        } catch (Exception e) {
            gestorUI.registrar("❌ Error al actualizar estadísticas: " + e.getMessage());
        }
    }
}

