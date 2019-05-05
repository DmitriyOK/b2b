package ru.protal.b2b.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.protal.b2b.repository.dao.UserDao;

import java.util.List;

public interface UserRepository extends JpaRepository<UserDao, Long> {

    UserDao findByLogin(String login);
    UserDao findByLoginOrEmail(String login, String eMail);

    @Query("SELECT u FROM UserDao u LEFT JOIN OrderDao o ON u.userId = o.user WHERE o IS NULL")
    List<UserDao> findAllWhereOrderIsNull();
}
