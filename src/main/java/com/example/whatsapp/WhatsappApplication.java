package com.example.whatsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WhatsappApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatsappApplication.class, args);
	}

}