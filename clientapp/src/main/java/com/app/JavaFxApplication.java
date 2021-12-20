package com.app;

import com.app.controller.MainController;
import com.app.controller.AuthController;
import com.app.controller.RegisterController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import net.rgielen.fxweaver.core.FxWeaver;



import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;


import java.io.IOException;

/**
 *  класс программы, Spring + JavaFX
 */
public class JavaFxApplication extends Application {
    protected static Scene scene;
    protected static Stage stage;


    private static ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        try {
        String[] args = getParameters().getRaw().toArray(new String[0]);


            this.applicationContext = new SpringApplicationBuilder()
                    .sources(ClientAppApplication.class)
                    .run(args);

    }catch (Exception e){
        System.out.println("Файл конфигурации не найден или поврежден! Завершение работы!");
        System.exit(1);
    }

    }

    // создадим при старте окно входа в систему
    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = loadFXML("auth");
        scene = new Scene(root, 400, 200);
        stage = primaryStage;
        primaryStage.setMinHeight(200);
        primaryStage.setMinWidth(400);
        primaryStage.setMaxHeight(800);
        primaryStage.setMaxWidth(1400);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("ИС Аэропорт");
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        primaryStage.show();


    }


    // уставливает, какой контроллер будет "главным" в данный момент и какую
    // форму загрузим
    public static void setRoot(String fxml) throws IOException {

        scene.setRoot(loadFXML(fxml));

        // если авторизации установим рзамер 400*220
        if (fxml.equals("auth")) {
            stage.setHeight(220);stage.setWidth(400);
            stage.setResizable(false);

        }else
            // если регистрации 400*350
        if (fxml.equals("register"))
        {

            stage.setHeight(350);stage.setWidth(400);
            stage.setResizable(false);
        }
        else {

            // если основная программа  1000*800
            stage.setHeight(800);stage.setWidth(1000);
            stage.setMaxHeight(1080);stage.setMaxWidth(1920);
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
            stage.setResizable(true);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Platform.exit();
                    System.exit(0);
                }
            });
        }
    }

    // "загружает" fxml и контроллер
    private static Parent loadFXML(String fxml)  {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        if (fxml.equalsIgnoreCase("auth"))
        return fxWeaver.loadView(AuthController.class);
        if (fxml.equalsIgnoreCase("register"))
            return fxWeaver.loadView(RegisterController.class);
        return fxWeaver.loadView(MainController.class);
    }

    @Override
    public void stop() {
        this.applicationContext.close();
        Platform.exit();
    }




}
