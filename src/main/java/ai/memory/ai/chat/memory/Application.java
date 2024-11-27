package ai.memory.ai.chat.memory;

import ai.memory.ai.chat.memory.services.DataBaseService;
import ai.memory.ai.chat.memory.services.SystemPromptFileReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

	private final DataBaseService dataBaseService;

	public Application(DataBaseService dataBaseService) {
		this.dataBaseService = dataBaseService;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		dataBaseService.purgeData();
	}
}
