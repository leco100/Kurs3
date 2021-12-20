package com.app.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


import com.app.JavaFxApplication;
import com.app.model.Users;
import com.app.service.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * контроллер авторизации
 */
@Component
@FxmlView("auth.fxml")
public class AuthController implements Initializable {

    @Autowired
    UserService service;

    @FXML
    TextField fLogin;
    @FXML
    PasswordField fPassword;


    /**
     * отвечает за авторизацию пользователя
     * @throws IOException
     */

    @FXML
    private void login() throws IOException {
        String login = fLogin.getText();
        String password = fPassword.getText();
        if (login.isEmpty() || password.isEmpty()){
            showAlert("Заполните оба поля!");
            return;
        }
        try {
            // запросим на  сервере проверить пользователя
            Users user = service.authUser(login,password);
            // если все ок и небыло исключение то запустим основное окно
            Stage primStage = (Stage) fLogin.getScene().getWindow();
            primStage.setTitle(user.getFirstName()+" "+user.getLastName());
            JavaFxApplication.setRoot("main");

        }catch (Exception e){
             //e.printStackTrace();
             showAlert(e.getMessage());
        }


    }

    // если пользователь нажал кнопку регистрации
    @FXML
    private void register() throws IOException {
        // если нажал кнопку регистрации - вызовем форму регистрации
        JavaFxApplication.setRoot("register");
    }

    // выводит сообщение об ошибке
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("");
        alert.setHeaderText("Внимание!");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // если нажал кнопку отмена - то завершим приложение
    @FXML
    private void cancel() throws IOException {
        Platform.exit();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
