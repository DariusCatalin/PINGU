package JOC_DEL_PINGU; 

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Buscamos tu diseño del menú en el src
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaMenu.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("El Joc del Pingu");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            System.out.println("No he podido cargar la ventana:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}