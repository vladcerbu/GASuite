package controller;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmBox {
    static boolean answer = false;

    public static Boolean display(String title, String content) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setWidth(300);
        window.setHeight(250);

        Label label = new Label();
        label.setText(content);

        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        yesButton.setOnAction(e-> {
            answer = true;
            window.close();
        });
        noButton.setOnAction(e-> {
            answer = false;
            window.close();
        });

        VBox layout1 = new VBox(10);
        HBox layout2 = new HBox(10);
        layout2.getChildren().addAll(yesButton,noButton);
        layout1.getChildren().addAll(label,layout2);
        layout1.setAlignment(Pos.CENTER);
        layout2.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout1);
        window.setScene(scene);
        window.showAndWait();

        return answer;
    }
}
