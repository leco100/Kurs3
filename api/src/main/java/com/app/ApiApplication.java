package com.app;

import java.util.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication

public class ApiApplication {
	private final static Logger log = Logger.getLogger(ApiApplication.class.getName());
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
		log.info("start api..");
	}

}


