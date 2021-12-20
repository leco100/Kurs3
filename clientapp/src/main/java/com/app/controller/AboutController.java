package com.app.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

/**
 * контроллер о программе
 */
@Component
@FxmlView("about.fxml")
public class AboutController implements Initializable {

    @Value("classpath:about.html")
    Resource resourceFile;

    @FXML
    WebView webView;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("initialize");
        try {
            // загрузим из ресурсов файл about.html
            // в нем запись о программе
            WebEngine webEngine = webView.getEngine();
            webEngine.load(resourceFile.getURL().toExternalForm());


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
