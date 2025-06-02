# Дипломная работа «Облачное хранилище»

Описание и требования к проекту: [задание](./files/cloudservice.md)

[Введение](#введение)

[Запуск приложения](#запуск-приложения)

[Работа с приложением](#работа-с-приложением)


## Введение

**Задача состояла из двух блоков:**
1. Реализовать работу с авторизацией и токенами
2. Реализовать методы работы с файлами

**Стек технологий:**
1. MySQL - СУБД
2. Spring Boot 
3. Spring Security - фреймворк для работы с аутентификацией и авторизацией
4. JWT - стандарт для работы с токенами

**Примечания по работе безопасности приложения:**
1. Приложение хранит только авторизационные данные пользвателя в БД и не хранит токены.
2. Токен хранится на стороне клиента, приложение понимает по информации из токена, какой пользователь зашел.
3. Токены имеют срок жизни (установлено 4 часа).

**Краткая схема приложения:**

![img.png](screenshots/app_schema.png)


## Запуск приложения

Приложение использует порт 8080.

### 1. Упаковать проект и получить jar-файл

![img.png](screenshots/make_package.png)


### 2. Собрать docker образ

В терминале, находясь в корне проекта, выполнить команду:
```java
docker build -t cloudservice:latest -t cloudservice:1.0 . 
```

### 3. Запустить контейнер

В терминале, находясь в корне проекта, выполнить команду:
```java
docker-compose up -d
```
Терминал выведет сообщение, что контейнер запущен:
```java
[+] Running 3/3
 ✔ Network dwcloudservice_default  Created          0.5s 
 ✔ Container cloud_db              Started          2.8s 
 ✔ Container cloud_service         Started          3.6s 
```


## Работа с приложением
Приложение реализует методы, описанные в [YAML](./files/CloudServiceSpecification.yaml)
### FRONT
Сервис предоставляет REST-интерфейс для интеграции с [FRONT](./netology-diplom-frontend).

### Postman

Коллекция запросов postman [здесь](./files/DW_Cloud_Service_postman_collection.json)

#### 1. Регистрация пользователя (не описана в yaml, сделана для удобства тестирования)
```java
curl --location 'http://localhost:8080/cloud/signup' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=10E4FBEAD21E979D812374EA94FB03EE' \
--data '{
  "login": "user",
  "password": "user"
}'
```
#### 2. Авторизация пользователя
```java
curl --location 'http://localhost:8080/cloud/login' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=10E4FBEAD21E979D812374EA94FB03EE' \
--data '{
  "login": "user",
  "password": "user"
}'
```
#### 3. Вывод списка файлов
```java
curl --location 'http://localhost:8080/cloud/list?limit=10' \
--header 'auth-token: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzQ4NzY1NjkyLCJleHAiOjE3NDg3ODAwOTJ9.eh4vkYfZn5NTperLMCnJS46a5kBuTCwxH6TaScCafe0' \
--header 'Cookie: JSESSIONID=10E4FBEAD21E979D812374EA94FB03EE'
```
#### 4. Добавление файла
```java
curl --location 'http://localhost:8080/cloud/file?filename=file11.txt' \
--header 'auth-token: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzQ4NzY1NjkyLCJleHAiOjE3NDg3ODAwOTJ9.eh4vkYfZn5NTperLMCnJS46a5kBuTCwxH6TaScCafe0' \
--header 'Cookie: JSESSIONID=10E4FBEAD21E979D812374EA94FB03EE' \
--form 'file=@"postman-cloud:///1f0394a7-cfd3-4ee0-90bc-ea953125fdb9"' \
--form 'hash="111"'
```
#### 5. Удаление файла
```java
curl --location --request DELETE 'http://localhost:8080/cloud/file?filename=file3.txt' \
--header 'auth-token: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzQ4NzY1NjkyLCJleHAiOjE3NDg3ODAwOTJ9.eh4vkYfZn5NTperLMCnJS46a5kBuTCwxH6TaScCafe0' \
--header 'Cookie: JSESSIONID=10E4FBEAD21E979D812374EA94FB03EE' \
--data ''
```
#### 6. Изменение файла
```java
curl --location --request PUT 'http://localhost:8080/cloud/file?filename=file11.txt' \
--header 'auth-token: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzQ4NzY1NjkyLCJleHAiOjE3NDg3ODAwOTJ9.eh4vkYfZn5NTperLMCnJS46a5kBuTCwxH6TaScCafe0' \
--header 'Cookie: JSESSIONID=10E4FBEAD21E979D812374EA94FB03EE' \
--form 'file=@"postman-cloud:///1f03bc8b-9b99-4290-949b-9666ac63876d"'
```
#### 7. Получение файла
```java
curl --location 'http://localhost:8080/cloud/file?filename=file11.txt' \
--header 'auth-token: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzQ4NzY1NjkyLCJleHAiOjE3NDg3ODAwOTJ9.eh4vkYfZn5NTperLMCnJS46a5kBuTCwxH6TaScCafe0' \
--header 'Cookie: JSESSIONID=10E4FBEAD21E979D812374EA94FB03EE'
```
#### 8. Выход пользователя
```java
curl --location --request POST 'http://localhost:8080/cloud/logout' \
--header 'auth-token: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzQ4NzY1NjkyLCJleHAiOjE3NDg3ODAwOTJ9.eh4vkYfZn5NTperLMCnJS46a5kBuTCwxH6TaScCafe0' \
--header 'Cookie: JSESSIONID=10E4FBEAD21E979D812374EA94FB03EE' \
--data ''
```