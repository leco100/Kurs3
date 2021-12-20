package com.app.controller;

import com.app.config.ApiConfig;
import com.app.model.*;
import com.app.service.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 *  главное окно программы, отображает табло, позволеят
 *  вызвать другие формы из меню
 */
@Component
@FxmlView("main.fxml")
@Configuration
@PropertySource("file:./server.conf")
public class MainController implements Initializable {
    @Value("${air.code}")
    private String airCode;

    @FXML
    Label labelInfo;

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    ApiConfig apiConfig;
    @Autowired
    AirportService airportService;

    @Autowired
    FlightService flightService;
    @Autowired
    CountryService countryService;


    @FXML
    private ComboBox<Country> country;

    @FXML
    private ComboBox<String> city;

    @FXML
    private ComboBox<Airport> airport;


    @FXML
    private TableView<Flight> tabDeparture;
    @FXML
    private TableView<Flight> tabArrival;

    // справочник стран
    ObservableList<Country> countries = FXCollections.observableArrayList();
    // cправочник аэропортов
    ObservableList<Airport> airports = FXCollections.observableArrayList();

    // офильтрованые аэропорты
    FilteredList<Airport> airportsFrom;// = new FilteredList<>(airports);
    // города
    ObservableList<String> citiesFrom = FXCollections.observableArrayList();

    // cписок прилетающих
    ObservableList<Flight> arrivals = FXCollections.observableArrayList();
    // список отлетающих
    ObservableList<Flight> departures = FXCollections.observableArrayList();

    // планировщик заданий
    ScheduledExecutorService scheduledExecutorService;

    // проперти "домашнего" аэропорта
    SimpleObjectProperty<Airport> homeAir = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // фильтр аэропортов
        airportsFrom  = new FilteredList<>(airports);
        // "домашний" аэропорт, добавим ему слушатель изменений
        homeAir.addListener(new ChangeListener<Airport>() {
            @Override
            public void changed(ObservableValue<? extends Airport> observable, Airport oldValue, Airport newValue) {
                labelInfo.setText("Табло показывает рейсы по аэропорту " + newValue.getName());
                // обновим файл
                apiConfig.updateProperty("air.code",newValue.getCode());
                // применим фильтр
                applyFilter();
            }
        });
        try {
            // загрузим аэропорт с сервера по коду из файла
            Airport air = airportService.findByCode(airCode);
            // "положим" в его в homeAir
            homeAir.setValue(air);

        } catch (Exception e) {
            showAlert("Аэропорт не найден!");
        }

        // загрузим отдельным потоком справочники стран и аэропортов
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loadCountries();
                loadAirports();
                airport.setItems(airportsFrom);
            }
        });

        // инициализируем таблицы
       initTableArrival();
       initTableDep();

       // назначим слушатель если что-то выбрали в combox "страна"
        country.valueProperty().addListener(new ChangeListener<Country>() {
            @Override
            public void changed(ObservableValue<? extends Country> observable, Country oldValue, Country newValue) {
                // отфильтруем аэрпорты согласно стране
                airportsFrom.setPredicate(airportPredicate(newValue));
                // сформируем список городов
                List<String> cities = airportsFrom.stream().map(a -> a.getCity()).distinct().sorted().collect(Collectors.toList());
                // загрузим его "наблюдаемый" список
                citiesFrom.clear();
                citiesFrom.addAll(cities);
                // укажем что combox "Город" должен брать значения из нашего списка
                city.setItems(citiesFrom);
                // очистим выбраный аэропорт
                airport.getSelectionModel().clearSelection();
                airport.getSelectionModel().select(null);
                airport.valueProperty().set(null);
                airport.setValue(null);

            }
        });
        // слушатель выбора города
        city.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // если ничего не выбрали - то все покажем
                if (newValue == null) airportsFrom.setPredicate(p -> true);
                // офильтруем аэропорты по городу
                airportsFrom.setPredicate(p -> {
                    if (p == null) return true;
                    if (newValue == null) return true;
                    return p.getCity().contains(newValue);
                });

            }
        });
        // слушаетель выбора аэропорта
        airport.valueProperty().addListener(new ChangeListener<Airport>() {
            @Override
            public void changed(ObservableValue<? extends Airport> observable, Airport oldValue, Airport newValue) {
                // если что-то выбрали - то укажем это homeAir
                if (newValue != null)
                    homeAir.setValue(newValue);
            }
        });


    }


    // инизиализация таблицы отлетов
    public void initTableDep() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("hh:mm");


        TableColumn<Flight, String> codeColumn = new TableColumn<Flight, String>("Рейс");
        codeColumn.setCellValueFactory(new PropertyValueFactory<Flight, String>("code"));
        codeColumn.setPrefWidth(100);
        tabDeparture.getColumns().add(codeColumn);

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
        tabDeparture.getColumns().add(timeDepColumn);

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

        statusColumn.setPrefWidth(100);
        tabDeparture.getColumns().add(statusColumn);




        TableColumn<Flight, Airport> areaToColumn = new TableColumn<>("Аэророрт назначения");
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

        areaToColumn.setPrefWidth(100);
        tabDeparture.getColumns().add(areaToColumn);

        TableColumn<Flight, Airport> countryToColumn = new TableColumn<>("Страна назначения");
        countryToColumn.setCellValueFactory(new PropertyValueFactory<Flight, Airport>("areaDestination"));
        countryToColumn.setCellFactory(column -> new TableCell<Flight, Airport>() {
            @Override
            protected void updateItem(Airport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getCountry().getName());
                }
            }
        });

        countryToColumn.setPrefWidth(100);
        tabDeparture.getColumns().add(countryToColumn);

        TableColumn<Flight, Airport> cityToColumn = new TableColumn<>("Город назначения");
        cityToColumn.setCellValueFactory(new PropertyValueFactory<Flight, Airport>("areaDestination"));
        cityToColumn.setCellFactory(column -> new TableCell<Flight, Airport>() {
            @Override
            protected void updateItem(Airport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getCity());
                }
            }
        });

        cityToColumn.setPrefWidth(250);
        tabDeparture.getColumns().add(cityToColumn);

        TableColumn<Flight, Integer> flightColumn = new TableColumn<>("Время в полете, мин");
        flightColumn.setCellValueFactory(new PropertyValueFactory<Flight, Integer>("flightTime"));

        flightColumn.setPrefWidth(250);
        tabDeparture.getColumns().add(flightColumn);

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
        tabDeparture.getColumns().add(companyColumn);

        TableColumn<Flight, Airplane> airplaneColumn = new TableColumn<>("Самолет");
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
        tabDeparture.getColumns().add(airplaneColumn);
        tabDeparture.setItems(departures);


    }

    // инизиализация таблицы прилетов
    public void initTableArrival() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("hh:mm");


        TableColumn<Flight, String> codeColumn = new TableColumn<Flight, String>("Рейс");
        codeColumn.setCellValueFactory(new PropertyValueFactory<Flight, String>("code"));
        codeColumn.setPrefWidth(100);
        tabArrival.getColumns().add(codeColumn);



        TableColumn<Flight, LocalTime> timeDepColumn = new TableColumn<>("Время приземления");
        timeDepColumn.setCellValueFactory(new PropertyValueFactory<Flight, LocalTime>("timeArrival"));
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
        tabArrival.getColumns().add(timeDepColumn);

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

        statusColumn.setPrefWidth(100);
        tabArrival.getColumns().add(statusColumn);


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
        tabArrival.getColumns().add(areaFromColumn);

        TableColumn<Flight, Airport> countryFromColumn = new TableColumn<>("Страна вылета");
        countryFromColumn.setCellValueFactory(new PropertyValueFactory<Flight, Airport>("areaSrc"));
        countryFromColumn.setCellFactory(column -> new TableCell<Flight, Airport>() {
            @Override
            protected void updateItem(Airport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getCountry().getName());
                }
            }
        });

        countryFromColumn.setPrefWidth(250);
        tabArrival.getColumns().add(countryFromColumn);

        TableColumn<Flight, Airport> cityFromColumn = new TableColumn<>("Город вылета");
        cityFromColumn.setCellValueFactory(new PropertyValueFactory<Flight, Airport>("areaSrc"));
        cityFromColumn.setCellFactory(column -> new TableCell<Flight, Airport>() {
            @Override
            protected void updateItem(Airport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getCity());
                }
            }
        });

        cityFromColumn.setPrefWidth(250);
        tabArrival.getColumns().add(cityFromColumn);



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
        tabArrival.getColumns().add(companyColumn);

        TableColumn<Flight, Airplane> airplaneColumn = new TableColumn<>("Самолет");
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
        tabArrival.getColumns().add(airplaneColumn);
        tabArrival.setItems(arrivals);

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

    // обработка меню Страны
    @FXML
    private void actionCountry() {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(CountryController.class);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Справочник стран");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // обработка меню Компании
    @FXML
    private void actionCompany() {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(CompanyController.class);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Справочник компаний");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // обработка меню "Модели самолетов
    @FXML
    private void actionAirplane() {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(AirplaneController.class);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Справочник моделей самолетов");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // вызов меню Аэропорты

    @FXML
    private void actionAirport() {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(AirportController.class);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Справочник аэропортов");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // вызов справочника авиарейсов
    @FXML
    private void actionFlight() {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(FlightController.class);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Авиарейсы");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // вызов диаграммы TOP5
    @FXML
    private void actionChart() {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(ChartController.class);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("ТОП-5");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // о программе
    @FXML
    private void actionAbout() {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(AboutController.class);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("О программе");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    // загрузка стран
    private void loadCountries() {
        try {
            List<Country> list = countryService.findAll();
            countries.clear();
            countries.addAll(list);
            country.setItems(countries);
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    // загрузка аэропортов
    private void loadAirports() {
        try {
            List<Airport> list = airportService.findAll();
            airports.clear();
            airports.addAll(list);
            List<String> cities = list.stream().map(a -> a.getCity()).distinct().collect(Collectors.toList());
            citiesFrom.clear();
            citiesFrom.addAll(cities);
            cities.clear();

        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    // предикат фильтра аэропорта по стране
    private Predicate<Airport> airportPredicate(Country country) {
        return item -> {
            if (country == null) return true;
            return item.getCountry().equals(country);
        };
    }


    // применим фильтр
    private void applyFilter() {
        // если установили null  то просто выйдем
        if (homeAir.getValue() == null) return;
        // если планировщик создан и запущен -то остановим его
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown())
            scheduledExecutorService.shutdownNow();
        // полуем "доманий" аэропорт
        Airport newValue = homeAir.getValue();
        if (newValue != null) {
            // создадим новый планировщик на 2 потока одновременно
            scheduledExecutorService =
                    Executors.newScheduledThreadPool(2);
            // добавим задачи, которые будут выполняться раз в 1 минуту
            scheduledExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // загрузка прилетов
                                List<Flight> list = flightService.findByAirportDstAndDate(newValue, LocalDate.now());
                                list.sort((f1, f2) -> {
                                    LocalDateTime t1 = LocalDateTime.of(f1.getDateArrival(), f1.getTimeArrival());
                                    LocalDateTime t2 = LocalDateTime.of(f2.getDateArrival(), f2.getTimeArrival());
                                    return t1.compareTo(t2);
                                });
                                arrivals.clear();
                                arrivals.addAll(list);
                                tabArrival.setItems(arrivals);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },0,
                    1, TimeUnit.MINUTES);
            scheduledExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // загрузка отлетов
                                List<Flight> list = flightService.findByAirportSrcAndDate(newValue, LocalDate.now());
                                list.sort((f1, f2) -> {
                                    LocalDateTime t1 = LocalDateTime.of(f1.getDateDep(), f1.getTimeDep());
                                    LocalDateTime t2 = LocalDateTime.of(f2.getDateDep(), f2.getTimeDep());
                                    return t1.compareTo(t2);
                                });
                                departures.clear();
                                departures.addAll(list);
                                tabDeparture.setItems(departures);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    0,
            1, TimeUnit.MINUTES);
        }
    }




}

