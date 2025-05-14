package ru.safiullina.dwCloudService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.safiullina.dwCloudService.entity.File;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByFileName(String fileName);

    Boolean existsByFileName(String fileName);

}
