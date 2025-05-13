package ru.safiullina.dwCloudService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.safiullina.dwCloudService.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
