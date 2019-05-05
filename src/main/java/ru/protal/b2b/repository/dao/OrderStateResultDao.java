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
@Table(name = "order_state_result")
public class OrderStateResultDao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderstateresultseq")
    @SequenceGenerator(name = "orderstateresultseq", sequenceName = "order_state_result_seq")
    @Column(name = "order_state_result_id", unique = true)
    Long orderStateResultId;

    @Column(name = "name", unique = true)
    String name;
}
