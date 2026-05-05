package JOC_DEL_PINGU;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
public class PantallaVictoria {

    @FXML private ImageView imgVictoria;
    @FXML private Label     lblGanador;
    @FXML private Button    btnNuevaPartida;
    @FXML private Button    btnGuardar;
    @FXML private Button    btnMenu;
    @FXML private Button    btnSalir;
    @FXML private javafx.scene.layout.VBox overlayBottom;

    // Datos pasados desde PantallaPartida
    private String nombreGanador;
    private String colorGanador;

    // Label dinámico con las estadísticas obtenidas de Oracle
    private Label lblEstadisticas;

    @FXML
    private void initialize() {
        // Enlazar fitWidth/Height al tamaño de la escena para llenar siempre la pantalla
        if (imgVictoria != null) {
            imgVictoria.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    imgVictoria.fitWidthProperty().bind(newScene.widthProperty());
                    imgVictoria.fitHeightProperty().bind(newScene.heightProperty());
                    // El overlay también se extiende al ancho completo
                    if (overlayBottom != null) {
                        overlayBottom.prefWidthProperty().bind(newScene.widthProperty());
                    }
                }
            });
        }
    }

    // ==========================================
    // INICIALIZACIÓN (llamada por PantallaPartida antes de mostrar)
    // ==========================================

    /**
     * Recibe el nombre y color del jugador ganador.
     * Debe llamarse DESPUÉS de que el FXMLLoader haya procesado el FXML.
     */
    public void setGanador(String nombre, String color) {
        this.nombreGanador = nombre;
        this.colorGanador  = color;
        aplicarDatosGanador();
        cargarEstadisticasBBDD(); // ⭐ NUEVO: cargar stats desde Oracle
    }

    private void aplicarDatosGanador() {
        // Etiqueta con el nombre
        if (lblGanador != null) {
            lblGanador.setText("🏆  ¡" + nombreGanador + " GANA LA PARTIDA!");
        }

        // Imagen de victoria según el color
        String rutaImg = obtenerRutaVictoria(colorGanador);
        if (imgVictoria != null && rutaImg != null) {
            try {
                var resource = getClass().getResourceAsStream(rutaImg);
                if (resource != null) {
                    imgVictoria.setImage(new Image(resource));
                } else {
                    System.err.println("No se encontró la imagen de victoria: " + rutaImg);
                }
            } catch (Exception e) {
                System.err.println("Error cargando imagen de victoria: " + e.getMessage());
            }
        }
    }

    /** Mapea color del jugador → ruta del recurso de imagen de victoria. */
    private String obtenerRutaVictoria(String color) {
        if (color == null) return "/resources/Victoria Azul.png";
        switch (color.toLowerCase()) {
            case "rojo":     return "/resources/Rojo Victoria.png";
            case "verde":    return "/resources/Verde Victoria.png";
            case "amarillo": return "/resources/Victoria Amarillo.png";
            case "azul":
            default:         return "/resources/Victoria Azul.png";
        }
    }

    // ==========================================
    // BOTONES
    // ==========================================

    @FXML
    private void handleNuevaPartida(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaConfig.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Nueva Partida");
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        // La partida ya se ha guardado y finalizado automáticamente desde
        // PantallaPartida (notificarFinPartidaBBDD), así que aquí solo confirmamos.
        System.out.println("La partida finalizada ya se ha registrado en BBDD.");
        if (lblGanador != null) {
            lblGanador.setText(lblGanador.getText() + "  ✔ Guardado");
        }
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Partida guardada");
        alert.setHeaderText("Partida registrada en BBDD");
        alert.setContentText("Tu victoria ha sido registrada y las estadísticas se han actualizado.");
        alert.showAndWait();
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaPrincipal.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Menú Principal");
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSalir(ActionEvent event) {
        System.out.println("Saliendo del juego desde pantalla de victoria. ¡Hasta luego!");
        javafx.application.Platform.exit();
    }

    /**
     * Carga las estadísticas desde Oracle usando funciones PL/SQL:
     *  - max_wins() → récord global de victorias
     *  - media_de_wins() → media global
     *  - menos_wins_porcentaje() → % de jugadores con menos victorias
     *  - obtenerIdJugador() → para buscar las partidas ganadas del jugador actual
     *
     * Las estadísticas se muestran en un VBox bonito sobre el botón.
     */
    private void cargarEstadisticasBBDD() {
        GestorBBDD gestor = new GestorBBDD();
        try {
            gestor.iniciarConexionGUI();
            if (gestor.getConexion() == null) {
                System.err.println("⚠️ No se pudieron cargar las estadísticas (sin conexión).");
                return;
            }

            // Llamadas a las funciones PL/SQL
            int recordGlobal = gestor.obtenerRecord();
            double mediaGlobal = gestor.obtenerMediaWins();
            int idJugador = gestor.obtenerIdJugador(nombreGanador);

            // Obtener partidas ganadas del jugador llamando a la function
            // PARTIDAS_GANADAS_JUGADOR de Oracle (no SELECT directo)
            final int misVictorias = (idJugador != -1) ? gestor.obtenerVictoriasJugador(idJugador) : 0;

            // % de jugadores con menos victorias que el ganador
            final double porcentaje = gestor.obtenerPorcentajeMenosWins(misVictorias);

            // Cerrar conexión
            gestor.cerrarConexion();

            // Mostrar las estadísticas en la GUI
            javafx.application.Platform.runLater(() ->
                mostrarEstadisticasEnPantalla(recordGlobal, mediaGlobal, porcentaje, misVictorias)
            );

        } catch (Exception e) {
            System.err.println("❌ Error cargando estadísticas: " + e.getMessage());
            try { gestor.cerrarConexion(); } catch (Exception ignored) {}
        }
    }

    /**
     * Crea un VBox con las estadísticas y lo añade al overlay inferior
     * (encima de los botones).
     */
    private void mostrarEstadisticasEnPantalla(int record, double media, double porcentaje, int misVictorias) {
        if (overlayBottom == null) return;

        // Crear el contenedor de estadísticas
        VBox boxStats = new VBox(8);
        boxStats.setAlignment(Pos.CENTER);
        boxStats.setPadding(new Insets(15));
        boxStats.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65); " +
                          "-fx-background-radius: 15; " +
                          "-fx-border-color: gold; " +
                          "-fx-border-width: 2; " +
                          "-fx-border-radius: 15;");
        boxStats.setMaxWidth(500);

        // Título
        Label lblTitulo = new Label("📊 ESTADÍSTICAS GLOBALES");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblTitulo.setStyle("-fx-text-fill: gold;");

        // Récord global
        Label lblRecord = new Label("🥇 Récord global: " + record + " partidas ganadas");
        lblRecord.setFont(Font.font("Arial", 14));
        lblRecord.setStyle("-fx-text-fill: white;");

        // Media global
        Label lblMedia = new Label("📈 Media global: " + media + " victorias por jugador");
        lblMedia.setFont(Font.font("Arial", 14));
        lblMedia.setStyle("-fx-text-fill: white;");

        // Mis victorias
        Label lblMis = new Label("⭐ Tus victorias totales: " + misVictorias);
        lblMis.setFont(Font.font("Arial", 14));
        lblMis.setStyle("-fx-text-fill: white;");

        // Porcentaje
        Label lblPorcentaje = new Label("🎯 Has ganado más partidas que el "
                                        + porcentaje + "% de los jugadores");
        lblPorcentaje.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblPorcentaje.setStyle("-fx-text-fill: #00ff88;");
        lblPorcentaje.setWrapText(true);

        boxStats.getChildren().addAll(lblTitulo, lblRecord, lblMedia, lblMis, lblPorcentaje);

        // Insertar el box ANTES de los botones (al principio del overlay)
        overlayBottom.getChildren().add(0, boxStats);
    }
}
