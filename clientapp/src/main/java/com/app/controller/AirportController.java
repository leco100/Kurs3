package com.app.controller;

import com.app.config.ApiConfig;
import com.app.exception.NotFoundException;
import com.app.model.Airport;
import com.app.model.Country;
import com.app.service.AirportService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * контроллер справочник аэропортов
 */
@Component
@FxmlView("airport.fxml")
public class AirportController implements Initializable {

    @Autowired
    ApiConfig apiConfig;

    @Autowired
    AirportService service;
    @Autowired
    CountryService countryService;

    boolean isAdmin = false;

    ObservableList<Country> countries = FXCollections.observableArrayList();
    ObservableList<Airport> airports = FXCollections.observableArrayList();

    @FXML
    TextField  tFilter;

    // фильтрованные данные
    FilteredList<Airport> filteredData;

    @FXML
    ComboBox<Country> country;



    @FXML
    TableView<Airport> tabAirport;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
         isAdmin = apiConfig.getUser().getRole().getName().equals("ROLE_ADMIN");
         // обновим справочник стран из сервера
         loadCountries(null);
         // создадим таблцицу
         initTab();
        // обновим справочник аэропортов из сервера
         loadAirports(null);
    }

    private void loadAirports(Country country) {
        // запомним текущеий
        Airport airport = tabAirport.getSelectionModel().getSelectedItem();
        airports.clear();
        try {
            boolean all = country==null || country.getCode().equals("ALL");
            // загрузим или все справочники или только по коду страны
            List<Airport> list = all ? service.findAll():service.findByCountry(country.getCode());
            if (!list.isEmpty()){
                airports.addAll(list);
                if (airport !=null && countries.contains(airport))
                    tabAirport.getSelectionModel().select(airport);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTab(){
        // cоздадим колонки согласно типам данных

        TableColumn<Airport, String> codeColumn = new TableColumn<Airport, String>("Код");
        codeColumn.setCellValueFactory(new PropertyValueFactory<Airport, String>("code"));
        codeColumn.setPrefWidth(100);
        tabAirport.getColumns().add(codeColumn);

        TableColumn<Airport, String> nameColumn = new TableColumn<Airport, String>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<Airport, String>("name"));
        nameColumn.setPrefWidth(100);
        tabAirport.getColumns().add(nameColumn);

        TableColumn<Airport, String> cityColumn = new TableColumn<Airport, String>("Город");
        cityColumn.setCellValueFactory(new PropertyValueFactory<Airport, String>("city"));
        cityColumn.setPrefWidth(100);
        tabAirport.getColumns().add(cityColumn);



        TableColumn<Airport, Country> countryColumn = new TableColumn<>("Страна");
        countryColumn.setCellValueFactory(new PropertyValueFactory<Airport, Country>("country"));

        countryColumn.setCellFactory(column -> new TableCell<Airport, Country>() {
            @Override
            protected void updateItem(Country item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });

        tabAirport.getColumns().add(countryColumn);

        // если зашли под админом - то покажем кто последний правил
        if (isAdmin) {
            TableColumn<Airport, Boolean> visibleColumn = new TableColumn<Airport, Boolean>("Видимый");
            //visibleColumn.setCellValueFactory(new PropertyValueFactory<Company, Boolean>("visible"));
            visibleColumn.setCellValueFactory(c->{
                BooleanProperty active = new SimpleBooleanProperty(c.getValue().isVisible());
                return active;
            });
            visibleColumn.setCellFactory(column -> new CheckBoxTableCell<>());
            tabAirport.getColumns().add(visibleColumn);
        }


        tabAirport.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // создадим контекстное меню и добавим обработчки
        ContextMenu contextMenu = new ContextMenu();
        if (isAdmin){
            MenuItem itemAdd = new MenuItem("Добавить");
            itemAdd.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Optional<Airport> result = airportDialog(null).showAndWait();
                    if (result.isPresent()){
                        Airport airport = result.get();
                        try {
                            airport= service.save(airport);
                            airports.add(airport);
                            tabAirport.getSelectionModel().select(airport);
                        } catch (Exception e) {
                            String msg = e.getMessage();
                            msg.replace("400 Bad Request: ","").trim();
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
                    Airport selected = tabAirport.getSelectionModel().getSelectedItem();
                    if (selected==null) return;
                    Optional<Airport> result = airportDialog(selected).showAndWait();
                    int indx = airports.indexOf(selected);
                    if (result.isPresent()){
                        Airport airport = result.get();
                        try {
                            airport= service.save(airport);
                            airports.remove(indx);
                            airports.add(indx,airport);
                            tabAirport.getSelectionModel().select(airport);
                        } catch (Exception e) {
                            e.printStackTrace();
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
                    Airport selected = tabAirport.getSelectionModel().getSelectedItem();
                    if (selected==null) return;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Удаление записи");
                    alert.setHeaderText("Удаление "+selected.getName());
                    alert.setContentText("Удалить эту компанию?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        try {
                            int indx = airports.indexOf(selected);
                            service.delete(selected.getCode());
                            try {
                                service.findByCode(selected.getCode());
                            }catch (NotFoundException e) {
                                    airports.remove(indx);
                            }catch (Exception e){
                                showAlert(e.getMessage());
                            }

                        } catch (Exception e) {
                            String msg = e.getMessage();
                            msg.replace("400 Bad Request: ","").trim();
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
                    Airport airport = tabAirport.getSelectionModel().getSelectedItem();
                    if (actionEvent==null) return;
                    boolean isVisible = !airport.isVisible();
                    airport.setVisible(isVisible);

                        try {
                            int indx = airports.indexOf(airport);
                            airport= service.save(airport);
                            airports.remove(indx);
                            airports.add(indx,airport);
                            tabAirport.getSelectionModel().select(indx);
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
               //loadCompanies(country.getValue());
               applyFilter();
            }
        });
        contextMenu.getItems().add(itemRefresh);
        tabAirport.setContextMenu(contextMenu);

        // создадим фильтрованые список на основе списка стран
        filteredData = new FilteredList<>(airports);
        // отсортируем фильтрованые данные
        SortedList<Airport> sortedData = new SortedList<>(filteredData);
        // согласно выбраной колонке в таблцие
        sortedData.comparatorProperty().bind(tabAirport.comparatorProperty());
        // и укажем что таблица будет показывать отфильтрованые и отсортированые данные
        tabAirport.setItems(sortedData);
        // вызовем сортировку
        tabAirport.sort();

    }

    /**
     * загружает список аэропортов с сервера
     * @param selected  страна, по которой грузятся
     */
    private void loadCountries(Country selected){

        countries.clear();
        try {
            boolean all = selected==null || selected.getCode().equals("ALL");
            List<Country> list = all ?  countryService.findAll(): Arrays.asList(countryService.findByCode(selected.getCode()));
            if (!list.isEmpty()){
                countries.addAll(list);
                countries.add(0,Country.builder().code("ALL").name("Все").build());
                country.setItems(countries);
                if (selected==null || selected.getCode().equals("ALL"))
                   country.getSelectionModel().selectFirst();
                else
                   country.getSelectionModel().select(selected);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(e.getMessage());
        }
    }

    Dialog airportDialog(Airport airport) {
        final Dialog<Airport> dialog = new Dialog<>();
        dialog.setTitle("Аэропорт");
        if (airport==null)
            dialog.setHeaderText("Добавление нового аэропорта");
        else dialog.setHeaderText("Редактирование  аэропорта");
        dialog.setResizable(false);


        TextField tCode = new TextField();
        tCode.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
        tCode.setPrefWidth(350);
        tCode.setEditable(true);
        if (airport!=null){
            tCode.setText(airport.getCode());
            tCode.setEditable(false);
        }

        TextField tName = new TextField();
        tName.setPrefWidth(350);

        if (airport!=null){
            tName.setText(airport.getName());
        }

        TextField tCity = new TextField();
        tCity.setPrefWidth(350);

        if (airport!=null){
            tCity.setText(airport.getCity());
        }


        ComboBox<Country>  tCountry  = new ComboBox<>(countries);
        tCountry.setPrefWidth(350);

        if (airport==null &&  country.getValue()!=null){
            tCountry.getSelectionModel().select(country.getValue());
        }else if (airport!=null){
            tCountry.getSelectionModel().select(airport.getCountry());
        }



        GridPane grid = new GridPane();

        grid.add(new Label("Код"), 1, 1);
        grid.add(tCode, 2, 1);
        grid.add(new Label("Название"), 1, 2);
        grid.add(tName, 2, 2);
        grid.add(new Label("Город"), 1, 3);
        grid.add(tCity, 2, 3);
        grid.add(new Label("Страна"), 1, 4);
        grid.add(tCountry, 2, 4);
        grid.setPrefWidth(500);
        grid.setMinWidth(500);
        grid.setVgap(10);grid.setHgap(10);
        dialog.getDialogPane().setContent(grid);

        final ButtonType buttonTypeOk = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        final ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);


        dialog.setResultConverter(new javafx.util.Callback<ButtonType, Airport>() {
            @Override
            public Airport call(ButtonType buttonType) {
                if (buttonType == buttonTypeOk) {

                    Airport result =  Airport.builder().name(tName.getText())
                            .code(tCode.getText())
                            .name(tName.getText())
                            .city(tCity.getText())
                            .country(tCountry.getValue())
                            .visible(true).build();
                    return result;
                }
                return null;
            }
        });

        final  Country allCompany = countries.get(0);
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(buttonTypeOk);

        BooleanBinding booleanBind = Bindings.or(tName.textProperty().isEmpty(),
        tCode.textProperty().isEmpty()).or(tCity.textProperty().isEmpty())
                .or(tCountry.valueProperty().isEqualTo(allCompany));

        btOk.disableProperty().bind(booleanBind);
        return dialog;
    }


    // вызывает Alert WARNING с текстом message
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
        Country selectedCountry = country.getValue();
        loadCountries(selectedCountry);
        loadAirports(country.getValue());
        filteredData.setPredicate(createPredicate(tFilter.getText().trim()));
    }

    @FXML
    private void applyFilterName(){
        filteredData.setPredicate(createPredicate(tFilter.getText().trim()));
    }

    private Predicate<Airport> createPredicate(final String str) {
        // для каждой записи будет применятся фильтр, и будет возрващаеть true
        // если запись подпадает под условия фильтра

        return item -> {
            if (str==null || str.isEmpty()) return true;

            boolean isName = str==null? true : item.getName().toLowerCase().contains(str.toLowerCase());
            boolean isCity = str==null? true : item.getCity().toLowerCase().contains(str.toLowerCase());
            boolean isCode = str==null? true : item.getCode().toLowerCase().contains(str.toLowerCase());
            boolean isCountry = str==null? true : item.getCountry().getName().toLowerCase().contains(str.toLowerCase());
            return  isName || isCode || isCity|| isCountry;
        };
    }


}
