package sk.eventfindr.fsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventfindrApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventfindrApplication.class, args);
	}

}
