package ru.safiullina.dwCloudService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.safiullina.dwCloudService.entity.File;
import ru.safiullina.dwCloudService.entity.User;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    Boolean existsByFileName(String fileName);
    Optional<File> findByFileName(String fileName);


    Boolean existsByFileNameAndUser(String fileName, User user);
    Optional<File> findByFileNameAndUser(String fileName, User user);

    /**
     * Аннотация @Modifying, обозначающая, что этот запрос меняет данные в БД,
     * т.е. операции INSERT, UPDATE, DELETE.
     * @param id - id файла
     */
    @Modifying
    @Query("DELETE FROM File WHERE id = :id")
    void deleteAllById(@Param("id") Long id);

}
