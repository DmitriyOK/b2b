package ru.protal.b2b;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import ru.protal.b2b.repository.dao.UserDao;
import ru.protal.b2b.repository.dao.UserStatusDao;
import ru.protal.b2b.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    public void saveUserSuccess(){
        UserDao expectedUser = UserDao.builder()
                .firstName("ivan")
                .middleName("ivanovich")
                .lastName("ivanov")
                .email("abcs@das.com")
                .login("ivan234")
                .password("asasd")
                .status(UserStatusDao.builder()
                        .status("NEW").build())
                .build();

        entityManager.persist(expectedUser);
        entityManager.flush();

        UserDao currentUser = userRepository.findByLogin("ivan234");

        assertThat(expectedUser).isEqualTo(currentUser);
    }
}
