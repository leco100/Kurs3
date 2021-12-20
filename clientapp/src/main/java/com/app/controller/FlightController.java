package com.app.controller;

import com.app.config.ApiConfig;
import com.app.exception.NotFoundException;
import com.app.export.ExportService;
import com.app.model.*;
import com.app.service.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@FxmlView("flight.fxml")
public class FlightController implements Initializable {

    @Autowired
    ApiConfig apiConfig;
    @Autowired
    CountryService countryService;

    @Autowired
    AirportService airportService;

    @Autowired
    AirplaneService airplaneService;

    @Autowired
    CompanyService companyService;

    @Autowired
    FlightService flightService;

    @FXML
    private DatePicker dateFrom;
    @FXML
    private DatePicker dateTo;

    @FXML
    private ComboBox<Company> company;

    @FXML
    private ComboBox<Country> countryFrom;
    @FXML
    private ComboBox<Country> countryTo;


    @FXML
    private ComboBox<String> cityFrom;
    @FXML
    private ComboBox<String> cityTo;


    @FXML
    private ComboBox<Airport> airportFrom;
    @FXML
    private ComboBox<Airport> airportTo;

    @FXML
    private TableView<Flight> tabFlight;

    @FXML
    private Label reportStatus;
    @FXML
    private ProgressBar progressBar;

    ExportService exportService;


    final ObservableList<Country> countries = FXCollections.observableArrayList();
    final ObservableList<Airport> airports = FXCollections.observableArrayList();
    final ObservableList<Airplane> airplanes = FXCollections.observableArrayList();
    final ObservableList<Company> companies = FXCollections.observableArrayList();
    final ObservableList<Flight> flights = FXCollections.observableArrayList();

    final FilteredList<Airport> airportsFrom = new FilteredList<>(airports);
    final FilteredList<Airport> airportsTo = new FilteredList<>(airports);
    final FilteredList<Company> companiesFiltered = new FilteredList<>(companies);
    final FilteredList<Flight> flightsFiltered = new FilteredList<>(flights);

    final ObservableList<String> citiesFrom = FXCollections.observableArrayList();
    final ObservableList<String> citiesTo = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now().plusDays(7);
        dateFrom.setValue(from);
        dateTo.setValue(to);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loadCountries();
                loadCompanies();
                loadAirports();
                loadAirplanes();

            }
        });
        loadFlights();
        airportFrom.setItems(airportsFrom);
        airportTo.setItems(airportsTo);
        countryFrom.valueProperty().addListener(new ChangeListener<Country>() {
            @Override
            public void changed(ObservableValue<? extends Country> observable, Country oldValue, Country newValue) {
                airportsFrom.setPredicate(airportPredicate(newValue));
                List<String> cities = airportsFrom.stream().map(a -> a.getCity()).distinct().sorted().collect(Collectors.toList());
                citiesFrom.clear(); citiesFrom.addAll(cities);
                cityFrom.setItems(citiesFrom);
                airportFrom.getSelectionModel().clearSelection();
                airportFrom.getSelectionModel().select(null);
                airportFrom.valueProperty().set(null);
                airportFrom.setValue(null);

                companiesFiltered.setPredicate(companyPredicate(newValue, countryTo.getValue()));

            }
        });
        countryTo.valueProperty().addListener(new ChangeListener<Country>() {
            @Override
            public void changed(ObservableValue<? extends Country> observable, Country oldValue, Country newValue) {
                //applyFilter();

                airportsTo.setPredicate(airportPredicate(newValue));
                List<String> cities = airportsTo.stream().map(a -> a.getCity()).distinct().sorted().collect(Collectors.toList());
                citiesTo.clear(); citiesTo.addAll(cities);
                cityTo.setItems(citiesTo);
                airportTo.getSelectionModel().clearSelection();
                airportTo.getSelectionModel().select(null);
                airportTo.valueProperty().set(null);
                airportTo.setValue(null);
                companiesFiltered.setPredicate(companyPredicate(countryFrom.getValue(), countryTo.getValue()));

            }
        });
        cityFrom.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue==null) airportsFrom.setPredicate(p->true);
                airportsFrom.setPredicate(p->{
                    if (p==null) return true;
                    if (newValue==null) return true;
                    return p.getCity().contains(newValue);
                });

            }
        });
        cityTo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue==null) airportsTo.setPredicate(p->true);
                    airportsTo.setPredicate(p->{
                        if (p==null) return true;
                        if (newValue==null) return true;
                        return p.getCity().contains(newValue);
                });

            }
        });
        initTable();
        exportService = new ExportService();
        exportService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                progressBar.progressProperty().unbind();
                reportStatus.textProperty().unbind();



            }
        });

        exportService.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                progressBar.progressProperty().unbind();
                reportStatus.textProperty().unbind();
                reportStatus.setText("EXPORT FAIL");
                Throwable throwable = exportService.getException();
                throwable.printStackTrace();

            }
        });

    }

    private void initTable() {


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("hh:mm");


        TableColumn<Flight, String> codeColumn = new TableColumn<Flight, String>("Код рейса");
        codeColumn.setCellValueFactory(new PropertyValueFactory<Flight, String>("code"));
        codeColumn.setPrefWidth(100);
        tabFlight.getColumns().add(codeColumn);

        TableColumn<Flight, LocalDate> dateDepColumn = new TableColumn<>("Дата вылета");
        dateDepColumn.setCellValueFactory(new PropertyValueFactory<Flight, LocalDate>("dateDep"));

        dateDepColumn.setCellFactory(column -> new TableCell<Flight, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        dateDepColumn.setPrefWidth(100);
        tabFlight.getColumns().add(dateDepColumn);

        TableColumn<Flight, LocalTime> timeDepColumn = new TableColumn<>("Время вылета");
        timeDepColumn.setCellValueFactory(new PropertyValueFactory<Flight, LocalTime>("timeDep"));
        timeDepColumn.setCellFactory(column -> new TableCell<Flight, LocalTime>() {
            @Override
            protected void updateItem(LocalTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(formatterTime.format(date));
                }
            }
        });

        timeDepColumn.setPrefWidth(100);
        tabFlight.getColumns().add(timeDepColumn);

        TableColumn<Flight, Airport> areaFromColumn = new TableColumn<>("Аэророрт вылета");
        areaFromColumn.setCellValueFactory(new PropertyValueFactory<Flight, Airport>("areaSrc"));
        areaFromColumn.setCellFactory(column -> new TableCell<Flight, Airport>() {
            @Override
            protected void updateItem(Airport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getCode() + " " + item.getName());
                }
            }
        });

        areaFromColumn.setPrefWidth(250);
        tabFlight.getColumns().add(areaFromColumn);

        TableColumn<Flight, LocalDate> dateArivColumn = new TableColumn<>("Дата прилета");
        dateArivColumn.setCellValueFactory(new PropertyValueFactory<Flight, LocalDate>("dateArrival"));

        dateArivColumn.setCellFactory(column -> new TableCell<Flight, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        dateArivColumn.setPrefWidth(100);
        tabFlight.getColumns().add(dateArivColumn);

        TableColumn<Flight, LocalTime> timeArivColumn = new TableColumn<>("Время прилета");
        timeArivColumn.setCellValueFactory(new PropertyValueFactory<Flight, LocalTime>("timeArrival"));
        timeArivColumn.setCellFactory(column -> new TableCell<Flight, LocalTime>() {
            @Override
            protected void updateItem(LocalTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(formatterTime.format(date));
                }
            }
        });

        timeArivColumn.setPrefWidth(100);
        tabFlight.getColumns().add(timeArivColumn);

        TableColumn<Flight, Airport> areaToColumn = new TableColumn<>("Аэророрт прилета");
        areaToColumn.setCellValueFactory(new PropertyValueFactory<Flight, Airport>("areaDestination"));
        areaToColumn.setCellFactory(column -> new TableCell<Flight, Airport>() {
            @Override
            protected void updateItem(Airport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getCode() + " " + item.getName());
                }
            }
        });

        areaToColumn.setPrefWidth(250);
        tabFlight.getColumns().add(areaToColumn);

        TableColumn<Flight, Integer> flightColumn = new TableColumn<>("Время в полете, мин");
        flightColumn.setCellValueFactory(new PropertyValueFactory<Flight, Integer>("flightTime"));

        flightColumn.setPrefWidth(250);
        tabFlight.getColumns().add(flightColumn);

        TableColumn<Flight, Integer> addColumn = new TableColumn<>("Время задержки, мин");
        addColumn.setCellValueFactory(new PropertyValueFactory<Flight, Integer>("addTime"));

        addColumn.setPrefWidth(250);
        tabFlight.getColumns().add(addColumn);

        TableColumn<Flight, FlyStatus> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<Flight, FlyStatus>("status"));
        statusColumn.setCellFactory(column -> new TableCell<Flight, FlyStatus>() {
            @Override
            protected void updateItem(FlyStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });

        statusColumn.setPrefWidth(250);
        tabFlight.getColumns().add(statusColumn);

        TableColumn<Flight, Company> companyColumn = new TableColumn<>("Компания");
        companyColumn.setCellValueFactory(new PropertyValueFactory<Flight, Company>("company"));
        companyColumn.setCellFactory(column -> new TableCell<Flight, Company>() {
            @Override
            protected void updateItem(Company item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });

        companyColumn.setPrefWidth(250);
        tabFlight.getColumns().add(companyColumn);

        TableColumn<Flight, Airplane> airplaneColumn = new TableColumn<>("Тип самолета");
        airplaneColumn.setCellValueFactory(new PropertyValueFactory<Flight, Airplane>("airplane"));
        airplaneColumn.setCellFactory(column -> new TableCell<Flight, Airplane>() {
            @Override
            protected void updateItem(Airplane item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });

        airplaneColumn.setPrefWidth(250);
        tabFlight.getColumns().add(airplaneColumn);


        if (apiConfig.getUser().getRole().getName().equals("ROLE_ADMIN")) {

            ContextMenu contextMenu = new ContextMenu();

            MenuItem itemAdd = new MenuItem("Создать");
            itemAdd.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {

                    Optional<Flight> result = flightDialog().showAndWait();
                    if (result.isPresent()) {
                        Flight flight = result.get();
                        try {
                            flight = flightService.insert(flight);
                            flights.add(flight);
                            applyFilter();
                        } catch (Exception e) {
                            showAlert(e.getMessage());
                        }
                    }

                }
            });

            MenuItem itemEdit = new MenuItem("Редактировать");
            itemEdit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {

                    Flight selected = tabFlight.getSelectionModel().getSelectedItem();
                    if (selected==null) return;;
                    Optional<Flight> result = editDialog(selected).showAndWait();

                    if (result.isPresent()) {
                        Flight flight = result.get();
                        try {
                            flight = flightService.save(flight);
                            flights.remove(selected);
                            flights.add(flight);
                            applyFilter();
                            tabFlight.refresh();
                            tabFlight.getSelectionModel().select(flight);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(e.getMessage());
                        }
                    }

                }
            });


            MenuItem itemDel = new MenuItem("Удалить");
            BooleanBinding booleanBind = Bindings.isEmpty(flightsFiltered);
            itemDel.disableProperty().bind(booleanBind);
            itemDel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {

                    Flight flight = tabFlight.getSelectionModel().getSelectedItem();
                    if (flight == null) return;
                    if (!flight.getStatus().equals(FlyStatus.SCHEDULED)) return;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Удаление записи");
                    alert.setHeaderText("Удаление рейс" + flight.getCode());
                    alert.setContentText("Удалить этот рейс?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        flight.setVisible(false);

                        try {
                            flightService.delete(flight.getCode());
                            try {
                                flightService.findByCode(flight.getCode());
                            } catch (NotFoundException e) {
                                flights.remove(flight);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(e.getMessage());
                        }
                    }

                }
            });


            contextMenu.getItems().addAll(itemAdd,itemEdit, itemDel);

            tabFlight.setContextMenu(contextMenu);
            tabFlight.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            // слушатель выделения, когда мы выбираем какую-то ячейку
            tabFlight.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {

                    if (tabFlight.getSelectionModel().getSelectedItem() != null) {
                        // получим рейс
                        Flight item = tabFlight.getSelectionModel().getSelectedItem();
                        SimpleObjectProperty<FlyStatus> statusProperty =new SimpleObjectProperty();
                        statusProperty.set(item.getStatus());
                        // создадим привязку к значениям
                        BooleanBinding sentBinding =  statusProperty.isEqualTo(FlyStatus.SENT);
                        BooleanBinding delayedBinding =  statusProperty.isEqualTo(FlyStatus.DELAYED);
                        BooleanBinding regBinding =  statusProperty.isEqualTo(FlyStatus.REGISTRATION);
                        BooleanBinding canceledBinding =  statusProperty.isEqualTo(FlyStatus.CANCELED);
                        BooleanBinding arrivedBinding =  statusProperty.isEqualTo(FlyStatus.ARRIVED);
                        BooleanBinding expectedBinding =  statusProperty.isEqualTo(FlyStatus.EXPECTED);
                        // когда нельзя удалять
                        BooleanBinding del = BooleanBinding.booleanExpression(sentBinding).or(delayedBinding)
                                .or(regBinding).or(canceledBinding).or(arrivedBinding).or(expectedBinding).or(arrivedBinding);
                        // когда нельзя редактировать
                        BooleanBinding edit = BooleanBinding.booleanExpression(canceledBinding).or(arrivedBinding);
                        // привяжем статусы
                        itemEdit.disableProperty().bind(edit);
                        itemDel.disableProperty().bind(del);




                    }
                }
            });

        }

        SortedList<Flight> sortedData = new SortedList<>(flightsFiltered);
        // согласно выбраной колонке в таблцие
        sortedData.comparatorProperty().bind(tabFlight.comparatorProperty());
        // и укажем что таблица будет показывать отфильтрованые и отсортированые данные
        tabFlight.setItems(sortedData);
        // вызовем сортировку
        tabFlight.sort();


    }


    private void loadCountries() {
        try {
            List<Country> list = countryService.findAll();
            countries.clear();
            countries.addAll(list);
            countryFrom.setItems(countries);
            countryTo.setItems(countries);
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    private void loadAirports() {
        try {
            List<Airport> list = airportService.findAll();
            airports.clear();
            airports.addAll(list);
            List<String> cities = list.stream().map(a -> a.getCity()).distinct().collect(Collectors.toList());
            citiesFrom.clear(); citiesFrom.addAll(cities);
            citiesTo.clear(); citiesTo.addAll(cities);

        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    private void loadCompanies() {

        try {
            List<Company> list = companyService.findAll();
            companies.clear();
            companies.addAll(list);
            company.setItems(companiesFiltered);
        } catch (Exception e) {
            showAlert(e.getMessage());
        }

    }

    private void loadAirplanes() {
        try {
            List<Airplane> list = airplaneService.findAll();
            airplanes.clear();
            airplanes.addAll(list);
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    @FXML
    private void loadFlights() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    flights.clear();
                    List<Flight> list = flightService.findByPeriod(dateFrom.getValue(), dateTo.getValue());
                    flights.addAll(list);
                    applyFilter();
                } catch (Exception e) {
                    showAlert(e.getMessage());
                }
            }
        });

    }

    // применим фильтр рейсов
    @FXML
    private void applyFilter() {

        // установим предикат
        flightsFiltered.setPredicate(item -> {
            Airport from = airportFrom.getValue();
            boolean isAirportFrom = from == null ? true : item.getAreaSrc().equals(from);
            Airport to = airportTo.getValue();
            boolean isAirportTo = to == null ? true : item.getAreaDestination().equals(to);
            Company c = company.getValue();
            boolean isCompany = c == null ? true : item.getCompany().equals(c);

            boolean isCountryFrom = countryFrom.getValue() == null ? true :
                    item.getAreaSrc().getCountry().equals(countryFrom.getValue());
            boolean isCountryTo = countryTo.getValue() == null ? true :
                    item.getAreaDestination().getCountry().equals(countryTo.getValue());
            // он вернет true только если все условия соблюдены
            return isAirportFrom && isAirportTo && isCompany && isCountryFrom && isCountryTo;
        });
    }

    // очистка фильтров
    @FXML
    private void clearFilter() {
        countryFrom.getSelectionModel().clearSelection();
        countryTo.getSelectionModel().clearSelection();
        airportsFrom.setPredicate(airportPredicate(null));
        airportFrom.setValue(null);
        airportsTo.setPredicate(airportPredicate(null));
        airportTo.setValue(null);
        companiesFiltered.setPredicate(companyPredicate(null, null));
        company.setValue(null);
        applyFilter();
    }

    // вызывает Alert WARNING с текстом message
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка!");
        alert.setHeaderText("Внимание!");
        alert.setContentText(message);
        alert.showAndWait();
    }


    // выводит сообщение с типом INFORMATION
    public void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Внимание");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // возвращет предикат для airport по стране
    private Predicate<Airport> airportPredicate(Country country) {
        return item -> {
            if (country == null) return true;
            return item.getCountry().equals(country);
        };
    }

    // возвращает предикат company по стране (из обохи направлений)
    private Predicate<Company> companyPredicate(Country from, Country to) {
        return item -> {
            if (from == null && to == null) return true;
            boolean isFrom = from == null ? true : item.getCountry().equals(from);
            boolean isTo = to == null ? true : item.getCountry().equals(to);
            //или та или та должна совпасть
            return isFrom || isTo;
        };
    }

    // диалог редактирования рейса
    // так как у нас учебный проект
    // то разрешим менять "все" в одном диалоге
    // что бы показать как можно менять данные диспечеру
    Dialog editDialog(Flight flight){
        final Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Авиарейс");
        dialog.setHeaderText("Редактирование  рейса");
        dialog.setResizable(false);

        TextField tCode = new TextField();
        tCode.setText(flight.getCode());
        tCode.setEditable(false);
        tCode.setPrefWidth(350);
        DatePicker tDate = new DatePicker();
        tDate.setValue(flight.getDateDep());
        // дату нельз менять! такой рейс нужно отменить и создать с новой датой

        tDate.setEditable(false);

        ObservableList<Integer> hours = FXCollections.observableArrayList(IntStream.rangeClosed(0, 23).boxed().collect(Collectors.toList()));
        ComboBox<Integer> chHour = new ComboBox<>();
        chHour.setItems(hours);
        // разрешим менять дату время на случай задержки рейса
        if (!flight.getStatus().equals(FlyStatus.SCHEDULED)
                || flight.getStatus().equals(FlyStatus.DELAYED)) chHour.setDisable(true);
        chHour.getSelectionModel().select(Integer.valueOf(flight.getTimeDep().getHour()));


        ObservableList<Integer> minutes = FXCollections.observableArrayList(IntStream.iterate(0, i -> i + 5).limit(12).boxed().collect(Collectors.toList()));
        ComboBox<Integer> chMinutes = new ComboBox<>();
        // разрешим менять дату время на случай задержки рейса
        if (!flight.getStatus().equals(FlyStatus.SCHEDULED)
                || flight.getStatus().equals(FlyStatus.DELAYED)) chMinutes.setDisable(true);
        chMinutes.setItems(minutes);
        chMinutes.getSelectionModel().select(Integer.valueOf(flight.getTimeDep().getMinute()));


        // направление запретим менять
        ComboBox<Airport> tFrom = new ComboBox<>();
        tFrom.getSelectionModel().select(flight.getAreaSrc());
        tFrom.setPrefWidth(350);
        tFrom.setEditable(false);
        tFrom.setDisable(true);

        ComboBox<Airport> tTo = new ComboBox<>(airportsTo);
        tTo.getSelectionModel().select(flight.getAreaDestination());
        tTo.setEditable(false);
        tTo.setDisable(true);
        tTo.setPrefWidth(350);


        // разрешим менять время полета, контроль за человеком
        Spinner<Integer> tFlightTime = new Spinner<Integer>();
        tFlightTime.setDisable(true);
        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 10);
        valueFactory.setValue(flight.getFlightTime());
        tFlightTime.setValueFactory(valueFactory);
        TextFormatter timeFormater = new TextFormatter(valueFactory.getConverter(), valueFactory.getValue());
        tFlightTime.getEditor().setTextFormatter(timeFormater);
        valueFactory.valueProperty().bindBidirectional(timeFormater.valueProperty());
        tFlightTime.setValueFactory(valueFactory);




        // разрешим менять время полета, контроль за человеком
        Spinner<Integer> tFlightAddTime = new Spinner<Integer>();
        tFlightAddTime.setEditable(true);
        SpinnerValueFactory<Integer> valueFactoryAdd = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 10);
        valueFactoryAdd.setValue(flight.getAddTime());
        TextFormatter formatter = new TextFormatter(valueFactoryAdd.getConverter(), valueFactoryAdd.getValue());
        tFlightAddTime.getEditor().setTextFormatter(formatter);
        valueFactoryAdd.valueProperty().bindBidirectional(formatter.valueProperty());

        tFlightAddTime.setValueFactory(valueFactoryAdd);
        if (flight.getStatus().equals(FlyStatus.ARRIVED)
                || flight.getStatus().equals(FlyStatus.REGISTRATION)) tFlightAddTime.setEditable(false);


        // можно менять компанию
        ComboBox<Company> tCompany = new ComboBox<>(companiesFiltered);
        tCompany.getSelectionModel().select(flight.getCompany());
        if (flight.getStatus().equals(FlyStatus.ARRIVED)
                || flight.getStatus().equals(FlyStatus.REGISTRATION)) tCompany.setEditable(false);
        tCompany.setPrefWidth(350);

        // можно менять самолет
        ComboBox<Airplane> tAirplane = new ComboBox<>(airplanes);
        tAirplane.getSelectionModel().select(flight.getAirplane());
        // но нельзя менять когда уже регистрация
        if (flight.getStatus().equals(FlyStatus.ARRIVED)
            || flight.getStatus().equals(FlyStatus.REGISTRATION)) tAirplane.setEditable(false);
        tAirplane.setPrefWidth(350);

        // устанавливать новый статус
        ObservableList<FlyStatus> statuses = FXCollections.observableArrayList(FlyStatus.values());
        ComboBox<FlyStatus> tStatus = new ComboBox<>(statuses);
        tStatus.getSelectionModel().select(flight.getStatus());
        tStatus.setPrefWidth(350);

        GridPane grid = new GridPane();

        grid.add(new Label("Код"), 1, 1);
        grid.add(tCode, 2, 1);
        grid.add(new Label("Дата вылета"), 1, 2);
        grid.add(tDate, 2, 2);
        grid.add(new Label("Время"), 1, 3);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BASELINE_LEFT);
        hBox.setSpacing(5);
        hBox.getChildren().addAll(chHour, new Label("ч."), chMinutes, new Label("мин."));

        grid.add(hBox, 2, 3);

        grid.add(new Label("Аэропорт вылета"), 1, 4);
        grid.add(tFrom, 2, 4);

        grid.add(new Label("Аэропорт назначения"), 1, 5);
        grid.add(tTo, 2, 5);

        grid.add(new Label("Время полета"), 1, 6);
        grid.add(tFlightTime, 2, 6);

        grid.add(new Label("Добавочное Время полета"), 1, 7);
        grid.add(tFlightAddTime, 2, 7);


        grid.add(new Label("Статус"), 1, 8);
        grid.add(tStatus, 2, 8);

        grid.add(new Label("Компания"), 1, 9);
        grid.add(tCompany, 2, 9);
        grid.add(new Label("Тип самолета"), 1, 10);
        grid.add(tAirplane, 2, 10);


        grid.setPrefWidth(520);
        grid.setMinWidth(520);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.BASELINE_LEFT);
        dialog.getDialogPane().setContent(grid);

        final ButtonType buttonTypeOk = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        final ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);


        dialog.setResultConverter(new javafx.util.Callback<ButtonType, Flight>() {
            @Override
            public Flight call(ButtonType buttonType) {
                if (buttonType == buttonTypeOk) {
                    // вернем рейс, дату берем ту что была!!
                    Flight result = Flight.builder()
                            .code(flight.getCode())
                            .areaDestination(flight.getAreaDestination())
                            .areaSrc(flight.getAreaSrc())
                            .dateDep(flight.getDateDep())
                            .timeDep(flight.getTimeDep())
                            .flightTime(flight.getFlightTime())
                            .visible(true)
                            .addTime(tFlightAddTime.getValue())
                            .company(tCompany.getValue())
                            .airplane(tAirplane.getValue())
                            .status(tStatus.getValue()).build();

                    return result;
                }
                return null;
            }
        });

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(buttonTypeOk);

        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            // тут проверка времени созданий, если раскомментировать -
            // то не даст создавать рейсы в прошлом
            /*
            LocalTime time = LocalTime.of(chHour.getValue(), chMinutes.getValue());
            LocalDateTime dateTime = LocalDateTime.of(tDate.getValue(), time);

            if (dateTime.isBefore(LocalDateTime.now().plusHours(12))) {
                showAlert("Не корректное время!");
                event.consume();
                return;
            }*/
            if (tFrom.getValue().equals(tTo.getValue())) {
                showAlert("Аэропорты должны быть разные!");
                event.consume();
                return;
            }
        });

        // если что-то не выбрано - то не даст сохранить
        BooleanBinding booleanBind = Bindings.or(tCode.textProperty().isEmpty(),
                tCompany.valueProperty().isNull()).or(tAirplane.valueProperty().isNull())
                .or(tFlightTime.valueProperty().isEqualTo(0)).or(tStatus.valueProperty().isNull())
                .or(tFrom.valueProperty().isNull()).or(tTo.valueProperty().isNull()).or(tDate.valueProperty().isNull());


        btOk.disableProperty().bind(booleanBind);
        return dialog;

    }
    // диалог создания рейса
    // тут можно все))
    Dialog flightDialog() {
        final Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Авиарейс");
        dialog.setHeaderText("Добавление нового рейса");
        dialog.setResizable(false);

        TextField tCode = new TextField();
        tCode.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
        tCode.setPrefWidth(350);
        DatePicker tDate = new DatePicker();
        // этот блок закоментировали что бы можно было создавать рейсы в "прошлом"
        // если расскоментировать - то можно стоздавать рейсы только на будущую дату
        /*
        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item.isBefore(LocalDate.now())
                                ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                            }
                        };

                    }
                };
        tDate.setDayCellFactory(dayCellFactory);*/

        ObservableList<Integer> hours = FXCollections.observableArrayList(IntStream.rangeClosed(0, 23).boxed().collect(Collectors.toList()));
        ComboBox<Integer> chHour = new ComboBox<>();
        chHour.setItems(hours);

        ObservableList<Integer> minutes = FXCollections.observableArrayList(IntStream.iterate(0, i -> i + 5).limit(12).boxed().collect(Collectors.toList()));
        ComboBox<Integer> chMinutes = new ComboBox<>();
        chMinutes.setItems(minutes);


        ComboBox<Airport> tFrom = new ComboBox<>(airportsFrom);
        tFrom.setPrefWidth(350);

        ComboBox<Airport> tTo = new ComboBox<>(airportsTo);
        tTo.setPrefWidth(350);


        Spinner<Integer> tFlightTime = new Spinner<Integer>();
        tFlightTime.setEditable(true);
        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 10);
        tFlightTime.setValueFactory(valueFactory);
        TextFormatter formatter = new TextFormatter(valueFactory.getConverter(), valueFactory.getValue());
        tFlightTime.getEditor().setTextFormatter(formatter);
        valueFactory.valueProperty().bindBidirectional(formatter.valueProperty());

        ComboBox<Company> tCompany = new ComboBox<>(companiesFiltered);
        tCompany.setPrefWidth(350);

        ComboBox<Airplane> tAirplane = new ComboBox<>(airplanes);
        tAirplane.setPrefWidth(350);


        GridPane grid = new GridPane();

        grid.add(new Label("Код"), 1, 1);
        grid.add(tCode, 2, 1);
        grid.add(new Label("Дата вылета"), 1, 2);
        grid.add(tDate, 2, 2);
        grid.add(new Label("Время"), 1, 3);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BASELINE_LEFT);
        hBox.setSpacing(5);
        hBox.getChildren().addAll(chHour, new Label("ч."), chMinutes, new Label("мин."));

        grid.add(hBox, 2, 3);

        grid.add(new Label("Аэропорт вылета"), 1, 4);
        grid.add(tFrom, 2, 4);

        grid.add(new Label("Аэропорт назначения"), 1, 5);
        grid.add(tTo, 2, 5);

        grid.add(new Label("Время полета"), 1, 6);
        grid.add(tFlightTime, 2, 6);


        grid.add(new Label("Компания"), 1, 7);
        grid.add(tCompany, 2, 7);
        grid.add(new Label("Тип самолета"), 1, 8);
        grid.add(tAirplane, 2, 8);


        grid.setPrefWidth(520);
        grid.setMinWidth(520);
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.BASELINE_LEFT);
        dialog.getDialogPane().setContent(grid);

        final ButtonType buttonTypeOk = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        final ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);


        dialog.setResultConverter(new javafx.util.Callback<ButtonType, Flight>() {
            @Override
            public Flight call(ButtonType buttonType) {
                // если нажали сохранить то вернем рейс
                if (buttonType == buttonTypeOk) {
                    LocalTime time = LocalTime.of(chHour.getValue(), chMinutes.getValue());
                    Flight result = Flight.builder().
                            code(tCode.getText())
                            .dateDep(tDate.getValue())
                            .timeDep(time)
                            .areaSrc(tFrom.getValue())
                            .areaDestination(tTo.getValue())
                            .company(tCompany.getValue())
                            .airplane(tAirplane.getValue())
                            .flightTime(tFlightTime.getValue())
                            .status(FlyStatus.SCHEDULED)
                            .visible(true).build();
                    return result;
                }
                return null;
            }
        });

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(buttonTypeOk);

        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            // тут проверка времени созданий, если раскомментировать -
            // то не даст создавать рейсы в прошлом
            /*
            LocalTime time = LocalTime.of(chHour.getValue(), chMinutes.getValue());
            LocalDateTime dateTime = LocalDateTime.of(tDate.getValue(), time);

            if (dateTime.isBefore(LocalDateTime.now().plusHours(12))) {
                showAlert("Не корректное время!");
                event.consume();
                return;
            }*/
            if (tFrom.getValue().equals(tTo.getValue())) {
                showAlert("Аэропорты должны быть разные!");
                event.consume();
                return;
            }
        });

        BooleanBinding booleanBind = Bindings.or(tCode.textProperty().isEmpty(),
                tCompany.valueProperty().isNull()).or(tAirplane.valueProperty().isNull())
                .or(tFlightTime.valueProperty().isEqualTo(0))
                .or(tFrom.valueProperty().isNull()).or(tTo.valueProperty().isNull()).or(tDate.valueProperty().isNull());


        btOk.disableProperty().bind(booleanBind);
        return dialog;
    }
    // зупскает экспорт найденых рейсов в excel
    @FXML
    private void export(){
        progressBar.progressProperty().bind(exportService.progressProperty());
        reportStatus.textProperty().bind(exportService.messageProperty());
        exportService.startService(flights,dateFrom.getValue(),dateTo.getValue());

    }


}

