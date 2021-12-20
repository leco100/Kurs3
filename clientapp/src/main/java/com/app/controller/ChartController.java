package com.app.controller;

import com.app.model.Airport;
import com.app.model.Flight;
import com.app.service.FlightService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import lombok.SneakyThrows;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.collections4.map.LinkedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * контроллер вывода круговой диаграммы top 5 аэропортов по кол-ву рейсов
 * учитываются как аэропорты вылета, так и прилета
 */
@Component
@FxmlView("chart.fxml")
public class ChartController  implements Initializable {
    @FXML
    private PieChart pieChart;
    @FXML
    private DatePicker from;

    @FXML
    private DatePicker to;

    @Autowired
    FlightService flightService;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // установим диапазон 30 дней - 15 до и 15 после текущей даты
        from.setValue(LocalDate.now().minusDays(15));
        to.setValue(LocalDate.now().plusDays(15));

    }
    @FXML
    private void apply(){
        Platform.runLater(new Runnable() {
            // загрузим данные с сервера
            @SneakyThrows
            @Override
            public void run() {
                List<Flight> flights = flightService.findByPeriod(from.getValue(), to.getValue());
                // сгруппируем по аэропортам
                Map<Airport,Long> mapSrc = flights.stream().collect(Collectors.groupingBy(Flight::getAreaSrc, Collectors.counting()));
                Map<Airport,Long> mapDst = flights.stream().collect(Collectors.groupingBy(Flight::getAreaDestination, Collectors.counting()));
                Map<String,Long> result = new HashMap<>();
                // посчитаем итоги
                mapSrc.forEach((a,c)->{
                    long sum = c+mapDst.getOrDefault(a,0L);
                    result.put(a.getName(),sum);
                });
                // отстортируем  в обратном порядке по кол-ву рейсов
                LinkedHashMap<String, Long> reverseSortedMap = new LinkedHashMap<>();
                result.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
                // заполним диаграму
                pieChart.getData().clear();
                reverseSortedMap.entrySet().stream().limit(5).forEach((e)->{
                     PieChart.Data slice = new PieChart.Data(e.getKey(), e.getValue());
                     pieChart.getData().add(slice);
                 });

                 //  укажем как выводить метки
                pieChart.getData().forEach(data ->
                        data.nameProperty().bind(
                                Bindings.concat(
                                        data.getName(), "- ", String.format("%.0f", data.pieValueProperty().getValue())
                                )
                        )
                );

            }
        });
    }
}
