package nl.bvsit.coworker;

import nl.bvsit.coworker.service.FileStorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CoworkerApplication implements CommandLineRunner {
	//See https://github.com/eugenp/tutorials/tree/master/spring-boot-rest
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Autowired(required = false) //Prevent exception in @DataJpaTest
	FileStorageService fileStorageService;

	public static void main(String[] args) {
		SpringApplication.run(CoworkerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if(fileStorageService!=null) fileStorageService.init(); //Create folder for uploads if not exists
	}
}
