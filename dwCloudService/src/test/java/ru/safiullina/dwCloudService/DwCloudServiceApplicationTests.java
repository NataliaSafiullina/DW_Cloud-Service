package ru.safiullina.dwCloudService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.safiullina.dwCloudService.controller.AuthController;
import ru.safiullina.dwCloudService.dto.ErrorMessageResponse;
import ru.safiullina.dwCloudService.dto.LoginRequest;
import ru.safiullina.dwCloudService.dto.LoginResponse;
import ru.safiullina.dwCloudService.dto.SignupRequest;
import ru.safiullina.dwCloudService.entity.User;

// https://hamcrest.org/JavaHamcrest/tutorial
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Тесты с использованием testcontainers (10.5.1 TestContainers).
 * Цель протестировать ошибку 401 Unauthorized error и,
 * немного 200 Ок.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DwCloudServiceApplicationTests {

	/**
	 * TestRestTemplate — это специализированная версия клиентского шаблона RestTemplate,
	 * предназначенная специально для тестирования приложений на платформе Spring Boot.
	 * Она позволяет отправлять HTTP-запросы к контроллерам приложения и проверять возвращаемые данные.
	 */
	@Autowired
	private TestRestTemplate restTemplate;

	@Container
	private static final GenericContainer<?> cloudservice = new GenericContainer<>("cloudservice:1.0")
			.withExposedPorts(8080);

	@Autowired
	private AuthController authController;
	private static SignupRequest signupRequest;
	private static LoginRequest loginRequest;
	private static final String userName = "user";
	private static String token;

	private static final String baseUrl = "http://localhost:8080/cloud";


	@BeforeAll
	static void makeObjects() {
		// Пароль совпадает с именем пользователя для тестирования (чтобы не забывать пароль)
		signupRequest = new SignupRequest(userName, userName);
		loginRequest = new LoginRequest(userName, userName);
	}

	@Test
	void contextLoads() {
		assertThat(authController, notNullValue());
	}

	// Пользователь регистрируется
	@Disabled
	@Test
	void signupTest200() {
		String endpoint = "/signup";

		// Отправляем POST-запрос
		ResponseEntity<User> response = restTemplate.postForEntity(baseUrl + endpoint,
				signupRequest, User.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(userName, response.getBody().getLogin());
	}

	// Пользователь аутентифицируется
	@Test
	void loginTest200() {
		String endpoint = "/login";

		// Отправляем POST-запрос
		ResponseEntity<LoginResponse> response = restTemplate.postForEntity(baseUrl + endpoint,
				loginRequest, LoginResponse.class);

		token = response.getBody().getAuthToken();
		System.out.println(token);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getAuthToken());
	}
	@Test
	void loginTest400() {
		String endpoint = "/login";
		LoginRequest wrongLogin = new LoginRequest("not existing user", "not existing pass");

		// Отправляем POST-запрос
		ResponseEntity<ErrorMessageResponse> response = restTemplate.postForEntity(baseUrl + endpoint,
				wrongLogin, ErrorMessageResponse.class);

		System.out.println(response.getBody());

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat("Bad credentials", is(response.getBody().getMessage().trim()));
	}

	// Пользователь использует сервис.
	// Проверим ошибку аутентификации, так как остальное проверено Unit тестами.
	@Test
	void getFileListTest200 () {
		String endpoint = "/list";
		System.out.println(token);

		// Формируем заголовок запроса
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", "Bearer " + token);

		// Создаем объект HttpEntity с пустым телом и заданными заголовками
		HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

		// Отправляем GET-запрос
		ResponseEntity<String> response = restTemplate.exchange(
				baseUrl + endpoint + "?limit=10",
				HttpMethod.GET,
				httpEntity,
				String.class);

		System.out.println(response.getBody());

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
	}
	@Test
	void getFileListTest401 () {
		String endpoint = "/list";
		System.out.println(token);

		// Формируем заголовок запроса
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", "Bearer " + token + "errorTail");

		// Создаем объект HttpEntity с пустым телом и заданными заголовками
		HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

		// Отправляем GET-запрос
		ResponseEntity<ErrorMessageResponse> response = restTemplate.exchange(
				baseUrl + endpoint + "?limit=10",
				HttpMethod.GET,
				httpEntity,
				ErrorMessageResponse.class);

		System.out.println(response.getBody());

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat("Unauthorized error", is(response.getBody().getMessage().trim()));
	}
	@Test
	void postFileTest401 () {
		String endpoint = "/file";
		System.out.println(token);

		// Формируем заголовок запроса
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", "Bearer " + token + "errorTail");

		// Создаем объект HttpEntity с пустым телом и заданными заголовками
		HttpEntity<String> httpEntity = new HttpEntity<>("some body", headers);

		// Отправляем POST-запрос
		ResponseEntity<ErrorMessageResponse> response = restTemplate.exchange(
				baseUrl + endpoint + "?filename=file1.txt",
				HttpMethod.POST,
				httpEntity,
				ErrorMessageResponse.class);

		System.out.println(response.getBody());

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat("Unauthorized error", is(response.getBody().getMessage().trim()));
	}
	@Test
	void getFileTest401 () {
		String endpoint = "/file";
		System.out.println(token);

		// Формируем заголовок запроса
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", "Bearer " + token + "errorTail");

		// Создаем объект HttpEntity с пустым телом и заданными заголовками
		HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

		// Отправляем GET-запрос
		ResponseEntity<ErrorMessageResponse> response = restTemplate.exchange(
				baseUrl + endpoint + "?filename=file1.txt",
				HttpMethod.GET,
				httpEntity,
				ErrorMessageResponse.class);

		System.out.println(response.getBody());

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat("Unauthorized error", is(response.getBody().getMessage().trim()));
	}
	@Test
	void putFileTest401 () {
		String endpoint = "/file";
		System.out.println(token);

		// Формируем заголовок запроса
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", "Bearer " + token + "errorTail");

		// Создаем объект HttpEntity с пустым телом и заданными заголовками
		HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

		// Отправляем PUT-запрос
		ResponseEntity<ErrorMessageResponse> response = restTemplate.exchange(
				baseUrl + endpoint + "?filename=file1.txt",
				HttpMethod.PUT,
				httpEntity,
				ErrorMessageResponse.class);

		System.out.println(response.getBody());

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat("Unauthorized error", is(response.getBody().getMessage().trim()));
	}
	@Test
	void deleteFileTest401 () {
		String endpoint = "/file";
		System.out.println(token);

		// Формируем заголовок запроса
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", "Bearer " + token + "errorTail");

		// Создаем объект HttpEntity с пустым телом и заданными заголовками
		HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

		// Отправляем DELETE-запрос
		ResponseEntity<ErrorMessageResponse> response = restTemplate.exchange(
				baseUrl + endpoint + "?filename=file1.txt",
				HttpMethod.DELETE,
				httpEntity,
				ErrorMessageResponse.class);

		System.out.println(response.getBody());

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat("Unauthorized error", is(response.getBody().getMessage().trim()));
	}


	// Пользователь выходит
	@Test
	void logoutTest200() {
		String endpoint = "/logout";
		System.out.println(token);

		// Формируем заголовок запроса
		HttpHeaders headers = new HttpHeaders();
		headers.add("auth-token", "Bearer " + token);

		// Создаем объект HttpEntity с пустым телом и заданными заголовками
		HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

		// Отправляем POST-запрос
		ResponseEntity<String> response = restTemplate.exchange(
				baseUrl + endpoint,
				HttpMethod.POST,
				httpEntity,
				String.class);

		System.out.println(response.getBody());

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat("Success logout", is(response.getBody().trim()));
	}
}
