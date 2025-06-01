package ru.safiullina.dwCloudService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.safiullina.dwCloudService.entity.File;
import ru.safiullina.dwCloudService.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    Boolean existsByFileNameAndUser(String fileName, User user);

    Optional<File> findByFileNameAndUser(String fileName, User user);

    Integer countByFileNameAndUser(String fileName, User user);

    @Modifying
    @Query(value = "DELETE FROM files WHERE file_name = :file_name AND user_id = :user_id", nativeQuery = true)
    void deleteByFileNameAndUserId(@Param("file_name") String fileName, @Param("user_id") Long userId);

    /**
     * Аннотация @Modifying, обозначающая, что этот запрос меняет данные в БД,
     * т.е. операции INSERT, UPDATE, DELETE.
     *
     * @param id - id файла
     */
    @Modifying
    @Query("DELETE FROM File WHERE id = :id")
    void deleteById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE File f SET f.fileContent = :file WHERE f.id = :id")
    void updateById(@Param("id") Long id, @Param("file") byte[] bytes);

    @Query(value = "SELECT * FROM files WHERE user_id = :user LIMIT :limit", nativeQuery = true)
    List<File> findByUserWithLimit(@Param("user") Long userId, @Param("limit") int limit);
}
