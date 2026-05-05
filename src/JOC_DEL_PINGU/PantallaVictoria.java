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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class PantallaVictoria {

    @FXML private ImageView imgVictoria;
    @FXML private Label     lblGanador;
    @FXML private Button    btnNuevaPartida;
    @FXML private Button    btnGuardar;
    @FXML private Button    btnMenu;
    @FXML private Button    btnSalir;

    // Datos pasados desde PantallaPartida
    private String nombreGanador;
    private String colorGanador;

    // Sonido de victoria
    private MediaPlayer mediaPlayer;

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
        reproducirSonidoVictoria();
    }

    /** Reproduce el sonido de victoria de Clash Royale. */
    private void reproducirSonidoVictoria() {
        try {
            var url = getClass().getResource("/resources/sonido de victoria clash royale.mp3");
            if (url != null) {
                Media media = new Media(url.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
            } else {
                System.err.println("No se encontró el sonido de victoria.");
            }
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido de victoria: " + e.getMessage());
        }
    }

    /** Detiene el sonido si está reproduciéndose. */
    private void detenerSonido() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
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
        detenerSonido();
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
        // Placeholder: en un futuro conectar con GestorBBDD
        System.out.println("Guardar partida finalizada... (no implementado)");
        if (lblGanador != null) {
            lblGanador.setText(lblGanador.getText() + "  ✔ Guardado");
        }
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        detenerSonido();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaPrincipal.fxml"));
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
        detenerSonido();
        System.out.println("Saliendo del juego desde pantalla de victoria. ¡Hasta luego!");
        javafx.application.Platform.exit();
    }
}
