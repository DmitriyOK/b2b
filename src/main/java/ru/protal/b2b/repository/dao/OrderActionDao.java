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
@Table(name = "order_action")
public class OrderActionDao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderactionseq")
    @SequenceGenerator(name = "orderactionseq", sequenceName = "order_action_seq")
    @Column(name = "order_action_id", unique = true)
    Long state_id;

    @Column(name = "name", unique = true)
    String name;
}
