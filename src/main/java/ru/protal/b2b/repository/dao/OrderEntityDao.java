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
@Table(name = "order_entity")
public class OrderEntityDao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderentityseq")
    @SequenceGenerator(name = "orderentityseq", sequenceName = "order_entity_seq")
    @Column(name = "order_entity_id", unique = true)
    Long orderEntityId;

    @Column(name = "name", unique = true)
    String name;
}
