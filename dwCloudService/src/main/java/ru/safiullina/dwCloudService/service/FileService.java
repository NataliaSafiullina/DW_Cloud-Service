package ru.safiullina.dwCloudService.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.safiullina.dwCloudService.entity.File;
import ru.safiullina.dwCloudService.entity.User;
import ru.safiullina.dwCloudService.exeption.ErrorInputDataException;
import ru.safiullina.dwCloudService.exeption.ServiceException;
import ru.safiullina.dwCloudService.repository.FileRepository;
import ru.safiullina.dwCloudService.utils.ResponseText;

import java.io.IOException;
import java.util.Optional;

@Service
public class FileService {

    private final UserService userService;

    private final FileRepository fileRepository;

    private final JwtService jwtService;

    public FileService(UserService userService, FileRepository fileRepository, JwtService jwtService) {
        this.userService = userService;
        this.fileRepository = fileRepository;
        this.jwtService = jwtService;
    }

    public void saveFile(MultipartFile file, String fileName, String hash, String userName) throws IOException {

        if (userService.existsByUsername(userName)) {
            User user = userService.findByUsername(userName);
            File fileEntity = new File(user, fileName, hash, file.getBytes());
            fileRepository.save(fileEntity);
        }
    }

    public Boolean existFile(String fileName) {
        return fileRepository.existsByFileName(fileName);
    }

    public byte[] getFile(String fileName) {
        if (fileRepository.existsByFileName(fileName)) {
            return fileRepository.findByFileName(fileName).get().getFileContent();
        }
        return null;
    }

    @Transactional
    public ResponseEntity<?> deleteFile(String token, String fileName) {

        // Получаем имя пользователя из токена.
        String userName = jwtService.extractUsernameFromToken(token);
        // Получаем объект пользователя по его имени.
        User user = userService.findByUsername(userName);
        if (user == null) {
            // Бросаем исключение и попадаем в итоге в handleErrorInputDataException()
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Проверяем существует ли у данного пользователя требуемый файл.
        // И сразу его найдем.
        Optional<File> file = fileRepository.findByFileNameAndUser(fileName, user);
        if (file.isEmpty()) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        System.out.println(" +++ File ID = " + file.get().getId());

        // Пытаемся удалить файл.
        fileRepository.deleteAllById(file.get().getId());
        // Проверим, что файл удален.
        if (fileRepository.existsByFileNameAndUser(fileName, user)) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseText.ERROR_DELETE_FILE);
        }

        return new ResponseEntity<>(ResponseText.SUCCESS_DELETED, HttpStatus.OK);
    }
}
