package com.wpn.personallibrarytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PersonalLibraryTrackerApplication {
	public static void main(String[] args) {
		SpringApplication.run(PersonalLibraryTrackerApplication.class, args);
	}

}
