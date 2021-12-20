package com.app.controller;

import java.io.IOException;
import java.rmi.RemoteException;


import com.app.JavaFxApplication;
import com.app.model.Role;
import com.app.model.Users;
import com.app.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * контроллер овечает за регистрацию пользователей
 */
@Component
@FxmlView("register.fxml")
public class RegisterController{

    @Autowired
    UserService service;

    @FXML
    TextField fLogin;
    @FXML
    TextField fPassword;

    @FXML
    TextField fFirstName;
    @FXML
    TextField fLastName;
    @FXML
    ComboBox chRole;


    // инициализация
    @FXML
    public void initialize() {
        // создадим список ролей
        ObservableList<String> roles = FXCollections.observableArrayList();
        roles.addAll("Администратор");
        roles.addAll("Пользователь");
        chRole.setItems(roles); chRole.getSelectionModel().select(1);

    }

    @FXML
    private void register() throws IOException {

        // считаем поля и проверим пусты ли
        String firstName = fFirstName.getText();
        if (firstName.isEmpty()){
            showAlert("Имя не может быть пустым");
            return;
        }

        String lastName = fLastName.getText();
        if (lastName.isEmpty()){
            showAlert("Фамилия не может быть пустой");
            return;
        }
        String login = fLogin.getText();
        if (login.isEmpty()){
            showAlert("Логин не может быть пустым");
            return;
        }
        String password = fPassword.getText();
        if (password.isEmpty()){
            showAlert("Пароль не может быть пустым");
            return;
        }

        String strRole = (String) chRole.getSelectionModel().getSelectedItem();
        // если все ок
        try {
            Role role = new Role("ROLE_USER");
            if (strRole.equalsIgnoreCase("Администратор")) role = new Role("ROLE_ADMIN");
            // сохраним пользователя в базе
            Users user =Users.builder().firstName(firstName).lastName(lastName)
            .login(login).password(password).role(role).build();
            user = service.createUser(user);
            // переключимся на вход
            JavaFxApplication.setRoot("auth");
            // выведем сообщение
            showAlert("Теперь Вы можете войти под своим логином");

        }catch (Exception e){
            e.printStackTrace();
            // сообщение об ошибке
            showAlert(e.getMessage());
        }
    }

    // если нажали отмена - вернемся на форму входа
    @FXML
    private void cancel() throws IOException {
        JavaFxApplication.setRoot("auth");
    }


    // выводит сообщение с типом INFORMATION
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Внимание");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
