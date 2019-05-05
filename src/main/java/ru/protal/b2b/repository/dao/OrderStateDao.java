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
@Table(name = "order_state")
public class OrderStateDao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stateseq")
    @SequenceGenerator(name = "stateseq", sequenceName = "state_seq")
    @Column(name = "state_id", unique = true)
    Long stateId;

    @Column(name = "name", unique = true)
    String name;
}
