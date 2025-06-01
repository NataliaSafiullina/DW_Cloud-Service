package ru.safiullina.dwCloudService.service;

import ru.safiullina.dwCloudService.entity.File;
import ru.safiullina.dwCloudService.entity.User;
import ru.safiullina.dwCloudService.exeption.ErrorInputDataException;
import ru.safiullina.dwCloudService.exeption.ServiceException;
import ru.safiullina.dwCloudService.repository.FileRepository;
import ru.safiullina.dwCloudService.utils.ResponseText;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Unit-тесты (6.6.5 JUnit, 6.7 Mockito)
 * Протестируем сервисы (Service).
 * Вместо работы с БД (Repository) используем заглушки (mock).
 */
@SpringBootTest
@ActiveProfiles("test")
class ServiceTest {

    @Autowired
    private JwtService jwtService;
    private final FileRepository fileRepository = Mockito.mock(FileRepository.class);
    private final UserService userService = Mockito.mock(UserService.class);
    private final FileService fileService = new FileService(userService, fileRepository, jwtService);

    private static final String userName = "user";
    private static final String fileName = "some file";
    private static final MultipartFile fileContent = new MockMultipartFile(
            "file",  // имя файла (можно использовать что-то другое)
            "example.txt", // имя файла (например, "example.txt")
            "application/json", // MIME-тип (можно определить самостоятельно)
            "some content".getBytes()
    );
    private static final String hash = "some hash";
    private static final String token = "some token";
    private static User user;
    private static File fileEntity;
    private static final int limit = 1;

    @BeforeAll
    static void setObjects() throws IOException {
        user = new User(1L, userName, userName);
        fileEntity = new File(null, user, fileName, hash, fileContent.getBytes());
    }

    @Test
    void jwtSecretInitializationTest() {
        Assertions.assertEquals("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
                jwtService.getJwtSecret());
    }

    @Disabled
    @Test
    void extractUsernameFromTokenTest() {
        // Дано (given)
        final String expected = "user";

        // Когда (when)
        String newToken = Jwts.builder()
                .setSubject(expected)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 3600 * 1000L))
                .signWith(SignatureAlgorithm.HS256, jwtService.getJwtSecret())
                .compact();

        String userName = jwtService.extractUsernameFromToken(newToken);

        // Тогда (then)
        Assertions.assertEquals(expected, userName);
    }


    @Test
    void saveFileTest200() throws IOException {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем проверять имя файла, вернем, что имя не занято
        Mockito.when(fileRepository.existsByFileNameAndUser(fileName, user))
                .thenReturn(false);
        // Когда будем сохранять файл, вернём ту же запись, как знак удачного сохранения
        Mockito.when(fileRepository.save(Mockito.any()))
                .thenReturn(fileEntity);

        assertThat(HttpStatus.OK, is(fileService.saveFile(fileContent, fileName, hash, token).getStatusCode()));
    }

    @Test
    void saveFileTest400() {
        // Когда будем получать пользователя, вернём пустого пользователя
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.empty());

        // Получим исключение с текстом Error input data
        assertThrows(ErrorInputDataException.class
                , () -> fileService.saveFile(fileContent, userName, hash, token)
                , ResponseText.ERROR_INPUT_DATA);
    }

    @Test
    void saveFileTest500() {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем проверять имя файла, вернем, что имя не занято
        Mockito.when(fileRepository.existsByFileNameAndUser(fileName, user))
                .thenReturn(false);
        // Когда будем сохранять файл, вернём null
        Mockito.when(fileRepository.save(Mockito.any()))
                .thenReturn(null);

        // Получим исключение 500, ошибка сервера
        Throwable exception = assertThrows(ServiceException.class,
                () -> fileService.saveFile(fileContent, userName, hash, token));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR + ResponseText.ERROR_UPLOAD_FILE, exception.getMessage());
    }


    @Test
    void getFileTest200() {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем проверять имя файла, что файл существует
        Mockito.when(fileRepository.existsByFileNameAndUser(fileName, user))
                .thenReturn(true);
        // Когда будем извлекать файл из БД, вернем наш объект file, он не пустой, всё отлично
        Mockito.when(fileRepository.findByFileNameAndUser(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.ofNullable(fileEntity));

        assertThat(HttpStatus.OK, is(fileService.getFile(fileName, token).getStatusCode()));
    }

    @Test
    void getFileTest400() {
        // Когда будем получать пользователя, вернём user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем проверять имя файла, скажем, что файл не существует
        Mockito.when(fileRepository.existsByFileNameAndUser(fileName, user))
                .thenReturn(false);

        // Получим исключение с текстом Error input data
        assertThrows(ErrorInputDataException.class
                , () -> fileService.getFile(fileName, token)
                , ResponseText.ERROR_INPUT_DATA);
    }

    @Test
    void getFileTest500() {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем проверять имя файла, вернем, что файл существует
        Mockito.when(fileRepository.existsByFileNameAndUser(fileName, user))
                .thenReturn(true);
        // Когда будем извлекать файл из БД, вернём null
        Mockito.when(fileRepository.findByFileNameAndUser(fileName, user))
                .thenReturn(Optional.empty());

        // Получим исключение 500, ошибка сервера
        Throwable exception = assertThrows(ServiceException.class,
                () -> fileService.getFile(fileName, token));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR + ResponseText.ERROR_UPLOAD_FILE, exception.getMessage());
    }


    @Test
    void deleteFileTest200() {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем проверять сколько у пользователя таких файлов, скажем, что один
        Mockito.when(fileRepository.countByFileNameAndUser(fileName, user))
                .thenReturn(1);
        // Когда будем извлекать файл из БД, вернем наш объект file
        Mockito.when(fileRepository.findByFileNameAndUser(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.ofNullable(fileEntity));
        // Когда будем проверять имя файла, что файл не существует
        Mockito.when(fileRepository.existsByFileNameAndUser(fileName, user))
                .thenReturn(false);

        assertThat(HttpStatus.OK, is(fileService.deleteFile(token, fileName).getStatusCode()));
    }

    @Test
    void deleteFileTest400() {
        // Когда будем получать пользователя, вернём user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем проверять сколько у пользователя таких файлов, скажем, что один
        Mockito.when(fileRepository.countByFileNameAndUser(fileName, user))
                .thenReturn(1);
        // Когда будем извлекать файл из БД, вернем null
        Mockito.when(fileRepository.findByFileNameAndUser(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        // Получим исключение с текстом Error input data
        assertThrows(ErrorInputDataException.class
                , () -> fileService.deleteFile(token, fileName)
                , ResponseText.ERROR_INPUT_DATA);
    }

    @Test
    void deleteFileTest500() {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем проверять сколько у пользователя таких файлов, скажем, что один
        Mockito.when(fileRepository.countByFileNameAndUser(fileName, user))
                .thenReturn(1);
        // Когда будем извлекать файл из БД, вернем наш объект file
        Mockito.when(fileRepository.findByFileNameAndUser(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.ofNullable(fileEntity));
        // Когда будем проверять имя файла, скажем, что файл существует
        Mockito.when(fileRepository.existsByFileNameAndUser(fileName, user))
                .thenReturn(true);

        // Получим исключение 500, ошибка сервера
        Throwable exception = assertThrows(ServiceException.class,
                () -> fileService.deleteFile(token, fileName));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR + ResponseText.ERROR_DELETE_FILE, exception.getMessage());
    }


    @Test
    void putFileTest200() throws IOException {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем извлекать файл из БД, вернем наш объект file
        Mockito.when(fileRepository.findByFileNameAndUser(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.ofNullable(fileEntity));
        // Когда будем извлекать файл из БД, вернем наш объект file
        Mockito.when(fileRepository.findById(Mockito.any()))
                .thenReturn(Optional.ofNullable(fileEntity));

        assertThat(HttpStatus.OK, is(fileService.putFile(token, fileName, fileContent).getStatusCode()));
    }

    @Test
    void putFileTest400() {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем извлекать файл из БД, вернем null
        Mockito.when(fileRepository.findByFileNameAndUser(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.empty());

        // Получим исключение с текстом Error input data
        assertThrows(ErrorInputDataException.class
                , () -> fileService.putFile(token, fileName, fileContent)
                , ResponseText.ERROR_INPUT_DATA);
    }

    @Test
    void putFileTest500() {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем извлекать файл из БД, вернем file
        Mockito.when(fileRepository.findByFileNameAndUser(Mockito.any(), Mockito.any()))
                .thenReturn(Optional.ofNullable(fileEntity));
        // Когда будем извлекать файл из БД, вернем null
        Mockito.when(fileRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        // Получим исключение 500, ошибка сервера
        Throwable exception = assertThrows(ServiceException.class,
                () -> fileService.putFile(token, fileName, fileContent));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR + ResponseText.ERROR_UPLOAD_FILE, exception.getMessage());
    }


    @Test
    void getListFilesTest200() {
        List<File> list = new ArrayList<>();
        list.add(fileEntity);
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем извлекать файл из БД, вернем наш объект file
        Mockito.when(fileRepository.findByUserWithLimit(Mockito.any(Long.class), Mockito.any(Integer.class)))
                .thenReturn(list);

        assertNotNull(fileService.getListFiles(token, limit));
    }

    @Test
    void getListFilesTest400() {
        // Когда будем получать пользователя, вернём null
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.empty());

        // Получим исключение с текстом Error input data
        assertThrows(ErrorInputDataException.class
                , () -> fileService.getListFiles(token, limit)
                , ResponseText.ERROR_INPUT_DATA);
    }

    @Test
    void getListFilesTest500() {
        // Когда будем получать пользователя, вернём пользователя user
        Mockito.when(userService.findUserByToken(token))
                .thenReturn(Optional.ofNullable(user));
        // Когда будем извлекать файл из БД, вернем null
        Mockito.when(fileRepository.findByUserWithLimit(Mockito.any(Long.class), Mockito.any(Integer.class)))
                .thenReturn(null);

        // Получим исключение 500, ошибка сервера
        Throwable exception = assertThrows(ServiceException.class,
                () -> fileService.getListFiles(token, limit));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR + ResponseText.ERROR_GET_FILE_LIST, exception.getMessage());
    }

}