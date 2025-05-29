package ru.safiullina.dwCloudService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.safiullina.dwCloudService.dto.FileListResponse;
import ru.safiullina.dwCloudService.exeption.ErrorInputDataException;
import ru.safiullina.dwCloudService.service.FileService;
import ru.safiullina.dwCloudService.service.JwtService;
import ru.safiullina.dwCloudService.service.UserService;

import java.io.IOException;
import java.util.List;

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

        return fileService.saveFile(file,fileName,hash,authToken);
    }

    /**
     *  Получение файла из БД и отправка на фронт.
     */
    @GetMapping("/file")
    public ResponseEntity<?> getFile(@RequestHeader("auth-token") String authToken,
                                     @RequestParam("filename") String fileName) {

        return fileService.getFile(fileName, authToken);

    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@RequestHeader("auth-token") String authToken,
                                        @RequestParam("filename") String fileName) {

        return fileService.deleteFile(authToken, fileName);
    }

    @PutMapping("/file")
    public ResponseEntity<?> putFile(@RequestHeader("auth-token") String authToken,
                                     @RequestParam("filename") String fileName,
                                     @RequestPart("file") MultipartFile file) throws IOException {

        return fileService.putFile(authToken,fileName,file);
    }


    @GetMapping("/list")
    public List<FileListResponse> getList(@RequestHeader("auth-token") String authToken,
                                          @RequestParam("limit") Integer limit) {
        return fileService.getListFiles(authToken, limit);
    }


}
