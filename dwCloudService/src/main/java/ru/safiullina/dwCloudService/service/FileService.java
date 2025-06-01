package ru.safiullina.dwCloudService.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.safiullina.dwCloudService.dto.FileListResponse;
import ru.safiullina.dwCloudService.entity.File;
import ru.safiullina.dwCloudService.entity.User;
import ru.safiullina.dwCloudService.exeption.ErrorInputDataException;
import ru.safiullina.dwCloudService.exeption.ServiceException;
import ru.safiullina.dwCloudService.repository.FileRepository;
import ru.safiullina.dwCloudService.utils.ResponseText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final UserService userService;

    private final FileRepository fileRepository;


    public FileService(UserService userService, FileRepository fileRepository, JwtService jwtService) {
        this.userService = userService;
        this.fileRepository = fileRepository;
    }

    @Transactional
    public ResponseEntity<?> saveFile(MultipartFile file, String fileName, String hash, String token) throws IOException {

        // Ищем пользователя в БД
        Optional<User> user = userService.findUserByToken(token);
        if (user.isEmpty()) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Проверим, что имя файла уникально
        if (fileRepository.existsByFileNameAndUser(fileName, user.get())) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Сохраняем файл и проверяем, что мы сохранили
        File fileEntity = new File(user.get(), fileName, hash, file.getBytes());
        File savedFile = fileRepository.save(fileEntity);
        if (!fileEntity.equals(savedFile)) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseText.ERROR_UPLOAD_FILE);
        }

        return new ResponseEntity<>(ResponseText.SUCCESS_UPLOAD, HttpStatus.OK);
    }


    @Transactional
    public ResponseEntity<?> getFile(String fileName, String token) {

        // Ищем пользователя в БД
        Optional<User> user = userService.findUserByToken(token);
        if (user.isEmpty()) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Проверим, существует ли файл.
        if (!fileRepository.existsByFileNameAndUser(fileName, user.get())) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Получаем файл из БД
        Optional<File> file = fileRepository.findByFileNameAndUser(fileName, user.get());
        if (file.isEmpty()) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseText.ERROR_UPLOAD_FILE);
        }

        // Устанавливаем Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));

        return ResponseEntity.ok()
                .headers(headers)
                .body(file.get().getFileContent());
    }

    @Transactional
    public ResponseEntity<?> deleteFile(String token, String fileName) {

        // Ищем пользователя в БД
        Optional<User> user = userService.findUserByToken(token);
        if (user.isEmpty()) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Вообще сюда не должно попадать, если сохранение не давало сохранять файл с таким же именем
        if (fileRepository.countByFileNameAndUser(fileName, user.get()) > 1) {
            fileRepository.deleteByFileNameAndUserId(fileName, user.get().getId());
            return new ResponseEntity<>(ResponseText.SUCCESS_DELETED, HttpStatus.OK);
        }

        // Проверяем существует ли у данного пользователя требуемый файл.
        // И сразу его найдем.
        Optional<File> file = fileRepository.findByFileNameAndUser(fileName, user.get());
        if (file.isEmpty()) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }


        // Пытаемся удалить файл.
        fileRepository.deleteById(file.get().getId());
        // Проверим, что файл удален.
        if (fileRepository.existsByFileNameAndUser(fileName, user.get())) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseText.ERROR_DELETE_FILE);
        }

        return new ResponseEntity<>(ResponseText.SUCCESS_DELETED, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> putFile(String token, String fileName, MultipartFile fileForPut) throws IOException {

        // Ищем пользователя в БД
        Optional<User> user = userService.findUserByToken(token);
        if (user.isEmpty()) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Проверяем существует ли у данного пользователя требуемый файл.
        // И сразу его найдем.
        Optional<File> file = fileRepository.findByFileNameAndUser(fileName, user.get());
        if (file.isEmpty()) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Пытаемся обновить файл
        fileRepository.updateById(file.get().getId(), fileForPut.getBytes());
        Optional<File> savedFile = fileRepository.findById(file.get().getId());
        if (savedFile.isEmpty() && !fileForPut.equals(savedFile)) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseText.ERROR_UPLOAD_FILE);
        }

        return new ResponseEntity<>(ResponseText.SUCCESS_UPLOAD, HttpStatus.OK);
    }

    @Transactional
    public List<FileListResponse> getListFiles(String token, Integer limit) {

        // Ищем пользователя в БД
        Optional<User> user = userService.findUserByToken(token);
        if (user.isEmpty()) {
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }

        // Пытаемся получить список файлов.
        List<FileListResponse> fileListResponses = new ArrayList<>();
        try {
            List<File> fileList = fileRepository.findByUserWithLimit(user.get().getId(), limit);

            for (File file : fileList) {
                fileListResponses.add(new FileListResponse(file.getFileName(), file.getFileContent().length));
            }
        } catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseText.ERROR_GET_FILE_LIST);
        }

        return fileListResponses;
    }
}
