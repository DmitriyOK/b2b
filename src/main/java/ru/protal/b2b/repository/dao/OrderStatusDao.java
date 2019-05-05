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
@Table(name = "order_status")
public class OrderStatusDao  {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderstatusseq")
    @SequenceGenerator(name = "orderstatusseq", sequenceName = "order_state_seq")
    @Column(name = "order_state_id", unique = true)
    Long stateId;

    @Column(name = "name", unique = true)
    String name;
}

