package ru.protal.b2b.repository.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.protal.b2b.controller.dto.response.UserInfo;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserDao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator ="userseq")
    @SequenceGenerator(name = "userseq", sequenceName = "users_seq")
    @Column(name = "user_id", unique = true)
    Long userId;

    @Column(name = "login", nullable = false, unique = true, length = 35)
    String login;

    @Column(name = "email", nullable = false, unique = true, length = 35)
    String email;

    @Column(name = "firstName", nullable = false, length = 35)
    String firstName;

    @Column(name = "middleName", nullable = false, length = 35)
    String middleName;

    @Column(name = "lastName", nullable = false, length = 35)
    String lastName;

    @Column(name = "password",nullable = false)
    String password;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "status_id", referencedColumnName = "status_id")
    UserStatusDao status;

    public static UserInfo from(UserDao user){
        return UserInfo.builder()
                .id(user.getUserId())
                .login(user.getLogin())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .status(user.getStatus().getStatus())
                .build();
    }
}

