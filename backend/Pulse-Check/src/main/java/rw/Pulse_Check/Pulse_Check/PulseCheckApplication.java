package rw.Pulse_Check.Pulse_Check;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PulseCheckApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulseCheckApplication.class, args);
	}

}
