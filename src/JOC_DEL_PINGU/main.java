package JOC_DEL_PINGU;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Buscamos el archivo del diseño del menú
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PantallaMenu.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("El Joc del Pingu - Inicio");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            System.out.println("Error: No encuentro el archivo PantallaMenu.fxml tt");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}