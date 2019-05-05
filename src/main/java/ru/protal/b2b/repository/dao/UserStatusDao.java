package ru.protal.b2b.repository.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_status")
public class UserStatusDao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator ="userstatseq")
    @SequenceGenerator(name = "userstatseq", sequenceName = "user_stat_seq")
    @Column(name = "status_id", unique = true)
    Long statusId;

    @Column(name = "status", unique = true)
    String status;
}


