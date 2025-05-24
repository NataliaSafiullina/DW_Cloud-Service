package ru.safiullina.dwCloudService.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.safiullina.dwCloudService.entity.File;
import ru.safiullina.dwCloudService.entity.User;
import ru.safiullina.dwCloudService.repository.FileRepository;

import java.io.IOException;

@Service
public class FileService {

    private final UserService userService;

    private final FileRepository fileRepository;

    public FileService(UserService userService, FileRepository fileRepository) {
        this.userService = userService;
        this.fileRepository = fileRepository;
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
}
