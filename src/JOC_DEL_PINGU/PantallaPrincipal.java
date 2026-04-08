package JOC_DEL_PINGU;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class PantallaPrincipal {

    @FXML private Button btnNuevaPartida;
    @FXML private Button btnCargarPartida;
    @FXML private Button btnSalir;

    @FXML
    private void handleNuevaPartida(ActionEvent event) {
        try {
            System.out.println("Iniciando nueva partida...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaJuego.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Partida");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCargarPartida(ActionEvent event) {
        System.out.println("Cargar partida pulsado. Aquí iría el selector de partidas de la BBDD.");
        // TODO: Mostrar lista de partidas guardadas
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        try {
            System.out.println("Cerrando sesión...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaMenu.fxml")); // Asumiendo este nombre para el login
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Login");
            stage.show();
        } catch (Exception e) {
            // Si el nombre es diferente, intentamos con el recurso correcto.
            // A veces el login se llama de otra forma en FXML.
            System.out.println("Error volviendo al login: " + e.getMessage());
        }
    }
}
