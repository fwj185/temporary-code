package org.example.notepad1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class NotepadApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NotepadApplication.class.getResource("/org/example/notepad1/notepad.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        
        // 设置应用图标
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/notepad.png")));
        } catch (Exception e) {
            // 图标加载失败时忽略
        }
        
        stage.setTitle("记事本");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}