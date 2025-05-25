package ru.safiullina.dwCloudService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.safiullina.dwCloudService.service.FileService;
import ru.safiullina.dwCloudService.service.JwtService;
import ru.safiullina.dwCloudService.service.UserService;

import java.io.IOException;

@RestController
@RequestMapping("/cloud")
public class BusinessController {

    private final UserService userService;
    private final FileService fileService;
    private final JwtService jwtService;

    public BusinessController(UserService userService, FileService fileService, JwtService jwtService) {
        this.userService = userService;
        this.fileService = fileService;
        this.jwtService = jwtService;
    }

    @GetMapping("/count")
    public Long getUsersCount(@RequestHeader("auth-token") String authToken) {
        System.out.println("5 +++ Token from header = " + authToken);
        System.out.println("51 +++ User = " + jwtService.extractUsernameFromToken(authToken));
        return userService.getUsersCount();
    }

    /**
     * Сохранение файла в БД
     */
    @PostMapping("/file")
    public ResponseEntity<?> postFile(@RequestHeader("auth-token") String authToken,
                                      @RequestParam("filename") String fileName,
                                      @RequestPart("hash") String hash,
                                      @RequestPart("file") MultipartFile file) throws IOException {

        if (!file.isEmpty()) {
            // Обработка файла (например, сохранение)
            System.out.println("Имя файла: " + file.getOriginalFilename());

            String userName = jwtService.extractUsernameFromToken(authToken);
            fileService.saveFile(file, fileName, hash, userName);

            if (fileService.existFile(fileName)) {
                return ResponseEntity.ok("Файл успешно принят и загружен!");
            } else {
                return ResponseEntity.ok("Файл успешно принят, но не сохранен.");
            }

        }

        return ResponseEntity.badRequest().body("Ошибка загрузки файла");
    }

    /**
     *  Получение файла из БД и отправка на фронт.
     */
    @GetMapping("/file")
    public ResponseEntity<?> getFile(@RequestHeader("auth-token") String authToken,
                                     @RequestParam("filename") String fileName) {

        byte[] fileContent = fileService.getFile(fileName);
        if (fileContent != null) {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Устанавливаем Content-Type

            // return ResponseEntity.ok(fileContent);
            // return ResponseEntity.ok(new FileResponse(fileName, Arrays.toString(fileContent)));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        }

        return ResponseEntity.badRequest().body("Ошибка получения файла");
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@RequestHeader("auth-token") String authToken,
                                        @RequestParam("filename") String fileName) {

        return fileService.deleteFile(authToken, fileName);
    }

    // TODO: put file
    @PutMapping("/file")
    public ResponseEntity<?> putFile() {
        return null;
    }

    // TODO: get list
    @GetMapping("/list")
    public ResponseEntity<?> getList() {
        return null;
    }


}
