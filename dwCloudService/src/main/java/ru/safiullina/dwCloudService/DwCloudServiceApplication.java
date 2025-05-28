package ru.safiullina.dwCloudService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DwCloudServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DwCloudServiceApplication.class, args);
	}

}

// TODO: поменять properties на yaml
// TODO: Код покрыт unit-тестами с использованием mockito.
// TODO: Добавлены интеграционные тесты с использованием testcontainers
