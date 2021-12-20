package com.app.controller;

import com.app.config.ApiConfig;
import com.app.exception.NotFoundException;
import com.app.model.Company;
import com.app.model.Country;
import com.app.service.CompanyService;
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
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
@FxmlView("company.fxml")
public class CompanyController implements Initializable {

    @Autowired
    ApiConfig apiConfig;

    @Autowired
    CompanyService service;
    @Autowired
    CountryService countryService;

    boolean isAdmin = false;

    ObservableList<Country> countries = FXCollections.observableArrayList();
    ObservableList<Company> companies = FXCollections.observableArrayList();

    @FXML
    TextField  tFilter;

    // фильтрованные данные
    FilteredList<Company> filteredData;

    @FXML
    ComboBox<Country> country;



    @FXML
    TableView<Company> tabCompany;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
         isAdmin = apiConfig.getUser().getRole().getName().equals("ROLE_ADMIN");
         loadCountries(null);
         initTab();

         loadCompanies(null);
    }

    private void loadCompanies(Country country) {
        Company company = tabCompany.getSelectionModel().getSelectedItem();
        companies.clear();
        try {
            boolean all = country==null || country.getCode().equals("ALL");
            List<Company> list = all ? service.findAll():service.findByCountry(country.getCode());
            if (!list.isEmpty()){
                companies.addAll(list);
                if (company !=null && countries.contains(company))
                    tabCompany.getSelectionModel().select(company);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTab(){
        // cоздадим колонки согласно типам данных
        TableColumn<Company, String> nameColumn = new TableColumn<Company, String>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<Company, String>("name"));
        nameColumn.setPrefWidth(100);
        tabCompany.getColumns().add(nameColumn);

        TableColumn<Company, String> emailColumn = new TableColumn<Company, String>("email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<Company, String>("email"));
        emailColumn.setPrefWidth(150);
        tabCompany.getColumns().add(emailColumn);

        TableColumn<Company, String> phoneColumn = new TableColumn<Company, String>("Телефон");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<Company, String>("phone"));
        phoneColumn.setPrefWidth(150);
        tabCompany.getColumns().add(phoneColumn);

        TableColumn<Company, String> addressColumn = new TableColumn<Company, String>("Адрес");
        addressColumn.setCellValueFactory(new PropertyValueFactory<Company, String>("nameEng"));
        addressColumn.setPrefWidth(150);
        tabCompany.getColumns().add(addressColumn);

        TableColumn<Company, Country> countryColumn = new TableColumn<>("Страна");
        countryColumn.setCellValueFactory(new PropertyValueFactory<Company, Country>("country"));

        countryColumn.setCellFactory(column -> new TableCell<Company, Country>() {
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

        tabCompany.getColumns().add(countryColumn);

        // если зашли под админом - то покажем кто последний правил
        if (isAdmin) {
            TableColumn<Company, Boolean> visibleColumn = new TableColumn<Company, Boolean>("Видимый");
            //visibleColumn.setCellValueFactory(new PropertyValueFactory<Company, Boolean>("visible"));
            visibleColumn.setCellValueFactory(c->{
                BooleanProperty active = new SimpleBooleanProperty(c.getValue().isVisible());
                return active;
            });
            visibleColumn.setCellFactory(column -> new CheckBoxTableCell<>());
            tabCompany.getColumns().add(visibleColumn);
        }


        tabCompany.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // создадим контекстное меню и добавим обработчки
        ContextMenu contextMenu = new ContextMenu();
        if (isAdmin){
            MenuItem itemAdd = new MenuItem("Добавить");
            itemAdd.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Optional<Company> result = companyDialog(null).showAndWait();
                    if (result.isPresent()){
                        Company company = result.get();
                        try {
                            company= service.save(company);
                            companies.add(company);
                            tabCompany.getSelectionModel().select(company);
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
                    Company selected = tabCompany.getSelectionModel().getSelectedItem();
                    System.out.println(selected.isVisible());
                    if (selected==null) return;
                    Optional<Company> result = companyDialog(selected).showAndWait();
                    int indx = companies.indexOf(selected);
                    if (result.isPresent()){
                        Company company = result.get();
                        try {
                            company= service.save(company);
                            companies.remove(indx);
                            companies.add(indx,company);
                            tabCompany.getSelectionModel().select(company);
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
                    Company selected = tabCompany.getSelectionModel().getSelectedItem();
                    if (selected==null) return;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Удаление записи");
                    alert.setHeaderText("Удаление "+selected.getName());
                    alert.setContentText("Удалить эту компанию?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        try {
                            int indx = companies.indexOf(selected);
                            service.delete(selected.getId());
                            try {
                                service.findById(selected.getId());
                            }catch (NotFoundException e) {
                                    companies.remove(indx);
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
                    Company company = tabCompany.getSelectionModel().getSelectedItem();
                    if (company==null) return;
                    boolean isVisible = !company.isVisible();
                    company.setVisible(isVisible);

                        try {
                            int indx = companies.indexOf(company);
                            company= service.save(company);
                            companies.remove(indx);
                            companies.add(indx,company);
                            tabCompany.getSelectionModel().select(indx);
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
        tabCompany.setContextMenu(contextMenu);

        // создадим фильтрованые список на основе списка стран
        filteredData = new FilteredList<>(companies);
        // отсортируем фильтрованые данные
        SortedList<Company> sortedData = new SortedList<>(filteredData);
        // согласно выбраной колонке в таблцие
        sortedData.comparatorProperty().bind(tabCompany.comparatorProperty());
        // и укажем что таблица будет показывать отфильтрованые и отсортированые данные
        tabCompany.setItems(sortedData);
        // вызовем сортировку
        tabCompany.sort();

    }

    /**
     * загружает список стран с сервера
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

    Dialog companyDialog(Company company) {
        final Dialog<Company> dialog = new Dialog<>();
        dialog.setTitle("Компания");
        if (company==null)
            dialog.setHeaderText("Добавление новой компании");
        else dialog.setHeaderText("Редактирование  компании");
        dialog.setResizable(false);




        TextField tName = new TextField();
        tName.setPrefWidth(350);

        if (company!=null){
            tName.setText(company.getName());
        }
        TextField tEmail = new TextField();
        tEmail.setPrefWidth(350);
        if (company!=null){
            tEmail.setText(company.getEmail());
        }

        TextField tPhone = new TextField();
        tPhone.setPrefWidth(350);
        if (company!=null){
            tPhone.setText(company.getPhone());
        }


        TextField tAddress = new TextField();
        tAddress.setPrefWidth(350);
        if (company!=null){
            tAddress.setText(company.getAddress());
        }

        ComboBox<Country>  tCountry  = new ComboBox<>(countries);
        tCountry.setPrefWidth(350);

        if (company==null &&  country.getValue()!=null){
            tCountry.getSelectionModel().select(country.getValue());
        }else if (company!=null){
            tCountry.getSelectionModel().select(company.getCountry());
        }



        GridPane grid = new GridPane();
        grid.add(new Label("Название"), 1, 1);
        grid.add(tName, 2, 1);
        grid.add(new Label("Email"), 1, 2);
        grid.add(tEmail, 2, 2);
        grid.add(new Label("Телефон"), 1, 3);
        grid.add(tPhone, 2, 3);
        grid.add(new Label("Адрес"), 1, 4);
        grid.add(tAddress, 2, 4);
        grid.add(new Label("Страна"), 1, 5);
        grid.add(tCountry, 2, 5);
        grid.setPrefWidth(500);
        grid.setMinWidth(500);
        grid.setVgap(10);grid.setHgap(10);
        dialog.getDialogPane().setContent(grid);

        final ButtonType buttonTypeOk = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        final ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);


        dialog.setResultConverter(new javafx.util.Callback<ButtonType, Company>() {
            @Override
            public Company call(ButtonType buttonType) {
                if (buttonType == buttonTypeOk) {

                    Company result =  Company.builder().name(tName.getText())
                            .email(tEmail.getText())
                            .phone(tPhone.getText())
                            .address(tAddress.getText())
                            .country(tCountry.getValue())
                            .visible(true).build();
                    if (company!=null) result.setId(company.getId());
                    return result;
                }
                return null;
            }
        });

        final  Country allCompany = countries.get(0);
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(buttonTypeOk);
        final Pattern emailPattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*@[a-zA-Z][a-zA-Z0-9_]+(\\.[a-zA-Z][a-zA-Z0-9_]*)+");

        BooleanBinding emailInvalid = Bindings.createBooleanBinding(() ->
                !emailPattern.matcher(tEmail.getText()).matches(), tEmail.textProperty());
        BooleanBinding booleanBind = Bindings.or(tName.textProperty().isEmpty(),
        tEmail.textProperty().isEmpty()).or(tPhone.textProperty().isEmpty())
                .or(tAddress.textProperty().isEmpty()).or(tCountry.valueProperty().isNull())
                .or(tCountry.valueProperty().isEqualTo(allCompany)).or(emailInvalid);

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
        Country selectedCountry = country.getValue();
        loadCountries(selectedCountry);
        loadCompanies(country.getValue());
        filteredData.setPredicate(createPredicate(tFilter.getText().trim()));
    }

    @FXML
    private void applyFilterName(){
        filteredData.setPredicate(createPredicate(tFilter.getText().trim()));
    }

    private Predicate<Company> createPredicate(final String str) {
        // для каждой записи будет применятся фильтр, и будет возрващаеть true
        // если запись подпадает под условия фильтра

        return item -> {
            if (str==null || str.isEmpty()) return true;

            boolean isName = str==null? true : item.getName().toLowerCase().contains(str.toLowerCase());
            boolean isPhone = str==null? true : item.getPhone().toLowerCase().contains(str.toLowerCase());
            boolean isEmail = str==null? true : item.getEmail().toLowerCase().contains(str.toLowerCase());
            boolean isAddress = str==null? true : item.getAddress().toLowerCase().contains(str.toLowerCase());
            return  isName || isPhone || isEmail ||isAddress;
        };
    }


}
