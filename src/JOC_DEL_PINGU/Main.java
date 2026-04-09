package JOC_DEL_PINGU;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    // ==================== MÈTODE START ====================
    
   
    @Override
    public void start(Stage primaryStage) {
        try {
            // Carreguem el disseny del menú des del fitxer FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaMenu.fxml"));
            Parent root = loader.load();
            
            // Creem l'escena amb el contingut carregat
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch(Exception ignored){}
            
            // Configurem l'escenari principal
            primaryStage.setTitle("El Joc del Pingu");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true); // Permitir redimensionar y full screen
            primaryStage.setMaximized(true); // Empezar maximizado
            primaryStage.show();
            
            System.out.println("✅ Aplicació iniciada correctament!");
            
        } catch (Exception e) {
            System.out.println("❌ Error al carregar la finestra:");
            System.out.println("   Verifica que el fitxer PantallaMenu.fxml existeixi.");
            e.printStackTrace();
        }
    }
    
    // ==================== MÈTODE MAIN ====================
    
   
    public static void main(String[] args) {
        launch(args);
    }
}