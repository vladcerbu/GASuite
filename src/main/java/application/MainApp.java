package application;

import controller.ConfirmBox;
import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Stage primaryStage;
    public static Scene primaryScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApp.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scene.fxml"));
        Parent root = loader.load();
        MainController mainCtrl = loader.getController();
        mainCtrl.init(primaryStage);
        primaryScene = new Scene(root);
        primaryStage.setTitle("GA-Suite");
        primaryStage.setScene(primaryScene);
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            if(ConfirmBox.display("Exiting application","Are you sure you want to close the app?"))
                primaryStage.close();
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
