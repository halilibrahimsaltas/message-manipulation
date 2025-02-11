package com.message.message_manipulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class MessageManipulationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageManipulationApplication.class, args);
	}

}
