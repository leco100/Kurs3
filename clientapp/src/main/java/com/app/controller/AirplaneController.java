package com.app.controller;

import com.app.config.ApiConfig;
import com.app.exception.NotFoundException;
import com.app.model.Airplane;
import com.app.service.AirplaneService;
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

/**
 * контроллер справочника самолетов
 */
@Component
@FxmlView("airplane.fxml")
public class AirplaneController implements Initializable {

    // конфгурация
    @Autowired
    ApiConfig apiConfig;

    // служба rest клиент читает с сервера и пишет туда же
    @Autowired
    AirplaneService service;

    boolean isAdmin = false;
    // список самолетов
    ObservableList<Airplane> airplanes = FXCollections.observableArrayList();

    @FXML
    TextField  tFilter;

    // фильтрованные данные
    FilteredList<Airplane> filteredData;

    @FXML
    TableView<Airplane> tabAirplane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
         isAdmin = apiConfig.getUser().getRole().getName().equals("ROLE_ADMIN");
         initTab();
         loadAirplanes();
    }
    // иницализация таблицы
    private void initTab(){
        // cоздадим колонки согласно типам данных
        TableColumn<Airplane, String> codeColumn = new TableColumn<Airplane, String>("Модель");
        codeColumn.setCellValueFactory(new PropertyValueFactory<Airplane, String>("model"));
        codeColumn.setPrefWidth(100);
        tabAirplane.getColumns().add(codeColumn);

        TableColumn<Airplane, String> nameColumn = new TableColumn<Airplane, String>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<Airplane, String>("name"));
        nameColumn.setPrefWidth(150);
        tabAirplane.getColumns().add(nameColumn);

        TableColumn<Airplane, Integer> econonyColumn = new TableColumn<Airplane, Integer>("Кол-во мест\nэконом класса");
        econonyColumn.setCellValueFactory(new PropertyValueFactory<Airplane, Integer>("economySeats"));
        econonyColumn.setPrefWidth(150);
        tabAirplane.getColumns().add(econonyColumn);

        TableColumn<Airplane, Integer> businesColumn = new TableColumn<Airplane, Integer>("Кол-во мест\nбизнес класса");
        businesColumn.setCellValueFactory(new PropertyValueFactory<Airplane, Integer>("businessSeats"));
        businesColumn.setPrefWidth(150);
        tabAirplane.getColumns().add(businesColumn);


        // если зашли под админом - то покажем скрыт или нет
        if (isAdmin) {
            TableColumn<Airplane, Boolean> visibleColumn = new TableColumn<Airplane, Boolean>("Видимый");
            //visibleColumn.setCellValueFactory(new PropertyValueFactory<Airplane, Boolean>("visible"));
            visibleColumn.setCellValueFactory(c->{
                BooleanProperty active = new SimpleBooleanProperty(c.getValue().isVisible());
                return active;
            });
            visibleColumn.setCellFactory(column -> new CheckBoxTableCell<>());
            tabAirplane.getColumns().add(visibleColumn);
        }
        // выбирать можно только по одной записи
        tabAirplane.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // создадим контекстное меню и добавим обработчки
        ContextMenu contextMenu = new ContextMenu();
        if (isAdmin){
            MenuItem itemAdd = new MenuItem("Добавить");
            itemAdd.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    // вызовем диалого создания самолета
                    Optional<Airplane> result = airplaneDialog(null).showAndWait();
                    // если нажали кнопку "сохранит" - то что-то вернуло
                    if (result.isPresent()){
                        Airplane airplane = result.get();
                        try {
                            // запишем в базу
                            airplane= service.save(airplane);
                            // добавим в список, что не перечитываеть его ещё раз
                            airplanes.add(airplane);
                            // выделим его в таблице
                            tabAirplane.getSelectionModel().select(airplane);
                            // если ошбика
                        } catch (Exception e) {
                            // обрежем сообщене 400 Bad Request:
                            String msg = e.getMessage();
                            msg.replace("400 Bad Request: ","").trim();
                            // выведем ошибку
                            showAlert(e.getMessage());
                        }
                    }
                }
            });
            // добавим пункт меню
            contextMenu.getItems().add(itemAdd);

            // меню редактировать
            MenuItem itemEdit = new MenuItem("Редактировать");
            itemEdit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    //  получим выделенный обьект
                    Airplane selected = tabAirplane.getSelectionModel().getSelectedItem();
                    if (selected==null) return;
                    // и передадим его в диалог, подожем пока не нажмут кнопку сохранить или
                    // отменить
                    Optional<Airplane> result = airplaneDialog(selected).showAndWait();
                    if (result.isPresent()){
                        // получим результат
                        Airplane airplane = result.get();
                        try {
                            // сохраним в базу
                            airplane= service.save(airplane);
                            // удалим с таблицы и вставим по новому
                            int indx = airplanes.indexOf(selected);
                            airplanes.remove(indx);
                            airplanes.add(indx,airplane);
                            tabAirplane.getSelectionModel().select(airplane);
                        } catch (Exception e) {
                            // если ошибка - то выведем сообщение об ошибке
                            String msg = e.getMessage();
                            msg.replace("400 Bad Request: ","").trim();
                            showAlert(e.getMessage());
                        }
                    }

                }
            });
            contextMenu.getItems().add(itemEdit);

            // пункт меню удалить
            MenuItem itemDel = new MenuItem("Удалить");
            itemDel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Airplane selected = tabAirplane.getSelectionModel().getSelectedItem();
                    if (selected==null) return;
                    // вызовем диалог, в ктором спросим будет ли удалять
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Удаление записи");
                    alert.setHeaderText("Удаление "+selected.getName());
                    alert.setContentText("Удалить это самолет?");
                    Optional<ButtonType> result = alert.showAndWait();
                    // если согласиля
                    if (result.get() == ButtonType.OK){
                        try {
                            int indx = airplanes.indexOf(selected);
                            // удалим с базы
                            service.delete(selected.getId());
                            try {
                                // если удалило - то в базе его больше нет
                                service.findById(selected.getId());
                            }catch (NotFoundException e) {
                                // значит можно удалять с таблицы
                                    airplanes.remove(indx);
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

            // скрывает/отображает объект в базе, скрытые объект пользователь не видит в справочнике
            // но если они был использованы в связаных обьектах - то увидити
            MenuItem itemVisible = new MenuItem("Скрыть/отобразить");
            itemVisible.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Airplane airplane = tabAirplane.getSelectionModel().getSelectedItem();
                    if (airplane==null) return;
                    boolean isVisible = !airplane.isVisible();
                    airplane.setVisible(isVisible);

                        try {
                            int indx = airplanes.indexOf(airplane);
                            // сохраним в базе
                            airplane= service.save(airplane);
                            // обновим в таблице
                            airplanes.remove(indx);
                            airplanes.add(indx,airplane);
                            tabAirplane.getSelectionModel().select(indx);
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
               loadAirplanes();
            }
        });
        contextMenu.getItems().add(itemRefresh);
        tabAirplane.setContextMenu(contextMenu);

        // создадим фильтрованые список на основе списка стран
        filteredData = new FilteredList<>(airplanes);
        // отсортируем фильтрованые данные
        SortedList<Airplane> sortedData = new SortedList<>(filteredData);
        // согласно выбраной колонке в таблцие
        sortedData.comparatorProperty().bind(tabAirplane.comparatorProperty());
        // и укажем что таблица будет показывать отфильтрованые и отсортированые данные
        tabAirplane.setItems(sortedData);
        // вызовем сортировку
        tabAirplane.sort();

    }

    /**
     * загружает список моделей самолетов с сервера
     */
    private void loadAirplanes(){
        // если был выбрана какая-то запись - то запомним какая
        Airplane Airplane = tabAirplane.getSelectionModel().getSelectedItem();
        // очистим список
        airplanes.clear();
        try {
            // загрузим  из базы
            List<Airplane> all = service.findAll();
            if (!all.isEmpty()){
                // добавим в списко
                airplanes.addAll(all);
                if (Airplane!=null && airplanes.contains(Airplane))
                    tabAirplane.getSelectionModel().select(Airplane);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // выведем ошибку, если есть
            showAlert(e.getMessage());
        }
    }

    // диалог создания/редактирования самолета
    Dialog airplaneDialog(Airplane airplane) {
        final Dialog<Airplane> dialog = new Dialog<>();
        dialog.setTitle("Самолет");
        if (airplane==null)
            dialog.setHeaderText("Добавление нового самолета");
        else dialog.setHeaderText("Редактирование  самолета");
        dialog.setResizable(false);


        // создадим поля
        TextField tModel = new TextField();
        if (airplane!=null){
            tModel.setText(airplane.getModel());
        }

        TextField tName = new TextField();
        if (airplane!=null){
            tName.setText(airplane.getName());
        }
        Spinner<Integer> tEconomySeats =  new Spinner<Integer>();
        tEconomySeats.setEditable(true);
        SpinnerValueFactory<Integer> economyValueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 300, 100);
        tEconomySeats.setValueFactory(economyValueFactory);
        // так как мы вручную тоже хотим вводить значения, а спинер этого не понимает - то такой финт
        TextFormatter economyFormater = new TextFormatter(economyValueFactory.getConverter(), economyValueFactory.getValue());
        tEconomySeats.getEditor().setTextFormatter(economyFormater);
        economyValueFactory.valueProperty().bindBidirectional(economyFormater.valueProperty());
        tEconomySeats.setValueFactory(economyValueFactory);

        if (airplane!=null){
            economyValueFactory.setValue(tEconomySeats.getValue());;
        }

        Spinner<Integer> tBusinesSeats =  new Spinner<Integer>();
        tBusinesSeats.setEditable(true);
        SpinnerValueFactory<Integer> bussinesValueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 100);

        tBusinesSeats.setValueFactory(bussinesValueFactory);
        TextFormatter businesFormater = new TextFormatter(economyValueFactory.getConverter(), economyValueFactory.getValue());
        tBusinesSeats.getEditor().setTextFormatter(businesFormater);
        bussinesValueFactory.valueProperty().bindBidirectional(businesFormater.valueProperty());
        tBusinesSeats.setValueFactory(bussinesValueFactory);

        if (airplane!=null){
            bussinesValueFactory.setValue(tBusinesSeats.getValue());;
        }



        // поместим их в grid
        GridPane grid = new GridPane();
        grid.add(new Label("Модель"), 1, 1);
        grid.add(tModel, 2, 1);
        grid.add(new Label("Название"), 1, 2);
        grid.add(tName, 2, 2);
        grid.add(new Label("Ко-во мест в эконом классе"), 1, 3);
        grid.add(tEconomySeats, 2, 3);
        grid.add(new Label("Ко-во мест в бизнес классе"), 1, 4);
        grid.add(tBusinesSeats, 2, 4);

        grid.setVgap(10);grid.setHgap(10);
        dialog.getDialogPane().setContent(grid);

        // создадим кнопки
        final ButtonType buttonTypeOk = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        final ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);


        // если нажмем ок- то нужно что-то вернуть
        dialog.setResultConverter(new javafx.util.Callback<ButtonType, Airplane>() {
            @Override
            public Airplane call(ButtonType buttonType) {
                if (buttonType == buttonTypeOk) {

                    return Airplane.builder().model(tModel.getText())
                            .name(tName.getText())
                            .economySeats(tEconomySeats.getValue())
                            .businessSeats(tBusinesSeats.getValue())
                            .visible(true).build();
                }
                return null;
            }
        });

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(buttonTypeOk);
        // запретим нажимать кнопку сохранить, если поля пустые
        BooleanBinding booleanBind = Bindings.or(tModel.textProperty().isEmpty(),
        tName.textProperty().isEmpty());
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

    // предикат, будет возращает true если строка str
    // каким-то образом есть в airplane
    private Predicate<Airplane> createPredicate(final String str) {
        // для каждой записи будет применятся фильтр, и будет возрващаеть true
        // если запись подпадает под условия фильтра
        return item -> {
            if (str==null || str.isEmpty()) return true;

            boolean isModel = false;
            boolean isName = false;
            boolean isEconomySeat = false;
            boolean isBussinesSeat = false;
            // или в модели или в названии или кол-ве мест
            if (item!=null && item.getModel().toLowerCase().contains(str.toLowerCase())) isModel=true;
            if (item!=null && item.getName().toLowerCase().contains(str.toLowerCase())) isName=true;
            if (item!=null && Integer.toString(item.getEconomySeats()).contains(str.toLowerCase())) isEconomySeat=true;
            if (item!=null && Integer.toString(item.getBusinessSeats()).contains(str.toLowerCase())) isBussinesSeat=true;
            return  isModel || isName || isEconomySeat || isBussinesSeat;
        };
    }


}
