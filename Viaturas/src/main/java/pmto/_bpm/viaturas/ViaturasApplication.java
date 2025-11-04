package pmto._bpm.viaturas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ViaturasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ViaturasApplication.class, args);
	}

}
