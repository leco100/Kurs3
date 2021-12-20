package com.app.controller;

import com.app.config.ApiConfig;
import com.app.exception.NotFoundException;
import com.app.model.Country;
import com.app.service.CountryService;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

@Component
@FxmlView("country.fxml")
public class CountryController implements Initializable {

    @Autowired
    ApiConfig apiConfig;

    @Autowired
    CountryService service;

    boolean isAdmin = false;
    ObservableList<Country> countries = FXCollections.observableArrayList();

    @FXML
    TextField  tFilter;

    // фильтрованные данные
    FilteredList<Country> filteredData;

    @FXML
    TableView<Country> tabCountry;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
         isAdmin = apiConfig.getUser().getRole().getName().equals("ROLE_ADMIN");
         initTab();
         loadCountries();
    }
    private void initTab(){
        // cоздадим колонки согласно типам данных
        TableColumn<Country, String> codeColumn = new TableColumn<Country, String>("Код страны");
        codeColumn.setCellValueFactory(new PropertyValueFactory<Country, String>("code"));
        codeColumn.setPrefWidth(100);
        tabCountry.getColumns().add(codeColumn);

        TableColumn<Country, String> nameColumn = new TableColumn<Country, String>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<Country, String>("name"));
        nameColumn.setPrefWidth(150);
        tabCountry.getColumns().add(nameColumn);

        TableColumn<Country, String> nameEngColumn = new TableColumn<Country, String>("Название ENG");
        nameEngColumn.setCellValueFactory(new PropertyValueFactory<Country, String>("nameEng"));
        nameEngColumn.setPrefWidth(150);
        tabCountry.getColumns().add(nameEngColumn);


        // если зашли под админом - то покажем кто последний правил
        if (isAdmin) {
            TableColumn<Country, Boolean> visibleColumn = new TableColumn<Country, Boolean>("Видимый");
            //visibleColumn.setCellValueFactory(new PropertyValueFactory<Country, Boolean>("visible"));
            visibleColumn.setCellValueFactory(c->{
                BooleanProperty active = new SimpleBooleanProperty(c.getValue().isVisible());
                return active;
            });
            visibleColumn.setCellFactory(column -> new CheckBoxTableCell<>());
            tabCountry.getColumns().add(visibleColumn);
        }


        tabCountry.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // создадим контекстное меню и добавим обработчки
        ContextMenu contextMenu = new ContextMenu();
        if (isAdmin){
            MenuItem itemAdd = new MenuItem("Добавить");
            itemAdd.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Optional<Country> result = countryDialog(null).showAndWait();
                    if (result.isPresent()){
                        Country country = result.get();
                        try {
                            country= service.save(country);
                            countries.add(country);
                            tabCountry.getSelectionModel().select(country);
                        } catch (Exception e) {
                            String msg = e.getMessage();
                            msg.replace("500 Internal Server Error: ","").trim();
                            showAlert(e.getMessage());
                        }
                    }
                }
            });
            contextMenu.getItems().add(itemAdd);

            MenuItem itemEdit = new MenuItem("Редактировать");
            itemEdit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Country selected = tabCountry.getSelectionModel().getSelectedItem();
                    System.out.println(selected.isVisible());
                    if (selected==null) return;
                    Optional<Country> result = countryDialog(selected).showAndWait();
                    if (result.isPresent()){
                        Country country = result.get();
                        try {
                            country= service.save(country);
                            int indx = countries.indexOf(selected);
                            countries.remove(indx);
                            countries.add(indx,country);
                            tabCountry.getSelectionModel().select(country);
                        } catch (Exception e) {
                            String msg = e.getMessage();
                            msg.replace("400 Bad Request: ","").trim();
                            showAlert(e.getMessage());
                        }
                    }

                }
            });
            contextMenu.getItems().add(itemEdit);


            MenuItem itemDel = new MenuItem("Удалить");
            itemDel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Country selected = tabCountry.getSelectionModel().getSelectedItem();
                    if (selected==null) return;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Удаление записи");
                    alert.setHeaderText("Удаление "+selected.getName());
                    alert.setContentText("Удалить эту страну?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        try {
                            int indx = countries.indexOf(selected);
                            service.delete(selected.getCode());
                            try {
                                service.findByCode(selected.getCode());
                            }catch (NotFoundException e) {
                                    countries.remove(indx);
                            }catch (Exception e){
                                showAlert(e.getMessage());
                            }

                        } catch (Exception e) {
                            String msg = e.getMessage();
                            msg.replace("500 Internal Server Error: ","").trim();
                            showAlert(e.getMessage());
                        }
                    }

                }
            });
            contextMenu.getItems().add(itemDel);

            MenuItem itemVisible = new MenuItem("Скрыть/отобразить");
            itemVisible.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Country country = tabCountry.getSelectionModel().getSelectedItem();
                    if (country==null) return;
                    boolean isVisible = !country.isVisible();
                    country.setVisible(isVisible);

                        try {
                            int indx = countries.indexOf(country);
                            country= service.save(country);
                            countries.remove(indx);
                            countries.add(indx,country);
                            tabCountry.getSelectionModel().select(indx);
                        } catch (Exception e) {
                            String msg = e.getMessage();
                            msg.replace("400 Bad Request: ","").trim();
                            showAlert(e.getMessage());
                        }

                }
            });
            contextMenu.getItems().add(itemVisible);
        }
        // данные можно обновлять
        MenuItem itemRefresh = new MenuItem("Обновить");
        itemRefresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
               loadCountries();
            }
        });
        contextMenu.getItems().add(itemRefresh);
        tabCountry.setContextMenu(contextMenu);

        // создадим фильтрованые список на основе списка стран
        filteredData = new FilteredList<>(countries);
        // отсортируем фильтрованые данные
        SortedList<Country> sortedData = new SortedList<>(filteredData);
        // согласно выбраной колонке в таблцие
        sortedData.comparatorProperty().bind(tabCountry.comparatorProperty());
        // и укажем что таблица будет показывать отфильтрованые и отсортированые данные
        tabCountry.setItems(sortedData);
        // вызовем сортировку
        tabCountry.sort();

    }

    /**
     * загружает список стран с сервера
     */
    private void loadCountries(){
        Country country = tabCountry.getSelectionModel().getSelectedItem();
        countries.clear();
        try {
            List<Country> all = service.findAll();
            if (!all.isEmpty()){
                countries.addAll(all);
                if (country!=null && countries.contains(country))
                    tabCountry.getSelectionModel().select(country);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Dialog countryDialog(Country country) {
        final Dialog<Country> dialog = new Dialog<>();
        dialog.setTitle("Страна");
        if (country==null)
            dialog.setHeaderText("Добавление новой страны");
        else dialog.setHeaderText("Редактирование  страны");
        dialog.setResizable(false);


        TextField tCode = new TextField();
        tCode.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
        if (country!=null){
            tCode.setText(country.getCode());
        }

        TextField tName = new TextField();
        if (country!=null){
            tName.setText(country.getName());
        }
        TextField tNameEng = new TextField();
        if (country!=null){
            tNameEng.setText(country.getNameEng());
        }



        GridPane grid = new GridPane();
        grid.add(new Label("Код"), 1, 1);
        grid.add(tCode, 2, 1);
        grid.add(new Label("Название"), 1, 2);
        grid.add(tName, 2, 2);
        grid.add(new Label("Название (eng)"), 1, 3);
        grid.add(tNameEng, 2, 3);
        grid.setVgap(10);grid.setHgap(10);
        dialog.getDialogPane().setContent(grid);

        final ButtonType buttonTypeOk = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        final ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);


        dialog.setResultConverter(new javafx.util.Callback<ButtonType, Country>() {
            @Override
            public Country call(ButtonType buttonType) {
                if (buttonType == buttonTypeOk) {

                    return Country.builder().code(tCode.getText())
                            .name(tName.getText())
                            .nameEng(tNameEng.getText())
                            .visible(true).build();
                }
                return null;
            }
        });

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(buttonTypeOk);
        BooleanBinding booleanBind = Bindings.or(tCode.textProperty().isEmpty(),
        tName.textProperty().isEmpty()).or(tNameEng.textProperty().isEmpty());
        btOk.disableProperty().bind(booleanBind);

        return dialog;
    }

    // вызывате Alert WARNING с текстом message
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка!");
        alert.setHeaderText("Внимание!");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // вызывате Alert INFORMATION с текстом message
    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Внимание!");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void applyFilter(){

        filteredData.setPredicate(createPredicate(tFilter.getText().trim()));
    }

    private Predicate<Country> createPredicate(final String str) {
        // для каждой записи будет применятся фильтр, и будет возрващаеть true
        // если запись подпадает под условия фильтра
        return item -> {
            if (str==null || str.isEmpty()) return true;

            boolean isCode = false;
            boolean isName = false;
            boolean isNameEng = false;
            if (item!=null && item.getCode().toLowerCase().contains(str.toLowerCase())) isCode=true;
            if (item!=null && item.getName().toLowerCase().contains(str.toLowerCase())) isName=true;
            if (item!=null && item.getNameEng().toLowerCase().contains(str.toLowerCase())) isNameEng=true;
            return  isCode || isName || isNameEng;
        };
    }


}
