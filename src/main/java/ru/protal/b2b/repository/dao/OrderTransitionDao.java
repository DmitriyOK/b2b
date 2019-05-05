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
@Table(name = "order_transition")
public class OrderTransitionDao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator ="transitionseq")
    @SequenceGenerator(name = "transitionseq", sequenceName = "transition_seq")
    @Column(name = "transition_id", unique = true)
    Long transitionId;

    @JoinColumn(name = "order_id", nullable = false)
    Long orderId;

    @Column(name = "from_state_id", nullable = false, length = 35)
    Long fromStateId;

    @Column(name = "to_state_id", nullable = false, length = 35)
    Long toStateId;;

    @Column(name = "result", nullable = false, length = 35)
    Long resultId;
}
